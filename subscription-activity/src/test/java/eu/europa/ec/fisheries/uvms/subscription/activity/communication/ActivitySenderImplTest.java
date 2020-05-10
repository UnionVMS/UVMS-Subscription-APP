package eu.europa.ec.fisheries.uvms.subscription.activity.communication;

import static org.mockito.Mockito.when;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.jms.Destination;
import javax.jms.Queue;
import javax.xml.datatype.DatatypeFactory;

import eu.europa.ec.fisheries.uvms.activity.model.exception.ActivityModelMarshallException;
import eu.europa.ec.fisheries.uvms.activity.model.schemas.ActivityModuleMethod;
import eu.europa.ec.fisheries.uvms.activity.model.schemas.CreateAndSendFAQueryRequest;
import eu.europa.ec.fisheries.uvms.activity.model.schemas.PluginType;
import eu.europa.ec.fisheries.uvms.subscription.service.messaging.SubscriptionProducerBean;
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
        CreateAndSendFAQueryRequest request = new CreateAndSendFAQueryRequest(ActivityModuleMethod.CREATE_AND_SEND_FA_QUERY, PluginType.FLUX, "012345678", "CFR", true,
                DatatypeFactory.newInstance().newXMLGregorianCalendar("2019-01-01T10:00:00"), DatatypeFactory.newInstance().newXMLGregorianCalendar("2019-02-01T10:00:00"),
                "receiver", "dataflow");
        when(subscriptionProducer.getDestination()).thenReturn(destination);
        Assertions.assertDoesNotThrow(() -> sut.send(request));
    }
}