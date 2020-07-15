/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subscription.rules.communication;

import static eu.europa.ec.fisheries.uvms.subscription.service.trigger.TriggeredSubscriptionDataUtil.KEY_MOVEMENT_GUID_PREFIX;
import static eu.europa.ec.fisheries.uvms.subscription.service.trigger.TriggeredSubscriptionDataUtil.KEY_REPORT_ID_PREFIX;

import eu.europa.ec.fisheries.schema.rules.module.v1.CreateTicketRequest;
import eu.europa.ec.fisheries.schema.rules.module.v1.RulesModuleMethod;
import eu.europa.ec.fisheries.schema.rules.ticket.v1.TicketStatusType;
import eu.europa.ec.fisheries.schema.rules.ticket.v1.TicketType;
import eu.europa.ec.fisheries.uvms.subscription.service.bean.SubscriptionActivityService;
import eu.europa.ec.fisheries.wsdl.asset.types.VesselIdentifiersHolder;
import eu.europa.fisheries.uvms.subscription.model.enums.TriggerType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Implementation of {@link RulesSender}.
 */
@ApplicationScoped
class RulesSenderImpl implements RulesSender {

	private static final Logger LOG = LoggerFactory.getLogger(RulesSenderImpl.class);
	private static final String FORMAT = "yyyy-MM-dd HH:mm:ss Z";

	private RulesClient rulesClient;
	private SubscriptionActivityService subscriptionActivityService;

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
	public RulesSenderImpl(RulesClient rulesClient,SubscriptionActivityService subscriptionActivityService) {
		this.rulesClient = rulesClient;
		this.subscriptionActivityService = subscriptionActivityService;
	}

	@Override
	public void createNotificationsAsync(TriggerType triggerType, String subscriptionName, Date openDate, VesselIdentifiersHolder vesselIdentifiers, Map<String,String> dataMap) {
		List<TicketType> ticketTypes = null;
		switch (triggerType){
			case INC_POSITION:
				ticketTypes = incPositionTicketTypes(subscriptionName,openDate,vesselIdentifiers,dataMap);
				break;
			case INC_FA_REPORT:
				ticketTypes = faReportTicketTypes(subscriptionName,openDate,vesselIdentifiers,dataMap);
				break;
			case INC_FA_QUERY:
			case SCHEDULER:
			case MANUAL:
			default:
				break;
		}
		if(ticketTypes == null || ticketTypes.isEmpty()) {
			return;
		}
		CreateTicketRequest createTicketRequest = new CreateTicketRequest();
		createTicketRequest.getTickets().addAll(ticketTypes);
		createTicketRequest.setMethod(RulesModuleMethod.CREATE_TICKETS_REQUEST);
		rulesClient.sendAsyncRequest(createTicketRequest);
	}

	private static List<TicketType> incPositionTicketTypes(String subscriptionName, Date openDate, VesselIdentifiersHolder vesselIdentifiers, Map<String,String> dataMap){
		List<String> movementIds = dataMap.entrySet().stream()
				.filter(entry -> entry.getKey().startsWith(KEY_MOVEMENT_GUID_PREFIX))
				.map(Map.Entry::getValue)
				.collect(Collectors.toList());
		if (movementIds.isEmpty()) {
			movementIds.add(UUID.randomUUID().toString());
		}
		return movementIds.stream()
				.map(toTicketType(subscriptionName, openDate, vesselIdentifiers, dataMap))
				.collect(Collectors.toList());
	}

	private List<TicketType> faReportTicketTypes(String subscriptionName, Date openDate, VesselIdentifiersHolder vesselIdentifiers, Map<String,String> dataMap){
		List<String> reportIds = dataMap.entrySet().stream()
				.filter(entry -> entry.getKey().startsWith(KEY_REPORT_ID_PREFIX))
				.map(Map.Entry::getValue)
				.collect(Collectors.toList());
		if(reportIds.isEmpty()) {
			LOG.warn("Report Id list was empty,not creating TicketTypes");
			return null;
		}
		/* find movement guid list */
		List<String> movementGuids = subscriptionActivityService.findMovementGuidsByReportIdsAndAssetGuid(reportIds,vesselIdentifiers.getAssetGuid());
		return movementGuids.stream()
				.map(toTicketType(subscriptionName, openDate, vesselIdentifiers, dataMap))
				.collect(Collectors.toList());
	}

	private static Function<String, TicketType> toTicketType(String subscriptionName, Date openDate, VesselIdentifiersHolder vesselIdentifiers, Map<String,String> dataMap) {
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
