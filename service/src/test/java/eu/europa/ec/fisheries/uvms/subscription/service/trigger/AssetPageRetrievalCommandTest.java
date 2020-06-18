/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subscription.service.trigger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import eu.europa.ec.fisheries.uvms.subscription.service.messaging.AssetPageRetrievalMessage;
import eu.europa.ec.fisheries.uvms.subscription.service.messaging.SubscriptionSender;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class AssetPageRetrievalCommandTest {

    @Test
    void execute() {
        boolean isGroup = true;
        long subscriptionId = 500L;
        String assetGroupName = "greece";
        long pageNumber = 0L;
        long pageSize = 3L;
        String encodedMessageFromQueue = AssetPageRetrievalMessage.encodeManualSubscriptionMessage(new AssetPageRetrievalMessage(isGroup, subscriptionId, assetGroupName, pageNumber, pageSize));
        SubscriptionSender subscriptionSender = mock(SubscriptionSender.class);

        AssetPageRetrievalCommand sut = new AssetPageRetrievalCommand(AssetPageRetrievalMessage.decodeManualSubscriptionMessage(encodedMessageFromQueue), subscriptionSender);
        sut.execute();

        ArgumentCaptor<AssetPageRetrievalMessage> captor = ArgumentCaptor.forClass(AssetPageRetrievalMessage.class);
        verify(subscriptionSender).sendAssetPageRetrievalMessageSameTx(captor.capture());
        assertAssetPageRetrievalMessage(encodedMessageFromQueue, captor);
    }

    private void assertAssetPageRetrievalMessage(String encodedMessageFromQueue, ArgumentCaptor<AssetPageRetrievalMessage> captor) {
        AssetPageRetrievalMessage message = captor.getValue();
        AssetPageRetrievalMessage expectedMessage = AssetPageRetrievalMessage.decodeManualSubscriptionMessage(encodedMessageFromQueue);
        assertEquals(expectedMessage.getSubscriptionId(), message.getSubscriptionId());
        assertEquals(expectedMessage.getAssetGroupGuid(), message.getAssetGroupGuid());
        assertEquals(expectedMessage.getPageNumber(), message.getPageNumber());
        assertEquals(expectedMessage.getPageSize(), message.getPageSize());
    }
}