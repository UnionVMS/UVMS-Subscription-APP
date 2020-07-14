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

import eu.europa.ec.fisheries.schema.rules.alarm.v1.AlarmItemType;
import eu.europa.ec.fisheries.schema.rules.alarm.v1.AlarmReportType;
import eu.europa.ec.fisheries.schema.rules.alarm.v1.AlarmStatusType;
import eu.europa.ec.fisheries.schema.rules.asset.v1.AssetId;
import eu.europa.ec.fisheries.schema.rules.asset.v1.AssetIdList;
import eu.europa.ec.fisheries.schema.rules.asset.v1.AssetIdType;
import eu.europa.ec.fisheries.schema.rules.asset.v1.AssetType;
import eu.europa.ec.fisheries.schema.rules.module.v1.CreateAlarmsReportRequest;
import eu.europa.ec.fisheries.schema.rules.module.v1.RulesModuleMethod;
import eu.europa.ec.fisheries.schema.rules.movement.v1.RawMovementType;
import eu.europa.ec.fisheries.uvms.subscription.service.trigger.TriggeredSubscriptionDataUtil;
import eu.europa.ec.fisheries.wsdl.asset.types.VesselIdentifiersHolder;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.text.DateFormat;
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

	private static final String FORMAT = "yyyy-MM-dd HH:mm:ss Z";

	private RulesClient rulesClient;

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
	public void createAlarmReportAsync(String subscriptionName, Date openDate, VesselIdentifiersHolder vesselIdentifiers, Map<String,String> dataMap) {
		List<String> movementIds = dataMap.entrySet().stream()
				.filter(entry -> entry.getKey().startsWith(KEY_MOVEMENT_GUID_PREFIX))
				.map(Map.Entry::getValue)
				.collect(Collectors.toList());
		if (movementIds.isEmpty()) {
			movementIds.add(UUID.randomUUID().toString());
		}

		List<AlarmReportType> alarmReports = movementIds.stream()
				.map(toAlarmReportType(subscriptionName, openDate, vesselIdentifiers, dataMap))
				.collect(Collectors.toList());

		CreateAlarmsReportRequest createAlarmReportRequest = new CreateAlarmsReportRequest();
		createAlarmReportRequest.getAlarm().addAll(alarmReports);
		createAlarmReportRequest.setMethod(RulesModuleMethod.CREATE_ALARMS_REPORT_REQUEST);
		rulesClient.sendAsyncRequest(createAlarmReportRequest);
	}

	private Function<String, AlarmReportType> toAlarmReportType(String subscriptionName, Date openDate, VesselIdentifiersHolder vesselIdentifiers, Map<String,String> dataMap) {
		return moveGuid -> {
			AssetId assetId = new AssetId();
			assetId.setAssetType(AssetType.VESSEL);
			addVesselIdIfNotNull(assetId, AssetIdType.CFR, vesselIdentifiers.getCfr());
			addVesselIdIfNotNull(assetId, AssetIdType.IRCS, vesselIdentifiers.getIrcs());

			RawMovementType rawMovement = new RawMovementType();
			rawMovement.setAssetId(assetId);
			rawMovement.setGuid(moveGuid);
			rawMovement.setConnectId(dataMap.get(TriggeredSubscriptionDataUtil.KEY_CONNECT_ID));
			rawMovement.setExternalMarking(vesselIdentifiers.getExtMark());

			AlarmItemType alarmItem = new AlarmItemType();
			alarmItem.setRuleName(subscriptionName);
			alarmItem.setGuid(UUID.randomUUID().toString());

			AlarmReportType alarmReport = new AlarmReportType();
			alarmReport.setRawMovement(rawMovement);
			alarmReport.setStatus(AlarmStatusType.OPEN);
			if (openDate != null) {
				DateFormat df = new SimpleDateFormat(FORMAT);
				alarmReport.setOpenDate(df.format(openDate));
			}
			alarmReport.setUpdatedBy("UVMS");
			alarmReport.setAssetGuid(vesselIdentifiers.getAssetGuid());
			alarmReport.getAlarmItem().add(alarmItem);

			return alarmReport;
		};
	}

	private void addVesselIdIfNotNull(AssetId assetId, AssetIdType assetIdType, String value) {
		if (value != null) {
			AssetIdList assetIdList = new AssetIdList();
			assetIdList.setIdType(assetIdType);
			assetIdList.setValue(value);
			assetId.getAssetIdList().add(assetIdList);
		}
	}
}
