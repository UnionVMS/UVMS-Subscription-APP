/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */

package eu.europa.ec.fisheries.uvms.subscription.service.dao;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import eu.europa.ec.fisheries.uvms.commons.domain.DateRange_;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity_;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionOutput_;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionSubscriber_;
import eu.europa.fisheries.uvms.subscription.model.enums.ColumnType;
import eu.europa.fisheries.uvms.subscription.model.enums.DirectionType;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.search.OrderByData;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.search.SubscriptionListQuery;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.search.SubscriptionSearchCriteria;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@ApplicationScoped
@Slf4j
class SubscriptionDaoImpl implements SubscriptionDao {

    private EntityManager em;

    @Inject
    public SubscriptionDaoImpl(EntityManager em) {
        this.em = em;
    }

    @Override
    public List<SubscriptionEntity> listSubscriptions(@Valid @NotNull SubscriptionListQuery subscriptionListParams) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<SubscriptionEntity> query = cb.createQuery(SubscriptionEntity.class);
        Root<SubscriptionEntity> subscription = query.from(SubscriptionEntity.class);
        query.select(subscription);
        applyCriteria(query, subscription, subscriptionListParams.getCriteria());
        applyOrder(query, subscription, subscriptionListParams.getOrderBy());
        return em.createQuery(query)
                .setFirstResult(subscriptionListParams.getPagination().getOffset() * subscriptionListParams.getPagination().getPageSize())
                .setMaxResults(subscriptionListParams.getPagination().getPageSize())
                .getResultList();
    }

    private void applyOrder(CriteriaQuery<?> query, Root<SubscriptionEntity> subscription, OrderByData<ColumnType> orderByData) {
        if(orderByData != null) {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            if (DirectionType.ASC.equals(orderByData.getDirection())) {
                query.orderBy(cb.asc(getColumn(subscription, orderByData.getField())), cb.asc(subscription.get(SubscriptionEntity_.id)));
            } else if (DirectionType.DESC.equals(orderByData.getDirection())) {
                query.orderBy(cb.desc(getColumn(subscription, orderByData.getField())), cb.desc(subscription.get(SubscriptionEntity_.id)));
            }
        }
    }

    private Expression<?> getColumn(Root<SubscriptionEntity> subscription, ColumnType column) {
        switch (column) {
            case NAME:
                return subscription.get(SubscriptionEntity_.name);
            case DESCRIPTION:
                return subscription.get(SubscriptionEntity_.description);
            case STARTDATE:
                return subscription.get(SubscriptionEntity_.validityPeriod).get(DateRange_.startDate);
            case ENDDATE:
                return subscription.get(SubscriptionEntity_.validityPeriod).get(DateRange_.endDate);
            case ENDPOINT:
                return subscription.get(SubscriptionEntity_.output).get(SubscriptionOutput_.subscriber).get(SubscriptionSubscriber_.endpointId);
            case CHANNEL:
                return subscription.get(SubscriptionEntity_.output).get(SubscriptionOutput_.subscriber).get(SubscriptionSubscriber_.channelId);
            case ORGANISATION:
                return subscription.get(SubscriptionEntity_.output).get(SubscriptionOutput_.subscriber).get(SubscriptionSubscriber_.organisationId);
            case MESSAGETYPE:
                return subscription.get(SubscriptionEntity_.output).get(SubscriptionOutput_.messageType);
            default:
                return subscription.get(SubscriptionEntity_.id);
        }
    }

    @Override
    public Long count(@Valid @NotNull SubscriptionSearchCriteria criteria) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<SubscriptionEntity> subscription = query.from(SubscriptionEntity.class);
        query.select(cb.count(subscription));
        applyCriteria(query, subscription, criteria);
        return em.createQuery(query).getSingleResult();
    }

    private void applyCriteria(CriteriaQuery<?> query, Root<SubscriptionEntity> subscription, SubscriptionSearchCriteria criteria) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        List<Predicate> predicates = new ArrayList<>();
        if(StringUtils.isNotEmpty(criteria.getName())){
            predicates.add(cb.like(subscription.get(SubscriptionEntity_.name), "%" + criteria.getName() + "%"));
        }
        if(criteria.getActive() != null){
            predicates.add(cb.equal(subscription.get(SubscriptionEntity_.active), criteria.getActive()));
        }
        if(criteria.getOrganisation() != null){
            predicates.add(cb.equal(subscription.get(SubscriptionEntity_.output).get(SubscriptionOutput_.subscriber).get(SubscriptionSubscriber_.organisationId), criteria.getOrganisation()));
        }
        if(criteria.getEndPoint() != null){
            predicates.add(cb.equal(subscription.get(SubscriptionEntity_.output).get(SubscriptionOutput_.subscriber).get(SubscriptionSubscriber_.endpointId), criteria.getEndPoint()));
        }
        if(criteria.getChannel() != null){
            predicates.add(cb.equal(subscription.get(SubscriptionEntity_.output).get(SubscriptionOutput_.subscriber).get(SubscriptionSubscriber_.channelId), criteria.getChannel()));
        }
        if(StringUtils.isNotEmpty(criteria.getDescription())){
            predicates.add(cb.like(subscription.get(SubscriptionEntity_.description),"%" +  criteria.getDescription() + "%"));
        }
        if (criteria.getStartDate() != null) {
            predicates.add(cb.lessThanOrEqualTo(subscription.get(SubscriptionEntity_.validityPeriod).get(DateRange_.startDate), Date.from(criteria.getStartDate().toInstant())));
            if (criteria.getEndDate() == null) {
                predicates.add(cb.greaterThan(subscription.get(SubscriptionEntity_.validityPeriod).get(DateRange_.endDate), Date.from(criteria.getStartDate().toInstant())));
            }
        }
        if (criteria.getEndDate() != null) {
            predicates.add(cb.greaterThanOrEqualTo(subscription.get(SubscriptionEntity_.validityPeriod).get(DateRange_.endDate), Date.from(criteria.getEndDate().toInstant())));
            if (criteria.getStartDate() == null) {
                predicates.add(cb.lessThan(subscription.get(SubscriptionEntity_.validityPeriod).get(DateRange_.startDate), Date.from(criteria.getEndDate().toInstant())));
            }
        }
        if(criteria.getMessageType() != null){
            predicates.add(cb.equal(subscription.get(SubscriptionEntity_.output).get(SubscriptionOutput_.messageType), criteria.getMessageType()));
        }
        if(criteria.getAccessibilityType() != null){
            predicates.add(cb.equal(subscription.get(SubscriptionEntity_.accessibility), criteria.getAccessibilityType()));
        }
        query.where(cb.and(predicates.toArray(new Predicate[0])));
    }

    @Override
    public SubscriptionEntity findSubscriptionByName(@NotNull String name) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<SubscriptionEntity> query = cb.createQuery(SubscriptionEntity.class);
        Root<SubscriptionEntity> subscription = query.from(SubscriptionEntity.class);
        query.select(subscription).where(cb.equal(subscription.get(SubscriptionEntity_.name), name));
        return em.createQuery(query).getResultList().stream().findFirst().orElse(null);
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
