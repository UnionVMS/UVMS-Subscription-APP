package eu.europa.ec.fisheries.uvms.subscription.service.trigger;

import java.util.Set;
import java.util.stream.Collectors;

import eu.europa.ec.fisheries.uvms.subscription.service.domain.TriggeredSubscriptionDataEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.TriggeredSubscriptionEntity;

/**
 * Some useful utilities for triggered subscription data.
 */
public class TriggeredSubscriptionDataUtil {
	public static final String KEY_CONNECT_ID = "connectId";
	public static final String KEY_OCCURRENCE = "occurrence";

	/**
	 * Extract only the {@code connectId} data.
	 *
	 * @param entity The entity whose data to filter
	 * @return A set containing only the {@code connectId} {@code TriggeredSubscriptionDataEntity}
	 */
	public static Set<TriggeredSubscriptionDataEntity> extractConnectId(TriggeredSubscriptionEntity entity) {
		return entity.getData().stream()
				.filter(d -> KEY_CONNECT_ID.equals(d.getKey()))
				.collect(Collectors.toSet());
	}
}
