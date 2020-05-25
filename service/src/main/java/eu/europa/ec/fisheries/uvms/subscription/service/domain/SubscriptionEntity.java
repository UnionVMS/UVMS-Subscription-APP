/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */

package eu.europa.ec.fisheries.uvms.subscription.service.domain;

import static javax.persistence.CascadeType.ALL;
import static javax.persistence.EnumType.STRING;
import static javax.persistence.GenerationType.AUTO;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import eu.europa.ec.fisheries.uvms.commons.domain.DateRange;
import eu.europa.fisheries.uvms.subscription.model.enums.AccessibilityType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "subscription")
@NamedQueries({
        @NamedQuery(name = SubscriptionEntity.BY_NAME, query = "SELECT s FROM SubscriptionEntity s " +
                //"LEFT JOIN FETCH s.conditions c " +
                //"LEFT JOIN FETCH s.areas a " +
                "WHERE s.name = :name")
})
@EqualsAndHashCode(exclude = {"id"})
public class SubscriptionEntity implements Serializable {

    public static final String LIST_SUBSCRIPTION = "subscription.listSubscriptions";
    public static final String BY_NAME = "subscription.byName";

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = AUTO)
    private Long id;
//
//    @OneToMany(mappedBy = "subscription", cascade = ALL, orphanRemoval = true)
//    @Valid
//    private Set<ConditionEntity> conditions = new HashSet<>();
//

    @Column(unique = true, name = "name")
    @NotNull
    private String name;

    @Enumerated(STRING)
    @Column(name = "accessibility")
    private AccessibilityType accessibility;

    @Column(name = "description")
    private String description;

    @Column(name = "active")
    @NotNull
    private boolean active;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "startDate", column = @Column(name = "start_date")),
            @AttributeOverride(name = "endDate", column = @Column(name = "end_date"))
    })
    @Valid
    private DateRange validityPeriod = new DateRange(new Date(), new Date(Long.MAX_VALUE));

    @Embedded
    @Valid
    private SubscriptionOutput output;

    @Embedded
    @Valid
    private SubscriptionExecution execution;

    @OneToMany(mappedBy = "subscription", cascade = ALL, orphanRemoval = true)
    @Valid
    private Set<AreaEntity> areas = new HashSet<>();

    @OneToMany(mappedBy = "subscription", cascade = ALL, orphanRemoval = true)
    @Valid
    private Set<AssetEntity> assets = new HashSet<>();

    @OneToMany(mappedBy = "subscription", cascade = ALL, orphanRemoval = true)
    @Valid
    private Set<AssetGroupEntity> assetGroups = new HashSet<>();

    public void setAreas(Set<AreaEntity> areas) {
        this.areas.clear();
        this.areas.addAll(areas);
    }

    public void setAssets(Set<AssetEntity> assets) {
        this.assets.clear();
        this.assets.addAll(assets);
    }

    public void setAssetGroups(Set<AssetGroupEntity> assetGroups) {
        this.assetGroups.clear();
        this.assetGroups.addAll(assetGroups);
    }
}
