# Full Application Testing Walkthrough



**Base URL:** `http://localhost:8080`

---

## The Story

**WASAC** bills water. **REG** bills electricity.

You will play four people across one billing cycle:

| Person | Login | Role |
|--------|-------|------|
| Patrick (IT Admin) | `admin@wasac.rw` / `Admin@1234` | Sets up tariffs and staff |
| Alice (Field Operator) | `operator@wasac.rw` / `Operator@123` | Records meter readings |
| David (Finance) | `finance@wasac.rw` / `Finance@123` | Generates bills and collects payments |
| Marie (Resident) | Signs up herself | Views bills as a customer |

**Customer in this scenario:** Marie Uwase — national ID `1199880076543210`, phone `0788123456`.

---


## Phase 1 — Patrick (Admin) Sets Up the System

Patrick is the system administrator. He logs in, confirms tariffs exist, registers the customer, and installs meters.

---

### Step 1.1 — Admin logs in

**POST** `{{baseUrl}}/api/auth/login`

```json
{
  "email": "admin@wasac.rw",
  "password": "Admin@1234"
}
```

**What you should see:** `200 OK`

```json
{
  "access_token": "eyJhbGciOiJIUzUxMiJ9...",
  "refresh_token": "eyJhbGciOiJIUzUxMiJ9..."
}
```

**Save:** `access_token` → `adminToken`

**Verify:** Copy `adminToken` and call **GET** `{{baseUrl}}/api/users/me` with Bearer auth. You should see Patrick's profile with `"role": "ADMIN"`.

---

### Step 1.2 — Patrick creates water tariff (WASAC)

**POST** `{{baseUrl}}/api/tariffs`  
**Token:** `adminToken`

```json
{
  "name": "WASAC Water Standard 2024",
  "utilityType": "WATER",
  "tariffType": "FLAT",
  "effectiveFrom": "2024-01-01",
  "vatRate": 18,
  "fixedServiceCharge": 500,
  "flatRate": 350
}
```

**What you should see:** `201 Created` with `"version": 1`, `"utilityType": "WATER"`.

This means: **350 FRW per unit** of water, plus **500 FRW** fixed charge, plus **18% VAT**.

---

### Step 1.3 — Patrick creates electricity tariff (REG)

**POST** `{{baseUrl}}/api/tariffs`  
**Token:** `adminToken`

```json
{
  "name": "REG Electricity Tiered 2024",
  "utilityType": "ELECTRICITY",
  "tariffType": "TIERED",
  "effectiveFrom": "2024-01-01",
  "vatRate": 18,
  "fixedServiceCharge": 1000,
  "penaltyRate": 5,
  "tiers": [
    { "minConsumption": 0, "maxConsumption": 100, "ratePerUnit": 120 },
    { "minConsumption": 100, "ratePerUnit": 180 }
  ]
}
```

**What you should see:** `201 Created` with two tiers listed.

**Verify:** **GET** `{{baseUrl}}/api/tariffs` — you should see both tariffs.

---

### Step 1.4 — Patrick registers customer Marie

**POST** `{{baseUrl}}/api/customers`  
**Token:** `adminToken`

```json
{
  "fullName": "Marie Uwase",
  "nationalId": "1199880076543210",
  "address": "KG 15 Ave, Kigali",
  "phoneNumber": "0788123456",
  "email": "marie.uwase@wasac.rw",
  "password": "Marie@2024"
}
```

**Role:** ADMIN only

**What you should see:** `201 Created`

```json
{
  "id": 1,
  "fullName": "Marie Uwase",
  "nationalId": "1199880076543210",
  "phoneNumber": "0788123456",
  "status": "ACTIVE"
}
```

**Save:** `id` → `customerId`

---

### Step 1.5 — Patrick installs Marie's water meter

**POST** `{{baseUrl}}/api/meters`  
**Token:** `adminToken`

```json
{
  "meterNumber": "WASAC-WTR-10001",
  "type": "WATER",
  "installationDate": "2024-03-01",
  "customerId": 1
}
```

Use your actual `customerId` if it is not `1`.

**What you should see:** `201 Created` with `"type": "WATER"`, `"status": "ACTIVE"`.

**Save:** `id` → `waterMeterId`

---

### Step 1.6 — Patrick installs Marie's electricity meter

**POST** `{{baseUrl}}/api/meters`  
**Token:** `adminToken`

```json
{
  "meterNumber": "REG-ELC-20001",
  "type": "ELECTRICITY",
  "installationDate": "2024-03-01",
  "customerId": 1
}
```

