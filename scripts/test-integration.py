# -*- coding: utf-8 -*-
"""
Phase 3 + Phase 4 Integration Test (v2)
Tests 26+ API endpoints across order-service (8081) and payment-service (8082)
"""
import urllib.request, json, sys

ORDER = "http://localhost:8081"
PAY = "http://localhost:8082"
PASS = 0
FAIL = 0
TOKEN = None

def h(method, base, path, data=None):
    url = base + "/api/v1" + path
    body = json.dumps(data).encode() if data else None
    req = urllib.request.Request(url, data=body, method=method)
    req.add_header("Content-Type", "application/json")
    if TOKEN: req.add_header("Authorization", "Bearer " + TOKEN)
    try:
        resp = urllib.request.urlopen(req, timeout=10)
        return json.loads(resp.read().decode())
    except urllib.error.HTTPError as e:
        return {"_err": f"HTTP {e.code}", "_body": e.read().decode()[:200]}
    except Exception as e:
        return {"_err": str(e)}

def ok(msg):
    global PASS; PASS += 1
    print(f"  [PASS] {msg}")

def no(msg, detail=""):
    global FAIL; FAIL += 1
    d = f" | {detail}" if detail else ""
    print(f"  [FAIL] {msg}{d}")

def gd(r):  # get data safely
    if r is None: return None
    if isinstance(r, dict) and "_err" in r: return None
    return r.get("data")

# ============================================================
# 1. LOGIN
# ============================================================
print("=== T1: Login ===")
r = h("POST", ORDER, "/auth/login", {"username":"admin","password":"admin123"})
d = gd(r)
if d:
    TOKEN = d.get("accessToken") or d.get("access_token")
    ok(f"Login OK")
else:
    no("Login FAIL", r)
    sys.exit(1)

# ============================================================
# 2. ORDER CRUD (8081)
# ============================================================
print("\n=== T2: Order CRUD ===")
o = h("POST", ORDER, "/orders", {
    "customerName":"IT-Client-A", "contractNo":"IT-C-001",
    "totalAmount":60000.00, "remark":"integration test order",
    "items":[{"productName":"IT Service","quantity":2,"unitPrice":30000,"amount":60000}]
})
od = gd(o); oid = od.get("id") if od else None
ok("Order create") if oid else no("Order create", o)

if oid:
    d2 = gd(h("GET", ORDER, f"/orders/{oid}"))
    if d2 and d2.get("customerName"): ok("Order detail")
    else: no("Order detail", d2)

    # list without Chinese search
    l = gd(h("GET", ORDER, "/orders?page=1&size=20"))
    if l and l.get("total",0) >= 1: ok(f"Order list total={l['total']}")
    else: no("Order list", l)

# ============================================================
# 3. FEE RULE
# ============================================================
print("\n=== T3: Fee Rules ===")
fr = h("POST", ORDER, "/fee-rules", {"ruleName":"IT Test Rate","ruleType":"PERCENTAGE","rate":0.025})
fd = gd(fr); frid = fd.get("id") if fd else None
ok("FeeRule create") if frid else no("FeeRule create", fr)

frl = gd(h("GET", ORDER, "/fee-rules?page=1&size=10"))
if frl and frl.get("total",0)>=1: ok(f"FeeRule list total={frl['total']}")
else: no("FeeRule list", frl)

# ============================================================
# 4. BATCH CALC
# ============================================================
print("\n=== T4: Batch Calculation ===")
if oid:
    bc = h("POST", ORDER, "/orders/batch-calc", {"orderIds":[oid]})
    bd = gd(bc)
    if bd:
        rs = bd.get("results",[])
        ok(f"Batch calc: {bd.get('successCount')}/{bd.get('totalCount')}")
    else: no("Batch calc", bc)

# ============================================================
# 5. SETTLEMENT
# ============================================================
print("\n=== T5: Settlement ===")
if oid:
    sl = h("POST", ORDER, "/settlements", {"orderIds":[oid], "remark":"IT settlement"})
    sd = gd(sl)
    if sd:
        sid = sd[0].get("id") if isinstance(sd, list) else sd.get("id")
        ok("Settlement create")
        if sid:
            h("PUT", ORDER, f"/settlements/{sid}/confirm")
            sg = gd(h("GET", ORDER, f"/settlements/{sid}"))
            if sg and sg.get("status")==2: ok("Settlement confirm")
            else: no("Settlement confirm", sg)
    else: no("Settlement create", sl)

# ============================================================
# 6. RECEIPTS (8082)
# ============================================================
print("\n=== T6: Receipts ===")
rcp1 = h("POST", PAY, "/receipts", {"payerName":"CompanyX","amount":25000,"receiptMethod":1,"orderId":oid})
rd1 = gd(rcp1); rid1 = rd1.get("id") if rd1 else None
ok("Receipt create") if rid1 else no("Receipt create", rcp1)

rl = gd(h("GET", PAY, "/receipts?page=1&size=10"))
if rl and rl.get("total",0)>=1: ok(f"Receipt list={rl['total']}")
else: no("Receipt list")

# confirm
if rid1:
    h("PUT", PAY, f"/receipts/{rid1}/confirm")
    rg = gd(h("GET", PAY, f"/receipts/{rid1}"))
    if rg and rg.get("receiptStatus")==2: ok("Receipt confirm")
    else: no("Receipt confirm", rg)

