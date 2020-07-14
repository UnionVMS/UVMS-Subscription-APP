/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subscription.service.validation;

import static eu.europa.ec.fisheries.uvms.subscription.service.validation.ValidationUtil.require;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import eu.europa.ec.fisheries.uvms.subscription.service.dto.SubscriptionDto;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.SubscriptionExecutionDto;
import eu.europa.fisheries.uvms.subscription.model.enums.OutgoingMessageType;
import eu.europa.fisheries.uvms.subscription.model.enums.TriggerType;

/**
 * Implementation of custom validator for SubscriptionExecutiontDto.
 */
public class SubscriptionDtoExecutionValidator implements ConstraintValidator<ValidSubscriptionDtoExecution, SubscriptionDto> {

    @Override
    public boolean isValid(SubscriptionDto subscriptionDto, ConstraintValidatorContext context) {
        boolean valid = true;
        if (subscriptionDto != null && subscriptionDto.getOutput() != null) {
            if (OutgoingMessageType.POSITION.equals(subscriptionDto.getOutput().getMessageType())) {
                valid = require(context, "TriggerType must be INC POSITION when output message type is POSITION", subscriptionDto)
                        .path("execution", SubscriptionDto::getExecution)
                        .path("triggerType", SubscriptionExecutionDto::getTriggerType)
                        .toBe(TriggerType.INC_POSITION::equals);
            }
        }
        return valid;
    }
}
