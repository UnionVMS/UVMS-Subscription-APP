/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subscription.service.messaging;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Collections;

import lombok.SneakyThrows;

@ApplicationScoped
public class JmsSubscriptionClientImpl implements SubscriptionClient {

    private static final String JMS_MESSAGE_SOURCE_KEY = "subscriptionSource";
    private static final String SOURCE_MANUAL = "manual";
    private static final String SOURCE_SCHEDULED = "scheduled";

    private SubscriptionManualProducerBean producerBean;

    @Inject
    public JmsSubscriptionClientImpl(SubscriptionManualProducerBean producerBean) {
        this.producerBean = producerBean;
    }

    @SneakyThrows
    @Override
    public void sendAssetPageRetrievalMessageSameTx(String messageBody) {
        producerBean.sendModuleMessageWithPropsSameTx(messageBody, Collections.singletonMap(JMS_MESSAGE_SOURCE_KEY, SOURCE_MANUAL));
    }

    @SneakyThrows
    @Override
    public void sendMessageForScheduledSubscriptionExecutionSameTx(String messageBody) {
        producerBean.sendModuleMessageWithPropsSameTx(messageBody, Collections.singletonMap(JMS_MESSAGE_SOURCE_KEY, SOURCE_SCHEDULED));
    }
}
