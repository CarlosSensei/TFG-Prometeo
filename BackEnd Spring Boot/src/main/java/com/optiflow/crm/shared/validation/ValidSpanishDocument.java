package com.optiflow.crm.shared.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = SpanishDocumentValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidSpanishDocument {

    String message() default "El DNI/NIE no es válido";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}