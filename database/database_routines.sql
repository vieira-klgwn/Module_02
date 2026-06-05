-- =============================================================================
-- WASAC & REG Utility Billing — PostgreSQL Database Routines
-- =============================================================================
--
-- Contents:
--   1. Helper functions (message formatting)
--   2. Stored procedure: process full payment + notify customer
--   3. Trigger: on bill INSERT → insert notification
--   4. Trigger: on payment INSERT → update bill status + notify on full payment
--   5. Cursor example: list customers with unpaid bills
--
-- Message format:
--   Dear <CustomerName>
--   Your <Month/Year> utility bill of <Amount> FRW has been successfully processed
--
-- =============================================================================
-- HOW TO RUN IN psql
-- =============================================================================
--
-- 1. Open a terminal and connect to PostgreSQL:
--
--      psql -h localhost -U postgres -d springboot_practice
--
--    (Enter your postgres password when prompted.)
--
-- 2. Run this entire script from psql:
--
--      \i /home/vieira/Documents/National_Exams_Java/database/database_routines.sql
--
--    Or from the shell without entering psql interactively:
--
--      psql -h localhost -U postgres -d springboot_practice \
--        -f /home/vieira/Documents/National_Exams_Java/database/database_routines.sql
--
-- 3. Verify objects were created:
--
--      \df fn_*          -- list functions
--      \df sp_*          -- list procedures
--      \dft              -- list triggers (on tables)
--
-- 4. Test bill notification trigger (manual insert — use existing customer_id):
--
--      INSERT INTO bills (
--          bill_reference, customer_id, meter_id, meter_reading_id, tariff_id,
--          billing_month, billing_year, consumption, subtotal, vat_amount,
--          fixed_charge, penalty_amount, total_amount, remaining_balance, status, generated_at
--      ) VALUES (
--          'BILL-TEST001', 1, 1, 1, 1,
--          6, 2024, 50, 17500, 3240, 500, 0, 21240, 21240, 'PENDING', NOW()
--      );
--
--      SELECT id, subject, message FROM notifications ORDER BY id DESC LIMIT 1;
--
-- 5. Test full payment procedure:
--
--      CALL sp_process_full_payment(1, 21240.00, 'MOBILE_MONEY', CURRENT_DATE);
--
-- 6. Run cursor demo:
--
--      CALL sp_list_unpaid_bills_report();
--
-- NOTE: The Spring Boot app also sends emails and saves notifications.
--       Triggers include duplicate guards (reference_type + reference_id).
--       Install these routines for exam database-level requirements.
--
-- =============================================================================

-- ---------------------------------------------------------------------------
-- Cleanup (safe re-run)
-- ---------------------------------------------------------------------------
DROP TRIGGER IF EXISTS trg_bill_notification ON bills;
DROP TRIGGER IF EXISTS trg_payment_full_settlement ON payments;
DROP FUNCTION IF EXISTS trg_bill_notification_fn() CASCADE;
DROP FUNCTION IF EXISTS trg_payment_full_settlement_fn() CASCADE;
DROP PROCEDURE IF EXISTS sp_process_full_payment(BIGINT, NUMERIC, VARCHAR, DATE);
DROP PROCEDURE IF EXISTS sp_list_unpaid_bills_report();
DROP FUNCTION IF EXISTS fn_build_bill_message(VARCHAR, INT, INT, NUMERIC) CASCADE;
DROP FUNCTION IF EXISTS fn_month_year_label(INT, INT) CASCADE;

-- ---------------------------------------------------------------------------
-- 1. Helper: month/year label (e.g. June/2024)
-- ---------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION fn_month_year_label(p_month INT, p_year INT)
RETURNS VARCHAR
LANGUAGE plpgsql
AS $$
DECLARE
    month_names VARCHAR[] := ARRAY[
        'January','February','March','April','May','June',
        'July','August','September','October','November','December'
    ];
BEGIN
    IF p_month < 1 OR p_month > 12 THEN
        RAISE EXCEPTION 'Invalid billing month: %', p_month;
    END IF;
    RETURN month_names[p_month] || '/' || p_year::VARCHAR;
END;
$$;

