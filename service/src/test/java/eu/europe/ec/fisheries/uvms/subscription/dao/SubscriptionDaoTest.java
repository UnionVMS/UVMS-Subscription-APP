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
import static eu.europa.ec.fisheries.uvms.subscription.service.domain.TriggerType.MANUAL;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.ninja_squad.dbsetup.DbSetup;
import com.ninja_squad.dbsetup.destination.DataSourceDestination;
import com.ninja_squad.dbsetup.operation.Operation;
import eu.europa.ec.fisheries.uvms.subscription.service.dao.SubscriptionDao;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;

public class SubscriptionDaoTest extends BaseSubscriptionDaoTest {

    private SubscriptionDao dao = new SubscriptionDao(em);

    @Before
    public void prepare(){
        Operation operation = sequenceOf(
                DELETE_ALL,
                INSERT_SUBSCRIPTION_DATA,
                INSERT_CONDITION_DATA
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

}
