/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */

package eu.europa.ec.fisheries.uvms.subscription.message.bean;

import static eu.europa.ec.fisheries.uvms.commons.message.api.MessageConstants.QUEUE_ASSET_EVENT;
import static eu.europa.ec.fisheries.uvms.commons.message.api.MessageConstants.QUEUE_CONFIG;
import static eu.europa.ec.fisheries.uvms.commons.message.api.MessageConstants.QUEUE_ECB_PROXY;
import static eu.europa.ec.fisheries.uvms.commons.message.api.MessageConstants.QUEUE_MDR_EVENT;
import static eu.europa.ec.fisheries.uvms.commons.message.api.MessageConstants.QUEUE_MODULE_RULES;
import static eu.europa.ec.fisheries.uvms.commons.message.api.MessageConstants.QUEUE_RULES;
import static eu.europa.ec.fisheries.uvms.commons.message.api.MessageConstants.QUEUE_SALES;
import static eu.europa.ec.fisheries.uvms.commons.message.impl.JMSUtils.lookupConnectionFactory;
import static eu.europa.ec.fisheries.uvms.commons.message.impl.JMSUtils.lookupQueue;

import javax.annotation.PostConstruct;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.jms.ConnectionFactory;
import javax.jms.Queue;

import eu.europa.ec.fisheries.uvms.commons.message.impl.SimpleAbstractProducer;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Stateless
@LocalBean
@Slf4j
@Getter
public class SubscriptionProducerBean extends SimpleAbstractProducer {

    @Getter(AccessLevel.NONE)
    private ConnectionFactory connectionFactory;

    private Queue salesQueue;
    private Queue assetQueue;
    private Queue ecbProxyQueue;
    private Queue configQueue;
    private Queue rulesEventQueue;
    private Queue mdrQueue;
    private Queue rulesQueue;

    @PostConstruct
    public void init() {
        connectionFactory = lookupConnectionFactory();
        assetQueue = lookupQueue(QUEUE_ASSET_EVENT);
        salesQueue = lookupQueue(QUEUE_SALES);
        ecbProxyQueue = lookupQueue(QUEUE_ECB_PROXY);
        configQueue = lookupQueue(QUEUE_CONFIG);
        rulesEventQueue = lookupQueue(QUEUE_MODULE_RULES);
        rulesQueue = lookupQueue(QUEUE_RULES);
        mdrQueue = lookupQueue(QUEUE_MDR_EVENT);
    }

    @Override
    public ConnectionFactory getConnectionFactory() {
        return connectionFactory;
    }
}
