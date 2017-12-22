/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */

package eu.europa.ec.fisheries.uvms.subscription.service.mapper;

import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.SubscriptionDto;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;

@Mapper(uses = CustomMapper.class, componentModel = "cdi")
public interface SubscriptionMapper {

    @Mappings({
            @Mapping(target = "startDate", source = "validityPeriod.startDate"),
            @Mapping(target = "endDate", source = "validityPeriod.endDate"),
            @Mapping(target = "active", source = "enabled"),
            @Mapping(ignore = true, target = "conditions"),
            @Mapping(ignore = true, target = "areas")
    })
    SubscriptionDto mapEntityToDto(SubscriptionEntity subscription);

    @InheritInverseConfiguration
    @Mappings({
            @Mapping(target = "stateType", constant = "INACTIVE"),
            @Mapping(ignore = true, target = "conditions"),
            @Mapping(ignore = true, target = "areas"),
    })
    SubscriptionEntity mapDtoToEntity(SubscriptionDto subscription);

    @Mappings({
            @Mapping(source = "startDate", target = "validityPeriod.startDate"),
            @Mapping(source = "endDate", target = "validityPeriod.endDate"),
            @Mapping(source = "active", target = "enabled"),
            @Mapping(ignore = true, target = "conditions"),
            @Mapping(ignore = true, target = "guid"),
            @Mapping(ignore = true, target = "areas"),
            @Mapping(ignore = true, target = "stateType"),

    })
    void updateEntity(SubscriptionDto dto, @MappingTarget SubscriptionEntity entity);

}
