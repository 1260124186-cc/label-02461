#!/usr/bin/env bash
# RBAC 自测脚本：覆盖 README 自测流程中全部 curl。需先启动服务：docker compose up -d
# 在项目根目录执行：./backend/scripts/self-test.sh

set -e
# 保证脚本内中文与 JSON 使用 UTF-8，避免 -d 传参乱码
export LANG=C.UTF-8
export LC_ALL=C.UTF-8
BASE_URL="${BASE_URL:-http://localhost:8080}"
RED='\033[0;31m'
GREEN='\033[0;32m'
NC='\033[0m'

# 每轮自测使用唯一 roleKey、用户名，避免重复运行导致冲突
TIMESTAMP=$(date +%s)
ROLE_KEY="test_role_$TIMESTAMP"
TESTUSER_NAME="testuser_$TIMESTAMP"

# 从 JSON 响应中解析 data 字段的数值（用于新增接口返回的 id）
# 支持 "data":123 或含换行/空格的 JSON；兼容 "data":"123" 字符串形式
parse_data_id() {
  local body
  body=$(echo "$1" | tr -d '\n' | tr -d ' ')
  local id
  id=$(echo "$body" | grep -oE '"data":[0-9]+' | sed 's/"data"://')
  [ -n "$id" ] && echo "$id" && return
  echo "$body" | grep -oE '"data":"[0-9]+"' | sed 's/"data":"\([0-9]*\)"/\1/'
}

check_200() {
  local code=$1
  local desc=$2
  if [ "$code" = "200" ]; then
    echo -e "${GREEN}OK${NC}"
    return 0
  fi
  echo -e "${RED}HTTP $code${NC}"
  exit 1
}

echo "=== RBAC 自测脚本 (BASE_URL=$BASE_URL) ==="

# ---------- 1. 登录获取 Token ----------
echo -n "[1] POST /auth/login... "
LOGIN_RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}')
HTTP_CODE=$(echo "$LOGIN_RESP" | tail -n1)
BODY=$(echo "$LOGIN_RESP" | sed '$d')
if [ "$HTTP_CODE" != "200" ]; then
  echo -e "${RED}失败 HTTP $HTTP_CODE${NC}"
  echo "$BODY" | head -c 300
  exit 1
fi
TOKEN=$(echo "$BODY" | grep -o '"token":"[^"]*"' | sed 's/"token":"\([^"]*\)"/\1/')
if [ -z "$TOKEN" ]; then
  echo -e "${RED}未解析到 token${NC}"
  exit 1
fi
echo -e "${GREEN}OK${NC}"

# ---------- 2. 获取当前用户信息 ----------
echo -n "[2] GET /auth/info... "
CODE=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/auth/info" -H "Authorization: Bearer $TOKEN")
check_200 "$CODE" "auth/info"

# ---------- 3. 菜单管理 ----------
echo -n "[3] GET /system/menu/tree... "
CODE=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/system/menu/tree" -H "Authorization: Bearer $TOKEN")
check_200 "$CODE" "menu/tree"

echo -n "[4] POST /system/menu (新增菜单)... "
ADD_MENU_RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/system/menu" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"menuName":"测试菜单","parentId":0,"sort":1,"menuType":"M","status":1,"visible":1}')
ADD_MENU_CODE=$(echo "$ADD_MENU_RESP" | tail -n1)
ADD_MENU_BODY=$(echo "$ADD_MENU_RESP" | sed '$d')
if [ "$ADD_MENU_CODE" != "200" ]; then
  echo -e "${RED}HTTP $ADD_MENU_CODE${NC}"
  exit 1
fi
MENU_ID=$(parse_data_id "$ADD_MENU_BODY")
[ -z "$MENU_ID" ] && { echo -e "${RED}未解析到菜单 id${NC}"; exit 1; }
echo -e "${GREEN}OK (id=$MENU_ID)${NC}"

echo -n "[5] PUT /system/menu (修改菜单)... "
CODE=$(curl -s -o /dev/null -w "%{http_code}" -X PUT "$BASE_URL/system/menu" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"id\":$MENU_ID,\"menuName\":\"测试菜单-改\",\"parentId\":0,\"menuType\":\"M\",\"sort\":1,\"status\":1,\"visible\":1}")
check_200 "$CODE" "menu update"

echo -n "[6] DELETE /system/menu/$MENU_ID... "
CODE=$(curl -s -o /dev/null -w "%{http_code}" -X DELETE "$BASE_URL/system/menu/$MENU_ID" -H "Authorization: Bearer $TOKEN")
check_200 "$CODE" "menu delete"

# ---------- 4. 角色管理 ----------
echo -n "[7] GET /system/role/page?current=1&size=10... "
CODE=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/system/role/page?current=1&size=10" -H "Authorization: Bearer $TOKEN")
check_200 "$CODE" "role/page"

echo -n "[8] POST /system/role (新增角色)... "
ADD_ROLE_RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/system/role" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json; charset=utf-8" \
  -d "{\"roleName\":\"TestRole\",\"roleKey\":\"$ROLE_KEY\",\"sort\":2,\"menuIds\":[]}")
