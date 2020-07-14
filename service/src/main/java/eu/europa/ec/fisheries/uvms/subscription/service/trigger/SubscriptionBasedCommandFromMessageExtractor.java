/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subscription.service.trigger;

import static eu.europa.fisheries.uvms.subscription.model.enums.TriggeredSubscriptionStatus.ACTIVE;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import eu.europa.ec.fisheries.uvms.subscription.service.bean.Command;
import eu.europa.ec.fisheries.uvms.subscription.service.bean.SubscriptionFinder;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.AssetEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.TriggeredSubscriptionDataEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.TriggeredSubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.search.SubscriptionSearchCriteria.SenderCriterion;
import eu.europa.ec.fisheries.uvms.subscription.service.filter.AreaFilterComponent;
import eu.europa.ec.fisheries.uvms.subscription.service.messaging.AssetPageRetrievalMessage;
import eu.europa.ec.fisheries.uvms.subscription.service.messaging.asset.AssetSender;
import eu.europa.ec.fisheries.uvms.subscription.service.util.DateTimeService;
import eu.europa.ec.fisheries.wsdl.asset.types.VesselIdentifiersWithConnectIdHolder;
import eu.europa.fisheries.uvms.subscription.model.enums.SubscriptionVesselIdentifier;

/**
 * Base abstract class for manual and scheduled subscriptions.
 */
public abstract class SubscriptionBasedCommandFromMessageExtractor implements SubscriptionCommandFromMessageExtractor {

    private SubscriptionFinder subscriptionFinder;
    private TriggerCommandsFactory triggerCommandsFactory;
    private DatatypeFactory datatypeFactory;
    private DateTimeService dateTimeService;
    private AssetSender assetSender;
    private AreaFilterComponent areaFilterComponent;

    public SubscriptionBasedCommandFromMessageExtractor(SubscriptionFinder subscriptionFinder,
                                                        TriggerCommandsFactory triggerCommandsFactory,
                                                        DatatypeFactory datatypeFactory,
                                                        DateTimeService dateTimeService, AssetSender assetSender, AreaFilterComponent areaFilterComponent) {
        this.subscriptionFinder = subscriptionFinder;
        this.triggerCommandsFactory = triggerCommandsFactory;
        this.datatypeFactory = datatypeFactory;
        this.dateTimeService = dateTimeService;
        this.assetSender = assetSender;
        this.areaFilterComponent = areaFilterComponent;
    }

    /**
     * Constructor for frameworks.
     */
    @SuppressWarnings("unused")
    SubscriptionBasedCommandFromMessageExtractor() {
    }

    @Override
    public abstract String getEligibleSubscriptionSource();

    @Override
    public void preserveDataFromSupersededTriggering(TriggeredSubscriptionEntity superseded, TriggeredSubscriptionEntity replacement) {
        // NOOP
    }

    @Override
    public Function<TriggeredSubscriptionEntity, Set<TriggeredSubscriptionDataEntity>> getDataForDuplicatesExtractor() {
        return TriggeredSubscriptionDataUtil::extractConnectId;
    }

    @Override
    public Stream<Command> extractCommands(String representation, SenderCriterion ignoredSenderCriterion, ZonedDateTime receptionDateTime) {
        AssetPageRetrievalMessage assetPageRetrievalMessage = AssetPageRetrievalMessage.decodeMessage(representation);
        SubscriptionEntity subscription = subscriptionFinder.findSubscriptionById(assetPageRetrievalMessage.getSubscriptionId());

        List<AssetEntity> assets = assetPageRetrievalMessage.isGroup() ?
                handleAssetGroup(assetPageRetrievalMessage) : handleMainAssets(assetPageRetrievalMessage, subscription.getAssets());

        Stream<Command> nextPageCommandStream = assets.size() == assetPageRetrievalMessage.getPageSize() ?
                Stream.of(createAssetMessageCommandForNextPage(assetPageRetrievalMessage)) : Stream.empty();

        return Stream.concat(nextPageCommandStream, makeCommandsForSubscription(subscription, assets, receptionDateTime));
    }

