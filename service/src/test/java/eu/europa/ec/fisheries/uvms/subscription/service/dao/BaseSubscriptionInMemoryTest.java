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
            .columns("id", "name", "active", "description", "trigger_type", "start_date", "end_date", "alert", "message_type", "channel_id", "organisation_id", "endpoint_id", "vessel_ids", "password", "deadline", "deadline_unit", "stop_when_quit_area", "has_assets", "has_areas", "next_scheduled_execution")
            .values(1L, "subscription1", "1", "A lorem ipsum tade", TriggerType.SCHEDULER,    d("20170101"), d("20181231"), "1", OutgoingMessageType.NONE, 11L, 10L, 2L, 0b0, null, 11, "DAYS", false, true, true, d("20170101"))
            .values(2L, "subscription2", "1", "B lorem ipsum tade", TriggerType.INC_POSITION, d("20180101"), d("20181231"), "1", OutgoingMessageType.FA_QUERY, 11L, 11L, 2L, 0b10, null, 2, "WEEKS", false, null, true, null)
            .values(3L, "subscription3", "1", "C lorem ipsum", TriggerType.MANUAL,            d("20190101"), d("20191231"), "1", OutgoingMessageType.FA_REPORT, 11L, 5L, 2L, 0b11, "test", 3, "MONTHS", true, true, false, null)
            .values(4L, "subscription4", "0", "D lorem ipsum", TriggerType.SCHEDULER,         d("20200101"), d("20201231"), "0", OutgoingMessageType.POSITION, 11L, 11L, 2L, 0b01, "abcd1234", 44, "DAYS", true, null, null, d("20200101")).build(),
            sql("alter sequence subscription.hibernate_sequence restart with 100000"));

    static final Operation INSERT_CONDITION = sequenceOf(insertInto("subscription.condition").columns("id", "position", "subscription_id", "message_type", "criteria_type", "sub_criteria_type", "value_type", "value", "condition_type")
            .values(1L, 1L, 1, "FLUX_FA_REPORT_MESSAGE", "SENDER", "ORGANISATION", "UNKNOWN", "BEL", "START").build()
    );

    static final Operation INSERT_AREA = sequenceOf(
            insertInto("subscription.area").columns("id", "gid", "subscription_id", "area_type")
                    .values(100L, 101L, 1L, "EEZ")
                    .values(200L, 102L, 1L, "USERAREA")
                    .values(300L, 103L, 2L, "USERAREA")
            .build());

    static final Operation INSERT_ASSET = sequenceOf(
            insertInto("subscription.asset").columns("id", "guid", "name", "cfr", "ircs", "iccat", "ext_mark", "uvi", "subscription_id")
                    .values(100L, "asset_guid_1", "asset1", "cfr1", "ircs1", "iccat1", "ext_mark1", "uvi1", 1L)
                    .values(200L, "asset_guid_2", "asset2", "cfr2", "ircs2", "iccat2", "ext_mark2", "uvi2", 1L)
                    .values(300L, "asset_guid_3", "asset3", "cfr3", "ircs3", "iccat3", "ext_mark3", "uvi3", 3L)
            .build());

    static final Operation INSERT_ASSET_GROUP = sequenceOf(
            insertInto("subscription.asset_group").columns("id", "guid", "name", "subscription_id")
                    .values(100L, "asset_group_guid_1", "asset_group_1", 2L)
            .build());

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
