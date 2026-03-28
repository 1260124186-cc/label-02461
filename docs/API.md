# API 文档（RBAC 权限管理系统）

本文档在 `README.md` 的接口列表基础上，补充**鉴权方式、权限点（Authority）映射、参数说明、示例请求/响应、常见错误码**，用于快速联调与二次开发。

## 1. 基础信息

- **Base URL**：`http://localhost:8080`
- **Swagger UI**：`/swagger-ui.html`
- **OpenAPI JSON**：`/v3/api-docs`

## 2. 统一响应结构

系统所有接口均使用统一响应体 `R<T>`：

### 2.1 成功响应

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {}
}
```

### 2.2 失败响应

```json
{
  "code": 403,
  "msg": "没有权限访问",
  "data": null
}
```

> 说明：失败时 HTTP 状态码可能为 401/403/500（见“错误码与异常”）；同时 `code` 字段也会给出业务码（与全局异常处理保持一致）。

## 3. 鉴权与权限点（Authority）

### 3.1 Token 获取

通过 `POST /auth/login` 登录，返回 `data.token`。后续请求在 Header 中携带：

```
Authorization: Bearer <token>
```

### 3.2 权限点如何定义

接口权限校验通过 Spring Security 的 `@PreAuthorize("hasAuthority('xxx')")` 实现，`xxx` 对应 **菜单/按钮表 `sys_menu.permission`**。

- `menuType=M`：目录（一般不设置 `permission`）
- `menuType=C`：菜单（可设置 `permission`，常用于“页面可见/可访问”）
- `menuType=F`：按钮权限（建议必须设置 `permission`，用于接口级授权）

示例（按钮权限）：

```json
{
  "menuName": "用户新增",
  "parentId": 100,
  "menuType": "F",
  "permission": "system:user:add",
  "status": 1,
  "visible": 1,
  "sort": 2
}
```

## 4. 错误码与异常（全局约定）

后端通过 `GlobalExceptionHandler` 统一处理异常，返回体均为 `R`；**HTTP 状态码**与**业务 code** 对应关系如下。

### 4.1 安全与参数类（框架/校验）

| HTTP | code | 说明 |
|------|------|------|
| 400 | 400 | 参数校验失败（`@Valid` / `@Validated` / 绑定错误） |
| 401 | 401 | 用户名或密码错误（`BadCredentialsException`）或未登录（业务抛 `ErrorCode.UNAUTHORIZED`） |
| 403 | 403 | 没有权限访问（`AccessDeniedException`）或用户已被禁用（`ErrorCode.USER_DISABLED`） |
| 500 | 500 | 未捕获的系统异常 |

### 4.2 业务错误码（ErrorCode 枚举）

业务异常推荐使用 `ErrorCode` 构造，以便返回正确的 HTTP 状态与统一文案。常见枚举与 HTTP 对应关系：

| 业务码 | HTTP 状态 | 说明 |
|--------|-----------|------|
| 400 | 400 | 密码相关：未设置、长度不符、缺少字母/数字等（`PASSWORD_*`） |
| 401 | 401 | 未登录（`UNAUTHORIZED`） |
| 403 | 403 | 用户已被禁用（`USER_DISABLED`） |
| 404 | 404 | 用户/角色/菜单不存在（`USER_NOT_FOUND`、`ROLE_NOT_FOUND`、`MENU_NOT_FOUND`） |
| 409 | 409 | 唯一性冲突：用户名已存在、角色标识已存在、存在子菜单不允许删除等（`USERNAME_EXISTS`、`ROLE_KEY_EXISTS`、`MENU_HAS_CHILDREN`） |
| 500 | 500 | 其它业务错误（`BUSINESS_ERROR` 或未归类） |

失败响应形态示例（参数校验）：

```json
{
  "code": 400,
  "msg": "username: 用户名不能为空; password: 密码不能为空",
  "data": null
}
```

业务异常示例（资源不存在时 HTTP 为 404）：

```json
{
  "code": 404,
  "msg": "用户不存在",
  "data": null
}
```

## 5. 认证模块（Auth）

### 5.1 登录

- **接口**：`POST /auth/login`
- **鉴权**：否
- **请求体**：`LoginDTO`

```json
{
  "username": "admin",
  "password": "admin123"
}
```

成功响应（示例）：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIs...",
    "userInfo": {
      "id": 1,
      "username": "admin",
      "nickname": "超级管理员",
      "avatar": null,
      "roles": ["admin"],
      "permissions": ["system:user:list", "system:role:add"]
    }
  }
}
```

### 5.2 获取当前用户信息

- **接口**：`GET /auth/info`
- **鉴权**：是（Bearer Token）
- **权限点**：无（只要已登录即可）

```bash
curl "http://localhost:8080/auth/info" \
  -H "Authorization: Bearer <token>"
```

## 6. 用户模块（SysUser）

### 6.1 分页查询用户

- **接口**：`GET /system/user/page`
- **鉴权**：是
- **权限点**：`system:user:list`
- **Query 参数**：
  - `current`：页码，默认 1
  - `size`：每页条数，默认 10
  - `username`：可选，模糊匹配
  - `status`：可选，0/1

示例：

```bash
curl "http://localhost:8080/system/user/page?current=1&size=10&username=adm" \
  -H "Authorization: Bearer <token>"
```

