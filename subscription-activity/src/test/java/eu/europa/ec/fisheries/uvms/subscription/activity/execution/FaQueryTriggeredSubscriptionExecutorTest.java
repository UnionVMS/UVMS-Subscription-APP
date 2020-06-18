/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subscription.activity.execution;

import static eu.europa.fisheries.uvms.subscription.model.enums.SubscriptionExecutionStatusType.QUEUED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;

import eu.europa.ec.fisheries.uvms.activity.model.schemas.VesselIdentifierType;
import eu.europa.ec.fisheries.uvms.commons.domain.DateRange;
import eu.europa.ec.fisheries.uvms.subscription.activity.communication.ActivitySender;
import eu.europa.ec.fisheries.uvms.subscription.service.messaging.usm.ReceiverAndDataflow;
import eu.europa.ec.fisheries.uvms.subscription.service.messaging.usm.UsmSender;
import eu.europa.ec.fisheries.uvms.subscription.service.dao.TriggeredSubscriptionDao;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionExecution;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionExecutionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionOutput;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionSubscriber;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.TriggeredSubscriptionDataEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.TriggeredSubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.messaging.asset.AssetSender;
import eu.europa.ec.fisheries.wsdl.asset.types.VesselIdentifiersHolder;
import eu.europa.fisheries.uvms.subscription.model.enums.OutgoingMessageType;
import eu.europa.fisheries.uvms.subscription.model.enums.SubscriptionTimeUnit;
import eu.europa.fisheries.uvms.subscription.model.enums.SubscriptionVesselIdentifier;
import eu.europa.fisheries.uvms.subscription.model.enums.TriggerType;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests for the {@link FaQueryTriggeredSubscriptionExecutor}.
 */
@EnableAutoWeld
@ExtendWith(MockitoExtension.class)
public class FaQueryTriggeredSubscriptionExecutorTest {

	private static final Long TRIGGERED_SUBSCRIPTION_ID = 33L;
	private static final Long CHANNEL_ID = 22L;
	private static final Long ENDPOINT_ID = 11L;
	private static final String CONNECT_ID = "connectid";
	private static final String RECEIVER = "RECEIVER";
	private static final String DATAFLOW = "DATAFLOW";
	private static final String OCCURRENCE = "2017-03-04T17:39:00Z";
	private static final Date START_DATE = Date.from(LocalDateTime.of(2020, 6, 11, 12, 1,2).atZone(ZoneId.of("UTC")).toInstant());
	private static final Date END_DATE = Date.from(LocalDateTime.of(2020, 6, 21, 13, 3,4).atZone(ZoneId.of("UTC")).toInstant());

	@Produces @Mock
	private TriggeredSubscriptionDao triggeredSubscriptionDao;

	@Produces @Mock
	private ActivitySender activitySender;

	@Produces @Mock
	private AssetSender assetSender;

	@Produces @Mock
	private UsmSender usmSender;

	@Inject
	private FaQueryTriggeredSubscriptionExecutor sut;

	@Produces
	DatatypeFactory getDatatypeFactory() throws Exception {
		return DatatypeFactory.newInstance();
	}

	@Test
	void testEmptyConstructor() {
		FaQueryTriggeredSubscriptionExecutor sut = new FaQueryTriggeredSubscriptionExecutor();
		assertNotNull(sut);
	}

	@Test
	void testExecuteNoFaQuery() {
		SubscriptionExecutionEntity execution = setup(OutgoingMessageType.FA_REPORT, TriggerType.INC_FA_QUERY);
		sut.execute(execution);
		verifyNoMoreInteractions(activitySender, usmSender);
	}

	@Test
	void testExecute() throws Exception {
		SubscriptionExecutionEntity execution = setup(OutgoingMessageType.FA_QUERY, TriggerType.INC_FA_QUERY);
		execution.getTriggeredSubscription().getSubscription().getOutput().setHistory(3);
		execution.getTriggeredSubscription().getSubscription().getOutput().setHistoryUnit(SubscriptionTimeUnit.DAYS);
		setupMocks();

		sut.execute(execution);

		@SuppressWarnings("unchecked")
		ArgumentCaptor<List<VesselIdentifierType>> idsCaptor = ArgumentCaptor.forClass(List.class);
		ArgumentCaptor<XMLGregorianCalendar> dateCaptor1 = ArgumentCaptor.forClass(XMLGregorianCalendar.class);
		ArgumentCaptor<XMLGregorianCalendar> dateCaptor2 = ArgumentCaptor.forClass(XMLGregorianCalendar.class);
		DatatypeFactory datatypeFactory = DatatypeFactory.newInstance();
		verify(activitySender).createAndSendQueryForVessel(idsCaptor.capture(), eq(true), dateCaptor1.capture(), dateCaptor2.capture(), eq(RECEIVER), eq(DATAFLOW));
		assertNull(execution.getExecutionTime());
		assertEquals(QUEUED, execution.getStatus());
		assertEquals(datatypeFactory.newXMLGregorianCalendar("2017-03-01T17:39:00Z"), dateCaptor1.getValue());
		assertEquals(datatypeFactory.newXMLGregorianCalendar(OCCURRENCE), dateCaptor2.getValue());
		assertEquals(1, idsCaptor.getValue().size());
		assertEquals("DUMMY IRCS", idsCaptor.getValue().get(0).getValue());
	}

