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
import static eu.europa.ec.fisheries.wsdl.subscription.module.MessageType.FLUX_FA_QUERY_MESSAGE;
import static eu.europa.ec.fisheries.wsdl.subscription.module.SubscriptionPermissionAnswer.NO;
import static eu.europa.ec.fisheries.wsdl.subscription.module.SubscriptionPermissionAnswer.YES;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europa.ec.fisheries.uvms.commons.message.api.MessageException;
import eu.europa.ec.fisheries.uvms.commons.message.impl.JAXBUtils;
import eu.europa.ec.fisheries.uvms.commons.rest.dto.PaginationDto;
import eu.europa.ec.fisheries.uvms.commons.service.interceptor.AuditActionEnum;
import eu.europa.ec.fisheries.uvms.subscription.service.dao.SubscriptionDao;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.search.ColumnType;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.search.DirectionType;
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

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.jms.TextMessage;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
@Slf4j
@Transactional
class SubscriptionServiceBean implements SubscriptionService {

    private static final String SUBSCRIPTION = "SUBSCRIPTION";

    @Inject
    private SubscriptionDao subscriptionDAO;

    @Inject
    private SubscriptionUserConsumerBean subscriptionUserConsumerBean;

    @Inject
    private SubscriptionUserProducerBean subscriptionUserProducerBean;

    @Inject
    private SubscriptionMapper mapper;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Inject
    private SubscriptionAuditProducer auditProducer;

    @Inject
    private SubscriptionProducerBean subscriptionProducer;

    /**
     * Check if the incoming message has a valid subscription
     * @param query filter criteria to retrieve subscriptions to be triggered
     * @return SubscriptionPermissionResponse
     */
    @Override
    public SubscriptionPermissionResponse hasActiveSubscriptions(SubscriptionDataQuery query) {
        SubscriptionPermissionResponse response = new SubscriptionPermissionResponse();
/*        Map<String, Object> stringObjectMap = CustomMapper.mapCriteriaToQueryParameters(query);
        stringObjectMap.put("strict", true); // only use exact match in query
        List<SubscriptionEntity> subscriptionEntities = subscriptionDAO.listSubscriptions(stringObjectMap, new HashMap<ColumnType, DirectionType>(),  -1 , -1);
        boolean empty = CollectionUtils.isEmpty(subscriptionEntities);
        if (empty){
            response.setSubscriptionCheck(NO);
        } else {
            response.setSubscriptionCheck(YES);
        }*/
        // Business wants to returnpermission denied in case of FA Query for untill the real implementation has been done, in Activity and Subscriptions!
        SubscriptionPermissionAnswer subscriptionPermissionAnswer = query.getMessageType() == FLUX_FA_QUERY_MESSAGE ? NO : YES;
        response.setSubscriptionCheck(subscriptionPermissionAnswer);
        return response;
    }

    /**
     * List subscriptions.
     *
     * @param parameters the query parameters
     * @param pagination the pagination parameters
     * @return page of listSubscriptions results
     */
    @Override
    @SneakyThrows
    public SubscriptionListResponseDto listSubscriptions(@Valid @NotNull QueryParameterDto parameters, @Valid @NotNull PaginationDto pagination,
                                                         @Valid @NotNull OrderByDto orderByDto, String scopeName, String roleName, String requester) {

        SubscriptionListResponseDto responseDto = new SubscriptionListResponseDto();

        Integer pageSize = pagination.getPageSize();
        Integer page = pagination.getOffset();

        @SuppressWarnings("unchecked")
        Map<String, Object> map = objectMapper.convertValue(parameters, Map.class);
        map.put("strict", false); // only use LIKE query

        Map<ColumnType, DirectionType> orderMap = Collections.singletonMap(orderByDto.getColumn(), orderByDto.getDirection());

        List<SubscriptionEntity> subscriptionEntities = subscriptionDAO.listSubscriptions(map, orderMap,  -1 , -1);

        int countResults = 0;

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

    @Override
    @SneakyThrows
    public SubscriptionDto create(@Valid @NotNull SubscriptionDto subscription, @NotNull String currentUser) {
        SubscriptionEntity entity = mapper.mapDtoToEntity(subscription);
        SubscriptionEntity saved = subscriptionDAO.createEntity(entity);
        sendLogToAudit(mapToAuditLog(SUBSCRIPTION, AuditActionEnum.CREATE.name(), saved.getId().toString(), currentUser));
        return mapper.mapEntityToDto(saved);
    }

    @Override
    @SneakyThrows
    public SubscriptionDto update(@Valid @NotNull SubscriptionDto subscription, @NotNull String currentUser) {
        SubscriptionEntity entityById = subscriptionDAO.findById(subscription.getId());

        if (entityById == null){
            throw new IllegalArgumentException("Unable to update entity: not found");
        }
        mapper.updateEntity(subscription, entityById);
        SubscriptionEntity subscriptionEntity = subscriptionDAO.update(entityById);
        sendLogToAudit(mapToAuditLog(SUBSCRIPTION, AuditActionEnum.MODIFY.name(), subscriptionEntity.getId().toString(), currentUser));
        return mapper.mapEntityToDto(subscriptionEntity);
    }

    @Override
    @SneakyThrows
    public void delete(@NotNull Long id, @NotNull String currentUser) {
        subscriptionDAO.delete(id);
        sendLogToAudit(mapToAuditLog(SUBSCRIPTION, AuditActionEnum.MODIFY.name(), String.valueOf(id), currentUser));
    }

    private void sendLogToAudit(String log) throws MessageException {
        auditProducer.sendModuleMessage(log, subscriptionProducer.getDestination());
    }

    @Override
    public SubscriptionEntity findSubscriptionByName(@NotNull final String name) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("name", name);
        return subscriptionDAO.byName(parameters);
    }
}
