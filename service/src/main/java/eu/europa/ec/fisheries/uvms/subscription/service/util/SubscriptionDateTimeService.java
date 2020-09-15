/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subscription.service.util;

import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionOutput;
import java.time.ZonedDateTime;

/**
 * Subscription Date and time utilities.
 * <p>
 * This class abstracts the current time facilities of the system, to
 * help with testing time-sensitive pieces of code.
 */
public interface SubscriptionDateTimeService {

    /**
     * Calculates the start date
	 * @param output Will use its queryPeriod or history unit data
     * @param endDate Will be used in case outputs queryPeriod is null
     * @return The calculated start date
     */
	ZonedDateTime calculateStartDate(SubscriptionOutput output, ZonedDateTime endDate);

	/**
	 * Calculates the end date
	 * @param output Will use its queryPeriods endDate
	 * @param occurrence Will be used in case outputs queryPeriod is null
	 * @return The calculated end date
	 */
	ZonedDateTime calculateEndDate(SubscriptionOutput output, String occurrence);

	/**
	 * Calculates the end date, occurrence will be calculated if query period not set on output
	 * @param output Will use its queryPeriods endDate
	 * @return The calculated end date
	 */
	ZonedDateTime calculateEndDate(SubscriptionOutput output);

}
