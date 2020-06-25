/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subscription.service.messaging;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import java.util.Map;

import eu.europa.ec.fisheries.uvms.commons.message.api.MessageException;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@EnableAutoWeld
@ExtendWith(MockitoExtension.class)
class JmsSubscriptionClientImplTest {

    private static final String JMS_MESSAGE_SOURCE_KEY = "subscriptionSource";
    private static final String SOURCE_MANUAL = "manual";
    private static final String SOURCE_SCHEDULED = "scheduled";

    @Mock
    @Produces
    private SubscriptionManualProducerBean producerBean;

    @Inject
    private JmsSubscriptionClientImpl sut;

    @Test
    void testSendAssetPageRetrievalMessageSameTx() throws MessageException {
        String encodedMessage = "encoded message body";
        sut.sendAssetPageRetrievalMessageSameTx(encodedMessage);

        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, String>> propsCaptor = ArgumentCaptor.forClass(Map.class);

        verify(producerBean).sendModuleMessageWithPropsSameTx(messageCaptor.capture(), propsCaptor.capture());
        String messageBody = messageCaptor.getValue();
        Map<String, String> messageProps = propsCaptor.getValue();

        assertEquals(encodedMessage, messageBody);
        assertEquals(SOURCE_MANUAL, messageProps.get(JMS_MESSAGE_SOURCE_KEY));
    }

    @Test
    void testSendMessageForScheduledSubscriptionExecutionSameTx() throws MessageException {
        String encodedMessage = "encoded message body";
        sut.sendMessageForScheduledSubscriptionExecutionSameTx(encodedMessage);

        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, String>> propsCaptor = ArgumentCaptor.forClass(Map.class);

        verify(producerBean).sendModuleMessageWithPropsSameTx(messageCaptor.capture(), propsCaptor.capture());
        String messageBody = messageCaptor.getValue();
        Map<String, String> messageProps = propsCaptor.getValue();

        assertEquals(encodedMessage, messageBody);
        assertEquals(SOURCE_SCHEDULED, messageProps.get(JMS_MESSAGE_SOURCE_KEY));
    }
}
