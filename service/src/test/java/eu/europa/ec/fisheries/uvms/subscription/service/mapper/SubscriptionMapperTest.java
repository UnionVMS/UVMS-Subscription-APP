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

import java.util.Date;

import eu.europa.ec.fisheries.uvms.subscription.helper.SubscriptionTestHelper;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.AccessibilityType;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionType;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.TriggerType;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.SubscriptionDto;
import eu.europa.ec.fisheries.wsdl.subscription.module.MessageType;
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
        dto.setChannel(new Long( 1 ));
        dto.setDescription("description");
        dto.setTriggerType(TriggerType.SCHEDULER);
        dto.setOrganisation(new Long( 1 ));
        dto.setActive(true);
        dto.setDelay("1,1,1");
        dto.setEndPoint(new Long(2));
        dto.setSubscriptionType(SubscriptionType.TX_PULL);
        dto.setMessageType(MessageType.FLUX_FA_REPORT_MESSAGE);
        dto.setAccessibility(AccessibilityType.PRIVATE);
    }

    @Test
    public void testMapDtoToEntity() {
        SubscriptionEntity entity = mapper.mapDtoToEntity(dto);

        assertEquals(TriggerType.SCHEDULER, entity.getExecution().getTriggerType());
        assertEquals(true, entity.isActive());
        assertEquals(new Long(1), entity.getOutput().getSubscriber().getOrganisationId());
        assertEquals(new Long(1), entity.getOutput().getSubscriber().getChannelId());
        assertEquals("name", entity.getName());
//        assertEquals(SubscriptionType.TX_PULL, entity.getSubscriptionType());
        assertEquals(MessageType.FLUX_FA_REPORT_MESSAGE, entity.getExecution().getTriggerType());
        assertEquals("description", entity.getDescription());
        assertEquals(startDate, entity.getValidityPeriod().getStartDate());
        assertEquals(endDate, entity.getValidityPeriod().getEndDate());
        assertEquals(new Long(2), entity.getOutput().getSubscriber().getEndpointId());
    }

    @Test
    public void testUpdateEntity(){

        SubscriptionEntity entity = SubscriptionTestHelper.random();

        mapper.updateEntity(dto, entity);

        assertEquals(dto.getMessageType(), entity.getExecution().getTriggerType());
        assertEquals(dto.getChannel(), entity.getOutput().getSubscriber().getChannelId());
        assertEquals(dto.getStartDate(), entity.getValidityPeriod().getStartDate());
        assertEquals(dto.getEndDate(), entity.getValidityPeriod().getEndDate());
        assertEquals(dto.getTriggerType(), entity.getExecution().getTriggerType());
        assertEquals(dto.getName(), entity.getName());
        assertEquals(dto.getDescription(), entity.getDescription());
        assertEquals(dto.getEndPoint(), entity.getOutput().getSubscriber().getEndpointId());
        assertEquals(dto.getOrganisation(), entity.getOutput().getSubscriber().getOrganisationId());
        assertEquals(dto.getActive(), entity.isActive());
    }
}
