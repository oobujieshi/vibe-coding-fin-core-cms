# -*- coding: utf-8 -*-
"""清理 Linear 旧项目 + 重建中文版"""
import urllib.request, json, sys, time, os

API_KEY = os.environ.get("LINEAR_API_KEY", "")
if not API_KEY:
    # fallback: read from project .env
    env_file = os.path.join(os.path.dirname(__file__), "..", ".env")
    if os.path.exists(env_file):
        with open(env_file) as f:
            for line in f:
                if line.startswith("LINEAR_API_KEY="):
                    API_KEY = line.strip().split("=", 1)[1]
                    break
TEAM_ID = "e63fb4b7-04d0-461d-8ccd-31a3a21d4046"
OLD_PROJECT_ID = "16732aca-d793-42a8-aab1-0d40676773df"
API_URL = "https://api.linear.app/graphql"

OLD_ISSUES = [
    "eb0227a9-6033-4f55-b003-b203a7ba5487",
    "f289e251-836a-46c2-978e-f87769919a08",
    "7401f499-4c79-4dbc-bce6-d1ce4b6acd0f",
    "5dc3ed0d-4825-491f-bf0e-5d31afda00a1",
    "39ce8191-b1af-42a1-b172-18610abd64e1",
    "2c9a11d0-58bb-44ea-b6f4-3e826d8b1fae",
    "dc6312d1-4e91-495c-80fa-c0251e85e993",
    "3db01908-6660-40c2-8978-eefab3e1229d",
    "8d6fe2ec-e4f2-4be8-ba06-09f3c4f15607",
]

PHASES = [
    ("Phase 0：开发环境安装",
     "Docker 环境搭建：MySQL 8.0 + Redis 7.x + RabbitMQ 3.12 + Nacos 2.3 + MinIO\n任务：T0-1 ~ T0-5"),
    ("Phase 1：项目初始化",
     "三端骨架搭建：Maven 多模块后端 + Vue3 前端 + 微信小程序 + Git 仓库 + CI/CD + 开发规范\n任务：T1-1 ~ T1-5"),
    ("Phase 2：基础设施搭建",
     "微服务基础设施：SpringCloud + Nacos + Gateway + JWT 认证 + RBAC 权限 + 17张表DDL + MyBatisPlus 代码生成 + 统一异常/AOP审计/加密 + 前端登录+组件库\n任务：T2-1 ~ T2-11"),
    ("Phase 3：订单结算服务",
     "订单 CRUD + 状态流转 + 费率规则管理 + 批量费用计算引擎 + 结算单全流程 + EasyExcel 导入导出 + 3个前端页面\n任务：T3-1 ~ T3-8"),
    ("Phase 4：收付款服务",
     "收款管理 + 到账确认 + 扫码核销 + 付款多级审批 + 客户账单生成/合并/拆分/发送 + RabbitMQ 消息推送 + 4个前端页面\n任务：T4-1 ~ T4-9"),
    ("Phase 5：资金流水与报表",
     "银行账户管理 + 流水手动录入/同步 + 自动对账引擎 + 3张报表 + Excel/PDF 异步导出 + 4个前端页面\n任务：T5-1 ~ T5-12"),
    ("Phase 6：小程序端",
     "可与 Phase 5 并行：微信登录 + 账单查询 + 支付确认 + 扫码核销 + 审批通知 + 报表摘要\n任务：T6-1 ~ T6-6"),
    ("Phase 7：集成测试与部署",
     "前后端联调 + 单元测试(≥70%) + 压力测试 + 安全渗透 + SQL/前端性能优化 + Docker镜像/K8s部署\n任务：T7-1 ~ T7-7"),
    ("Phase 8：上线与验收",
     "生产环境部署 + 用户培训 + 验收文档交付（技术文档+API文档+运维手册）\n任务：T8-1 ~ T8-3"),
]


def gql(query: str) -> dict:
    data = json.dumps({"query": query}, ensure_ascii=False).encode("utf-8")
    req = urllib.request.Request(
        API_URL,
        data=data,
        headers={
            "Authorization": API_KEY,
            "Content-Type": "application/json; charset=utf-8",
        },
        method="POST",
    )
    with urllib.request.urlopen(req, timeout=15) as resp:
        return json.loads(resp.read().decode("utf-8"))


# --- Step 1: Archive old issues ---
print("=== 1. 归档旧 Issues ===")
for iid in OLD_ISSUES:
    q = f'mutation {{ issueArchive(id: "{iid}") {{ success }} }}'
    try:
        res = gql(q)
        ok = res["data"]["issueArchive"]["success"]
        print(f"  {iid[:8]}... -> {'OK' if ok else 'FAIL'}")
    except Exception as e:
        print(f"  {iid[:8]}... -> ERR: {e}")
    time.sleep(0.3)

# --- Step 2: Delete old project ---
print("\n=== 2. 删除旧项目 ===")
q = f'mutation {{ projectDelete(id: "{OLD_PROJECT_ID}") {{ success }} }}'
try:
    res = gql(q)
    print(f"  -> {res}")
except Exception as e:
    print(f"  -> ERR: {e}")
time.sleep(0.5)

# --- Step 3: Create new project ---
print("\n=== 3. 创建新项目 FinCoreCms ===")
q = f'mutation {{ projectCreate(input: {{ name: "FinCoreCms", description: "财务管理模块开发 — Phase 0~8 执行计划", teamIds: ["{TEAM_ID}"] }}) {{ project {{ id name url }} }} }}'
res = gql(q)
new_project = res["data"]["projectCreate"]["project"]
print(f"  项目: {new_project['name']}")
print(f"  ID: {new_project['id']}")
print(f"  URL: {new_project['url']}")

# --- Step 4: Create Phase issues ---
print("\n=== 4. 创建 Phase Issues（中文） ===")
issue_map = {}
for title, desc in PHASES:
    # Use JSON-safe strings
    safe_title = json.dumps(title, ensure_ascii=False)[1:-1]
    safe_desc = json.dumps(desc, ensure_ascii=False)[1:-1]
    q = (
        f'mutation {{ issueCreate(input: {{'
        f' title: "{safe_title}",'
        f' description: "{safe_desc}",'
        f' teamId: "{TEAM_ID}",'
        f' projectId: "{new_project["id"]}"'
        f' }}) {{ issue {{ id title url }} }} }}'
    )
    try:
        res = gql(q)
        issue = res["data"]["issueCreate"]["issue"]
        issue_map[title] = issue
        print(f"  {issue['title']} -> {issue['url']}")
    except Exception as e:
        print(f"  {title} -> ERR: {e}")
    time.sleep(0.3)

# --- Summary ---
print("\n=== 5. 汇总 ===")
print(f"项目: {new_project['url']}\n")
for title in PHASES:
    issue = issue_map.get(title[0])
    if issue:
        print(f"| {title[0]} | {issue['id']} | {issue['url']} |")