-- ---------------------------------------------------------------------------
-- 2. Helper: standard notification message
-- ---------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION fn_build_bill_message(
    p_customer_name VARCHAR,
    p_month         INT,
    p_year          INT,
    p_amount        NUMERIC
)
RETURNS TEXT
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN 'Dear ' || p_customer_name || E'\n\n'
        || 'Your ' || fn_month_year_label(p_month, p_year)
        || ' utility bill of ' || TRIM(TRAILING '.' FROM TRIM(TRAILING '0' FROM p_amount::TEXT))
        || ' FRW has been successfully processed';
END;
$$;

-- ---------------------------------------------------------------------------
-- 3. Stored Procedure: record payment, update bill on full payment, notify
-- ---------------------------------------------------------------------------
CREATE OR REPLACE PROCEDURE sp_process_full_payment(
    p_bill_id        BIGINT,
    p_amount_paid    NUMERIC,
    p_payment_method VARCHAR,
    p_payment_date   DATE
)
LANGUAGE plpgsql
AS $$
DECLARE
    v_bill           RECORD;
    v_customer       RECORD;
    v_total_paid     NUMERIC;
    v_new_balance    NUMERIC;
    v_message        TEXT;
    v_subject        VARCHAR;
    v_test_recipient VARCHAR := 'klgwnboy@gmail.com';
BEGIN
    SELECT b.*, c.full_name AS customer_name
    INTO v_bill
    FROM bills b
    JOIN customers c ON c.id = b.customer_id
    WHERE b.id = p_bill_id
    FOR UPDATE;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Bill not found: %', p_bill_id;
    END IF;

    IF v_bill.status = 'CANCELLED' THEN
        RAISE EXCEPTION 'Cannot pay a cancelled bill';
    END IF;

    IF p_amount_paid <= 0 THEN
        RAISE EXCEPTION 'Payment amount must be positive';
    END IF;

    IF p_amount_paid > v_bill.remaining_balance THEN
        RAISE EXCEPTION 'Payment amount exceeds remaining balance';
    END IF;

    INSERT INTO payments (
        bill_id, bill_reference, amount_paid, payment_method, payment_date, created_at
    ) VALUES (
        p_bill_id, v_bill.bill_reference, p_amount_paid, p_payment_method, p_payment_date, NOW()
    );

    v_new_balance := v_bill.remaining_balance - p_amount_paid;

    IF v_new_balance = 0 THEN
        UPDATE bills
        SET status = 'PAID',
            remaining_balance = 0
        WHERE id = p_bill_id;

        v_message := fn_build_bill_message(
            v_bill.customer_name, v_bill.billing_month, v_bill.billing_year, v_bill.total_amount
        );
        v_subject := 'Bill Paid - ' || fn_month_year_label(v_bill.billing_month, v_bill.billing_year);

        IF NOT EXISTS (
            SELECT 1 FROM notifications
            WHERE reference_type = 'PAYMENT' AND reference_id = p_bill_id
              AND message LIKE 'Dear %has been successfully processed'
        ) THEN
            INSERT INTO notifications (
                customer_id, subject, message, status,
                reference_type, reference_id, recipient_email, created_at
            ) VALUES (
                v_bill.customer_id, v_subject, v_message, 'SENT',
                'PAYMENT', p_bill_id, v_test_recipient, NOW()
            );
        END IF;

        RAISE NOTICE 'Bill % fully paid. Notification inserted for customer %.',
            v_bill.bill_reference, v_bill.customer_name;
    ELSE
        UPDATE bills
        SET status = 'PARTIALLY_PAID',
            remaining_balance = v_new_balance
        WHERE id = p_bill_id;

        RAISE NOTICE 'Partial payment recorded. Remaining balance: % FRW', v_new_balance;
    END IF;
END;
$$;

-- ---------------------------------------------------------------------------
-- 4. Trigger function: AFTER INSERT on bills → insert notification
-- ---------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION trg_bill_notification_fn()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
DECLARE
    v_customer_name  VARCHAR;
    v_message        TEXT;
    v_subject        VARCHAR;
    v_test_recipient VARCHAR := 'klgwnboy@gmail.com';
