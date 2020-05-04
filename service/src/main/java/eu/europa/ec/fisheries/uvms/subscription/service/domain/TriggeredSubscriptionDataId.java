package eu.europa.ec.fisheries.uvms.subscription.service.domain;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Primary key for the {@link TriggeredSubscriptionDataEntity}.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class TriggeredSubscriptionDataId implements Serializable {
	private Long triggeredSubscription;
	private String key;
}
