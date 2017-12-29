/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */

package eu.europa.ec.fisheries.uvms.subscription.service.bean;

import static eu.europa.ec.fisheries.uvms.audit.model.mapper.AuditLogMapper.mapToAuditLog;
import static eu.europa.ec.fisheries.wsdl.subscription.module.SubscriptionPermissionAnswer.YES;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.interceptor.Interceptors;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europa.ec.fisheries.uvms.commons.rest.dto.PaginationDto;
import eu.europa.ec.fisheries.uvms.commons.service.interceptor.AuditActionEnum;
import eu.europa.ec.fisheries.uvms.commons.service.interceptor.ValidationInterceptor;
import eu.europa.ec.fisheries.uvms.subscription.service.dao.SubscriptionDao;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.ColumnType;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.DirectionType;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.OrderByDto;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.QueryParameterDto;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.SubscriptionDto;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.SubscriptionListResponseDto;
import eu.europa.ec.fisheries.uvms.subscription.service.mapper.SubscriptionMapper;
import eu.europa.ec.fisheries.wsdl.subscription.module.SubscriptionDataQuery;
import eu.europa.ec.fisheries.wsdl.subscription.module.SubscriptionPermissionResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;

@Stateless
@LocalBean
@Slf4j
public class SubscriptionServiceBean {

    private static final String SUBSCRIPTION = "SUBSCRIPTION";
    private SubscriptionDao subscriptionDAO;

    @EJB
    private SubscriptionProducerBean producer;

    @Inject
    private SubscriptionMapper mapper;

    private ObjectMapper objectMapper = new ObjectMapper();

    @PersistenceContext(unitName = "subscriptionPU")
    private EntityManager em;

    @PostConstruct
    public void init() {
        subscriptionDAO = new SubscriptionDao(em);
    }

    /**
     * Check if the incoming message has a valid subscription
     * @param query filter criteria to retrieve subscriptions to be triggered
     * @return SubscriptionPermissionResponse
     */
    @SuppressWarnings("unchecked")
    public SubscriptionPermissionResponse hasActiveSubscriptions(SubscriptionDataQuery query) {

        SubscriptionPermissionResponse response = new SubscriptionPermissionResponse();
        //TODO query subscritions
        response.setSubscriptionCheck(YES);
        return response;
    }

    /**
     * List subscriptions. Used over REST service.
     * @param parameters the query parameters
     * @param pagination the pagination parameters
     * @return page of listSubscriptions results
     */
    @SneakyThrows
    @Interceptors(ValidationInterceptor.class)
    public SubscriptionListResponseDto listSubscriptions(@NotNull QueryParameterDto parameters, @NotNull PaginationDto pagination, @NotNull OrderByDto orderByDto) {

        SubscriptionListResponseDto responseDto = new SubscriptionListResponseDto();

        Integer pageSize = pagination.getPageSize();
        Integer page = pagination.getOffset();

        @SuppressWarnings("unchecked")
        Map<String, Object> map = objectMapper.convertValue(parameters, Map.class);
        @SuppressWarnings("unchecked")
        Map<ColumnType, DirectionType> orderMap = new HashMap<>();
        orderMap.put(orderByDto.getColumn(), orderByDto.getDirection());

        List<SubscriptionEntity> subscriptionEntities = subscriptionDAO.listSubscriptions(map, orderMap,  -1 , -1);

        Integer countResults = 0;

        if (CollectionUtils.isNotEmpty(subscriptionEntities)){
            countResults = subscriptionEntities.size();
        }

        int firstResult = (page - 1) * pageSize;
        subscriptionEntities = subscriptionDAO.listSubscriptions(map, orderMap, firstResult , pageSize);
        responseDto.setList(subscriptionEntities);

        if (firstResult >= 0) {
            responseDto.setCurrentPage(page);
            int totalNumberOfPages = (countResults / pageSize);
            responseDto.setTotalNumberOfPages(totalNumberOfPages + 1);
        }

        return responseDto;
    }

    @SneakyThrows
    @Interceptors(ValidationInterceptor.class)
    public SubscriptionDto create(@NotNull SubscriptionDto subscription, @NotNull String currentUser) {
        SubscriptionEntity entity = mapper.mapDtoToEntity(subscription);
        SubscriptionEntity saved = subscriptionDAO.createEntity(entity);
        String log = mapToAuditLog(SUBSCRIPTION, AuditActionEnum.CREATE.name(), saved.getId().toString(), currentUser);
        producer.sendMessage(producer.getAuditEventQueue(), producer.getSubscriptionEventQueue(), log);
        return mapper.mapEntityToDto(saved);
    }

    @SneakyThrows
    @Interceptors(ValidationInterceptor.class)
    public SubscriptionDto update(@NotNull SubscriptionDto subscription, @NotNull String currentUser) {
        SubscriptionEntity entityById = subscriptionDAO.findEntityById(SubscriptionEntity.class, subscription.getId());
        mapper.updateEntity(subscription, entityById);
        SubscriptionEntity subscriptionEntity = subscriptionDAO.updateEntity(entityById);
        String log = mapToAuditLog(SUBSCRIPTION, AuditActionEnum.MODIFY.name(), subscriptionEntity.getId().toString(), currentUser);
        producer.sendMessage(producer.getAuditEventQueue(), producer.getSubscriptionEventQueue(), log);
        return mapper.mapEntityToDto(subscriptionEntity);
    }

    @SneakyThrows
    @Interceptors(ValidationInterceptor.class)
    public void delete(@NotNull Long id, @NotNull String currentUser) {
        subscriptionDAO.deleteEntity(SubscriptionEntity.class, id);
        String log = mapToAuditLog(SUBSCRIPTION, AuditActionEnum.MODIFY.name(), String.valueOf(id), currentUser);
        producer.sendMessage(producer.getAuditEventQueue(), producer.getSubscriptionEventQueue(), log);
    }

    @Interceptors(ValidationInterceptor.class)
    public SubscriptionEntity findSubscriptionByName(@NotNull final String name) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("name", name);
        return subscriptionDAO.byName(parameters);
    }
}
