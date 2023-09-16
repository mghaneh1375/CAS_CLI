package bogen.studio.cas_client.Validator;

import bogen.studio.cas_client.DTO.LoginRequest;
import org.json.JSONObject;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class LoginRequestValidator implements ConstraintValidator<ValidatedLoginRequest, LoginRequest> {

    @Override
    public void initialize(ValidatedLoginRequest constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(LoginRequest dto, ConstraintValidatorContext context) {

        boolean isErrored = false;
        JSONObject errs = new JSONObject();



        if(isErrored) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(errs.toString()).addConstraintViolation();
        }

        return !isErrored;

    }
}
