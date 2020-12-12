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
package eu.europa.ec.fisheries.uvms.subscription.service.config;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum ParameterKey {

    SUBSCRIPTION_EMAIL_DEFAULT_SUBJECT("subscription.email.default.subject","Subscription default email subject"),
    SUBSCRIPTION_EMAIL_DEFAULT_SENDER("subscription.email.default.sender","Subscription default email sender"),
    SUBSCRIPTION_EMAIL_DEFAULT_BODY("subscription.email.default.body","Subscription default email body"),
    AUTHZ_FA_QUERY("subscription.authz.fa.query","Whether to run the authorisation logic for incoming FA Queries"),
    AUTHZ_FA_REPORT("subscription.authz.fa.report","Whether to run the authorisation logic for incoming FA Reports"),
    AUTHZ_VMS_REPORT("subscription.authz.vms.report","Whether to run the authorisation logic for incoming VMS (position) Reports");

    private final String key;
    private final String description;

    ParameterKey(String key,String description) {
        this.key = key;
        this.description = description;
    }

    public String getKey() {
        return key;
    }
    public String getDescription() {
        return description;
    }

    public static List<String> keys(){
        return Arrays.stream(values()).map(ParameterKey::getKey).collect(Collectors.toList());
    }
}
