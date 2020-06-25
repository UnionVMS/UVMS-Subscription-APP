/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subscription.service.messaging;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AssetPageRetrievalMessage {

    private final boolean isGroup;
    private final Long subscriptionId;
    private final String assetGroupGuid;
    private final Long pageNumber;
    private final Long pageSize;

    /**
     * Encodes the data included in the SubscriptionMessage
     * (is group, subscriptionId, an assetGroupName, the firstPage and the pageSize) into
     * the following string format: "g;subscriptionId;assetGroupName;pa
     * ge_number;page_size"
     *
     * @param assetPageRetrievalMessage including the data needed for the message
     * @return the encoded/formatted string
     */
    public static String encodeMessage(AssetPageRetrievalMessage assetPageRetrievalMessage) {
        return String.join(";",
                assetPageRetrievalMessage.isGroup() ? "g" : "m",
                assetPageRetrievalMessage.getSubscriptionId().toString(),
                assetPageRetrievalMessage.getAssetGroupGuid(),
                assetPageRetrievalMessage.getPageNumber().toString(),
                assetPageRetrievalMessage.getPageSize().toString());
    }

    /**
     * Decodes the isGroup flag, the subscriptionId, an assetGroupName, the firstPage and the pageSize
     * from an encoded string of format "g;subscriptionId;assetGroupName;page_number;page_size"
     * to a SubscriptionMessage object
     *
     * @param encodedMessage the encoded string
     * @return decoded message object
     */
    public static AssetPageRetrievalMessage decodeMessage(String encodedMessage) {
        String[] messageParameters = encodedMessage.split(";");
        return new AssetPageRetrievalMessage("g".equals(messageParameters[0]),
                Long.valueOf(messageParameters[1]),
                messageParameters[2],
                Long.valueOf(messageParameters[3]),
                Long.valueOf(messageParameters[4]));
    }
}
