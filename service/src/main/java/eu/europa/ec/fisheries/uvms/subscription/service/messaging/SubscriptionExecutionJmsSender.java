/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subscription.service.messaging;

import static eu.europa.ec.fisheries.uvms.subscription.service.messaging.SubscriptionMessagingConstants.EXECUTION_QUEUE_RESOURCE_MAPPED_NAME;

import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionExecutionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.execution.SubscriptionExecutionSender;
import eu.europa.fisheries.uvms.subscription.model.exceptions.ExecutionException;

/**
 * Implementation of {@link SubscriptionExecutionSender} using JMS.
 */
@ApplicationScoped
class SubscriptionExecutionJmsSender implements SubscriptionExecutionSender {

	@Resource(mappedName = "java:/ConnectionFactory")
	private ConnectionFactory connectionFactory;

	@Resource(mappedName = EXECUTION_QUEUE_RESOURCE_MAPPED_NAME)
	private Queue subscriptionExecutionQueue;

	@Override
	public void enqueue(SubscriptionExecutionEntity entity) {
		try(
			Connection connection = connectionFactory.createConnection();
			Session session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
			MessageProducer producer = session.createProducer(subscriptionExecutionQueue);
		) {
			TextMessage textMessage = session.createTextMessage(entity.getId().toString());
			producer.send(textMessage);
		} catch (JMSException e) {
			throw new ExecutionException(e);
		}
	}
}
