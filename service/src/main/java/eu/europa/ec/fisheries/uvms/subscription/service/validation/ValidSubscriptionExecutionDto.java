package eu.europa.ec.fisheries.uvms.subscription.service.validation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
/**
 * Validation annotation for SubscriptionExecutionDto.
 */
@Target(TYPE)
@Retention(RUNTIME)
@Constraint(validatedBy = SubscriptionExecutionDtoValidator.class)
public @interface ValidSubscriptionExecutionDto {

    String message() default "Subscription execution is not valid";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}
