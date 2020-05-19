/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subscription.activity.execution;

import static java.lang.Boolean.TRUE;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.xml.datatype.DatatypeFactory;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import eu.europa.ec.fisheries.uvms.activity.model.schemas.ActivityModuleMethod;
import eu.europa.ec.fisheries.uvms.activity.model.schemas.CreateAndSendFAQueryRequest;
import eu.europa.ec.fisheries.uvms.activity.model.schemas.PluginType;
import eu.europa.ec.fisheries.uvms.activity.model.schemas.VesselIdentifierSchemeIdEnum;
import eu.europa.ec.fisheries.uvms.activity.model.schemas.VesselIdentifierType;
import eu.europa.ec.fisheries.uvms.subscription.activity.communication.ActivitySender;
import eu.europa.ec.fisheries.uvms.subscription.activity.communication.ReceiverAndDataflow;
import eu.europa.ec.fisheries.uvms.subscription.activity.communication.UsmSender;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionExecutionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.TriggeredSubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.execution.SubscriptionExecutor;
import eu.europa.ec.fisheries.uvms.subscription.service.messaging.asset.AssetSender;
import eu.europa.ec.fisheries.wsdl.asset.types.VesselIdentifiersHolder;
import eu.europa.fisheries.uvms.subscription.model.enums.OutgoingMessageType;
import eu.europa.fisheries.uvms.subscription.model.enums.SubscriptionVesselIdentifier;

/**
 * Implementation of {@link SubscriptionExecutor} for executing FA queries.
 */
@ApplicationScoped
public class FaQueryTriggeredSubscriptionExecutor implements SubscriptionExecutor {

	private ActivitySender activitySender;
	private UsmSender usmSender;
	private AssetSender assetSender;
	private DatatypeFactory datatypeFactory;

	/**
	 * Constructor for dependency injection.
	 *
	 * @param activitySender The service for communicating with activity
	 * @param usmSender The service for communicating with USM
	 * @param datatypeFactory The XML helper object
	 */
	@Inject
	public FaQueryTriggeredSubscriptionExecutor(ActivitySender activitySender, UsmSender usmSender, AssetSender assetSender, DatatypeFactory datatypeFactory) {
		this.activitySender = activitySender;
		this.usmSender = usmSender;
		this.assetSender = assetSender;
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
	public void execute(SubscriptionExecutionEntity execution) {
		TriggeredSubscriptionEntity triggeredSubscription = execution.getTriggeredSubscription();
		SubscriptionEntity subscription = triggeredSubscription.getSubscription();
		if (subscription.getOutput().getMessageType() == OutgoingMessageType.FA_QUERY) {
			ReceiverAndDataflow receiverAndDataflow = usmSender.findReceiverAndDataflow(subscription.getOutput().getSubscriber().getEndpointId(), subscription.getOutput().getSubscriber().getChannelId());
			Map<String, String> dataMap = toDataMap(triggeredSubscription);
			String connectId = dataMap.get("connectId"); // TODO make these constants
			Objects.requireNonNull(connectId, "connectId not found in data of " + triggeredSubscription.getId());
			List<VesselIdentifierType> vesselIdentifiers = new ArrayList<>();
			VesselIdentifiersHolder idsHolder = assetSender.findVesselIdentifiers(connectId);
			if(idsHolder.getCfr() != null && subscription.getOutput().getVesselIds().contains(SubscriptionVesselIdentifier.CFR)){
				vesselIdentifiers.add(new VesselIdentifierType(VesselIdentifierSchemeIdEnum.CFR, idsHolder.getCfr()));
			}
			if(idsHolder.getIrcs() != null && subscription.getOutput().getVesselIds().contains(SubscriptionVesselIdentifier.IRCS)){
				vesselIdentifiers.add(new VesselIdentifierType(VesselIdentifierSchemeIdEnum.IRCS, idsHolder.getIrcs()));
			}
			if(idsHolder.getIccat() != null && subscription.getOutput().getVesselIds().contains(SubscriptionVesselIdentifier.ICCAT)){
				vesselIdentifiers.add(new VesselIdentifierType(VesselIdentifierSchemeIdEnum.ICCAT, idsHolder.getIccat()));
			}
			if(idsHolder.getUvi() != null && subscription.getOutput().getVesselIds().contains(SubscriptionVesselIdentifier.UVI)){
				vesselIdentifiers.add(new VesselIdentifierType(VesselIdentifierSchemeIdEnum.UVI, idsHolder.getUvi()));
			}
			if(idsHolder.getExtMark() != null && subscription.getOutput().getVesselIds().contains(SubscriptionVesselIdentifier.EXT_MARK)){
				vesselIdentifiers.add(new VesselIdentifierType(VesselIdentifierSchemeIdEnum.EXT_MARK, idsHolder.getExtMark()));
			}
			String occurrence = dataMap.get("occurrence");
			Objects.requireNonNull(occurrence, "occurrence not found in data of " + triggeredSubscription.getId());
			ZonedDateTime occurrenceZdt = datatypeFactory.newXMLGregorianCalendar(occurrence).toGregorianCalendar().toZonedDateTime();
			ZonedDateTime startDate = occurrenceZdt.minus(subscription.getOutput().getHistory(), subscription.getOutput().getHistoryUnit().getTemporalUnit());
			CreateAndSendFAQueryRequest message = new CreateAndSendFAQueryRequest(
					ActivityModuleMethod.CREATE_AND_SEND_FA_QUERY,
					PluginType.FLUX,
					vesselIdentifiers,
					TRUE.equals(subscription.getOutput().getConsolidated()),
					datatypeFactory.newXMLGregorianCalendar(GregorianCalendar.from(startDate)),
					datatypeFactory.newXMLGregorianCalendar(GregorianCalendar.from(occurrenceZdt)),
					receiverAndDataflow.getReceiver(),
					receiverAndDataflow.getDataflow()
			);
			activitySender.send(message);
		}
	}
}