BEGIN
    SELECT full_name INTO v_customer_name
    FROM customers
    WHERE id = NEW.customer_id;

    IF v_customer_name IS NULL THEN
        RAISE EXCEPTION 'Customer not found for bill customer_id=%', NEW.customer_id;
    END IF;

    v_message := fn_build_bill_message(
        v_customer_name, NEW.billing_month, NEW.billing_year, NEW.total_amount
    );
    v_subject := 'Utility Bill - ' || fn_month_year_label(NEW.billing_month, NEW.billing_year);

    IF NOT EXISTS (
        SELECT 1 FROM notifications
        WHERE reference_type = 'BILL' AND reference_id = NEW.id
    ) THEN
        INSERT INTO notifications (
            customer_id, subject, message, status,
            reference_type, reference_id, recipient_email, created_at
        ) VALUES (
            NEW.customer_id, v_subject, v_message, 'SENT',
            'BILL', NEW.id, v_test_recipient, NOW()
        );
    END IF;

    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_bill_notification
    AFTER INSERT ON bills
    FOR EACH ROW
    EXECUTE FUNCTION trg_bill_notification_fn();

-- ---------------------------------------------------------------------------
-- 5. Trigger function: AFTER INSERT on payments → full payment settlement
-- ---------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION trg_payment_full_settlement_fn()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
DECLARE
    v_bill           RECORD;
    v_total_paid     NUMERIC;
    v_message        TEXT;
    v_subject        VARCHAR;
    v_test_recipient VARCHAR := 'klgwnboy@gmail.com';
BEGIN
    SELECT b.*, c.full_name AS customer_name
    INTO v_bill
    FROM bills b
    JOIN customers c ON c.id = b.customer_id
    WHERE b.id = NEW.bill_id;

    IF NOT FOUND THEN
        RETURN NEW;
    END IF;

    SELECT COALESCE(SUM(amount_paid), 0)
    INTO v_total_paid
    FROM payments
    WHERE bill_id = NEW.bill_id;

    IF v_total_paid >= v_bill.total_amount AND v_bill.status <> 'PAID' THEN
        UPDATE bills
        SET status = 'PAID',
            remaining_balance = 0
        WHERE id = NEW.bill_id;

        v_message := fn_build_bill_message(
            v_bill.customer_name, v_bill.billing_month, v_bill.billing_year, v_bill.total_amount
        );
        v_subject := 'Bill Paid - ' || fn_month_year_label(v_bill.billing_month, v_bill.billing_year);

        IF NOT EXISTS (
            SELECT 1 FROM notifications
            WHERE reference_type = 'PAYMENT' AND reference_id = NEW.bill_id
              AND message LIKE '%has been successfully processed'
        ) THEN
            INSERT INTO notifications (
                customer_id, subject, message, status,
                reference_type, reference_id, recipient_email, created_at
            ) VALUES (
                v_bill.customer_id, v_subject, v_message, 'SENT',
                'PAYMENT', NEW.bill_id, v_test_recipient, NOW()
            );
        END IF;
    END IF;

    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_payment_full_settlement
    AFTER INSERT ON payments
    FOR EACH ROW
    EXECUTE FUNCTION trg_payment_full_settlement_fn();

-- ---------------------------------------------------------------------------
-- 6. Cursor example: report customers with unpaid approved bills
-- ---------------------------------------------------------------------------
CREATE OR REPLACE PROCEDURE sp_list_unpaid_bills_report()
LANGUAGE plpgsql
AS $$
DECLARE
    bill_cursor CURSOR FOR
        SELECT b.id,
               b.bill_reference,
               c.full_name,
               fn_month_year_label(b.billing_month, b.billing_year) AS period,
               b.total_amount,
               b.remaining_balance,
               b.status
        FROM bills b
        JOIN customers c ON c.id = b.customer_id
        WHERE b.status IN ('APPROVED', 'PARTIALLY_PAID')
          AND b.remaining_balance > 0
        ORDER BY b.generated_at;

    v_row RECORD;
BEGIN
    RAISE NOTICE '=== Unpaid Bills Report ===';
    OPEN bill_cursor;
    LOOP
        FETCH bill_cursor INTO v_row;
        EXIT WHEN NOT FOUND;
        RAISE NOTICE 'Bill % | Customer: % | Period: % | Due: % FRW | Status: %',
            v_row.bill_reference, v_row.full_name, v_row.period,
            v_row.remaining_balance, v_row.status;
    END LOOP;
    CLOSE bill_cursor;
    RAISE NOTICE '=== End of Report ===';
END;
$$;

-- ---------------------------------------------------------------------------
-- Done
-- ---------------------------------------------------------------------------
DO $$
BEGIN
    RAISE NOTICE 'Database routines installed successfully.';
    RAISE NOTICE 'Test recipient email (app + DB): klgwnboy@gmail.com';
END;
$$;
