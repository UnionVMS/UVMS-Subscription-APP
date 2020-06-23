/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2020.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subscription.service.messaging.usm;

import java.util.List;

import eu.europa.ec.fisheries.wsdl.user.module.FindOrganisationByEndpointAndChannelRequest;
import eu.europa.ec.fisheries.wsdl.user.module.FindOrganisationByEndpointAndChannelResponse;
import eu.europa.ec.fisheries.wsdl.user.types.EndPoint;
import eu.europa.ec.fisheries.wsdl.user.types.Organisation;


/**
 * Client to interesting USM services.
 */
public interface UsmClient {
	/**
	 * Retrieve all organisations.
	 *
	 * @param scopeName name of user scope
	 * @param roleName role name of user
	 * @param requester requester value
	 * @return A list of organisations
	 */
	List<Organisation> getAllOrganisations(String scopeName, String roleName, String requester);

	/**
	 * Retrieve the endpoint with the given id.
	 *
	 * @param endpointId The endpoint id
	 * @return The endpoint
	 */
	EndPoint findEndpoint(Long endpointId);

	/**
	 * Find organization data by dataflow and endpoint name. The {@code UserModuleMethod.FIND_ORGANISATION_BY_ENDPOINT_AND_CHANNEL} method.
	 *
	 * @param request The request
	 * @return The response
	 */
	FindOrganisationByEndpointAndChannelResponse findOrganisationByEndpointAndChannel(FindOrganisationByEndpointAndChannelRequest request);
}
