/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subscription.service.bean;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import eu.europa.ec.fisheries.uvms.subscription.service.dao.SubscriptionDao;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.search.SubscriptionSearchCriteria.AreaCriterion;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.search.SubscriptionSearchCriteriaImpl;
import eu.europa.fisheries.uvms.subscription.model.enums.TriggerType;

/**
 * Implementation of {@link SubscriptionFinder} that translates the business cases for each search
 * to criteria to pass to the DAO.
 */
@ApplicationScoped
class SubscriptionFinderImpl implements SubscriptionFinder {

	private SubscriptionDao dao;

	/**
	 * Injection constructor.
	 *
	 * @param dao The DAO
	 */
	@Inject
	public SubscriptionFinderImpl(SubscriptionDao dao) {
		this.dao = dao;
	}

	/**
	 * Constructor for frameworks.
	 */
	@SuppressWarnings("unused")
	SubscriptionFinderImpl() {
		// NOOP
	}

	@Override
	public List<SubscriptionEntity> findSubscriptionsTriggeredByAreas(Collection<AreaCriterion> areas, @Valid @NotNull ZonedDateTime validAt, Collection<TriggerType> triggerTypes) {
		if (areas == null || areas.isEmpty()) {
			return Collections.emptyList();
		}
		SubscriptionSearchCriteriaImpl criteria = new SubscriptionSearchCriteriaImpl();
		criteria.setActive(true);
		criteria.setValidAt(validAt);
		criteria.setInAnyArea(areas);
		criteria.setWithAnyTriggerType(triggerTypes);
		return dao.listSubscriptions(criteria);
	}
}