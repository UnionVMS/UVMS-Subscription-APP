package eu.europa.ec.fisheries.uvms.subscription.service.dto.search;

import java.time.ZonedDateTime;
import java.util.Collection;

import eu.europa.ec.fisheries.uvms.subscription.service.domain.search.ActivityCriterion;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.search.AreaCriterion;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.search.SubscriptionSearchCriteria;
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
    private Long endpoint;
    private Long channel;
    private String description;
    private ZonedDateTime startDate;
    private ZonedDateTime endDate;
    private ZonedDateTime validAt;
    private OutgoingMessageType messageType;
    private Collection<AreaCriterion> inAnyArea;
    private Boolean allowWithNoArea;
    private Collection<AssetCriterion> withAnyAsset;
    private Boolean allowWithNoAsset;
    private Collection<ActivityCriterion> withAnyStartActivity;
    private Boolean allowWithNoStartActivity;
    private SenderCriterion sender;
    private Boolean allowWithNoSenders;
    private Collection<TriggerType> withAnyTriggerType;
}
