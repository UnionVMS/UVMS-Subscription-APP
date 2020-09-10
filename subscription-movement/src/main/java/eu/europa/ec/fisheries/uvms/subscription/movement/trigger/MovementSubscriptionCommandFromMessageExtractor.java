/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2020.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subscription.movement.trigger;

import static eu.europa.ec.fisheries.uvms.subscription.service.trigger.TriggeredSubscriptionDataUtil.KEY_MOVEMENT_GUID_PREFIX;
import static eu.europa.fisheries.uvms.subscription.model.enums.TriggeredSubscriptionStatus.ACTIVE;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import eu.europa.ec.fisheries.schema.movement.common.v1.SimpleResponse;
import eu.europa.ec.fisheries.schema.movement.module.v1.CreateMovementBatchResponse;
import eu.europa.ec.fisheries.schema.movement.v1.MovementBaseType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementMetaDataAreaType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementTypeType;
import eu.europa.ec.fisheries.uvms.commons.message.impl.JAXBUtils;
import eu.europa.ec.fisheries.uvms.spatial.model.schemas.AreaExtendedIdentifierType;
import eu.europa.ec.fisheries.uvms.spatial.model.schemas.BatchSpatialEnrichmentRS;
import eu.europa.ec.fisheries.uvms.spatial.model.schemas.SpatialEnrichmentRSListElement;
import eu.europa.ec.fisheries.uvms.subscription.movement.mapper.MovementModelMapper;
import eu.europa.ec.fisheries.uvms.subscription.service.bean.Command;
import eu.europa.ec.fisheries.uvms.subscription.service.bean.SubscriptionFinder;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.TriggeredSubscriptionDataEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.TriggeredSubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.search.AreaCriterion;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.search.SubscriptionSearchCriteria.AssetCriterion;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.search.SubscriptionSearchCriteria.SenderCriterion;
import eu.europa.ec.fisheries.uvms.subscription.service.messaging.asset.AssetSender;
import eu.europa.ec.fisheries.uvms.subscription.service.trigger.StopConditionCriteria;
import eu.europa.ec.fisheries.uvms.subscription.service.trigger.SubscriptionCommandFromMessageExtractor;
import eu.europa.ec.fisheries.uvms.subscription.service.trigger.TriggerCommandsFactory;
import eu.europa.ec.fisheries.uvms.subscription.service.trigger.TriggeredSubscriptionDataUtil;
import eu.europa.ec.fisheries.uvms.subscription.service.util.DateTimeService;
import eu.europa.ec.fisheries.uvms.subscription.service.util.SequenceIntIterator;
import eu.europa.ec.fisheries.uvms.subscription.spatial.communication.SpatialSender;
import eu.europa.ec.fisheries.wsdl.subscription.module.AreaType;
import eu.europa.fisheries.uvms.subscription.model.enums.AssetType;
import eu.europa.fisheries.uvms.subscription.model.enums.TriggerType;
import eu.europa.fisheries.uvms.subscription.model.exceptions.MessageFormatException;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Implementation of {@link SubscriptionCommandFromMessageExtractor} for movement messages.
 */
@ApplicationScoped
public class MovementSubscriptionCommandFromMessageExtractor implements SubscriptionCommandFromMessageExtractor {

	private static final String SOURCE = "movement";
	private static final Predicate<TriggeredSubscriptionDataEntity> BY_KEY_MOVEMENT_GUID_PREFIX = d -> d.getKey().startsWith(KEY_MOVEMENT_GUID_PREFIX);

	private SubscriptionFinder subscriptionFinder;
	private DatatypeFactory datatypeFactory;
	private AssetSender assetSender;
	private SpatialSender spatialSender;
	private DateTimeService dateTimeService;
	private TriggerCommandsFactory triggerCommandsFactory;

	/**
	 * Constructor for injection.
	 *
	 * @param subscriptionFinder     The finder
	 * @param datatypeFactory        The data type factory
	 * @param assetSender            The asset sender
	 * @param spatialSender          The spatial sender
	 * @param dateTimeService        The date/time service
	 * @param triggerCommandsFactory The factory for commands
	 */
	@Inject
	public MovementSubscriptionCommandFromMessageExtractor(SubscriptionFinder subscriptionFinder, DatatypeFactory datatypeFactory, AssetSender assetSender,SpatialSender spatialSender, DateTimeService dateTimeService, TriggerCommandsFactory triggerCommandsFactory) {
		this.subscriptionFinder = subscriptionFinder;
		this.datatypeFactory = datatypeFactory;
		this.assetSender = assetSender;
		this.spatialSender = spatialSender;
		this.dateTimeService = dateTimeService;
		this.triggerCommandsFactory = triggerCommandsFactory;
	}

	/**
	 * Constructor for frameworks.
	 */
	@SuppressWarnings("unused")
	MovementSubscriptionCommandFromMessageExtractor() {
		// NOOP
	}

