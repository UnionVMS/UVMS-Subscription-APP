/*
﻿Developed with the contribution of the European Commission - Directorate General for Maritime Affairs and Fisheries
© European Union, 2015-2016.

This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can
redistribute it and/or modify it under the terms of the GNU General Public License as published by the
Free Software Foundation, either version 3 of the License, or any later version. The IFDM Suite is distributed in
the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details. You should have received a
copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
*/
package eu.europa.ec.fisheries.uvms.subscription.rules.execution;

import static eu.europa.ec.fisheries.uvms.subscription.service.trigger.TriggeredSubscriptionDataUtil.KEY_MOVEMENT_GUID_PREFIX;
import static eu.europa.ec.fisheries.uvms.subscription.service.trigger.TriggeredSubscriptionDataUtil.KEY_REPORT_ID_PREFIX;

import eu.europa.ec.fisheries.uvms.subscription.rules.communication.RulesSender;
import eu.europa.ec.fisheries.uvms.subscription.service.bean.SubscriptionActivityService;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionExecutionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.TriggeredSubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.execution.SubscriptionExecutor;
import eu.europa.ec.fisheries.uvms.subscription.service.messaging.asset.AssetSender;
import eu.europa.ec.fisheries.uvms.subscription.service.trigger.TriggeredSubscriptionDataUtil;
import eu.europa.ec.fisheries.wsdl.asset.types.VesselIdentifiersHolder;
import eu.europa.fisheries.uvms.subscription.model.enums.TriggerType;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Implementation of {@link SubscriptionExecutor} for creating alerts.
 */
@ApplicationScoped
public class AlarmsSubscriptionExecutor implements SubscriptionExecutor {

    private RulesSender rulesSender;
    private AssetSender assetSender;
    private SubscriptionActivityService subscriptionActivityService;

    @Inject
    public AlarmsSubscriptionExecutor(RulesSender rulesSender, AssetSender assetSender, SubscriptionActivityService subscriptionActivityService) {
        this.rulesSender = rulesSender;
        this.assetSender = assetSender;
        this.subscriptionActivityService = subscriptionActivityService;
    }

    /**
     * Constructor for frameworks.
     */
    @SuppressWarnings("unused")
    AlarmsSubscriptionExecutor() {
        // NOOP
    }

    @Override
    public void execute(SubscriptionExecutionEntity execution) {
        TriggeredSubscriptionEntity triggeredSubscription = execution.getTriggeredSubscription();
        SubscriptionEntity subscription = triggeredSubscription.getSubscription();
        if(subscription.getOutput().isAlert()) {
            Map<String,String> dataMap = toDataMap(triggeredSubscription);
            String connectId = dataMap.get(TriggeredSubscriptionDataUtil.KEY_CONNECT_ID);
            Objects.requireNonNull(connectId, "connectId not found in data of " + triggeredSubscription.getId());
            VesselIdentifiersHolder vesselIdentifiers = assetSender.findVesselIdentifiers(connectId);
            Objects.requireNonNull(vesselIdentifiers.getAssetGuid(), "asset GUID null for connectId " + connectId + " of " + triggeredSubscription.getId());
            List<String> movementGuids = determineMovementGuids(subscription.getExecution().getTriggerType(), vesselIdentifiers, dataMap);
            if (!movementGuids.isEmpty()) {
                rulesSender.createAlertsAsync(subscription.getName(), triggeredSubscription.getEffectiveFrom(), vesselIdentifiers, movementGuids);
            }
        }
    }

    private List<String> determineMovementGuids(TriggerType triggerType, VesselIdentifiersHolder vesselIdentifiers, Map<String,String> dataMap) {
        switch (triggerType) {
            case INC_POSITION:
                return determineMovementGuidsForIncomingPosition(dataMap);
            case INC_FA_REPORT:
                return determineMovementGuidsForIncomingFaReport(vesselIdentifiers, dataMap);
            default:
                return Collections.emptyList();
        }
    }

	private static List<String> determineMovementGuidsForIncomingPosition(Map<String,String> dataMap){
		return dataMap.entrySet().stream()
				.filter(entry -> entry.getKey().startsWith(KEY_MOVEMENT_GUID_PREFIX))
				.map(Map.Entry::getValue)
				.collect(Collectors.toList());
	}

	private List<String> determineMovementGuidsForIncomingFaReport(VesselIdentifiersHolder vesselIdentifiers, Map<String,String> dataMap){
		List<String> reportIds = dataMap.entrySet().stream()
				.filter(entry -> entry.getKey().startsWith(KEY_REPORT_ID_PREFIX))
				.map(Map.Entry::getValue)
				.collect(Collectors.toList());
		if(reportIds.isEmpty()) {
			return Collections.emptyList();
		}
		return subscriptionActivityService.findMovementGuidsByReportIdsAndAssetGuid(reportIds,vesselIdentifiers.getAssetGuid());
	}
}
