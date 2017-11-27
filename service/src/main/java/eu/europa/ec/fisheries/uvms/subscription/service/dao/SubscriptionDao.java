/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */

package eu.europa.ec.fisheries.uvms.subscription.service.dao;

import static eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity.LIST_SUBSCRIPTION;
import static eu.europa.ec.fisheries.wsdl.subscription.module.AssetType.AIR;
import static eu.europa.ec.fisheries.wsdl.subscription.module.AssetType.VESSEL;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import eu.europa.ec.fisheries.uvms.commons.service.dao.AbstractDAO;
import eu.europa.ec.fisheries.uvms.commons.service.dao.QueryParameter;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity;
import eu.europa.ec.fisheries.wsdl.subscription.module.AssetId;
import eu.europa.ec.fisheries.wsdl.subscription.module.AssetIdList;
import eu.europa.ec.fisheries.wsdl.subscription.module.AssetIdType;
import eu.europa.ec.fisheries.wsdl.subscription.module.AssetType;
import eu.europa.ec.fisheries.wsdl.subscription.module.SubscriptionQuery;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SubscriptionDao extends AbstractDAO<SubscriptionEntity> {

    private EntityManager em;

    public SubscriptionDao(EntityManager em) {
        this.em = em;
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    @SuppressWarnings("unchecked")
    @SneakyThrows
    public List<SubscriptionEntity> listSubscriptions(SubscriptionQuery query) {

        Map parameters = QueryParameter
                .with("organisation", query != null ? query.getOrganisation() : null)
                .and("channel", query != null ? query.getChannel() : null)
                .and("endPoint", query != null ? query.getEndPoint() : null)
                .and("messageType", query != null ? query.getMessageType() : null)
                .and("description", query != null ? query.getDescription() : null)
                .and("name", query != null ? query.getName() : null)
                .and("active", query != null ? query.isActive() : null)
                .and("cfrListHasItems", 0)
                .and("cfrValues", new ArrayList<>())
                .parameters();

        if (query != null && query.getAssetId() != null){
            getAssetIdentifiers(query, parameters);
        }

        return findEntityByNamedQuery(SubscriptionEntity.class, LIST_SUBSCRIPTION, parameters);
    }

    private void getAssetIdentifiers(SubscriptionQuery query, Map<String, Object> parameters) {
        AssetId assetId = query.getAssetId();

        if (assetId != null){
            AssetType assetType = assetId.getAssetType();
            if (VESSEL.equals(assetType)) {
                getVesselIdentifierParameters(parameters, assetId);
            }
            if (AIR.equals(assetType)){
                log.debug("Yeah right :-)");
            }
        }
    }

    private void getVesselIdentifierParameters(Map<String, Object> parameters, AssetId assetId) {

        List<AssetIdList> assetIdList = assetId.getAssetIdList();
        List<String> cfrValues = new ArrayList<>();
        Integer cfrListHasItems = cfrValues.size();

        for (AssetIdList idList : assetIdList){

            AssetIdType idType = idList.getIdType();

            switch (idType){
                case CFR:
                    cfrValues.add(idList.getValue());
                    cfrListHasItems++;
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
        parameters.put("cfrListHasItems", cfrListHasItems);

    }
}
