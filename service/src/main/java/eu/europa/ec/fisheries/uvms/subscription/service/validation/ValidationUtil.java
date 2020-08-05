package eu.europa.ec.fisheries.uvms.subscription.service.validation;

import javax.validation.ConstraintValidatorContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

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
            if(nodeBuilder == null) {
                nodeBuilder = violationBuilder.addPropertyNode(s);
            } else {
                nodeBuilder = nodeBuilder.addPropertyNode(s);
            }
        }
        if (nodeBuilder != null) {
            nodeBuilder.addConstraintViolation();
        }
    }

    static <R> ValidationUtilContext<R> require(ConstraintValidatorContext context, String message, R value) {
        return new ValidationUtilContext<>(context, message, value);
    }

    class ValidationUtilContext<V> {
        private final ConstraintValidatorContext context;
        private final String message;
        private final Optional<V> value;
        private final List<String> path;

        private ValidationUtilContext(ConstraintValidatorContext context, String message, V value) {
            this.context = context;
            this.message = message;
            this.value = Optional.ofNullable(value);
            path = new ArrayList<>();
        }

        private ValidationUtilContext(ConstraintValidatorContext context, String message, List<String> path, Optional<V> value) {
            this.context = context;
            this.message = message;
            this.path = path;
            this.value = value;
        }

        public <R> ValidationUtilContext<R> path(String propertyName, Function<V,R> getNext) {
            Objects.requireNonNull(propertyName);
            List<String> newPath = new ArrayList<>(this.path);
            newPath.add(propertyName);
            return new ValidationUtilContext<>(context, message, newPath, value.map(getNext));
        }

        public boolean toBe(Predicate<V> condition) {
            return value.map(v -> {
                boolean isValid = condition.test(v);
                if (!isValid) {
                    addViolationMessageToPath(context, message, path.toArray(new String[]{}));
                }
                return isValid;
            }).orElse(Boolean.TRUE);
        }
    }
}
