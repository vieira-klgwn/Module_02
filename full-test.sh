#!/bin/bash
set -e
BASE=http://localhost:8080
PASS=0
FAIL=0
TS=$(date +%s)
TODAY=$(date +%Y-%m-%d)

log() { echo ""; echo "=== $1 ==="; }
ok() { echo "✓ $1"; PASS=$((PASS+1)); }
bad() { echo "✗ $1"; echo "  Response: $2"; FAIL=$((FAIL+1)); }
token() { curl -s -X POST "$BASE/api/auth/login" -H "Content-Type: application/json" -d "$1" | python3 -c "import sys,json; print(json.load(sys.stdin)['access_token'])"; }
code() { curl -s -o /tmp/resp.json -w "%{http_code}" "$@"; }

ADMIN_TOKEN=$(token '{"email":"admin@wasac.rw","password":"Admin@1234"}')
OP_TOKEN=$(token '{"email":"operator@wasac.rw","password":"Operator@123"}')
FIN_TOKEN=$(token '{"email":"finance@wasac.rw","password":"Finance@123"}')

log "AUTH"
C=$(code -X POST "$BASE/api/auth/login" -H "Content-Type: application/json" -d '{"email":"admin@wasac.rw","password":"Admin@1234"}')
[[ "$C" == "200" ]] && ok "Admin login" || bad "Admin login" "$C"
C=$(code -X POST "$BASE/api/auth/login" -H "Content-Type: application/json" -d '{"email":"admin@wasac.rw","password":"wrong"}')
[[ "$C" == "401" || "$C" == "403" ]] && ok "Bad login rejected" || bad "Bad login" "$C"
C=$(code "$BASE/api/customers")
[[ "$C" == "401" || "$C" == "403" ]] && ok "Unauthenticated blocked" || bad "Unauth" "$C"

log "CUSTOMER + USER CREATION"
NID=$(printf '1%015d' $((TS % 1000000000000000)))
PHONE="07$(printf '%08d' $((TS % 100000000)))"
EMAIL="cust$TS@test.rw"
CUST_BODY="{\"fullName\":\"Test Cust $TS\",\"nationalId\":\"$NID\",\"address\":\"Kigali\",\"phoneNumber\":\"$PHONE\",\"email\":\"$EMAIL\",\"password\":\"Customer@1\"}"
C=$(code -X POST "$BASE/api/customers" -H "Authorization: Bearer $ADMIN_TOKEN" -H "Content-Type: application/json" -d "$CUST_BODY")
RESP=$(cat /tmp/resp.json)
[[ "$C" == "201" ]] && ok "Create customer" || bad "Create customer" "$RESP"
CUSTOMER_ID=$(echo "$RESP" | python3 -c "import sys,json; print(json.load(sys.stdin).get('id',''))" 2>/dev/null)

# DB verify user linked
DB_USER=$(PGPASSWORD=klgwn psql -h localhost -U postgres -d springboot_practice -t -A -c "SELECT u.email,u.role,c.id FROM users u JOIN customers c ON u.customer_id=c.id WHERE c.national_id='$NID';" 2>/dev/null || echo "DB_SKIP")
[[ "$DB_USER" == *"$EMAIL"* && "$DB_USER" == *"CUSTOMER"* ]] && ok "DB: user linked to customer" || bad "DB user link" "$DB_USER"

# Customer can login
CUST_TOKEN=$(token "{\"email\":\"$EMAIL\",\"password\":\"Customer@1\"}" 2>/dev/null || echo "")
[[ -n "$CUST_TOKEN" ]] && ok "Customer user can login" || bad "Customer login" "failed"

# Validation: bad national ID format
C=$(code -X POST "$BASE/api/customers" -H "Authorization: Bearer $ADMIN_TOKEN" -H "Content-Type: application/json" -d '{"fullName":"X","nationalId":"2234567890123456","address":"K","phoneNumber":"0788123456","email":"badnid@test.rw","password":"Customer@1"}')
[[ "$C" == "400" ]] && ok "Invalid national ID rejected" || bad "Invalid NID" "$C"
# Validation: bad phone
C=$(code -X POST "$BASE/api/customers" -H "Authorization: Bearer $ADMIN_TOKEN" -H "Content-Type: application/json" -d '{"fullName":"X","nationalId":"999","address":"K","phoneNumber":"123","email":"x@t.rw","password":"Customer@1"}')
[[ "$C" == "400" ]] && ok "Invalid phone rejected" || bad "Invalid phone" "$C"
# Duplicate national ID
C=$(code -X POST "$BASE/api/customers" -H "Authorization: Bearer $ADMIN_TOKEN" -H "Content-Type: application/json" -d "$CUST_BODY")
[[ "$C" == "400" ]] && ok "Duplicate national ID rejected" || bad "Dup NID" "$(cat /tmp/resp.json)"

