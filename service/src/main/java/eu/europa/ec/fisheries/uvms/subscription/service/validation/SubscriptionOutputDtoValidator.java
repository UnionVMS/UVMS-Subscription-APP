package eu.europa.ec.fisheries.uvms.subscription.service.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import eu.europa.ec.fisheries.uvms.subscription.service.dto.SubscriptionOutputDto;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.SubscriptionSubscriberDTO;
import eu.europa.fisheries.uvms.subscription.model.enums.OutgoingMessageType;

/**
 * Implementation of custom validator for SubscriptionOutputDto.
 */

public class SubscriptionOutputDtoValidator implements ConstraintValidator<ValidSubscriptionOutputDto, SubscriptionOutputDto> {

    @Override
    public boolean isValid(SubscriptionOutputDto output, ConstraintValidatorContext context) {
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
        return true;
    }
}
