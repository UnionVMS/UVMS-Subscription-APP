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

package eu.europa.ec.fisheries.uvms.subscription.service.bean;

import eu.europa.ec.fisheries.uvms.subscription.service.domain.AssetEntity;
import eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionOutput;
import eu.europa.ec.fisheries.uvms.subscription.service.filter.AreaFiltererComponent;
import eu.europa.ec.fisheries.uvms.subscription.service.mapper.CustomMapper;
import eu.europa.ec.fisheries.uvms.subscription.service.messaging.asset.AssetSender;
import eu.europa.ec.fisheries.wsdl.asset.types.AssetHistGuidIdWithVesselIdentifiers;
import lombok.extern.slf4j.Slf4j;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

@ApplicationScoped
@Slf4j
public class SubscriptionAssetServiceBean implements SubscriptionAssetService{
    
    private AssetSender assetSender;
    private AreaFiltererComponent areaFiltererComponent;
    /**
     * Constructor for frameworks.
     */
    @SuppressWarnings("unused")
    SubscriptionAssetServiceBean(){
        //NOOP
    }
    
    @Inject
    public SubscriptionAssetServiceBean(AssetSender assetSender, AreaFiltererComponent areaFiltererComponent) {
        this.assetSender = assetSender;
        this.areaFiltererComponent = areaFiltererComponent;
    }
    
    @Override
    public List<AssetEntity> getAssetEntitiesPaginated(String areasGeometry, SubscriptionOutput output,Integer page, Integer limit){
        if(areasGeometry == null){
            return Collections.emptyList();
        }
        List<String> connectIds = areaFiltererComponent.getConnectIdsByDateRangeAndGeometry(null, output, areasGeometry,page,limit);
        if(connectIds.isEmpty()){
            return Collections.emptyList();
        }
        List<AssetHistGuidIdWithVesselIdentifiers> newIdentifiers = assetSender.findMultipleVesselIdentifiers(connectIds);
        return CustomMapper.toAssetEntityListFrom(newIdentifiers);
    }
}
