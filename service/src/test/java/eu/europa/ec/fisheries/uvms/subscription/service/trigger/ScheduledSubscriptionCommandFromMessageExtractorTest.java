/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2020.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subscription.service.trigger;

import static eu.europa.fisheries.uvms.subscription.model.enums.TriggeredSubscriptionStatus.ACTIVE;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.xml.datatype.DatatypeFactory;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import eu.europa.ec.fisheries.uvms.commons.domain.DateRange;
import eu.europa.ec.fisheries.uvms.subscription.helper.DateTimeServiceTestImpl;
import eu.europa.ec.fisheries.uvms.subscription.service.bean.Command;
import eu.europa.ec.fisheries.uvms.subscription.service.bean.SubscriptionAssetService;
import eu.europa.ec.fisheries.uvms.subscription.service.bean.SubscriptionFinder;
import eu.europa.ec.fisheries.uvms.subscription.service.bean.SubscriptionSpatialService;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.AssetEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.AssetGroupEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionOutput;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.TriggeredSubscriptionDataEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.TriggeredSubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.filter.AreaFiltererComponent;
import eu.europa.ec.fisheries.uvms.subscription.service.messaging.AssetPageRetrievalMessage;
import eu.europa.ec.fisheries.uvms.subscription.service.messaging.SubscriptionSender;
import eu.europa.ec.fisheries.uvms.subscription.service.messaging.asset.AssetSender;
import eu.europa.ec.fisheries.wsdl.asset.types.VesselIdentifiersWithConnectIdHolder;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mapstruct.ap.internal.util.Collections;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests for the {@link ScheduledSubscriptionCommandFromMessageExtractor}.
 */
@EnableAutoWeld
@ExtendWith(MockitoExtension.class)
class ScheduledSubscriptionCommandFromMessageExtractorTest {

    private static final String SOURCE = "scheduled";
    private static final LocalDateTime NOW = LocalDateTime.now();
    private static final ZonedDateTime NOWZDT = NOW.atZone(ZoneId.of("UTC"));

    @Produces
    @Mock
    private SubscriptionFinder subscriptionFinder;

    @Produces
    @Mock
    private TriggerCommandsFactory triggerCommandsFactory;

    @Produces
    @Mock
    private AssetSender assetSender;

    @Produces
    @Mock
    private SubscriptionAssetService subscriptionAssetService;

    @Produces
    @Mock
    private SubscriptionSpatialService subscriptionSpatialService;

    @Produces
    @Mock
    private AreaFiltererComponent areaFiltererComponent;

    @Produces
    @ApplicationScoped
    private final DateTimeServiceTestImpl dateTimeService = new DateTimeServiceTestImpl();

    @Produces
    DatatypeFactory getDatatypeFactory() throws Exception {
        return DatatypeFactory.newInstance();
    }

    @Inject
    private ScheduledSubscriptionCommandFromMessageExtractor sut;

    @Test
    void testEmptyConstructor() {
        ScheduledSubscriptionCommandFromMessageExtractor sut = new ScheduledSubscriptionCommandFromMessageExtractor();
        assertNotNull(sut);
    }

    @Test
    void getEligibleSubscriptionSource() {
        assertEquals(SOURCE, sut.getEligibleSubscriptionSource());
    }

    @Test
    void testPreserveDataFromSupersededTriggering() {
        assertDoesNotThrow(() -> sut.preserveDataFromSupersededTriggering(null, null));
    }

