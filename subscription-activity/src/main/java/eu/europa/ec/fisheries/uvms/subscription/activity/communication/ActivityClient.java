/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2020.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subscription.activity.communication;

import eu.europa.ec.fisheries.uvms.activity.model.schemas.ActivityModuleRequest;

/**
 * Low-level client to interesting Activity module services.
 */
public interface ActivityClient {

    /**
     * Common usage
     *
     * @param request The activity module request
     * @param responseClass The class to be expected on unmarshall
     * @return The activity module response
     */
    <T> T sendRequest(ActivityModuleRequest request, Class<T> responseClass);

    /**
     * Send a request for which the response will be sent to our normal incoming queue asynchronously.
     * The request is sent in the same transaction as the caller.
     *
     * @param request The activity module request
     */
    void sendAsyncRequest(ActivityModuleRequest request);
}
