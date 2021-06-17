/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2020.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subscription.activity.trigger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.xml.datatype.DatatypeFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;

import eu.europa.ec.fisheries.uvms.subscription.service.bean.Command;
import eu.europa.ec.fisheries.uvms.subscription.service.bean.SubscriptionFinder;
import eu.europa.ec.fisheries.uvms.subscription.service.bean.SubscriptionSpatialService;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.TriggeredSubscriptionDataEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.TriggeredSubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.search.ActivityCriterion;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.search.AreaCriterion;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.search.SubscriptionSearchCriteria;
import eu.europa.ec.fisheries.uvms.subscription.service.messaging.asset.AssetSender;
import eu.europa.ec.fisheries.uvms.subscription.service.trigger.StopConditionCriteria;
import eu.europa.ec.fisheries.uvms.subscription.service.trigger.TriggerCommandsFactory;
import eu.europa.fisheries.uvms.subscription.model.enums.TriggerType;
import eu.europa.fisheries.uvms.subscription.model.enums.TriggeredSubscriptionStatus;
import eu.europa.fisheries.uvms.subscription.model.exceptions.MessageFormatException;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests for the {@link ActivitySubscriptionCommandFromMessageExtractor}.
 */
@EnableAutoWeld
@ExtendWith(MockitoExtension.class)
class ActivitySubscriptionCommandFromMessageExtractorTest {

    private static final Date NOW = new Date();
    private static final ZonedDateTime RECEPTION_DT = ZonedDateTime.parse("2020-07-14T12:33:44Z", DateTimeFormatter.ISO_ZONED_DATE_TIME);

    @Produces
    @Mock
    private SubscriptionFinder subscriptionFinder;

    @Produces @Mock
    private AssetSender assetSender;
    
    @Produces @Mock
    private SubscriptionSpatialService subscriptionSpatialService;

    @Produces @ApplicationScoped
    private final DateTimeServiceTestImpl dateTimeService = new DateTimeServiceTestImpl();

    @Produces @Mock
    private TriggerCommandsFactory triggerCommandsFactory;

    @Inject
    private ActivitySubscriptionCommandFromMessageExtractor sut;

    @Produces
    DatatypeFactory getDatatypeFactory() throws Exception {
        return DatatypeFactory.newInstance();
    }

    @Test
    void testEmptyConstructor() {
        ActivitySubscriptionCommandFromMessageExtractor sut = new ActivitySubscriptionCommandFromMessageExtractor();
        assertNotNull(sut);
    }

    @Test
    void testGetEligibleSubscriptionSource() {
        assertEquals("activity", sut.getEligibleSubscriptionSource());
    }

    @Test
    void testPreserveDataFromSupersededTriggering() {
        TriggeredSubscriptionEntity superseded = new TriggeredSubscriptionEntity();
        TriggeredSubscriptionEntity replacement = new TriggeredSubscriptionEntity();
        superseded.getData().add(new TriggeredSubscriptionDataEntity(superseded, "reportId_12", "value"));
        superseded.getData().add(new TriggeredSubscriptionDataEntity(superseded, "irrelevant", "42"));
        replacement.getData().add(new TriggeredSubscriptionDataEntity(replacement, "irrelevant", "43"));

        sut.preserveDataFromSupersededTriggering(superseded, replacement);

        assertEquals(2, replacement.getData().size());
        assertEquals(2, superseded.getData().size());
        TriggeredSubscriptionDataEntity copiedData = replacement.getData().stream().filter(x -> x.getKey().startsWith("reportId_")).findFirst().get();
        assertSame(replacement, copiedData.getTriggeredSubscription());
        assertEquals("reportId_0", copiedData.getKey());
        assertEquals("value", copiedData.getValue());
        TriggeredSubscriptionDataEntity originalData = superseded.getData().stream().filter(x -> x.getKey().startsWith("reportId_")).findFirst().get();
        assertSame(superseded, originalData.getTriggeredSubscription());
    }

