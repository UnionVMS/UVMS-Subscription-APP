package eu.europa.ec.fisheries.uvms.subscription.movement.execution;

import static eu.europa.fisheries.uvms.subscription.model.enums.SubscriptionExecutionStatusType.QUEUED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import eu.europa.ec.fisheries.uvms.subscription.movement.communication.MovementSender;
import eu.europa.ec.fisheries.uvms.subscription.service.dao.TriggeredSubscriptionDao;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionExecution;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionExecutionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionOutput;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionSubscriber;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.TriggeredSubscriptionDataEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.TriggeredSubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.messaging.asset.AssetSender;
import eu.europa.ec.fisheries.uvms.subscription.service.messaging.usm.ReceiverAndDataflow;
import eu.europa.ec.fisheries.uvms.subscription.service.messaging.usm.UsmSender;
import eu.europa.ec.fisheries.uvms.subscription.service.util.SubscriptionDateTimeService;
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
 * Tests for the {@link PositionTriggeredSubscriptionExecutor}.
 */
@EnableAutoWeld
@ExtendWith(MockitoExtension.class)
class PositionTriggeredSubscriptionExecutorTest {

    private static final Long TRIGGERED_SUBSCRIPTION_ID = 33L;
    private static final Long CHANNEL_ID = 22L;
    private static final Long ENDPOINT_ID = 11L;
    private static final String CONNECT_ID = "connectid";
    private static final String OCCURRENCE = "2017-03-04T17:39:00Z";
    private static final List<String> MOVEMENT_GUIDS = Arrays.asList("a-movement-guid", "another-movement-guid");
    private static final String VESSEL_COUNTRY = "GRC";
    private static final String VESSEL_CFR = "CFR123456789";
    private static final String VESSEL_IRCS = "DUMMY IRCS";
    private static final String A_MESSAGE_ID = "a-message-id";

    @Produces
    @Mock
    private TriggeredSubscriptionDao triggeredSubscriptionDao;

    @Produces
    @Mock
    private AssetSender assetSender;

    @Produces
    @Mock
    private MovementSender movementSender;

    @Produces
    @Mock
    private UsmSender usmSender;

    @Produces
    @Mock
    private SubscriptionDateTimeService subscriptionDateTimeService;

    @Inject
    private PositionTriggeredSubscriptionExecutor sut;

    @Test
    void testEmptyConstructor() {
        PositionTriggeredSubscriptionExecutor sut = new PositionTriggeredSubscriptionExecutor();
        assertNotNull(sut);
    }

    @Test
    void testExecuteNoFaQuery() {
        SubscriptionExecutionEntity execution = setup(OutgoingMessageType.FA_REPORT, TriggerType.INC_FA_QUERY);
        sut.execute(execution);
        verifyNoMoreInteractions(movementSender, assetSender, usmSender);
    }

    @Test
    void execute() {
        SubscriptionExecutionEntity execution = setup(OutgoingMessageType.POSITION, TriggerType.INC_POSITION);
        SubscriptionOutput output = execution.getTriggeredSubscription().getSubscription().getOutput();
        output.setHistory(3);
        output.setHistoryUnit(SubscriptionTimeUnit.DAYS);
        setupMocks();

        sut.execute(execution);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, String>> vesselIdentifiersCaptor = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<String> countryCaptor = ArgumentCaptor.forClass(String.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<String>> movementGuidsCaptor = ArgumentCaptor.forClass(List.class);
        ArgumentCaptor<String> receiverCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> dataflowCaptor = ArgumentCaptor.forClass(String.class);

        verify(movementSender).forwardPosition(vesselIdentifiersCaptor.capture(), countryCaptor.capture(), movementGuidsCaptor.capture(), receiverCaptor.capture(), dataflowCaptor.capture());
        assertNull(execution.getExecutionTime());
        assertEquals(QUEUED, execution.getStatus());

        assertEquals(VESSEL_IRCS, vesselIdentifiersCaptor.getValue().get("IRCS"));
        assertNull(vesselIdentifiersCaptor.getValue().get("CFR"));
        assertEquals(VESSEL_COUNTRY, countryCaptor.getValue());
        assertEquals(MOVEMENT_GUIDS.size(), movementGuidsCaptor.getValue().size());
        assertTrue(MOVEMENT_GUIDS.containsAll(movementGuidsCaptor.getValue()));
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
        triggeredSubscription.getData().add(new TriggeredSubscriptionDataEntity(triggeredSubscription, "movementGuidIndex_1", MOVEMENT_GUIDS.get(0)));
        triggeredSubscription.getData().add(new TriggeredSubscriptionDataEntity(triggeredSubscription, "movementGuidIndex_2", MOVEMENT_GUIDS.get(1)));
        lenient().when(triggeredSubscriptionDao.getById(TRIGGERED_SUBSCRIPTION_ID)).thenReturn(triggeredSubscription);
        SubscriptionExecutionEntity execution = new SubscriptionExecutionEntity();
        execution.setTriggeredSubscription(triggeredSubscription);
        execution.setStatus(QUEUED);
        return execution;
    }

    private void setupMocks() {
        when(movementSender.forwardPosition(any(), any(), any(), any(), any())).thenReturn(new ArrayList<>());
        VesselIdentifiersHolder idsHolder = new VesselIdentifiersHolder();
        idsHolder.setCfr(VESSEL_CFR);
        idsHolder.setIrcs(VESSEL_IRCS);
        idsHolder.setCountryCode(VESSEL_COUNTRY);
        lenient().when(assetSender.findVesselIdentifiers(CONNECT_ID)).thenReturn(idsHolder);
        ReceiverAndDataflow receiverAndDataflow = new ReceiverAndDataflow();
        receiverAndDataflow.setDataflow("dataflow");
        receiverAndDataflow.setReceiver("receiver");
        lenient().when(usmSender.findReceiverAndDataflow(ENDPOINT_ID, CHANNEL_ID)).thenReturn(receiverAndDataflow);
    }
}