    @ParameterizedTest
    @MethodSource("pagedAssetParams")
    void extractCommandsFromMainAssets(AssetPageRetrievalMessage receivedMessageFromQueue, AssetPageRetrievalMessage messageForQueue) {
        SubscriptionEntity subscriptionEntity = createSubscriptionEntity(true);
        subscriptionEntity.getAssets().add(makeAsset(1L, "Queen Mary"));
        dateTimeService.setNow(NOW);

        SubscriptionSender subscriptionSender = mock(SubscriptionSender.class);
        when(subscriptionFinder.findSubscriptionById(500L)).thenReturn(subscriptionEntity);
        AssetPageRetrievalCommand assetPageRetrievalCommand = new AssetPageRetrievalCommand(messageForQueue, subscriptionSender);
        when(triggerCommandsFactory.createAssetPageRetrievalCommand(any())).thenReturn(assetPageRetrievalCommand);
        //skip area filtering
        when(areaFiltererComponent.filterAssetsBySubscriptionAreas(any(),any(),any())).thenAnswer(i-> i.getArgument(0));
        Stream<Command> result = sut.extractCommands(AssetPageRetrievalMessage.encodeMessage(receivedMessageFromQueue), null, "123",NOWZDT);

        assertNotNull(result);
        assertEquals(3, result.count());

        ArgumentCaptor<TriggeredSubscriptionEntity> triggeredSubscriptionCaptor = ArgumentCaptor.forClass(TriggeredSubscriptionEntity.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Function<TriggeredSubscriptionEntity, Set<TriggeredSubscriptionDataEntity>>> dataExtractorCaptor = ArgumentCaptor.forClass(Function.class);
        verify(triggerCommandsFactory, times(2)).createTriggerSubscriptionCommand(triggeredSubscriptionCaptor.capture(), dataExtractorCaptor.capture());
        TriggeredSubscriptionEntity triggeredSubscription = triggeredSubscriptionCaptor.getValue();
        assertSame(subscriptionEntity, triggeredSubscription.getSubscription());
        assertNotNull(triggeredSubscription.getCreationDate());
        assertEquals(ACTIVE, triggeredSubscription.getStatus());
        assertEquals(SOURCE, triggeredSubscription.getSource());
        assertEquals(Date.from(NOW.toInstant(ZoneOffset.UTC)), triggeredSubscription.getCreationDate());
        assertEquals(Date.from(NOW.toInstant(ZoneOffset.UTC)), triggeredSubscription.getEffectiveFrom());
    }

    protected static Stream<Arguments> pagedAssetParams() {
        return Stream.of(
                Arguments.of(new AssetPageRetrievalMessage(false, 500L, "mainAssets", 1L, 2L),
                        new AssetPageRetrievalMessage(false, 500L, "mainAssets", 2L, 2L)),
                Arguments.of(new AssetPageRetrievalMessage(false, 500L, "mainAssets", 2L, 2L),
                        new AssetPageRetrievalMessage(false, 500L, "mainAssets", 3L, 2L))
        );
    }

    @ParameterizedTest
    @MethodSource("withQueryPeriodTestInput")
    void extractCommandsFromAssetGroup(boolean withQueryPeriod) {
        String isGroup = "g";
        Long subscriptionId = 500L;
        String assetGroupName = "greece";
        Long pageNumber = 0L;
        Long pageSize = 3L;
        String encodedMessageFromQueue = String.join(";", isGroup, subscriptionId.toString(), assetGroupName, pageNumber.toString(), pageSize.toString());
        SubscriptionEntity subscriptionEntity = createSubscriptionEntity(withQueryPeriod);
        List<VesselIdentifiersWithConnectIdHolder> groupAssets = makeAssetsWithStaticData();
        dateTimeService.setNow(NOW);

        when(subscriptionFinder.findSubscriptionById(subscriptionId)).thenReturn(subscriptionEntity);
        SubscriptionSender subscriptionSender = mock(SubscriptionSender.class);
        AssetPageRetrievalMessage assetPageRetrievalMessage = new AssetPageRetrievalMessage(true, 500L, "greece", 1L, 3L);
        AssetPageRetrievalCommand assetPageRetrievalCommand = new AssetPageRetrievalCommand(assetPageRetrievalMessage, subscriptionSender);
        when(triggerCommandsFactory.createAssetPageRetrievalCommand(any())).thenReturn(assetPageRetrievalCommand);
        when(assetSender.findAssetIdentifiersByAssetGroupGuid(assetGroupName, dateTimeService.getNowAsDate(), pageNumber, pageSize)).thenReturn(groupAssets);
        //skip area filtering
        when(areaFiltererComponent.filterAssetsBySubscriptionAreas(any(),any(),any())).thenAnswer(i-> i.getArgument(0));
        Stream<Command> result = sut.extractCommands(encodedMessageFromQueue, null, "123",NOWZDT);

        assertNotNull(result);
        assertEquals(4, result.count());

        ArgumentCaptor<String> assetGroupNameCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Date> dateCaptor = ArgumentCaptor.forClass(Date.class);
        ArgumentCaptor<Long> pageNumberCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Long> pageSizeCaptor = ArgumentCaptor.forClass(Long.class);
        verify(assetSender).findAssetIdentifiersByAssetGroupGuid(assetGroupNameCaptor.capture(), dateCaptor.capture(), pageNumberCaptor.capture(), pageSizeCaptor.capture());
        assertEquals(assetGroupName, assetGroupNameCaptor.getValue());
        assertEquals(pageNumber, pageNumberCaptor.getValue());
        assertEquals(pageSize, pageSizeCaptor.getValue());

        ArgumentCaptor<TriggeredSubscriptionEntity> triggeredSubscriptionCaptor = ArgumentCaptor.forClass(TriggeredSubscriptionEntity.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Function<TriggeredSubscriptionEntity, Set<TriggeredSubscriptionDataEntity>>> dataExtractorCaptor = ArgumentCaptor.forClass(Function.class);
        verify(triggerCommandsFactory, times(3)).createTriggerSubscriptionCommand(triggeredSubscriptionCaptor.capture(), dataExtractorCaptor.capture());
        TriggeredSubscriptionEntity triggeredSubscription = triggeredSubscriptionCaptor.getValue();
        assertSame(subscriptionEntity, triggeredSubscription.getSubscription());
        assertNotNull(triggeredSubscription.getCreationDate());
        assertEquals(ACTIVE, triggeredSubscription.getStatus());
        assertEquals(SOURCE, triggeredSubscription.getSource());
        assertEquals(Date.from(NOW.toInstant(ZoneOffset.UTC)), triggeredSubscription.getCreationDate());
        assertEquals(Date.from(NOW.toInstant(ZoneOffset.UTC)), triggeredSubscription.getEffectiveFrom());
        assertNotNull(triggeredSubscription.getData());

        assertEquals("guid-3", findInData(triggeredSubscription, "connectId"));
        if (withQueryPeriod) {
            assertNull(findInData(triggeredSubscription, "occurrence"));
        } else {
            assertNotNull(findInData(triggeredSubscription, "occurrence"));
        }
        assertEquals("IRCS_VALUE", findInData(triggeredSubscription, "IRCS"));
        assertEquals("UVI_VALUE", findInData(triggeredSubscription, "UVI"));
        assertEquals("ICCAT_VALUE", findInData(triggeredSubscription, "ICCAT"));
        assertEquals("CFR_VALUE", findInData(triggeredSubscription, "CFR"));
        assertNull(findInData(triggeredSubscription, "EXT_MARK"));
    }

    protected static Stream<Arguments> withQueryPeriodTestInput() {
        return Stream.of(Arguments.of(true), Arguments.of(false));
    }


    private String findInData(TriggeredSubscriptionEntity triggeredSubscription, String key) {
        return triggeredSubscription.getData().stream().filter(element -> key.equals(element.getKey())).findAny().map(TriggeredSubscriptionDataEntity::getValue).orElse(null);
    }

    private SubscriptionEntity createSubscriptionEntity(boolean withQueryPeriod) {
        SubscriptionEntity subscriptionEntity = new SubscriptionEntity();
        subscriptionEntity.setId(500L);
        subscriptionEntity.setAssetGroups(Collections.asSet(makeAssetGroupEntity("greece"), makeAssetGroupEntity("small-ships")));
        subscriptionEntity.setAssets(new HashSet<>(makeAssets()));
        SubscriptionOutput output = new SubscriptionOutput();
        DateRange queryPeriod;
        if (withQueryPeriod) {
            queryPeriod = new DateRange();
            queryPeriod.setStartDate(new Date());
            queryPeriod.setStartDate(Date.from(new Date().toInstant().plus(2, ChronoUnit.DAYS)));
            output.setQueryPeriod(queryPeriod);
        }
        subscriptionEntity.setOutput(output);
        return subscriptionEntity;
    }

    private AssetEntity makeAsset(long id, String name) {
        AssetEntity assetEntity = new AssetEntity();
        assetEntity.setId(id);
        assetEntity.setGuid(UUID.randomUUID().toString());
        assetEntity.setName(name);
        return assetEntity;
    }

    private AssetEntity makeAssetWithStaticData(long id, String name, String guid) {
        AssetEntity assetEntity = new AssetEntity();
        assetEntity.setId(id);
        assetEntity.setGuid(guid);
        assetEntity.setName(name);
        assetEntity.setIrcs("IRCS_VALUE");
        assetEntity.setCfr("CFR_VALUE");
        assetEntity.setIccat("ICCAT_VALUE");
        assetEntity.setUvi("UVI_VALUE");
        assetEntity.setExtMark(null);
        return assetEntity;
    }

    private AssetGroupEntity makeAssetGroupEntity(String name) {
        AssetGroupEntity assetGroupEntity = new AssetGroupEntity();
        assetGroupEntity.setName(name);
        assetGroupEntity.setGuid(UUID.randomUUID().toString());
        return assetGroupEntity;
    }

    private List<AssetEntity> makeAssets() {
        return Arrays.asList(makeAsset(1L, "Sea Pearl"), makeAsset(2L, "Titanic"), makeAsset(3L, "King George"));
    }

    private List<VesselIdentifiersWithConnectIdHolder> makeAssetsWithStaticData() {
        return Stream.of(makeAssetWithStaticData(1L, "Sea Pearl", "guid-1"), makeAssetWithStaticData(2L, "Titanic", "guid-2"), makeAssetWithStaticData(3L, "King George", "guid-3"))
                .map(a -> {
                    VesselIdentifiersWithConnectIdHolder holder = new VesselIdentifiersWithConnectIdHolder();
                    holder.setConnectId(a.getGuid());
                    holder.setCfr(a.getCfr());
                    holder.setIrcs(a.getIrcs());
                    holder.setIccat(a.getIccat());
                    holder.setUvi(a.getUvi());
                    holder.setExtMark(a.getExtMark());
                    return holder;
                }).collect(Collectors.toList());
    }
}