    @Test
    void testJAXBExceptionResultsInApplicationException() {
        assertThrows(MessageFormatException.class, () -> sut.extractCommands("bad",null,"123", RECEPTION_DT));
    }

    @Test
    void testDoNotTriggerRequestWithNullActivityOccurrenceDate() {
        verifyEmptyStreamForResource("ForwardReportToSubscriptionRequest-missing-activity-occurrence-date.xml");
    }

    @Test
    void testDoNotTriggerRequestWithNullAssetGuid() {
        verifyEmptyStreamForResource("ForwardReportToSubscriptionRequest-null-asset-guid.xml");
    }

    @Test
    void testDoNotTriggerRequestWithMultipleAssetGuids() {
        verifyEmptyStreamForResource("ForwardReportToSubscriptionRequest-multiple-asset-guids.xml");
    }

    @Test
    void testTriggerOKWithTwoFAReports() {
        SubscriptionEntity subscription = new SubscriptionEntity();
        subscription.setId(1L);
        when(subscriptionFinder.findTriggeredSubscriptions(any(), any(), any(), any(), any(), any())).thenReturn(Collections.singletonList(subscription));
        String representation = readResource("ForwardReportToSubscriptionRequest-two-valid-FAReports.xml");
        dateTimeService.setNow(NOW);
        SubscriptionSearchCriteria.SenderCriterion senderCriterion = new SubscriptionSearchCriteria.SenderCriterion(1L, 2L, 3L);

        List<Command> commands = sut.extractCommands(representation, senderCriterion,"123", RECEPTION_DT).collect(Collectors.toList());
        assertEquals(6, commands.size());

        ArgumentCaptor<TriggeredSubscriptionEntity> triggeredSubscriptionCaptor = ArgumentCaptor.forClass(TriggeredSubscriptionEntity.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Function<TriggeredSubscriptionEntity, Set<TriggeredSubscriptionDataEntity>>> dataExtractorCaptor = ArgumentCaptor.forClass(Function.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<BiPredicate<TriggeredSubscriptionEntity, TriggeredSubscriptionEntity>> processTriggeringsCaptor = ArgumentCaptor.forClass(BiPredicate.class);
        verify(triggerCommandsFactory, times(2)).createTriggerSubscriptionFromSpecificMessageCommand(triggeredSubscriptionCaptor.capture(), dataExtractorCaptor.capture(), processTriggeringsCaptor.capture());
        TriggeredSubscriptionEntity triggeredSubscription1 = triggeredSubscriptionCaptor.getAllValues().get(0);
        assertSame(subscription, triggeredSubscription1.getSubscription());
        assertNotNull(triggeredSubscription1.getCreationDate());
        assertEquals(TriggeredSubscriptionStatus.ACTIVE, triggeredSubscription1.getStatus());
        assertEquals(NOW, triggeredSubscription1.getCreationDate());
        assertEquals(Date.from(RECEPTION_DT.toInstant()), triggeredSubscription1.getEffectiveFrom());
        TriggeredSubscriptionEntity triggeredSubscription2 = triggeredSubscriptionCaptor.getAllValues().get(1);
        assertSame(subscription, triggeredSubscription2.getSubscription());
        assertNotNull(triggeredSubscription2.getCreationDate());
        assertEquals(TriggeredSubscriptionStatus.ACTIVE, triggeredSubscription2.getStatus());
        assertEquals(NOW, triggeredSubscription2.getCreationDate());
        assertEquals(Date.from(RECEPTION_DT.toInstant()), triggeredSubscription2.getEffectiveFrom());
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Collection<AreaCriterion>> areasCaptor = ArgumentCaptor.forClass(Collection.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Collection<SubscriptionSearchCriteria.AssetCriterion>> assetsCaptor = ArgumentCaptor.forClass(Collection.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Collection<ActivityCriterion>> startActivitiesCaptor = ArgumentCaptor.forClass(Collection.class);
        ArgumentCaptor<SubscriptionSearchCriteria.SenderCriterion> senderCaptor = ArgumentCaptor.forClass(SubscriptionSearchCriteria.SenderCriterion.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Collection<TriggerType>> triggerTypeCaptor = ArgumentCaptor.forClass(Collection.class);
        verify(subscriptionFinder, times(2)).findTriggeredSubscriptions(areasCaptor.capture(), assetsCaptor.capture(), startActivitiesCaptor.capture(), senderCaptor.capture(), any(), triggerTypeCaptor.capture());
        String areaCriteria1 = areasCaptor.getAllValues().get(0).stream().map(ac -> ac.getType().toString() + '-' + ac.getGid()).sorted().collect(Collectors.joining(","));
        String assetsCriteria1 = assetsCaptor.getAllValues().get(0).stream().map(ac -> ac.getType().toString() + '-' + ac.getGuid()).collect(Collectors.joining(","));
        String startActivityCriteria1 = startActivitiesCaptor.getAllValues().get(0).stream().map(sac -> sac.getType().toString() + '-' + sac.getValue()).collect(Collectors.joining(","));
        assertEquals("FMZ-270,FMZ-346,FMZ-38", areaCriteria1, "includes all movements except EXI");
        assertEquals("ASSET-assetGuid1", assetsCriteria1);
        assertEquals("DECLARATION-AREA_EXIT", startActivityCriteria1);
        assertEquals(1, triggerTypeCaptor.getAllValues().get(0).size());
        assertEquals(TriggerType.INC_FA_REPORT, triggerTypeCaptor.getAllValues().get(0).iterator().next());
        assertEquals(senderCriterion, senderCaptor.getValue());
        String areaCriteria2 = areasCaptor.getAllValues().get(1).stream().map(ac -> ac.getType().toString() + '-' + ac.getGid()).sorted().collect(Collectors.joining(","));
        String assetsCriteria2 = assetsCaptor.getAllValues().get(1).stream().map(ac -> ac.getType().toString() + '-' + ac.getGuid()).collect(Collectors.joining(","));
        String startActivityCriteria2 = startActivitiesCaptor.getAllValues().get(1).stream().map(sac -> sac.getType().toString() + '-' + sac.getValue()).collect(Collectors.joining(","));
        assertEquals("GFCM-21", areaCriteria2, "includes all movements except EXI");
        assertEquals("ASSET-assetGuid2", assetsCriteria2);
        assertEquals("DECLARATION-AREA_ENTRY", startActivityCriteria2);
        assertEquals(1, triggerTypeCaptor.getAllValues().get(1).size());
        assertEquals(TriggerType.INC_FA_REPORT, triggerTypeCaptor.getAllValues().get(1).iterator().next());

        ArgumentCaptor<StopConditionCriteria> stopConditionCriteriaArgumentCaptor = ArgumentCaptor.forClass(StopConditionCriteria.class);
        verify(triggerCommandsFactory, times(4)).createStopSubscriptionCommand(stopConditionCriteriaArgumentCaptor.capture());
        StopConditionCriteria stopConditionCriteria1 = stopConditionCriteriaArgumentCaptor.getAllValues().get(0);
        assertEquals("assetGuid1", stopConditionCriteria1.getConnectId());
        String notInAreaCriteria1 = stopConditionCriteria1.getAreas().stream().map(ac -> ac.getType().toString() + '-' + ac.getGid()).sorted().collect(Collectors.joining(","));
        assertEquals("FMZ-270,FMZ-346,FMZ-38", notInAreaCriteria1);
        assertNull(stopConditionCriteria1.getActivities());
        StopConditionCriteria stopConditionCriteria2 = stopConditionCriteriaArgumentCaptor.getAllValues().get(1);
        assertEquals("assetGuid1", stopConditionCriteria2.getConnectId());
        String stopActivitiesCriteria2 = stopConditionCriteria2.getActivities().stream().map(sac -> sac.getType().toString() + '-' + sac.getValue()).collect(Collectors.joining(","));
        assertEquals("DECLARATION-AREA_EXIT", stopActivitiesCriteria2);
        assertNull(stopConditionCriteria2.getAreas()); 
        
        StopConditionCriteria stopConditionCriteria3 = stopConditionCriteriaArgumentCaptor.getAllValues().get(2);
        assertEquals("assetGuid2", stopConditionCriteria3.getConnectId());
        String notInAreaCriteria3 = stopConditionCriteria3.getAreas().stream().map(ac -> ac.getType().toString() + '-' + ac.getGid()).sorted().collect(Collectors.joining(","));
        assertEquals("GFCM-21", notInAreaCriteria3);
        assertNull(stopConditionCriteria3.getActivities());
        StopConditionCriteria stopConditionCriteria4 = stopConditionCriteriaArgumentCaptor.getAllValues().get(3);
        assertEquals("assetGuid2", stopConditionCriteria4.getConnectId());
        String stopActivitiesCriteria4 = stopConditionCriteria4.getActivities().stream().map(sac -> sac.getType().toString() + '-' + sac.getValue()).collect(Collectors.joining(","));
        assertEquals("DECLARATION-AREA_ENTRY", stopActivitiesCriteria4);
        assertNull(stopConditionCriteria4.getAreas());

        assertEquals(2, dataExtractorCaptor.getAllValues().size());
        TriggeredSubscriptionEntity e = new TriggeredSubscriptionEntity();
        e.getData().add(new TriggeredSubscriptionDataEntity(e, "connectId", "CONNECT ID"));
        e.getData().add(new TriggeredSubscriptionDataEntity(e, "vesselId", "VESSEL ID"));
        e.getData().add(new TriggeredSubscriptionDataEntity(e, "vesselSchemeId", "VESSEL SCHEME ID"));
        e.getData().add(new TriggeredSubscriptionDataEntity(e, "occurrence", "OCCURRENCE"));
        e.getData().add(new TriggeredSubscriptionDataEntity(e, "somethingElse", "XXX"));
        Set<TriggeredSubscriptionDataEntity> result = dataExtractorCaptor.getAllValues().get(0).apply(e);
        assertEquals(Collections.singleton(new TriggeredSubscriptionDataEntity(e, "connectId", "CONNECT ID")), result);

        BiPredicate<TriggeredSubscriptionEntity, TriggeredSubscriptionEntity> processTriggerings = processTriggeringsCaptor.getValue();
        TriggeredSubscriptionEntity triggeredSubscriptionCandidate = new TriggeredSubscriptionEntity();
        TriggeredSubscriptionEntity existingTriggeredSubscription = new TriggeredSubscriptionEntity();
        triggeredSubscriptionCandidate.getData().add(new TriggeredSubscriptionDataEntity(triggeredSubscriptionCandidate, "reportId_12", "value"));
        triggeredSubscriptionCandidate.getData().add(new TriggeredSubscriptionDataEntity(triggeredSubscriptionCandidate, "irrelevant", "42"));
        existingTriggeredSubscription.getData().add(new TriggeredSubscriptionDataEntity(existingTriggeredSubscription, "irrelevant", "43"));
        assertTrue(processTriggerings.test(triggeredSubscriptionCandidate, existingTriggeredSubscription));
        assertEquals(2, existingTriggeredSubscription.getData().size());
        assertEquals(2, triggeredSubscriptionCandidate.getData().size());
        TriggeredSubscriptionDataEntity copiedData = existingTriggeredSubscription.getData().stream().filter(x -> x.getKey().startsWith("reportId_")).findFirst().get();
        assertSame(existingTriggeredSubscription, copiedData.getTriggeredSubscription());
        assertEquals("reportId_0", copiedData.getKey());
        assertEquals("value", copiedData.getValue());
        TriggeredSubscriptionDataEntity originalData = triggeredSubscriptionCandidate.getData().stream().filter(x -> x.getKey().startsWith("reportId_")).findFirst().get();
        assertSame(triggeredSubscriptionCandidate, originalData.getTriggeredSubscription());
    }

    @Test
    void testTriggerWithTwoFAReportsWithSameAssetGuids() {
        SubscriptionEntity subscription = new SubscriptionEntity();
        subscription.setId(1L);
        when(subscriptionFinder.findTriggeredSubscriptions(any(), any(), any(), any(), any(), any())).thenReturn(Collections.singletonList(subscription));
        String representation = readResource("ForwardReportToSubscriptionRequest-two-valid-FAReports-same-asset-guid.xml");
        dateTimeService.setNow(NOW);
        SubscriptionSearchCriteria.SenderCriterion senderCriterion = new SubscriptionSearchCriteria.SenderCriterion(1L, 2L, 3L);

        List<Command> commands = sut.extractCommands(representation, senderCriterion, "123",RECEPTION_DT).collect(Collectors.toList());

        assertEquals(5, commands.size());
        ArgumentCaptor<TriggeredSubscriptionEntity> triggeredSubscriptionCaptor = ArgumentCaptor.forClass(TriggeredSubscriptionEntity.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Function<TriggeredSubscriptionEntity, Set<TriggeredSubscriptionDataEntity>>> dataExtractorCaptor = ArgumentCaptor.forClass(Function.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<BiPredicate<TriggeredSubscriptionEntity, TriggeredSubscriptionEntity>> processTriggeringsCaptor = ArgumentCaptor.forClass(BiPredicate.class);
        verify(triggerCommandsFactory).createTriggerSubscriptionFromSpecificMessageCommand(triggeredSubscriptionCaptor.capture(), dataExtractorCaptor.capture(), processTriggeringsCaptor.capture());
        TriggeredSubscriptionEntity triggeredSubscription1 = triggeredSubscriptionCaptor.getValue();
        assertSame(subscription, triggeredSubscription1.getSubscription());
        assertNotNull(triggeredSubscription1.getCreationDate());
        assertEquals(TriggeredSubscriptionStatus.ACTIVE, triggeredSubscription1.getStatus());
        assertEquals(NOW, triggeredSubscription1.getCreationDate());
        assertEquals(Date.from(RECEPTION_DT.toInstant()), triggeredSubscription1.getEffectiveFrom());
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Collection<AreaCriterion>> areasCaptor = ArgumentCaptor.forClass(Collection.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Collection<SubscriptionSearchCriteria.AssetCriterion>> assetsCaptor = ArgumentCaptor.forClass(Collection.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Collection<ActivityCriterion>> startActivitiesCaptor = ArgumentCaptor.forClass(Collection.class);
        ArgumentCaptor<SubscriptionSearchCriteria.SenderCriterion> senderCaptor = ArgumentCaptor.forClass(SubscriptionSearchCriteria.SenderCriterion.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Collection<TriggerType>> triggerTypeCaptor = ArgumentCaptor.forClass(Collection.class);
        verify(subscriptionFinder, times(2)).findTriggeredSubscriptions(areasCaptor.capture(), assetsCaptor.capture(), startActivitiesCaptor.capture(), senderCaptor.capture(), any(), triggerTypeCaptor.capture());
        String areaCriteria1 = areasCaptor.getAllValues().get(0).stream().map(ac -> ac.getType().toString() + '-' + ac.getGid()).sorted().collect(Collectors.joining(","));
        String assetsCriteria1 = assetsCaptor.getAllValues().get(0).stream().map(ac -> ac.getType().toString() + '-' + ac.getGuid()).collect(Collectors.joining(","));
        String startActivityCriteria1 = startActivitiesCaptor.getAllValues().get(0).stream().map(sac -> sac.getType().toString() + '-' + sac.getValue()).collect(Collectors.joining(","));
        assertEquals("FMZ-270,FMZ-346,FMZ-38", areaCriteria1, "includes all movements except EXI");
        assertEquals("ASSET-assetGuid1", assetsCriteria1);
        assertEquals("DECLARATION-AREA_EXIT", startActivityCriteria1);
        assertEquals(1, triggerTypeCaptor.getAllValues().get(0).size());
        assertEquals(TriggerType.INC_FA_REPORT, triggerTypeCaptor.getAllValues().get(0).iterator().next());
        assertEquals(senderCriterion, senderCaptor.getValue());
        String areaCriteria2 = areasCaptor.getAllValues().get(1).stream().map(ac -> ac.getType().toString() + '-' + ac.getGid()).sorted().collect(Collectors.joining(","));
        String assetsCriteria2 = assetsCaptor.getAllValues().get(1).stream().map(ac -> ac.getType().toString() + '-' + ac.getGuid()).collect(Collectors.joining(","));
        String startActivityCriteria2 = startActivitiesCaptor.getAllValues().get(1).stream().map(sac -> sac.getType().toString() + '-' + sac.getValue()).collect(Collectors.joining(","));
        assertEquals("GFCM-21", areaCriteria2, "includes all movements except EXI");
        assertEquals("ASSET-assetGuid1", assetsCriteria2);
        assertEquals("DECLARATION-AREA_ENTRY", startActivityCriteria2);
        assertEquals(1, triggerTypeCaptor.getAllValues().get(1).size());
        assertEquals(TriggerType.INC_FA_REPORT, triggerTypeCaptor.getAllValues().get(1).iterator().next());

        ArgumentCaptor<StopConditionCriteria> stopConditionCriteriaArgumentCaptor = ArgumentCaptor.forClass(StopConditionCriteria.class);
        verify(triggerCommandsFactory, times(4)).createStopSubscriptionCommand(stopConditionCriteriaArgumentCaptor.capture());
        StopConditionCriteria stopConditionCriteria1 = stopConditionCriteriaArgumentCaptor.getAllValues().get(0);
        assertEquals("assetGuid1", stopConditionCriteria1.getConnectId());
        String notInAreaCriteria1 = stopConditionCriteria1.getAreas().stream().map(ac -> ac.getType().toString() + '-' + ac.getGid()).sorted().collect(Collectors.joining(","));
        assertEquals("FMZ-270,FMZ-346,FMZ-38", notInAreaCriteria1);
        assertNull(stopConditionCriteria1.getActivities());
        StopConditionCriteria stopConditionCriteria2 = stopConditionCriteriaArgumentCaptor.getAllValues().get(1);
        assertEquals("assetGuid1", stopConditionCriteria2.getConnectId());
        String stopActivitiesCriteria2 = stopConditionCriteria2.getActivities().stream().map(sac -> sac.getType().toString() + '-' + sac.getValue()).collect(Collectors.joining(","));
        assertEquals("DECLARATION-AREA_EXIT", stopActivitiesCriteria2);
        assertNull(stopConditionCriteria2.getAreas());

        StopConditionCriteria stopConditionCriteria3 = stopConditionCriteriaArgumentCaptor.getAllValues().get(2);
        assertEquals("assetGuid1", stopConditionCriteria3.getConnectId());
        String notInAreaCriteria3 = stopConditionCriteria3.getAreas().stream().map(ac -> ac.getType().toString() + '-' + ac.getGid()).sorted().collect(Collectors.joining(","));
        assertEquals("GFCM-21", notInAreaCriteria3);
        assertNull(stopConditionCriteria3.getActivities());
        StopConditionCriteria stopConditionCriteria4 = stopConditionCriteriaArgumentCaptor.getAllValues().get(3);
        assertEquals("assetGuid1", stopConditionCriteria4.getConnectId());
        String stopActivitiesCriteria4 = stopConditionCriteria4.getActivities().stream().map(sac -> sac.getType().toString() + '-' + sac.getValue()).collect(Collectors.joining(","));
        assertEquals("DECLARATION-AREA_ENTRY", stopActivitiesCriteria4);
        assertNull(stopConditionCriteria4.getAreas());

        assertEquals(1, dataExtractorCaptor.getAllValues().size());
        TriggeredSubscriptionEntity e = new TriggeredSubscriptionEntity();
        e.getData().add(new TriggeredSubscriptionDataEntity(e, "connectId", "CONNECT ID"));
        e.getData().add(new TriggeredSubscriptionDataEntity(e, "vesselId", "VESSEL ID"));
        e.getData().add(new TriggeredSubscriptionDataEntity(e, "vesselSchemeId", "VESSEL SCHEME ID"));
        e.getData().add(new TriggeredSubscriptionDataEntity(e, "occurrence", "OCCURRENCE"));
        e.getData().add(new TriggeredSubscriptionDataEntity(e, "somethingElse", "XXX"));
        Set<TriggeredSubscriptionDataEntity> result = dataExtractorCaptor.getAllValues().get(0).apply(e);
        assertEquals(Collections.singleton(new TriggeredSubscriptionDataEntity(e, "connectId", "CONNECT ID")), result);
    }

    @Test
    void testNoTrigger() {
        when(subscriptionFinder.findTriggeredSubscriptions(any(), any(), any(), any(), any(), any())).thenReturn(Collections.emptyList());
        String representation = readResource("ForwardReportToSubscriptionRequest-two-valid-FAReports.xml");
        dateTimeService.setNow(NOW);
        SubscriptionSearchCriteria.SenderCriterion senderCriterion = new SubscriptionSearchCriteria.SenderCriterion(1L, 2L, 3L);

        List<Command> commands = sut.extractCommands(representation, senderCriterion,"213", RECEPTION_DT).collect(Collectors.toList());

        assertEquals(4, commands.size()); //4 commands for finding subscriptions by stop criteria

        verify(triggerCommandsFactory, never()).createTriggerSubscriptionCommand(any(), any());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Collection<AreaCriterion>> areasCaptor = ArgumentCaptor.forClass(Collection.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Collection<SubscriptionSearchCriteria.AssetCriterion>> assetsCaptor = ArgumentCaptor.forClass(Collection.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Collection<ActivityCriterion>> startActivitiesCaptor = ArgumentCaptor.forClass(Collection.class);
        ArgumentCaptor<SubscriptionSearchCriteria.SenderCriterion> senderCaptor = ArgumentCaptor.forClass(SubscriptionSearchCriteria.SenderCriterion.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Collection<TriggerType>> triggerTypeCaptor = ArgumentCaptor.forClass(Collection.class);
        verify(subscriptionFinder, times(2)).findTriggeredSubscriptions(areasCaptor.capture(), assetsCaptor.capture(), startActivitiesCaptor.capture(), senderCaptor.capture(), any(), triggerTypeCaptor.capture());
        String areaCriteria1 = areasCaptor.getAllValues().get(0).stream().map(ac -> ac.getType().toString() + '-' + ac.getGid()).sorted().collect(Collectors.joining(","));
        String assetsCriteria1 = assetsCaptor.getAllValues().get(0).stream().map(ac -> ac.getType().toString() + '-' + ac.getGuid()).collect(Collectors.joining(","));
        String startActivityCriteria1 = startActivitiesCaptor.getAllValues().get(0).stream().map(sac -> sac.getType().toString() + '-' + sac.getValue()).collect(Collectors.joining(","));
        assertEquals("FMZ-270,FMZ-346,FMZ-38", areaCriteria1, "includes all movements except EXI");
        assertEquals("ASSET-assetGuid1", assetsCriteria1);
        assertEquals("DECLARATION-AREA_EXIT", startActivityCriteria1);
        assertEquals(1, triggerTypeCaptor.getAllValues().get(0).size());
        assertEquals(TriggerType.INC_FA_REPORT, triggerTypeCaptor.getAllValues().get(0).iterator().next());
        assertEquals(senderCriterion, senderCaptor.getValue());
        String areaCriteria2 = areasCaptor.getAllValues().get(1).stream().map(ac -> ac.getType().toString() + '-' + ac.getGid()).sorted().collect(Collectors.joining(","));
        String assetsCriteria2 = assetsCaptor.getAllValues().get(1).stream().map(ac -> ac.getType().toString() + '-' + ac.getGuid()).collect(Collectors.joining(","));
        String startActivityCriteria2 = startActivitiesCaptor.getAllValues().get(1).stream().map(sac -> sac.getType().toString() + '-' + sac.getValue()).collect(Collectors.joining(","));
        assertEquals("GFCM-21", areaCriteria2, "includes all movements except EXI");
        assertEquals("ASSET-assetGuid2", assetsCriteria2);
        assertEquals("DECLARATION-AREA_ENTRY", startActivityCriteria2);
        assertEquals(1, triggerTypeCaptor.getAllValues().get(1).size());
        assertEquals(TriggerType.INC_FA_REPORT, triggerTypeCaptor.getAllValues().get(1).iterator().next());

        ArgumentCaptor<StopConditionCriteria> stopConditionCriteriaArgumentCaptor = ArgumentCaptor.forClass(StopConditionCriteria.class);
        verify(triggerCommandsFactory, times(4)).createStopSubscriptionCommand(stopConditionCriteriaArgumentCaptor.capture());
        StopConditionCriteria stopConditionCriteria1 = stopConditionCriteriaArgumentCaptor.getAllValues().get(0);
        assertEquals("assetGuid1", stopConditionCriteria1.getConnectId());
        String notInAreaCriteria1 = stopConditionCriteria1.getAreas().stream().map(ac -> ac.getType().toString() + '-' + ac.getGid()).sorted().collect(Collectors.joining(","));
        assertEquals("FMZ-270,FMZ-346,FMZ-38", notInAreaCriteria1);
        assertNull(stopConditionCriteria1.getActivities());
        StopConditionCriteria stopConditionCriteria2 = stopConditionCriteriaArgumentCaptor.getAllValues().get(1);
        assertEquals("assetGuid1", stopConditionCriteria2.getConnectId());
        String stopActivitiesCriteria2 = stopConditionCriteria2.getActivities().stream().map(sac -> sac.getType().toString() + '-' + sac.getValue()).collect(Collectors.joining(","));
        assertEquals("DECLARATION-AREA_EXIT", stopActivitiesCriteria2);
        assertNull(stopConditionCriteria2.getAreas());
        StopConditionCriteria stopConditionCriteria3 = stopConditionCriteriaArgumentCaptor.getAllValues().get(2);
        assertEquals("assetGuid2", stopConditionCriteria3.getConnectId());
        String notInAreaCriteria3 = stopConditionCriteria3.getAreas().stream().map(ac -> ac.getType().toString() + '-' + ac.getGid()).sorted().collect(Collectors.joining(","));
        assertEquals("GFCM-21", notInAreaCriteria3);
        assertNull(stopConditionCriteria3.getActivities());
        StopConditionCriteria stopConditionCriteria4 = stopConditionCriteriaArgumentCaptor.getAllValues().get(3);
        assertEquals("assetGuid2", stopConditionCriteria4.getConnectId());
        String stopActivitiesCriteria4 = stopConditionCriteria4.getActivities().stream().map(sac -> sac.getType().toString() + '-' + sac.getValue()).collect(Collectors.joining(","));
        assertEquals("DECLARATION-AREA_ENTRY", stopActivitiesCriteria4);
        assertNull(stopConditionCriteria4.getAreas());

    }

    private void verifyEmptyStreamForResource(String resourceName) {
        String representation = readResource(resourceName);
        long size = sut.extractCommands(representation,null, "123",RECEPTION_DT).count();
        assertEquals(0, size);
        verifyNoInteractions(subscriptionFinder);
    }

    private String readResource(String resourceName) {
        try(InputStream is = this.getClass().getResourceAsStream(resourceName)) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buf = new byte[512];
            int r;
            while ((r = is.read(buf, 0, buf.length)) >= 0) {
                baos.write(buf, 0, r);
            }
            return new String(baos.toByteArray(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}