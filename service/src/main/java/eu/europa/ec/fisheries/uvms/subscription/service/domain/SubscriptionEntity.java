/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */

package eu.europa.ec.fisheries.uvms.subscription.service.domain;

import static eu.europa.ec.fisheries.uvms.subscription.service.domain.StateType.INACTIVE;
import static eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity.LIST_SUBSCRIPTION;
import static eu.europa.ec.fisheries.uvms.subscription.service.domain.TriggerType.MANUAL;
import static javax.persistence.CascadeType.ALL;
import static javax.persistence.EnumType.STRING;
import static javax.persistence.GenerationType.AUTO;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
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
import java.util.List;
import java.util.UUID;

import eu.europa.ec.fisheries.uvms.commons.domain.DateRange;
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
                "SELECT s FROM SubscriptionEntity s LEFT JOIN FETCH s.conditions c WHERE " +
                        "((:channel is null AND s.channel IS NOT NULL or s.channel IS NULL) or s.channel = :channel) AND" +
                        "((:organisation is null AND s.organisation IS NOT NULL or s.organisation IS NULL) or s.organisation = :organisation) AND" +
                        "((:endPoint is null AND s.endPoint IS NOT NULL or s.endPoint IS NULL) or s.endPoint = :endPoint) AND" +
                        "((:enabled is null AND s.enabled IS NOT NULL or s.enabled IS NULL) or s.enabled = :enabled) AND" +
                        "((:name is null AND s.name IS NOT NULL or s.name IS NULL) or s.name = :name) AND" +
                        //"((:description is null AND :description IS NOT NULL or :description IS NULL) or s.description = :description) AND" +
                        "((:criteriaType is null AND c.criteriaType IS NOT NULL or c.criteriaType = 'UNKNOWN') or c.criteriaType = :criteriaType) AND " +
                        "((:subCriteriaType is null AND c.subCriteriaType IS NOT NULL or c.subCriteriaType = 'UNKNOWN') or c.subCriteriaType = :subCriteriaType) AND " +
                        "((:valueType is null AND c.valueType IS NOT NULL or c.valueType = 'UNKNOWN') or c.valueType = :valueType) AND " +
                        "((:value is null AND c.value IS NOT NULL or c.value IS NULL) or c.value = :value) AND " +
                        "((:dataType is null AND c.dataType IS NOT NULL or c.dataType = 'UNKNOWN') or c.dataType = :dataType)"
                    )
})
@EqualsAndHashCode(exclude = "conditions")
@ToString(exclude = "conditions")
public class SubscriptionEntity implements Serializable {

    public static final String LIST_SUBSCRIPTION = "subscription.list";

    @Id
    @GeneratedValue(strategy = AUTO)
    private Long id;

    @OneToMany(mappedBy = "subscription", cascade = ALL, orphanRemoval = true)
    private List<ConditionEntity> conditions;

    @Column(unique = true)
    @NotNull
    private String name;

    @Size(min = 36, max = 36)
    @Column(name = "subscription_guid", unique = true)
    private String guid;

    private String description;

    @NotNull
    private boolean enabled = true;

    @Embedded
    @NotNull
    private DateRange validityPeriod = new DateRange(new Date(), new Date(Long.MAX_VALUE));

    @NotNull
    private String organisation;

    @NotNull
    private String endPoint;

    @NotNull
    private String channel;

    @Enumerated(STRING)
    private TriggerType trigger = MANUAL;

    private String delay;

    @Enumerated(STRING)
    private StateType state = INACTIVE;

    @PrePersist
    private void prepersist() {
        setGuid(UUID.randomUUID().toString());
    }
}
