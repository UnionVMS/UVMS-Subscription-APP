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
import static eu.europa.ec.fisheries.uvms.subscription.service.dto.SubscriptionQueryDto.*;
import static junitparams.JUnitParamsRunner.$;
import static org.junit.Assert.assertEquals;

import java.util.List;

import com.ninja_squad.dbsetup.DbSetup;
import com.ninja_squad.dbsetup.destination.DataSourceDestination;
import com.ninja_squad.dbsetup.operation.Operation;
import eu.europa.ec.fisheries.uvms.subscription.service.dao.SubscriptionDao;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.dto.SubscriptionQueryDto;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JUnitParamsRunner.class)
public class SubscriptionDaoTest extends BaseSubscriptionDaoTest {

    private SubscriptionDao daoUnderTest = new SubscriptionDao(em);

    @Before
    public void prepare(){
        Operation operation = sequenceOf(
                DELETE_ALL, INSERT_SUBSCRIPTION, INSERT_CONDITION, INSERT_AREA
                );

        DbSetup dbSetup = new DbSetup(new DataSourceDestination(ds), operation);
        dbSetupTracker.launchIfNecessary(dbSetup);
    }

    @Test
    @Parameters(method = "subscriptionQueryParameters")
    public void testListSubscription(SubscriptionQueryDto query, int expected){
        List<SubscriptionEntity> subscriptionEntities = daoUnderTest.listSubscriptions(query);
        assertEquals(expected, subscriptionEntities.size());
    }

    @Test
    @SneakyThrows
    public void testCreate(){

    }

    protected Object[] subscriptionQueryParameters(){
        return $(
                $(builder().channel("channel1").build(), 0),
                $(builder().channel("channel2").build(), 2),
                $(builder().channel("channel3").build(), 1),
                $(builder().build(), 0),
                $(null, 0),
                $(builder().enabled(true).build(), 3),
                $(builder().organisation("org1").name("subscription4").channel("channel4").build(), 1)
        );
    }
}
