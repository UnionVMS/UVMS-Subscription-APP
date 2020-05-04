/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subscription.activity.execution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.xml.datatype.DatatypeFactory;

import eu.europa.ec.fisheries.uvms.activity.model.schemas.CreateAndSendFAQueryRequest;
import eu.europa.ec.fisheries.uvms.subscription.activity.communication.ActivitySender;
import eu.europa.ec.fisheries.uvms.subscription.activity.communication.ReceiverAndDataflow;
import eu.europa.ec.fisheries.uvms.subscription.activity.communication.UsmSender;
import eu.europa.ec.fisheries.uvms.subscription.service.dao.TriggeredSubscriptionDao;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionOutput;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionSubscriber;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.TriggeredSubscriptionDataEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.TriggeredSubscriptionEntity;
import eu.europa.fisheries.uvms.subscription.model.enums.SubscriptionTimeUnit;
import eu.europa.fisheries.uvms.subscription.model.enums.OutgoingMessageType;
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
	private static final String RECEIVER = "RECEIVER";
	private static final String DATAFLOW = "DATAFLOW";
	private static final String VESSEL_ID = "VESSEL_ID";
	private static final String VESSEL_SCHEME_ID = "VESSEL_SCHEME_ID";
	private static final String OCCURRENCE = "2017-03-04T17:39:00Z";

	@Produces @Mock
	private TriggeredSubscriptionDao triggeredSubscriptionDao;

	@Produces @Mock
	private ActivitySender activitySender;

	@Produces @Mock
	private UsmSender usmSender;

	@Inject
	private FaQueryTriggeredSubscriptionExecutor sut;

	@Produces
	DatatypeFactory getDatatypeFactory() throws Exception {
		return DatatypeFactory.newInstance();
	}

	@Test
	void testExecuteNoFaQuery() {
		setup(OutgoingMessageType.FA_REPORT);
		sut.execute(TRIGGERED_SUBSCRIPTION_ID);
		verifyNoMoreInteractions(activitySender, usmSender);
	}

	@Test
	void testExecute() throws Exception {
		setup(OutgoingMessageType.FA_QUERY);
		ReceiverAndDataflow receiverAndDataflow = new ReceiverAndDataflow(RECEIVER, DATAFLOW);
		when(usmSender.findReceiverAndDataflow(ENDPOINT_ID,CHANNEL_ID)).thenReturn(receiverAndDataflow);

		sut.execute(TRIGGERED_SUBSCRIPTION_ID);

		ArgumentCaptor<CreateAndSendFAQueryRequest> captor = ArgumentCaptor.forClass(CreateAndSendFAQueryRequest.class);
		verify(activitySender).send(captor.capture());
		CreateAndSendFAQueryRequest request = captor.getValue();
		DatatypeFactory datatypeFactory = DatatypeFactory.newInstance();
		assertEquals(datatypeFactory.newXMLGregorianCalendar("2017-03-01T17:39:00Z"), request.getStartDate());
	}

	private void setup(OutgoingMessageType outgoingMessageType) {
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
		subscription.setOutput(output);
		TriggeredSubscriptionEntity triggeredSubscription = new TriggeredSubscriptionEntity();
		triggeredSubscription.setId(TRIGGERED_SUBSCRIPTION_ID);
		triggeredSubscription.setSubscription(subscription);
		triggeredSubscription.getData().add(new TriggeredSubscriptionDataEntity(triggeredSubscription, "vesselId", VESSEL_ID));
		triggeredSubscription.getData().add(new TriggeredSubscriptionDataEntity(triggeredSubscription, "vesselSchemeId", VESSEL_SCHEME_ID));
		triggeredSubscription.getData().add(new TriggeredSubscriptionDataEntity(triggeredSubscription, "occurrence", OCCURRENCE));
		when(triggeredSubscriptionDao.getById(TRIGGERED_SUBSCRIPTION_ID)).thenReturn(triggeredSubscription);
	}
}
