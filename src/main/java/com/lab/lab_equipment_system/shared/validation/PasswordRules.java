package com.lab.lab_equipment_system.shared.validation;

import com.lab.lab_equipment_system.shared.exception.AccountRuleException;
import java.nio.charset.StandardCharsets;

public final class PasswordRules {
    private PasswordRules() {}
    public static void requireValid(String password) {
        if (password == null || password.isBlank() || password.length() < 6)
            throw new AccountRuleException("Mật khẩu phải có ít nhất 6 ký tự.");
        if (password.getBytes(StandardCharsets.UTF_8).length > 72)
            throw new AccountRuleException("Mật khẩu tối đa 72 byte UTF-8; ký tự tiếng Việt có thể chiếm nhiều byte.");
    }
}
