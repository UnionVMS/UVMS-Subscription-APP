/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */

package eu.europa.ec.fisheries.uvms.subscription.service.mapper;

import java.util.Base64;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import eu.europa.ec.fisheries.uvms.subscription.service.domain.AreaEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.AssetEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.AssetGroupEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.EmailBodyEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEmailConfiguration;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionFishingActivity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionOutput;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.AreaDto;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.AssetDto;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.SubscriptionDto;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.SubscriptionEmailConfigurationDto;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.SubscriptionFishingActivityDto;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.SubscriptionOutputDto;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.list.SubscriptionListDto;
import eu.europa.fisheries.uvms.subscription.model.enums.AssetType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

@Mapper(componentModel = "cdi")
public interface SubscriptionMapper {

    @Mapping(source = "subscription.validityPeriod.startDate", target = "startDate")
    @Mapping(source = "subscription.validityPeriod.endDate", target = "endDate")
    @Mapping(expression = "java(subscriptionOutputToSubscriptionOutputDto(subscription.getOutput(), emailBody))", target = "output")
    @Mapping(expression = "java(collectEntityAssetsToSet(subscription.getAssets(), subscription.getAssetGroups()))", target = "assets")
    SubscriptionDto mapEntityToDto(SubscriptionEntity subscription, EmailBodyEntity emailBody);

    @Mapping(expression = "java(subscriptionEmailConfigurationToSubscriptionEmailConfigurationDto(output.getEmailConfiguration(), emailBody))", target = "emailConfiguration")
    SubscriptionOutputDto subscriptionOutputToSubscriptionOutputDto(SubscriptionOutput output, EmailBodyEntity emailBody);

    @Mapping(constant = "true", target = "passwordIsPlaceholder")
    @Mapping(source = "emailConfiguration.password", target = "password", qualifiedByName = "getPlaceHolder")
    @Mapping(source = "emailBody.body", target="body")
    SubscriptionEmailConfigurationDto subscriptionEmailConfigurationToSubscriptionEmailConfigurationDto(SubscriptionEmailConfiguration emailConfiguration, EmailBodyEntity emailBody);

    @Mapping(source = "subscription.startDate", target = "validityPeriod.startDate")
    @Mapping(source = "subscription.endDate", target = "validityPeriod.endDate")
    @Mapping(source = "subscription.output.emailConfiguration.password", target = "output.emailConfiguration.password", qualifiedByName = "encodePasswordAsBase64")
    @Mapping(expression = "java(getAreaEntitiesFromAreaDtos(subscription.getAreas()))", target = "areas")
    @Mapping(expression = "java(extractAssetEntitiesFromDto(subscription.getAssets()))", target = "assets")
    @Mapping(expression = "java(extractAssetGroupEntitiesFromDto(subscription.getAssets()))", target = "assetGroups")
    @Mapping(expression = "java(extractSubscriptionActivityFromDto(subscription.getStartActivities()))", target = "startActivities")
    @Mapping(expression = "java(extractSubscriptionActivityFromDto(subscription.getStopActivities()))", target = "stopActivities")
    SubscriptionEntity mapDtoToEntity(SubscriptionDto subscription);

    @Mapping(source = "startDate", target = "validityPeriod.startDate")
    @Mapping(source = "endDate", target = "validityPeriod.endDate")
    @Mapping(source = "dto.output.emailConfiguration.password", target = "output.emailConfiguration.password", qualifiedByName = "encodePasswordAsBase64")
    @Mapping(expression = "java(getAreaEntitiesFromAreaDtos(dto.getAreas()))", target = "areas")
    @Mapping(expression = "java(extractAssetEntitiesFromDto(dto.getAssets()))", target = "assets")
    @Mapping(expression = "java(extractAssetGroupEntitiesFromDto(dto.getAssets()))", target = "assetGroups")
    @Mapping(expression = "java(extractSubscriptionActivityFromDto(dto.getStartActivities()))", target = "startActivities")
    @Mapping(expression = "java(extractSubscriptionActivityFromDto(dto.getStopActivities()))", target = "stopActivities")
    void updateEntity(SubscriptionDto dto, @MappingTarget SubscriptionEntity entity);

    SubscriptionListDto asListDto(SubscriptionEntity entity);

    @Named("encodePasswordAsBase64")
    static String encodePasswordAsBase64(String password){
        if(password != null && !password.isEmpty()){
            return Base64.getEncoder().encodeToString(password.getBytes());
        } else {
            return null;
        }
    }

    @Named("getPlaceHolder")
    static String getPlaceHolder(String password){
        if(password != null){
            return "********";
        } else {
            return null;
        }
    }

    default Set<AssetDto> collectEntityAssetsToSet(Set<AssetEntity> assetEntities, Set<AssetGroupEntity> assetGroups) {
        Set<AssetDto> assets = new HashSet<>();
        assets.addAll(getAssetDtosFromAssetEntities(assetEntities));
        assets.addAll(getAssetDtosFromAssetGroupEntities(assetGroups));
        return assets;
    }

    default Set<AssetDto> getAssetDtosFromAssetEntities(Set<AssetEntity> assetEntities) {
        return Optional.ofNullable(assetEntities).orElse(Collections.emptySet()).stream()
                .map(assetEntity -> new AssetDto(assetEntity.getId(), assetEntity.getGuid(), assetEntity.getName(), AssetType.ASSET))
                .collect(Collectors.toSet());
    }

    default Set<AssetDto> getAssetDtosFromAssetGroupEntities(Set<AssetGroupEntity> assetGroupEntities) {
        return Optional.ofNullable(assetGroupEntities).orElse(Collections.emptySet()).stream()
                .map(assetGroupEntity -> new AssetDto(assetGroupEntity.getId(), assetGroupEntity.getGuid(), assetGroupEntity.getName(), AssetType.VGROUP))
                .collect(Collectors.toSet());
    }

    default Set<AreaEntity> getAreaEntitiesFromAreaDtos(Set<AreaDto> areaDtos) {
        return Optional.ofNullable(areaDtos).orElse(Collections.emptySet()).stream()
                .map(this::areaEntityFromDto)
                .collect(Collectors.toSet());
    }

    default Set<AssetEntity> extractAssetEntitiesFromDto(Set<AssetDto> assetDtos) {
        return Optional.ofNullable(assetDtos).orElse(Collections.emptySet()).stream()
                .filter(dto -> dto.getType().equals(AssetType.ASSET))
                .map(this::assetEntityFromDto)
                .collect(Collectors.toSet());
    }

    default Set<AssetGroupEntity> extractAssetGroupEntitiesFromDto(Set<AssetDto> assetDtos) {
        return Optional.ofNullable(assetDtos).orElse(Collections.emptySet()).stream()
                .filter(dto -> dto.getType().equals(AssetType.VGROUP))
                .map(this::assetGroupEntityFromDto)
                .collect(Collectors.toSet());
    }
    default Set<SubscriptionFishingActivity> extractSubscriptionActivityFromDto(Set<SubscriptionFishingActivityDto> startActivitiesDto) {
        return Optional.ofNullable(startActivitiesDto).orElse(Collections.emptySet()).stream()
                .map(this::subscriptionActivityFromDto)
                .collect(Collectors.toSet());
    }

    SubscriptionFishingActivity subscriptionActivityFromDto(SubscriptionFishingActivityDto dto);

    AreaEntity areaEntityFromDto(AreaDto dto);

    AssetEntity assetEntityFromDto(AssetDto dto);

    AssetGroupEntity assetGroupEntityFromDto(AssetDto dto);
}
