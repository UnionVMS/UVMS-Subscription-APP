/*
Developed with the contribution of the European Commission - Directorate General for Maritime Affairs and Fisheries
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
import eu.europa.ec.fisheries.uvms.subscription.service.domain.AssetEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionOutput;
import eu.europa.ec.fisheries.uvms.subscription.service.filter.AreaFiltererComponent;
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

@ApplicationScoped
public class AreaFiltererComponentImpl implements AreaFiltererComponent {

    private static final Logger LOG = LoggerFactory.getLogger(AreaFiltererComponentImpl.class);

    private MovementSender movementSender;
    private SubscriptionDateTimeService subscriptionDateTimeService;
     
    @Inject
    public AreaFiltererComponentImpl(MovementSender movementSender,
                                     SubscriptionDateTimeService subscriptionDateTimeService){
        this.movementSender = movementSender;
        this.subscriptionDateTimeService = subscriptionDateTimeService;
    }

    /**
     * Constructor for frameworks.
     */
    @SuppressWarnings("unused")
    AreaFiltererComponentImpl() {
        // NOOP
    }

    @Override
    public List<AssetEntity> filterAssetsBySubscriptionAreas(List<AssetEntity> assets,SubscriptionEntity subscriptionEntity,String areasGeometry) {
        if (!Boolean.TRUE.equals(subscriptionEntity.getHasAreas())) {
            LOG.debug("Not filtering data, areas not selected.");
            return assets;
        }
        return filterAssetsInAreas(assets,subscriptionEntity,areasGeometry);
    }
    
    private List<AssetEntity> filterAssetsInAreas(List<AssetEntity> assetEntities,SubscriptionEntity subscriptionEntity ,String areasGeometry){
        if(assetEntities.isEmpty() || areasGeometry == null){
            return assetEntities;
        }
        
        Map<String, AssetEntity> assetMapByGuid = assetEntities.stream()
                .collect(Collectors.toMap(AssetEntity::getGuid, Function.identity()));

        List<String> filteredGuidList = getConnectIdsByDateRangeAndGeometry(new ArrayList<>(assetMapByGuid.keySet()),
                subscriptionEntity.getOutput(),
                areasGeometry,1, null);

        return filteredGuidList.stream().map(assetMapByGuid::get).collect(Collectors.toList());
    }

    @Override
    public List<String> getConnectIdsByDateRangeAndGeometry(List<String> inList, SubscriptionOutput output, String areasGeometryUnion, Integer fromPage, Integer limit){
        ZonedDateTime endTime = subscriptionDateTimeService.calculateEndDate(output);
        ZonedDateTime startTime = subscriptionDateTimeService.calculateStartDate(output,endTime);
        return movementSender.sendGetConnectIdsByDateAndGeometryRequest(inList,Date.from(startTime.toInstant()),Date.from(endTime.toInstant()),areasGeometryUnion,fromPage,limit);
    }
}
