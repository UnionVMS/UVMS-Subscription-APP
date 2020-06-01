package eu.europa.ec.fisheries.uvms.subscription.service.dto.search;

import java.time.ZonedDateTime;
import java.util.Collection;

import eu.europa.ec.fisheries.uvms.subscription.service.domain.search.SubscriptionSearchCriteria;
import eu.europa.fisheries.uvms.subscription.model.enums.AccessibilityType;
import eu.europa.fisheries.uvms.subscription.model.enums.OutgoingMessageType;
import eu.europa.fisheries.uvms.subscription.model.enums.TriggerType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubscriptionSearchCriteriaImpl implements SubscriptionSearchCriteria {

    private String name;
    private Boolean active;
    private Long organisation;
    private Long endPoint;
    private Long channel;
    private String description;
    private ZonedDateTime startDate;
    private ZonedDateTime endDate;
    private ZonedDateTime validAt;
    private OutgoingMessageType messageType;
    private AccessibilityType accessibilityType;
    private Collection<AreaCriterion> inAnyArea;
    private Collection<AssetCriterion> withAnyAsset;
    private Collection<TriggerType> withAnyTriggerType;
}
