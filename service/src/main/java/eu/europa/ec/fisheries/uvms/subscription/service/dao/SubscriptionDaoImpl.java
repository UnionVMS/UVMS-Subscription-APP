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

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

import eu.europa.ec.fisheries.uvms.commons.date.DateUtils;
import eu.europa.ec.fisheries.uvms.commons.service.dao.AbstractDAO;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.ColumnType;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.DirectionType;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;

@ApplicationScoped
@Slf4j
class SubscriptionDaoImpl extends AbstractDAO<SubscriptionEntity> implements SubscriptionDao {

    private static final String START_DATE = "startDate";
    private static final String END_DATE = "endDate";

    private EntityManager em;

    @Inject
    public SubscriptionDaoImpl(EntityManager em) {
        this.em = em;
    }

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    @Override
    @SneakyThrows
    public List<SubscriptionEntity> listSubscriptions(@Valid @NotNull Map<String, Object> queryParameters, @Valid @NotNull Map<ColumnType, DirectionType> orderBy, @Valid @NotNull Integer firstResult, @Valid @NotNull Integer maxResult) {

        String queryString = em.createNamedQuery(LIST_SUBSCRIPTION).unwrap(org.hibernate.Query.class).getQueryString();

        List<SubscriptionEntity> resultList = null;

        StringBuilder builder = new StringBuilder(queryString).append(" ORDER BY s.");

        if (MapUtils.isNotEmpty(orderBy)){
            Map.Entry<ColumnType, DirectionType> next = orderBy.entrySet().iterator().next();
            builder.append(next.getKey().propertyName()).append(" ").append(next.getValue().name());
        }
        else {
            builder.append("id ASC");
        }

        TypedQuery<SubscriptionEntity> selectQuery = getEntityManager().createQuery(builder.toString(), SubscriptionEntity.class);

        Object startDate = queryParameters.get(START_DATE);
        Object endDate = queryParameters.get(END_DATE);

        if (startDate != null && endDate == null) {
            queryParameters.put(END_DATE, DateUtils.END_OF_TIME.toDate());
        }

        if (endDate != null && startDate == null) {
            queryParameters.put(START_DATE, DateUtils.START_OF_TIME.toDate());
        }

        if (endDate == null && startDate == null){
            queryParameters.put(END_DATE, DateUtils.END_OF_TIME.toDate());
            queryParameters.put(START_DATE, DateUtils.START_OF_TIME.toDate());
        }

        for (Map.Entry<String, Object> entry : queryParameters.entrySet()){
            selectQuery.setParameter(entry.getKey(), entry.getValue());
        }

        if (firstResult >= 0 && maxResult > 0){
            selectQuery.setFirstResult(firstResult);
            selectQuery.setMaxResults(maxResult);
        }

        try {
            resultList = selectQuery.getResultList();
        }
        catch (Exception e){
            log.error(e.getLocalizedMessage(),e);
        }
        return resultList;
    }

    @Override
    @SneakyThrows
    public SubscriptionEntity byName(@Valid @NotNull Map<String, Object> queryParameters){

        SubscriptionEntity entity = null;

        List<SubscriptionEntity> entityByNamedQuery = findEntityByNamedQuery(SubscriptionEntity.class, SubscriptionEntity.BY_NAME, queryParameters, 1);
        if (CollectionUtils.isNotEmpty(entityByNamedQuery)){
            entity = entityByNamedQuery.get(0);
        }
        return entity;
    }
}
