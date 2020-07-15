/*
 *
 *  Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2020.
 *
 *  This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 *  and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 *  the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 *  details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package eu.europa.ec.fisheries.uvms.subscription.service.bean;

import java.util.List;

/**
 * Implementation of this service is responsible for subscription-activity service communication
 */
public interface SubscriptionActivityService {
    /**
     * Asks activity module to map given report id list with its associated occurrence date
     * @param reportIds The report id list
     * @param assetGuid Asset guid
     * @return List of movement guids
     */
    List<String> findMovementGuidsByReportIdsAndAssetGuid(List<String> reportIds,String assetGuid);
}
