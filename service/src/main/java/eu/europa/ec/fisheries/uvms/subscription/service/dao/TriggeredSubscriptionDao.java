package eu.europa.ec.fisheries.uvms.subscription.service.dao;

import eu.europa.ec.fisheries.uvms.subscription.service.domain.TriggeredSubscriptionEntity;

/**
 * DAO for {@link TriggeredSubscriptionEntity}.
 */
public interface TriggeredSubscriptionDao {
	/**
	 * Persist the given entity.
	 *
	 * @param entity The triggered subscription to persist
	 * @return The persisted entity, with id filled
	 */
	TriggeredSubscriptionEntity create(TriggeredSubscriptionEntity entity);

	/**
	 * Find the triggered subscription with the given id.
	 *
	 * @param id The id
	 * @return The found triggered subscription, throws {@code EntityDoesNotExistException} if not found
	 */
	TriggeredSubscriptionEntity getById(Long id);
}
