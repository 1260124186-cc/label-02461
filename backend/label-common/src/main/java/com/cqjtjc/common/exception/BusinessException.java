package com.cqjtjc.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 业务异常，用于可预期的业务规则校验失败（如资源不存在、唯一性冲突、参数不合法等）。
 * <p>
 * 推荐使用 {@link ErrorCode} 构造，便于统一错误码与 HTTP 状态；也保留按 code+message 构造以兼容已有代码。
 * </p>
 *
 * @see ErrorCode
 * @see GlobalExceptionHandler
 */
@Getter
public class BusinessException extends RuntimeException {

    private final int code;
    private final ErrorCode errorCode;

    /**
     * 使用错误码枚举构造，返回枚举中的 code 与默认 message。
     */
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.errorCode = errorCode;
    }

    /**
     * 使用错误码枚举构造，并覆盖默认 message（如需要携带动态参数时）。
     */
    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.code = errorCode.getCode();
        this.errorCode = errorCode;
    }

    /**
     * 按业务码与文案构造（不绑定枚举），用于临时或未纳入 ErrorCode 的场景。
     */
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
        this.errorCode = null;
    }

    /**
     * 仅文案构造，业务码固定为 500。建议新逻辑优先使用 {@link #BusinessException(ErrorCode)}。
     */
    public BusinessException(String message) {
        super(message);
        this.code = 500;
        this.errorCode = null;
    }

    /**
     * 便于全局异常处理根据 code 推导 HTTP 状态（用于设置响应状态码）。
     */
    public HttpStatus getHttpStatus() {
        return errorCode != null ? errorCode.getHttpStatus() : ErrorCode.httpStatusOf(code);
    }
}
