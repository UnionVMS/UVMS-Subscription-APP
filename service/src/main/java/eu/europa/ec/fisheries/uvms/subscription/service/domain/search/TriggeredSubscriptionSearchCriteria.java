/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subscription.service.domain.search;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import eu.europa.fisheries.uvms.subscription.model.enums.TriggeredSubscriptionStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Criteria for searching triggered subscriptions.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TriggeredSubscriptionSearchCriteria {
	private Set<TriggeredSubscriptionStatus> withStatus;
	private Boolean subscriptionQuitArea;
	private Map<String,String> triggeredSubscriptionData;
	private Set<AreaCriterion> notInAreas;

	public void setSingleStatus(TriggeredSubscriptionStatus singleStatus) {
		setWithStatus(Collections.singleton(singleStatus));
	}
}
