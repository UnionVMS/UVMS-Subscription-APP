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

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Date;
import java.util.Random;

import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionOutput;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionSubscriber;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.search.OrderByData;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.search.PaginationData;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.search.SubscriptionListQuery;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.search.SubscriptionSearchCriteria;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.SubscriptionDto;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.SubscriptionEmailConfigurationDto;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.SubscriptionExecutionDto;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.SubscriptionOutputDto;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.SubscriptionSubscriberDto;
import eu.europa.fisheries.uvms.subscription.model.enums.ColumnType;
import eu.europa.fisheries.uvms.subscription.model.enums.DirectionType;
import eu.europa.fisheries.uvms.subscription.model.enums.OutgoingMessageType;
import eu.europa.fisheries.uvms.subscription.model.enums.SubscriptionTimeUnit;
import eu.europa.fisheries.uvms.subscription.model.enums.TriggerType;


public class SubscriptionTestHelper {

    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyyMMdd");

    private SubscriptionTestHelper() {
        // NOOP
    }

    public static SubscriptionListQuery createDateRangeQuery(String startDate, String endDate) {
        return createQuery(null, null, null, null, null, null, startDate == null ? null : LocalDate.parse(startDate, DTF).atStartOfDay(ZoneId.of("UTC")), endDate == null ? null : LocalDate.parse(endDate, DTF).atStartOfDay(ZoneId.of("UTC")), null, null, null, null, null);
    }

    public static ZonedDateTime zdt(String date) {
        return date == null ? null : LocalDate.parse(date, DTF).atStartOfDay(ZoneId.of("UTC"));
    }

