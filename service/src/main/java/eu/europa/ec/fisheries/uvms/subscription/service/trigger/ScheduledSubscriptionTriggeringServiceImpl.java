/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subscription.service.trigger;

import static javax.transaction.Transactional.TxType.REQUIRES_NEW;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalUnit;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Stream;

import eu.europa.ec.fisheries.uvms.subscription.service.dao.SubscriptionDao;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.AssetGroupEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.messaging.AssetPageRetrievalMessage;
import eu.europa.ec.fisheries.uvms.subscription.service.messaging.SubscriptionSender;
import eu.europa.ec.fisheries.uvms.subscription.service.util.DateTimeService;
import eu.europa.fisheries.uvms.subscription.model.exceptions.EntityDoesNotExistException;
import eu.europa.fisheries.uvms.subscription.model.exceptions.ScheduledSubscriptionProcessingException;

/**
 * Implementation of the {@link ScheduledSubscriptionTriggeringService}.
 */
@ApplicationScoped
class ScheduledSubscriptionTriggeringServiceImpl implements ScheduledSubscriptionTriggeringService {

    private static final DateTimeFormatter TIME_EXPRESSION_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    private static final int PAGE_SIZE = 10;
    private static final int FIRST_PAGE = 0;
    private static final long SCHEDULED_SUBSCRIPTIONS_JMS_MESSAGE_PAGE_SIZE = 10L;
    private static final long SCHEDULED_SUBSCRIPTIONS_JMS_FIRST_PAGE = 1L;
    private static final String MAIN_ASSETS = "mainAssets";

    private SubscriptionDao subscriptionDAO;
    private SubscriptionSender subscriptionSender;
    private DateTimeService dateTimeService;

    /**
     * Constructor for injection
     *
     * @param subscriptionDAO    the subscriptions dao
     * @param subscriptionSender service that facilitates the enqueueing of jms messages
     * @param dateTimeService The DateTimeService
     */
    @Inject
    public ScheduledSubscriptionTriggeringServiceImpl(SubscriptionDao subscriptionDAO, SubscriptionSender subscriptionSender, DateTimeService dateTimeService) {
        this.subscriptionDAO = subscriptionDAO;
        this.subscriptionSender = subscriptionSender;
        this.dateTimeService = dateTimeService;
    }

    /**
     * Constructor for frameworks.
     */
    @SuppressWarnings("unused")
    ScheduledSubscriptionTriggeringServiceImpl() {
        // NOOP
    }

    @Override
    public Stream<Long> findScheduledSubscriptionIdsForTriggering(Date activationDate) {
        return subscriptionDAO.findScheduledSubscriptionIdsForTriggering(activationDate, FIRST_PAGE, PAGE_SIZE).stream();
    }

    @Transactional(REQUIRES_NEW)
    @Override
    public void enqueueForTriggeringInNewTransaction(Long subscriptionId) {
        SubscriptionEntity subscriptionEntity = Optional.ofNullable(subscriptionDAO.findById(subscriptionId))
                .orElseThrow(() -> new EntityDoesNotExistException("SubscriptionEntity with id " + subscriptionId));

        validateScheduledSubscriptionProcessingState(subscriptionEntity);

        if (isOutOfValidityPeriod(subscriptionEntity)) {
            invalidateNextScheduleExecutionDate(subscriptionEntity);
        } else {
            enqueueAssetGroupRetrievalMessages(subscriptionEntity);
            enqueueAssetRetrievalMessages(subscriptionEntity);
            enqueueAssetRetrievalMessagesByAreas(subscriptionEntity);
            updateNextScheduledExecutionDate(subscriptionEntity);
        }
    }

    private void validateScheduledSubscriptionProcessingState(SubscriptionEntity subscriptionEntity) {
        if (!subscriptionEntity.isActive()) {
            throw new ScheduledSubscriptionProcessingException("Scheduled subscription is inactive");
        }
        if (subscriptionEntity.getExecution().getNextScheduledExecution().after(dateTimeService.getNowAsDate())) {
            throw new ScheduledSubscriptionProcessingException("Scheduled Subscription is already processed");
        }
    }

    private void enqueueAssetGroupRetrievalMessages(SubscriptionEntity subscriptionEntity) {
        for (AssetGroupEntity assetGroup : subscriptionEntity.getAssetGroups()) {
            subscriptionSender.sendMessageForScheduledSubscriptionExecutionSameTx(
                    new AssetPageRetrievalMessage(
                            true,
                            subscriptionEntity.getId(),
                            assetGroup.getGuid(),
                            SCHEDULED_SUBSCRIPTIONS_JMS_FIRST_PAGE,
                            SCHEDULED_SUBSCRIPTIONS_JMS_MESSAGE_PAGE_SIZE));
        }
    }

    private void enqueueAssetRetrievalMessages(SubscriptionEntity subscriptionEntity) {
        subscriptionSender.sendMessageForScheduledSubscriptionExecutionSameTx(
                new AssetPageRetrievalMessage(
                        false,
                        subscriptionEntity.getId(),
                        MAIN_ASSETS,
                        SCHEDULED_SUBSCRIPTIONS_JMS_FIRST_PAGE,
                        SCHEDULED_SUBSCRIPTIONS_JMS_MESSAGE_PAGE_SIZE));
    }

    private void enqueueAssetRetrievalMessagesByAreas(SubscriptionEntity subscriptionEntity) {
        subscriptionSender.sendMessageForScheduledSubscriptionExecutionSameTx(
                new AssetPageRetrievalMessage(
                        null,
                        subscriptionEntity.getId(),
                        MAIN_ASSETS,
                        SCHEDULED_SUBSCRIPTIONS_JMS_FIRST_PAGE,
                        SCHEDULED_SUBSCRIPTIONS_JMS_MESSAGE_PAGE_SIZE));
    }
    
    private void invalidateNextScheduleExecutionDate(SubscriptionEntity subscriptionEntity) {
        subscriptionEntity.getExecution().setNextScheduledExecution(null);
    }

    private void updateNextScheduledExecutionDate(SubscriptionEntity subscriptionEntity) {
        Date nextActivationDate = calculateNextScheduledExecutionDate(subscriptionEntity);
        subscriptionEntity.getExecution().setNextScheduledExecution(nextActivationDate);
    }

    private boolean isOutOfValidityPeriod(SubscriptionEntity subscriptionEntity) {
        return subscriptionEntity.getValidityPeriod() == null || dateTimeService.getNowAsDate().after(subscriptionEntity.getValidityPeriod().getEndDate());
    }

    private Date calculateNextScheduledExecutionDate(SubscriptionEntity subscriptionEntity) {
        Integer frequency = subscriptionEntity.getExecution().getFrequency();
        TemporalUnit temporalUnit = subscriptionEntity.getExecution().getFrequencyUnit().getTemporalUnit();
        LocalTime time = LocalTime.parse(subscriptionEntity.getExecution().getTimeExpression(), TIME_EXPRESSION_FORMAT);
        Instant requestedTime = time.atDate(dateTimeService.getToday()).toInstant(ZoneOffset.UTC);
        requestedTime = requestedTime.plus(frequency, temporalUnit);
        return Date.from(requestedTime);
    }
}
