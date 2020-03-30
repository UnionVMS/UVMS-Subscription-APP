package eu.europa.ec.fisheries.uvms.subscription.service.dto.search;

import eu.europa.ec.fisheries.uvms.subscription.service.domain.search.SubscriptionListQuery;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubscriptionListQueryImpl implements SubscriptionListQuery {

    PaginationDataImpl pagination;
    OrderByDataImpl orderBy;
    SubscriptionSearchCriteriaImpl criteria;
}
