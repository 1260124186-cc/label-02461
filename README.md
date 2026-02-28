# RBAC 权限管理系统

基于 Spring Boot 3 + MyBatis-Plus + Spring Security + SpringDoc 的 RBAC 权限管理系统。

## How to Run

```bash
# 克隆项目后，在项目根目录执行（可用 docker compose 或 docker-compose）
docker-compose up --build -d

# 查看日志
docker-compose logs -f backend

# 停止服务
docker-compose down

# 停止并清除数据
docker-compose down -v
```

启动完成后访问：
- API 服务：http://localhost:8080
- Swagger 文档：http://localhost:8080/swagger-ui.html

## Services

| 服务 | 端口 | 说明 |
|------|------|------|
| backend | 8080 | Spring Boot 后端 API 服务 |
| mysql | 3306 | MySQL 8.0 数据库 |

## 测试账号

| 用户名 | 密码 | 角色 | 说明 |
|--------|------|------|------|
| admin | admin123 | 超级管理员 | 拥有所有权限 |

登录接口：`POST /auth/login`

```json
{
  "username": "admin",
  "password": "admin123"
}
```

## 题目内容

现在帮我给这个项目使用springboot+mybatis+mybatis-plus+springsecurity+springdoc，生成一个rbac权限系统，使用三层架构，整个项目分模块进行设计，rbac是system这个模块里面的内容，包名统一为：com.cqjtjc

---

## 项目介绍

### 技术栈

- Spring Boot 3.2.5
- MyBatis-Plus 3.5.6
- Spring Security 6
- SpringDoc OpenAPI 2.5 (Swagger)
- JWT (jjwt 0.12.5)
- MySQL 8.0
- Docker & Docker Compose

### 项目结构

```
├── .gitignore
├── README.md
├── docker-compose.yml              # Docker 编排
└── backend/                        # 后端（Maven 多模块）
    ├── Dockerfile                  # 多阶段构建
    ├── pom.xml                     # 父 POM
    ├── sql/
    │   └── init.sql                # 数据库初始化
    ├── scripts/
    │   └── self-test.sh            # 自测脚本
    ├── label-common/                # 通用模块
    │   ├── pom.xml
    │   └── src/main/java/com/cqjtjc/common/
    │       ├── config/              # WebMvc、MyBatis-Plus、JWT 等配置
    │       ├── exception/           # 全局异常、BusinessException
    │       ├── result/              # R、PageResult 统一响应
    │       ├── utils/               # JwtUtils
    │       └── validation/          # AddGroup、UpdateGroup
    ├── label-system/                # 系统模块（RBAC）
    │   ├── pom.xml
    │   └── src/main/java/com/cqjtjc/system/
    │       ├── controller/          # Auth、SysUser、SysRole、SysMenu
    │       ├── domain/              # entity、dto、vo
    │       ├── mapper/              # MyBatis Mapper
    │       ├── security/            # JWT 过滤、Security 配置
    │       └── service/             # 业务接口与实现
    └── label-admin/                 # 启动模块
        ├── pom.xml
        └── src/main/
            ├── java/com/cqjtjc/
            │   └── LabelApplication.java
            └── resources/
                └── application.yml
```

### RBAC 模型

- 用户(sys_user) ↔ 角色(sys_role)：多对多，通过 sys_user_role 关联
- 角色(sys_role) ↔ 菜单/权限(sys_menu)：多对多，通过 sys_role_menu 关联
- 菜单类型：M(目录) / C(菜单) / F(按钮权限)
- 接口级权限控制：通过 `@PreAuthorize("hasAuthority('xxx')")` 实现

### API 接口

| 模块 | 接口 | 说明 |
|------|------|------|
| 认证 | POST /auth/login | 登录获取 JWT Token |
| 认证 | GET /auth/info | 获取当前用户信息 |
| 用户 | GET /system/user/page | 分页查询用户（参数：current, size） |
| 用户 | POST /system/user | 新增用户 |
| 用户 | PUT /system/user | 修改用户 |
| 用户 | PUT /system/user/{id}/resetPassword | 重置密码（查询参数：newPassword） |
| 用户 | DELETE /system/user/{id} | 删除用户 |
| 角色 | GET /system/role/page | 分页查询角色 |
| 角色 | POST /system/role | 新增角色 |
| 角色 | PUT /system/role | 修改角色 |
| 角色 | DELETE /system/role/{id} | 删除角色 |
| 菜单 | GET /system/menu/tree | 查询菜单树 |
| 菜单 | POST /system/menu | 新增菜单 |
| 菜单 | PUT /system/menu | 修改菜单 |
| 菜单 | DELETE /system/menu/{id} | 删除菜单 |

