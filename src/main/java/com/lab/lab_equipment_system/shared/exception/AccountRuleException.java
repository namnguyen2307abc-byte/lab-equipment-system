package com.lab.lab_equipment_system.shared.exception;

/** Vi phạm quy tắc nghiệp vụ khi quản lý tài khoản (trùng tên đăng nhập, khóa chính mình...). */
public class AccountRuleException extends RuntimeException {
    public AccountRuleException(String message) { super(message); }
}
