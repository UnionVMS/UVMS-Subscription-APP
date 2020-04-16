/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */

package eu.europa.ec.fisheries.uvms.subscription.service.dao;

import static com.ninja_squad.dbsetup.Operations.deleteAllFrom;
import static com.ninja_squad.dbsetup.Operations.insertInto;
import static com.ninja_squad.dbsetup.Operations.sql;
import static com.ninja_squad.dbsetup.operation.CompositeOperation.sequenceOf;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.ninja_squad.dbsetup.operation.Operation;
import eu.europa.fisheries.uvms.subscription.model.enums.OutgoingMessageType;
import eu.europa.fisheries.uvms.subscription.model.enums.TriggerType;

public abstract class BaseSubscriptionInMemoryTest extends BaseDAOTest {

    static final Operation DELETE_ALL = sequenceOf(deleteAllFrom("subscription.subscription"));

    static final Operation INSERT_SUBSCRIPTION = sequenceOf(insertInto("subscription.subscription")
            .columns("id", "name", "active", "description", "trigger_type", "start_date", "end_date", "alert", "message_type", "channel_id", "organisation_id", "vessel_ids")
            .values(1L, "subscription1", "1", "A lorem ipsum tade", TriggerType.SCHEDULER, d("20170101"), d("20181231"), "1", OutgoingMessageType.NONE, 11L, 10L, 0b0)
            .values(2L, "subscription2", "1", "B lorem ipsum tade", TriggerType.SCHEDULER, d("20180101"), d("20181231"), "1", OutgoingMessageType.FA_QUERY, 11L, 11L, 0b10)
            .values(3L, "subscription3", "1", "C lorem ipsum", TriggerType.MANUAL,         d("20190101"), d("20191231"), "1", OutgoingMessageType.FA_REPORT, 11L, 5L, 0b11)
            .values(4L, "subscription4", "0", "D lorem ipsum", TriggerType.SCHEDULER,      d("20200101"), d("20201231"), "0", OutgoingMessageType.POSITION, 11L, 11L, 0b01).build(),
            sql("alter sequence subscription.hibernate_sequence restart with 100000"));

    static final Operation INSERT_CONDITION = sequenceOf(insertInto("subscription.condition").columns("id", "position", "subscription_id", "message_type", "criteria_type", "sub_criteria_type", "value_type", "value", "condition_type")
            .values(1L, 1L, 1, "FLUX_FA_REPORT_MESSAGE", "SENDER", "ORGANISATION", "UNKNOWN", "BEL", "START").build()
    );

    static final Operation INSERT_AREA = sequenceOf(insertInto("subscription.area").columns("id", "area_guid", "subscription_id", "area_type", "area_value_type", "value").values(1L, "0dbe00af-a300-4cea-b1d7-1e6826ff8826", 1L, "EEZ", "AREA_GUID", "182022980198").values(2L, "0d20128e-097b-4ca0-b7d0-71bedf17d215", 1L, "USERAREA", "AREA_NAME", "myArea").values(3L, "be11e3b0-e1cc-4b9c-903b-b86d8c173798", 1L, "USERAREA", "AREA_NAME", "myArea").build());

    @Override protected String getSchema() {
        return "subscription";
    }

    @Override protected String getPersistenceUnitName() {
        return "testPU";
    }

    static Date d(String s) {
        try {
            return new SimpleDateFormat("yyyyMMdd").parse(s);
        } catch (ParseException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
