/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2020.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */

package eu.europa.ec.fisheries.uvms.subscription.service.mapper;

import static eu.europa.ec.fisheries.uvms.commons.date.DateUtils.UI_FORMATTER;
import static eu.europa.ec.fisheries.wsdl.subscription.module.AreaType.PORT;
import static eu.europa.ec.fisheries.wsdl.subscription.module.AreaType.USERAREA;
import static eu.europa.fisheries.uvms.subscription.model.enums.SubscriptionFaReportDocumentType.DECLARATION;
import static eu.europa.fisheries.uvms.subscription.model.enums.SubscriptionFaReportDocumentType.NOTIFICATION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.stream.Collectors;

import eu.europa.ec.fisheries.uvms.subscription.helper.SubscriptionTestHelper;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.AreaEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionFishingActivity;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.AreaDto;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.AssetDto;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.SubscriptionDto;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.SubscriptionExecutionDto;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.SubscriptionFishingActivityDto;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.SubscriptionOutputDto;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.SubscriptionSubscriberDto;
import eu.europa.fisheries.uvms.subscription.model.enums.AssetType;
import eu.europa.fisheries.uvms.subscription.model.enums.OutgoingMessageType;
import eu.europa.fisheries.uvms.subscription.model.enums.SubscriptionTimeUnit;
import eu.europa.fisheries.uvms.subscription.model.enums.TriggerType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SubscriptionMapperTest {
    private static final int DEADLINE = 123;
    private static final long AREA_GID_1 = 1001L;
    private static final long AREA_GID_2 = 1002L;
    private static final String ASSET_GUID_1 = "00000000-0000-0000-0000-000000000001";
    private static final String ASSET_NAME_1 = "name1";
    private static final String ASSET_GUID_2 = "00000000-0000-0000-0000-000000000002";
    private static final String ASSET_NAME_2 = "name2";

    private SubscriptionMapper mapper = new SubscriptionMapperImpl();

    private SubscriptionDto dto;
    private Date startDate;
    private Date endDate;

    @BeforeEach
    void before() {
        dto = new SubscriptionDto();

        startDate = UI_FORMATTER.parseDateTime("2016-08-01T11:50:16").toDate();
        endDate = UI_FORMATTER.parseDateTime("2017-08-01T11:50:16").toDate();
        dto.setStartDate(startDate);
        dto.setEndDate(endDate);
        dto.setName("name");
        dto.setDescription("description");
        dto.setActive(true);

        SubscriptionSubscriberDto subscriber = new SubscriptionSubscriberDto();
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

        dto.setDeadline(DEADLINE);
        dto.setDeadlineUnit(SubscriptionTimeUnit.DAYS);
        dto.setStopWhenQuitArea(true);

        dto.setStartActivities(new HashSet<>());
        dto.getStartActivities().add(new SubscriptionFishingActivityDto(DECLARATION, "a"));
        dto.getStartActivities().add(new SubscriptionFishingActivityDto(NOTIFICATION, "b"));

        dto.setStopActivities(new HashSet<>());
        dto.getStopActivities().add(new SubscriptionFishingActivityDto(DECLARATION, "c"));
        dto.getStopActivities().add(new SubscriptionFishingActivityDto(NOTIFICATION, "d"));

        dto.setAreas(new HashSet<>());
        dto.getAreas().add(new AreaDto(null, AREA_GID_1, PORT));
        dto.getAreas().add(new AreaDto(33L, AREA_GID_2, USERAREA));

        dto.setAssets(new HashSet<>());
        dto.getAssets().add(new AssetDto(null, ASSET_GUID_1, ASSET_NAME_1, AssetType.ASSET));
        dto.getAssets().add(new AssetDto(44L, ASSET_GUID_2, ASSET_NAME_2, AssetType.VGROUP));
    }

    @Test
    void testMapDtoToEntity() {
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
        assertEquals(DEADLINE, entity.getDeadline());
        assertEquals(SubscriptionTimeUnit.DAYS, entity.getDeadlineUnit());
        assertTrue(entity.isStopWhenQuitArea());

        checkCollections(entity);
    }

    @Test
    void testNullCollectionsInDto() {
        dto.setAreas(null);
        dto.setAssets(null);
        dto.setStartActivities(null);
        dto.setStopActivities(null);
        SubscriptionEntity entity = mapper.mapDtoToEntity(dto);
        assertNotNull(entity.getAreas());
        assertNotNull(entity.getAssets());
        assertNotNull(entity.getAssetGroups());
        assertNotNull(entity.getStartActivities());
        assertNotNull(entity.getStopActivities());
    }

    @Test
    void testUpdateEntity() {
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
        assertEquals(DEADLINE, entity.getDeadline());
        assertEquals(SubscriptionTimeUnit.DAYS, entity.getDeadlineUnit());
        assertTrue(entity.isStopWhenQuitArea());

        checkCollections(entity);
    }

    void checkCollections(SubscriptionEntity entity) {
        assertEquals(new HashSet<>(Arrays.asList(new SubscriptionFishingActivity(DECLARATION, "a"), new SubscriptionFishingActivity(NOTIFICATION, "b"))), entity.getStartActivities());
        assertEquals(new HashSet<>(Arrays.asList(new SubscriptionFishingActivity(DECLARATION, "c"), new SubscriptionFishingActivity(NOTIFICATION, "d"))), entity.getStopActivities());

        assertEquals(Arrays.asList(AREA_GID_1, AREA_GID_2), entity.getAreas().stream().map(AreaEntity::getGid).sorted().collect(Collectors.toList()));
        assertEquals(Arrays.asList(PORT, USERAREA), entity.getAreas().stream().sorted(Comparator.comparing(AreaEntity::getGid)).map(AreaEntity::getAreaType).collect(Collectors.toList()));
        assertEquals(Arrays.asList(null, 33L), entity.getAreas().stream().sorted(Comparator.comparing(AreaEntity::getGid)).map(AreaEntity::getId).collect(Collectors.toList()));

        assertEquals(1, entity.getAssets().size());
        assertNull(entity.getAssets().iterator().next().getId());
        assertEquals(ASSET_GUID_1, entity.getAssets().iterator().next().getGuid());
        assertEquals(ASSET_NAME_1, entity.getAssets().iterator().next().getName());
        assertEquals(1, entity.getAssetGroups().size());
        assertEquals(44L, entity.getAssetGroups().iterator().next().getId());
        assertEquals(ASSET_GUID_2, entity.getAssetGroups().iterator().next().getGuid());
        assertEquals(ASSET_NAME_2, entity.getAssetGroups().iterator().next().getName());
    }
}
