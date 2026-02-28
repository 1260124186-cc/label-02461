package com.cqjtjc.system.util;

import com.cqjtjc.common.exception.BusinessException;
import com.cqjtjc.common.exception.ErrorCode;
import org.springframework.util.StringUtils;

import java.util.regex.Pattern;

/**
 * 密码强度校验：长度、复杂度，避免弱密码与默认密码。
 */
public final class PasswordStrengthValidator {

    /** 最小长度 */
    public static final int MIN_LENGTH = 8;
    /** 最大长度，防止过长输入 */
    public static final int MAX_LENGTH = 128;
    /** 至少包含一个字母 */
    private static final Pattern HAS_LETTER = Pattern.compile("[a-zA-Z]");
    /** 至少包含一个数字 */
    private static final Pattern HAS_DIGIT = Pattern.compile("[0-9]");

    private PasswordStrengthValidator() {
    }

    /**
     * 校验密码强度，不通过时抛出 BusinessException。
     * 规则：长度 8~128，至少包含字母和数字。
     *
     * @param password 明文密码，可为 null 或空
     * @throws BusinessException 未提供密码或强度不足时
     */
    public static void validate(String password) {
        if (!StringUtils.hasText(password)) {
            throw new BusinessException(ErrorCode.PASSWORD_REQUIRED);
        }
        if (password.length() < MIN_LENGTH) {
            throw new BusinessException(ErrorCode.PASSWORD_TOO_SHORT);
        }
        if (password.length() > MAX_LENGTH) {
            throw new BusinessException(ErrorCode.PASSWORD_TOO_LONG);
        }
        if (!HAS_LETTER.matcher(password).find()) {
            throw new BusinessException(ErrorCode.PASSWORD_NO_LETTER);
        }
        if (!HAS_DIGIT.matcher(password).find()) {
            throw new BusinessException(ErrorCode.PASSWORD_NO_DIGIT);
        }
    }
}
