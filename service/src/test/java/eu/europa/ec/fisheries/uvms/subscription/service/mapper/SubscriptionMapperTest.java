/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */

package eu.europa.ec.fisheries.uvms.subscription.service.mapper;

import static eu.europa.ec.fisheries.uvms.commons.date.DateUtils.UI_FORMATTER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;

import eu.europa.ec.fisheries.uvms.subscription.helper.SubscriptionTestHelper;
import eu.europa.fisheries.uvms.subscription.model.enums.OutgoingMessageType;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity;
import eu.europa.fisheries.uvms.subscription.model.enums.TriggerType;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.SubscriptionDto;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.SubscriptionExecutionDto;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.SubscriptionOutputDto;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.SubscriptionSubscriberDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SubscriptionMapperTest {

    private SubscriptionMapper mapper = new SubscriptionMapperImpl();

    private SubscriptionDto dto;
    private Date startDate;
    private Date endDate;

    @BeforeEach
    public void before() {
        dto = new SubscriptionDto();

        startDate = UI_FORMATTER.parseDateTime("2016-08-01T11:50:16").toDate();
        endDate = UI_FORMATTER.parseDateTime("2017-08-01T11:50:16").toDate();
        dto.setStartDate(startDate);
        dto.setEndDate(endDate);
        dto.setName("name");
        dto.setDescription("description");
        dto.setActive(true);

        SubscriptionSubscriberDTO subscriber = new SubscriptionSubscriberDTO();
        subscriber.setEndpointId(2L);
        subscriber.setOrganisationId(1L);
        subscriber.setChannelId(1L);

        SubscriptionOutputDto output = new SubscriptionOutputDto();
        output.setMessageType(OutgoingMessageType.FA_REPORT);
        output.setSubscriber(subscriber);

        SubscriptionExecutionDto execution = new SubscriptionExecutionDto();
        execution.setTriggerType(TriggerType.SCHEDULER);

        dto.setOutput(output);
        dto.setExecution(execution);
    }

    @Test
    public void testMapDtoToEntity() {
        SubscriptionEntity entity = mapper.mapDtoToEntity(dto);

        assertEquals(startDate, entity.getValidityPeriod().getStartDate());
        assertEquals(endDate, entity.getValidityPeriod().getEndDate());
        assertEquals("name", entity.getName());
        assertEquals("description", entity.getDescription());
        assertTrue(entity.isActive());
        assertEquals(2L, entity.getOutput().getSubscriber().getEndpointId());
        assertEquals(1L, entity.getOutput().getSubscriber().getOrganisationId());
        assertEquals(1L, entity.getOutput().getSubscriber().getChannelId());
        assertEquals(OutgoingMessageType.FA_REPORT, entity.getOutput().getMessageType());
        assertEquals(TriggerType.SCHEDULER, entity.getExecution().getTriggerType());
    }

    @Test
    public void testUpdateEntity(){

        SubscriptionEntity entity = SubscriptionTestHelper.random();

        mapper.updateEntity(dto, entity);

        assertEquals(dto.getOutput().getMessageType(), entity.getOutput().getMessageType());
        assertEquals(dto.getOutput().getSubscriber().getChannelId(), entity.getOutput().getSubscriber().getChannelId());
        assertEquals(dto.getStartDate(), entity.getValidityPeriod().getStartDate());
        assertEquals(dto.getEndDate(), entity.getValidityPeriod().getEndDate());
        assertEquals(dto.getExecution().getTriggerType(), entity.getExecution().getTriggerType());
        assertEquals(dto.getName(), entity.getName());
        assertEquals(dto.getDescription(), entity.getDescription());
        assertEquals(dto.getOutput().getSubscriber().getEndpointId(), entity.getOutput().getSubscriber().getEndpointId());
        assertEquals(dto.getOutput().getSubscriber().getOrganisationId(), entity.getOutput().getSubscriber().getOrganisationId());
        assertEquals(dto.getActive(), entity.isActive());
    }
}
