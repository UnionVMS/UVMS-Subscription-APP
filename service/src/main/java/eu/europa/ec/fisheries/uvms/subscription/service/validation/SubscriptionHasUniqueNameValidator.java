package eu.europa.ec.fisheries.uvms.subscription.service.validation;

import javax.inject.Inject;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import eu.europa.ec.fisheries.uvms.subscription.service.bean.SubscriptionService;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.SubscriptionDto;

/**
 * Implementation of custom validator for SubscriptionExecutionDto.
 */

public class SubscriptionHasUniqueNameValidator implements ConstraintValidator<SubscriptionHasUniqueName, SubscriptionDto> {

    @Inject
    private SubscriptionService subscriptionService;


    @Override
    public boolean isValid(SubscriptionDto subscription, ConstraintValidatorContext context) {
        return subscriptionService.checkNameAvailability(subscription.getName(), subscription.getId());
    }
}