ADD_ROLE_CODE=$(echo "$ADD_ROLE_RESP" | tail -n1)
ADD_ROLE_BODY=$(echo "$ADD_ROLE_RESP" | sed '$d')
if [ "$ADD_ROLE_CODE" != "200" ]; then
  echo -e "${RED}HTTP $ADD_ROLE_CODE${NC}"
  echo "响应: $ADD_ROLE_BODY"
  exit 1
fi
ROLE_ID=$(parse_data_id "$ADD_ROLE_BODY")
[ -z "$ROLE_ID" ] && { echo -e "${RED}未解析到角色 id${NC}"; exit 1; }
echo -e "${GREEN}OK (id=$ROLE_ID)${NC}"

echo -n "[9] PUT /system/role (修改角色)... "
CODE=$(curl -s -o /dev/null -w "%{http_code}" -X PUT "$BASE_URL/system/role" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json; charset=utf-8" \
  -d "{\"id\":$ROLE_ID,\"roleName\":\"TestRole-Updated\",\"roleKey\":\"$ROLE_KEY\",\"sort\":2}")
check_200 "$CODE" "role update"

echo -n "[10] DELETE /system/role/$ROLE_ID... "
CODE=$(curl -s -o /dev/null -w "%{http_code}" -X DELETE "$BASE_URL/system/role/$ROLE_ID" -H "Authorization: Bearer $TOKEN")
check_200 "$CODE" "role delete"

# ---------- 5. 用户管理 ----------
echo -n "[11] GET /system/user/page?current=1&size=10... "
CODE=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/system/user/page?current=1&size=10" -H "Authorization: Bearer $TOKEN")
check_200 "$CODE" "user/page"

echo -n "[12] POST /system/user (新增用户)... "
ADD_USER_RESP=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/system/user" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json; charset=utf-8" \
  -d "{\"username\":\"$TESTUSER_NAME\",\"password\":\"test1234\",\"nickname\":\"TestUser\",\"roleIds\":[]}")
ADD_USER_CODE=$(echo "$ADD_USER_RESP" | tail -n1)
ADD_USER_BODY=$(echo "$ADD_USER_RESP" | sed '$d')
if [ "$ADD_USER_CODE" != "200" ]; then
  echo -e "${RED}HTTP $ADD_USER_CODE${NC}"
  echo "响应: $ADD_USER_BODY"
  exit 1
fi
USER_ID=$(parse_data_id "$ADD_USER_BODY")
[ -z "$USER_ID" ] && { echo -e "${RED}未解析到用户 id${NC}"; exit 1; }
echo -e "${GREEN}OK (id=$USER_ID)${NC}"

echo -n "[13] PUT /system/user (修改用户)... "
CODE=$(curl -s -o /dev/null -w "%{http_code}" -X PUT "$BASE_URL/system/user" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json; charset=utf-8" \
  -d "{\"id\":$USER_ID,\"nickname\":\"TestUser-Updated\"}")
check_200 "$CODE" "user update"

echo -n "[14] PUT /system/user/$USER_ID/resetPassword?newPassword=Newpass123... "
CODE=$(curl -s -o /dev/null -w "%{http_code}" -X PUT "$BASE_URL/system/user/$USER_ID/resetPassword?newPassword=Newpass123" \
  -H "Authorization: Bearer $TOKEN")
check_200 "$CODE" "resetPassword"

# ---------- 6. 权限验证：测试用户无 system:user:list，应返回 403 ----------
echo -n "[15] 权限验证：测试用户登录后 GET /system/user/page 预期 403... "
TESTUSER_LOGIN=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json; charset=utf-8" \
  -d "{\"username\":\"$TESTUSER_NAME\",\"password\":\"Newpass123\"}")
TESTUSER_CODE=$(echo "$TESTUSER_LOGIN" | tail -n1)
TESTUSER_BODY=$(echo "$TESTUSER_LOGIN" | sed '$d')
if [ "$TESTUSER_CODE" != "200" ]; then
  echo -e "${RED}测试用户登录失败 HTTP $TESTUSER_CODE${NC}"
  exit 1
fi
TOKEN_TEST=$(echo "$TESTUSER_BODY" | grep -o '"token":"[^"]*"' | sed 's/"token":"\([^"]*\)"/\1/')
CODE=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/system/user/page?current=1&size=10" -H "Authorization: Bearer $TOKEN_TEST")
if [ "$CODE" = "403" ]; then
  echo -e "${GREEN}OK (403 Forbidden)${NC}"
else
  echo -e "${RED}预期 403，实际 HTTP $CODE${NC}"
  exit 1
fi

# ---------- 7. 删除测试用户（需用 admin 的 token） ----------
echo -n "[16] DELETE /system/user/$USER_ID (admin 删除测试用户)... "
CODE=$(curl -s -o /dev/null -w "%{http_code}" -X DELETE "$BASE_URL/system/user/$USER_ID" -H "Authorization: Bearer $TOKEN")
check_200 "$CODE" "user delete"

echo ""
echo -e "${GREEN}=== 自测全部通过（共 16 项，覆盖 README 全部 curl） ===${NC}"
