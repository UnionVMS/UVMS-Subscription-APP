/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */

package eu.europa.ec.fisheries.uvms.subscription.service.domain;

import static eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity.LIST_SUBSCRIPTION;
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
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import eu.europa.ec.fisheries.uvms.commons.domain.DateRange;
import eu.europa.ec.fisheries.uvms.subscription.service.type.TriggerType;
import eu.europa.ec.fisheries.wsdl.subscription.module.MessageType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Entity
@Data
@RequiredArgsConstructor
@Table(name = "subscription")
@NamedQueries({
        @NamedQuery(name = LIST_SUBSCRIPTION, query =
                "SELECT s FROM SubscriptionEntity s LEFT JOIN FETCH s.assets a WHERE " +
                        "((:channel is null) or s.channel = :channel) AND" +
                        "((:organisation is null) or s.organisation = :organisation) AND" +
                        "((:endPoint is null) or s.endPoint = :endPoint) AND" +
                        "((:messageType is null) or s.messageType = :messageType) AND" +
                        "((:active is null) or s.active = :active) AND" +
                        "((:name is null) or s.name = :name) AND" +
                        "((:description is null) or s.description = :description) AND" +
                        "(a.idType = 'CFR' AND a.value in (:cfrValues))"
                    )
})
@EqualsAndHashCode(exclude = "assets")
@ToString(exclude = "assets")
public class SubscriptionEntity implements Serializable {

    public static final String LIST_SUBSCRIPTION = "subscription.list";

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
    private boolean active = true;

    @Embedded
    @NotNull
    private DateRange validityPeriod = new DateRange(new Date(), new Date(Long.MAX_VALUE));

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

    @OneToMany(mappedBy = "subscription",cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<AssetIdentifierEntity> assets = new HashSet<>();

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
