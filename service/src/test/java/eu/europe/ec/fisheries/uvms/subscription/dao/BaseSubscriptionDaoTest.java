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


import static com.ninja_squad.dbsetup.Operations.deleteAllFrom;
import static com.ninja_squad.dbsetup.Operations.insertInto;
import static com.ninja_squad.dbsetup.operation.CompositeOperation.sequenceOf;

import com.ninja_squad.dbsetup.operation.Operation;
import eu.europa.ec.fisheries.uvms.BaseDAOTest;

public abstract class BaseSubscriptionDaoTest extends BaseDAOTest {

    protected static final Operation DELETE_ALL = sequenceOf(
            deleteAllFrom("subscription.subscription"),
            deleteAllFrom("subscription.asset_identifier")
    );

    protected static final Operation INSERT_SUBSCRIPTION_REFERENCE_DATA = sequenceOf(
            insertInto("subscription.subscription")
                    .columns("ID", "NAME", "CHANNEL", "ORGANISATION", "MESSAGETYPE", "ACTIVE")
                    .values(1L, "subscription1", "channel1", "org1", "FLUX_FA_QUERY", "1")
                    .values(2L, "subscription2", "channel1", "org2", "FLUX_FA_QUERY", "1")
                    .values(3L, "subscription3", "channel1", "org3", "FLUX_FA_QUERY", "1")
                    .values(4L, "subscription4", "channel1", "org4", "FLUX_FA_QUERY", "0")
                    .build()
    );

    protected static final Operation INSERT_ASSET_IDENTIFIER_REFERENCE_DATA = sequenceOf(
            insertInto("subscription.asset_identifier")
                    .columns("ID", "SUBSCRIPTION_ID", "ASSET_TYPE", "ID_TYPE", "VALUE")
                    .values(1L, 1L, "VESSEL", "CFR", "cfr1")
                    .values(2L, 1L, "VESSEL", "CFR", "cfr2")

                    .build()
    );

    @Override
    protected String getSchema() {
        return "subscription";
    }

    @Override
    protected String getPersistenceUnitName() {
        return "testPU";
    }

}
