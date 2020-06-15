/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2020.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subscription.activity.communication;

import eu.europa.ec.fisheries.uvms.activity.model.schemas.CreateAndSendFAQueryForTripRequest;
import eu.europa.ec.fisheries.uvms.activity.model.schemas.CreateAndSendFAQueryForVesselRequest;
import eu.europa.ec.fisheries.uvms.activity.model.schemas.CreateAndSendFAQueryResponse;

/**
 * Low-level client to interesting Activity module services.
 */
public interface ActivityClient {

    /**
     * Call @{@code CREATE_AND_SEND_FA_QUERY_FOR_VESSEL}
     *
     * @param request The activity module request
     * @return The activity module response
     */
    CreateAndSendFAQueryResponse sendRequest(CreateAndSendFAQueryForVesselRequest request);

    /**
     * Call @{@code CREATE_AND_SEND_FA_QUERY_FOR_TRIP}
     *
     * @param request The activity module request
     * @return The activity module response
     */
    CreateAndSendFAQueryResponse sendRequest(CreateAndSendFAQueryForTripRequest request);
}
