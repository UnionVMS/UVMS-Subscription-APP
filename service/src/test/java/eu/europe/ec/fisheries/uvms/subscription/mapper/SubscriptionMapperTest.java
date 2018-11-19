/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */

package eu.europe.ec.fisheries.uvms.subscription.mapper;

import java.util.Date;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.*;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.SubscriptionDto;
import eu.europa.ec.fisheries.uvms.subscription.service.mapper.SubscriptionMapper;
import eu.europa.ec.fisheries.uvms.subscription.service.mapper.SubscriptionMapperImpl;
import eu.europa.ec.fisheries.wsdl.subscription.module.MessageType;
import org.junit.Before;
import org.junit.Test;
import static eu.europa.ec.fisheries.uvms.commons.date.DateUtils.UI_FORMATTER;
import static org.junit.Assert.assertEquals;

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
        dto.setChannel(new Long( 1 ));
        dto.setDescription("description");
        dto.setTriggerType(TriggerType.AUTO);
        dto.setOrganisation(new Long( 1 ));
        dto.setActive(true);
        dto.setDelay("1,1,1");
        dto.setEndPoint(new Long(2));
        dto.setSubscriptionType(SubscriptionType.TX_PULL);
        dto.setMessageType(MessageType.FA_REPORT_MESSAGE_PULL);
        dto.setAccessibility(AccessibilityType.PRIVATE);


    }

    @Test
    public void testMapDtoToEntity(){

        SubscriptionEntity entity = mapper.mapDtoToEntity(dto);

        assertEquals(TriggerType.AUTO, entity.getTriggerType());
        assertEquals(true, entity.isEnabled());
        assertEquals(new Long(1), entity.getOrganisation());
        assertEquals(new Long(1), entity.getChannel());
        assertEquals("name", entity.getName());
        assertEquals(SubscriptionType.TX_PULL, entity.getSubscriptionType());
        assertEquals(MessageType.FA_REPORT_MESSAGE_PULL, entity.getMessageType());
        assertEquals(StateType.INACTIVE, entity.getStateType());
        assertEquals("description", entity.getDescription());
        assertEquals("1,1,1", entity.getDelay());
        assertEquals(startDate, entity.getValidityPeriod().getStartDate());
        assertEquals(endDate, entity.getValidityPeriod().getEndDate());
        assertEquals(new Long(2), entity.getEndPoint());
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
