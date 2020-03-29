package eu.europa.ec.fisheries.uvms.subscription.service.domain;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import lombok.Data;

/**
 * A subscriber of a subscription.
 */
@Embeddable
@Data
public class SubscriptionSubscriber {
	@Column(name = "organisation_id")
	private Long organisationId;

	@Column(name = "endpoint_id")
    private Long endpointId;

	@Column(name = "channel_id")
    private Long channelId;
}
