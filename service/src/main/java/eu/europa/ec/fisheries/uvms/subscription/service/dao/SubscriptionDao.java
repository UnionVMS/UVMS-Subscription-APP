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

import javax.persistence.EntityManager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.europa.ec.fisheries.uvms.commons.service.dao.AbstractDAO;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.SubscriptionQueryDto;
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

    @SneakyThrows
    public List<SubscriptionEntity> listSubscriptions(SubscriptionQueryDto query) {

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("isEmpty", query != null ? query.isEmpty() : null);
        parameters.put("name", query != null ? query.getName() : null);
        parameters.put("organisation", query != null ? query.getOrganisation() : null);
        parameters.put("enabled", query != null ? query.getEnabled() : null);
        parameters.put("channel", query != null ? query.getChannel() : null);
        parameters.put("endPoint", query != null ? query.getEndPoint() : null);
        parameters.put("messageType", query != null ? query.getMessageType() : null);
        parameters.put("subscriptionType", query != null ? query.getSubscriptionType() : null);

        return findEntityByNamedQuery(SubscriptionEntity.class, LIST_SUBSCRIPTION, parameters);

    }

}
