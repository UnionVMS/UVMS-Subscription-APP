/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2020.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subscription.movement.trigger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.xml.datatype.DatatypeFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;

import eu.europa.ec.fisheries.uvms.subscription.service.bean.Command;
import eu.europa.ec.fisheries.uvms.subscription.service.bean.SubscriptionFinder;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.TriggeredSubscriptionDataEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.TriggeredSubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.search.SubscriptionSearchCriteria.AssetCriterion;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.search.SubscriptionSearchCriteria.SenderCriterion;
import eu.europa.ec.fisheries.uvms.subscription.service.messaging.asset.AssetSender;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.search.AreaCriterion;
import eu.europa.ec.fisheries.uvms.subscription.service.trigger.StopConditionCriteria;
import eu.europa.ec.fisheries.uvms.subscription.service.trigger.TriggerCommandsFactory;
import eu.europa.fisheries.uvms.subscription.model.enums.TriggerType;
import eu.europa.fisheries.uvms.subscription.model.enums.TriggeredSubscriptionStatus;
import eu.europa.fisheries.uvms.subscription.model.exceptions.MessageFormatException;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests for the {@link MovementSubscriptionCommandFromMessageExtractor}.
 */
@EnableAutoWeld
@ExtendWith(MockitoExtension.class)
public class MovementSubscriptionCommandFromMessageExtractorTest {

	private static final Date NOW = new Date();
	private static final ZonedDateTime RECEPTION_DT = ZonedDateTime.parse("2020-07-14T12:33:44Z", DateTimeFormatter.ISO_ZONED_DATE_TIME);

	@Produces @Mock
	private SubscriptionFinder subscriptionFinder;

	@Produces @Mock
	private AssetSender assetSender;

	@Produces @ApplicationScoped
	private final DateTimeServiceTestImpl dateTimeService = new DateTimeServiceTestImpl();

	@Produces @Mock
	private TriggerCommandsFactory triggerCommandsFactory;

	@Inject
	private MovementSubscriptionCommandFromMessageExtractor sut;

	@Produces
	DatatypeFactory getDatatypeFactory() throws Exception {
		return DatatypeFactory.newInstance();
	}

	@Test
	void testEmptyConstructor() {
		MovementSubscriptionCommandFromMessageExtractor sut = new MovementSubscriptionCommandFromMessageExtractor();
		assertNotNull(sut);
	}

	@Test
	void testGetEligibleSubscriptionSource() {
		assertEquals("movement", sut.getEligibleSubscriptionSource());
	}

	@Test
	void testPreserveDataFromSupersededTriggering() {
		TriggeredSubscriptionEntity superseded = new TriggeredSubscriptionEntity();
		TriggeredSubscriptionEntity replacement = new TriggeredSubscriptionEntity();
		superseded.getData().add(new TriggeredSubscriptionDataEntity(superseded, "movementGuidIndex_12", "value"));
		superseded.getData().add(new TriggeredSubscriptionDataEntity(superseded, "irrelevant", "42"));
		replacement.getData().add(new TriggeredSubscriptionDataEntity(replacement, "irrelevant", "43"));

		sut.preserveDataFromSupersededTriggering(superseded, replacement);

		assertEquals(2, replacement.getData().size());
		assertEquals(2, superseded.getData().size());
		TriggeredSubscriptionDataEntity copiedData = replacement.getData().stream().filter(x -> x.getKey().startsWith("movementGuidIndex_")).findFirst().get();
		assertSame(replacement, copiedData.getTriggeredSubscription());
		assertEquals("movementGuidIndex_0", copiedData.getKey());
		assertEquals("value", copiedData.getValue());
		TriggeredSubscriptionDataEntity originalData = superseded.getData().stream().filter(x -> x.getKey().startsWith("movementGuidIndex_")).findFirst().get();
		assertSame(superseded, originalData.getTriggeredSubscription());
	}

	@Test
	void testJAXBExceptionResultsInApplicationException() {
		assertThrows(MessageFormatException.class, () -> sut.extractCommands("bad",null, RECEPTION_DT));
	}

	@Test
	void testNOKReturnsEmptyStream() {
		verifyEmptyStreamForResource("CreateMovementBatchResponse-NOK.xml");
	}

	@Test
	void testDoNotTriggerOnDuplicateMovements() {
		verifyEmptyStreamForResource("CreateMovementBatchResponse-OK-duplicate.xml");
	}

