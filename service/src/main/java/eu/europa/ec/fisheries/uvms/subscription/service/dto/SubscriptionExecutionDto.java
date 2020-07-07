/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subscription.service.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;

import eu.europa.ec.fisheries.uvms.subscription.service.validation.ValidSubscriptionExecutionDto;
import eu.europa.fisheries.uvms.subscription.model.enums.SubscriptionTimeUnit;
import eu.europa.fisheries.uvms.subscription.model.enums.TriggerType;
import lombok.Data;

/**
 * Encapsulates instructions for how and when to execute a subscription.
 */
@Data
@ValidSubscriptionExecutionDto
public class SubscriptionExecutionDto {

    private TriggerType triggerType;

    @Min(0)
    private Integer frequency;

    private SubscriptionTimeUnit frequencyUnit;

	private Boolean immediate;

    @Pattern(regexp = "^(?:23|22|21|20|[01]?[0-9]):[0-5][0-9]$")
    private String timeExpression;
}
