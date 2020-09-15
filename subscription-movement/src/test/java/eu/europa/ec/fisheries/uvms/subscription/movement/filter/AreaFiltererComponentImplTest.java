/*
﻿Developed with the contribution of the European Commission - Directorate General for Maritime Affairs and Fisheries
© European Union, 2015-2016.

This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can
redistribute it and/or modify it under the terms of the GNU General Public License as published by the
Free Software Foundation, either version 3 of the License, or any later version. The IFDM Suite is distributed in
the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details. You should have received a
copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
*/
package eu.europa.ec.fisheries.uvms.subscription.movement.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import eu.europa.ec.fisheries.uvms.commons.domain.DateRange;
import eu.europa.ec.fisheries.uvms.subscription.movement.communication.MovementSender;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.AreaEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.AssetEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionOutput;
import eu.europa.ec.fisheries.uvms.subscription.service.util.DateTimeService;
import eu.europa.ec.fisheries.uvms.subscription.service.util.SubscriptionDateTimeService;
import lombok.SneakyThrows;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.xml.datatype.DatatypeFactory;
import java.sql.Date;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Tests for the {@link AreaFiltererComponentImpl}.
 */
@EnableAutoWeld
@ExtendWith(MockitoExtension.class)
public class AreaFiltererComponentImplTest {

    @Produces
    @Mock
    private MovementSender movementSender;
    
    @Produces
    @Mock
    private DateTimeService dateTimeService;
    
    @Produces
    @Mock
    private SubscriptionDateTimeService subscriptionDateTimeService ;

    @Inject
    private AreaFiltererComponentImpl sut;

    @Produces
    @SneakyThrows
    DatatypeFactory dataTypeFactory(){
        return DatatypeFactory.newInstance();
    }

    @Test
    void testEmptyConstructor() {
        AreaFiltererComponentImpl sut = new AreaFiltererComponentImpl();
        assertNotNull(sut);
    }

    @Test
    void testMakeAreaFilterWithNoAreas(){
        SubscriptionEntity subscriptionEntity = createSubscriptionEntity(1L,false);
        AssetEntity assetEntity = createAsset(1L,"TestAsset",UUID.randomUUID().toString());
        List<AssetEntity> assetEntities = new ArrayList<>();
        assetEntities.add(assetEntity);

        List<AssetEntity> filteredData = sut.filterAssetsBySubscriptionAreas(assetEntities,subscriptionEntity,null);

        assertNotNull(filteredData);
        assertEquals(1, filteredData.size());
    }

    @Test
    void testMakeAreaFilterWithSelectedAreas(){
        SubscriptionEntity subscriptionEntity = createSubscriptionEntity(1L,true);
        String guid1 = UUID.randomUUID().toString();
        String guid2 = UUID.randomUUID().toString();
        AssetEntity assetEntity1 = createAsset(1L,"TestAsset",guid1);
        AssetEntity assetEntity2 = createAsset(2L,"TestAsset",guid2);
        List<AssetEntity> assetEntities = new ArrayList<>();
        assetEntities.add(assetEntity1);
        assetEntities.add(assetEntity2);

        List<AssetEntity> filteredData = sut.filterAssetsBySubscriptionAreas(assetEntities,subscriptionEntity,null);

        assertNotNull(filteredData);
        assertEquals(assetEntity1.getId(), filteredData.get(0).getId());
    }

    private SubscriptionEntity createSubscriptionEntity(Long id,boolean sendRequiredData) {
        SubscriptionEntity subscriptionEntity = new SubscriptionEntity();
        subscriptionEntity.setId(id);
        subscriptionEntity.setActive(true);
        if(sendRequiredData){
            Set<AreaEntity> areaEntitySet = new HashSet<>(Arrays.asList(mock(AreaEntity.class), mock(AreaEntity.class)));
            subscriptionEntity.setAreas(areaEntitySet);
            LocalDate todayLD = LocalDate.now();
            Instant lastMonth = todayLD.minusMonths(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
            Instant today = todayLD.atStartOfDay(ZoneId.systemDefault()).toInstant();
            DateRange queryPeriod = new DateRange();
            queryPeriod.setStartDate(Date.from(lastMonth));
            queryPeriod.setEndDate(Date.from(today));
            SubscriptionOutput output = new SubscriptionOutput();
            output.setQueryPeriod(queryPeriod);
            subscriptionEntity.setOutput(output);
        }
        subscriptionEntity.setHasAreas(subscriptionEntity.getAreas() != null && !subscriptionEntity.getAreas().isEmpty());
        return subscriptionEntity;
    }

    private AssetEntity createAsset(long id, String name,String guid) {
        AssetEntity assetEntity = new AssetEntity();
        assetEntity.setId(id);
        assetEntity.setGuid(guid);
        assetEntity.setName(name);
        return assetEntity;
    }
}