log "METER"
METER_NUM="WTR-$TS"
C=$(code -X POST "$BASE/api/meters" -H "Authorization: Bearer $ADMIN_TOKEN" -H "Content-Type: application/json" -d "{\"meterNumber\":\"$METER_NUM\",\"type\":\"WATER\",\"installationDate\":\"2024-01-01\",\"customerId\":$CUSTOMER_ID}")
[[ "$C" == "201" ]] && ok "Create meter" || bad "Create meter" "$(cat /tmp/resp.json)"
METER_ID=$(cat /tmp/resp.json | python3 -c "import sys,json; print(json.load(sys.stdin).get('id',''))")

log "TARIFF FUTURE-ONLY"
TOMORROW=$(python3 -c "from datetime import date,timedelta; print((date.today()+timedelta(days=1)).isoformat())")
C=$(code -X POST "$BASE/api/tariffs" -H "Authorization: Bearer $ADMIN_TOKEN" -H "Content-Type: application/json" -d "{\"name\":\"Future Water $TS\",\"utilityType\":\"WATER\",\"tariffType\":\"FLAT\",\"effectiveFrom\":\"2020-01-01\",\"vatRate\":18,\"fixedServiceCharge\":500,\"flatRate\":999}")
[[ "$C" == "400" ]] && ok "Past tariff date rejected" || bad "Past tariff" "$(cat /tmp/resp.json)"
C=$(code -X POST "$BASE/api/tariffs" -H "Authorization: Bearer $ADMIN_TOKEN" -H "Content-Type: application/json" -d "{\"name\":\"Future Water $TS\",\"utilityType\":\"WATER\",\"tariffType\":\"FLAT\",\"effectiveFrom\":\"$TOMORROW\",\"vatRate\":18,\"fixedServiceCharge\":500,\"flatRate\":999}")
[[ "$C" == "201" ]] && ok "Future tariff created" || bad "Future tariff" "$(cat /tmp/resp.json)"
NEW_TARIFF_ID=$(cat /tmp/resp.json | python3 -c "import sys,json; print(json.load(sys.stdin).get('id',''))")

log "METER READINGS"
C=$(code -X POST "$BASE/api/meter-readings" -H "Authorization: Bearer $OP_TOKEN" -H "Content-Type: application/json" -d "{\"meterId\":$METER_ID,\"previousReading\":100,\"currentReading\":150,\"readingDate\":\"2024-09-15\"}")
[[ "$C" == "201" ]] && ok "Valid reading created" || bad "Reading" "$(cat /tmp/resp.json)"
READING_ID=$(cat /tmp/resp.json | python3 -c "import sys,json; print(json.load(sys.stdin).get('id',''))")
C=$(code -X POST "$BASE/api/meter-readings" -H "Authorization: Bearer $OP_TOKEN" -H "Content-Type: application/json" -d "{\"meterId\":$METER_ID,\"previousReading\":200,\"currentReading\":150,\"readingDate\":\"2024-10-15\"}")
[[ "$C" == "400" ]] && ok "current<=previous rejected" || bad "Bad reading order" "$(cat /tmp/resp.json)"
C=$(code -X POST "$BASE/api/meter-readings" -H "Authorization: Bearer $OP_TOKEN" -H "Content-Type: application/json" -d "{\"meterId\":$METER_ID,\"previousReading\":150,\"currentReading\":180,\"readingDate\":\"2024-09-20\"}")
[[ "$C" == "400" ]] && ok "Duplicate month/year rejected" || bad "Dup reading" "$(cat /tmp/resp.json)"
C=$(code -X POST "$BASE/api/meter-readings" -H "Authorization: Bearer $CUST_TOKEN" -H "Content-Type: application/json" -d "{\"meterId\":$METER_ID,\"previousReading\":150,\"currentReading\":200,\"readingDate\":\"2024-10-15\"}")
[[ "$C" == "403" ]] && ok "Customer cannot create reading" || bad "Role check reading" "$C"

log "BILLING"
C=$(code -X POST "$BASE/api/bills/generate" -H "Authorization: Bearer $FIN_TOKEN" -H "Content-Type: application/json" -d "{\"meterReadingId\":$READING_ID}")
[[ "$C" == "201" ]] && ok "Bill generated" || bad "Bill gen" "$(cat /tmp/resp.json)"
BILL_ID=$(cat /tmp/resp.json | python3 -c "import sys,json; print(json.load(sys.stdin).get('id',''))")
BILL_REF=$(cat /tmp/resp.json | python3 -c "import sys,json; print(json.load(sys.stdin).get('billReference',''))")
TARIFF_ON_BILL=$(cat /tmp/resp.json | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('tariff',{}).get('id','') if isinstance(d.get('tariff'),dict) else '')" 2>/dev/null)
# DB: bill should NOT use future tariff for Sep 2024 reading
DB_TARIFF=$(PGPASSWORD=klgwn psql -h localhost -U postgres -d springboot_practice -t -A -c "SELECT tariff_id FROM bills WHERE id=$BILL_ID;" 2>/dev/null || echo "")
[[ "$DB_TARIFF" != "$NEW_TARIFF_ID" ]] && ok "Bill uses old tariff not future one (DB tariff_id=$DB_TARIFF)" || bad "Tariff versioning" "used future tariff $NEW_TARIFF_ID"

