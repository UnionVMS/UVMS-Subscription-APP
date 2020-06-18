/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subscription.service.messaging;

import static org.mockito.Mockito.verify;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests for the {@link SubscriptionSenderImpl}.
 */
@EnableAutoWeld
@ExtendWith(MockitoExtension.class)
class SubscriptionSenderImplTest {

    @Mock
    @Produces
    private SubscriptionClient subscriptionClient;

    @Inject
    private SubscriptionSenderImpl sut;

    @Test
    void testSendAssetPageRetrievalMessage() {
        AssetPageRetrievalMessage assetPageRetrievalMessage = new AssetPageRetrievalMessage(true, 500L, "greece", 1L, 10L);
        sut.sendAssetPageRetrievalMessageSameTx(assetPageRetrievalMessage);

        verify(subscriptionClient).sendAssetPageRetrievalMessageSameTx(AssetPageRetrievalMessage.encodeManualSubscriptionMessage(assetPageRetrievalMessage));
    }
}