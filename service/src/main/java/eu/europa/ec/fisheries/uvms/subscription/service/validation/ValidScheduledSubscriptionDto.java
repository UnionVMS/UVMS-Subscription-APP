package eu.europa.ec.fisheries.uvms.subscription.service.validation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Validation annotation for scheduled (execution {@code TriggerType.SCHEDULER}) SubscriptionDto.
 */
@Target(TYPE)
@Retention(RUNTIME)
@Constraint(validatedBy = ScheduledSubscriptionDtoValidator.class)
public @interface ValidScheduledSubscriptionDto {

	String message() default "Scheduled Subscription is not valid";

	Class<?>[] groups() default { };

	Class<? extends Payload>[] payload() default { };
}