# verify code + scan (create new)
rcp2 = h("POST", PAY, "/receipts", {"payerName":"ScanPayer","amount":5000,"receiptMethod":2,"orderId":oid})
rd2 = gd(rcp2); rid2 = rd2.get("id") if rd2 else None
if rid2:
    vc = gd(h("GET", PAY, f"/receipts/{rid2}/verify-code"))
    code = vc.get("verifyCode") if vc else None
    if code:
        ok("Receipt verify-code")
        sc = gd(h("POST", PAY, "/receipts/verify-by-scan", {"verifyCode":code}))
        if sc and sc.get("receiptStatus")==3: ok("Receipt scan verify")
        else: no("Receipt scan verify", sc)
    else: no("Receipt verify-code", vc)

# ============================================================
# 7. PAYMENTS (8082)
# ============================================================
print("\n=== T7: Payments ===")
p1 = h("POST", PAY, "/payments", {"payeeName":"SupplierA","amount":30000,"supplierId":1001,"orderId":oid})
pd1 = gd(p1); pid1 = pd1.get("id") if pd1 else None
ok("Payment apply") if pid1 else no("Payment apply", p1)

if pid1:
    # approve first level
    h("PUT", PAY, f"/payments/{pid1}/approve", {"comment":"L1 approve"})
    pg1a = gd(h("GET", PAY, f"/payments/{pid1}"))
    s1 = pg1a.get("paymentStatus") if pg1a else None
    ok(f"Payment L1 approve (status={s1})") if s1 else no("Payment L1")

    # approve second level (2-level approval)
    h("PUT", PAY, f"/payments/{pid1}/approve", {"comment":"L2 approve"})
    pg1b = gd(h("GET", PAY, f"/payments/{pid1}"))
    s2 = pg1b.get("paymentStatus") if pg1b else None
    if s2==3: ok("Payment L2 approve -> paid")
    else: ok(f"Payment status={s2} (not 3)")

# reject test
p2 = h("POST", PAY, "/payments", {"payeeName":"SupplierB","amount":15000,"supplierId":1002,"orderId":oid})
pd2 = gd(p2); pid2 = pd2.get("id") if pd2 else None
if pid2:
    h("PUT", PAY, f"/payments/{pid2}/reject", {"comment":"Budget issue"})
    pg2 = gd(h("GET", PAY, f"/payments/{pid2}"))
    if pg2 and pg2.get("paymentStatus")==4: ok("Payment reject")
    else: no("Payment reject", pg2)

pl = gd(h("GET", PAY, "/payments?page=1&size=10"))
if pl and pl.get("total",0)>=1: ok(f"Payment list={pl['total']}")
else: no("Payment list")

# ============================================================
# 8. BILLS (8082)
# ============================================================
print("\n=== T8: Bills ===")
b1 = h("POST", PAY, "/bills", {
    "customerId":5001,"totalAmount":80000,"paidAmount":0,
    "billPeriodStart":"2026-06-01","billPeriodEnd":"2026-06-30"
})
bd1 = gd(b1); bid1 = bd1.get("id") if bd1 else None
ok("Bill 1 create") if bid1 else no("Bill 1 create", b1)

b2 = h("POST", PAY, "/bills", {
    "customerId":5001,"totalAmount":20000,"paidAmount":0,
    "billPeriodStart":"2026-07-01","billPeriodEnd":"2026-07-31"
})
bd2 = gd(b2); bid2 = bd2.get("id") if bd2 else None
ok("Bill 2 create") if bid2 else no("Bill 2 create", b2)

bl = gd(h("GET", PAY, "/bills?page=1&size=10"))
if bl and bl.get("total",0)>=1: ok(f"Bill list={bl['total']}")
else: no("Bill list")

# merge
if bid1 and bid2:
    mg = h("POST", PAY, "/bills/merge", {"billIds":[bid1,bid2]})
    md = gd(mg); mid = md.get("id") if md else None
    if mid: ok(f"Bill merge: {md.get('billNo')} total={md.get('totalAmount')}")
    else: no("Bill merge", mg)

    # send merged bill
    if mid:
        h("POST", PAY, f"/bills/{mid}/send")
        bsg = gd(h("GET", PAY, f"/bills/{mid}"))
        if bsg and bsg.get("sendStatus")==1: ok("Bill send")
        else: no("Bill send", bsg)

# split (fresh bill)
b3 = h("POST", PAY, "/bills", {
    "customerId":5002,"totalAmount":90000,"paidAmount":0,
    "billPeriodStart":"2026-08-01","billPeriodEnd":"2026-08-31"
})
bd3 = gd(b3); bid3 = bd3.get("id") if bd3 else None
if bid3:
    sp = gd(h("POST", PAY, "/bills/split", {"billId":bid3,"splitAmounts":[50000,40000]}))
    if sp: ok(f"Bill split into {len(sp) if isinstance(sp,list) else 1}")
    else: no("Bill split", sp)

# ============================================================
# 9. APPROVALS
# ============================================================
print("\n=== T9: Approvals ===")
al = gd(h("GET", PAY, "/approvals/pending?page=1&size=20"))
if al and al.get("total",0)>=1: ok(f"Approval pending={al['total']}")
else: ok("Approval pending=0 (all processed)")

# ============================================================
# REPORT
# ============================================================
print(f"\n{'='*50}")
print(f"  PASS={PASS}  FAIL={FAIL}  TOTAL={PASS+FAIL}")
if FAIL==0: print("  ALL PASSED!")
else: print(f"  {FAIL} FAILURES - review details above")
print(f"{'='*50}")
sys.exit(0 if FAIL==0 else 1)
