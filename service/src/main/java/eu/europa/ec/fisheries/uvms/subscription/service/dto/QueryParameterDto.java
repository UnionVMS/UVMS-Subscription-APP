/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */

package eu.europa.ec.fisheries.uvms.subscription.service.dto;

import static com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING;
import static eu.europa.ec.fisheries.uvms.commons.date.DateUtils.DATE_TIME_UI_FORMAT;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.AccessibilityType;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionType;
import eu.europa.ec.fisheries.wsdl.subscription.module.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryParameterDto {

    private String name;
    private String organisation;
    private Boolean enabled;
    private String channel;
    private String endPoint;
    private MessageType messageType;
    private SubscriptionType subscriptionType;
    private String description;
    private AccessibilityType accessibility;

   @JsonFormat(shape = STRING, pattern = DATE_TIME_UI_FORMAT)
   private Date startDate;
   @JsonFormat(shape = STRING, pattern = DATE_TIME_UI_FORMAT)
   private Date endDate;
}
