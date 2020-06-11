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
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.xml.datatype.DatatypeFactory;

import java.util.EnumSet;

import eu.europa.ec.fisheries.uvms.activity.model.schemas.CreateAndSendFAQueryForVesselRequest;
import eu.europa.ec.fisheries.uvms.subscription.activity.communication.ActivitySender;
import eu.europa.ec.fisheries.uvms.subscription.activity.communication.ReceiverAndDataflow;
import eu.europa.ec.fisheries.uvms.subscription.activity.communication.UsmSender;
import eu.europa.ec.fisheries.uvms.subscription.service.dao.TriggeredSubscriptionDao;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity;
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
		SubscriptionExecutionEntity execution = setup(OutgoingMessageType.FA_REPORT);
		sut.execute(execution);
		verifyNoMoreInteractions(activitySender, usmSender);
	}

	@Test
	void testExecute() throws Exception {
		SubscriptionExecutionEntity execution = setup(OutgoingMessageType.FA_QUERY);
		ReceiverAndDataflow receiverAndDataflow = new ReceiverAndDataflow(RECEIVER, DATAFLOW);
		when(usmSender.findReceiverAndDataflow(ENDPOINT_ID,CHANNEL_ID)).thenReturn(receiverAndDataflow);
		VesselIdentifiersHolder idsHolder = new VesselIdentifiersHolder();
		idsHolder.setCfr("CFR123456789");
		idsHolder.setIrcs("DUMMY IRCS");
		when(assetSender.findVesselIdentifiers(CONNECT_ID)).thenReturn(idsHolder);

		sut.execute(execution);

		ArgumentCaptor<CreateAndSendFAQueryForVesselRequest> captor = ArgumentCaptor.forClass(CreateAndSendFAQueryForVesselRequest.class);
		verify(activitySender).send(captor.capture());
		CreateAndSendFAQueryForVesselRequest request = captor.getValue();
		DatatypeFactory datatypeFactory = DatatypeFactory.newInstance();
		assertEquals(datatypeFactory.newXMLGregorianCalendar("2017-03-01T17:39:00Z"), request.getStartDate());
		assertEquals(1, request.getVesselIdentifiers().size());
		assertEquals("DUMMY IRCS", request.getVesselIdentifiers().get(0).getValue());
		assertNull(execution.getExecutionTime());
		assertEquals(QUEUED, execution.getStatus());
	}

	private SubscriptionExecutionEntity setup(OutgoingMessageType outgoingMessageType) {
		SubscriptionEntity subscription = new SubscriptionEntity();
		SubscriptionSubscriber subscriber = new SubscriptionSubscriber();
		subscriber.setChannelId(CHANNEL_ID);
		subscriber.setEndpointId(ENDPOINT_ID);
		SubscriptionOutput output = new SubscriptionOutput();
		output.setMessageType(outgoingMessageType);
		output.setSubscriber(subscriber);
		output.setHistory(3);
		output.setHistoryUnit(SubscriptionTimeUnit.DAYS);
		output.setConsolidated(true);
		output.setVesselIds(EnumSet.of(SubscriptionVesselIdentifier.IRCS));
		subscription.setOutput(output);
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
}