	@Test
	void testNoPositionTime() {
		String representation = readResource("CreateMovementBatchResponse-OK-null-position-time.xml");
		List<Command> commands = sut.extractCommands(representation,null, RECEPTION_DT).collect(Collectors.toList());

		assertEquals(1, commands.size());
		ArgumentCaptor<StopConditionCriteria> stopConditionCriteriaArgumentCaptor = ArgumentCaptor.forClass(StopConditionCriteria.class);
		verify(triggerCommandsFactory).createStopSubscriptionCommand(stopConditionCriteriaArgumentCaptor.capture());
		StopConditionCriteria stopConditionCriteria = stopConditionCriteriaArgumentCaptor.getValue();
		assertEquals("93b63a1c-45ea-11e7-bec7-4c32759615eb", stopConditionCriteria.getConnectId());
		String notInAreaCriteria = stopConditionCriteria.getAreas().stream().map(ac -> ac.getType().toString() + '-' + ac.getGid()).sorted().collect(Collectors.joining(","));
		assertEquals("FMZ-270,STATRECT-73", notInAreaCriteria);
		assertNull(stopConditionCriteria.getActivities());
	}

	private void verifyEmptyStreamForResource(String resourceName) {
		String representation = readResource(resourceName);
		long size = sut.extractCommands(representation,null, RECEPTION_DT).count();
		assertEquals(0, size);
		verifyNoInteractions(subscriptionFinder);
	}

	@Test
	void testDoNotTriggerOnOKResponseAndNoMovements() {
		verifyEmptyStreamForResource("CreateMovementBatchResponse-OK-no-movements.xml");
	}

