package eu.europa.ec.fisheries.uvms.subscription.service.validation;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target(PARAMETER)
@Retention(RUNTIME)
@Constraint(validatedBy = SubscriptionDtoValidator.class)
public @interface ValidSubscriptionDto {

    String message() default "Subscription is not valid";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}
