package eu.europa.ec.fisheries.uvms.subscription.service.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;

import eu.europa.fisheries.uvms.subscription.model.enums.TriggerType;
import lombok.Data;

/**
 * Encapsulates instructions for how and when to execute a subscription.
 */
@Data
public class SubscriptionExecutionDto {

    private TriggerType triggerType;

    @Min(0)
    private Integer frequency;

	private Boolean immediate;

    @Pattern(regexp = "^(?:23|22|21|20|[01][0-9]):[0-5][0-9]$")
    private String timeExpression;
}
