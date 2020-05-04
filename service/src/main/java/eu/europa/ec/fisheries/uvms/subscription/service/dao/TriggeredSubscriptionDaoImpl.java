package eu.europa.ec.fisheries.uvms.subscription.service.dao;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import eu.europa.ec.fisheries.uvms.subscription.service.domain.TriggeredSubscriptionEntity;
import eu.europa.fisheries.uvms.subscription.model.exceptions.EntityDoesNotExistException;

/**
 * Implementation of {@link TriggeredSubscriptionDao} using JPA.
 */
@ApplicationScoped
class TriggeredSubscriptionDaoImpl implements TriggeredSubscriptionDao {

	private EntityManager em;

	/**
	 * Constructor for injection.
	 *
	 * @param em The entity manager
	 */
	@Inject
	public TriggeredSubscriptionDaoImpl(EntityManager em) {
		this.em = em;
	}

	/**
	 * Constructor for frameworks.
	 */
	TriggeredSubscriptionDaoImpl() {
		// NOOP
	}

	@Override
	public TriggeredSubscriptionEntity create(TriggeredSubscriptionEntity entity) {
		em.persist(entity);
		return entity;
	}

	@Override
	public TriggeredSubscriptionEntity getById(Long id) {
		TriggeredSubscriptionEntity result = em.find(TriggeredSubscriptionEntity.class, id);
		if (result == null) {
			throw new EntityDoesNotExistException("TriggeredSubscription with id " + id);
		}
		return result;
	}
}
