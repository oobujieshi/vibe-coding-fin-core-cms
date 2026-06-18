# -*- coding: utf-8 -*-
"""为每个 Phase Issue 创建子任务 (child issues)"""
import urllib.request, json, time

API_KEY = "lin_api_p03itLCMntyyTFU98KAic7HwxntYGwtepLLz1nIx"
TEAM_ID = "e63fb4b7-04d0-461d-8ccd-31a3a21d4046"
API_URL = "https://api.linear.app/graphql"

# Phase parent issue IDs (from rebuild-linear.py output)
PARENTS = {
    0: "a4180860-1b71-4c30-84bf-c66d0740bde5",
    1: "fb8fe740-1b9b-4870-ab76-d491fa3d9f15",
    2: "63282d2f-593e-4630-8ccd-f967b8eb6bfd",
    3: "3d41161b-5682-48ed-ae16-920c10205606",
    4: "d3312361-b8c6-46c4-9a19-9cdbac9fa7dc",
    5: "c3d9565b-ab7a-41b7-a88e-dec5dfaa1a9b",
    6: "2e5967bb-2a9d-4c94-9fbe-099c54736c38",
    7: "e6f87a75-bbb3-49cd-99f6-cfb00ad36c10",
    8: "03bc9add-34ad-4a58-bbfe-def4bd153501",
}

