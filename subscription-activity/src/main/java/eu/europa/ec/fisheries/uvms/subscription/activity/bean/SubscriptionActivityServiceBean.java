/*
 *
 *  Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2020.
 *
 *  This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 *  and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 *  the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 *  details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package eu.europa.ec.fisheries.uvms.subscription.activity.bean;

import eu.europa.ec.fisheries.uvms.subscription.activity.communication.ActivitySender;
import eu.europa.ec.fisheries.uvms.subscription.service.bean.SubscriptionActivityService;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.List;

@ApplicationScoped
class SubscriptionActivityServiceBean implements SubscriptionActivityService {

    private ActivitySender activitySender;

    @Inject
    public SubscriptionActivityServiceBean(ActivitySender activitySender){
        this.activitySender = activitySender;
    }

    /**
     * Constructor for frameworks.
     */
    @SuppressWarnings("unused")
    SubscriptionActivityServiceBean(){
        //NOOP
    }

    @Override
    public List<String> findMovementGuidsByReportIdsAndAssetGuid(List<String> reportIds, String assetGuid) {
        return activitySender.findMovementGuidsByReportIdsAndAssetGuid(reportIds,assetGuid);
    }
}
