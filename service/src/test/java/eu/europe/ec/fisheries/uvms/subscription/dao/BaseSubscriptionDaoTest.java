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

     static final Operation DELETE_ALL = sequenceOf(
            deleteAllFrom("subscription.subscription")
     );

    static final Operation INSERT_SUBSCRIPTION_DATA = sequenceOf(
            insertInto("subscription.subscription")
                    .columns("ID", "NAME", "ENABLED")
                    .values(1L, "subscription1", "1")
                    .values(2L, "subscription2", "1")
                    .values(3L, "subscription3", "1")
                    .values(4L, "subscription4", "0")
                    .build()
    );

    static final Operation INSERT_CONDITION_DATA = sequenceOf(
            insertInto("subscription.condition")
                    .columns("ID", "SUBSCRIPTION_ID", "conditionType", "dataType", "criteriaType", "subCriteriaType", "valueType", "value")
                    .values(1L, 1L, "START", "FISHING_ACTIVITY", "SENDER", "ORGANISATION", "NONE", "BEL")
                    .values(2L, 1L, "START", "FISHING_ACTIVITY", "FAReportDocument", "PurposeCode", "FLUX_GP_PURPOSE", "9")
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
