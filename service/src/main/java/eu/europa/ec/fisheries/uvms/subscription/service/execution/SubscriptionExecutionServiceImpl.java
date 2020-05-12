/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subscription.service.execution;

import static eu.europa.fisheries.uvms.subscription.model.enums.SubscriptionExecutionStatusType.EXECUTED;
import static eu.europa.fisheries.uvms.subscription.model.enums.SubscriptionExecutionStatusType.QUEUED;
import static javax.transaction.Transactional.TxType.REQUIRES_NEW;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Stream;

import eu.europa.ec.fisheries.uvms.subscription.service.dao.SubscriptionExecutionDao;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionExecutionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.scheduling.SubscriptionExecutionScheduler;
import eu.europa.ec.fisheries.uvms.subscription.service.util.DateTimeService;
import eu.europa.fisheries.uvms.subscription.model.exceptions.EntityDoesNotExistException;

/**
 * Implementation of the {@link SubscriptionExecutionService}.
 */
@ApplicationScoped
class SubscriptionExecutionServiceImpl implements SubscriptionExecutionService {

	private SubscriptionExecutionDao dao;
	private SubscriptionExecutionSender subscriptionExecutionSender;
	private DateTimeService dateTimeService;
	private Instance<SubscriptionExecutor> subscriptionExecutorInstance;
	private SubscriptionExecutionScheduler subscriptionExecutionScheduler;


	/**
	 * Constructor for injection.
	 *
	 * @param dao The DAO
	 * @param subscriptionExecutionSender The execution queue sender
	 * @param dateTimeService             The date/time service
	 * @param subscriptionExecutorInstance   The executors
	 * @param subscriptionExecutionScheduler The scheduler
	 */
	@Inject
	public SubscriptionExecutionServiceImpl(SubscriptionExecutionDao dao, SubscriptionExecutionSender subscriptionExecutionSender, DateTimeService dateTimeService, Instance<SubscriptionExecutor> subscriptionExecutorInstance, SubscriptionExecutionScheduler subscriptionExecutionScheduler) {
		this.dao = dao;
		this.subscriptionExecutionSender = subscriptionExecutionSender;
		this.dateTimeService = dateTimeService;
		this.subscriptionExecutorInstance = subscriptionExecutorInstance;
		this.subscriptionExecutionScheduler = subscriptionExecutionScheduler;
	}

	/**
	 * Constructor for frameworks.
	 */
	@SuppressWarnings("unused")
	SubscriptionExecutionServiceImpl() {
		// NOOP
	}

	@Override
	public SubscriptionExecutionEntity save(SubscriptionExecutionEntity entity) {
		return dao.create(entity);
	}

	@Override
	public Stream<Long> findPendingSubscriptionExecutionIds(Date activationDate) {
		return dao.findIdsOfPendingWithRequestDateBefore(activationDate);
	}

	@Transactional(REQUIRES_NEW)
	@Override
	public void enqueueForExecutionInNewTransaction(Long executionId) {
		SubscriptionExecutionEntity entity = Optional.ofNullable(dao.findById(executionId)).orElseThrow(() -> new EntityDoesNotExistException("SubscriptionExecutionEntity with id " + executionId));
		entity.setQueuedTime(dateTimeService.getNowAsDate());
		entity.setStatus(QUEUED);
		subscriptionExecutionSender.enqueue(entity);
	}

	@Override
	public void execute(Long id) {
		Optional.ofNullable(dao.findById(id))
				.filter(execution -> execution.getStatus() == QUEUED && execution.getTriggeredSubscription().getActive() && execution.getTriggeredSubscription().getSubscription().isActive())
				.flatMap(execution -> {
					subscriptionExecutorInstance.stream().forEach(executor -> executor.execute(execution));
					execution.setStatus(EXECUTED);
					execution.setExecutionTime(dateTimeService.getNowAsDate());
					return subscriptionExecutionScheduler.scheduleNext(execution.getTriggeredSubscription(), execution);
				})
				.ifPresent(this::save);
	}
}