	@Override
	public String getEligibleSubscriptionSource() {
		return SOURCE;
	}

	@Override
	public Function<TriggeredSubscriptionEntity, Set<TriggeredSubscriptionDataEntity>> getDataForDuplicatesExtractor() {
		return TriggeredSubscriptionDataUtil::extractConnectIdAndMovementGuid;
	}

	@Override
	public void preserveDataFromSupersededTriggering(TriggeredSubscriptionEntity superseded, TriggeredSubscriptionEntity replacement) {
		processTriggering(superseded, replacement);
	}

	@Override
	public Stream<Command> extractCommands(String representation, SenderCriterion senderCriterion, ZonedDateTime receptionDateTime) {
		Map<String, Map<Long, TriggeredSubscriptionEntity>> movementGuidToTriggeringMap = new HashMap<>();
		List<MovementType> movementTypes = Stream.of(unmarshal(representation))
				.filter(message -> message.getResponse() == SimpleResponse.OK)
				.flatMap(message -> message.getMovements().stream())
				.filter(m -> !m.isDuplicate())
				.collect(Collectors.toList());
		enrichWithUserAreas(movementTypes);
		return movementTypes.stream()
				.flatMap(t -> makeCommandsForMovement(t, senderCriterion, movementGuidToTriggeringMap, receptionDateTime));
	}
	
	private CreateMovementBatchResponse unmarshal(String representation) {
		try {
			return JAXBUtils.unMarshallMessage(representation, CreateMovementBatchResponse.class);
		} catch (JAXBException e) {
			throw new MessageFormatException(e);
		}
	}

	private Stream<Command> makeCommandsForMovement(MovementType movement, SenderCriterion senderCriterion, Map<String, Map<Long, TriggeredSubscriptionEntity>> movementGuidToTriggeringsMap, ZonedDateTime receptionDateTime) {
		return Stream.concat(
				findTriggeredSubscriptions(movement, senderCriterion)
						.map(movementAndSubscription -> makeTriggeredSubscriptionEntity(movementAndSubscription, movementGuidToTriggeringsMap, receptionDateTime))
						.map(this::makeTriggerSubscriptionCommand),
				Stream.of(movement)
						.map(this::makeStopConditionCriteria)
						.map(triggerCommandsFactory::createStopSubscriptionCommand)
		);
	}

	private Stream<MovementAndSubscription> findTriggeredSubscriptions(MovementType movement, SenderCriterion senderCriterion) {
		if (movement.getPositionTime() == null) {
			return Stream.empty();
		}
		ZonedDateTime validAt = ZonedDateTime.ofInstant(movement.getPositionTime().toInstant(), ZoneId.of("UTC"));
		List<AreaCriterion> areas = extractAreas(movement).collect(Collectors.toList());
		List<AssetCriterion> assets = new ArrayList<>();
		assets.add(new AssetCriterion(AssetType.ASSET, movement.getConnectId()));
		List<String> assetGroupsForAsset = assetSender.findAssetGroupsForAsset(movement.getConnectId(), movement.getPositionTime());
		assets.addAll(assetGroupsForAsset.stream().map(a -> new AssetCriterion(AssetType.VGROUP, a)).collect(Collectors.toList()));
		return subscriptionFinder.findTriggeredSubscriptions(areas, assets, null, senderCriterion, validAt, Collections.singleton(TriggerType.INC_POSITION)).stream()
				.map(subscription -> new MovementAndSubscription(movement, subscription));
	}

	private Stream<AreaCriterion> extractAreas(MovementType movement) {
		return movement.getMetaData().getAreas().stream()
				.filter(area -> area.getTransitionType() != MovementTypeType.EXI)
				.map(area -> new AreaCriterion(AreaType.fromValue(area.getAreaType()), Long.valueOf(area.getRemoteId())));
	}

	private TriggeredSubscriptionEntity makeTriggeredSubscriptionEntity(MovementAndSubscription input, Map<String, Map<Long, TriggeredSubscriptionEntity>> movementGuidToTriggeringsMap, ZonedDateTime receptionDateTime) {
		TriggeredSubscriptionEntity result;
		Map<Long, TriggeredSubscriptionEntity> triggerings = movementGuidToTriggeringsMap.get(input.getMovement().getGuid());
		if (triggerings == null) {
			result = createNewTriggeredSubscriptionEntity(input, receptionDateTime);
			triggerings = new HashMap<>();
			triggerings.put(input.getSubscription().getId(), result);
			movementGuidToTriggeringsMap.put(input.getMovement().getGuid(), triggerings);
		} else {
			result = triggerings.get(input.getSubscription().getId());
			if (result == null) {
				result = createNewTriggeredSubscriptionEntity(input, receptionDateTime);
				triggerings.put(input.getSubscription().getId(), result);
			} else {
				addMovementGuidToTriggeredSubscriptionData(input, result.getData(), result);
			}
		}
		return result;
	}

