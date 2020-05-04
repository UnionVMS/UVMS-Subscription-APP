package eu.europa.ec.fisheries.uvms.subscription.service.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;

import eu.europa.ec.fisheries.uvms.subscription.service.validation.ValidSubscriptionExecutionDto;
import eu.europa.fisheries.uvms.subscription.model.enums.SubscriptionTimeUnit;
import eu.europa.fisheries.uvms.subscription.model.enums.TriggerType;
import lombok.Data;

/**
 * Encapsulates instructions for how and when to execute a subscription.
 */
@Data
@ValidSubscriptionExecutionDto
public class SubscriptionExecutionDto {

    private TriggerType triggerType;

    @Min(0)
    private Integer frequency;

    private SubscriptionTimeUnit frequencyUnit;

	private Boolean immediate;

    @Pattern(regexp = "^(?:23|22|21|20|[01][0-9]):[0-5][0-9]$")
    private String timeExpression;
}
