/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subscription.activity.execution;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.xml.datatype.DatatypeFactory;
import java.time.ZonedDateTime;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.Objects;

import eu.europa.ec.fisheries.uvms.activity.model.schemas.ActivityModuleMethod;
import eu.europa.ec.fisheries.uvms.activity.model.schemas.CreateAndSendFAQueryRequest;
import eu.europa.ec.fisheries.uvms.activity.model.schemas.PluginType;
import eu.europa.ec.fisheries.uvms.subscription.activity.communication.ActivitySender;
import eu.europa.ec.fisheries.uvms.subscription.activity.communication.ReceiverAndDataflow;
import eu.europa.ec.fisheries.uvms.subscription.activity.communication.UsmSender;
import eu.europa.ec.fisheries.uvms.subscription.service.dao.TriggeredSubscriptionDao;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.TriggeredSubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.execution.TriggeredSubscriptionExecutor;
import eu.europa.fisheries.uvms.subscription.model.enums.OutgoingMessageType;

/**
 * Implementation of {@link TriggeredSubscriptionExecutor} for executing FA queries.
 */
@ApplicationScoped
public class FaQueryTriggeredSubscriptionExecutor implements TriggeredSubscriptionExecutor {

	private TriggeredSubscriptionDao triggeredSubscriptionDao;
	private ActivitySender activitySender;
	private UsmSender usmSender;
	private DatatypeFactory datatypeFactory;

	/**
	 * Constructor for dependency injection.
	 *
	 * @param triggeredSubscriptionDao The triggered subscription DAO
	 * @param activitySender The service for communicating with activity
	 * @param usmSender The service for communicating with USM
	 * @param datatypeFactory The XML helper object
	 */
	@Inject
	public FaQueryTriggeredSubscriptionExecutor(TriggeredSubscriptionDao triggeredSubscriptionDao, ActivitySender activitySender, UsmSender usmSender, DatatypeFactory datatypeFactory) {
		this.triggeredSubscriptionDao = triggeredSubscriptionDao;
		this.activitySender = activitySender;
		this.usmSender = usmSender;
		this.datatypeFactory = datatypeFactory;
	}

	/**
	 * Constructor for frameworks.
	 */
	@SuppressWarnings("unused")
	FaQueryTriggeredSubscriptionExecutor() {
		// NOOP
	}

	@Override
	public void execute(Long id) {
		execute(triggeredSubscriptionDao.getById(id));
	}

	@Override
	public void execute(TriggeredSubscriptionEntity triggeredSubscription) {
		SubscriptionEntity subscription = triggeredSubscription.getSubscription();
		if (subscription.getOutput().getMessageType() == OutgoingMessageType.FA_QUERY) {
			ReceiverAndDataflow receiverAndDataflow = usmSender.findReceiverAndDataflow(subscription.getOutput().getSubscriber().getEndpointId(), subscription.getOutput().getSubscriber().getChannelId());
			Map<String, String> dataMap = toDataMap(triggeredSubscription);
			String vesselId = dataMap.get("vesselId"); // TODO make these constants
			Objects.requireNonNull(vesselId, "vesselId not found in data of " + triggeredSubscription.getId());
			String vesselSchemeId = dataMap.get("vesselSchemeId");
			Objects.requireNonNull(vesselSchemeId, "vesselSchemeId not found in data of " + triggeredSubscription.getId());
			String occurrence = dataMap.get("occurrence");
			Objects.requireNonNull(occurrence, "occurrence not found in data of " + triggeredSubscription.getId());
			ZonedDateTime occurrenceZdt = datatypeFactory.newXMLGregorianCalendar(occurrence).toGregorianCalendar().toZonedDateTime();
			ZonedDateTime startDate = occurrenceZdt.minus(subscription.getOutput().getHistory(), subscription.getOutput().getHistoryUnit().getTemporalUnit());
			CreateAndSendFAQueryRequest message = new CreateAndSendFAQueryRequest(
					ActivityModuleMethod.CREATE_AND_SEND_FA_QUERY,
					PluginType.FLUX,
					vesselId,
					vesselSchemeId,
					subscription.getOutput().getConsolidated(),
					datatypeFactory.newXMLGregorianCalendar(GregorianCalendar.from(startDate)),
					datatypeFactory.newXMLGregorianCalendar(GregorianCalendar.from(occurrenceZdt)),
					receiverAndDataflow.getReceiver(),
					receiverAndDataflow.getDataflow()
			);
			activitySender.send(message);
		}
	}
}
