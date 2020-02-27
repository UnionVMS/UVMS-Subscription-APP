package eu.europa.ec.fisheries.uvms.subscription.service.dao;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

import eu.europa.ec.fisheries.uvms.commons.service.dao.DAO;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.ColumnType;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.DirectionType;

public interface SubscriptionDao extends DAO<SubscriptionEntity> {
	List<SubscriptionEntity> listSubscriptions(@Valid @NotNull Map<String, Object> queryParameters, @Valid @NotNull Map<ColumnType, DirectionType> orderBy, @Valid @NotNull Integer firstResult, @Valid @NotNull Integer maxResult);

	SubscriptionEntity byName(@Valid @NotNull Map<String, Object> queryParameters);
}