**Save:** `id` → `elecMeterId`

**Verify:** **GET** `{{baseUrl}}/api/meters/customer/1` — Marie should have **2 meters** (water + electricity).

---

### Step 1.7 — Patrick creates an extra operator (optional)

**POST** `{{baseUrl}}/api/users`  
**Token:** `adminToken`

```json
{
  "fullName": "Alice Backup Operator",
  "email": "alice2@wasac.rw",
  "phoneNumber": "0788222333",
  "password": "Operator@9",
  "role": "OPERATOR"
}
```

**What you should see:** `201 Created` with `"role": "OPERATOR"`.

Patrick's admin work is done for now.

---

## Phase 2 — Alice (Operator) Records Meter Readings

Alice visits Marie's home at the end of June and reads both meters.

---

### Step 2.1 — Operator logs in

**POST** `{{baseUrl}}/api/auth/login`

```json
{
  "email": "operator@wasac.rw",
  "password": "Operator@123"
}
```

**Save:** `access_token` → `operatorToken`

---

### Step 2.2 — Alice records water reading (June 2024)

Marie's water meter: previous **120**, current **175** (used **55 units**).

**POST** `{{baseUrl}}/api/meter-readings`  
**Token:** `operatorToken`

```json
{
  "meterId": 1,
  "previousReading": 120,
  "currentReading": 175,
  "readingDate": "2024-06-30"
}
```

Use your actual `waterMeterId`.

**What you should see:** `201 Created`

```json
{
  "id": 1,
  "previousReading": 120,
  "currentReading": 175,
  "readingDate": "2024-06-30",
  "billingMonth": 6,
  "billingYear": 2024
}
```

**Save:** `id` → `waterReadingId`

---

### Step 2.3 — Alice records electricity reading (June 2024)

Electricity meter: previous **500**, current **680** (used **180 units**).

**POST** `{{baseUrl}}/api/meter-readings`  
**Token:** `operatorToken`

```json
{
  "meterId": 2,
  "previousReading": 500,
  "currentReading": 680,
  "readingDate": "2024-06-30"
}
```

Use your actual `elecMeterId`.

**Save:** `id` → `elecReadingId`

**Verify:** **GET** `{{baseUrl}}/api/meter-readings/meter/1` — shows June water reading.

---

### Step 2.4 — Alice tries something she should NOT do (security check)

Try creating a tariff as the operator:

**POST** `{{baseUrl}}/api/tariffs`  
**Token:** `operatorToken`

```json
{
  "name": "Hacked Tariff",
  "utilityType": "WATER",
  "tariffType": "FLAT",
  "effectiveFrom": "2024-01-01",
  "vatRate": 0,
  "fixedServiceCharge": 0,
  "flatRate": 1
}
```

**What you should see:** `403 Forbidden` — operators cannot manage tariffs.

---

## Phase 3 — David (Finance) Bills Marie

David generates bills from the readings, approves them, and prepares for payment.

---

### Step 3.1 — Finance logs in

**POST** `{{baseUrl}}/api/auth/login`

```json
{
  "email": "finance@wasac.rw",
  "password": "Finance@123"
}
```

**Save:** `access_token` → `financeToken`

---

### Step 3.2 — David generates Marie's water bill

**POST** `{{baseUrl}}/api/bills/generate`  
**Token:** `financeToken`

```json
{
  "meterReadingId": 1,
  "applyPenalty": false
}
```

Use your actual `waterReadingId`.

**What you should see:** `201 Created` with something like:

```json
{
  "id": 1,
  "billReference": "BILL-A1B2C3D4",
  "consumption": 55,
  "subtotal": 19250,
  "fixedCharge": 500,
  "vatAmount": 3555,
  "totalAmount": 23305,
  "remainingBalance": 23305,
  "status": "PENDING",
  "billingMonth": 6,
  "billingYear": 2024
}
```

> Amounts may vary slightly depending on rounding. The important fields are `billReference`, `totalAmount`, `remainingBalance`, and `status: "PENDING"`.

**Save:** `id` → `waterBillId`, `billReference` → `waterBillRef`, `totalAmount` → `waterBillTotal`

**Behind the scenes:** A notification was created for Marie.

---

### Step 3.3 — David generates electricity bill

**POST** `{{baseUrl}}/api/bills/generate`  
**Token:** `financeToken`

```json
{
  "meterReadingId": 2,
  "applyPenalty": true
}
```

**What you should see:** `201 Created` with `penaltyAmount` > 0 (5% penalty applied).

---

