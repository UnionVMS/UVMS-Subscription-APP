/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */

package eu.europa.ec.fisheries.uvms.subscription.service.domain;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;
import eu.europa.ec.fisheries.uvms.commons.domain.DateRange;
import eu.europa.ec.fisheries.uvms.subscription.service.type.TriggerType;
import eu.europa.ec.fisheries.wsdl.subscription.module.MessageType;
import eu.europa.ec.fisheries.wsdl.subscription.module.SubscriptionType;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Entity
@Data
@RequiredArgsConstructor
public class SubscriptionEntity implements Serializable {

    @JsonProperty("subscription_type")
    @NotNull
    @Enumerated(EnumType.STRING)
    private SubscriptionType subscriptionType;

    @JsonProperty("subscription_type")
    @NotNull
    @Enumerated(EnumType.STRING)
    private MessageType messageType;

    @JsonProperty("name")
    @NotNull
    private String name;

    private String description;

    private Boolean active;

    @Embedded
    private DateRange validityPeriod;

    @NotNull
    private String organisation;

    @NotNull
    private String endPoint;

    @NotNull
    private String channel;

    private String startCondition;

    private String endCondition;

    @Enumerated(EnumType.STRING)
    @NotNull
    private TriggerType trigger;

    private String delay;

    @JsonProperty("start")
    public Date getStartDate(){
        return validityPeriod.getStartDate();
    }

    @JsonProperty("end")
    public Date getEndDateDate(){
        return validityPeriod.getStartDate();
    }

}
