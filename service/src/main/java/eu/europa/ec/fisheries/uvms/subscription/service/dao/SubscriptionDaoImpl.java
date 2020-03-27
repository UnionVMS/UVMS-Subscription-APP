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
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import eu.europa.ec.fisheries.uvms.commons.date.DateUtils;
import eu.europa.ec.fisheries.uvms.commons.domain.DateRange_;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity_;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.search.ColumnType;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.search.DirectionType;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.search.SubscriptionListQuery;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.search.SubscriptionSearchCriteria;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;

@ApplicationScoped
@Slf4j
class SubscriptionDaoImpl implements SubscriptionDao {

    private static final String START_DATE = "startDate";
    private static final String END_DATE = "endDate";

    private EntityManager em;

    @Inject
    public SubscriptionDaoImpl(EntityManager em) {
        this.em = em;
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

        TypedQuery<SubscriptionEntity> selectQuery = em.createQuery(builder.toString(), SubscriptionEntity.class);

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
    public List<SubscriptionEntity> listSubscriptions(@Valid @NotNull SubscriptionListQuery subscriptionListParams) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<SubscriptionEntity> query = cb.createQuery(SubscriptionEntity.class);
        applyCriteria(query, subscriptionListParams.getCriteria());
        return em.createQuery(query)
                .setFirstResult((subscriptionListParams.getPagination().getOffset() - 1) * subscriptionListParams.getPagination().getPageSize())
                .setMaxResults(subscriptionListParams.getPagination().getPageSize())
                .getResultList();
    }

    @Override
    public Long count(@Valid @NotNull SubscriptionSearchCriteria criteria) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<SubscriptionEntity> root = query.from(SubscriptionEntity.class);
        query.select(cb.count(root));
        applyCriteria(query, criteria);
        return em.createQuery(query).getSingleResult();
    }

    private void applyCriteria(CriteriaQuery<?> query, SubscriptionSearchCriteria criteria) {
        Root<SubscriptionEntity> subscription = query.from(SubscriptionEntity.class);
        CriteriaBuilder cb = em.getCriteriaBuilder();
        List<Predicate> predicates = new ArrayList<>();
        // implement period correctly!
        if (criteria.getStartDate() != null) {
            predicates.add(cb.greaterThanOrEqualTo(subscription.get(SubscriptionEntity_.validityPeriod).get(DateRange_.startDate), Date.from(criteria.getStartDate().toInstant())));
        }
        if (criteria.getEndDate() != null) {
            predicates.add(cb.greaterThanOrEqualTo(subscription.get(SubscriptionEntity_.validityPeriod).get(DateRange_.startDate), Date.from(criteria.getEndDate().toInstant())));
        }
        // ...
    }

    @Override
    public SubscriptionEntity byName(@Valid @NotNull Map<String, Object> queryParameters) {
        TypedQuery<SubscriptionEntity> query = em.createNamedQuery(SubscriptionEntity.BY_NAME, SubscriptionEntity.class);
        queryParameters.forEach(query::setParameter);
        query.setMaxResults(1);
        return query.getResultList().stream().findFirst().orElse(null);
    }

    @Override
    public SubscriptionEntity createEntity(SubscriptionEntity entity) {
        em.persist(entity);
        return entity;
    }

    @Override
    public SubscriptionEntity findById(Long id) {
        return em.find(SubscriptionEntity.class, id);
    }

    @Override
    public SubscriptionEntity update(SubscriptionEntity entity) {
        return em.merge(entity);
    }

    @Override
    public void delete(Long id) {
        Object ref = em.getReference(SubscriptionEntity.class, id);
        em.remove(ref);
    }
}
