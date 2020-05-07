/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

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
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import eu.europa.ec.fisheries.schema.movement.common.v1.SimpleResponse;
import eu.europa.ec.fisheries.schema.movement.module.v1.CreateMovementBatchResponse;
import eu.europa.ec.fisheries.schema.movement.v1.MovementMetaDataAreaType;
import eu.europa.ec.fisheries.schema.movement.v1.MovementType;
import eu.europa.ec.fisheries.uvms.commons.message.impl.JAXBUtils;
import eu.europa.ec.fisheries.uvms.subscription.service.bean.SubscriptionFinder;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.TriggeredSubscriptionDataEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.TriggeredSubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.search.SubscriptionSearchCriteria.AreaCriterion;
import eu.europa.ec.fisheries.uvms.subscription.service.trigger.TriggeredSubscriptionCreator;
import eu.europa.ec.fisheries.wsdl.subscription.module.AreaType;
import eu.europa.fisheries.uvms.subscription.model.enums.TriggerType;
import eu.europa.fisheries.uvms.subscription.model.exceptions.MessageFormatException;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Implementation of {@link TriggeredSubscriptionCreator} for movement messages.
 */
@ApplicationScoped
public class MovementTriggeredSubscriptionCreator implements TriggeredSubscriptionCreator {

	private static final String SOURCE = "movement";

	private SubscriptionFinder subscriptionFinder;
	private DatatypeFactory datatypeFactory;

	/**
	 * Constructor for injection.
	 *
	 * @param subscriptionFinder The finder
	 */
	@Inject
	public MovementTriggeredSubscriptionCreator(SubscriptionFinder subscriptionFinder, DatatypeFactory datatypeFactory) {
		this.subscriptionFinder = subscriptionFinder;
		this.datatypeFactory = datatypeFactory;
	}

	/**
	 * Constructor for frameworks.
	 */
	@SuppressWarnings("unused")
	MovementTriggeredSubscriptionCreator() {
		// NOOP
	}

	@Override
	public String getEligibleSubscriptionSource() {
		return SOURCE;
	}

	@Override
	public Stream<TriggeredSubscriptionEntity> createTriggeredSubscriptions(String representation) {
		CreateMovementBatchResponse message = unmarshal(representation);
		if (message.getResponse() != SimpleResponse.OK || message.getMovements() == null) {
			return Stream.empty();
		}
		return message.getMovements().stream()
				.filter(m -> !m.isDuplicate())
				.flatMap(this::findTriggeredSubscriptions)
				.map(this::makeTriggeredSubscriptionEntity);
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
		return subscriptionFinder.findSubscriptionsTriggeredByAreas(areas, validAt, Collections.singleton(TriggerType.INC_POSITION)).stream()
				.map(subscription -> new MovementAndSubscription(movement, subscription));
	}

	private AreaCriterion toAreaCriterion(MovementMetaDataAreaType area) {
		return new AreaCriterion(AreaType.fromValue(area.getAreaType()), Long.valueOf(area.getRemoteId()));
	}

	private TriggeredSubscriptionEntity makeTriggeredSubscriptionEntity(MovementAndSubscription input) {
		TriggeredSubscriptionEntity result = new TriggeredSubscriptionEntity();
		result.setSubscription(input.getSubscription());
		result.setSource(SOURCE);
		result.setCreationDate(new Date());
		result.setData(makeTriggeredSubscriptionData(result, input));
		return result;
	}

	private Set<TriggeredSubscriptionDataEntity> makeTriggeredSubscriptionData(TriggeredSubscriptionEntity triggeredSubscription, MovementAndSubscription input) {
		Set<TriggeredSubscriptionDataEntity> result = new HashSet<>();
		Optional.ofNullable(input.getMovement().getAssetId()).ifPresent(assetId -> {
			result.add(new TriggeredSubscriptionDataEntity(triggeredSubscription, "vesselId", assetId.getValue()));
			result.add(new TriggeredSubscriptionDataEntity(triggeredSubscription, "vesselSchemeId", assetId.getIdType().toString()));
		});
		Optional.ofNullable(input.getMovement().getPositionTime()).ifPresent(positionTime -> {
			GregorianCalendar calendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
			calendar.setTime(positionTime);
			XMLGregorianCalendar xmlCalendar = datatypeFactory.newXMLGregorianCalendar(calendar);
			result.add(new TriggeredSubscriptionDataEntity(triggeredSubscription, "occurrence", xmlCalendar.toXMLFormat()));
		});
		return result;
	}

	@Getter
	@AllArgsConstructor
	private static class MovementAndSubscription {
		private final MovementType movement;
		private final SubscriptionEntity subscription;
	}
}
