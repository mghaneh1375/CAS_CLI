package bogen.studio.cas_client.Validator;

import bogen.studio.cas_client.DTO.SignUpRequest;
import bogen.studio.cas_client.Enum.AuthVia;
import my.common.commonkoochita.Validator.PhoneValidator;
import org.json.JSONObject;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.net.URISyntaxException;
import java.util.Arrays;

import static bogen.studio.cas_client.Utility.StaticValues.VALID_DOMAINS;
import static bogen.studio.cas_client.Utility.Utility.getDomainName;
import static bogen.studio.cas_client.Utility.Utility.isValidMail;

public class SignUpRequestValidator implements ConstraintValidator<ValidatedSignUpRequest, SignUpRequest> {

    @Override
    public void initialize(ValidatedSignUpRequest constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(SignUpRequest dto, ConstraintValidatorContext context) {

        boolean isErrored = false;
        JSONObject errs = new JSONObject();

        String value = dto.getValue();
        AuthVia via = dto.getVia();

        if (via.equals(AuthVia.MAIL) && !isValidMail(value)) {
            errs.put("mail", "ایمیل وارد شده معتبر نمی باشد");
            isErrored = true;
        }
        else if (via.equals(AuthVia.SMS) && !PhoneValidator.isValid(value)) {
            errs.put("phone", "شماره همراه وارد شده معتبر نمی باشد");
            isErrored = true;
        }

        try {

            String domain = getDomainName(dto.getCallback());

            System.out.println(domain);

            if(Arrays.stream(VALID_DOMAINS).noneMatch(domain::contains)) {
                errs.put("callback", "دامنه مورد نظر معتبر نمی باشد");
                isErrored = true;
            }
        } catch (URISyntaxException e) {
            errs.put("callback", "درخواست شما معتبر نمی باشد");
            isErrored = true;
        }

        if(isErrored) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(errs.toString()).addConstraintViolation();
        }

        return !isErrored;

    }
}
