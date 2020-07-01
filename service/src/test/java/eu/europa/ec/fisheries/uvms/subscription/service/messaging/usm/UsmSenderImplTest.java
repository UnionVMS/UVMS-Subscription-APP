package eu.europa.ec.fisheries.uvms.subscription.service.messaging.usm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import eu.europa.ec.fisheries.wsdl.user.module.FindOrganisationByEndpointAndChannelRequest;
import eu.europa.ec.fisheries.wsdl.user.module.FindOrganisationByEndpointAndChannelResponse;
import eu.europa.ec.fisheries.wsdl.user.module.UserModuleMethod;
import eu.europa.ec.fisheries.wsdl.user.types.Channel;
import eu.europa.ec.fisheries.wsdl.user.types.EndPoint;
import eu.europa.ec.fisheries.wsdl.user.types.OrganisationEndpointAndChannelId;
import eu.europa.fisheries.uvms.subscription.model.exceptions.EntityDoesNotExistException;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests for {@link UsmSenderImpl}
 */
@EnableAutoWeld
@ExtendWith(MockitoExtension.class)
class UsmSenderImplTest {

    private static final Long ENDPOINT_ID = 111L;
    private static final Long CHANNEL_ID = 222L;
    private static final String DATAFLOW = "df";
    private static final String URI = "URI";
    private static final String ENDPOINT = "SRC:EP";

    @Produces @Mock
    private UsmClient usmClient;

    @Inject
    private UsmSenderImpl sut;

    @Test
    void testCreateUsingEmptyConstructor() {
        UsmSender usmSender = new UsmSenderImpl();
        assertNotNull(usmSender);
    }

    @Test
    void testFindReceiverAndDataflowNullEndpoint() {
        when(usmClient.findEndpoint(ENDPOINT_ID)).thenReturn(null);
        assertThrows(EntityDoesNotExistException.class, () -> sut.findReceiverAndDataflow(ENDPOINT_ID, CHANNEL_ID));
    }

    @Test
    void testFindReceiverAndDataflowNoChannel() {
        EndPoint endpoint = new EndPoint();
        endpoint.setId(ENDPOINT_ID);
        endpoint.getChannels().add(new Channel());
        when(usmClient.findEndpoint(ENDPOINT_ID)).thenReturn(endpoint);
        assertThrows(EntityDoesNotExistException.class, () -> sut.findReceiverAndDataflow(ENDPOINT_ID, CHANNEL_ID));
    }

    @Test
    void testFindReceiverAndDataflow() {
        Channel channel = new Channel();
        channel.setId(CHANNEL_ID);
        channel.setDataFlow(DATAFLOW);
        EndPoint endpoint = new EndPoint();
        endpoint.setId(ENDPOINT_ID);
        endpoint.setUri(URI);
        endpoint.getChannels().add(channel);
        when(usmClient.findEndpoint(ENDPOINT_ID)).thenReturn(endpoint);
        ReceiverAndDataflow result = sut.findReceiverAndDataflow(ENDPOINT_ID, CHANNEL_ID);
        Assertions.assertEquals(DATAFLOW, result.getDataflow());
        Assertions.assertEquals(URI, result.getReceiver());
    }

    @Test
    void testFindOrganizationByDataFlowAndEndpoint() {
        OrganisationEndpointAndChannelId result = new OrganisationEndpointAndChannelId();
        FindOrganisationByEndpointAndChannelResponse resp = new FindOrganisationByEndpointAndChannelResponse();
        resp.setResult(result);
        when(usmClient.findOrganisationByEndpointAndChannel(any())).thenReturn(resp);
        OrganisationEndpointAndChannelId outcome = sut.findOrganizationByDataFlowAndEndpoint(DATAFLOW, ENDPOINT);
        assertSame(result, outcome);
        ArgumentCaptor<FindOrganisationByEndpointAndChannelRequest> captor = ArgumentCaptor.forClass(FindOrganisationByEndpointAndChannelRequest.class);
        verify(usmClient).findOrganisationByEndpointAndChannel(captor.capture());
        assertEquals(UserModuleMethod.FIND_ORGANISATION_BY_ENDPOINT_AND_CHANNEL, captor.getValue().getMethod());
    }
}