返回 `PageResult<SysUser>`（示例）：

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "total": 1,
    "current": 1,
    "size": 10,
    "pages": 1,
    "records": [
      {
        "id": 1,
        "username": "admin",
        "password": null,
        "nickname": "超级管理员",
        "email": null,
        "phone": null,
        "avatar": null,
        "status": 1,
        "createTime": "2026-02-28T10:00:00",
        "updateTime": "2026-02-28T10:00:00",
        "deleted": 0
      }
    ]
  }
}
```

> 注意：接口会主动将 `password` 置空，避免泄露。

### 6.2 获取用户详情

- **接口**：`GET /system/user/{id}`
- **鉴权**：是
- **权限点**：`system:user:query`

### 6.3 新增用户

- **接口**：`POST /system/user`
- **鉴权**：是
- **权限点**：`system:user:add`
- **请求体**：`SysUserDTO`
- **关键校验**：
  - `username`：2~50，必填
  - `password`：8~128，必填，并且需包含字母与数字（强度校验）
  - `roleIds`：可选，用户-角色绑定

示例：

```bash
curl -X POST "http://localhost:8080/system/user" \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "username":"u1",
    "password":"Pass1234",
    "nickname":"用户1",
    "roleIds":[]
  }'
```

成功响应：`data` 为新增用户 ID（`Long`）。

### 6.4 修改用户

- **接口**：`PUT /system/user`
- **鉴权**：是
- **权限点**：`system:user:edit`
- **请求体**：`SysUserDTO`（需包含 `id`）

### 6.5 删除用户

- **接口**：`DELETE /system/user/{id}`
- **鉴权**：是
- **权限点**：`system:user:delete`

### 6.6 重置密码

- **接口**：`PUT /system/user/{id}/resetPassword`
- **鉴权**：是
- **权限点**：`system:user:resetPwd`
- **请求体**：`ResetPasswordDTO`（同样要求强密码）

示例：

```bash
curl -X PUT "http://localhost:8080/system/user/1/resetPassword" \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "newPassword":"NewPass1"
  }'
```

## 7. 角色模块（SysRole）

### 7.1 分页查询角色

- **接口**：`GET /system/role/page`
- **鉴权**：是
- **权限点**：`system:role:list`
- **Query 参数**：`current`、`size`、`roleName(可选)`

### 7.2 查询所有角色

- **接口**：`GET /system/role/list`
- **鉴权**：是
- **权限点**：`system:role:query`

### 7.3 获取角色详情

- **接口**：`GET /system/role/{id}`
- **鉴权**：是
- **权限点**：`system:role:query`

### 7.4 新增角色（绑定菜单/权限）

- **接口**：`POST /system/role`
- **鉴权**：是
- **权限点**：`system:role:add`
- **请求体**：`SysRoleDTO`
- **字段说明**：
  - `roleKey`：角色标识（唯一）
  - `menuIds`：该角色拥有的菜单/权限 ID 列表（对应 `sys_menu.id`）

示例：

```bash
curl -X POST "http://localhost:8080/system/role" \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "roleName":"普通用户",
    "roleKey":"user",
    "sort":2,
    "menuIds":[1021,1001]
  }'
```

### 7.5 修改角色

- **接口**：`PUT /system/role`
- **鉴权**：是
- **权限点**：`system:role:edit`
- **请求体**：`SysRoleDTO`（需包含 `id`）

### 7.6 删除角色

- **接口**：`DELETE /system/role/{id}`
- **鉴权**：是
- **权限点**：`system:role:delete`

## 8. 菜单模块（SysMenu）

### 8.1 查询菜单树

- **接口**：`GET /system/menu/tree`
- **鉴权**：是
- **权限点**：`system:menu:query`

### 8.2 查询所有菜单

- **接口**：`GET /system/menu/list`
- **鉴权**：是
- **权限点**：`system:menu:list`

### 8.3 获取菜单详情

- **接口**：`GET /system/menu/{id}`
- **鉴权**：是
- **权限点**：`system:menu:query`

### 8.4 按角色查询菜单 ID 列表

- **接口**：`GET /system/menu/role/{roleId}`
- **鉴权**：是
- **权限点**：`system:menu:query`

### 8.5 新增菜单/权限点

- **接口**：`POST /system/menu`
- **鉴权**：是
- **权限点**：`system:menu:add`
- **请求体**：`SysMenuDTO`

### 8.6 修改菜单

- **接口**：`PUT /system/menu`
- **鉴权**：是
- **权限点**：`system:menu:edit`

### 8.7 删除菜单

- **接口**：`DELETE /system/menu/{id}`
- **鉴权**：是
- **权限点**：`system:menu:delete`
- **业务约束**：存在子菜单时禁止删除（会返回业务错误 `code=500,msg=存在子菜单，不允许删除`）

## 9. RBAC 配置闭环示例（从 0 到可用权限）

下面给出一个“创建按钮权限 → 绑定到角色 → 分配给用户 → 验证接口访问”的最小闭环（你也可以直接运行 `backend/scripts/self-test.sh`）。

1) **登录**：`POST /auth/login` 得到 `token`
2) **新增按钮权限**（menuType=F）：

```bash
curl -X POST "http://localhost:8080/system/menu" \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"menuName":"用户列表","parentId":100,"menuType":"F","permission":"system:user:list","status":1,"visible":1,"sort":1}'
```

3) **新增角色并绑定 menuIds**：`POST /system/role`
4) **新增用户并绑定 roleIds**：`POST /system/user`
5) **用新用户登录后请求 `GET /system/user/page`**：有权限返回 200，无权限返回 403

