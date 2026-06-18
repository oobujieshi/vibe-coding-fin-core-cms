import urllib.request,json,sys,os
B="http://localhost:8081/api/v1"; T=None; p=f=0
def ok(m):
    global p; p+=1; print(f"  PASS {m}", flush=True)
def no(m):
    global f; f+=1; print(f"  FAIL {m}", flush=True)
def api(m,u,b=None):
    d=json.dumps(b).encode()if b else None
    h={"Content-Type":"application/json"}
    if T: h["Authorization"]=f"Bearer {T}"
    q=urllib.request.Request(B+u,data=d,headers=h,method=m)
    try: return json.loads(urllib.request.urlopen(q,timeout=10).read().decode())
    except urllib.error.HTTPError as e: return json.loads(e.read())
    except Exception as ex: return {"error":str(ex)}

print("1. Login", flush=True)
x=api("POST","/auth/login",{"username":"admin","password":"admin123"})
if x.get("code")==200: T=x["data"]["accessToken"]; ok("login admin/admin123")
else: no(str(x)); sys.exit(1)

print("2. Order CRUD", flush=True)
o={"customerName":"TestCust","contractNo":"HT-TEST-001","totalAmount":100000,"remark":"test","items":[{"productName":"ProductA","quantity":2,"unitPrice":30000,"amount":60000}]}
x=api("POST","/orders",o)
if not x.get("data"): no(f"create: {x}"); sys.exit(1)
oid=x["data"]["id"]; ono=x["data"]["orderNo"]; ok(f"create #{oid} {ono}")

x=api("GET",f"/orders/{oid}")
if x.get("data") and x["data"].get("customerName")==o["customerName"]: ok(f"detail {x['data']['customerName']}")
else: no(f"detail: {x}")

x=api("GET","/orders?page=1&size=10")
if x.get("data") and x["data"].get("total",0)>=1: ok(f"list {x['data']['total']} rows")
else: no(f"list: {x}")

print("3. FeeRule", flush=True)
x=api("POST","/fee-rules",{"ruleName":"TestRate2pct","ruleType":"PERCENTAGE","rate":0.02,"minFee":100,"maxFee":5000})
if x.get("data") and x["data"].get("id"): rid=x["data"]["id"]; ok(f"create #{rid}")
else: no(f"create: {x}"); rid=None

if rid:
    x=api("GET","/fee-rules?page=1&size=10")
    if x.get("data"): ok(f"list {x['data'].get('total',0)} rules")
    else: no(f"list: {x}")

print("4. Batch Calc", flush=True)
x=api("POST","/orders/batch-calc",{"orderIds":[oid]})
if x.get("data") and x["data"].get("successCount")==1: ok(f"calc ok success={x['data']['successCount']}")
else: no(f"calc: {x}")

x=api("GET",f"/orders/{oid}")
if x.get("data") and x["data"].get("feeAmount") and x["data"]["feeAmount"]>0: ok(f"fee amount={x['data']['feeAmount']}")
else: no(f"fee: {x}")

print("5. Settlement", flush=True)
x=api("POST","/settlements",{"orderIds":[oid],"remark":"test settle"})
if x.get("data") and len(x["data"])>0 and x["data"][0].get("id"):
    sid=x["data"][0]["id"]; sn=x["data"][0]["settlementNo"]; ok(f"settle #{sid} {sn}")
else: no(f"settle: {x}"); sid=None

if sid:
    x=api("GET","/settlements?page=1&size=10")
    if x.get("data"): ok(f"settle list {x['data'].get('total',0)}")
    else: no(f"settle list: {x}")

    x=api("PUT",f"/settlements/{sid}/confirm")
    if x.get("code")==200: ok("confirm settle")
    else: no(f"confirm: {x}")

print(f"\n{'='*30}\nPASS={p} FAIL={f}\n{'='*30}", flush=True)
sys.exit(0 if f==0 else 1)
