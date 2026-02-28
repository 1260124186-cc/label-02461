package com.cqjtjc.common.exception;

import org.springframework.http.HttpStatus;

/**
 * 业务错误码枚举。
 * <p>
 * 统一定义 code（返回给前端的业务码）、默认文案及对应的 HTTP 状态，
 * 便于全局异常处理与文档维护。新增业务异常时请在此补充并让 Service 使用。
 * </p>
 */
public enum ErrorCode {

    // ---------- 认证 4xx ----------
    /** 未登录或 Token 无效 */
    UNAUTHORIZED(401, "未登录", HttpStatus.UNAUTHORIZED),
    /** 用户已被禁用，禁止登录 */
    USER_DISABLED(403, "用户已被禁用", HttpStatus.FORBIDDEN),

    // ---------- 用户 4xx ----------
    /** 用户不存在 */
    USER_NOT_FOUND(404, "用户不存在", HttpStatus.NOT_FOUND),
    /** 用户名已存在（新增/修改时唯一性冲突） */
    USERNAME_EXISTS(409, "用户名已存在", HttpStatus.CONFLICT),
    /** 密码未设置或为空 */
    PASSWORD_REQUIRED(400, "请设置密码，且需满足：长度 8~128 位，包含字母和数字", HttpStatus.BAD_REQUEST),
    /** 密码长度不足 */
    PASSWORD_TOO_SHORT(400, "密码长度至少 8 位", HttpStatus.BAD_REQUEST),
    /** 密码长度超限 */
    PASSWORD_TOO_LONG(400, "密码长度不能超过 128 位", HttpStatus.BAD_REQUEST),
    /** 密码须包含字母 */
    PASSWORD_NO_LETTER(400, "密码须包含至少一个字母", HttpStatus.BAD_REQUEST),
    /** 密码须包含数字 */
    PASSWORD_NO_DIGIT(400, "密码须包含至少一个数字", HttpStatus.BAD_REQUEST),

    // ---------- 角色 4xx ----------
    /** 角色不存在 */
    ROLE_NOT_FOUND(404, "角色不存在", HttpStatus.NOT_FOUND),
    /** 角色标识已存在（唯一性冲突） */
    ROLE_KEY_EXISTS(409, "角色标识已存在", HttpStatus.CONFLICT),

    // ---------- 菜单 4xx ----------
    /** 菜单不存在 */
    MENU_NOT_FOUND(404, "菜单不存在", HttpStatus.NOT_FOUND),
    /** 存在子菜单，不允许删除 */
    MENU_HAS_CHILDREN(409, "存在子菜单，不允许删除", HttpStatus.CONFLICT),

    // ---------- 通用 5xx ----------
    /** 未归类的业务错误，默认 500 */
    BUSINESS_ERROR(500, "操作失败", HttpStatus.INTERNAL_SERVER_ERROR);

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(int code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    /**
     * 根据业务码推导 HTTP 状态（用于未使用枚举构造的 BusinessException）。
     */
    public static HttpStatus httpStatusOf(int code) {
        if (code >= 400 && code < 500) {
            switch (code) {
                case 400:
                    return HttpStatus.BAD_REQUEST;
                case 401:
                    return HttpStatus.UNAUTHORIZED;
                case 403:
                    return HttpStatus.FORBIDDEN;
                case 404:
                    return HttpStatus.NOT_FOUND;
                case 409:
                    return HttpStatus.CONFLICT;
                default:
                    return HttpStatus.BAD_REQUEST;
            }
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
