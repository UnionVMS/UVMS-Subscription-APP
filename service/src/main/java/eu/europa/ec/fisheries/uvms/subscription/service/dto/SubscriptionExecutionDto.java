package eu.europa.ec.fisheries.uvms.subscription.service.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import eu.europa.ec.fisheries.uvms.subscription.service.domain.TriggerType;
import lombok.Data;

/**
 * Encapsulates instructions for how and when to execute a subscription.
 */
@Data
public class SubscriptionExecutionDto {

    @NotNull
    private TriggerType triggerType;

	@Min(0)
    private Integer frequency;

	private Boolean immediate;

	private String timeExpression;
}
