package eu.europa.ec.fisheries.uvms.subscription.service.validation;

import static eu.europa.ec.fisheries.uvms.subscription.service.validation.ValidationUtil.requirePropertyNotNullWithMessage;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import eu.europa.ec.fisheries.uvms.subscription.service.dto.SubscriptionOutputDto;
import eu.europa.fisheries.uvms.subscription.model.enums.OutgoingMessageType;

/**
 * Implementation of custom validator for SubscriptionOutputDto.
 */
public class SubscriptionOutputDtoValidator implements ConstraintValidator<ValidSubscriptionOutputDto, SubscriptionOutputDto> {

    @Override
    public boolean isValid(SubscriptionOutputDto output, ConstraintValidatorContext context) {
        boolean valid = true;
        if (output != null && (output.getMessageType() == OutgoingMessageType.FA_QUERY || output.getMessageType() == OutgoingMessageType.FA_REPORT)) {
            valid = requirePropertyNotNullWithMessage(context, output.getLogbook(), "logbook", "Logbook is required")
                    && requirePropertyNotNullWithMessage(context, output.getConsolidated(), "consolidated", "Consolidated is required")
                    && requirePropertyNotNullWithMessage(context, output.getHistory(), "history", "History is required");
        }
        return valid;
    }
}
