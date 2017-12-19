/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */

package eu.europa.ec.fisheries.uvms.subscription.service.dao;

import static eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity.COUNT_SUBSCRIPTION;
import static eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity.LIST_SUBSCRIPTION;

import javax.interceptor.Interceptors;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.europa.ec.fisheries.uvms.commons.service.dao.AbstractDAO;
import eu.europa.ec.fisheries.uvms.commons.service.interceptor.ValidationInterceptor;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.SubscriptionListPayload;
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

    public Long count(){
        Query namedQuery = getEntityManager().createNamedQuery(COUNT_SUBSCRIPTION);
        return (Long) namedQuery.getSingleResult();
    }

    @SneakyThrows
    @Interceptors(ValidationInterceptor.class)
    public List<SubscriptionEntity> listSubscriptions(@Valid SubscriptionListPayload payload, @NotNull Integer firstResult, @NotNull Integer maxResult) {

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("isEmpty", payload.isEmpty());
        parameters.put("name", payload.getName());
        parameters.put("organisation", payload.getOrganisation());
        parameters.put("enabled", payload.getEnabled());
        parameters.put("channel", payload.getChannel());
        parameters.put("endPoint", payload.getEndPoint());
        parameters.put("messageType", payload.getMessageType());
        parameters.put("subscriptionType", payload.getSubscriptionType());

        Query selectQuery = getEntityManager().createNamedQuery(LIST_SUBSCRIPTION);

        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            selectQuery.setParameter(entry.getKey(), entry.getValue());
        }

        if (firstResult > 0 && maxResult > 0){
            selectQuery.setFirstResult(firstResult);
            selectQuery.setMaxResults(maxResult);
        }

        return selectQuery.getResultList();
    }

}