    private Stream<Command> makeCommandsForSubscription(SubscriptionEntity subscription, List<AssetEntity> assets, ZonedDateTime receptionDateTime) {
        List<AssetAndSubscriptionData> assetAndSubscriptionData = assets.stream()
                .distinct()
                .map(makeAssetAndSubscriptionDataMapper(subscription))
                .collect(Collectors.toList());
        return areaFilterComponent.filterAssetsBySubscriptionAreas(assetAndSubscriptionData)
                .map(makeTriggeredSubscriptionEntity(receptionDateTime))
                .map(this::makeCommandForSubscription);
    }

    private Function<AssetAndSubscriptionData,TriggeredSubscriptionEntity> makeTriggeredSubscriptionEntity(ZonedDateTime receptionDateTime) {
        return assetAndSubscriptionData -> {
            TriggeredSubscriptionEntity result = new TriggeredSubscriptionEntity();
            result.setSubscription(assetAndSubscriptionData.getSubscription());
            result.setSource(getEligibleSubscriptionSource());
            result.setCreationDate(dateTimeService.getNowAsDate());
            result.setEffectiveFrom(Date.from(receptionDateTime.toInstant()));
            result.setStatus(ACTIVE);
            result.setData(makeTriggeredSubscriptionData(result, assetAndSubscriptionData));
            return result;
        };
    }

    private Set<TriggeredSubscriptionDataEntity> makeTriggeredSubscriptionData(TriggeredSubscriptionEntity triggeredSubscription, AssetAndSubscriptionData data) {
        Set<TriggeredSubscriptionDataEntity> result = new HashSet<>();
        addConnectIdData(triggeredSubscription, data, result);
        addOccurrenceDataIfRequired(triggeredSubscription, data, result);
        addVesselIdentifierData(triggeredSubscription, data, result);
        return result;
    }

    private void addConnectIdData(TriggeredSubscriptionEntity triggeredSubscription, AssetAndSubscriptionData data, Set<TriggeredSubscriptionDataEntity> result) {
        result.add(new TriggeredSubscriptionDataEntity(triggeredSubscription, TriggeredSubscriptionDataUtil.KEY_CONNECT_ID, data.getAssetEntity().getGuid()));
    }

    private void addOccurrenceDataIfRequired(TriggeredSubscriptionEntity triggeredSubscription,AssetAndSubscriptionData data, Set<TriggeredSubscriptionDataEntity> result) {
        if (triggeredSubscription.getSubscription().getOutput().getQueryPeriod() == null) {
            result.add(new TriggeredSubscriptionDataEntity(triggeredSubscription, TriggeredSubscriptionDataUtil.KEY_OCCURRENCE, data.getOccurrenceKeyData()));
        }
    }

    private void addVesselIdentifierData(TriggeredSubscriptionEntity triggeredSubscription, AssetAndSubscriptionData data, Set<TriggeredSubscriptionDataEntity> result) {
        Optional.ofNullable(data.getAssetEntity()).ifPresent(asset -> {
            addIdentifierIfNotNull(triggeredSubscription, result, SubscriptionVesselIdentifier.IRCS.name(), asset.getIrcs());
            addIdentifierIfNotNull(triggeredSubscription, result, SubscriptionVesselIdentifier.CFR.name(), asset.getCfr());
            addIdentifierIfNotNull(triggeredSubscription, result, SubscriptionVesselIdentifier.ICCAT.name(), asset.getIccat());
            addIdentifierIfNotNull(triggeredSubscription, result, SubscriptionVesselIdentifier.UVI.name(), asset.getUvi());
            addIdentifierIfNotNull(triggeredSubscription, result, SubscriptionVesselIdentifier.EXT_MARK.name(), asset.getExtMark());
        });
    }

