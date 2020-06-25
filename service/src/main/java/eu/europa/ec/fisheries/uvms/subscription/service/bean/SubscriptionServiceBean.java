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
import static java.lang.Boolean.TRUE;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import eu.europa.ec.fisheries.uvms.commons.domain.DateRange;
import eu.europa.ec.fisheries.uvms.commons.message.api.MessageException;
import eu.europa.ec.fisheries.uvms.commons.service.interceptor.AuditActionEnum;
import eu.europa.ec.fisheries.uvms.subscription.service.authentication.AuthenticationContext;
import eu.europa.ec.fisheries.uvms.subscription.service.authorisation.AllowedRoles;
import eu.europa.ec.fisheries.uvms.subscription.service.dao.SubscriptionDao;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.AreaEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.AssetEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.AssetGroupEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.EmailBodyEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.search.SubscriptionListQuery;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.AreaDto;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.AssetDto;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.SubscriptionDto;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.SubscriptionEmailConfigurationDto;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.SubscriptionExecutionDto;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.list.SubscriptionListResponseDto;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.search.SubscriptionListQueryImpl;
import eu.europa.ec.fisheries.uvms.subscription.service.mapper.CustomMapper;
import eu.europa.ec.fisheries.uvms.subscription.service.mapper.SubscriptionMapper;
import eu.europa.ec.fisheries.uvms.subscription.service.messaging.AssetPageRetrievalMessage;
import eu.europa.ec.fisheries.uvms.subscription.service.messaging.SubscriptionAuditProducer;
import eu.europa.ec.fisheries.uvms.subscription.service.messaging.SubscriptionProducerBean;
import eu.europa.ec.fisheries.uvms.subscription.service.messaging.usm.UsmClient;
import eu.europa.ec.fisheries.uvms.subscription.service.messaging.SubscriptionSender;
import eu.europa.ec.fisheries.uvms.subscription.service.messaging.asset.AssetSender;
import eu.europa.ec.fisheries.uvms.subscription.service.util.DateTimeService;
import eu.europa.ec.fisheries.wsdl.asset.types.AssetHistGuidIdWithVesselIdentifiers;
import eu.europa.ec.fisheries.wsdl.asset.types.VesselIdentifiersHolder;
import eu.europa.ec.fisheries.wsdl.subscription.module.SubscriptionDataQuery;
import eu.europa.ec.fisheries.wsdl.subscription.module.SubscriptionPermissionAnswer;
import eu.europa.ec.fisheries.wsdl.subscription.module.SubscriptionPermissionResponse;
import eu.europa.ec.fisheries.wsdl.user.types.Organisation;
import eu.europa.fisheries.uvms.subscription.model.enums.AssetType;
import eu.europa.fisheries.uvms.subscription.model.enums.SubscriptionTimeUnit;
import eu.europa.fisheries.uvms.subscription.model.enums.TriggerType;
import eu.europa.fisheries.uvms.subscription.model.exceptions.EntityDoesNotExistException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;

@ApplicationScoped
@Slf4j
@Transactional
class SubscriptionServiceBean implements SubscriptionService {

    private static final DateTimeFormatter TIME_EXPRESSION_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private static final String SUBSCRIPTION = "SUBSCRIPTION";
    private static final String MAIN_ASSETS = "mainAssets";
    private static final long PAGE_SIZE_FOR_MANUAL = 500L;

    @Inject
    private DateTimeService dateTimeService;

    @Inject
    private SubscriptionDao subscriptionDAO;

    @Inject
    private UsmClient usmClient;

    @Inject
    private AssetSender assetSender;

    @Inject
    private SubscriptionMapper mapper;

    @Inject
    private SubscriptionAuditProducer auditProducer;

