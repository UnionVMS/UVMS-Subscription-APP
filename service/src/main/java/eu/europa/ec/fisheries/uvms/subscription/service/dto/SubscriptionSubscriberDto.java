package eu.europa.ec.fisheries.uvms.subscription.service.dto;

import java.io.Serializable;
import lombok.Data;

/**
 * A subscriber of a subscription.
 */
@Data
public class SubscriptionSubscriberDto implements Serializable {

	private Long organisationId;

    private Long endpointId;

    private Long channelId;
}
