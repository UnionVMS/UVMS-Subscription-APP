/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */

package eu.europa.ec.fisheries.uvms.subscription.helper;

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;

import java.util.Random;

import eu.europa.ec.fisheries.uvms.subscription.service.domain.AreaEntity;
import eu.europa.fisheries.uvms.subscription.model.enums.OutgoingMessageType;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionOutput;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionSubscriber;
import eu.europa.ec.fisheries.wsdl.subscription.module.AreaType;
import eu.europa.ec.fisheries.wsdl.subscription.module.AreaValueType;
import org.apache.commons.lang.RandomStringUtils;


public class SubscriptionTestHelper {

    private SubscriptionTestHelper(){

    }

    public static SubscriptionEntity random() {
        Random rnd = new Random();
        SubscriptionEntity subscriptionEntity = new SubscriptionEntity();
        SubscriptionOutput output = new SubscriptionOutput();
        SubscriptionSubscriber subscriber = new SubscriptionSubscriber();
        subscriber.setChannelId(rnd.nextLong());
        subscriber.setEndpointId(rnd.nextLong());
        subscriber.setOrganisationId(rnd.nextLong());
        output.setSubscriber(subscriber);
        output.setMessageType(OutgoingMessageType.NONE);
        subscriptionEntity.setOutput(output);
        subscriptionEntity.setDescription(randomAlphabetic(200));
        subscriptionEntity.setName(randomAlphabetic(40));
        subscriptionEntity.setActive(rnd.nextBoolean());
        return subscriptionEntity;
    }

    public static AreaEntity randomArea(){
        AreaEntity areaEntity = new AreaEntity();
        areaEntity.setValue(RandomStringUtils.randomAlphabetic(100));
        areaEntity.setAreaValueType(AreaValueType.values()[new Random().nextInt(AreaValueType.values().length)]);
        areaEntity.setAreaType(AreaType.values()[new Random().nextInt(AreaType.values().length)]);
        return areaEntity;
    }
}
