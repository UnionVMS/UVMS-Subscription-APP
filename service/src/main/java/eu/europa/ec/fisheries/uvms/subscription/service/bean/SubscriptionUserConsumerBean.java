package eu.europa.ec.fisheries.uvms.subscription.service.bean;

import eu.europa.ec.fisheries.uvms.commons.message.api.MessageConstants;
import eu.europa.ec.fisheries.uvms.commons.message.impl.AbstractConsumer;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import javax.ejb.*;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;

import static eu.europa.ec.fisheries.uvms.commons.message.api.MessageConstants.*;
import static eu.europa.ec.fisheries.uvms.commons.message.impl.JMSUtils.lookupConnectionFactory;
import static eu.europa.ec.fisheries.uvms.commons.message.impl.JMSUtils.lookupQueue;

@Stateless
@LocalBean
@Slf4j
@Getter
public class SubscriptionUserConsumerBean extends AbstractConsumer {

    @Override
    public String getDestinationName(){
            return MessageConstants.QUEUE_SUBSCRIPTION;
    }
}
