package eu.europa.ec.fisheries.uvms.subscription.service.dao;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.search.SubscriptionListQuery;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.search.SubscriptionSearchCriteria;
import eu.europa.fisheries.uvms.subscription.model.enums.ColumnType;
import eu.europa.fisheries.uvms.subscription.model.enums.DirectionType;

public interface SubscriptionDao {
	List<SubscriptionEntity> listSubscriptions(@Valid @NotNull SubscriptionListQuery subscriptionListParams);

	Long count(@Valid @NotNull SubscriptionSearchCriteria criteria);

	Boolean valueExists(@NotNull String name);

	SubscriptionEntity createEntity(SubscriptionEntity entity);

	SubscriptionEntity findById(Long id);

	SubscriptionEntity update(SubscriptionEntity entity);

	void delete(Long id);
}
