package com.lab.lab_equipment_system.shared.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = Utf8ByteLengthValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Utf8ByteLength {
    String message() default "Mật khẩu tối đa 72 byte UTF-8; ký tự tiếng Việt có thể chiếm nhiều byte.";
    int max() default 72;
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