	@Test
	void testExecuteManualSubscription() throws Exception {
		SubscriptionExecutionEntity execution = setup(OutgoingMessageType.FA_QUERY, TriggerType.MANUAL);
		execution.getTriggeredSubscription().getSubscription().getOutput().setQueryPeriod(new DateRange(START_DATE, END_DATE));
		execution.getTriggeredSubscription().getData().add(new TriggeredSubscriptionDataEntity(execution.getTriggeredSubscription(), "IRCS", "IRCS FROM DATA"));
		setupMocks();

		sut.execute(execution);

		@SuppressWarnings("unchecked")
		ArgumentCaptor<List<VesselIdentifierType>> idsCaptor = ArgumentCaptor.forClass(List.class);
		ArgumentCaptor<XMLGregorianCalendar> dateCaptor1 = ArgumentCaptor.forClass(XMLGregorianCalendar.class);
		ArgumentCaptor<XMLGregorianCalendar> dateCaptor2 = ArgumentCaptor.forClass(XMLGregorianCalendar.class);
		DatatypeFactory datatypeFactory = DatatypeFactory.newInstance();
		verify(activitySender).createAndSendQueryForVessel(idsCaptor.capture(), eq(true), dateCaptor1.capture(), dateCaptor2.capture(), eq(RECEIVER), eq(DATAFLOW));
		assertNull(execution.getExecutionTime());
		assertEquals(QUEUED, execution.getStatus());
		assertEquals(datatypeFactory.newXMLGregorianCalendar("2020-06-11T12:01:02Z"), dateCaptor1.getValue());
		assertEquals(datatypeFactory.newXMLGregorianCalendar("2020-06-21T13:03:04Z"), dateCaptor2.getValue());
		assertEquals(1, idsCaptor.getValue().size());
		assertEquals("IRCS FROM DATA", idsCaptor.getValue().get(0).getValue());
	}

	private SubscriptionExecutionEntity setup(OutgoingMessageType outgoingMessageType, TriggerType triggerType) {
		SubscriptionEntity subscription = new SubscriptionEntity();
		SubscriptionSubscriber subscriber = new SubscriptionSubscriber();
		subscriber.setChannelId(CHANNEL_ID);
		subscriber.setEndpointId(ENDPOINT_ID);
		SubscriptionOutput output = new SubscriptionOutput();
		output.setMessageType(outgoingMessageType);
		output.setSubscriber(subscriber);
		output.setConsolidated(true);
		output.setVesselIds(EnumSet.of(SubscriptionVesselIdentifier.IRCS));
		subscription.setOutput(output);
		SubscriptionExecution subscriptionExecution = new SubscriptionExecution();
		subscriptionExecution.setTriggerType(triggerType);
		subscription.setExecution(subscriptionExecution);
		TriggeredSubscriptionEntity triggeredSubscription = new TriggeredSubscriptionEntity();
		triggeredSubscription.setId(TRIGGERED_SUBSCRIPTION_ID);
		triggeredSubscription.setSubscription(subscription);
		triggeredSubscription.getData().add(new TriggeredSubscriptionDataEntity(triggeredSubscription, "connectId", CONNECT_ID));
		triggeredSubscription.getData().add(new TriggeredSubscriptionDataEntity(triggeredSubscription, "occurrence", OCCURRENCE));
		lenient().when(triggeredSubscriptionDao.getById(TRIGGERED_SUBSCRIPTION_ID)).thenReturn(triggeredSubscription);
		SubscriptionExecutionEntity execution = new SubscriptionExecutionEntity();
		execution.setTriggeredSubscription(triggeredSubscription);
		execution.setStatus(QUEUED);
		return execution;
	}

	private void setupMocks() {
		ReceiverAndDataflow receiverAndDataflow = new ReceiverAndDataflow(RECEIVER, DATAFLOW);
		when(usmSender.findReceiverAndDataflow(ENDPOINT_ID, CHANNEL_ID)).thenReturn(receiverAndDataflow);
		VesselIdentifiersHolder idsHolder = new VesselIdentifiersHolder();
		idsHolder.setCfr("CFR123456789");
		idsHolder.setIrcs("DUMMY IRCS");
		lenient().when(assetSender.findVesselIdentifiers(CONNECT_ID)).thenReturn(idsHolder);
	}
}
