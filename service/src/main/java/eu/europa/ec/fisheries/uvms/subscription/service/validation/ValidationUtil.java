package eu.europa.ec.fisheries.uvms.subscription.service.validation;

import javax.validation.ConstraintValidatorContext;

/**
 * Utility class implementing methods shared by custom validators.
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

    static boolean requirePropertyNotNullWithMessage(ConstraintValidatorContext context, Object value, String message, String... path) {
        if(value == null) {
            addViolationMessageToPath(context, message, path);
            return false;
        }
        return true;
    }

    static boolean requirePropertyNullWithMessage(ConstraintValidatorContext context, Object value, String message, String... path) {
        if(value != null) {
            addViolationMessageToPath(context, message, path);
            return false;
        }
        return true;
    }

    static void addViolationMessageToPath(ConstraintValidatorContext context, String message, String[] path) {
        ConstraintValidatorContext.ConstraintViolationBuilder violationBuilder = context.buildConstraintViolationWithTemplate(message);
        ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext nodeBuilder = null;
        for (String s : path) {
            nodeBuilder = violationBuilder.addPropertyNode(s);
        }
        if (nodeBuilder != null) {
            nodeBuilder.addConstraintViolation();
        }
    }
}
