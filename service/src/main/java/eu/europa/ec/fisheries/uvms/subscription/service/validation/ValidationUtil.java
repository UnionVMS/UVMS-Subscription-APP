package eu.europa.ec.fisheries.uvms.subscription.service.validation;

import javax.validation.ConstraintValidatorContext;

/**
 * Utility class implementing method shared by custom validators.
 */
public interface ValidationUtil {
    static boolean requirePropertyNotNullWithMessage(ConstraintValidatorContext context, Object value, String propertyName, String message) {
        if(value == null) {
            context.buildConstraintViolationWithTemplate(message)
                    .addPropertyNode(propertyName)
                    .addConstraintViolation();
            return false;
        }
        return true;
    }
}
