package eu.europa.ec.fisheries.uvms.subscription.service.dao;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.search.SubscriptionListQuery;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.search.SubscriptionSearchCriteria;

public interface SubscriptionDao {
	List<SubscriptionEntity> listSubscriptions(@Valid @NotNull SubscriptionListQuery subscriptionListParams);

	Long count(@Valid @NotNull SubscriptionSearchCriteria criteria);

	SubscriptionEntity findSubscriptionByName(@NotNull String name);

	SubscriptionEntity createEntity(SubscriptionEntity entity);

	SubscriptionEntity findById(Long id);

	SubscriptionEntity update(SubscriptionEntity entity);

	void delete(Long id);
}
