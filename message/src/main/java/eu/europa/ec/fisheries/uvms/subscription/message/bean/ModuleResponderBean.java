/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */

package eu.europa.ec.fisheries.uvms.subscription.message.bean;

import javax.annotation.PostConstruct;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import eu.europa.ec.fisheries.uvms.commons.message.impl.JMSUtils;
import eu.europa.ec.fisheries.uvms.subscription.message.SubscriptionMessageProducer;
import lombok.extern.slf4j.Slf4j;

@Stateless
@LocalBean
@Slf4j
public class ModuleResponderBean implements SubscriptionMessageProducer {

    private ConnectionFactory connectionFactory;

    @PostConstruct
    public void init() {
        connectionFactory = JMSUtils.lookupConnectionFactory();
    }

    @Override
    public void sendModuleResponseMessage(TextMessage message, String text) {

        try ( Connection connection = connectionFactory.createConnection();
              Session session = JMSUtils.connectToQueue(connection);
              MessageProducer producer = session.createProducer(message.getJMSReplyTo())) {

            log.debug("Sending message back to recipient from  with correlationId {} on queue: {}",
                    message.getJMSMessageID(), message.getJMSReplyTo());

            final TextMessage response = session.createTextMessage();
            response.setText(text);
            producer.send(response);
        } //Connection, Session and Producer are auto closed here. No explicit calls to close.
        catch (Exception e) {
            log.error("[ Error when returning request. ] {} {}", e.getMessage(), e.getStackTrace());
        }
    }
}
