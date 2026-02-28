package com.cqjtjc.common.exception;

import com.cqjtjc.common.result.R;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理：将各类异常统一封装为 {@link R} 并设置合适的 HTTP 状态码。
 * <p>
 * 处理顺序：业务异常 → 安全/校验异常 → 兜底异常。业务异常推荐使用 {@link ErrorCode}，
 * 以便返回正确的 HTTP 状态（如 404、409）并便于日志分级（4xx 打 warn，5xx 打 error）。
 * </p>
 *
 * @see BusinessException
 * @see ErrorCode
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 业务异常：按异常中的 code 与 message 返回，HTTP 状态由 {@link ErrorCode} 或 code 推导。
     * 4xx 打 warn、5xx 打 error，避免正常业务校验刷屏 error 日志。
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<R<Void>> handleBusinessException(BusinessException e) {
        HttpStatus status = e.getHttpStatus();
        if (e.getCode() >= 500) {
            log.error("业务异常 [code={}]: {}", e.getCode(), e.getMessage());
        } else {
            log.warn("业务异常 [code={}]: {}", e.getCode(), e.getMessage());
        }
        return ResponseEntity.status(status).body(R.fail(e.getCode(), e.getMessage()));
    }

    /** 账号或密码错误（Spring Security 认证失败），固定 401。 */
    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public R<Void> handleBadCredentialsException(BadCredentialsException e) {
        log.warn("认证失败: 用户名或密码错误");
        return R.fail(401, "用户名或密码错误");
    }

    /** 无权限访问（已认证但权限不足），固定 403。 */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public R<Void> handleAccessDeniedException(AccessDeniedException e) {
        log.warn("权限不足: {}", e.getMessage());
        return R.fail(403, "没有权限访问");
    }

    /** 请求体校验失败（@Valid 触发的字段错误），固定 400。 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R<Void> handleValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("参数校验失败");
        log.warn("参数校验失败: {}", message);
        return R.fail(400, message);
    }

    /** 参数绑定失败（如类型不匹配、必填缺失），固定 400。 */
    @ExceptionHandler(BindException.class)
    public R<Void> handleBindException(BindException e) {
        String message = e.getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("参数绑定失败");
        log.warn("参数绑定失败: {}", message);
        return R.fail(400, message);
    }

    /** 方法参数级校验失败（@Validated 在参数上），固定 400。 */
    @ExceptionHandler(ConstraintViolationException.class)
    public R<Void> handleConstraintViolationException(ConstraintViolationException e) {
        log.warn("参数校验失败: {}", e.getMessage());
        return R.fail(400, e.getMessage());
    }

    /** 未分类异常，统一 500，避免敏感信息泄露。 */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public R<String> handleException(Exception e) {
        log.error("系统异常", e);
        String msg = e.getMessage() != null ? e.getMessage() : "系统内部错误";
        return R.fail(500, msg);
    }
}
