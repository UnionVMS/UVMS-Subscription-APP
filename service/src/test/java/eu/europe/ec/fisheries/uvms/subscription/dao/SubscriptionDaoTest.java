/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */

/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */

package eu.europe.ec.fisheries.uvms.subscription.dao;

import static com.ninja_squad.dbsetup.Operations.sequenceOf;
import static eu.europa.ec.fisheries.uvms.subscription.service.type.TriggerType.MANUAL;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import com.ninja_squad.dbsetup.DbSetup;
import com.ninja_squad.dbsetup.destination.DataSourceDestination;
import com.ninja_squad.dbsetup.operation.Operation;
import eu.europa.ec.fisheries.uvms.subscription.service.dao.SubscriptionDao;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity;
import eu.europa.ec.fisheries.wsdl.subscription.module.AssetId;
import eu.europa.ec.fisheries.wsdl.subscription.module.AssetIdList;
import eu.europa.ec.fisheries.wsdl.subscription.module.AssetIdType;
import eu.europa.ec.fisheries.wsdl.subscription.module.AssetType;
import eu.europa.ec.fisheries.wsdl.subscription.module.SubscriptionQuery;
import lombok.SneakyThrows;
import org.apache.commons.collections.CollectionUtils;
import org.junit.Before;
import org.junit.Test;

public class SubscriptionDaoTest extends BaseSubscriptionDaoTest {

    private SubscriptionDao dao = new SubscriptionDao(em);

    @Before
    public void prepare(){
        Operation operation = sequenceOf(
                DELETE_ALL,
                INSERT_SUBSCRIPTION_REFERENCE_DATA,
                INSERT_ASSET_IDENTIFIER_REFERENCE_DATA
        );
        DbSetup dbSetup = new DbSetup(new DataSourceDestination(ds), operation);
        dbSetupTracker.launchIfNecessary(dbSetup);
    }

    @Test
    @SneakyThrows
    public void testCreate(){

        SubscriptionEntity subscriptionEntity = new SubscriptionEntity();

        SubscriptionEntity entity = dao.createEntity(subscriptionEntity);

        assertNotNull(entity.getId());
        assertNotNull(entity.getGuid());
        assertNotNull(entity.getValidityPeriod().getStartDate());
        assertNotNull(entity.getValidityPeriod().getEndDate());
        assertEquals(MANUAL, entity.getTrigger());

    }

    @Test
    @SneakyThrows
    public void testListSubscriptionsWithNoParametersShouldReturnAllSubscriptions(){

        List<SubscriptionEntity> subscriptionEntities = dao.listSubscriptions(null);
        assertTrue(CollectionUtils.isNotEmpty(subscriptionEntities));
    }

    @Test
    @SneakyThrows
    public void testListChannel1SubscriptionsShouldReturn3Subscriptions(){

        SubscriptionQuery query = new SubscriptionQuery();
        query.setChannel("channel1");
        query.setActive(true);

        AssetId assetId = new AssetId();
        assetId.setAssetType(AssetType.VESSEL);
        AssetIdList asset1 = new AssetIdList();
        asset1.setIdType(AssetIdType.CFR);
        asset1.setValue("cfr1");
        assetId.getAssetIdList().add(asset1);
        AssetIdList asset2 = new AssetIdList();
        asset2.setIdType(AssetIdType.CFR);
        asset2.setValue("cfr2");
        assetId.getAssetIdList().add(asset2);
        query.setAssetId(assetId);

        List<SubscriptionEntity> subscriptionEntities = dao.listSubscriptions(query);
        assertEquals(2, subscriptionEntities.size());
    }

    @Test
    @SneakyThrows
    public void testListSubscriptionsWithoutChannelParameter(){

        SubscriptionQuery query = new SubscriptionQuery();
        List<SubscriptionEntity> subscriptionEntities = dao.listSubscriptions(query);
        assertTrue(CollectionUtils.isEmpty(subscriptionEntities));
    }
}
