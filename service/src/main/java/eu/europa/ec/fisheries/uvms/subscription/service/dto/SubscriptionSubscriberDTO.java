package eu.europa.ec.fisheries.uvms.subscription.service.dto;

import javax.validation.constraints.NotNull;

import lombok.Data;

/**
 * A subscriber of a subscription.
 */
@Data
public class SubscriptionSubscriberDTO {

    @NotNull
	private Long organisationId;

    @NotNull
    private Long endpointId;

    @NotNull
    private Long channelId;
}
