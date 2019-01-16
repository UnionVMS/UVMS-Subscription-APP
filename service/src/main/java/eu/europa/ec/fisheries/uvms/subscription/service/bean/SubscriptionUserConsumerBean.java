package eu.europa.ec.fisheries.uvms.subscription.service.bean;

import eu.europa.ec.fisheries.uvms.commons.message.api.MessageConstants;
import eu.europa.ec.fisheries.uvms.commons.message.impl.AbstractConsumer;
import lombok.extern.slf4j.Slf4j;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;

@Stateless
@LocalBean
@Slf4j
public class SubscriptionUserConsumerBean extends AbstractConsumer {

    @Override
    public String getDestinationName(){
            return MessageConstants.QUEUE_SUBSCRIPTION;
    }
}
