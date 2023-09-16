package bogen.studio.cas_client.Validator;


import javax.validation.Constraint;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Constraint(validatedBy = LoginRequestValidator.class)
@Target({TYPE})
@Retention(RUNTIME)

public @interface ValidatedLoginRequest {

    String message() default "";

    Class[] groups() default {};

    Class[] payload() default {};

}
