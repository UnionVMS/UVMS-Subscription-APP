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
	public static final String KEY_MOVEMENT_GUID_PREFIX = "movementGuidIndex_";
	public static final String KEY_REPORT_ID_PREFIX = "reportId_";

	@SuppressWarnings("unused")
	private TriggeredSubscriptionDataUtil() {
		throw new IllegalStateException("Utility class");
	}

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

	/**
	 * Extract the {@code connectId} and data containing key movements guids.
	 *
	 * @param entity The entity whose data to filter
	 * @return A set containing the {@code connectId} and movement guid {@code TriggeredSubscriptionDataEntity}
	 */
	public static Set<TriggeredSubscriptionDataEntity> extractConnectIdAndMovementGuid(TriggeredSubscriptionEntity entity) {
		return entity.getData().stream()
				.filter(d -> TriggeredSubscriptionDataUtil.KEY_CONNECT_ID.equals(d.getKey()) || d.getKey().startsWith(KEY_MOVEMENT_GUID_PREFIX))
				.collect(Collectors.toSet());
	}
}
