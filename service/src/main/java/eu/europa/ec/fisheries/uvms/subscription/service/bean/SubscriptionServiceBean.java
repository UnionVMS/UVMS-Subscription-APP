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
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.interceptor.Interceptors;
import javax.jms.TextMessage;
import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europa.ec.fisheries.uvms.commons.message.api.MessageException;
import eu.europa.ec.fisheries.uvms.commons.message.impl.JAXBUtils;
import eu.europa.ec.fisheries.uvms.commons.rest.dto.PaginationDto;
import eu.europa.ec.fisheries.uvms.commons.service.interceptor.AuditActionEnum;
import eu.europa.ec.fisheries.uvms.commons.service.interceptor.ValidationInterceptor;
import eu.europa.ec.fisheries.uvms.subscription.service.dao.SubscriptionDao;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.*;
import eu.europa.ec.fisheries.uvms.subscription.service.mapper.CustomMapper;
import eu.europa.ec.fisheries.uvms.subscription.service.mapper.SubscriptionMapper;
import eu.europa.ec.fisheries.uvms.subscription.service.messaging.SubscriptionAuditProducer;
import eu.europa.ec.fisheries.uvms.subscription.service.messaging.SubscriptionProducerBean;
import eu.europa.ec.fisheries.uvms.user.model.mapper.UserModuleRequestMapper;
import eu.europa.ec.fisheries.wsdl.subscription.module.SubscriptionDataQuery;
import eu.europa.ec.fisheries.wsdl.subscription.module.SubscriptionPermissionAnswer;
import eu.europa.ec.fisheries.wsdl.subscription.module.SubscriptionPermissionResponse;
import eu.europa.ec.fisheries.wsdl.user.module.FindOrganisationsResponse;
import eu.europa.ec.fisheries.wsdl.user.types.Organisation;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import static eu.europa.ec.fisheries.uvms.audit.model.mapper.AuditLogMapper.mapToAuditLog;
import static eu.europa.ec.fisheries.wsdl.subscription.module.SubscriptionPermissionAnswer.NO;
import static eu.europa.ec.fisheries.wsdl.subscription.module.SubscriptionPermissionAnswer.YES;

@Stateless
@LocalBean
@Slf4j
public class SubscriptionServiceBean extends BaseSubscriptionBean {

    private static final String SUBSCRIPTION = "SUBSCRIPTION";
    private SubscriptionDao subscriptionDAO;

    @EJB
    private SubscriptionProducerBean producer;

    @EJB
    private SubscriptionUserConsumerBean subscriptionUserConsumerBean;

    @EJB
    private SubscriptionUserProducerBean subscriptionUserProducerBean;

    @Inject
    private SubscriptionMapper mapper;

    private ObjectMapper objectMapper = new ObjectMapper();

    @EJB
    private SubscriptionAuditProducer auditProducer;

    @EJB
    private SubscriptionProducerBean subscriptionProducer;

    @PostConstruct
    public void init() {
        initEntityManager();
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
        Map<String, Object> stringObjectMap = new HashMap<>();
        stringObjectMap.put("strict", true); // exact match not like
        try {
            stringObjectMap = CustomMapper.mapCriteriaToQueryParameters(query);
        }
        catch (Exception e){
            log.warn(e.getMessage(), e);
        }
        List<SubscriptionEntity> subscriptionEntities = subscriptionDAO.listSubscriptions(stringObjectMap, new HashMap<>(),  -1 , -1);
        SubscriptionPermissionAnswer subscriptionPermissionAnswer = NO;
        if (CollectionUtils.isNotEmpty(subscriptionEntities)){
            subscriptionPermissionAnswer = YES;
        }
        response.setSubscriptionCheck(subscriptionPermissionAnswer);
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
    public SubscriptionListResponseDto listSubscriptions(@NotNull QueryParameterDto parameters, @NotNull PaginationDto pagination,
                                                         @NotNull OrderByDto orderByDto, String scopeName, String roleName, String requester) {

        SubscriptionListResponseDto responseDto = new SubscriptionListResponseDto();

        Integer pageSize = pagination.getPageSize();
        Integer page = pagination.getOffset();

        @SuppressWarnings("unchecked")
        Map<String, Object> map = objectMapper.convertValue(parameters, Map.class);
        map.put("strict", false); // only use LIKE query

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

        String getAllOrganisationRequest = UserModuleRequestMapper.mapToGetAllOrganisationRequest(scopeName, roleName, requester);

        String correlationID = subscriptionUserProducerBean.sendModuleMessage(getAllOrganisationRequest, subscriptionUserConsumerBean.getDestination());

        if (correlationID != null){

            TextMessage message = subscriptionUserConsumerBean.getMessage(correlationID, TextMessage.class );

            FindOrganisationsResponse responseMessage = JAXBUtils.unMarshallMessage( message.getText() , FindOrganisationsResponse.class);

            List<Organisation> organisationList = responseMessage.getOrganisation();

            responseDto.setList(CustomMapper.enrichSubscriptionList(subscriptionEntities,organisationList));

        }else
            responseDto.setList( subscriptionEntities );

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
        sendLogToAudit(mapToAuditLog(SUBSCRIPTION, AuditActionEnum.CREATE.name(), saved.getId().toString(), currentUser));
        return mapper.mapEntityToDto(saved);
    }

    @SneakyThrows
    @Interceptors(ValidationInterceptor.class)
    public SubscriptionDto update(@NotNull SubscriptionDto subscription, @NotNull String currentUser) {
        SubscriptionEntity entityById = subscriptionDAO.findEntityById(SubscriptionEntity.class, subscription.getId());

        if (entityById == null){
            throw new IllegalArgumentException("Unable to update entity: not found");
        }
        mapper.updateEntity(subscription, entityById);
        SubscriptionEntity subscriptionEntity = subscriptionDAO.updateEntity(entityById);
        sendLogToAudit(mapToAuditLog(SUBSCRIPTION, AuditActionEnum.MODIFY.name(), subscriptionEntity.getId().toString(), currentUser));
        return mapper.mapEntityToDto(subscriptionEntity);
    }

    @SneakyThrows
    @Interceptors(ValidationInterceptor.class)
    public void delete(@NotNull Long id, @NotNull String currentUser) {
        subscriptionDAO.deleteEntity(SubscriptionEntity.class, id);
        sendLogToAudit(mapToAuditLog(SUBSCRIPTION, AuditActionEnum.MODIFY.name(), String.valueOf(id), currentUser));
    }

    public void sendLogToAudit(String log) throws MessageException {
        auditProducer.sendModuleMessage(log, subscriptionProducer.getDestination());
    }

    @Interceptors(ValidationInterceptor.class)
    public SubscriptionEntity findSubscriptionByName(@NotNull final String name) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("name", name);
        return subscriptionDAO.byName(parameters);
    }
}
