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

## API 文档（Swagger / OpenAPI）

- Swagger UI：`http://localhost:8080/swagger-ui.html`
- OpenAPI JSON：`http://localhost:8080/v3/api-docs`
- 详细接口文档（含鉴权、权限点、参数与示例）：见 [`docs/API.md`](docs/API.md)

> 说明：除 `POST /auth/login` 外，其它接口默认需要 `Authorization: Bearer <token>`；并且会根据 `@PreAuthorize("hasAuthority('xxx')")` 做权限校验（对应 `sys_menu.permission`）。

## Services

| 服务 | 端口 | 说明 |
|------|------|------|
| backend | 8080 | Spring Boot 后端 API 服务 |
| mysql | 3306 | MySQL 8.0 数据库（容器内 3306；宿主机映射 13306，本机连接请用 `localhost:13306`） |

### 环境变量（后端）

| 变量 | 必填 | 说明 |
|------|------|------|
| `JWT_SECRET` | 是 | JWT 签名密钥，建议 ≥32 字符的强随机串。生成示例：`openssl rand -base64 32`。生产环境务必单独配置，勿使用默认值。 |
| `JWT_EXPIRATION` | 否 | Token 有效期（毫秒），默认 86400000（24 小时） |
| `DB_HOST` / `DB_PORT` / `DB_NAME` / `DB_USER` / `DB_PASSWORD` | 否 | 数据库连接，见 `application.yml` 默认值 |

使用 Docker Compose 时，可在项目根目录设置后再启动：`export JWT_SECRET=$(openssl rand -base64 32)` 或写入 `.env` 后 `docker-compose up -d`。

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

项目采用三层架构：**Controller（接口层）→ Service（业务层）→ Mapper（数据访问层）**；`entity`、`dto`、`vo` 直接位于 system 包下，分别承载持久化实体、请求入参与响应出参，与源码包结构一致。

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
    │   └── src/main/
    │       ├── java/com/cqjtjc/system/
    │       │   ├── controller/      # Auth、SysUser、SysRole、SysMenu
    │       │   ├── entity/          # 持久化实体（SysUser、SysRole、SysMenu、SysUserRole、SysRoleMenu）
    │       │   ├── dto/             # 请求/入参（LoginDTO、SysUserDTO、SysRoleDTO、SysMenuDTO）
    │       │   ├── vo/              # 响应/视图（LoginVO、MenuTreeVO）
    │       │   ├── mapper/          # MyBatis Mapper 接口
    │       │   ├── security/        # JWT 过滤、Security 配置
    │       │   ├── service/         # 业务接口与实现
    │       │   └── util/            # 业务工具（如 PasswordStrengthValidator）
    │       └── resources/
    │           └── mapper/          # Mapper XML，自定义 SQL 统一在此（mapper-locations 扫描）
    │               ├── SysUserMapper.xml
    │               ├── SysRoleMapper.xml
    │               ├── SysMenuMapper.xml
    │               ├── SysUserRoleMapper.xml
    │               └── SysRoleMenuMapper.xml
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

### 核心功能使用示例（推荐按此顺序操作）

更完整的参数与响应示例见 [`docs/API.md`](docs/API.md)。下面给出一个最常用的 RBAC 配置闭环：

1) **登录拿 Token**：`POST /auth/login` → 得到 `token`
2) **创建菜单/权限点**（可选）：`POST /system/menu`
   - 按钮权限使用 `menuType=F` 且填写 `permission`（例如 `system:user:add`）
3) **创建角色并绑定权限点**：`POST /system/role`（`menuIds` 传上一步菜单/权限的 ID 列表）
4) **创建用户并分配角色**：`POST /system/user`（`roleIds` 传角色 ID 列表）
5) **验证权限生效**：用新用户登录后访问受限接口，预期 403 或 200（取决于是否授予对应 `permission`）

### 实现要点

