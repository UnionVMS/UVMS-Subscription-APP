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

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class AssetPageRetrievalMessageTest {

    @ParameterizedTest
    @MethodSource("encodeMessageTestInput")
    void testEncodeManualSubscriptionMessage(String expectedEncodedMessage, AssetPageRetrievalMessage message) {
        String result = AssetPageRetrievalMessage.encodeManualSubscriptionMessage(message);
        assertEquals(expectedEncodedMessage, result);
    }

    protected static Stream<Arguments> encodeMessageTestInput() {
        return Stream.of(
                Arguments.of("g;500;greece-guid;0;10", new AssetPageRetrievalMessage(true, 500L, "greece-guid", 0L, 10L)),
                Arguments.of("m;500;mainAssets;1;10", new AssetPageRetrievalMessage(false, 500L, "mainAssets", 1L, 10L))
        );
    }

    @ParameterizedTest
    @MethodSource("decodeMessageTestInput")
    void testDecodeManualSubscriptionMessage(AssetPageRetrievalMessage expectedObject, String encodedInput) {
        AssetPageRetrievalMessage result = AssetPageRetrievalMessage.decodeManualSubscriptionMessage(encodedInput);
        assertEquals(expectedObject.isGroup(), result.isGroup());
        assertEquals(expectedObject.getSubscriptionId(), result.getSubscriptionId());
        assertEquals(expectedObject.getAssetGroupGuid(), result.getAssetGroupGuid());
        assertEquals(expectedObject.getPageNumber(), result.getPageNumber());
        assertEquals(expectedObject.getPageSize(), result.getPageSize());
    }

    protected static Stream<Arguments> decodeMessageTestInput() {
        return Stream.of(
                Arguments.of(new AssetPageRetrievalMessage(true, 500L, "greece-guid", 0L, 10L), "g;500;greece-guid;0;10"),
                Arguments.of(new AssetPageRetrievalMessage(false, 500L, "mainAssets", 1L, 10L), "m;500;mainAssets;1;10")
        );
    }
}