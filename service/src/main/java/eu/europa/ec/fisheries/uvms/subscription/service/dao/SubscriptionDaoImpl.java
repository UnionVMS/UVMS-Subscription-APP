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
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.SetJoin;
import javax.persistence.criteria.Subquery;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import eu.europa.ec.fisheries.uvms.commons.domain.DateRange_;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.AreaEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.AreaEntity_;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.AssetEntity_;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.AssetGroupEntity_;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.EmailBodyEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.EmailBodyEntity_;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEmailConfiguration_;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity_;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionExecution_;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionOutput_;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionSubscriber_;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.search.OrderByData;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.search.SubscriptionListQuery;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.search.SubscriptionSearchCriteria;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.search.SubscriptionSearchCriteria.AreaCriterion;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.search.SubscriptionSearchCriteria.AssetCriterion;
import eu.europa.fisheries.uvms.subscription.model.enums.AssetType;
import eu.europa.fisheries.uvms.subscription.model.enums.ColumnType;
import eu.europa.fisheries.uvms.subscription.model.enums.DirectionType;
import eu.europa.fisheries.uvms.subscription.model.exceptions.EntityDoesNotExistException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * {@link SubscriptionDao} JPA implementation.
 */
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
        CriteriaQuery<SubscriptionEntity> query = makeSubscriptionEntityCriteriaQuery(subscriptionListParams.getCriteria(), subscriptionListParams.getOrderBy());
        return em.createQuery(query)
                .setFirstResult(subscriptionListParams.getPagination().getOffset() * subscriptionListParams.getPagination().getPageSize())
                .setMaxResults(subscriptionListParams.getPagination().getPageSize())
                .getResultList();
    }

    @Override
    public List<SubscriptionEntity> listSubscriptions(@Valid @NotNull SubscriptionSearchCriteria criteria) {
        CriteriaQuery<SubscriptionEntity> query = makeSubscriptionEntityCriteriaQuery(criteria, null);
        return em.createQuery(query).getResultList();
    }

    private CriteriaQuery<SubscriptionEntity> makeSubscriptionEntityCriteriaQuery(SubscriptionSearchCriteria criteria, OrderByData<ColumnType> orderBy) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<SubscriptionEntity> query = cb.createQuery(SubscriptionEntity.class);
        Root<SubscriptionEntity> subscription = query.from(SubscriptionEntity.class);
        query.select(subscription);
        applyCriteria(query, subscription, criteria);
        if (orderBy != null) {
            applyOrder(query, subscription, orderBy);
        }
        return query;
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
        if (criteria.getValidAt() != null) {
            predicates.add(cb.between(
                    cb.literal(Date.from(criteria.getValidAt().toInstant())),
                    subscription.get(SubscriptionEntity_.validityPeriod).get(DateRange_.startDate),
                    subscription.get(SubscriptionEntity_.validityPeriod).get(DateRange_.endDate)
            ));
        }
        if(criteria.getMessageType() != null){
            predicates.add(cb.equal(subscription.get(SubscriptionEntity_.output).get(SubscriptionOutput_.messageType), criteria.getMessageType()));
        }
        if(criteria.getAccessibilityType() != null){
            predicates.add(cb.equal(subscription.get(SubscriptionEntity_.accessibility), criteria.getAccessibilityType()));
        }
        if(criteria.getInAnyArea() != null && !criteria.getInAnyArea().isEmpty()) {
            predicates.add(makeAreasSubquery(query, subscription, cb, criteria.getInAnyArea()));
        }
        if(criteria.getWithAnyAsset() != null && !criteria.getWithAnyAsset().isEmpty()) {
            makeAssetCriteriaPredicate(query, subscription, cb, criteria.getWithAnyAsset()).ifPresent(predicates::add);
        }
        if(criteria.getWithAnyTriggerType() != null && !criteria.getWithAnyTriggerType().isEmpty()) {
            predicates.add(subscription.get(SubscriptionEntity_.execution).get(SubscriptionExecution_.triggerType).in(criteria.getWithAnyTriggerType()));
        }
        query.where(cb.and(predicates.toArray(new Predicate[0])));
    }

    private Predicate makeAreasSubquery(CriteriaQuery<?> query, Root<SubscriptionEntity> subscription, CriteriaBuilder cb, Collection<AreaCriterion> areas) {
        Subquery<Long> subquery = query.subquery(Long.class);
        Root<AreaEntity> area = subquery.from(AreaEntity.class);
        subquery.select(area.get(AreaEntity_.id));
        Predicate[] predicates = areas.stream()
                .map(a -> cb.and(cb.equal(area.get(AreaEntity_.gid), a.getGid()), cb.equal(area.get(AreaEntity_.areaType), a.getType())))
                .toArray(Predicate[]::new);
        subquery.where(cb.and(cb.equal(area.get(AreaEntity_.subscription), subscription), cb.or(predicates)));
        return cb.exists(subquery);
    }

    private Optional<Predicate> makeAssetCriteriaPredicate(CriteriaQuery<?> query, Root<SubscriptionEntity> subscription, CriteriaBuilder cb, Collection<AssetCriterion> assetsCriteria) {
        Map<AssetType, List<AssetCriterion>> assetTypeMap = assetsCriteria.stream().collect(Collectors.groupingBy(AssetCriterion::getType));
        Predicate assetsSubquery = Optional.ofNullable(assetTypeMap.get(AssetType.ASSET))
                .map(assetCriteria -> makeGenericAssetsSubquery(query, subscription, cb, assetCriteria, SubscriptionEntity_.assets, AssetEntity_.guid))
                .orElse(null);
        Predicate assetGroupsSubquery = Optional.ofNullable(assetTypeMap.get(AssetType.VGROUP))
                .map(assetCriteria -> makeGenericAssetsSubquery(query, subscription, cb, assetCriteria, SubscriptionEntity_.assetGroups, AssetGroupEntity_.guid))
                .orElse(null);
        return Optional.ofNullable(orNullables(cb, assetsSubquery, assetGroupsSubquery));
    }

    private <E> Predicate makeGenericAssetsSubquery(CriteriaQuery<?> query, Root<SubscriptionEntity> subscription, CriteriaBuilder cb, Collection<AssetCriterion> assetsCriteria, SetAttribute<SubscriptionEntity, E> join, SingularAttribute<E, String> guid) {
        Subquery<Long> subquery = query.subquery(Long.class);
        Root<SubscriptionEntity> correlatedRoot = subquery.correlate(subscription);
        SetJoin<SubscriptionEntity, E> assetRoot = correlatedRoot.join(join);
        subquery.select(cb.literal(1L));
        List<String> ids = assetsCriteria.stream()
                .map(AssetCriterion::getGuid)
                .collect(Collectors.toList());
        subquery.where(assetRoot.get(guid).in(ids));
        return cb.exists(subquery);
    }

    private Predicate orNullables(CriteriaBuilder cb, Predicate p1, Predicate p2) {
        if (p1 != null) {
            return p2 == null ? p1 : cb.or(p1,p2);
        } else {
            return p2;
        }
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
        setAreasSubscription(entity);
        setAssetsSubscription(entity);
        setAssetGroupsSubscription(entity);
        em.persist(entity);
        return entity;
    }

    @Override
    public SubscriptionEntity findById(Long id) {
        return em.find(SubscriptionEntity.class, id);
    }

    @Override
    public EmailBodyEntity findEmailBodyEntity(Long id) {
       return em.find(EmailBodyEntity.class, id);
    }

    @Override
    public EmailBodyEntity createEmailBodyEntity(EmailBodyEntity entity) {
        em.persist(entity);
        return entity;
    }

    @Override
    public EmailBodyEntity updateEmailBodyEntity(EmailBodyEntity entity) {
        if(em.find(EmailBodyEntity.class, entity.getSubscription().getId())==null){
            em.persist(entity);
            return entity;
        }
        em.merge(entity);
        return entity;
    }

    @Override
    public SubscriptionEntity update(SubscriptionEntity entity) {
        setAreasSubscription(entity);
        setAssetsSubscription(entity);
        setAssetGroupsSubscription(entity);
        updateAreas(entity);
        updateAssets(entity);
        updateAssetGroups(entity);
        return em.merge(entity);
    }

    @Override
    public void updateEmailConfigurationPassword(Long id, String password) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaUpdate<SubscriptionEntity> update = cb.createCriteriaUpdate(SubscriptionEntity.class);
        Root<SubscriptionEntity> subscription = update.from(SubscriptionEntity.class);
        update.set(subscription.get(SubscriptionEntity_.output).get(SubscriptionOutput_.emailConfiguration).get(SubscriptionEmailConfiguration_.password), password);
        update.where(cb.equal(subscription.get(SubscriptionEntity_.id), id));
        em.createQuery(update).executeUpdate();
    }

    @Override
    public String getEmailConfigurationPassword(Long id) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<String> query = cb.createQuery(String.class);
        Root<SubscriptionEntity> subscription = query.from(SubscriptionEntity.class);
        query.select(subscription.get(SubscriptionEntity_.output).get(SubscriptionOutput_.emailConfiguration).get(SubscriptionEmailConfiguration_.password)).where(cb.equal(subscription.get(SubscriptionEntity_.id), id));
        return em.createQuery(query).getResultList().stream().findFirst().orElse(null);
    }

    @Override
    public void delete(Long id) {
        SubscriptionEntity subscription = em.find(SubscriptionEntity.class, id);
        if (subscription == null) {
            throw new EntityDoesNotExistException("Subscription with id " + id);
        }
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaDelete<EmailBodyEntity> delete = cb.createCriteriaDelete(EmailBodyEntity.class);
        Root<EmailBodyEntity> fromEmailBodyEntity = delete.from(EmailBodyEntity.class);
        delete.where(cb.equal(fromEmailBodyEntity.get(EmailBodyEntity_.SUBSCRIPTION), subscription));
        em.remove(subscription);
    }

    private void setAreasSubscription(SubscriptionEntity subscription) {
        if(subscription.getAreas() != null){
            subscription.getAreas().forEach(area -> area.setSubscription(subscription));
        }
    }

    private void setAssetsSubscription(SubscriptionEntity subscription) {
        if(subscription.getAssets() != null){
            subscription.getAssets().forEach(asset -> asset.setSubscription(subscription));
        }
    }

    private void setAssetGroupsSubscription(SubscriptionEntity subscription) {
        if(subscription.getAssetGroups() != null){
            subscription.getAssetGroups().forEach(assetGroup -> assetGroup.setSubscription(subscription));
        }
    }

    private void updateAreas(SubscriptionEntity subscription) {
        if(subscription.getAreas() != null){
            subscription.getAreas().stream().filter(area -> area.getId()!=null).forEach(em::merge);
        }
    }

    private void updateAssets(SubscriptionEntity subscription) {
        if(subscription.getAssets() != null){
            subscription.getAssets().stream().filter(asset -> asset.getId()!=null).forEach(em::merge);
        }
    }

    private void updateAssetGroups(SubscriptionEntity subscription) {
        if(subscription.getAssetGroups() != null){
            subscription.getAssetGroups().stream().filter(assetGroup -> assetGroup.getId()!=null).forEach(em::merge);
        }
    }
}
