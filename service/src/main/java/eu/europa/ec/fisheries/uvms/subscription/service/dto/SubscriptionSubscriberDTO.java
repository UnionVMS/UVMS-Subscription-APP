package eu.europa.ec.fisheries.uvms.subscription.service.dto;

import lombok.Data;

/**
 * A subscriber of a subscription.
 */
@Data
public class SubscriptionSubscriberDTO {

	private Long organisationId;

    private Long endpointId;

    private Long channelId;
}
