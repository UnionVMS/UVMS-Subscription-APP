/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2020.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subscription.service.messaging.usm;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import java.util.Optional;

import eu.europa.ec.fisheries.wsdl.user.module.FindOrganisationByEndpointAndChannelRequest;
import eu.europa.ec.fisheries.wsdl.user.module.FindOrganisationByEndpointAndChannelResponse;
import eu.europa.ec.fisheries.wsdl.user.module.UserModuleMethod;
import eu.europa.ec.fisheries.wsdl.user.types.Channel;
import eu.europa.ec.fisheries.wsdl.user.types.EndPoint;
import eu.europa.ec.fisheries.wsdl.user.types.OrganisationEndpointAndChannelId;
import eu.europa.fisheries.uvms.subscription.model.exceptions.EntityDoesNotExistException;

/**
 * Implementation of the {@link UsmSender}.
 */
@ApplicationScoped
class UsmSenderImpl implements UsmSender {

	private UsmClient usmClient;

	/**
	 * Injection constructor.
	 *
	 * @param usmClient The USM client
	 */
	@Inject
	public UsmSenderImpl(UsmClient usmClient) {
		this.usmClient = usmClient;
	}

	/**
	 * Constructor for frameworks.
	 */
	@SuppressWarnings("unused")
	UsmSenderImpl() {
		// NOOP
	}

	@Override
	public ReceiverAndDataflow findReceiverAndDataflow(Long endpointId, Long channelId) {
		EndPoint endpoint = usmClient.findEndpoint(endpointId);
		if(endpoint == null){
			throw new EntityDoesNotExistException("Endpoint with id " + endpoint + " not found.");
		}
		String dataflow = endpoint.getChannels().stream()
				.filter(channel -> channel.getId() == channelId)
				.map(Channel::getDataFlow)
				.findFirst()
				.orElseThrow(() -> new EntityDoesNotExistException("Cannot find channelId " + channelId + " under endpoint " + endpointId));
		return new ReceiverAndDataflow(endpoint.getUri(), dataflow);
	}

	@Override
	public OrganisationEndpointAndChannelId findOrganizationByDataFlowAndEndpointName(String dataflow, String endpointName) {
		FindOrganisationByEndpointAndChannelRequest request = new FindOrganisationByEndpointAndChannelRequest();
		request.setMethod(UserModuleMethod.FIND_ORGANISATION_BY_ENDPOINT_AND_CHANNEL);
		request.setChannelDataFlow(dataflow);
		request.setEndpointName(endpointName);
		return Optional.ofNullable(usmClient.findOrganisationByEndpointAndChannel(request))
				.map(FindOrganisationByEndpointAndChannelResponse::getResult)
				.orElse(null);
	}
}
