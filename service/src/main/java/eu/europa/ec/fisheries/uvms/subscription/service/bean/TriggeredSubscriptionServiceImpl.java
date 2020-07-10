/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subscription.service.bean;

import static eu.europa.fisheries.uvms.subscription.model.enums.TriggeredSubscriptionStatus.ACTIVE;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Stream;

import eu.europa.ec.fisheries.uvms.subscription.service.dao.TriggeredSubscriptionDao;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.TriggeredSubscriptionDataEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.TriggeredSubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.search.TriggeredSubscriptionSearchCriteria;
import eu.europa.ec.fisheries.uvms.subscription.service.trigger.StopConditionCriteria;
import eu.europa.ec.fisheries.uvms.subscription.service.trigger.TriggeredSubscriptionDataUtil;

/**
 * Implementation of the {@link TriggeredSubscriptionService}.
 */
@ApplicationScoped
@Transactional
class TriggeredSubscriptionServiceImpl implements TriggeredSubscriptionService {

	private TriggeredSubscriptionDao triggeredSubscriptionDao;

	/**
	 * Injection constructor.
	 *
	 * @param triggeredSubscriptionDao The DAO
	 */
	@Inject
	public TriggeredSubscriptionServiceImpl(TriggeredSubscriptionDao triggeredSubscriptionDao) {
		this.triggeredSubscriptionDao = triggeredSubscriptionDao;
	}

	/**
	 * Constructor for frameworks.
	 */
	@SuppressWarnings("unused")
	TriggeredSubscriptionServiceImpl() {
		// NOOP
	}

	@Override
	public TriggeredSubscriptionEntity save(TriggeredSubscriptionEntity triggeredSubscription) {
		return triggeredSubscriptionDao.create(triggeredSubscription);
	}

	@Override
	public boolean isDuplicate(TriggeredSubscriptionEntity entity, Set<TriggeredSubscriptionDataEntity> dataForDuplicates) {
		return triggeredSubscriptionDao.activeExists(entity.getSubscription(), dataForDuplicates);
	}

	@Override
	public Stream<TriggeredSubscriptionEntity> findAlreadyActivated(TriggeredSubscriptionEntity entity, Set<TriggeredSubscriptionDataEntity> dataForDuplicates) {
		return triggeredSubscriptionDao.findAlreadyActivated(entity.getSubscription(), dataForDuplicates);
	}

	@Override
	public Stream<TriggeredSubscriptionEntity> findByStopConditionCriteria(StopConditionCriteria criteria) {
		TriggeredSubscriptionSearchCriteria searchCriteriaForAreas = new TriggeredSubscriptionSearchCriteria();
		searchCriteriaForAreas.setSingleStatus(ACTIVE);
		searchCriteriaForAreas.setTriggeredSubscriptionData(Collections.singletonMap(TriggeredSubscriptionDataUtil.KEY_CONNECT_ID, criteria.getConnectId()));
		searchCriteriaForAreas.setNotInAreas(criteria.getAreas());
		searchCriteriaForAreas.setSubscriptionQuitArea(criteria.getAreas() != null);
		searchCriteriaForAreas.setWithAnyStopActivities(criteria.getActivities());
		return triggeredSubscriptionDao.find(searchCriteriaForAreas);
	}
}
