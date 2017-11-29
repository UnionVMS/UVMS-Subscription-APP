/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */

package eu.europa.ec.fisheries.uvms.subscription.service.mapper;

import static lombok.AccessLevel.PRIVATE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.europa.ec.fisheries.wsdl.subscription.module.AssetId;
import eu.europa.ec.fisheries.wsdl.subscription.module.AssetIdList;
import eu.europa.ec.fisheries.wsdl.subscription.module.AssetIdType;
import lombok.NoArgsConstructor;

/**
 * TODO create test
 */
@NoArgsConstructor(access = PRIVATE)
public class SubscriptionQueryMapper {

    public static Map<String, Object> mapAssetIdToMap(AssetId assetId){

        Map<String, Object> parameters = new HashMap<>();

        List<AssetIdList> assetIdList = assetId.getAssetIdList();
        List<String> cfrValues = new ArrayList<>();
        for (AssetIdList idList : assetIdList){

            AssetIdType idType = idList.getIdType();

            switch (idType){
                case CFR:
                    cfrValues.add(idList.getValue());
                    break;
                case ID:
                    break;
                case GUID:
                    break;
                case IMO:
                    break;
                case IRCS:
                    break;
                case MMSI:
                    break;
                default:
            }

        }

        parameters.put("cfrValues", cfrValues);
        return parameters;
    }
}