### Step 3.4 — David approves the water bill

Bills must be **approved** before payment is accepted.

**PUT** `{{baseUrl}}/api/bills/1/approve`  
**Token:** `financeToken`

Use your actual `waterBillId`.

**What you should see:** `200 OK` with `"status": "APPROVED"`.

Repeat for the electricity bill if you want to test paying it too.

---

### Step 3.5 — David checks notifications were sent

**GET** `{{baseUrl}}/api/notifications/customer/1`  
**Token:** `financeToken`

**What you should see:** At least one notification like:

```
Dear Marie Uwase

Your June/2024 utility bill of 23305 FRW has been successfully processed
```

---

### Step 3.6 — David tries to pay before approval (business rule check)

If you have a bill still in `PENDING` status, try paying it:

**POST** `{{baseUrl}}/api/payments`  
**Token:** `financeToken`

```json
{
  "billReference": "BILL-XXXXXXXX",
  "amountPaid": 1000,
  "paymentMethod": "CASH",
  "paymentDate": "2026-06-07"
}
```

> **Important:** `paymentDate` must be on or after the bill generation date (today if you just generated the bill). Use today's date when testing.

Use a **PENDING** (not yet approved) bill reference.

**What you should see:** `400 Bad Request` — *"Bill must be approved before payment"*

---

## Phase 4 — Marie (Customer) Signs Up and Views Her Bill

Marie creates her own account and checks what she owes.

---

### Step 4.1 — Marie signs up (no token needed)

**POST** `{{baseUrl}}/api/auth/register`

```json
{
  "fullName": "Marie Uwase",
  "email": "marie.uwase@gmail.com",
  "phoneNumber": "0788123456",
  "nationalId": "1199880076543210",
  "address": "KG 15 Ave, Kigali",
  "password": "Marie@2024",
  "confirmPassword": "Marie@2024"
}
```

**What you should see:** `200 OK` with `access_token` — she is logged in immediately as `CUSTOMER`.

**Save:** `access_token` → `customerToken`

---

### Step 4.2 — Marie views her profile

**GET** `{{baseUrl}}/api/users/me`  
**Token:** `customerToken`

**What you should see:** Her name, email, `"role": "CUSTOMER"`.

---

### Step 4.3 — Marie views her bills

**GET** `{{baseUrl}}/api/bills/customer/1`  
**Token:** `customerToken`

**What you should see:** List of bills with amounts and statuses.

---

### Step 4.4 — Marie views her notifications

**GET** `{{baseUrl}}/api/notifications/customer/1`  
**Token:** `customerToken`

**What you should see:** Bill notification messages for her account.

---

### Step 4.5 — Marie tries to record a payment (security check)

**POST** `{{baseUrl}}/api/payments`  
**Token:** `customerToken`

```json
{
  "billReference": "BILL-A1B2C3D4",
  "amountPaid": 1000,
  "paymentMethod": "MOBILE_MONEY",
  "paymentDate": "2026-06-07"
}
```

> **Important:** `paymentDate` must be on or after the bill generation date (today if you just generated the bill). Use today's date when testing.

**What you should see:** `403 Forbidden` — only finance staff can record payments.

---

## Phase 5 — David Records Payment (Full & Partial)

Back as David, record how Marie pays her water bill.

---

### Step 5.1 — Partial payment

Marie pays **10,000 FRW** toward her water bill.

**POST** `{{baseUrl}}/api/payments`  
**Token:** `financeToken`

```json
{
  "billReference": "BILL-A1B2C3D4",
  "amountPaid": 10000,
  "paymentMethod": "MOBILE_MONEY",
  "paymentDate": "2026-06-07"
}
```

Use today's date (or any date on/after bill generation). Replace `2026-06-07` with the current date.

Use your actual `waterBillRef`.

**What you should see:** `201 Created` with `"amountPaid": 10000`.

**Verify bill status:**

**GET** `{{baseUrl}}/api/bills/reference/BILL-A1B2C3D4`  
**Token:** `financeToken`

**What you should see:**
- `"status": "PARTIALLY_PAID"`
- `"remainingBalance"` reduced by 10,000

---

### Step 5.2 — Full payment (remaining balance)

Pay exactly what is left. If total was **23,305** and you paid **10,000**, pay **13,305** now.

**POST** `{{baseUrl}}/api/payments`  
**Token:** `financeToken`

```json
{
  "billReference": "BILL-A1B2C3D4",
  "amountPaid": 13305,
  "paymentMethod": "BANK_TRANSFER",
  "paymentDate": "2026-06-07"
}
```

