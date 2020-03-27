package eu.europa.ec.fisheries.uvms.subscription.service.dao;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.search.ColumnType;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.search.DirectionType;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.search.SubscriptionListQuery;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.search.SubscriptionSearchCriteria;

public interface SubscriptionDao {
	List<SubscriptionEntity> listSubscriptions(@Valid @NotNull Map<String, Object> queryParameters, @Valid @NotNull Map<ColumnType, DirectionType> orderBy, @Valid @NotNull Integer firstResult, @Valid @NotNull Integer maxResult);
	List<SubscriptionEntity> listSubscriptions(@Valid @NotNull SubscriptionListQuery subscriptionListParams);

	Long count(@Valid @NotNull SubscriptionSearchCriteria criteria);

	SubscriptionEntity byName(@Valid @NotNull Map<String, Object> queryParameters);

	SubscriptionEntity createEntity(SubscriptionEntity entity);

	SubscriptionEntity findById(Long id);

	SubscriptionEntity update(SubscriptionEntity entity);

	void delete(Long id);
}