    @Inject
    private SubscriptionSender subscriptionSender;

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
        if (entity == null) {
            throw new EntityDoesNotExistException("Subscription with id " + id);
        }
        EmailBodyEntity emailBodyEntity = subscriptionDAO.findEmailBodyEntity(entity.getId());
        return mapper.mapEntityToDto(entity, emailBodyEntity);
    }

    /**
     * Check if the incoming message has a valid subscription
     *
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
        SubscriptionListQuery updatedQueryParams = excludeManualFromQueryParams(queryParams);

        SubscriptionListResponseDto responseDto = new SubscriptionListResponseDto();
        Integer pageSize = updatedQueryParams.getPagination().getPageSize();
        Integer page = updatedQueryParams.getPagination().getOffset();

        Long totalCount = subscriptionDAO.count(updatedQueryParams.getCriteria());

        List<SubscriptionEntity> subscriptionEntities = subscriptionDAO.listSubscriptions(updatedQueryParams);

        List<Organisation> organisations = usmClient.getAllOrganisations(scopeName, roleName, authenticationContext.getUserPrincipal().getName());
        if (organisations != null) {
            responseDto.setList(customMapper.enrichSubscriptionList(subscriptionEntities, organisations));
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
    @AllowedRoles(MANAGE_SUBSCRIPTION)
    public SubscriptionDto prepareManualRequest(@NotNull SubscriptionDto subscriptionDto) {
        subscriptionDto.setName(UUID.randomUUID().toString());
        subscriptionDto.setActive(true);
        Optional.ofNullable(subscriptionDto.getOutput()).ifPresent(output -> output.setHasEmail(false));
        Optional.ofNullable(subscriptionDto.getOutput()).ifPresent(output -> output.setLogbook(false));
        SubscriptionExecutionDto execution = new SubscriptionExecutionDto();
        execution.setTriggerType(TriggerType.MANUAL);
        execution.setImmediate(true);
        execution.setFrequency(0);
        execution.setFrequencyUnit(SubscriptionTimeUnit.DAYS);
        subscriptionDto.setExecution(execution);
        return subscriptionDto;
    }

    @Override
    @SneakyThrows
    @AllowedRoles(MANAGE_SUBSCRIPTION)
    public SubscriptionDto createManual(@Valid @NotNull SubscriptionDto subscription) {
        SubscriptionEntity entity = mapper.mapDtoToEntity(subscription);
        setValidityPeriodForManualTrigger(entity);
        entity.setHasAssets((entity.getAssets() != null && !entity.getAssets().isEmpty()) || (entity.getAssetGroups() != null && !entity.getAssetGroups().isEmpty()));
        if (!entity.getAssets().isEmpty()) {
            enrichNewAssets(entity.getAssets());
        }
        SubscriptionEntity saved = subscriptionDAO.createEntity(entity);
        sendAssetPageRetrievalMessages(saved);
        sendLogToAudit(mapToAuditLog(SUBSCRIPTION, AuditActionEnum.CREATE.name(), saved.getId().toString(), authenticationContext.getUserPrincipal().getName()));
        return mapper.mapEntityToDto(saved, null);
    }

    @Override
    @SneakyThrows
    @AllowedRoles(MANAGE_SUBSCRIPTION)
    public SubscriptionDto create(@Valid @NotNull SubscriptionDto subscription) {
        SubscriptionEntity entity = mapper.mapDtoToEntity(subscription);
        enrichNewAssets(entity.getAssets());
        entity.setHasAreas(subscription.getAreas() != null && !subscription.getAreas().isEmpty());
        entity.setHasAssets(subscription.getAssets() != null && !subscription.getAssets().isEmpty());
        entity.setHasSenders(subscription.getSenders() != null && !subscription.getSenders().isEmpty());
        entity.setHasStartActivities(subscription.getStartActivities() != null && !subscription.getStartActivities().isEmpty());
        setNextScheduleExecutionIfApplicable(entity);
        SubscriptionEntity saved = subscriptionDAO.createEntity(entity);
        EmailBodyEntity emailBodyEntity = null;
        if (TRUE.equals(subscription.getOutput().getHasEmail())) {
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
        if (entityById == null) {
            throw new EntityDoesNotExistException("Subscription with id " + subscription.getId());
        }
        updateExistingAreasWithId(subscription.getAreas(), entityById.getAreas());
        updateExistingAssetsWithId(subscription.getAssets(), entityById.getAssets(), entityById.getAssetGroups());
        Map<Long, AssetEntity> subscriptionAssets = entityById.getAssets().stream().collect(Collectors.toMap(AssetEntity::getId, Function.identity()));
        mapper.updateEntity(subscription, entityById);
        enrichAssets(entityById, subscriptionAssets);
        entityById.setHasAreas(subscription.getAreas() != null && !subscription.getAreas().isEmpty());
        entityById.setHasAssets(subscription.getAssets() != null && !subscription.getAssets().isEmpty());
        entityById.setHasSenders(subscription.getSenders() != null && !subscription.getSenders().isEmpty());
        entityById.setHasStartActivities(subscription.getStartActivities() != null && !subscription.getStartActivities().isEmpty());
        SubscriptionEntity subscriptionEntity = subscriptionDAO.update(entityById);
        EmailBodyEntity emailBodyEntity = null;
        if (TRUE.equals(subscription.getOutput().getHasEmail())) {
            SubscriptionEmailConfigurationDto emailConfig = subscription.getOutput().getEmailConfiguration();
            emailBodyEntity = updateEmailBody(subscriptionEntity, emailConfig.getBody());

            if (emailConfig.getPasswordIsPlaceholder() != null && !emailConfig.getPasswordIsPlaceholder()) { //update password
                subscriptionDAO.updateEmailConfigurationPassword(subscriptionEntity.getId(), subscriptionEntity.getOutput().getEmailConfiguration().getPassword());
            } else { //password in unchanged, set placeholder according to stored password if existent
                String existingPassword = subscriptionDAO.getEmailConfigurationPassword(subscriptionEntity.getId());
                if (existingPassword != null) {
                    subscriptionEntity.getOutput().getEmailConfiguration().setPassword("********");
                } else {
                    subscriptionEntity.getOutput().getEmailConfiguration().setPassword(null);
                }
            }
        }
        sendLogToAudit(mapToAuditLog(SUBSCRIPTION, AuditActionEnum.MODIFY.name(), subscriptionEntity.getId().toString(), authenticationContext.getUserPrincipal().getName()));
        return mapper.mapEntityToDto(subscriptionEntity, emailBodyEntity);
    }

    private void enrichAssets(SubscriptionEntity entity, Map<Long, AssetEntity> subscriptionAssets) {
        Set<AssetEntity> newAssets = entity.getAssets().stream()
                .map(asset -> {
                    AssetEntity assetEntity = subscriptionAssets.get(asset.getId());
                    if (assetEntity != null) {
                        asset.setCfr(assetEntity.getCfr());
                        asset.setIccat(assetEntity.getIccat());
                        asset.setIrcs(assetEntity.getIrcs());
                        asset.setUvi(assetEntity.getUvi());
                        asset.setExtMark(assetEntity.getExtMark());
                        return null;
                    } else {
                        return asset;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        enrichNewAssets(newAssets);
    }

    private void enrichNewAssets(Set<AssetEntity> assets) {
        if (assets.isEmpty()) {
            return;
        }
        List<String> newAssetsIds = assets.stream().map(AssetEntity::getGuid).collect(Collectors.toList());
        Map<String, AssetHistGuidIdWithVesselIdentifiers> newIdentifiers = assetSender.findMultipleVesselIdentifiers(newAssetsIds).stream().collect(Collectors.toMap(AssetHistGuidIdWithVesselIdentifiers::getAssetHistGuid, Function.identity()));
        assets.forEach(asset -> {
            AssetHistGuidIdWithVesselIdentifiers guidWithIdentifiers = newIdentifiers.get(asset.getGuid());
            if (guidWithIdentifiers != null) {
                VesselIdentifiersHolder identifiers = guidWithIdentifiers.getIdentifiers();
                asset.setCfr(identifiers.getCfr());
                asset.setIrcs(identifiers.getIrcs());
                asset.setIccat(identifiers.getIccat());
                asset.setUvi(identifiers.getUvi());
                asset.setExtMark(identifiers.getExtMark());
            }
        });
    }

    @Override
    @SneakyThrows
    @AllowedRoles(MANAGE_SUBSCRIPTION)
    public void delete(@NotNull Long id) {
        subscriptionDAO.delete(id);
        sendLogToAudit(mapToAuditLog(SUBSCRIPTION, AuditActionEnum.MODIFY.name(), String.valueOf(id), authenticationContext.getUserPrincipal().getName()));
    }

    public EmailBodyEntity createEmailBody(SubscriptionEntity subscription, String body) {
        EmailBodyEntity entity = new EmailBodyEntity(subscription, body);
        return subscriptionDAO.createEmailBodyEntity(entity);
    }

    public EmailBodyEntity updateEmailBody(SubscriptionEntity subscription, String body) {
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

    @SneakyThrows
    private SubscriptionListQuery excludeManualFromQueryParams(SubscriptionListQuery queryParams) {
        SubscriptionListQueryImpl updatedQueryParams = (SubscriptionListQueryImpl) BeanUtils.cloneBean(queryParams);
        updatedQueryParams.getCriteria().setWithAnyTriggerType(excludeManualFromTriggerTypes(updatedQueryParams));
        return updatedQueryParams;
    }

    private Set<TriggerType> excludeManualFromTriggerTypes(SubscriptionListQuery queryParams) {
        Collection<TriggerType> triggerTypes = queryParams.getCriteria().getWithAnyTriggerType();
        if (triggerTypes == null || triggerTypes.isEmpty() || Collections.singletonList(TriggerType.MANUAL).containsAll(triggerTypes)) {
            triggerTypes = Arrays.asList(TriggerType.values());
        }
        return triggerTypes
                .stream()
                .filter(t -> !TriggerType.MANUAL.equals(t))
                .collect(Collectors.toSet());
    }

    private void updateExistingAreasWithId(Set<AreaDto> newAreas, Set<AreaEntity> oldAreas) {
        if (newAreas != null && oldAreas != null) {
            for (AreaDto areaDto : newAreas) {
                for (AreaEntity areaEntity : oldAreas) {
                    if (areaDto.getGid().equals(areaEntity.getGid()) && areaDto.getAreaType().equals(areaEntity.getAreaType())) {
                        areaDto.setId(areaEntity.getId());
                        break;
                    }
                }
            }
        }
    }

    private void updateExistingAssetsWithId(Set<AssetDto> newAssets, Set<AssetEntity> oldAssets, Set<AssetGroupEntity> oldAssetGroups) {
        if (newAssets != null) {
            for (AssetDto assetDto : newAssets) {
                if (assetDto.getType().equals(AssetType.ASSET) && oldAssets != null) {
                    oldAssets.stream().filter(asset -> asset.getGuid().equals(assetDto.getGuid())).findFirst().ifPresent(asset -> assetDto.setId(asset.getId()));
                } else if (assetDto.getType().equals(AssetType.VGROUP) && oldAssetGroups != null) {
                    oldAssetGroups.stream().filter(assetGroup -> assetGroup.getGuid().equals(assetDto.getGuid())).findFirst().ifPresent(assetGroup -> assetDto.setId(assetGroup.getId()));
                }
            }
        }
    }

    private void setValidityPeriodForManualTrigger(SubscriptionEntity entity) {
        DateRange validityPeriod = new DateRange();
        Instant now = Instant.now();
        validityPeriod.setStartDate(new Date(now.toEpochMilli()));
        validityPeriod.setEndDate(new Date(now.plus(365, ChronoUnit.DAYS).toEpochMilli()));
        entity.setValidityPeriod(validityPeriod);
    }

    private void sendAssetPageRetrievalMessages(SubscriptionEntity subscriptionEntity) {
        for (AssetGroupEntity assetGroup : subscriptionEntity.getAssetGroups()) {
            subscriptionSender.sendAssetPageRetrievalMessageSameTx(new AssetPageRetrievalMessage(true, subscriptionEntity.getId(), assetGroup.getGuid(), 1L, PAGE_SIZE_FOR_MANUAL));
        }
        if (!subscriptionEntity.getAssets().isEmpty()) {
            subscriptionSender.sendAssetPageRetrievalMessageSameTx(new AssetPageRetrievalMessage(false, subscriptionEntity.getId(), MAIN_ASSETS, 1L, PAGE_SIZE_FOR_MANUAL));
        }
    }

    private void setNextScheduleExecutionIfApplicable(SubscriptionEntity entity) {
        if (TriggerType.SCHEDULER.equals(entity.getExecution().getTriggerType())){
            entity.getExecution().setNextScheduledExecution(calculateNextScheduledExecutionDate(entity));
        }
    }

    private Date calculateNextScheduledExecutionDate(SubscriptionEntity subscriptionEntity) {
        Instant date = dateTimeService.getNowAsInstant();
        // we need to adjust the requestedTime to
        // the next occurrence of timeExpression, which might be tomorrow
        LocalTime time = LocalTime.parse(subscriptionEntity.getExecution().getTimeExpression(), TIME_EXPRESSION_FORMAT);
        Instant timeToday = time.atDate(dateTimeService.getToday()).toInstant(ZoneOffset.UTC);
        date = timeToday.isAfter(date) ? timeToday : timeToday.plus(1L, ChronoUnit.DAYS);
        return Date.from(date);
    }
}
