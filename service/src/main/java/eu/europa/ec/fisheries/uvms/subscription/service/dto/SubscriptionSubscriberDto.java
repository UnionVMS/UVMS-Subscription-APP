package eu.europa.ec.fisheries.uvms.subscription.service.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A subscriber of a subscription.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubscriptionSubscriberDto implements Serializable {

	private Long organisationId;

    private Long endpointId;

    private Long channelId;
}
