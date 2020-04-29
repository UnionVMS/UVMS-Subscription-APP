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
import static eu.europa.ec.fisheries.wsdl.subscription.module.SubscriptionFeaturesEnum.MANAGE_SUBSCRIPTION;
import static eu.europa.ec.fisheries.wsdl.subscription.module.SubscriptionFeaturesEnum.VIEW_SUBSCRIPTION;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.jms.TextMessage;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.stream.Collectors;

import eu.europa.ec.fisheries.uvms.commons.message.api.MessageException;
import eu.europa.ec.fisheries.uvms.commons.message.impl.JAXBUtils;
import eu.europa.ec.fisheries.uvms.commons.service.interceptor.AuditActionEnum;
import eu.europa.ec.fisheries.uvms.subscription.service.authentication.AuthenticationContext;
import eu.europa.ec.fisheries.uvms.subscription.service.authorisation.AllowedRoles;
import eu.europa.ec.fisheries.uvms.subscription.service.dao.SubscriptionDao;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.EmailBodyEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.search.SubscriptionListQuery;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.SubscriptionDto;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.SubscriptionEmailConfigurationDto;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.list.SubscriptionListResponseDto;
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
import eu.europa.fisheries.uvms.subscription.model.exceptions.EntityDoesNotExistException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

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

    @Inject
    private SubscriptionAuditProducer auditProducer;

    @Inject
    private SubscriptionProducerBean subscriptionProducer;

    @Inject
    private CustomMapper customMapper;

    @Inject
    private AuthenticationContext authenticationContext;

    @Override
    @AllowedRoles(VIEW_SUBSCRIPTION)
    public SubscriptionDto findById(@NotNull Long id) {
        SubscriptionEntity entity = subscriptionDAO.findById(id);
        if (entity == null){
            throw new EntityDoesNotExistException("Subscription with id " + id);
        }
        EmailBodyEntity emailBodyEntity = subscriptionDAO.findEmailBodyEntity(entity.getId());
        return mapper.mapEntityToDto(entity, emailBodyEntity);
    }

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
        response.setSubscriptionCheck(SubscriptionPermissionAnswer.YES);
        return response;
    }

    /**
     * List subscriptions.
     *
     * @param queryParams the query parameters
     * @return page of listSubscriptions results
     */
    @Override
    @SneakyThrows
    @AllowedRoles(VIEW_SUBSCRIPTION)
    public SubscriptionListResponseDto listSubscriptions(@Valid @NotNull SubscriptionListQuery queryParams, String scopeName, String roleName) {

        SubscriptionListResponseDto responseDto = new SubscriptionListResponseDto();
        Integer pageSize = queryParams.getPagination().getPageSize();
        Integer page = queryParams.getPagination().getOffset();

        Long totalCount = subscriptionDAO.count(queryParams.getCriteria());

        List<SubscriptionEntity> subscriptionEntities = subscriptionDAO.listSubscriptions(queryParams);

        String getAllOrganisationRequest = UserModuleRequestMapper.mapToGetAllOrganisationRequest(scopeName, roleName, authenticationContext.getUserPrincipal().getName());

        String correlationID = subscriptionUserProducerBean.sendModuleMessage(getAllOrganisationRequest, subscriptionUserConsumerBean.getDestination());

        if (correlationID != null){

            TextMessage message = subscriptionUserConsumerBean.getMessage(correlationID, TextMessage.class );

            FindOrganisationsResponse responseMessage = JAXBUtils.unMarshallMessage( message.getText() , FindOrganisationsResponse.class);

            List<Organisation> organisationList = responseMessage.getOrganisation();

            responseDto.setList(customMapper.enrichSubscriptionList(subscriptionEntities,organisationList));

        } else {
            responseDto.setList(subscriptionEntities.stream().map(mapper::asListDto).collect(Collectors.toList()));
        }

        responseDto.setCurrentPage(page);
        long totalNumberOfPages = (totalCount / pageSize);
        responseDto.setTotalNumberOfPages(totalNumberOfPages + 1);
        responseDto.setTotalCount(totalCount);


        return responseDto;
    }

    @Override
    @SneakyThrows
    @AllowedRoles(MANAGE_SUBSCRIPTION)
    public SubscriptionDto create(@Valid @NotNull SubscriptionDto subscription) {
        SubscriptionEntity entity = mapper.mapDtoToEntity(subscription);
        SubscriptionEntity saved = subscriptionDAO.createEntity(entity);
        EmailBodyEntity emailBodyEntity = null;
        if(subscription.getOutput().getHasEmail()){
            emailBodyEntity = createEmailBody(saved, subscription.getOutput().getEmailConfiguration().getBody());
        }
        sendLogToAudit(mapToAuditLog(SUBSCRIPTION, AuditActionEnum.CREATE.name(), saved.getId().toString(), authenticationContext.getUserPrincipal().getName()));
        return mapper.mapEntityToDto(saved, emailBodyEntity);
    }

    @Override
    @SneakyThrows
    @AllowedRoles(MANAGE_SUBSCRIPTION)
    public SubscriptionDto update(@Valid @NotNull SubscriptionDto subscription) {
        SubscriptionEntity entityById = subscriptionDAO.findById(subscription.getId());
        if (entityById == null){
            throw new EntityDoesNotExistException("Subscription with id " + subscription.getId());
        }
        mapper.updateEntity(subscription, entityById);
        SubscriptionEntity subscriptionEntity = subscriptionDAO.update(entityById);
        EmailBodyEntity emailBodyEntity = null;
        if(subscription.getOutput().getHasEmail()){
            SubscriptionEmailConfigurationDto emailConfig = subscription.getOutput().getEmailConfiguration();
            emailBodyEntity = updateEmailBody(subscriptionEntity, emailConfig.getBody());

            if(emailConfig.getPasswordIsPlaceholder() != null && !emailConfig.getPasswordIsPlaceholder()) { //update password
                subscriptionDAO.updateEmailConfigurationPassword(subscriptionEntity.getId(), subscriptionEntity.getOutput().getEmailConfiguration().getPassword());
            } else { //password in unchanged, set placeholder according to stored password if existent
                String existingPassword = subscriptionDAO.getEmailConfigurationPassword(subscriptionEntity.getId());
                if(existingPassword != null){
                    subscriptionEntity.getOutput().getEmailConfiguration().setPassword("********");
                } else {
                    subscriptionEntity.getOutput().getEmailConfiguration().setPassword(null);
                }
            }
        }
        sendLogToAudit(mapToAuditLog(SUBSCRIPTION, AuditActionEnum.MODIFY.name(), subscriptionEntity.getId().toString(), authenticationContext.getUserPrincipal().getName()));
        return mapper.mapEntityToDto(subscriptionEntity, emailBodyEntity);
    }

    @Override
    @SneakyThrows
    @AllowedRoles(MANAGE_SUBSCRIPTION)
    public void delete(@NotNull Long id) {
        subscriptionDAO.delete(id);
        sendLogToAudit(mapToAuditLog(SUBSCRIPTION, AuditActionEnum.MODIFY.name(), String.valueOf(id), authenticationContext.getUserPrincipal().getName()));
    }

    public EmailBodyEntity createEmailBody(SubscriptionEntity subscription, String body){
        EmailBodyEntity entity = new EmailBodyEntity(subscription, body);
        return subscriptionDAO.createEmailBodyEntity(entity);
    }

    public EmailBodyEntity updateEmailBody(SubscriptionEntity subscription, String body){
        EmailBodyEntity entity = new EmailBodyEntity(subscription, body);
        return subscriptionDAO.updateEmailBodyEntity(entity);
    }

    private void sendLogToAudit(String log) throws MessageException {
        auditProducer.sendModuleMessage(log, subscriptionProducer.getDestination());
    }

    @Override
    @AllowedRoles(VIEW_SUBSCRIPTION)
    public Boolean checkNameAvailability(@NotNull final String name, Long id) {
        SubscriptionEntity subscriptionByName = subscriptionDAO.findSubscriptionByName(name);
        return subscriptionByName == null || subscriptionByName.getId().equals(id);
    }
}
