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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Random;

import eu.europa.ec.fisheries.uvms.subscription.service.domain.AreaEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.search.OrderByData;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.search.PaginationData;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.search.SubscriptionListQuery;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.search.SubscriptionSearchCriteria;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.SubscriptionDto;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.SubscriptionExecutionDto;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.SubscriptionOutputDto;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.SubscriptionSubscriberDTO;
import eu.europa.fisheries.uvms.subscription.model.enums.ColumnType;
import eu.europa.fisheries.uvms.subscription.model.enums.DirectionType;
import eu.europa.fisheries.uvms.subscription.model.enums.OutgoingMessageType;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionOutput;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionSubscriber;
import eu.europa.ec.fisheries.wsdl.subscription.module.AreaType;
import eu.europa.ec.fisheries.wsdl.subscription.module.AreaValueType;
import eu.europa.fisheries.uvms.subscription.model.enums.TriggerType;
import org.apache.commons.lang.RandomStringUtils;


public class SubscriptionTestHelper {

    private SubscriptionTestHelper() {

    }

    public static SubscriptionListQuery createQuery(String name, Boolean active, Long organisation, Long endpoint, Long channel,
                                                    String description, ZonedDateTime startDate, ZonedDateTime endDate, OutgoingMessageType messageType, DirectionType direction, ColumnType field) {
        SubscriptionListQuery query = mock(SubscriptionListQuery.class);
        SubscriptionSearchCriteria searchCriteria = mock(SubscriptionSearchCriteria.class);
        PaginationData pagination = mock(PaginationData.class);
        @SuppressWarnings("unchecked")
        OrderByData<ColumnType> order = mock(OrderByData.class);

        when(searchCriteria.getName()).thenReturn(name);
        when(searchCriteria.getActive()).thenReturn(active);
        when(searchCriteria.getOrganisation()).thenReturn(organisation);
        when(searchCriteria.getEndPoint()).thenReturn(endpoint);
        when(searchCriteria.getChannel()).thenReturn(channel);
        when(searchCriteria.getDescription()).thenReturn(description);
        when(searchCriteria.getStartDate()).thenReturn(startDate);
        when(searchCriteria.getEndDate()).thenReturn(endDate);
        when(searchCriteria.getMessageType()).thenReturn(messageType);
        when(query.getCriteria()).thenReturn(searchCriteria);

        when(pagination.getPageSize()).thenReturn(25);
        when(pagination.getOffset()).thenReturn(0);
        when(query.getPagination()).thenReturn(pagination);

        when(order.getDirection()).thenReturn(direction);
        when(order.getField()).thenReturn(field);
        when(query.getOrderBy()).thenReturn(order);

        return query;
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

    public static AreaEntity randomArea() {
        AreaEntity areaEntity = new AreaEntity();
        areaEntity.setValue(RandomStringUtils.randomAlphabetic(100));
        areaEntity.setAreaValueType(AreaValueType.values()[new Random().nextInt(AreaValueType.values().length)]);
        areaEntity.setAreaType(AreaType.values()[new Random().nextInt(AreaType.values().length)]);
        return areaEntity;
    }

    public static SubscriptionDto createSubsriptionDto(Long id, String name, Boolean active, OutgoingMessageType messageType,
                                                       Long organisationId, Long endpointId, Long channelId, Boolean consolidated, Integer history,
                                                       Boolean logbook, TriggerType triggerType, Integer frequency, String timeExpresssion,
                                                       Date startDate, Date endDate) {
        SubscriptionDto dto = new SubscriptionDto();
        dto.setId(id);
        dto.setName(name);
        dto.setActive(active);

        SubscriptionOutputDto output = new SubscriptionOutputDto();
        output.setMessageType(messageType);

        SubscriptionSubscriberDTO subscriber = new SubscriptionSubscriberDTO();
        subscriber.setOrganisationId(organisationId);
        subscriber.setEndpointId(endpointId);
        subscriber.setChannelId(channelId);

        output.setSubscriber(subscriber);
        output.setConsolidated(consolidated);
        output.setHistory(history);
        output.setLogbook(logbook);
        dto.setOutput(output);

        SubscriptionExecutionDto execution = new SubscriptionExecutionDto();
        execution.setTriggerType(triggerType);
        execution.setFrequency(frequency);
        execution.setTimeExpression(timeExpresssion);
        dto.setExecution(execution);

        dto.setStartDate(startDate);
        dto.setEndDate(endDate);

        return dto;
    }
}
