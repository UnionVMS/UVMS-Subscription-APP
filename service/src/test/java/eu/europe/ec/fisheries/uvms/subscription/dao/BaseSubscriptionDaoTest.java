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
import static com.ninja_squad.dbsetup.Operations.sql;
import static com.ninja_squad.dbsetup.operation.CompositeOperation.sequenceOf;

import com.ninja_squad.dbsetup.operation.Operation;
import eu.europa.ec.fisheries.uvms.BaseDAOTest;

public abstract class BaseSubscriptionDaoTest extends BaseDAOTest {

     static final Operation DELETE_ALL = sequenceOf(
            deleteAllFrom("subscription.subscription")
     );

    static final Operation INSERT_SUBSCRIPTION = sequenceOf(
            insertInto("subscription.subscription")
                    .columns("id", "subscription_guid", "name", "enabled", "organisation", "channel", "end_point", "message_type", "subscription_type", "state_type", "trigger_type")
                    .values(1L, "1dcc7037-bcf2-4e34-be01-c868beecf87a", "name1", "1", "organisation1", "channel2", "endpoint1", "UNKNOWN", "UNKNOWN", "UNKNOWN", "AUTO")
                    .values(2L, "0dbe00af-a300-4cea-b1d7-1e6826ff8826", "subscription2", "1", "org1", "channel2", "endpoint2", "UNKNOWN", "UNKNOWN", "UNKNOWN", "AUTO")
                    .values(3L, "4b25f95f-b3de-4d2e-ad99-dd2fe828a0f0", "subscription3", "1", "org1", "channel3", "endpoint3", "UNKNOWN", "UNKNOWN", "UNKNOWN", "AUTO")
                    .values(4L, "14f7dc3c-813e-45d5-8470-3e38192ea5f9", "subscription4", "0", "org1", "channel4", "endpoint4", "UNKNOWN", "UNKNOWN", "UNKNOWN", "AUTO")
                    .build(),
            sql("alter sequence subscription.hibernate_sequence restart with 100000")
    );

    static final Operation INSERT_CONDITION = sequenceOf(
            insertInto("subscription.condition")
                    .columns("id", "position", "subscription_id", "data_type", "criteria_type", "sub_criteria_type", "value_type", "value", "condition_type")
                    .values(1L, 1L, 1, "FISHING_ACTIVITY", "SENDER", "ORGANISATION", "UNKNOWN", "BEL", "START")
                    .build()

    );

    static final Operation INSERT_AREA = sequenceOf(
            insertInto("subscription.area")
                    .columns("id", "area_guid", "subscription_id", "area_type", "area_value_type", "value")
                    .values(1L, "0dbe00af-a300-4cea-b1d7-1e6826ff8826", 1L, "EEZ", "AREA_GUID", "182022980198")
                    .values(2L, "0d20128e-097b-4ca0-b7d0-71bedf17d215", 1L, "USERAREA", "AREA_NAME", "myArea")
                    .values(3L, "be11e3b0-e1cc-4b9c-903b-b86d8c173798", 1L, "USERAREA", "AREA_NAME", "myArea")
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
