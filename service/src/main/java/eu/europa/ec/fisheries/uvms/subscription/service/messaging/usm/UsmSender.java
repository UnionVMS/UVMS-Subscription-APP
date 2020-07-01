/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2020.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subscription.service.messaging.usm;

import eu.europa.ec.fisheries.wsdl.user.types.OrganisationEndpointAndChannelId;

/**
 * Service to communicate with the User module.
 */
public interface UsmSender {
	/**
	 * Map the given endpoint and channel ids to the receiver symbolic name and dataflow URN.
	 *
	 * @param endpointId The endpoint id
	 * @param channelId The channel id
	 */
	ReceiverAndDataflow findReceiverAndDataflow(Long endpointId, Long channelId);

	/**
	 * Find organization data by dataflow and endpoint.
	 *
	 * @param dataflow     The dataflow
	 * @param endpoint     The endpoint
	 * @return The result, or {@code null} if not found
	 */
	OrganisationEndpointAndChannelId findOrganizationByDataFlowAndEndpoint(String dataflow, String endpoint);
}
