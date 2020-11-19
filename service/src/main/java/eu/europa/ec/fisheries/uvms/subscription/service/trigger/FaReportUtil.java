package eu.europa.ec.fisheries.uvms.subscription.service.trigger;

import com.google.common.collect.Lists;
import eu.europa.ec.fisheries.uvms.activity.model.schemas.*;
import eu.europa.ec.fisheries.uvms.subscription.service.bean.SubscriptionFinder;
import eu.europa.ec.fisheries.uvms.subscription.service.bean.SubscriptionSpatialService;
import eu.europa.ec.fisheries.uvms.subscription.service.dao.SubscriptionDao;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.search.ActivityCriterion;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.search.AreaCriterion;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.search.SubscriptionSearchCriteria;
import eu.europa.ec.fisheries.uvms.subscription.service.messaging.asset.AssetSender;
import eu.europa.ec.fisheries.uvms.subscription.service.messaging.usm.UsmSender;
import eu.europa.ec.fisheries.wsdl.subscription.module.AreaType;
import eu.europa.ec.fisheries.wsdl.subscription.module.SubscriptionMovementMetaDataAreaType;
import eu.europa.ec.fisheries.wsdl.subscription.module.SubscriptionMovementMetaDataAreaTypeResponseElement;
import eu.europa.fisheries.uvms.subscription.model.enums.AssetType;
import eu.europa.fisheries.uvms.subscription.model.enums.SubscriptionFaReportDocumentType;
import eu.europa.fisheries.uvms.subscription.model.enums.TriggerType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._20.DelimitedPeriod;
import un.unece.uncefact.data.standard.reusableaggregatebusinessinformationentity._20.FishingActivity;
import un.unece.uncefact.data.standard.unqualifieddatatype._20.DateTimeType;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.xml.datatype.XMLGregorianCalendar;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@ApplicationScoped
@Slf4j
public class FaReportUtil implements FaReportUtility {

    private static final SubscriptionSearchCriteria.SenderCriterion BAD_SENDER = new SubscriptionSearchCriteria.SenderCriterion(-1L, -1L, -1L);

    @Inject
    private SubscriptionFinder subscriptionFinder;

    @Inject
    private SubscriptionDao subscriptionDao;

    @Inject
    private SubscriptionSpatialService subscriptionSpatialService;

    @Inject
    private UsmSender usmSender;

    @Inject
    private AssetSender assetSender;

    @Override
    public Stream<ReportContext> extractReportsFromRequest(ForwardReportToSubscriptionRequest request) {
        return request.getFaReports().stream()
                .filter(reportToSubscription -> {
                    if (reportToSubscription.getAssetHistoryGuids() == null) {
                        log.error("Incoming FA Report ({}) has null asset guids", makeReportToSubscriptionId(reportToSubscription));
                        return false;
                    }
                    if (reportToSubscription.getAssetHistoryGuids().size() != 1) {
                        log.error("Incoming FA Report ({}) has {} asset guids, must only have one!", makeReportToSubscriptionId(reportToSubscription), reportToSubscription.getAssetHistoryGuids().size());
                        return false;
                    }
                    return true;
                }).flatMap(reportToSubscription -> {
                    enrichWithUserAreas(reportToSubscription);
                    return indexed(reportToSubscription.getFishingActivities(), i -> i).entrySet().stream()
                            .map(en -> new ReportContext(request, reportToSubscription.getFluxFaReportMessageIds(), en.getKey(), reportToSubscription.getFaReportType(), reportToSubscription.getActivityAreas().get(en.getValue()).getAreas(), null, reportToSubscription.getAssetHistoryGuids().get(0)))
                            .filter(this::handleOccurrenceDate);
                });
    }

    private void enrichWithUserAreas(ReportToSubscription reportToSubscription) {
        List<XMLGregorianCalendar> dates = reportToSubscription.getFishingActivities().stream().map(fa ->
                fa.getOccurrenceDateTime().getDateTime()).collect(Collectors.toList());
        List<String> wktList = reportToSubscription.getActivitiesWktLists();
        List<SubscriptionMovementMetaDataAreaTypeResponseElement> responseList = subscriptionSpatialService.getFishingActivitiesUserAreasEnrichmentByWkt(indexed(wktList, dates::get));
        int index = 0;
        for (SubscriptionMovementMetaDataAreaTypeResponseElement element : responseList) {
            List<Area> movementMetaDataAreaTypes = mapSubscriptionAreasToActivityAreas(element.getElements());
            // enrich corresponding areas of activity
            reportToSubscription.getActivityAreas().get(index++).getAreas().addAll(movementMetaDataAreaTypes);
        }
    }

    private static <K, V> Map<K, V> indexed(List<K> keys, Function<Integer, V> fn) {
        return IntStream.range(0, keys.size())
                .boxed()
                .collect(Collectors.toMap(keys::get, fn));
    }

    private String makeReportToSubscriptionId(ReportToSubscription reportToSubscription) {
        return reportToSubscription.getFluxFaReportMessageIds().stream().map(id -> id.getSchemeId() + ':' + id.getId()).collect(Collectors.joining(","));
    }

    private boolean handleOccurrenceDate(ReportContext reportContext) {
        FishingActivity fishingActivity = reportContext.getFishingActivity();
        DateTimeType activityDateTimeType = fishingActivity.getOccurrenceDateTime() != null ? fishingActivity.getOccurrenceDateTime() : getFirstDateFromDelimitedPeriods(fishingActivity.getSpecifiedDelimitedPeriods());
        if (activityDateTimeType == null || activityDateTimeType.getDateTime() == null) {
            return false;
        }
        reportContext.setOccurrenceDate(activityDateTimeType.getDateTime().toGregorianCalendar().getTime());
        return true;
    }

