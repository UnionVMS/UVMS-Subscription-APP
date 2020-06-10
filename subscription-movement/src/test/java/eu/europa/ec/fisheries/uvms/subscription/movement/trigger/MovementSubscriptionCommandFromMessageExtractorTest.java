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
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import eu.europa.ec.fisheries.uvms.subscription.service.bean.Command;
import eu.europa.ec.fisheries.uvms.subscription.service.bean.SubscriptionFinder;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.TriggeredSubscriptionDataEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.TriggeredSubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.search.SubscriptionSearchCriteria.AssetCriterion;
import eu.europa.ec.fisheries.uvms.subscription.service.messaging.asset.AssetSender;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.search.AreaCriterion;
import eu.europa.ec.fisheries.uvms.subscription.service.trigger.StopConditionCriteria;
import eu.europa.ec.fisheries.uvms.subscription.service.trigger.TriggerCommandsFactory;
import eu.europa.fisheries.uvms.subscription.model.enums.TriggerType;
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
	void testJAXBExceptionResultsInApplicationException() {
		assertThrows(MessageFormatException.class, () -> sut.extractCommands("bad"));
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
		List<Command> commands = sut.extractCommands(representation).collect(Collectors.toList());

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
		long size = sut.extractCommands(representation).count();
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
		when(subscriptionFinder.findTriggeredSubscriptions(any(), any(), any(), any())).thenReturn(Collections.singletonList(subscription));
		String representation = readResource("CreateMovementBatchResponse-OK.xml");
		dateTimeService.setNow(NOW);

		List<Command> commands = sut.extractCommands(representation).collect(Collectors.toList());

		assertEquals(2, commands.size());
		ArgumentCaptor<TriggeredSubscriptionEntity> triggeredSubscriptionCaptor = ArgumentCaptor.forClass(TriggeredSubscriptionEntity.class);
		@SuppressWarnings("unchecked")
		ArgumentCaptor<Function<TriggeredSubscriptionEntity,Set<TriggeredSubscriptionDataEntity>>> dataExtractorCaptor = ArgumentCaptor.forClass(Function.class);
		verify(triggerCommandsFactory).createTriggerSubscriptionCommand(triggeredSubscriptionCaptor.capture(), dataExtractorCaptor.capture());
		TriggeredSubscriptionEntity triggeredSubscription = triggeredSubscriptionCaptor.getValue();
		assertSame(subscription, triggeredSubscription.getSubscription());
		assertNotNull(triggeredSubscription.getCreationDate());
		assertTrue(triggeredSubscription.getActive());
		assertEquals(NOW, triggeredSubscription.getCreationDate());
		assertEquals(Date.from(LocalDateTime.of(2017,3,4,17,39).toInstant(ZoneOffset.UTC)), triggeredSubscription.getEffectiveFrom());
		@SuppressWarnings("unchecked")
		ArgumentCaptor<Collection<AreaCriterion>> areasCaptor = ArgumentCaptor.forClass(Collection.class);
		@SuppressWarnings("unchecked")
		ArgumentCaptor<Collection<AssetCriterion>> assetsCaptor = ArgumentCaptor.forClass(Collection.class);
		@SuppressWarnings("unchecked")
		ArgumentCaptor<Collection<TriggerType>> triggerTypeCaptor = ArgumentCaptor.forClass(Collection.class);
		verify(subscriptionFinder).findTriggeredSubscriptions(areasCaptor.capture(), assetsCaptor.capture(), any(), triggerTypeCaptor.capture());
		String areaCriteria = areasCaptor.getValue().stream().map(ac -> ac.getType().toString() + '-' + ac.getGid()).sorted().collect(Collectors.joining(","));
		String assetsCriteria = assetsCaptor.getValue().stream().map(ac -> ac.getType().toString() + '-' + ac.getGuid()).collect(Collectors.joining(","));
		assertEquals("FMZ-270,STATRECT-14,STATRECT-73", areaCriteria, "includes all movements except EXI");
		assertEquals("ASSET-93b63a1c-45ea-11e7-bec7-4c32759615eb", assetsCriteria);
		assertEquals(1, triggerTypeCaptor.getValue().size());
		assertEquals(TriggerType.INC_POSITION, triggerTypeCaptor.getValue().iterator().next());

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