Adjust `amountPaid` to match your actual `remainingBalance`. Use today's date for `paymentDate`.

**What you should see:** `201 Created`

**Verify bill:**

**GET** `{{baseUrl}}/api/bills/reference/BILL-A1B2C3D4`

**What you should see:**
- `"status": "PAID"`
- `"remainingBalance": 0`

**Verify payments history:**

**GET** `{{baseUrl}}/api/payments/bill/BILL-A1B2C3D4`

**What you should see:** Two payment records (partial + final).

---

### Step 5.3 — Overpayment attempt (business rule check)

Try paying again on the same fully paid bill:

**POST** `{{baseUrl}}/api/payments`  
**Token:** `financeToken`

```json
{
  "billReference": "BILL-A1B2C3D4",
  "amountPaid": 1000,
  "paymentMethod": "CASH",
  "paymentDate": "2026-06-07"
}
```

**What you should see:** `400 Bad Request` — *"Bill is already fully paid"* or *"Payment amount exceeds remaining balance"*

---

## Phase 6 — Next Month (Alice Records July Reading)

Simulate the next billing cycle to confirm duplicate-reading protection works.

---

### Step 6.1 — July water reading (success)

**POST** `{{baseUrl}}/api/meter-readings`  
**Token:** `operatorToken`

```json
{
  "meterId": 1,
  "previousReading": 175,
  "currentReading": 210,
  "readingDate": "2024-07-31"
}
```

**What you should see:** `201 Created` — new month, new reading allowed.

---

### Step 6.2 — Duplicate reading for same month (should fail)

**POST** `{{baseUrl}}/api/meter-readings`  
**Token:** `operatorToken`

```json
{
  "meterId": 1,
  "previousReading": 210,
  "currentReading": 220,
  "readingDate": "2024-07-15"
}
```

**What you should see:** `400 Bad Request` — *"A reading already exists for this meter in 7/2024"*

---

### Step 6.3 — Invalid reading (current ≤ previous)

**POST** `{{baseUrl}}/api/meter-readings`  
**Token:** `operatorToken`

```json
{
  "meterId": 1,
  "previousReading": 300,
  "currentReading": 250,
  "readingDate": "2024-08-31"
}
```

**What you should see:** `400 Bad Request` — *"Current reading must be greater than previous reading"*

---

## Phase 7 — Patrick Manages Inactive Customers (Admin Edge Case)

---

### Step 7.1 — Deactivate Marie's customer record

**PUT** `{{baseUrl}}/api/customers/1`  
**Token:** `adminToken`

```json
{
  "fullName": "Marie Uwase",
  "nationalId": "1199880076543210",
  "address": "KG 15 Ave, Kigali",
  "phoneNumber": "0788123456",
  "email": "marie.uwase@wasac.rw",
  "status": "INACTIVE"
}
```

---

### Step 7.2 — Try to record a reading on inactive customer's meter

**POST** `{{baseUrl}}/api/meter-readings`  
**Token:** `operatorToken`

```json
{
  "meterId": 1,
  "previousReading": 210,
  "currentReading": 230,
  "readingDate": "2024-08-31"
}
```

**What you should see:** `400 Bad Request` — *"Customer is inactive and cannot be used"*

---

### Step 7.3 — Reactivate Marie

**PUT** `{{baseUrl}}/api/customers/1`  
**Token:** `adminToken`

Set `"status": "ACTIVE"` in the body.

---

## Phase 8 — Logout

Each user can log out to invalidate their token.

**POST** `{{baseUrl}}/api/auth/logout`  
**Header:** `Authorization: Bearer <token>`

**What you should see:** `200 OK`

After logout, using that same token on a protected endpoint should fail.

---

## Complete Journey Summary

```
┌─────────────────────────────────────────────────────────────────┐
│  ADMIN (Patrick)                                                │
│  Login → Create tariffs → Register customer → Install meters    │
└────────────────────────────┬────────────────────────────────────┘
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│  OPERATOR (Alice)                                               │
│  Login → Record water reading → Record electricity reading      │
└────────────────────────────┬────────────────────────────────────┘
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│  FINANCE (David)                                                │
│  Login → Generate bills → Approve bills → Record payments       │
│          → Verify notifications                                 │
└────────────────────────────┬────────────────────────────────────┘
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│  CUSTOMER (Marie)                                               │
│  Sign up → View profile → View bills → View notifications       │
│  (Cannot pay or create readings herself)                        │
└─────────────────────────────────────────────────────────────────┘
```
