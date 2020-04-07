package eu.europa.ec.fisheries.uvms.subscription.service.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import eu.europa.ec.fisheries.uvms.subscription.service.dto.SubscriptionDto;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.SubscriptionExecutionDto;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.SubscriptionOutputDto;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.SubscriptionSubscriberDTO;
import eu.europa.fisheries.uvms.subscription.model.enums.OutgoingMessageType;
import eu.europa.fisheries.uvms.subscription.model.enums.TriggerType;

/**
 * Implementation of custom validator for SubscriptionExecutionDto.
 */

public class SubscriptionExecutionDtoValidator implements ConstraintValidator<ValidSubscriptionExecutionDto, SubscriptionExecutionDto> {

    @Override
    public boolean isValid(SubscriptionExecutionDto execution, ConstraintValidatorContext context) {
        if (TriggerType.SCHEDULER.equals(execution.getTriggerType())){
            if(execution.getFrequency() == null || execution.getFrequency() < 0 || execution.getTimeExpression() == null || execution.getTimeExpression().isEmpty()){
                return false;
            }

        }
        return true;
    }
}
