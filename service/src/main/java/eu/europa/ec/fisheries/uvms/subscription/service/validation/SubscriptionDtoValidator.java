package eu.europa.ec.fisheries.uvms.subscription.service.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import eu.europa.ec.fisheries.uvms.subscription.service.dto.SubscriptionDto;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.SubscriptionExecutionDto;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.SubscriptionOutputDto;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.SubscriptionSubscriberDTO;
import eu.europa.fisheries.uvms.subscription.model.enums.OutgoingMessageType;
import eu.europa.fisheries.uvms.subscription.model.enums.TriggerType;

public class SubscriptionDtoValidator implements ConstraintValidator<ValidSubscriptionDto, SubscriptionDto> {

    @Override
    public boolean isValid(SubscriptionDto subscription, ConstraintValidatorContext context) {
        if(subscription.getName() == null || subscription.getName().isEmpty() || subscription.getActive() == null){
            return false;
        }

        SubscriptionOutputDto output = subscription.getOutput();
        if(output == null || output.getMessageType() == null){
            return false;
        }

        if(output.getMessageType() != OutgoingMessageType.NONE){
            SubscriptionSubscriberDTO subscriber = output.getSubscriber();
            if(subscriber == null || subscriber.getChannelId() == null || subscriber.getEndpointId() == null || subscriber.getOrganisationId() == null ){
                return false;
            }
            if(output.getMessageType() == OutgoingMessageType.FA_QUERY || output.getMessageType() == OutgoingMessageType.FA_REPORT){
                if (output.getLogbook() == null || output.getConsolidated() == null || output.getHistory() == null){
                    return false;
                }
            }
        }

        SubscriptionExecutionDto execution = subscription.getExecution();
        if (TriggerType.SCHEDULER.equals(execution.getTriggerType())){
            if(execution.getFrequency() == null || execution.getFrequency() < 0 || execution.getTimeExpression() == null || execution.getTimeExpression().isEmpty()){
                return false;
            }

        }
        return true;
    }
}