C=$(code -X PUT "$BASE/api/bills/$BILL_ID/approve" -H "Authorization: Bearer $FIN_TOKEN")
[[ "$C" == "200" ]] && ok "Bill approved" || bad "Approve" "$(cat /tmp/resp.json)"
REMAINING=$(cat /tmp/resp.json | python3 -c "import sys,json; print(json.load(sys.stdin).get('remainingBalance',0))")

log "PAYMENTS"
C=$(code -X POST "$BASE/api/payments" -H "Authorization: Bearer $FIN_TOKEN" -H "Content-Type: application/json" -d "{\"billReference\":\"$BILL_REF\",\"amountPaid\":99999999,\"paymentMethod\":\"CASH\",\"paymentDate\":\"$TODAY\"}")
[[ "$C" == "400" ]] && ok "Overpayment rejected" || bad "Overpay" "$(cat /tmp/resp.json)"
C=$(code -X POST "$BASE/api/payments" -H "Authorization: Bearer $FIN_TOKEN" -H "Content-Type: application/json" -d "{\"billReference\":\"$BILL_REF\",\"amountPaid\":$REMAINING,\"paymentMethod\":\"MOBILE_MONEY\",\"paymentDate\":\"$TODAY\"}")
[[ "$C" == "201" ]] && ok "Full payment recorded" || bad "Payment" "$(cat /tmp/resp.json)"
DB_BILL_STATUS=$(PGPASSWORD=klgwn psql -h localhost -U postgres -d springboot_practice -t -A -c "SELECT status,remaining_balance FROM bills WHERE id=$BILL_ID;" 2>/dev/null || echo "")
[[ "$DB_BILL_STATUS" == *"PAID"* && "$DB_BILL_STATUS" == *"0.00"* ]] && ok "DB: bill PAID balance 0" || bad "DB bill status" "$DB_BILL_STATUS"

log "NOTIFICATIONS"
C=$(code "$BASE/api/notifications/customer/$CUSTOMER_ID" -H "Authorization: Bearer $FIN_TOKEN")
[[ "$C" == "200" ]] && ok "Get notifications" || bad "Notifications" "$C"

log "USERS CRUD"
C=$(code -X POST "$BASE/api/users" -H "Authorization: Bearer $ADMIN_TOKEN" -H "Content-Type: application/json" -d "{\"fullName\":\"Op2\",\"email\":\"op2$TS@wasac.rw\",\"phoneNumber\":\"0788999900\",\"password\":\"Operator@9\",\"role\":\"OPERATOR\"}")
[[ "$C" == "201" ]] && ok "Admin creates user" || bad "Create user" "$(cat /tmp/resp.json)"
USER_ID=$(cat /tmp/resp.json | python3 -c "import sys,json; print(json.load(sys.stdin).get('id',''))")
C=$(code "$BASE/api/users/$USER_ID" -H "Authorization: Bearer $ADMIN_TOKEN")
[[ "$C" == "200" ]] && ok "Get user by id" || bad "Get user" "$C"
C=$(code -X PUT "$BASE/api/users/$USER_ID" -H "Authorization: Bearer $ADMIN_TOKEN" -H "Content-Type: application/json" -d "{\"fullName\":\"Op2 Updated\",\"email\":\"op2$TS@wasac.rw\",\"phoneNumber\":\"0788999901\",\"role\":\"OPERATOR\"}")
[[ "$C" == "200" ]] && ok "Update user" || bad "Update user" "$(cat /tmp/resp.json)"

log "GET ENDPOINTS"
for EP in "/api/customers" "/api/meters" "/api/meter-readings" "/api/tariffs" "/api/bills" "/api/payments" "/api/notifications" "/api/users"; do
  C=$(code "$BASE$EP" -H "Authorization: Bearer $ADMIN_TOKEN")
  [[ "$C" == "200" ]] && ok "GET $EP" || bad "GET $EP" "$C"
done
C=$(code "$BASE/api/users/me" -H "Authorization: Bearer $ADMIN_TOKEN")
[[ "$C" == "200" ]] && ok "GET /api/users/me" || bad "GET me" "$C"

echo ""
echo "=============================="
echo "PASSED: $PASS | FAILED: $FAIL"
echo "=============================="
exit $FAIL
