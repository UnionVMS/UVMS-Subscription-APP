package eu.europa.ec.fisheries.uvms.subscription.service.trigger;

import java.util.List;
import java.util.stream.Stream;

import eu.europa.ec.fisheries.uvms.activity.model.schemas.Area;
import eu.europa.ec.fisheries.uvms.activity.model.schemas.ForwardReportToSubscriptionRequest;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.search.SubscriptionSearchCriteria;
import eu.europa.ec.fisheries.wsdl.subscription.module.SubscriptionMovementMetaDataAreaType;

public interface FaReportUtility {
    Stream<FaReportUtil.ReportContext> extractReportsFromRequest(ForwardReportToSubscriptionRequest request);

    List<Area> mapSubscriptionAreasToActivityAreas(List<SubscriptionMovementMetaDataAreaType> subscriptionMovementMetaDataAreaTypes);

    Area mapSubscriptionAreaToActivityArea(SubscriptionMovementMetaDataAreaType subscriptionMovementMetaDataAreaType);

    List<SubscriptionEntity> findTriggeredSubscriptions(FaReportUtil.ReportContext reportContext, SubscriptionSearchCriteria.SenderCriterion senderCriterion);

    SubscriptionSearchCriteria.SenderCriterion extractSenderCriterion(SenderInformation senderInformation);
}
