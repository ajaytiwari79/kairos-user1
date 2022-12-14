package com.kairos.scheduler.utils.custom_annotation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class NotNullOrEmptyValidator implements ConstraintValidator<NotNullOrEmpty,String> {
    @Override
    public void initialize(NotNullOrEmpty constraintAnnotation) {
        //This is Override method
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {


        if (value == null) {
            return false;
        }
        if (value.length() == 0) {
            return false;
        }

        boolean isAllWhitespace = value.matches("^\\s*$");
        return !isAllWhitespace;
    }
}
