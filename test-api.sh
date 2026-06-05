#!/bin/bash
BASE=http://localhost:8080
PASS=0
FAIL=0

check() {
  local name="$1"
  local expected="$2"
  local actual="$3"
  if echo "$actual" | grep -q "$expected"; then
    echo "✓ $name"
    PASS=$((PASS+1))
  else
    echo "✗ $name (expected: $expected)"
    echo "  Response: $actual"
    FAIL=$((FAIL+1))
  fi
}

echo "=== AUTH TESTS ==="

# Login admin
ADMIN_TOKEN=$(curl -s -X POST "$BASE/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@wasac.rw","password":"Admin@1234"}' | python3 -c "import sys,json; print(json.load(sys.stdin).get('access_token',''))")
check "Admin login" "eyJ" "$ADMIN_TOKEN"

# Invalid login
INVALID=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$BASE/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@wasac.rw","password":"wrong"}')
check "Invalid login returns 401" "401" "$INVALID"

# Signup
SIGNUP_EMAIL="customer$(date +%s)@test.rw"
SIGNUP=$(curl -s -X POST "$BASE/api/auth/register" \
  -H "Content-Type: application/json" \
  -d "{\"fullName\":\"Test Customer\",\"email\":\"$SIGNUP_EMAIL\",\"phoneNumber\":\"0788123456\",\"password\":\"Customer@1\",\"confirmPassword\":\"Customer@1\"}")
check "Signup" "access_token" "$SIGNUP"

# Invalid phone signup
BAD_PHONE=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$BASE/api/auth/register" \
  -H "Content-Type: application/json" \
  -d '{"fullName":"Bad","email":"bad@test.rw","phoneNumber":"12345","password":"Customer@1","confirmPassword":"Customer@1"}')
check "Invalid phone returns 400" "400" "$BAD_PHONE"

# Unauthorized access
UNAUTH=$(curl -s -o /dev/null -w "%{http_code}" "$BASE/api/customers")
check "Unauthorized returns 401/403" "401\|403" "$UNAUTH"

echo ""
echo "=== CUSTOMER TESTS ==="
NID="11998800$(date +%s | tail -c 8)"
CUSTOMER=$(curl -s -X POST "$BASE/api/customers" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"fullName\":\"Jean Uwimana\",\"nationalId\":\"$NID\",\"phoneNumber\":\"0788111222\"}")
CUSTOMER_ID=$(echo "$CUSTOMER" | grep -o '"id":[0-9]*' | head -1 | cut -d: -f2)
check "Create customer" '"fullName":"Jean Uwimana"' "$CUSTOMER"

# Duplicate national ID
DUP_NID=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$BASE/api/customers" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"fullName\":\"Duplicate\",\"nationalId\":\"$NID\",\"phoneNumber\":\"0788333444\"}")
check "Duplicate national ID returns 400" "400" "$DUP_NID"

echo ""
echo "=== METER TESTS ==="
METER=$(curl -s -X POST "$BASE/api/meters" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"meterNumber\":\"WTR-001\",\"type\":\"WATER\",\"installationDate\":\"2024-01-15\",\"customerId\":$CUSTOMER_ID}")
METER_ID=$(echo "$METER" | grep -o '"id":[0-9]*' | head -1 | cut -d: -f2)
check "Create meter" '"meterNumber":"WTR-001"' "$METER"

ELEC_METER=$(curl -s -X POST "$BASE/api/meters" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"meterNumber\":\"ELC-001\",\"type\":\"ELECTRICITY\",\"installationDate\":\"2024-02-01\",\"customerId\":$CUSTOMER_ID}")
ELEC_METER_ID=$(echo "$ELEC_METER" | grep -o '"id":[0-9]*' | head -1 | cut -d: -f2)

echo ""
echo "=== TARIFF TESTS ==="
TARIFF=$(curl -s -X POST "$BASE/api/tariffs" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"Water Flat 2024","utilityType":"WATER","tariffType":"FLAT","effectiveFrom":"2024-01-01","vatRate":18,"fixedServiceCharge":500,"flatRate":350}')
check "Create flat tariff" '"tariffType":"FLAT"' "$TARIFF"

