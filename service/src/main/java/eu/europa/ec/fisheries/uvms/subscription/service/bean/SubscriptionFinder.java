/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subscription.service.bean;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;

import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.search.SubscriptionSearchCriteria.AreaCriterion;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.search.SubscriptionSearchCriteria.AssetCriterion;
import eu.europa.fisheries.uvms.subscription.model.enums.TriggerType;

/**
 * Service to search for triggered subscriptions under various cases.
 */
public interface SubscriptionFinder {

	/**
	 * Find active subscriptions that are triggered by any of the given areas and assets.
	 *
	 * @param areas   The areas
	 * @param assets   The areas
	 * @param validAt The assets must be valid at this time
	 * @param triggerTypes The subscription trigger is any of these, {@code null} to ignore the criterion
	 * @return A non-null list of subscriptions
	 */
	List<SubscriptionEntity> findTriggeredSubscriptions(Collection<AreaCriterion> areas, Collection<AssetCriterion> assets, @Valid @NotNull ZonedDateTime validAt, Collection<TriggerType> triggerTypes);
}
