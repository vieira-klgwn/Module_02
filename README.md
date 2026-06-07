# WASAC & REG Rwanda Utility Billing System

Spring Boot backend for **WASAC (Water)** and **REG (Electricity)** utility billing.  
No frontend — test with **Postman** or **curl**.

**Base URL:** `http://localhost:8080`  
**Swagger UI:** `http://localhost:8080/swagger-ui/index.html`

---

## Table of Contents

1. [Project Purpose](#project-purpose)
2. [Prerequisites & Run](#prerequisites--run)
3. [Default Test Users](#default-test-users)
4. [Roles & Responsibilities](#roles--responsibilities)
5. [Postman Setup](#postman-setup)
6. [End-to-End Test Flow](#end-to-end-test-flow)
7. [Endpoint Reference](#endpoint-reference)
8. [Validation & Business Rule Tests](#validation--business-rule-tests)
9. [Security & Role Tests](#security--role-tests)
10. [Expected HTTP Responses](#expected-http-responses)
11. [Notification Format](#notification-format)

> **Start here for a full real-world walkthrough:** see [TESTING_WALKTHROUGH.md](TESTING_WALKTHROUGH.md) — a step-by-step scenario from admin setup through customer signup, billing, and payment.

> **Database triggers & procedures:** see [database/DATABASE_ROUTINES.md](database/DATABASE_ROUTINES.md) and run [`database/database_routines.sql`](database/database_routines.sql) in psql.

---

## Project Purpose

This system supports:

| Utility | Meter Type | Description |
|---------|------------|-------------|
| WASAC | `WATER` | Water supply billing |
| REG | `ELECTRICITY` | Electricity postpaid billing |

Core capabilities:

- User management with JWT authentication
- Customer and meter registration
- Meter readings (operator-only)
- Versioned tariff management (flat or tier-based)
- Bill generation, approval, and payment tracking
- In-app notifications on bill generation and payment

---

## Prerequisites & Run

| Requirement | Version |
|-------------|---------|
| Java | 21 |
| PostgreSQL | Running locally |
| Maven | Use included wrapper `./mvnw` |

### Database

Update `src/main/resources/application.properties` if needed:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/springboot_practice
spring.datasource.username=postgres
spring.datasource.password=<your-password>
```

### Start the application

```bash
./mvnw spring-boot:run
```

On first startup, default staff users are seeded automatically (see below).

> **Note:** If you migrated from an older schema and see column errors, reset the database once or set `spring.jpa.hibernate.ddl-auto=create` for a single run, then switch back to `update`.

---

## Default Test Users

These users are created on startup if they do not already exist:

| Email | Password | Role | Purpose |
|-------|----------|------|---------|
| `admin@wasac.rw` | `Admin@1234` | `ADMIN` | Tariffs, user management, bill approval |
| `operator@wasac.rw` | `Operator@123` | `OPERATOR` | Meter readings |
| `finance@wasac.rw` | `Finance@123` | `FINANCE` | Payments and billing approval |

### Customer signup (public)

Customers register themselves via `POST /api/auth/register`. They receive `ROLE_CUSTOMER` automatically.

**Example password rules:** minimum 8 characters, at least 1 uppercase, 1 lowercase, 1 number, 1 special character.

Example: `Customer@1`

---

## Roles & Responsibilities

| Role | Can Do |
|------|--------|
| **ADMIN** | Manage users, tariffs, customers, meters; approve bills |
| **OPERATOR** | Create meter readings; view customers/meters |
| **FINANCE** | Generate bills, approve bills, record payments |
| **CUSTOMER** | Sign up, view own bills/payments/notifications |

### Public endpoints (no token required)

- `POST /api/auth/login`
- `POST /api/auth/register`
- `POST /api/auth/register/admin`
- `POST /api/auth/register/operator`
- `POST /api/auth/register/finance`
- `POST /api/auth/refresh-token`
- Swagger UI and API docs

All other endpoints require: `Authorization: Bearer <access_token>`

---

## Postman Setup

1. Create an environment variable: `baseUrl` = `http://localhost:8080`
2. Login as a role and save the token:

**Request:** `POST {{baseUrl}}/api/auth/login`

```json
{
  "email": "admin@wasac.rw",
  "password": "Admin@1234"
}
```

**Response** (copy `access_token`):

```json
{
  "access_token": "eyJhbGciOiJIUzUxMiJ9...",
  "refresh_token": "eyJhbGciOiJIUzUxMiJ9..."
}
```

3. Set collection authorization:
   - Type: **Bearer Token**
   - Token: `{{access_token}}`

4. For role-specific tests, login with the appropriate user and replace the token.

---

## End-to-End Test Flow

Follow this order to verify the full billing lifecycle:

```
Login (ADMIN) → Create Customer → Create Meter → Create Tariff
    → Login (OPERATOR) → Record Meter Reading
    → Login (FINANCE) → Generate Bill → Approve Bill → Record Payment
    → Check Notifications
```

### Step 1 — Login as Admin

`POST /api/auth/login` with `admin@wasac.rw` / `Admin@1234`

---

### Step 2 — Create a customer

`POST /api/customers`  
**Role:** ADMIN

```json
{
  "fullName": "Jean Uwimana",
  "nationalId": "1199880012345678",
  "address": "KG 15 Ave, Kigali",
  "phoneNumber": "0788111222",
  "email": "jean@example.rw",
  "password": "Customer@1"
}
```

**Expected:** `201 Created` — save `id` as `customerId`. Creates a linked CUSTOMER user account.

---

### Step 3 — Create a water meter (WASAC)

`POST /api/meters`  
**Role:** ADMIN or OPERATOR

```json
{
  "meterNumber": "WTR-001",
  "type": "WATER",
  "installationDate": "2024-01-15",
  "customerId": 1
}
```

**Expected:** `201 Created` — save `id` as `meterId`.

For REG electricity, use `"type": "ELECTRICITY"` and a unique `meterNumber` (e.g. `ELC-001`).

---

### Step 4 — Create a tariff (ADMIN only)

`POST /api/tariffs`

**Flat tariff (water):**

```json
{
  "name": "Water Flat 2024",
  "utilityType": "WATER",
  "tariffType": "FLAT",
  "effectiveFrom": "2024-01-01",
  "vatRate": 18,
  "fixedServiceCharge": 500,
  "flatRate": 350
}
```

**Tiered tariff (electricity):**

```json
{
  "name": "Electricity Tiered 2024",
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

**Expected:** `201 Created` with `version: 1`. New tariffs auto-close the previous version for forward-only application.

---

### Step 5 — Login as Operator and record a reading

`POST /api/auth/login` with `operator@wasac.rw` / `Operator@123`

`POST /api/meter-readings`  
**Role:** OPERATOR only

```json
{
  "meterId": 1,
  "previousReading": 100,
  "currentReading": 150,
  "readingDate": "2024-06-15"
}
```

**Expected:** `201 Created` — save `id` as `meterReadingId`.  
Consumption = `currentReading - previousReading` = 50 units.

---

### Step 6 — Login as Finance, generate and approve bill

`POST /api/auth/login` with `finance@wasac.rw` / `Finance@123`

**Generate bill:**

`POST /api/bills/generate`

```json
{
  "meterReadingId": 1,
  "applyPenalty": false
}
```

**Expected:** `201 Created` with `billReference`, `totalAmount`, `remainingBalance`, `status: "PENDING"`.  
A notification is created automatically.

**Approve bill:**

`PUT /api/bills/{id}/approve`

**Expected:** `200 OK` with `status: "APPROVED"`.

---

### Step 7 — Record a payment

`POST /api/payments`  
**Role:** FINANCE only

```json
{
  "billReference": "BILL-XXXXXXXX",
  "amountPaid": 5000,
  "paymentMethod": "MOBILE_MONEY",
  "paymentDate": "2024-06-20"
}
```

**Payment methods:** `CASH`, `MOBILE_MONEY`, `BANK_TRANSFER`, `CARD`

**Expected:**
- Partial payment → `status: "PARTIALLY_PAID"`, `remainingBalance` reduced
- Full payment (amount = remaining balance) → `status: "PAID"`, `remainingBalance: 0`
- Payment notification created for the customer

---

### Step 8 — Verify notifications

`GET /api/notifications` (ADMIN or FINANCE)

`GET /api/notifications/customer/{customerId}`

**Expected:** Messages matching the [notification format](#notification-format) below.

---

## Endpoint Reference

### Authentication (`/api/auth`)

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/api/auth/register` | Public | Customer signup |
| POST | `/api/auth/login` | Public | Login, returns JWT |
| POST | `/api/auth/refresh-token` | Public | Refresh access token |
| POST | `/api/auth/logout` | Bearer | Invalidate token |

---

### Users (`/api/users`)

| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| POST | `/api/users` | ADMIN | Create staff user |
| GET | `/api/users` | ADMIN | List all users |
| GET | `/api/users/{id}` | ADMIN | Get user by ID |
| PUT | `/api/users/{id}` | ADMIN | Update user |
| DELETE | `/api/users/{id}` | ADMIN | Delete user |
| GET | `/api/users/me` | Any authenticated | Current user profile |
| PATCH | `/api/users/change-password` | Any authenticated | Change own password |

**Create user body (ADMIN):**

```json
{
  "fullName": "New Operator",
  "email": "newop@wasac.rw",
  "phoneNumber": "0788999000",
  "password": "Operator@9",
  "role": "OPERATOR"
}
```

**Roles:** `ADMIN`, `OPERATOR`, `FINANCE`, `CUSTOMER`

---

### Customers (`/api/customers`)

| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| POST | `/api/customers` | ADMIN | Create customer |
| GET | `/api/customers` | ADMIN, FINANCE, OPERATOR | List customers |
| GET | `/api/customers/{id}` | ADMIN, FINANCE, OPERATOR, CUSTOMER | Get customer |
| PUT | `/api/customers/{id}` | ADMIN | Update customer |
| DELETE | `/api/customers/{id}` | ADMIN | Delete customer |

---

### Meters (`/api/meters`)

| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| POST | `/api/meters` | ADMIN, OPERATOR | Create meter |
| GET | `/api/meters` | ADMIN, FINANCE, OPERATOR | List meters |
| GET | `/api/meters/{id}` | ADMIN, FINANCE, OPERATOR, CUSTOMER | Get meter |
| GET | `/api/meters/customer/{customerId}` | ADMIN, FINANCE, OPERATOR, CUSTOMER | Meters by customer |
| PUT | `/api/meters/{id}` | ADMIN | Update meter |
| DELETE | `/api/meters/{id}` | ADMIN | Delete meter |

**Meter types:** `WATER`, `ELECTRICITY`

---

### Meter Readings (`/api/meter-readings`)

| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| POST | `/api/meter-readings` | OPERATOR | Record reading |
| GET | `/api/meter-readings` | ADMIN, FINANCE, OPERATOR | List readings |
| GET | `/api/meter-readings/{id}` | ADMIN, FINANCE, OPERATOR | Get reading |
| GET | `/api/meter-readings/meter/{meterId}` | ADMIN, FINANCE, OPERATOR, CUSTOMER | Readings by meter |

---

### Tariffs (`/api/tariffs`)

| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| POST | `/api/tariffs` | ADMIN | Create versioned tariff |
| GET | `/api/tariffs` | ADMIN, FINANCE | List tariffs |
| GET | `/api/tariffs/{id}` | ADMIN, FINANCE | Get tariff |

---

### Bills (`/api/bills`)

| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| POST | `/api/bills/generate` | ADMIN, FINANCE | Generate bill from reading |
| PUT | `/api/bills/{id}/approve` | ADMIN, FINANCE | Approve pending bill |
| GET | `/api/bills` | ADMIN, FINANCE, CUSTOMER | List bills |
| GET | `/api/bills/{id}` | ADMIN, FINANCE, CUSTOMER | Get bill |
| GET | `/api/bills/reference/{reference}` | ADMIN, FINANCE, CUSTOMER | Get by bill reference |
| GET | `/api/bills/customer/{customerId}` | ADMIN, FINANCE, CUSTOMER | Bills by customer |

**Bill statuses:** `PENDING`, `APPROVED`, `PARTIALLY_PAID`, `PAID`, `CANCELLED`

---

### Payments (`/api/payments`)

| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| POST | `/api/payments` | FINANCE | Record payment |
| GET | `/api/payments` | ADMIN, FINANCE, CUSTOMER | List payments |
| GET | `/api/payments/{id}` | ADMIN, FINANCE, CUSTOMER | Get payment |
| GET | `/api/payments/bill/{billReference}` | ADMIN, FINANCE, CUSTOMER | Payments by bill |

---

### Notifications (`/api/notifications`)

| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| GET | `/api/notifications` | ADMIN, FINANCE | List all notifications |
| GET | `/api/notifications/customer/{customerId}` | ADMIN, FINANCE, CUSTOMER | Customer notifications |

---

## Validation & Business Rule Tests

Use these cases to confirm rules are enforced. All should return **400 Bad Request** with a clear message unless noted.

### Input validation

| Test | Request | Expected |
|------|---------|----------|
| Empty body | POST any endpoint with `{}` | `400` — field validation errors |
| Invalid email | `"email": "not-an-email"` | `400` — email format error |
| Invalid phone | `"phoneNumber": "12345"` | `400` — Rwanda phone format error |
| Invalid national ID | `"nationalId": "2234567890123456"` | `400` — must be 16 digits starting with 1 |
| Invalid national ID | `"nationalId": "1234-5678-9012-3456"` | `400` — letters/special chars rejected |
| Weak password | `"password": "short"` | `400` — password rule error |
| Duplicate email | Register same email twice | `400` — email already taken |
| Duplicate national ID | Same ID in users or customers table | `400` — National ID already exists |
| Missing national ID (any user create/register) | Omit `nationalId` | `400` |
| Missing address (customer create/register) | Omit `address` | `400` |
| Duplicate meter number | Two meters with same `meterNumber` | `400` |
| Duplicate tariff name | Two tariffs with same `name` | `400` |

**Valid National ID example:** `1200767890123456`

**Valid Rwanda phone formats:**
- `+250788123456`
- `0788123456`

**Password rules:** minimum 8 characters, at least 1 uppercase, 1 lowercase, 1 number, 1 special character.

### Meter rules

| Test | Expected |
|------|----------|
| Future installation date | `400` — cannot be in the future |
| Create meter for inactive customer | `400` |
| Duplicate meter number | `400` |

### Meter reading rules

| Test | Expected |
|------|----------|
| `currentReading` ≤ `previousReading` | `400` |
| Negative readings | `400` |
| Second reading same meter/month/year | `400` |
| Reading on inactive meter | `400` |
| Reading on meter with inactive customer | `400` |
| Reading date before installation date | `400` |
| Month/year manually sent in body | Ignored — derived from `readingDate` |

### Billing & payment rules

| Test | Expected |
|------|----------|
| Generate bill for inactive customer | `400` |
| Generate bill twice for same reading | `400` |
| Pay bill before approval | `400` |
| Payment amount > remaining balance | `400` |
| Payment date before bill generation | `400` |
| Pay already fully paid bill | `400` |
| Zero or negative payment amount | `400` |
| Negative tariff values | `400` |
| Past effectiveFrom for new tariff version | `400` |

### Inactive entity rules

| Test | Expected |
|------|----------|
| Create meter for inactive customer | `400` |
| Record reading on inactive meter/customer | `400` |
| Generate bill for inactive customer | `400` |
| Record payment for inactive customer | `400` |
| Login as inactive user | `400` |

### Customer data isolation (security)

| Test | Expected |
|------|----------|
| Customer lists bills (`GET /api/bills`) | Only own bills returned |
| Customer views another customer's bills | `403 Forbidden` |
| Customer views another customer's notifications | `403 Forbidden` |
| Customer views another customer's payments | `403 Forbidden` |

---

## Security & Role Tests

| Test | How | Expected |
|------|-----|----------|
| No token | Call `GET /api/customers` without header | `401` or `403` |
| Wrong role — customer creates reading | Login as CUSTOMER, POST `/api/meter-readings` | `403` |
| Wrong role — operator creates tariff | Login as OPERATOR, POST `/api/tariffs` | `403` |
| Wrong role — finance creates user | Login as FINANCE, POST `/api/users` | `403` |
| Wrong role — customer records payment | Login as CUSTOMER, POST `/api/payments` | `403` |
| Valid role | Login as OPERATOR, POST `/api/meter-readings` | `201` |
| Public signup | POST `/api/auth/register` without token | `200` with tokens |
| Public login | POST `/api/auth/login` without token | `200` with tokens |

---

## Expected HTTP Responses

| Situation | Status | Body |
|-----------|--------|------|
| Validation failure | `400` | `{ "error": "Validation Failed", "fieldName": "message", ... }` |
| Business rule violation | `400` | `{ "error": "Bad Request", "message": "..." }` |
| Not authenticated | `401` | Authentication error |
| Wrong role | `403` | Access denied |
| Created resource | `201` | Resource JSON |
| Success | `200` | Resource JSON |
| Deleted | `204` | Empty body |

---

## Notification Format

When a bill is generated:

```
Dear <Customer Name>

Your <Month/Year> utility bill of <Amount> FRW has been successfully processed
```

Example:

```
Dear Jean Uwimana

Your June/2024 utility bill of 21250 FRW has been successfully processed
```

When a payment is recorded, a notification email is also sent. **Full payment** uses the same processed message format above. Partial payments include amount paid and remaining balance.

### Email delivery

All notification emails are sent to the test inbox:

**`klgwnboy@gmail.com`**

Configure in `application.properties` via `app.notification.test-recipient`.

Verify in-app notifications via:

```
GET /api/notifications/customer/{customerId}
```

---

## Quick curl Smoke Test

```bash
# Login
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@wasac.rw","password":"Admin@1234"}' \
  | python3 -c "import sys,json; print(json.load(sys.stdin)['access_token'])")

# Create customer
curl -s -X POST http://localhost:8080/api/customers \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"fullName":"Test User","nationalId":"1199880099999999","address":"Kigali","phoneNumber":"0788009999","email":"test@example.rw","password":"Customer@1"}'
```

A full automated test script is available at `full-test.sh` in the project root (40 checks).

---

## Role Achievement Checklist

Use this checklist to confirm the project meets exam requirements:

- [ ] JWT login and signup work without a token
- [ ] All other endpoints reject unauthenticated requests
- [ ] ADMIN can manage users and tariffs
- [ ] OPERATOR can only create meter readings
- [ ] FINANCE can generate bills, approve bills, and record payments
- [ ] CUSTOMER can sign up and view bills/payments
- [ ] Validation returns `400` with clear messages (national ID, phone, email, password, dates)
- [ ] National ID uniqueness enforced across users and customers
- [ ] Business rules block invalid data before it reaches the database
- [ ] Bills use versioned tariffs (old tariffs remain for past cycles)
- [ ] Partial payments update balance; full payment marks bill `PAID`
- [ ] Notifications are created on bill generation and payment
- [ ] Emails are sent to `klgwnboy@gmail.com` for each notification
- [ ] PostgreSQL triggers/procedures installed (see `database/database_routines.sql`)
- [ ] WASAC (`WATER`) and REG (`ELECTRICITY`) meter types are supported