	private TriggeredSubscriptionEntity createNewTriggeredSubscriptionEntity(MovementAndSubscription input, ZonedDateTime receptionDateTime) {
		TriggeredSubscriptionEntity result = new TriggeredSubscriptionEntity();
		result.setSubscription(input.getSubscription());
		result.setSource(SOURCE);
		result.setCreationDate(dateTimeService.getNowAsDate());
		result.setStatus(ACTIVE);
		result.setEffectiveFrom(Date.from(receptionDateTime.toInstant()));
		makeTriggeredSubscriptionData(result, input);
		return result;
	}

	private void makeTriggeredSubscriptionData(TriggeredSubscriptionEntity triggeredSubscription, MovementAndSubscription input) {
		Set<TriggeredSubscriptionDataEntity> result = new HashSet<>();
		Optional.ofNullable(input.getMovement().getConnectId()).ifPresent(connectId ->
				result.add(new TriggeredSubscriptionDataEntity(triggeredSubscription, TriggeredSubscriptionDataUtil.KEY_CONNECT_ID, connectId))
		);
		Optional.ofNullable(input.getMovement().getPositionTime()).ifPresent(positionTime -> {
			GregorianCalendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
			calendar.setTime(positionTime);
			XMLGregorianCalendar xmlCalendar = datatypeFactory.newXMLGregorianCalendar(calendar);
			result.add(new TriggeredSubscriptionDataEntity(triggeredSubscription, TriggeredSubscriptionDataUtil.KEY_OCCURRENCE, xmlCalendar.toXMLFormat()));
		});
		addMovementGuidToTriggeredSubscriptionData(input, result, triggeredSubscription);
		triggeredSubscription.setData(result);
	}

	private void addMovementGuidToTriggeredSubscriptionData(MovementAndSubscription movementAndSubscription, Set<TriggeredSubscriptionDataEntity> triggeredSubscriptionData, TriggeredSubscriptionEntity triggeredSubscription) {
		long nextIndex = triggeredSubscriptionData.stream()
				.filter(data -> data.getKey().startsWith(KEY_MOVEMENT_GUID_PREFIX))
				.count();
		Optional.ofNullable(movementAndSubscription.getMovement().getGuid())
				.ifPresent(movementGuid -> triggeredSubscriptionData.add(new TriggeredSubscriptionDataEntity(triggeredSubscription, KEY_MOVEMENT_GUID_PREFIX + nextIndex, movementGuid)));
	}

	private Command makeTriggerSubscriptionCommand(TriggeredSubscriptionEntity triggeredSubscription) {
		return triggerCommandsFactory.createTriggerSubscriptionFromSpecificMessageCommand(triggeredSubscription, getDataForDuplicatesExtractor(), this::processTriggering);
	}

	private boolean processTriggering(TriggeredSubscriptionEntity triggeredSubscriptionCandidate, TriggeredSubscriptionEntity existingTriggeredSubscription) {
		SequenceIntIterator indexes = new SequenceIntIterator((int) existingTriggeredSubscription.getData().stream().filter(BY_KEY_MOVEMENT_GUID_PREFIX).count());
		triggeredSubscriptionCandidate.getData().stream()
				.filter(BY_KEY_MOVEMENT_GUID_PREFIX)
				.map(data -> new TriggeredSubscriptionDataEntity(existingTriggeredSubscription, KEY_MOVEMENT_GUID_PREFIX + indexes.nextInt(), data.getValue()))
				.forEach(existingTriggeredSubscription.getData()::add);
		return true;
	}

	private StopConditionCriteria makeStopConditionCriteria(MovementType movement) {
		StopConditionCriteria criteria = new StopConditionCriteria();
		criteria.setConnectId(movement.getConnectId());
		criteria.setAreas(extractAreas(movement).collect(Collectors.toSet()));
		return criteria;
	}

	private void enrichWithUserAreas(List<MovementType> movementTypes) {
		BatchSpatialEnrichmentRS response = spatialSender.getBatchUserAreasEnrichment(MovementModelMapper.movementTypesToSubscriptionAreaSimpleTypes(movementTypes));
		if(response != null) {
			Map<String,MovementType> movementTypeMap = movementTypes.stream().collect(Collectors.toMap(MovementBaseType::getGuid, Function.identity()));
			List<SpatialEnrichmentRSListElement> rsListElements = response.getEnrichmentRespLists();
			for(SpatialEnrichmentRSListElement rsListElement : rsListElements) {
				List<AreaExtendedIdentifierType> areas = rsListElement.getAreasByLocation().getAreas();
				List<MovementMetaDataAreaType> movementMetaDataAreaTypes = MovementModelMapper.mapSpatialAreaToMovementAreas(areas);
				movementTypeMap.get(rsListElement.getGuid()).getMetaData().getAreas().addAll(movementMetaDataAreaTypes);
			}
		}
	}
	
	@Getter
	@AllArgsConstructor
	private static class MovementAndSubscription {
		private final MovementType movement;
		private final SubscriptionEntity subscription;
	}
}