# Sub-tasks: (task_id, task_name, description)
SUBTASKS = {
    0: [
        ("T0-1", "Docker Desktop 安装确认", "docker --version + docker compose version"),
        ("T0-2", "编写 docker-compose.yml", "MySQL 8.0 + Redis 7.x + RabbitMQ 3.12 + Nacos 2.3 + MinIO"),
        ("T0-3", "编写 .env 环境变量文件", "各服务密码、端口配置"),
        ("T0-4", "启动容器并验证健康状态", "docker compose up -d + docker compose ps 全部 healthy"),
        ("T0-5", "服务连接验证", "依次连接各服务确认可用"),
    ],
    1: [
        ("T1-1", "Maven 多模块项目搭建", "项目骨架 + pom.xml"),
        ("T1-2", "Vue 3 前端项目初始化", "前端骨架 + 路由 + 布局"),
        ("T1-3", "微信小程序项目初始化", "小程序骨架 + 基础组件"),
        ("T1-4", "Git 仓库 + CI/CD 流水线", ".gitlab-ci.yml / Jenkinsfile"),
        ("T1-5", "开发规范文档", "编码规范、Git 提交规范"),
    ],
    2: [
        ("T2-1", "Spring Cloud 微服务框架搭建", "Gateway + Nacos"),
        ("T2-2", "Spring Security + JWT 认证模块", "登录/登出/刷新 Token"),
        ("T2-3", "RBAC 权限模块", "用户/角色/权限 表+CRUD"),
        ("T2-4", "数据库 DDL 建表脚本", "17 张表 + 索引"),
        ("T2-5", "MyBatis-Plus 代码生成", "Entity/Mapper/Service 模板"),
        ("T2-6", "统一异常处理 + 响应体封装", "Result<T> / GlobalExceptionHandler"),
        ("T2-7", "Knife4j 接口文档集成", "API 文档自动生成"),
        ("T2-8", "审计日志 AOP", "@AuditLog + t_audit_log"),
        ("T2-9", "数据加密服务", "AES-256-GCM 工具类"),
        ("T2-10", "PC 前端登录 + 权限路由", "登录页 + 动态菜单"),
        ("T2-11", "PC 前端公共组件库", "表格/表单/弹窗/日期选择器"),
    ],
    3: [
        ("T3-1", "订单 CRUD + 状态流转", "RESTful 8 个接口"),
        ("T3-2", "费率规则配置管理", "规则 CRUD + 启用/禁用"),
        ("T3-3", "批量费用计算引擎", "合同条款匹配 + 阶梯费率"),
        ("T3-4", "结算单生成与确认/作废", "结算流程闭环"),
        ("T3-5", "EasyExcel 导入导出", "模板下载 + 校验 + 批量导出"),
        ("T3-6", "订单管理页面", "列表/详情/新建/编辑"),
        ("T3-7", "费率规则管理页面", "规则配置表单/列表"),
        ("T3-8", "结算管理页面", "批量结算 / 结算单列表"),
    ],
    4: [
        ("T4-1", "收款记录管理 + 到账确认", "receipt CRUD + 状态流转"),
        ("T4-2", "扫码核销接口", "扫码 → 后端核销流程"),
        ("T4-3", "付款申请 + 多级审批流程", "审批流引擎（状态机）"),
        ("T4-4", "客户账单生成/合并/拆分/发送", "账单全流程"),
        ("T4-5", "审批消息推送", "RabbitMQ → 小程序通知 + 邮件"),
        ("T4-6", "收款管理页面", "列表/详情/核销"),
        ("T4-7", "付款管理页面", "付款申请/审批列表"),
        ("T4-8", "账单管理页面", "生成/合并/拆分/发送"),
        ("T4-9", "审批流页面", "待审批/已审批/审批详情"),
    ],
    5: [
        ("T5-1", "银行账户管理", "账户 CRUD（含加密存储）"),
        ("T5-2", "资金流水手动录入", "流水录入 + 列表查询"),
        ("T5-3", "银行接口流水同步（预留）", "抽象接口 + Mock 实现"),
        ("T5-4", "自动流水核对引擎", "匹配算法 + 差异标记"),
        ("T5-5", "收支明细报表", "多维度统计 + 聚合查询"),
        ("T5-6", "资金余额报表", "余额趋势 + 变动脉络"),
        ("T5-7", "结算统计报表", "结算金额/未结清统计"),
        ("T5-8", "Excel/PDF 异步导出", "RabbitMQ + Worker + OSS"),
        ("T5-9", "账户管理页面", "列表/详情"),
        ("T5-10", "流水管理页面", "列表/录入"),
        ("T5-11", "对账管理页面", "差异列表/处理"),
        ("T5-12", "报表页面（3个维度）", "图表 + 表格 + 导出"),
    ],
    6: [
        ("T6-1", "小程序登录 + 授权", "微信登录对接"),
        ("T6-2", "账单查询页面", "账单列表/详情"),
        ("T6-3", "支付确认页面", "扫码支付/确认"),
        ("T6-4", "扫码核销页面", "扫码 → 核销流程"),
        ("T6-5", "审批通知 + 处理", "消息推送 + 审批操作"),
        ("T6-6", "报表摘要页面", "关键数据卡片展示"),
    ],
    7: [
        ("T7-1", "前后端联调", "全流程走通"),
        ("T7-2", "单元测试（覆盖率 ≥ 70%）", "测试报告"),
        ("T7-3", "压力测试（批量结算 10000 订单）", "压测报告"),
        ("T7-4", "安全渗透测试", "渗透测试报告"),
        ("T7-5", "SQL 性能优化", "慢查询分析 + 索引调整"),
        ("T7-6", "前端性能优化", "打包优化 / 懒加载"),
        ("T7-7", "部署脚本 + Docker 镜像", "Dockerfile + K8s yaml"),
    ],
    8: [
        ("T8-1", "生产环境部署", "上线清单"),
        ("T8-2", "用户培训", "操作手册 + 培训视频"),
        ("T8-3", "验收文档交付", "技术文档 + API文档 + 运维手册"),
    ],
}


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


def create_subtask(phase: int, tid: str, name: str, desc: str, parent_id: str) -> bool:
    title = json.dumps(f"{tid}：{name}", ensure_ascii=False)[1:-1]
    description = json.dumps(desc, ensure_ascii=False)[1:-1]
    q = (
        f'mutation {{ issueCreate(input: {{'
        f' title: "{title}",'
        f' description: "{description}",'
        f' teamId: "{TEAM_ID}",'
        f' parentId: "{parent_id}"'
        f' }}) {{ issue {{ id title }} }} }}'
    )
    try:
        res = gql(q)
        issue = res["data"]["issueCreate"]["issue"]
        return issue
    except Exception as e:
        print(f"    ERR: {e}")
        return None


total = sum(len(v) for v in SUBTASKS.values())
done = 0
print(f"共 {total} 个子任务\n")

for phase in range(0, 9):
    parent_id = PARENTS[phase]
    tasks = SUBTASKS[phase]
    print(f"Phase {phase}（{len(tasks)} 个子任务）：")
    for tid, name, desc in tasks:
        result = create_subtask(phase, tid, name, desc, parent_id)
        if result:
            done += 1
            print(f"  [OK] {result['title']}")
        # no delay for speed
    print()

print(f"完成：{done}/{total}")
