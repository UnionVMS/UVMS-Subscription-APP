package eu.europa.ec.fisheries.uvms.subscription.service.validation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Validation annotation for SubscriptionDto, validating the subscription has unique name.
 */
@Target(TYPE)
@Retention(RUNTIME)
@Constraint(validatedBy = SubscriptionHasUniqueNameValidator.class)
public @interface SubscriptionHasUniqueName {

    String message() default "Subscription name already used";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}