- **Mapper**：自定义 SQL 统一写在 `resources/mapper/*.xml`，与 `application.yml` 中 `mapper-locations: classpath*:mapper/**/*.xml` 对应；基础 CRUD 使用 MyBatis-Plus `BaseMapper`；用户-角色、角色-菜单关联采用批量插入（`insertBatch`）提升写入效率。
- **自动填充**：`createTime`、`updateTime`、`deleted` 由 `label-common` 的 `MetaObjectHandler` 在插入/更新时自动填充，Service 层无需手动赋值。
- **日志**：Service 层对新增/修改/删除等关键操作使用 `@Slf4j` 记录 INFO 日志，便于排查与审计。
- **密码与安全**：新增用户必须设置密码（无默认弱密码）；密码强度校验：长度 8~128 位，须包含字母和数字（`PasswordStrengthValidator`），重置密码同样适用；JWT 密钥通过环境变量 `JWT_SECRET` 配置，禁止在配置文件中硬编码。
- **入参校验**：用户 DTO 使用 Jakarta Validation 做长度与格式校验（用户名 2~50、密码 8~128、昵称/邮箱/手机长度及邮箱格式等），新增/修改分组（AddGroup / UpdateGroup）区分必填与可选。
- **初始化数据**：`backend/sql/init.sql` 包含建表与完整初始化数据（用户、角色、菜单及关联），脚本末尾有结束标记便于确认未截断。

### API 接口

| 模块 | 接口 | 说明 |
|------|------|------|
| 认证 | POST /auth/login | 登录获取 JWT Token |
| 认证 | GET /auth/info | 获取当前用户信息（需登录） |
| 用户 | GET /system/user/page | 分页查询用户（参数：current, size, username, status） |
| 用户 | GET /system/user/{id} | 获取用户详情（需登录） |
| 用户 | POST /system/user | 新增用户（请求体需含 password，且满足长度与强度校验） |
| 用户 | PUT /system/user | 修改用户 |
| 用户 | PUT /system/user/{id}/resetPassword | 重置密码（查询参数：newPassword，须满足密码强度） |
| 用户 | DELETE /system/user/{id} | 删除用户 |
| 角色 | GET /system/role/page | 分页查询角色（参数：current, size, roleName） |
| 角色 | GET /system/role/list | 查询所有角色（下拉等场景） |
| 角色 | GET /system/role/{id} | 获取角色详情 |
| 角色 | POST /system/role | 新增角色 |
| 角色 | PUT /system/role | 修改角色 |
| 角色 | DELETE /system/role/{id} | 删除角色 |
| 菜单 | GET /system/menu/tree | 查询菜单树 |
| 菜单 | GET /system/menu/list | 查询所有菜单 |
| 菜单 | GET /system/menu/{id} | 获取菜单详情 |
| 菜单 | GET /system/menu/role/{roleId} | 根据角色ID查询菜单ID列表 |
| 菜单 | POST /system/menu | 新增菜单 |
| 菜单 | PUT /system/menu | 修改菜单 |
| 菜单 | DELETE /system/menu/{id} | 删除菜单 |

除登录外，上述接口均需在 Header 中携带 `Authorization: Bearer <token>`；角色/用户/菜单的增删改查均受 `@PreAuthorize` 权限控制（如 system:role:query、system:menu:add 等）。

### 单元测试

项目在各模块下提供 `src/test` 目录，满足工程细节与规范要求：

| 模块 | 测试目录 | 说明 |
|------|----------|------|
| label-common | `src/test/java/com/cqjtjc/common/` | 通用组件：`RTest`、`PageResultTest` |
| label-system | `src/test/java/com/cqjtjc/system/` | 业务层：如 `SysUserServiceImplTest`（Mock Mapper/Encoder） |
| label-admin | `src/test/java/com/cqjtjc/` | 启动：`LabelApplicationTests`（上下文加载，测试 profile 使用 H2 内存库） |

运行全部单元测试（需安装 Maven）：

```bash
cd backend
mvn test
```

按模块运行：`mvn test -pl label-common`、`mvn test -pl label-system`、`mvn test -pl label-admin`。
label-admin 的上下文测试使用 `application-test.yml` + H2，无需启动 MySQL。

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

# 新增用户（密码须 8 位以上且含字母与数字）
curl -X POST http://localhost:8080/system/user \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"test1234","nickname":"测试用户","roleIds":[]}'

# 修改用户（使用新增返回的 id）
curl -X PUT http://localhost:8080/system/user \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"id":<id>,"nickname":"测试用户-改"}'

# 重置密码（路径传用户 id，查询参数传新密码；新密码须满足强度：8~128 位且含字母与数字）
curl -X PUT "http://localhost:8080/system/user/<id>/resetPassword?newPassword=Newpass123" \
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
