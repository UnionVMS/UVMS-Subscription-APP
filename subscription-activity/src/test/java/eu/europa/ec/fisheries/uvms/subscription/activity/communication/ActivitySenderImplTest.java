package eu.europa.ec.fisheries.uvms.subscription.activity.communication;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.jms.Destination;
import javax.jms.Queue;
import javax.xml.datatype.DatatypeFactory;
import java.util.Collections;

import eu.europa.ec.fisheries.uvms.activity.model.schemas.ActivityModuleMethod;
import eu.europa.ec.fisheries.uvms.activity.model.schemas.CreateAndSendFAQueryRequest;
import eu.europa.ec.fisheries.uvms.activity.model.schemas.PluginType;
import eu.europa.ec.fisheries.uvms.activity.model.schemas.VesselIdentifierSchemeIdEnum;
import eu.europa.ec.fisheries.uvms.activity.model.schemas.VesselIdentifierType;
import eu.europa.ec.fisheries.uvms.commons.message.api.MessageException;
import eu.europa.ec.fisheries.uvms.subscription.service.messaging.SubscriptionProducerBean;
import eu.europa.fisheries.uvms.subscription.model.exceptions.ExecutionException;
import lombok.SneakyThrows;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests  for {@link ActivitySenderImpl}
 */
@EnableAutoWeld
@ExtendWith(MockitoExtension.class)
class ActivitySenderImplTest {

    @Produces @Mock
    private SubscriptionProducerBean subscriptionProducer;

    @Produces @Mock @ActivityQueue
    private Queue activityQueue;

    @Inject
    ActivitySenderImpl sut;

    @Mock
    Destination destination;

    @Test
    @SneakyThrows
    void testSend() {
        CreateAndSendFAQueryRequest request = new CreateAndSendFAQueryRequest(ActivityModuleMethod.CREATE_AND_SEND_FA_QUERY, PluginType.FLUX, Collections.singletonList(new VesselIdentifierType(VesselIdentifierSchemeIdEnum.CFR, "CFR123456789")), true,
                DatatypeFactory.newInstance().newXMLGregorianCalendar("2019-01-01T10:00:00"), DatatypeFactory.newInstance().newXMLGregorianCalendar("2019-02-01T10:00:00"),
                "receiver", "dataflow");
        when(subscriptionProducer.getDestination()).thenReturn(destination);
        Assertions.assertDoesNotThrow(() -> sut.send(request));
        verify(subscriptionProducer).sendMessageToSpecificQueueSameTx(any(), eq(activityQueue), eq(destination));
    }

    @Test
    @SneakyThrows
    void testSendWithException() {
        CreateAndSendFAQueryRequest request = new CreateAndSendFAQueryRequest();
        when(subscriptionProducer.getDestination()).thenReturn(destination);
        when(subscriptionProducer.sendMessageToSpecificQueueSameTx(any(), eq(activityQueue), eq(destination))).thenThrow(MessageException.class);
        assertThrows(ExecutionException.class, () -> sut.send(request));
    }
}