TARIFF_ELEC=$(curl -s -X POST "$BASE/api/tariffs" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"Electricity Tiered","utilityType":"ELECTRICITY","tariffType":"TIERED","effectiveFrom":"2024-01-01","vatRate":18,"fixedServiceCharge":1000,"tiers":[{"minConsumption":0,"maxConsumption":100,"ratePerUnit":120},{"minConsumption":100,"ratePerUnit":180}]}')

echo ""
echo "=== METER READING TESTS ==="
OP_TOKEN=$(curl -s -X POST "$BASE/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email":"operator@wasac.rw","password":"Operator@123"}' | python3 -c "import sys,json; print(json.load(sys.stdin)['access_token'])")

READING=$(curl -s -X POST "$BASE/api/meter-readings" \
  -H "Authorization: Bearer $OP_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"meterId\":$METER_ID,\"previousReading\":100,\"currentReading\":150,\"readingDate\":\"2024-06-15\"}")
READING_ID=$(echo "$READING" | grep -o '"id":[0-9]*' | head -1 | cut -d: -f2)
check "Create meter reading" '"currentReading":150' "$READING"

# Invalid reading (current <= previous)
BAD_READING=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$BASE/api/meter-readings" \
  -H "Authorization: Bearer $OP_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"meterId\":$METER_ID,\"previousReading\":200,\"currentReading\":150,\"readingDate\":\"2024-07-15\"}")
check "Invalid reading returns 400" "400" "$BAD_READING"

# Role restriction - customer cannot create reading
CUST_TOKEN=$(echo "$SIGNUP" | python3 -c "import sys,json; print(json.load(sys.stdin)['access_token'])")
ROLE_DENY=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$BASE/api/meter-readings" \
  -H "Authorization: Bearer $CUST_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"meterId\":$METER_ID,\"previousReading\":150,\"currentReading\":200,\"readingDate\":\"2024-07-15\"}")
check "Customer denied meter reading" "403" "$ROLE_DENY"

echo ""
echo "=== BILLING TESTS ==="
FIN_TOKEN=$(curl -s -X POST "$BASE/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email":"finance@wasac.rw","password":"Finance@123"}' | python3 -c "import sys,json; print(json.load(sys.stdin)['access_token'])")

BILL=$(curl -s -X POST "$BASE/api/bills/generate" \
  -H "Authorization: Bearer $FIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"meterReadingId\":$READING_ID}")
BILL_REF=$(echo "$BILL" | grep -o '"billReference":"[^"]*"' | cut -d'"' -f4)
BILL_ID=$(echo "$BILL" | grep -o '"id":[0-9]*' | head -1 | cut -d: -f2)
check "Generate bill" "billReference" "$BILL"

APPROVED=$(curl -s -X PUT "$BASE/api/bills/$BILL_ID/approve" \
  -H "Authorization: Bearer $FIN_TOKEN")
check "Approve bill" '"status":"APPROVED"' "$APPROVED"

echo ""
echo "=== PAYMENT TESTS ==="
PAYMENT=$(curl -s -X POST "$BASE/api/payments" \
  -H "Authorization: Bearer $FIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"billReference\":\"$BILL_REF\",\"amountPaid\":5000,\"paymentMethod\":\"MOBILE_MONEY\",\"paymentDate\":\"2024-06-20\"}")
check "Partial payment" '"amountPaid":5000' "$PAYMENT"

# Overpayment
OVERPAY=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$BASE/api/payments" \
  -H "Authorization: Bearer $FIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"billReference\":\"$BILL_REF\",\"amountPaid\":9999999,\"paymentMethod\":\"CASH\",\"paymentDate\":\"2024-06-21\"}")
check "Overpayment returns 400" "400" "$OVERPAY"

echo ""
echo "=== NOTIFICATION TESTS ==="
NOTIFS=$(curl -s "$BASE/api/notifications" -H "Authorization: Bearer $FIN_TOKEN")
check "Notifications created" "utility bill" "$NOTIFS"

echo ""
echo "=== USER MANAGEMENT TESTS ==="
NEW_USER=$(curl -s -X POST "$BASE/api/users" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"fullName":"New Operator","email":"newop@wasac.rw","phoneNumber":"0788999000","password":"Operator@9","role":"OPERATOR"}')
check "Admin creates user" '"role":"OPERATOR"' "$NEW_USER"

echo ""
echo "=============================="
echo "PASSED: $PASS | FAILED: $FAIL"
echo "=============================="
exit $FAIL
