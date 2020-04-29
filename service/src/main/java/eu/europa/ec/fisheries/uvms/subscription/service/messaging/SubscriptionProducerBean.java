/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */

package eu.europa.ec.fisheries.uvms.subscription.service.messaging;

import javax.annotation.PostConstruct;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.jms.Queue;

import eu.europa.ec.fisheries.uvms.activity.model.exception.ActivityModelMarshallException;
import eu.europa.ec.fisheries.uvms.activity.model.mapper.JAXBMarshaller;
import eu.europa.ec.fisheries.uvms.activity.model.schemas.ActivityModuleMethod;
import eu.europa.ec.fisheries.uvms.activity.model.schemas.CreateAndSendFAQueryRequest;
import eu.europa.ec.fisheries.uvms.activity.model.schemas.PluginType;
import eu.europa.ec.fisheries.uvms.commons.message.api.MessageConstants;
import eu.europa.ec.fisheries.uvms.commons.message.api.MessageException;
import eu.europa.ec.fisheries.uvms.commons.message.impl.AbstractProducer;
import eu.europa.ec.fisheries.uvms.commons.message.impl.JMSUtils;
import lombok.extern.slf4j.Slf4j;

@Stateless
@LocalBean
@Slf4j
public class SubscriptionProducerBean extends AbstractProducer {

    private Queue activityQueue;

    @PostConstruct
    public void init() {
        activityQueue = JMSUtils.lookupQueue(MessageConstants.QUEUE_MODULE_ACTIVITY);
    }

    public String sendMessageToActivityQueue(String vesselId, String vesselIdSchemeId, Boolean consolidated, String startDate, String endDate, String organisation, String endpoint, String channel) throws ActivityModelMarshallException, MessageException {
        CreateAndSendFAQueryRequest request = new CreateAndSendFAQueryRequest(ActivityModuleMethod.CREATE_AND_SEND_FA_QUERY, PluginType.FLUX, vesselId, vesselIdSchemeId, consolidated, startDate, endDate, organisation, endpoint, channel);
        return this.sendMessageToSpecificQueue(JAXBMarshaller.marshallJaxBObjectToString(request), activityQueue, getDestination());
    }

    @Override
    public String getDestinationName() {
        return MessageConstants.QUEUE_SUBSCRIPTION_EVENT;
    }
}