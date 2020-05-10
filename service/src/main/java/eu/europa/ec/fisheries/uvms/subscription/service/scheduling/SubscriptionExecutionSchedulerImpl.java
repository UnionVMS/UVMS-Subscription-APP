/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subscription.service.scheduling;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionExecutionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.TriggeredSubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.util.DateTimeService;
import eu.europa.fisheries.uvms.subscription.model.enums.SubscriptionExecutionStatusType;

/**
 * Implementation of the {@link SubscriptionExecutionScheduler}.
 */
@ApplicationScoped
class SubscriptionExecutionSchedulerImpl implements SubscriptionExecutionScheduler {

	private static final DateTimeFormatter TIME_EXPRESSION_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

	private DateTimeService dateTimeService;

	/**
	 * Injection constructor.
	 *
	 * @param dateTimeService The date/time service
	 */
	@Inject
	public SubscriptionExecutionSchedulerImpl(DateTimeService dateTimeService) {
		this.dateTimeService = dateTimeService;
	}

	/**
	 * Constructor for frameworks.
	 */
	@SuppressWarnings("unused")
	SubscriptionExecutionSchedulerImpl() {
		// NOOP
	}

	@Override
	public Optional<SubscriptionExecutionEntity> scheduleNext(TriggeredSubscriptionEntity triggeredSubscription, SubscriptionExecutionEntity lastExecution) {
		if (!TRUE.equals(triggeredSubscription.getActive()) || !triggeredSubscription.getSubscription().isActive()) {
			return Optional.empty();
		}
		if (lastExecution == null) {
			return makeNextSubscriptionExecutionEntity(triggeredSubscription, calculateBaseTime(triggeredSubscription));
		}
		Objects.requireNonNull(triggeredSubscription.getSubscription().getExecution());
		Objects.requireNonNull(triggeredSubscription.getSubscription().getExecution().getFrequency());
		Objects.requireNonNull(triggeredSubscription.getSubscription().getExecution().getFrequencyUnit());
		if (Integer.valueOf(0).equals(triggeredSubscription.getSubscription().getExecution().getFrequency())) {
			return Optional.empty();
		}
		return makeNextSubscriptionExecutionEntity(triggeredSubscription, calculateTimeWithFrequency(triggeredSubscription, lastExecution));
	}

	private Optional<SubscriptionExecutionEntity> makeNextSubscriptionExecutionEntity(TriggeredSubscriptionEntity triggeredSubscription, Instant requestedTime) {
		SubscriptionExecutionEntity next = new SubscriptionExecutionEntity();
		next.setTriggeredSubscription(triggeredSubscription);
		Date now = dateTimeService.getNowAsDate();
		next.setCreationDate(now);
		next.setStatus(SubscriptionExecutionStatusType.PENDING);
		next.setRequestedTime(Date.from(requestedTime));
		return Optional.of(next);
	}

	private Instant calculateBaseTime(TriggeredSubscriptionEntity triggeredSubscription) {
		Instant requestedTime = dateTimeService.getNowAsInstant();
		if (FALSE.equals(triggeredSubscription.getSubscription().getExecution().getImmediate())) {
			// if not immediate, we need to adjust the requestedTime to
			// the next occurrence of timeExpression, which might be tomorrow
			LocalTime time = LocalTime.parse(triggeredSubscription.getSubscription().getExecution().getTimeExpression(), TIME_EXPRESSION_FORMAT);
			Instant timeToday = time.atDate(dateTimeService.getToday()).toInstant(ZoneOffset.UTC);
			requestedTime = timeToday.isAfter(requestedTime) ? timeToday : timeToday.plus(1L, ChronoUnit.DAYS);
		}
		return requestedTime;
	}

	private Instant calculateTimeWithFrequency(TriggeredSubscriptionEntity triggeredSubscription, SubscriptionExecutionEntity lastExecution) {
		Date previousRequestedTime = lastExecution.getRequestedTime();
		Objects.requireNonNull(previousRequestedTime);
		return previousRequestedTime.toInstant().plus(triggeredSubscription.getSubscription().getExecution().getFrequency().longValue(), triggeredSubscription.getSubscription().getExecution().getFrequencyUnit().getTemporalUnit());
	}
}
