/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */

package eu.europa.ec.fisheries.uvms.subscription.service.domain;

import static eu.europa.ec.fisheries.uvms.subscription.service.type.SubscriptionType.RX_PULL;
import static eu.europa.ec.fisheries.uvms.subscription.service.type.SubscriptionType.TX_PULL;
import static eu.europa.ec.fisheries.uvms.subscription.service.type.SubscriptionType.UNDEFINED;
import static eu.europa.ec.fisheries.uvms.subscription.service.type.TriggerType.MANUAL;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import eu.europa.ec.fisheries.uvms.commons.domain.DateRange;
import eu.europa.ec.fisheries.uvms.subscription.service.type.SubscriptionType;
import eu.europa.ec.fisheries.uvms.subscription.service.type.TriggerType;
import eu.europa.ec.fisheries.wsdl.subscription.module.MessageType;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Entity
@Data
@RequiredArgsConstructor
@Table(name = "subscription")
public class SubscriptionEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    private MessageType messageType;

    @Column(unique = true)
    @NotNull
    private String name;

    @Size(min = 36, max = 36)
    @Column(name = "subscription_guid", unique = true)
    private String guid;

    private String description;

    @NotNull
    private Boolean active;

    @Embedded
    private DateRange validityPeriod = new DateRange();

    @NotNull
    private String organisation;

    @NotNull
    private String endPoint;

    @NotNull
    private String channel;

    private String startCondition;

    private String endCondition;

    @Enumerated(EnumType.STRING)
    @NotNull
    private TriggerType trigger = MANUAL;

    private String delay;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "post_id")
    private Set<AssetIdentifierEntity> assets = new HashSet<>();

    @JsonProperty("subscription_type")
    public SubscriptionType getSubscriptionType(){

        SubscriptionType subscriptionType;

        switch (messageType){
            case FLUX_FA_QUERY:
                subscriptionType = RX_PULL;
                break;
            case FLUX_FA_REPORT:
            subscriptionType = TX_PULL;
                break;
            default:
                subscriptionType = UNDEFINED;
        }
        return subscriptionType;
    }

    public void addAsset(AssetIdentifierEntity asset) {
        assets.add(asset);
        asset.setSubscription(this);
    }

    public void removeAsset(AssetIdentifierEntity asset) {
        assets.remove(asset);
        asset.setSubscription(this);
    }

    @PrePersist
    private void prepersist() {
        setGuid(UUID.randomUUID().toString());
    }
}
