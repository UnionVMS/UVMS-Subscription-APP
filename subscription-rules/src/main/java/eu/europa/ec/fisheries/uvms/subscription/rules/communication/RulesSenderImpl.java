/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subscription.rules.communication;

import eu.europa.ec.fisheries.schema.rules.module.v1.CreateTicketRequest;
import eu.europa.ec.fisheries.schema.rules.module.v1.RulesModuleMethod;
import eu.europa.ec.fisheries.schema.rules.ticket.v1.TicketStatusType;
import eu.europa.ec.fisheries.schema.rules.ticket.v1.TicketType;
import eu.europa.ec.fisheries.wsdl.asset.types.VesselIdentifiersHolder;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

/**
 * Implementation of {@link RulesSender}.
 */
@ApplicationScoped
class RulesSenderImpl implements RulesSender {

	private static final String FORMAT = "yyyy-MM-dd HH:mm:ss Z";

	private RulesClient rulesClient;

	/**
	 * Constructor for frameworks.
	 */
	@SuppressWarnings("unused")
	RulesSenderImpl(){
		//NOOP
	}

	/**
	 * Injection constructor
	 *
	 * @param rulesClient The low-level client to the services of the rules module
	 */
	@Inject
	public RulesSenderImpl(RulesClient rulesClient) {
		this.rulesClient = rulesClient;
	}

	@Override
	public void createAlertsAsync(String subscriptionName, Date openDate, VesselIdentifiersHolder vesselIdentifiers, List<String> movementGuids) {
		CreateTicketRequest createTicketRequest = new CreateTicketRequest();
		createTicketRequest.setMethod(RulesModuleMethod.CREATE_TICKETS_REQUEST);
		movementGuids.stream()
				.map(toTicketType(subscriptionName, openDate, vesselIdentifiers))
				.forEach(createTicketRequest.getTickets()::add);
		rulesClient.sendAsyncRequest(createTicketRequest);
	}

	private static Function<String, TicketType> toTicketType(String subscriptionName, Date openDate, VesselIdentifiersHolder vesselIdentifiers) {
		return moveGuid -> {
			TicketType ticketType = new TicketType();
			ticketType.setAssetGuid(vesselIdentifiers.getAssetGuid());
			ticketType.setOpenDate(new SimpleDateFormat(FORMAT).format(openDate));
			ticketType.setRuleName(subscriptionName);
			ticketType.setGuid(UUID.randomUUID().toString());
			ticketType.setStatus(TicketStatusType.OPEN);
			ticketType.setMovementGuid(moveGuid);
			ticketType.setUpdatedBy("UVMS");
			return ticketType;
		};
	}
}
