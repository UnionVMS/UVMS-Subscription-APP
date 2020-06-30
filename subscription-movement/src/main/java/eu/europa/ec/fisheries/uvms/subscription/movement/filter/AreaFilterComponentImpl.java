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
package eu.europa.ec.fisheries.uvms.subscription.movement.filter;

import eu.europa.ec.fisheries.uvms.subscription.movement.communication.MovementSender;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.filter.AreaFilterComponent;
import eu.europa.ec.fisheries.uvms.subscription.service.trigger.AssetAndSubscriptionData;
import eu.europa.ec.fisheries.uvms.subscription.service.util.SubscriptionDateTimeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ApplicationScoped
public class AreaFilterComponentImpl implements AreaFilterComponent {

    private static final Logger LOG = LoggerFactory.getLogger(AreaFilterComponentImpl.class);

    private MovementSender movementSender;
    private SubscriptionDateTimeService subscriptionDateTimeService;

    @Inject
    public AreaFilterComponentImpl(MovementSender movementSender, SubscriptionDateTimeService subscriptionDateTimeService){
        this.movementSender = movementSender;
        this.subscriptionDateTimeService = subscriptionDateTimeService;
    }

    /**
     * Constructor for frameworks.
     */
    @SuppressWarnings("unused")
    AreaFilterComponentImpl() {
        // NOOP
    }

    @Override
    public Stream<AssetAndSubscriptionData> filterAssetsBySubscriptionAreas(List<AssetAndSubscriptionData> assetAndSubscriptionDataList) {
        // subscriptionEntity is the same reference for all items in list
        AssetAndSubscriptionData firstData = assetAndSubscriptionDataList.get(0);
        SubscriptionEntity subscriptionEntity = firstData.getSubscription();

        if (!Boolean.TRUE.equals(subscriptionEntity.getHasAreas())) {
            LOG.debug("Not filtering data, areas not selected.");
            return assetAndSubscriptionDataList.stream();
        }

        ZonedDateTime endTime = subscriptionDateTimeService.calculateEndDate(subscriptionEntity.getOutput(),firstData.getOccurrenceKeyData());
        ZonedDateTime startTime = subscriptionDateTimeService.calculateStartDate(subscriptionEntity.getOutput(),endTime);

        Map<String, AssetAndSubscriptionData> assetMapByGuid = assetAndSubscriptionDataList.stream()
                .collect(Collectors.toMap(a -> a.getAssetEntity().getGuid(), Function.identity()));

        List<String> filteredGuidList = movementSender.sendFilterGuidListForAreasRequest(
                new ArrayList<>(assetMapByGuid.keySet()),
                Date.from(startTime.toInstant()),
                Date.from(endTime.toInstant()),
                subscriptionEntity.getAreas()
        );

        return filteredGuidList.stream().map(assetMapByGuid::get);
    }
}