    public static SubscriptionListQuery createQuery(String name, Boolean active, Long organisation, Long endpoint, Long channel,
                                                    String description, ZonedDateTime startDate, ZonedDateTime endDate, ZonedDateTime validAt,
                                                    OutgoingMessageType messageType, DirectionType direction, ColumnType field, Collection<TriggerType> triggerTypes) {
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
        when(searchCriteria.getValidAt()).thenReturn(validAt);
        when(searchCriteria.getMessageType()).thenReturn(messageType);
        when(searchCriteria.getWithAnyTriggerType()).thenReturn(triggerTypes);
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

    public static SubscriptionDto createSubscriptionDtoWithEmailConfig(Long id, String name, Boolean active, OutgoingMessageType messageType, Boolean hasEmail,
                                                        Long organisationId, Long endpointId, Long channelId, Boolean consolidated, Integer history, SubscriptionTimeUnit historyUnit,
                                                        Boolean logbook, TriggerType triggerType, Integer frequency, SubscriptionTimeUnit frequencyUnit, String timeExpression,
                                                        Date startDate, Date endDate, String body, Boolean isPdf, Boolean hasAttachments, String password, Boolean passwordIsPlaceholder, Boolean isXml ) {
        SubscriptionDto dto = createSubscriptionDto(id, name,active, messageType, hasEmail, organisationId, endpointId, channelId, consolidated, history, historyUnit, logbook, triggerType, frequency, frequencyUnit, timeExpression,startDate, endDate);
        if(hasEmail != null && hasEmail) {
            SubscriptionEmailConfigurationDto emailConfiguration = new SubscriptionEmailConfigurationDto();
            emailConfiguration.setBody(body);
            emailConfiguration.setIsPdf(isPdf);
            emailConfiguration.setHasAttachments(hasAttachments);
            emailConfiguration.setPassword(password);
            emailConfiguration.setIsXml(isXml);
            emailConfiguration.setPasswordIsPlaceholder(passwordIsPlaceholder);
            dto.getOutput().setEmailConfiguration(emailConfiguration);
        } else {
            dto.getOutput().setEmailConfiguration(null);
        }
        return dto;
    }

    public static SubscriptionDto createManualSubscriptionDtoWithEmailConfig(Long id, String name, Boolean active, OutgoingMessageType messageType, Boolean hasEmail,
                                                                       Long organisationId, Long endpointId, Long channelId, Boolean consolidated,
                                                                       Integer history, SubscriptionTimeUnit historyUnit, Date queryStartDate, Date queryEndDate,
                                                                       Boolean logbook, Integer frequency, SubscriptionTimeUnit frequencyUnit, String timeExpression, Date startDate, Date endDate,
                                                                       String body, Boolean isPdf, Boolean hasAttachments, String password, Boolean passwordIsPlaceholder, Boolean isXml ) {

        SubscriptionDto dto = createManualSubscriptionDto(id, name,active, messageType, hasEmail, organisationId, endpointId, channelId, consolidated, history, historyUnit, queryStartDate, queryEndDate, logbook, frequency, frequencyUnit, timeExpression,startDate, endDate);

        if(hasEmail != null && hasEmail) {
            SubscriptionEmailConfigurationDto emailConfiguration = new SubscriptionEmailConfigurationDto();
            emailConfiguration.setBody(body);
            emailConfiguration.setIsPdf(isPdf);
            emailConfiguration.setHasAttachments(hasAttachments);
            emailConfiguration.setPassword(password);
            emailConfiguration.setIsXml(isXml);
            emailConfiguration.setPasswordIsPlaceholder(passwordIsPlaceholder);
            dto.getOutput().setEmailConfiguration(emailConfiguration);
        } else {
            dto.getOutput().setEmailConfiguration(null);
        }
        return dto;
    }

    public static SubscriptionDto createManualSubscriptionDto(Long id, String name, Boolean active, OutgoingMessageType messageType, Boolean hasEmail,
                                                                             Long organisationId, Long endpointId, Long channelId, Boolean consolidated,
                                                                             Integer history, SubscriptionTimeUnit historyUnit,Date queryStartDate, Date queryEndDate,
                                                                             Boolean logbook, Integer frequency, SubscriptionTimeUnit frequencyUnit, String timeExpression, Date startDate, Date endDate) {

        SubscriptionDto dto = createSubscriptionDto(id, name,active, messageType, hasEmail, organisationId, endpointId, channelId, consolidated, history, historyUnit, logbook, TriggerType.MANUAL, frequency, frequencyUnit, timeExpression,startDate, endDate);
        dto.getOutput().setQueryStartDate(queryStartDate);
        dto.getOutput().setQueryEndDate(queryEndDate);
        return dto;
    }

    public static SubscriptionDto createSubscriptionDto(Long id, String name, Boolean active, OutgoingMessageType messageType, Boolean hasEmail,
                                                       Long organisationId, Long endpointId, Long channelId, Boolean consolidated, Integer history, SubscriptionTimeUnit historyUnit,
                                                       Boolean logbook, TriggerType triggerType, Integer frequency, SubscriptionTimeUnit frequencyUnit, String timeExpression,
                                                       Date startDate, Date endDate) {
        SubscriptionDto dto = new SubscriptionDto();
        dto.setId(id);
        dto.setName(name);
        dto.setActive(active);

        SubscriptionOutputDto output = new SubscriptionOutputDto();
        output.setMessageType(messageType);
        output.setHasEmail(hasEmail);

        if(messageType!=OutgoingMessageType.NONE){
            SubscriptionSubscriberDto subscriber = new SubscriptionSubscriberDto();
            subscriber.setOrganisationId(organisationId);
            subscriber.setEndpointId(endpointId);
            subscriber.setChannelId(channelId);
            output.setSubscriber(subscriber);
        }

        output.setConsolidated(consolidated);
        output.setHistory(history);
        output.setHistoryUnit(historyUnit);
        output.setLogbook(logbook);
        dto.setOutput(output);

        SubscriptionExecutionDto execution = new SubscriptionExecutionDto();
        execution.setTriggerType(triggerType);
        execution.setFrequency(frequency);
        execution.setFrequencyUnit(frequencyUnit);
        execution.setTimeExpression(timeExpression);
        dto.setExecution(execution);

        dto.setStartDate(startDate);
        dto.setEndDate(endDate);

        return dto;
    }
}
