/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2020.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subscription.service.trigger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import eu.europa.ec.fisheries.uvms.subscription.helper.DateTimeServiceTestImpl;
import eu.europa.ec.fisheries.uvms.subscription.service.dao.SubscriptionDao;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.AssetEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.AssetGroupEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionExecution;
import eu.europa.ec.fisheries.uvms.subscription.service.messaging.AssetPageRetrievalMessage;
import eu.europa.ec.fisheries.uvms.subscription.service.messaging.SubscriptionSender;
import eu.europa.fisheries.uvms.subscription.model.enums.SubscriptionTimeUnit;
import eu.europa.fisheries.uvms.subscription.model.enums.TriggerType;
import eu.europa.fisheries.uvms.subscription.model.exceptions.EntityDoesNotExistException;
import eu.europa.fisheries.uvms.subscription.model.exceptions.ScheduledSubscriptionProcessingException;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests for the {@link ScheduledSubscriptionTriggeringServiceImpl}.
 */
@EnableAutoWeld
@ExtendWith(MockitoExtension.class)
class ScheduledSubscriptionTriggeringServiceImplTest {

    private static final Date ACTIVATION_DATE = Date.from(Instant.parse("2020-05-05T12:00:00Z"));
    private static final Date A_DATE_BEFORE_NOW = Date.from(Instant.parse("2020-05-05T11:59:00Z"));
    private static final Date A_DATE_AFTER_NOW = Date.from(Instant.parse("2020-05-05T12:59:00Z"));

    private static final int PAGE_SIZE = 10;
    public static final int FIRST_PAGE = 0;
    private static final Long SCHEDULED_SUBSCRIPTIONS_JMS_MESSAGE_PAGE_SIZE = 10L;
    private static final Long SCHEDULED_SUBSCRIPTIONS_JMS_FIRST_PAGE = 1L;
    private static final String MAIN_ASSETS = "mainAssets";


    @Mock
    @Produces
    private SubscriptionDao subscriptionDao;

    @Mock
    @Produces
    private SubscriptionSender subscriptionSender;

    @Produces
    private DateTimeServiceTestImpl dateTimeService = new DateTimeServiceTestImpl();

    @Inject
    private ScheduledSubscriptionTriggeringServiceImpl sut;

    @BeforeEach
    void beforeEach() {
        LocalDateTime now = LocalDateTime.parse("2020-05-05T12:00:00");
        dateTimeService.setNow(now);
    }

    @Test
    void testFindScheduledSubscriptionIdsForTriggering() {
        List<SubscriptionEntity> expectedSubscriptionEntities = makeSubscriptionEntitiesList(1L, 2L);
        when(subscriptionDao.findScheduledSubscriptionIdsForTriggering(ACTIVATION_DATE, FIRST_PAGE, PAGE_SIZE)).thenReturn(expectedSubscriptionEntities.stream().map(SubscriptionEntity::getId).collect(Collectors.toList()));
        Stream<Long> result = sut.findScheduledSubscriptionIdsForTriggering(ACTIVATION_DATE);

        List<Long> expectedSubscriptionIds = expectedSubscriptionEntities.stream().map(SubscriptionEntity::getId).collect(Collectors.toList());

        // convert result to a list as stream will not let operate more than once to complete all the assertions needed
        List<Long> resultData = result.collect(Collectors.toList());

        assertNotNull(resultData);
        assertEquals(expectedSubscriptionEntities.size(), resultData.size());
        resultData.forEach(s -> {
            assert (expectedSubscriptionIds.contains(s));
        });
    }

