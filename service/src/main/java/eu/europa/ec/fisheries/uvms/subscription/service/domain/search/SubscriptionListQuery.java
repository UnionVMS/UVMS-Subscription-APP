package eu.europa.ec.fisheries.uvms.subscription.service.domain.search;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * All the information required to execute a search for subscriptions.
 */
public interface SubscriptionListQuery {
	@Valid
	@NotNull
	PaginationData getPagination();

	OrderByData<ColumnType> getOrderBy();

	SubscriptionSearchCriteria getCriteria();
}
