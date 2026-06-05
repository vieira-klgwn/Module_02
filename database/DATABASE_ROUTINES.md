# Database Routines Guide

PostgreSQL **triggers**, **stored procedures**, and a **cursor** for the Utility Billing System.

**Script location:** [`database/database_routines.sql`](database/database_routines.sql)

---

## What the routines do

| Object | Type | Behaviour |
|--------|------|-----------|
| `fn_month_year_label` | Function | Formats month/year as `June/2024` |
| `fn_build_bill_message` | Function | Builds the standard notification text |
| `trg_bill_notification` | **Trigger** on `bills` INSERT | Inserts a notification when a bill is generated |
| `trg_payment_full_settlement` | **Trigger** on `payments` INSERT | On full payment: sets bill to `PAID` and inserts notification |
| `sp_process_full_payment` | **Stored procedure** | Records payment, updates bill, notifies on full payment |
| `sp_list_unpaid_bills_report` | **Procedure + cursor** | Prints all unpaid approved bills |

### Notification message format

```
Dear <CustomerName>

Your <Month/Year> utility bill of <Amount> FRW has been successfully processed
```

---

## Prerequisites

1. PostgreSQL running locally
2. Database `springboot_practice` exists (same as `application.properties`)
3. Spring Boot app has been started at least once so Hibernate created the tables (`bills`, `payments`, `notifications`, `customers`, etc.)

---

## How to run in psql

### Option A — Interactive psql

```bash
psql -h localhost -U postgres -d springboot_practice
```

Inside psql:

```sql
\i /home/vieira/Documents/National_Exams_Java/database/database_routines.sql
```

Verify:

```sql
\df fn_*
\df sp_*
```

You should see the functions and procedures listed.

### Option B — One command from terminal

```bash
psql -h localhost -U postgres -d springboot_practice \
  -f /home/vieira/Documents/National_Exams_Java/database/database_routines.sql
```

---

## Testing the trigger (bill generation)

After you have at least one customer, meter, reading, and tariff in the database:

```sql
-- Check existing IDs first
SELECT id, full_name FROM customers LIMIT 5;
SELECT id FROM meters LIMIT 5;
SELECT id FROM meter_readings LIMIT 5;
SELECT id FROM tariffs LIMIT 5;

-- Insert a test bill (adjust IDs to match your data)
INSERT INTO bills (
    bill_reference, customer_id, meter_id, meter_reading_id, tariff_id,
    billing_month, billing_year, consumption, subtotal, vat_amount,
    fixed_charge, penalty_amount, total_amount, remaining_balance, status, generated_at
) VALUES (
    'BILL-DBTEST01', 1, 1, 1, 1,
    6, 2024, 55, 19250, 3555, 500, 0, 23305, 23305, 'PENDING', NOW()
);

-- Verify notification was inserted by trigger
SELECT id, subject, message, recipient_email, created_at
FROM notifications
ORDER BY id DESC
LIMIT 1;
```

Expected `message`:

```
Dear <CustomerName>

Your June/2024 utility bill of 23305 FRW has been successfully processed
```

---

## Testing the stored procedure (full payment)

```sql
-- Approve the bill first if needed (app normally does this)
UPDATE bills SET status = 'APPROVED' WHERE bill_reference = 'BILL-DBTEST01';

-- Process full payment via stored procedure
CALL sp_process_full_payment(
    (SELECT id FROM bills WHERE bill_reference = 'BILL-DBTEST01'),
    23305.00,
    'MOBILE_MONEY',
    CURRENT_DATE
);

-- Verify bill is PAID
SELECT bill_reference, status, remaining_balance FROM bills WHERE bill_reference = 'BILL-DBTEST01';

-- Verify payment notification
SELECT subject, message FROM notifications WHERE reference_type = 'PAYMENT' ORDER BY id DESC LIMIT 1;
```

---

## Testing the cursor (unpaid bills report)

```sql
CALL sp_list_unpaid_bills_report();
```

Output appears as `NOTICE` lines in psql showing each unpaid bill.

---

## Email notifications (Spring Boot)

Database routines insert rows into `notifications`. **Email delivery** is handled by the Java application:

- All test emails go to: **`klgwnboy@gmail.com`**
- Configured in `application.properties`:
  - `app.notification.test-recipient=klgwnboy@gmail.com`
  - Gmail SMTP settings under `spring.mail.*`

When you generate a bill or record a payment through the **API**, the app:

1. Saves the notification in the database
2. Sends an email to `klgwnboy@gmail.com`

### Test email via API

1. Start the app: `./mvnw spring-boot:run`
2. Login as finance: `finance@wasac.rw` / `Finance@123`
3. Generate and approve a bill (see `TESTING_WALKTHROUGH.md`)
4. Check inbox at `klgwnboy@gmail.com`

---

## Spring app vs database triggers

| Layer | Bill notification | Full payment notification | Email |
|-------|-------------------|---------------------------|-------|
| **Spring Boot** | Yes | Yes | Yes → `klgwnboy@gmail.com` |
| **PostgreSQL triggers** | On `bills` INSERT | On `payments` INSERT when fully paid | No (DB only) |

Triggers include duplicate guards so re-running the same event does not insert duplicate notifications.

To remove triggers:

```sql
DROP TRIGGER IF EXISTS trg_bill_notification ON bills;
DROP TRIGGER IF EXISTS trg_payment_full_settlement ON payments;
```

---

## Troubleshooting

| Problem | Solution |
|---------|----------|
| `relation "bills" does not exist` | Start the Spring Boot app once to create tables |
| `column recipient_email does not exist` | Restart app so Hibernate adds the new column, or run: `ALTER TABLE notifications ADD COLUMN IF NOT EXISTS recipient_email VARCHAR(255);` |
| Email not received | Check Gmail app password, spam folder, and `spring.mail.password` in `application.properties` |
| Duplicate notifications | Normal if both API and manual SQL insert fire; triggers skip duplicates by `reference_type` + `reference_id` |
