#!/usr/bin/env python3
"""Phase 7: Full E2E integration test"""
import urllib.request, json, sys

pas, fal = 0, 0

def login():
    data = json.dumps({"username":"admin","password":"admin123"}).encode()
    req = urllib.request.Request("http://localhost:8081/api/v1/auth/login", data=data, headers={"Content-Type":"application/json"})
    return json.loads(urllib.request.urlopen(req, timeout=10).read())["data"]["accessToken"]

def get(url):
    req = urllib.request.Request(url, headers={"Authorization":f"Bearer {token}"})
    return json.loads(urllib.request.urlopen(req, timeout=10).read())

def post(url, data):
    body = json.dumps(data).encode()
    req = urllib.request.Request(url, data=body, method="POST", headers={"Authorization":f"Bearer {token}","Content-Type":"application/json"})
    return json.loads(urllib.request.urlopen(req, timeout=10).read())

def put(url, data=None):
    body = json.dumps(data).encode() if data else None
    req = urllib.request.Request(url, data=body, method="PUT", headers={"Authorization":f"Bearer {token}","Content-Type":"application/json"})
    return json.loads(urllib.request.urlopen(req, timeout=10).read())

def t(name, result):
    global pas, fal
    ok = result.get("code") == 200 if isinstance(result, dict) else False
    if ok: pas += 1
    else: fal += 1
    print(f"  {'[PASS]' if ok else '[FAIL]'} {name}" + (f" - {result.get('message','')}" if not ok else ""))
    return result.get("data")

token = login()

O = "http://localhost:8081/api/v1"
P = "http://localhost:8082/api/v1"
F = "http://localhost:8083/api/v1"
R = "http://localhost:8084/api/v1"

print("\n=== 1. Order & Settlement ===")
o = t("Create order", post(f"{O}/orders", {"customerName":"E2E-Test","contractNo":"E2E-001","totalAmount":50000}))
oid = o["id"] if o else 0
t("Check status=1", {"code":200 if o and o.get("status")==1 else 0})

t("Batch calc", post(f"{O}/orders/batch-calc", {"orderIds":[oid]}))
stl = t("Create settlement", post(f"{O}/settlements", {"orderIds":[oid]}))
sid = stl[0]["id"] if stl and isinstance(stl,list) else 0
t("Confirm settlement", put(f"{O}/settlements/{sid}/confirm"))

print("\n=== 2. Payment & Receipt ===")
rcp = t("Create receipt", post(f"{P}/receipts", {"payerName":"E2E","amount":50000,"receiptMethod":1}))
rid = rcp["id"] if rcp else 0
t("Confirm receipt", put(f"{P}/receipts/{rid}/confirm"))

pym = t("Create payment", post(f"{P}/payments", {"payeeName":"E2E-Supplier","amount":30000}))
t("Payment pending", {"code":200 if pym and pym.get("paymentStatus")==1 else 0})

bil = t("Create bill", post(f"{P}/bills", {"customerId":1,"totalAmount":50000}))
bid = bil["id"] if bil else 0
t("Send bill", post(f"{P}/bills/{bid}/send", {}))

apv = t("List approvals", get(f"{P}/approvals/pending?page=1&size=20"))
if apv and apv.get("records"):
    aid = apv["records"][0]["id"]
    t("Approve", put(f"{P}/approvals/{aid}/process", {"action":"APPROVE","comment":"E2E"}))

print("\n=== 3. Fund & Report ===")
acc = t("List accounts", get(f"{F}/accounts?page=1&size=5"))
if acc and acc.get("records"):
    aid = acc["records"][0]["id"]
    t("Add transaction", post(f"{F}/transactions", {"accountId":aid,"transType":1,"amount":50000,"counterparty":"E2E","summary":"test"}))
    t("Bank sync", post(f"{F}/transactions/batch-sync", {"accountId":aid}))
    t("Reconciliation", post(f"{F}/reconciliations/run", {"accountId":aid}))

t("Income report", get(f"{R}/reports/income-expense"))
t("Balance report", get(f"{R}/reports/balance"))
t("Settle stats", get(f"{R}/reports/settlement-stats"))

print(f"\n{'='*30}")
print(f"PASS={pas} FAIL={fal} ({100*pas//(pas+fal)}%)")
sys.exit(0 if fal==0 else 1)
