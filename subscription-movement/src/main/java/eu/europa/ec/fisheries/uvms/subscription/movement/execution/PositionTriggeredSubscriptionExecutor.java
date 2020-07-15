/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subscription.movement.execution;

import static eu.europa.ec.fisheries.uvms.subscription.service.trigger.TriggeredSubscriptionDataUtil.KEY_MOVEMENT_GUID_PREFIX;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import eu.europa.ec.fisheries.uvms.subscription.movement.communication.MovementSender;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionExecutionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.TriggeredSubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.execution.SubscriptionExecutor;
import eu.europa.ec.fisheries.uvms.subscription.service.messaging.asset.AssetSender;
import eu.europa.ec.fisheries.uvms.subscription.service.messaging.usm.ReceiverAndDataflow;
import eu.europa.ec.fisheries.uvms.subscription.service.messaging.usm.UsmSender;
import eu.europa.ec.fisheries.wsdl.asset.types.VesselIdentifiersHolder;
import eu.europa.fisheries.uvms.subscription.model.enums.OutgoingMessageType;
import eu.europa.fisheries.uvms.subscription.model.enums.SubscriptionVesselIdentifier;

/**
 * Implementation of {@link SubscriptionExecutor} for executing Forward Position queries.
 */
@ApplicationScoped
public class PositionTriggeredSubscriptionExecutor implements SubscriptionExecutor {

    private static final String KEY_CONNECT_ID = "connectId";

    private MovementSender movementSender;
    private AssetSender assetSender;
    private UsmSender usmSender;

    /**
     * Constructor for dependency injection.
     *
     * @param movementSender The service for communicating with movement
     * @param assetSender    The service for communication with asset
     * @param usmSender      The service for communicating with USM
     */
    @Inject
    public PositionTriggeredSubscriptionExecutor(MovementSender movementSender, AssetSender assetSender, UsmSender usmSender) {
        this.movementSender = movementSender;
        this.assetSender = assetSender;
        this.usmSender = usmSender;
    }

    /**
     * Constructor for frameworks.
     */
    @SuppressWarnings("unused")
    PositionTriggeredSubscriptionExecutor() {
        // NOOP
    }

    @Override
    public void execute(SubscriptionExecutionEntity execution) {
        TriggeredSubscriptionEntity triggeredSubscription = execution.getTriggeredSubscription();
        SubscriptionEntity subscription = triggeredSubscription.getSubscription();
        if (subscription.getOutput().getMessageType() == OutgoingMessageType.POSITION) {
            ReceiverAndDataflow receiverAndDataflow = usmSender.findReceiverAndDataflow(subscription.getOutput().getSubscriber().getEndpointId(), subscription.getOutput().getSubscriber().getChannelId());
            Map<String, String> dataMap = toDataMap(triggeredSubscription);
            Map<String, String> vesselIdentifiers = new HashMap<>();
            VesselIdentifiersHolder idsHolder = extractVesselIds(triggeredSubscription, dataMap);
            populateVesselIdentifierList(subscription, vesselIdentifiers, idsHolder);
            String vesselCountry = idsHolder.getCountryCode();
            List<String> movementGuids = extractMovementGuids(dataMap);
            String generatedMessageId = movementSender.forwardPosition(
                    vesselIdentifiers,
                    vesselCountry,
                    movementGuids,
                    receiverAndDataflow.getReceiver(),
                    receiverAndDataflow.getDataflow());
            execution.getMessageIds().add(generatedMessageId);
        }
    }

    private List<String> extractMovementGuids(Map<String, String> dataMap) {
        return dataMap.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(KEY_MOVEMENT_GUID_PREFIX))
                .sorted(Comparator.comparingInt(e -> Integer.parseInt(e.getKey().substring(KEY_MOVEMENT_GUID_PREFIX.length()))))
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }

    private VesselIdentifiersHolder extractVesselIds(TriggeredSubscriptionEntity triggeredSubscription, Map<String, String> dataMap) {
        String connectId = dataMap.get(KEY_CONNECT_ID);
        Objects.requireNonNull(connectId, "connectId not found in data of " + triggeredSubscription.getId());
        return assetSender.findVesselIdentifiers(connectId);
    }

    private void populateVesselIdentifierList(SubscriptionEntity subscription, Map<String, String> vesselIdentifiers, VesselIdentifiersHolder idsHolder) {
        addIdentifier(subscription, vesselIdentifiers, idsHolder.getCfr(), SubscriptionVesselIdentifier.CFR);
        addIdentifier(subscription, vesselIdentifiers, idsHolder.getIrcs(), SubscriptionVesselIdentifier.IRCS);
        addIdentifier(subscription, vesselIdentifiers, idsHolder.getIccat(), SubscriptionVesselIdentifier.ICCAT);
        addIdentifier(subscription, vesselIdentifiers, idsHolder.getUvi(), SubscriptionVesselIdentifier.UVI);
        addIdentifier(subscription, vesselIdentifiers, idsHolder.getExtMark(), SubscriptionVesselIdentifier.EXT_MARK);
    }

    private void addIdentifier(SubscriptionEntity subscription, Map<String, String> vesselIdentifiers, String identifier, SubscriptionVesselIdentifier configuredSchemeId) {
        if (identifier != null && subscription.getOutput().getVesselIds().contains(configuredSchemeId)) {
            vesselIdentifiers.put(configuredSchemeId.name(), identifier);
        }
    }
}