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
import static eu.europa.ec.fisheries.wsdl.subscription.module.AssetType.VESSEL;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import eu.europa.ec.fisheries.uvms.commons.service.dao.AbstractDAO;
import eu.europa.ec.fisheries.uvms.commons.service.dao.QueryParameter;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.mapper.SubscriptionQueryMapper;
import eu.europa.ec.fisheries.wsdl.subscription.module.AssetId;
import eu.europa.ec.fisheries.wsdl.subscription.module.AssetType;
import eu.europa.ec.fisheries.wsdl.subscription.module.SubscriptionQuery;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.NotImplementedException;

@Slf4j
public class SubscriptionDao extends AbstractDAO<SubscriptionEntity> {

    private static final String CFR_VALUES = "cfrValues";
    private static final String ORGANISATION = "organisation";
    private static final String CHANNEL = "channel";
    private static final String END_POINT = "endPoint";
    private static final String MESSAGE_TYPE = "messageType";
    private static final String DESCRIPTION = "description";
    private static final String NAME = "name";
    private static final String ACTIVE = "active";
    private static final String CFR_LIST_HAS_ITEMS = "cfrListHasItems";
    private static final String SYSTEM_AREA_LIST_HAS_ITEMS = "systemAreaListHasItems";

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
                .with(ORGANISATION, query != null ? query.getOrganisation() : null)
                .and(CHANNEL, query != null ? query.getChannel() : null)
                .and(END_POINT, query != null ? query.getEndPoint() : null)
                .and(MESSAGE_TYPE, query != null ? query.getMessageType() : null)
                .and(DESCRIPTION, query != null ? query.getDescription() : null)
                .and(NAME, query != null ? query.getName() : null)
                .and(ACTIVE, query != null ? query.isActive() : null)
                .and(CFR_LIST_HAS_ITEMS, 0)
                .and(CFR_VALUES, new ArrayList<>())
                .and(SYSTEM_AREA_LIST_HAS_ITEMS, 0)
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
                Map<String, Object> stringObjectMap = SubscriptionQueryMapper.mapAssetIdToMap(assetId);
                List<String> cfrValues = (List<String>) stringObjectMap.get(CFR_VALUES);
                parameters.put(CFR_LIST_HAS_ITEMS, cfrValues.size());
                parameters.put(CFR_VALUES, cfrValues);
            }
            else {
                throw new NotImplementedException();
            }
        }
    }
}
