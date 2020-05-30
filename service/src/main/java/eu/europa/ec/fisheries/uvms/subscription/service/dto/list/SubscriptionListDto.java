package eu.europa.ec.fisheries.uvms.subscription.service.dto.list;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import eu.europa.ec.fisheries.uvms.commons.domain.DateRange;
import lombok.Data;

/**
 * Representation of a subscription to be used in lists.
 */
@Data
public class SubscriptionListDto {
	private Long id;
	private String name;
	private String description;
	private boolean active;
	@JsonUnwrapped
	private DateRange validityPeriod;
	private String organisationName;
	private String endpointName;
	private String channelName;
	private String messageType;
}
