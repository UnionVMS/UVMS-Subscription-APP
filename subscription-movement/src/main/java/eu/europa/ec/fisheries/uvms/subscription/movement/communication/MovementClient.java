/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2020.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subscription.movement.communication;

import eu.europa.ec.fisheries.schema.movement.module.v1.FilterGuidListByAreaAndDateRequest;
import eu.europa.ec.fisheries.schema.movement.module.v1.FilterGuidListByAreaAndDateResponse;
import eu.europa.ec.fisheries.schema.movement.module.v1.ForwardPositionRequest;
import eu.europa.ec.fisheries.schema.movement.module.v1.ForwardPositionResponse;

/**
 * Low-level client to interesting Movement module services.
 */
public interface MovementClient {

    /**
     * Call @{@code FILTER_GUID_LIST_FOR_DATE_BY_AREA}
     *
     * @param request The movement module request
     * @return The movement module response
     */
    FilterGuidListByAreaAndDateResponse filterGuidListForDateByArea(FilterGuidListByAreaAndDateRequest request);

    /**
     * Call @{@code FORWARD_POSITION}
     *
     * @param request the movement module request
     * @return The movement module response
     */
    ForwardPositionResponse forwardPosition(ForwardPositionRequest request);
}