	@Test
	void testOK() {
		SubscriptionEntity subscription = new SubscriptionEntity();
		when(subscriptionFinder.findTriggeredSubscriptions(any(), any(), any(), any(), any(), any())).thenReturn(Collections.singletonList(subscription));
		String representation = readResource("CreateMovementBatchResponse-OK.xml");
		dateTimeService.setNow(NOW);
		SenderCriterion senderCriterion = new SenderCriterion(1L, 2L, 3L);

		List<Command> commands = sut.extractCommands(representation, senderCriterion, RECEPTION_DT).collect(Collectors.toList());

		assertEquals(2, commands.size());
		ArgumentCaptor<TriggeredSubscriptionEntity> triggeredSubscriptionCaptor = ArgumentCaptor.forClass(TriggeredSubscriptionEntity.class);
		@SuppressWarnings("unchecked")
		ArgumentCaptor<Function<TriggeredSubscriptionEntity,Set<TriggeredSubscriptionDataEntity>>> dataExtractorCaptor = ArgumentCaptor.forClass(Function.class);
		@SuppressWarnings("unchecked")
		ArgumentCaptor<BiPredicate<TriggeredSubscriptionEntity, TriggeredSubscriptionEntity>> processTriggeringCaptor = ArgumentCaptor.forClass(BiPredicate.class);
		verify(triggerCommandsFactory).createTriggerSubscriptionFromSpecificMessageCommand(triggeredSubscriptionCaptor.capture(), dataExtractorCaptor.capture(), processTriggeringCaptor.capture());
		TriggeredSubscriptionEntity triggeredSubscription = triggeredSubscriptionCaptor.getValue();
		assertSame(subscription, triggeredSubscription.getSubscription());
		assertNotNull(triggeredSubscription.getCreationDate());
		assertEquals(TriggeredSubscriptionStatus.ACTIVE, triggeredSubscription.getStatus());
		assertEquals(NOW, triggeredSubscription.getCreationDate());
		assertEquals(Date.from(RECEPTION_DT.toInstant()), triggeredSubscription.getEffectiveFrom());
		@SuppressWarnings("unchecked")
		ArgumentCaptor<Collection<AreaCriterion>> areasCaptor = ArgumentCaptor.forClass(Collection.class);
		@SuppressWarnings("unchecked")
		ArgumentCaptor<Collection<AssetCriterion>> assetsCaptor = ArgumentCaptor.forClass(Collection.class);
		ArgumentCaptor<SenderCriterion> senderCaptor = ArgumentCaptor.forClass(SenderCriterion.class);
		@SuppressWarnings("unchecked")
		ArgumentCaptor<Collection<TriggerType>> triggerTypeCaptor = ArgumentCaptor.forClass(Collection.class);
		verify(subscriptionFinder).findTriggeredSubscriptions(areasCaptor.capture(), assetsCaptor.capture(), any(), senderCaptor.capture(), any(), triggerTypeCaptor.capture());
		String areaCriteria = areasCaptor.getValue().stream().map(ac -> ac.getType().toString() + '-' + ac.getGid()).sorted().collect(Collectors.joining(","));
		String assetsCriteria = assetsCaptor.getValue().stream().map(ac -> ac.getType().toString() + '-' + ac.getGuid()).collect(Collectors.joining(","));
		assertEquals("FMZ-270,STATRECT-14,STATRECT-73", areaCriteria, "includes all movements except EXI");
		assertEquals("ASSET-93b63a1c-45ea-11e7-bec7-4c32759615eb", assetsCriteria);
		assertEquals(1, triggerTypeCaptor.getValue().size());
		assertEquals(TriggerType.INC_POSITION, triggerTypeCaptor.getValue().iterator().next());
		assertEquals(senderCriterion, senderCaptor.getValue());

		ArgumentCaptor<StopConditionCriteria> stopConditionCriteriaArgumentCaptor = ArgumentCaptor.forClass(StopConditionCriteria.class);
		verify(triggerCommandsFactory).createStopSubscriptionCommand(stopConditionCriteriaArgumentCaptor.capture());
		StopConditionCriteria stopConditionCriteria = stopConditionCriteriaArgumentCaptor.getValue();
		assertEquals("93b63a1c-45ea-11e7-bec7-4c32759615eb", stopConditionCriteria.getConnectId());
		String notInAreaCriteria = stopConditionCriteria.getAreas().stream().map(ac -> ac.getType().toString() + '-' + ac.getGid()).sorted().collect(Collectors.joining(","));
		assertEquals("FMZ-270,STATRECT-14,STATRECT-73", notInAreaCriteria, "includes all movements except EXI");
		assertNull(stopConditionCriteria.getActivities());

		TriggeredSubscriptionEntity e = new TriggeredSubscriptionEntity();
		e.getData().add(new TriggeredSubscriptionDataEntity(e, "connectId", "CONNECT ID"));
		e.getData().add(new TriggeredSubscriptionDataEntity(e, "vesselId", "VESSEL ID"));
		e.getData().add(new TriggeredSubscriptionDataEntity(e, "vesselSchemeId", "VESSEL SCHEME ID"));
		e.getData().add(new TriggeredSubscriptionDataEntity(e, "occurrence", "OCCURRENCE"));
		e.getData().add(new TriggeredSubscriptionDataEntity(e, "somethingElse", "XXX"));
		Set<TriggeredSubscriptionDataEntity> result = dataExtractorCaptor.getValue().apply(e);
		assertEquals(Collections.singleton(new TriggeredSubscriptionDataEntity(e, "connectId", "CONNECT ID")), result);

		BiPredicate<TriggeredSubscriptionEntity, TriggeredSubscriptionEntity> processTriggerings = processTriggeringCaptor.getValue();
		TriggeredSubscriptionEntity triggeredSubscriptionCandidate = new TriggeredSubscriptionEntity();
		TriggeredSubscriptionEntity existingTriggeredSubscription = new TriggeredSubscriptionEntity();
		triggeredSubscriptionCandidate.getData().add(new TriggeredSubscriptionDataEntity(triggeredSubscriptionCandidate, "movementGuidIndex_12", "value"));
		triggeredSubscriptionCandidate.getData().add(new TriggeredSubscriptionDataEntity(triggeredSubscriptionCandidate, "irrelevant", "42"));
		existingTriggeredSubscription.getData().add(new TriggeredSubscriptionDataEntity(existingTriggeredSubscription, "irrelevant", "43"));
		assertTrue(processTriggerings.test(triggeredSubscriptionCandidate, existingTriggeredSubscription));
		assertEquals(2, existingTriggeredSubscription.getData().size());
		assertEquals(2, triggeredSubscriptionCandidate.getData().size());
		TriggeredSubscriptionDataEntity copiedData = existingTriggeredSubscription.getData().stream().filter(x -> x.getKey().startsWith("movementGuidIndex_")).findFirst().get();
		assertSame(existingTriggeredSubscription, copiedData.getTriggeredSubscription());
		assertEquals("movementGuidIndex_0", copiedData.getKey());
		assertEquals("value", copiedData.getValue());
		TriggeredSubscriptionDataEntity originalData = triggeredSubscriptionCandidate.getData().stream().filter(x -> x.getKey().startsWith("movementGuidIndex_")).findFirst().get();
		assertSame(triggeredSubscriptionCandidate, originalData.getTriggeredSubscription());
	}

	private String readResource(String resourceName) {
		try(InputStream is = this.getClass().getResourceAsStream(resourceName)) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] buf = new byte[512];
			int r;
			while ((r = is.read(buf, 0, buf.length)) >= 0) {
				baos.write(buf, 0, r);
			}
			return new String(baos.toByteArray(), StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
