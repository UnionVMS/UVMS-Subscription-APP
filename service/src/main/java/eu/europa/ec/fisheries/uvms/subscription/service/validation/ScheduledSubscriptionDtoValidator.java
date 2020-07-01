package eu.europa.ec.fisheries.uvms.subscription.service.validation;

import static eu.europa.ec.fisheries.uvms.subscription.service.validation.ValidationUtil.require;
import static eu.europa.fisheries.uvms.subscription.model.enums.TriggerType.SCHEDULER;
import static eu.europa.ec.fisheries.uvms.subscription.service.validation.ValidationUtil.requirePropertyNotNullWithMessage;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import eu.europa.ec.fisheries.uvms.subscription.service.dto.SubscriptionDto;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.SubscriptionExecutionDto;

/**
 * Implementation of custom validator for {@link SubscriptionDto} with its execution {@code TriggerType.SCHEDULER}.
 */
public class ScheduledSubscriptionDtoValidator implements ConstraintValidator<ValidScheduledSubscriptionDto, SubscriptionDto> {

	private static final String EXECUTION_PATH = "execution";
	private static final String FREQUENCY_PATH = "frequency";
	private static final String FREQUENCY_UNIT_PATH = "frequencyUnit";

	@Override
	public boolean isValid(SubscriptionDto value, ConstraintValidatorContext context) {
		boolean valid = true;
		if (value != null && value.getExecution() != null && value.getExecution().getTriggerType() == SCHEDULER) {
			valid = requirePropertyNotNullWithMessage(context, value.getExecution().getFrequency(), "Frequency is required", EXECUTION_PATH, FREQUENCY_PATH);
			valid &= requirePropertyNotNullWithMessage(context, value.getExecution().getFrequencyUnit(), "Frequency unit is required", EXECUTION_PATH, FREQUENCY_UNIT_PATH);
			valid &= require(context, "Frequency must be positive for scheduled subscriptions", value)
						.path(EXECUTION_PATH, SubscriptionDto::getExecution)
						.path(FREQUENCY_PATH, SubscriptionExecutionDto::getFrequency)
						.toBe(frequency -> frequency > 0);
		}
		return valid;
	}
}
