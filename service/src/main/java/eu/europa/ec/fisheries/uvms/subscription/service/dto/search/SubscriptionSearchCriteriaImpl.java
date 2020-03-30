package eu.europa.ec.fisheries.uvms.subscription.service.dto.search;

import java.time.ZonedDateTime;

import eu.europa.ec.fisheries.uvms.subscription.service.domain.OutgoingMessageType;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.search.SubscriptionSearchCriteria;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubscriptionSearchCriteriaImpl implements SubscriptionSearchCriteria {

    String name;
    Boolean active;
    Long organisation;
    Long endPoint;
    Long channel;
    String description;
    ZonedDateTime startDate;
    ZonedDateTime endDate;
    OutgoingMessageType messageType;
}
