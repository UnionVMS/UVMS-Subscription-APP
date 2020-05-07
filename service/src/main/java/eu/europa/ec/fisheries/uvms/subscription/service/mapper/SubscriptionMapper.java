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

import eu.europa.ec.fisheries.uvms.subscription.service.domain.EmailBodyEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionOutput;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.SubscriptionDto;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEmailConfiguration;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.SubscriptionEmailConfigurationDto;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.SubscriptionOutputDto;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.list.SubscriptionListDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

@Mapper(componentModel = "cdi")
public interface SubscriptionMapper {

    @Mapping(source = "subscription.validityPeriod.startDate", target = "startDate")
    @Mapping(source = "subscription.validityPeriod.endDate", target = "endDate")
    @Mapping(expression = "java(subscriptionOutputToSubscriptionOutputDto(subscription.getOutput(), emailBody))", target = "output")
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
    SubscriptionEntity mapDtoToEntity(SubscriptionDto subscription);

    @Mapping(source = "startDate", target = "validityPeriod.startDate")
    @Mapping(source = "endDate", target = "validityPeriod.endDate")
    @Mapping(source = "dto.output.emailConfiguration.password", target = "output.emailConfiguration.password", qualifiedByName = "encodePasswordAsBase64")
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
}