    private void addIdentifierIfNotNull(TriggeredSubscriptionEntity triggeredSubscription, Set<TriggeredSubscriptionDataEntity> result, String vesselIdentifierKey, String vesselIdentifierValue) {
        Optional.ofNullable(vesselIdentifierValue).ifPresent(id ->
                result.add(new TriggeredSubscriptionDataEntity(triggeredSubscription, vesselIdentifierKey, id))
        );
    }

    private Command makeCommandForSubscription(TriggeredSubscriptionEntity triggeredSubscription) {
        return triggerCommandsFactory.createTriggerSubscriptionCommand(triggeredSubscription, getDataForDuplicatesExtractor());
    }

    private List<AssetEntity> handleAssetGroup(AssetPageRetrievalMessage assetPageRetrievalMessage) {
        List<VesselIdentifiersWithConnectIdHolder> assetIdentifiersByAssetGroupGuid = assetSender.findAssetIdentifiersByAssetGroupGuid(assetPageRetrievalMessage.getAssetGroupGuid(),
                dateTimeService.getNowAsDate(),
                assetPageRetrievalMessage.getPageNumber(),
                assetPageRetrievalMessage.getPageSize());
        return toAssetEntitiesListFrom(assetIdentifiersByAssetGroupGuid);

    }

    private List<AssetEntity> toAssetEntitiesListFrom(List<VesselIdentifiersWithConnectIdHolder> assetIdentifiersByAssetGroupGuid) {
        return assetIdentifiersByAssetGroupGuid.stream()
                .map(this::toAssetEntityFrom)
                .collect(Collectors.toList());
    }

    private AssetEntity toAssetEntityFrom(VesselIdentifiersWithConnectIdHolder element) {
        AssetEntity assetEntity = new AssetEntity();
        assetEntity.setGuid(element.getConnectId());
        assetEntity.setIrcs(element.getIrcs());
        assetEntity.setCfr(element.getCfr());
        assetEntity.setIccat(element.getIccat());
        assetEntity.setUvi(element.getUvi());
        assetEntity.setExtMark(element.getExtMark());
        return assetEntity;
    }

    private List<AssetEntity> handleMainAssets(AssetPageRetrievalMessage assetPageRetrievalMessage, Set<AssetEntity> assetEntities) {
        return assetEntities.stream()
                .sorted(Comparator.comparingLong(AssetEntity::getId))
                .skip((assetPageRetrievalMessage.getPageNumber() - 1) * assetPageRetrievalMessage.getPageSize())
                .limit(assetPageRetrievalMessage.getPageSize())
                .collect(Collectors.toList());
    }

    private Command createAssetMessageCommandForNextPage(AssetPageRetrievalMessage assetPageRetrievalMessage) {
        return triggerCommandsFactory.createAssetPageRetrievalCommand(
                new AssetPageRetrievalMessage(
                        assetPageRetrievalMessage.isGroup(),
                        assetPageRetrievalMessage.getSubscriptionId(),
                        assetPageRetrievalMessage.getAssetGroupGuid(),
                        assetPageRetrievalMessage.getPageNumber() + 1,
                        assetPageRetrievalMessage.getPageSize())
        );
    }

    private Function<AssetEntity,AssetAndSubscriptionData> makeAssetAndSubscriptionDataMapper(SubscriptionEntity manualSubscription) {
        return assetEntity -> {
            String occurrenceKeyData = null;
            if(manualSubscription.getOutput().getQueryPeriod() == null){
                GregorianCalendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
                calendar.setTime(dateTimeService.getNowAsDate());
                XMLGregorianCalendar xmlCalendar = datatypeFactory.newXMLGregorianCalendar(calendar);
                occurrenceKeyData = xmlCalendar.toXMLFormat();
            }
            return new AssetAndSubscriptionData(assetEntity,manualSubscription,occurrenceKeyData);
        };
    }
}
