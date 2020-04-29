/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subscription.service.validation;

import static eu.europa.ec.fisheries.uvms.subscription.service.validation.ValidationUtil.requirePropertyNotNullWithMessage;
import static eu.europa.ec.fisheries.uvms.subscription.service.validation.ValidationUtil.requirePropertyNullWithMessage;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import eu.europa.ec.fisheries.uvms.subscription.service.dto.SubscriptionOutputDto;
import eu.europa.fisheries.uvms.subscription.model.enums.OutgoingMessageType;

/**
 * Implementation of custom validator for SubscriberDto in SubscriptionOutputDto.
 */
public class HasValidSubscriberDtoValidator implements ConstraintValidator<HasValidSubscriberDto, SubscriptionOutputDto> {

    @Override
    public boolean isValid(SubscriptionOutputDto output, ConstraintValidatorContext context) {
        boolean valid = true;
        if (output != null) {
            if(output.getMessageType() != OutgoingMessageType.NONE) {
                if(output.getSubscriber() != null){
                    valid = requirePropertyNotNullWithMessage(context, output.getSubscriber().getOrganisationId(),"Organisation ID is required","subscriber","organisationId")
                            & requirePropertyNotNullWithMessage(context, output.getSubscriber().getEndpointId(),"Endpoint ID is required","subscriber","endpointId")
                            & requirePropertyNotNullWithMessage(context, output.getSubscriber().getChannelId(),"Channel ID is required","subscriber","channelId");
                }
            } else {
                if(output.getSubscriber() != null){
                    valid = requirePropertyNullWithMessage(context, output.getSubscriber().getOrganisationId(),"Organisation ID must be empty","subscriber","organisationId")
                            & requirePropertyNullWithMessage(context, output.getSubscriber().getEndpointId(),"Endpoint ID must be empty","subscriber","endpointId")
                            & requirePropertyNullWithMessage(context, output.getSubscriber().getChannelId(),"Channel ID must be empty","subscriber","channelId");
                }
            }
        }
        return valid;
    }
}