---

## 自测命令

**一键自测（推荐）**：先启动服务，再在项目根目录执行：

```bash
# 启动服务（项目根目录）
docker compose up --build -d

# 等待后端就绪后执行自测脚本（约 30–60 秒）
./backend/scripts/self-test.sh
```

脚本覆盖自测流程中全部 curl：登录、用户信息、菜单树/新增/修改/删除、角色分页/新增/修改/删除、用户分页/新增/修改/重置密码、权限验证（testuser 预期 403）、删除测试用户；共 16 项，全部通过则输出 `自测全部通过`。

如需指定后端地址：`BASE_URL=http://127.0.0.1:8080 ./backend/scripts/self-test.sh`

---

## 自测流程（手动 curl）

> 以下所有请求均使用 `curl` 示例，也可使用 Postman / Swagger UI (`http://localhost:8080/swagger-ui.html`) 进行测试。

### 1. 启动服务

```bash
docker compose up --build -d
# 等待后端就绪（首次启动约 30-60s）
docker compose logs -f backend
# 看到 "Started Application" 字样即可
```

### 2. 登录获取 Token

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

返回结果中包含 `token` 字段，后续请求均需在 Header 中携带：

```
Authorization: Bearer <token>
```

### 3. 获取当前用户信息

```bash
curl http://localhost:8080/auth/info \
  -H "Authorization: Bearer <token>"
```

验证返回的用户名、角色、权限列表是否正确。

### 4. 菜单管理

```bash
# 查询菜单树
curl http://localhost:8080/system/menu/tree \
  -H "Authorization: Bearer <token>"

# 新增菜单
curl -X POST http://localhost:8080/system/menu \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"menuName":"测试菜单","parentId":0,"sort":1,"menuType":"M","status":1,"visible":1}'

# 修改菜单（使用新增返回的 id）
curl -X PUT http://localhost:8080/system/menu \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"id":<id>,"menuName":"测试菜单-改"}'

# 删除菜单
curl -X DELETE http://localhost:8080/system/menu/<id> \
  -H "Authorization: Bearer <token>"
```

### 5. 角色管理

```bash
# 分页查询角色
curl "http://localhost:8080/system/role/page?current=1&size=10" \
  -H "Authorization: Bearer <token>"

# 新增角色
curl -X POST http://localhost:8080/system/role \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"roleName":"测试角色","roleKey":"test_role","sort":2,"menuIds":[]}'

# 修改角色（使用新增返回的 id）
curl -X PUT http://localhost:8080/system/role \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"id":<id>,"roleName":"测试角色-改","roleKey":"test_role","sort":2}'

# 删除角色
curl -X DELETE http://localhost:8080/system/role/<id> \
  -H "Authorization: Bearer <token>"
```

### 6. 用户管理

```bash
# 分页查询用户（参数：current=页码, size=每页条数）
curl "http://localhost:8080/system/user/page?current=1&size=10" \
  -H "Authorization: Bearer <token>"

# 新增用户
curl -X POST http://localhost:8080/system/user \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"test123","nickname":"测试用户","roleIds":[]}'

# 修改用户（使用新增返回的 id）
curl -X PUT http://localhost:8080/system/user \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"id":<id>,"nickname":"测试用户-改"}'

# 重置密码（路径传用户 id，查询参数传新密码）
curl -X PUT "http://localhost:8080/system/user/<id>/resetPassword?newPassword=newpass123" \
  -H "Authorization: Bearer <token>"

# 删除用户
curl -X DELETE http://localhost:8080/system/user/<id> \
  -H "Authorization: Bearer <token>"
```

### 7. 权限验证

使用步骤 6 新增的 `testuser` 登录（该用户未分配角色），调用需要权限的接口（如 `GET /system/user/page`），预期返回 403 Forbidden，验证权限控制生效。

### 8. 停止服务

```bash
docker compose down      # 停止服务
docker compose down -v   # 停止并清除数据卷
```
