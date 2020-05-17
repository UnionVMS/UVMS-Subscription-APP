package eu.europa.ec.fisheries.uvms.subscription.service.validation;

import static eu.europa.ec.fisheries.uvms.subscription.service.validation.ValidationUtil.requirePropertyNotNullWithMessage;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import eu.europa.ec.fisheries.uvms.subscription.service.dto.SubscriptionExecutionDto;
import eu.europa.fisheries.uvms.subscription.model.enums.TriggerType;

/**
 * Implementation of custom validator for SubscriptionExecutionDto.
 */
public class SubscriptionExecutionDtoValidator implements ConstraintValidator<ValidSubscriptionExecutionDto, SubscriptionExecutionDto> {

    @Override
    public boolean isValid(SubscriptionExecutionDto execution, ConstraintValidatorContext context) {
        boolean valid = true;
        if (TriggerType.SCHEDULER.equals(execution.getTriggerType())){
            valid = requirePropertyNotNullWithMessage(context, execution.getFrequency(), "frequency", "Frequency is required");
            valid &= requirePropertyNotNullWithMessage(context, execution.getFrequencyUnit(), "frequencyUnit", "Frequency unit is required");
            valid &= requirePropertyNotNullWithMessage(context, execution.getTimeExpression(), "timeExpression","Time expression is required");
        }
        return valid;
    }
}