    @Override
    public List<Area> mapSubscriptionAreasToActivityAreas(List<SubscriptionMovementMetaDataAreaType> subscriptionMovementMetaDataAreaTypes) {
        return subscriptionMovementMetaDataAreaTypes.stream().map(this::mapSubscriptionAreaToActivityArea).collect(Collectors.toList());
    }

    @Override
    public Area mapSubscriptionAreaToActivityArea(SubscriptionMovementMetaDataAreaType subscriptionMovementMetaDataAreaType) {
        if (subscriptionMovementMetaDataAreaType == null) {
            return null;
        }
        Area area = new Area();
        area.setAreaType(subscriptionMovementMetaDataAreaType.getAreaType());
        area.setRemoteId(subscriptionMovementMetaDataAreaType.getRemoteId());
        area.setName(subscriptionMovementMetaDataAreaType.getName());
        return area;
    }

    private DateTimeType getFirstDateFromDelimitedPeriods(Collection<DelimitedPeriod> delimitedPeriods) {
        return delimitedPeriods.stream()
                .map(DelimitedPeriod::getStartDateTime)
                .filter(Objects::nonNull)
                .min((a, b) -> a.getDateTime().compare(b.getDateTime()))
                .orElse(null);
    }

    @Override
    public List<SubscriptionEntity> findTriggeredSubscriptions(ReportContext reportContext, SubscriptionSearchCriteria.SenderCriterion senderCriterion) {
        FishingActivity fishingActivity = reportContext.getFishingActivity();
        ZonedDateTime validAt = ZonedDateTime.ofInstant(reportContext.occurrenceDate.toInstant(), ZoneId.of("UTC"));

        List<AreaCriterion> areas = extractAreas(reportContext).collect(Collectors.toList());

        List<SubscriptionSearchCriteria.AssetCriterion> assets = new ArrayList<>();
        assets.add(makeAssetCriteria(reportContext.getAssetHistGuid()));
        assets.addAll(makeAssetGroupCriteria(reportContext.occurrenceDate, reportContext.getAssetHistGuid()));

        List<ActivityCriterion> startActivityCriteria = Collections.singletonList(new ActivityCriterion(SubscriptionFaReportDocumentType.valueOf(reportContext.getFaReportType()), fishingActivity.getTypeCode().getValue()));

        return subscriptionFinder.findTriggeredSubscriptions(areas, assets, startActivityCriteria, senderCriterion, validAt, Collections.singleton(TriggerType.INC_FA_REPORT));
    }

    private Stream<AreaCriterion> extractAreas(ReportContext reportContext) {
        return reportContext.getActivityAreas().stream()
                .map(area -> new AreaCriterion(AreaType.fromValue(area.getAreaType()), Long.valueOf(area.getRemoteId())));
    }

    private SubscriptionSearchCriteria.AssetCriterion makeAssetCriteria(String assetHistGuid) {
        return new SubscriptionSearchCriteria.AssetCriterion(AssetType.ASSET, assetHistGuid);
    }

    private List<SubscriptionSearchCriteria.AssetCriterion> makeAssetGroupCriteria(Date date, String assetHistGuid) {
        return assetSender.findAssetGroupsForAsset(assetHistGuid, date)
                .stream()
                .map(assetGroupGuid -> new SubscriptionSearchCriteria.AssetCriterion(AssetType.VGROUP, assetGroupGuid))
                .collect(Collectors.toList());
    }

    @Override
    public SubscriptionSearchCriteria.SenderCriterion extractSenderCriterion(SenderInformation senderInformation) {
        return Optional.ofNullable(senderInformation)
                .filter(si -> StringUtils.isNotBlank(si.getDataflow()) && StringUtils.isNotBlank(si.getSenderOrReceiver()))
                .map(si -> usmSender.findOrganizationByDataFlowAndEndpoint(si.getDataflow(), si.getSenderOrReceiver()))
                .map(sender -> new SubscriptionSearchCriteria.SenderCriterion(sender.getOrganisationId(), sender.getEndpointId(), sender.getChannelId()))
                .orElse(BAD_SENDER);
    }

    @Override
    public List<SubscriptionEntity> findTriggeredSubscriptionsForFAQuery(ForwardQueryToSubscriptionRequest forwardQueryToSubscriptionRequest,
                                                                         SubscriptionSearchCriteria.SenderCriterion senderCriterion) {

        List<String> assetHistGuids = Lists.newArrayList(forwardQueryToSubscriptionRequest.getQueryToSubscription().getAssetHistGuids());
        ZonedDateTime validAt = ZonedDateTime.ofInstant((new Date()).toInstant(), ZoneId.of("UTC"));
        List<SubscriptionSearchCriteria.AssetCriterion> assets = new ArrayList<>();
        assets.addAll(assetHistGuids.stream().map(this::makeAssetCriteria).collect(Collectors.toList()));
        assets.addAll(assetHistGuids.stream().
                flatMap(assetHistGuid -> this.makeAssetGroupCriteria(new Date(), assetHistGuid).stream()).
                filter(Objects::nonNull).
                collect(Collectors.toList()));

        return subscriptionFinder.findTriggeredSubscriptions(null, assets, null, senderCriterion, validAt, Collections.singleton(TriggerType.INC_FA_QUERY));
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class ReportContext {
        /**
         * The message that has triggered us.
         */
        private ForwardReportToSubscriptionRequest unmarshalledMessage;
        private List<FluxReportIdentifier> fluxFaReportMessageIds;
        private FishingActivity fishingActivity;
        private String faReportType;
        private List<Area> activityAreas;
        /**
         * Occurrence date of activity, extracted from activity.
         */
        private Date occurrenceDate;
        /**
         * Asset history guid of reporting asset.
         */
        private String assetHistGuid;
    }
}
