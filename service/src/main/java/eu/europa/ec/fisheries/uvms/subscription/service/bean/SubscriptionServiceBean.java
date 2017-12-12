/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */

package eu.europa.ec.fisheries.uvms.subscription.service.bean;

import javax.annotation.PostConstruct;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

import eu.europa.ec.fisheries.uvms.subscription.service.dao.SubscriptionDao;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.SubscriptionDto;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.SubscriptionListPayload;
import eu.europa.ec.fisheries.uvms.subscription.service.mapper.SubscriptionMapper;
import eu.europa.ec.fisheries.wsdl.subscription.module.SubscriptionDataQuery;
import eu.europa.ec.fisheries.wsdl.subscription.module.SubscriptionDataResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Stateless
@LocalBean
@Slf4j
public class SubscriptionServiceBean {

    private SubscriptionDao subscriptionDAO;

    @Inject
    private SubscriptionMapper mapper;

    @PersistenceContext(unitName = "subscriptionPU")
    private EntityManager em;

    @PostConstruct
    public void init() {
        subscriptionDAO = new SubscriptionDao(em);
    }

    /**
     * Check if the incoming message has a valid subscription
     * @param query filter criteria to retrieve subscriptions to be triggered
     * @return ?
     */
    @SuppressWarnings("unchecked")
    public SubscriptionDataResponse isValid(SubscriptionDataQuery query) {
        

        return new SubscriptionDataResponse();
    }

    /**
     * Search for subscriptions synchronously. Used over REST service.
     * @param payload filter criteria
     * @return page of list results
     */
    public List<SubscriptionEntity> list(SubscriptionListPayload payload) {
        return subscriptionDAO.listSubscriptions(payload);
    }

    @SneakyThrows
    public SubscriptionDto create(SubscriptionDto subscription) {
        SubscriptionEntity entity = mapper.mapDtoToEntity(subscription);
        SubscriptionEntity saved = subscriptionDAO.createEntity(entity);
        return mapper.mapEntityToDto(saved);
    }

    @SneakyThrows
    public SubscriptionDto update(SubscriptionDto subscription) {
        SubscriptionEntity entityById = subscriptionDAO.findEntityById(SubscriptionEntity.class, subscription.getId());
        entityById.merge(subscription);
        return mapper.mapEntityToDto(entityById);
    }
}