    @Test
    void testEnqueueForTriggeringInNewTransaction() {
        SubscriptionEntity subscriptionEntity = makeSubscriptionEntity(1L);
        when(subscriptionDao.findById(subscriptionEntity.getId())).thenReturn(subscriptionEntity);
        sut.enqueueForTriggeringInNewTransaction(subscriptionEntity.getId());

        ArgumentCaptor<AssetPageRetrievalMessage> assetPageRetrievalMessageCaptor = ArgumentCaptor.forClass(AssetPageRetrievalMessage.class);
        verify(subscriptionSender, times(3)).sendMessageForScheduledSubscriptionExecutionSameTx(assetPageRetrievalMessageCaptor.capture());
        List<AssetPageRetrievalMessage> assetMessages = assetPageRetrievalMessageCaptor.getAllValues().stream()
                .filter(v -> MAIN_ASSETS.equals(v.getAssetGroupGuid()))
                .collect(Collectors.toList());
        for (AssetPageRetrievalMessage message : assetMessages) {
            assertEquals(subscriptionEntity.getId(), message.getSubscriptionId());
            assertEquals(MAIN_ASSETS, message.getAssetGroupGuid());
            assertEquals(SCHEDULED_SUBSCRIPTIONS_JMS_FIRST_PAGE, message.getPageNumber());
            assertEquals(SCHEDULED_SUBSCRIPTIONS_JMS_MESSAGE_PAGE_SIZE, message.getPageSize());
        }

        List<AssetPageRetrievalMessage> assetGroupMessages = assetPageRetrievalMessageCaptor.getAllValues().stream()
                .filter(v -> !MAIN_ASSETS.equals(v.getAssetGroupGuid()))
                .collect(Collectors.toList());
        assertEquals(subscriptionEntity.getAssetGroups().size(), assetGroupMessages.size());
        for (AssetPageRetrievalMessage message : assetGroupMessages) {
            assertEquals(subscriptionEntity.getId(), message.getSubscriptionId());
            assert (subscriptionEntity.getAssetGroups().stream().map(AssetGroupEntity::getGuid).collect(Collectors.toList()).contains(message.getAssetGroupGuid()));
            assertEquals(SCHEDULED_SUBSCRIPTIONS_JMS_FIRST_PAGE, message.getPageNumber());
            assertEquals(SCHEDULED_SUBSCRIPTIONS_JMS_MESSAGE_PAGE_SIZE, message.getPageSize());
        }
    }

    @Test
    void testEnqueueForTriggeringInNewTransactionErrorEntityDoesNotExist() {
        SubscriptionEntity subscriptionEntity = new SubscriptionEntity();
        subscriptionEntity.setId(1L);
        when(subscriptionDao.findById(subscriptionEntity.getId())).thenReturn(null);

        assertThrows(EntityDoesNotExistException.class, () ->
                sut.enqueueForTriggeringInNewTransaction(subscriptionEntity.getId())
        );
    }

    @Test
    void testEnqueueForTriggeringInNewTransactionInactiveSubscriptionError() {
        SubscriptionEntity subscriptionEntity = new SubscriptionEntity();
        subscriptionEntity.setId(1L);
        subscriptionEntity.setActive(false);
        when(subscriptionDao.findById(subscriptionEntity.getId())).thenReturn(subscriptionEntity);

        assertThrows(ScheduledSubscriptionProcessingException.class, () ->
                sut.enqueueForTriggeringInNewTransaction(subscriptionEntity.getId())
        );
    }

    @Test
    void testEnqueueForTriggeringInNewTransactionAlreadyProcessedError() {
        SubscriptionEntity subscriptionEntity = makeSubscriptionEntity(1L);
        subscriptionEntity.setActive(true);
        subscriptionEntity.getExecution().setNextScheduledExecution(A_DATE_AFTER_NOW);
        when(subscriptionDao.findById(subscriptionEntity.getId())).thenReturn(subscriptionEntity);

        assertThrows(ScheduledSubscriptionProcessingException.class, () ->
                sut.enqueueForTriggeringInNewTransaction(subscriptionEntity.getId())
        );
    }

    private List<SubscriptionEntity> makeSubscriptionEntitiesList(Long... subscriptionIds) {
        return Arrays.stream(subscriptionIds)
                .map(this::makeSubscriptionEntity)
                .collect(Collectors.toList());
    }

    private SubscriptionEntity makeSubscriptionEntity(Long id) {
        SubscriptionEntity subscriptionEntity = new SubscriptionEntity();
        subscriptionEntity.setId(id);
        subscriptionEntity.setActive(true);

        Set<AssetEntity> assetEntitySet = new HashSet<>(Arrays.asList(mock(AssetEntity.class), mock(AssetEntity.class)));
        subscriptionEntity.setAssets(assetEntitySet);
        Set<AssetGroupEntity> assetGroupGuids = new HashSet<>(Arrays.asList(
                makeAssetGroupEntity("asset-group-guid-1"),
                makeAssetGroupEntity("asset-group-guid-2")
        ));
        subscriptionEntity.setAssetGroups(assetGroupGuids);

        SubscriptionExecution subscriptionExecution = new SubscriptionExecution();
        subscriptionExecution.setTriggerType(TriggerType.SCHEDULER);
        subscriptionExecution.setFrequencyUnit(SubscriptionTimeUnit.DAYS);
        subscriptionExecution.setFrequency(1);
        subscriptionExecution.setTimeExpression("13:00");
        subscriptionExecution.setNextScheduledExecution(A_DATE_BEFORE_NOW);
        subscriptionEntity.setExecution(subscriptionExecution);

        return subscriptionEntity;
    }

    private AssetGroupEntity makeAssetGroupEntity(String guid) {
        AssetGroupEntity assetGroupEntity = new AssetGroupEntity();
        assetGroupEntity.setGuid(guid);
        return assetGroupEntity;
    }
}