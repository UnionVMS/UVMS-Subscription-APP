/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */

package eu.europe.ec.fisheries.uvms.subscription.mapper;

import static eu.europa.ec.fisheries.uvms.commons.date.DateUtils.UI_FORMATTER;
import static org.junit.Assert.assertEquals;

import java.util.Date;

import eu.europa.ec.fisheries.uvms.subscription.service.domain.AccessibilityType;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.MessageType;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.StateType;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionType;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.TriggerType;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.SubscriptionDto;
import eu.europa.ec.fisheries.uvms.subscription.service.mapper.SubscriptionMapper;
import eu.europa.ec.fisheries.uvms.subscription.service.mapper.SubscriptionMapperImpl;
import org.junit.Before;
import org.junit.Test;

public class SubscriptionMapperTest {

    private SubscriptionMapper mapper = new SubscriptionMapperImpl();

    private SubscriptionDto dto;
    private Date startDate;
    private Date endDate;

    @Before
    public void before(){

        dto = new SubscriptionDto();

        startDate = UI_FORMATTER.parseDateTime("2016-08-01T11:50:16").toDate();
        endDate = UI_FORMATTER.parseDateTime("2017-08-01T11:50:16").toDate();

        dto.setStartDate(startDate);
        dto.setEndDate(endDate);

        dto.setName("name");
        dto.setChannel("channel");
        dto.setDescription("description");
        dto.setTriggerType(TriggerType.AUTO);
        dto.setOrganisation("organisation");
        dto.setActive(true);
        dto.setDelay("1,1,1");
        dto.setEndPoint("endPoint");
        dto.setSubscriptionType(SubscriptionType.TX_PULL);
        dto.setMessageType(MessageType.FLUXFAReportMessage);
        dto.setAccessibility(AccessibilityType.PRIVATE);


    }

    @Test
    public void testMapDtoToEntity(){

        SubscriptionEntity entity = mapper.mapDtoToEntity(dto);

        assertEquals(TriggerType.AUTO, entity.getTriggerType());
        assertEquals(true, entity.isEnabled());
        assertEquals("organisation", entity.getOrganisation());
        assertEquals("channel", entity.getChannel());
        assertEquals("name", entity.getName());
        assertEquals(SubscriptionType.TX_PULL, entity.getSubscriptionType());
        assertEquals(MessageType.FLUXFAReportMessage, entity.getMessageType());
        assertEquals(StateType.INACTIVE, entity.getStateType());
        assertEquals("description", entity.getDescription());
        assertEquals("1,1,1", entity.getDelay());
        assertEquals(startDate, entity.getValidityPeriod().getStartDate());
        assertEquals(endDate, entity.getValidityPeriod().getEndDate());
        assertEquals("endPoint", entity.getEndPoint());
        assertEquals(AccessibilityType.PRIVATE, entity.getAccessibility());

    }

    @Test
    public void testUpdateEntity(){

        SubscriptionEntity entity = SubscriptionEntity.random();

        mapper.updateEntity(dto, entity);

        assertEquals(dto.getMessageType(), entity.getMessageType());
        assertEquals(dto.getChannel(), entity.getChannel());
        assertEquals(dto.getDelay(), entity.getDelay());
        assertEquals(dto.getStartDate(), entity.getValidityPeriod().getStartDate());
        assertEquals(dto.getEndDate(), entity.getValidityPeriod().getEndDate());
        assertEquals(dto.getTriggerType(), entity.getTriggerType());
        assertEquals(dto.getName(), entity.getName());
        assertEquals(dto.getDescription(), entity.getDescription());
        assertEquals(dto.getEndPoint(), entity.getEndPoint());
        assertEquals(dto.getOrganisation(), entity.getOrganisation());
        assertEquals(dto.getActive(), entity.isEnabled());
        assertEquals(dto.getAccessibility(), entity.getAccessibility());


    }
}
