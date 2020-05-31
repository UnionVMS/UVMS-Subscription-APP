/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2020.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subscription.movement.trigger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import eu.europa.ec.fisheries.schema.movement.common.v1.SimpleResponse;
import eu.europa.ec.fisheries.schema.movement.module.v1.CreateMovementBatchResponse;
import eu.europa.ec.fisheries.schema.movement.v1.MovementMetaDataAreaType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementType;
import eu.europa.ec.fisheries.uvms.commons.message.impl.JAXBUtils;
import eu.europa.ec.fisheries.uvms.subscription.service.bean.Command;
import eu.europa.ec.fisheries.uvms.subscription.service.bean.SubscriptionFinder;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.TriggeredSubscriptionDataEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.TriggeredSubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.search.SubscriptionSearchCriteria.AssetCriterion;
import eu.europa.ec.fisheries.uvms.subscription.service.messaging.asset.AssetSender;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.search.AreaCriterion;
import eu.europa.ec.fisheries.uvms.subscription.service.trigger.SubscriptionCommandFromMessageExtractor;
import eu.europa.ec.fisheries.uvms.subscription.service.trigger.TriggerCommandsFactory;
import eu.europa.ec.fisheries.uvms.subscription.service.util.DateTimeService;
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

	private static final String KEY_CONNECT_ID = "connectId";
	private static final String KEY_OCCURRENCE = "occurrence";

	private static final Function<TriggeredSubscriptionEntity,Set<TriggeredSubscriptionDataEntity>> TRIGGERED_SUBSCRIPTION_DATA_FOR_DUPLICATES = entity ->
		entity.getData().stream()
				.filter(d -> KEY_CONNECT_ID.equals(d.getKey()))
				.collect(Collectors.toSet());

	private static final String SOURCE = "movement";

	private SubscriptionFinder subscriptionFinder;
	private DatatypeFactory datatypeFactory;
	private AssetSender assetSender;
	private DateTimeService dateTimeService;
	private TriggerCommandsFactory triggerCommandsFactory;

	/**
	 * Constructor for injection.
	 *
	 * @param subscriptionFinder The finder
	 * @param datatypeFactory The data type factory
	 * @param assetSender The asset sender
	 * @param dateTimeService The date/time service
	 * @param triggerCommandsFactory The factory for commands
	 */
	@Inject
	public MovementSubscriptionCommandFromMessageExtractor(SubscriptionFinder subscriptionFinder, DatatypeFactory datatypeFactory, AssetSender assetSender, DateTimeService dateTimeService, TriggerCommandsFactory triggerCommandsFactory) {
		this.subscriptionFinder = subscriptionFinder;
		this.datatypeFactory = datatypeFactory;
		this.assetSender = assetSender;
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
	public Stream<Command> extractCommands(String representation) {
		return Stream.of(unmarshal(representation))
				.filter(message -> message.getResponse() == SimpleResponse.OK)
				.filter(message -> message.getMovements() != null)
				.flatMap(message -> message.getMovements().stream())
				.filter(m -> !m.isDuplicate())
				.flatMap(this::findTriggeredSubscriptions)
				.map(this::makeTriggeredSubscriptionEntity)
				.map(this::makeTriggerSubscriptionCommand);
	}

	private CreateMovementBatchResponse unmarshal(String representation) {
		try {
			return JAXBUtils.unMarshallMessage(representation, CreateMovementBatchResponse.class);
		} catch (JAXBException e) {
			throw new MessageFormatException(e);
		}
	}

	private Stream<MovementAndSubscription> findTriggeredSubscriptions(MovementType movement) {
		if (movement.getPositionTime() == null) {
			return Stream.empty();
		}
		ZonedDateTime validAt = ZonedDateTime.ofInstant(movement.getPositionTime().toInstant(), ZoneId.of("UTC"));
		List<AreaCriterion> areas = movement.getMetaData().getAreas().stream()
				.map(this::toAreaCriterion)
				.collect(Collectors.toList());
		List<AssetCriterion> assets = new ArrayList<>();
		assets.add(new AssetCriterion(AssetType.ASSET, movement.getConnectId()));
		List<String> assetGroupsForAsset = assetSender.findAssetGroupsForAsset(movement.getConnectId(), movement.getPositionTime());
		assets.addAll(assetGroupsForAsset.stream().map(a -> new AssetCriterion(AssetType.VGROUP, a)).collect(Collectors.toList()));
		return subscriptionFinder.findTriggeredSubscriptions(areas, assets, validAt, Collections.singleton(TriggerType.INC_POSITION)).stream()
				.map(subscription -> new MovementAndSubscription(movement, subscription));
	}

	private AreaCriterion toAreaCriterion(MovementMetaDataAreaType area) {
		return new AreaCriterion(AreaType.fromValue(area.getAreaType()), Long.valueOf(area.getRemoteId()));
	}

	private TriggeredSubscriptionEntity makeTriggeredSubscriptionEntity(MovementAndSubscription input) {
		TriggeredSubscriptionEntity result = new TriggeredSubscriptionEntity();
		result.setSubscription(input.getSubscription());
		result.setSource(SOURCE);
		result.setCreationDate(dateTimeService.getNowAsDate());
		result.setActive(true);
		result.setEffectiveFrom(input.getMovement().getPositionTime());
		result.setData(makeTriggeredSubscriptionData(result, input));
		return result;
	}

	private Set<TriggeredSubscriptionDataEntity> makeTriggeredSubscriptionData(TriggeredSubscriptionEntity triggeredSubscription, MovementAndSubscription input) {
		Set<TriggeredSubscriptionDataEntity> result = new HashSet<>();
		Optional.ofNullable(input.getMovement().getConnectId()).ifPresent(connectId ->
			result.add(new TriggeredSubscriptionDataEntity(triggeredSubscription, KEY_CONNECT_ID, connectId))
		);
		Optional.ofNullable(input.getMovement().getPositionTime()).ifPresent(positionTime -> {
			GregorianCalendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
			calendar.setTime(positionTime);
			XMLGregorianCalendar xmlCalendar = datatypeFactory.newXMLGregorianCalendar(calendar);
			result.add(new TriggeredSubscriptionDataEntity(triggeredSubscription, KEY_OCCURRENCE, xmlCalendar.toXMLFormat()));
		});
		return result;
	}

	private Command makeTriggerSubscriptionCommand(TriggeredSubscriptionEntity triggeredSubscription) {
		return triggerCommandsFactory.createTriggerSubscriptionCommand(triggeredSubscription, TRIGGERED_SUBSCRIPTION_DATA_FOR_DUPLICATES);
	}

	@Getter
	@AllArgsConstructor
	private static class MovementAndSubscription {
		private final MovementType movement;
		private final SubscriptionEntity subscription;
	}
}
