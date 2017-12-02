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
import java.util.Set;
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
                "SELECT DISTINCT s FROM SubscriptionEntity s " +
                "LEFT JOIN FETCH s.conditions c " +
                "LEFT JOIN FETCH s.areas a " +
                "WHERE " +
                "((:channel is NULL AND :isEmpty = false) OR s.channel = :channel) AND" +
                "((:organisation is NULL AND :isEmpty = false) OR s.organisation = :organisation) AND" +
                "((:endPoint is NULL AND :isEmpty = false) OR s.endPoint = :endPoint) AND" +
                "((:enabled is NULL AND :isEmpty = false) OR s.enabled = :enabled) AND" +
                "((:name is NULL AND :isEmpty = false) OR s.name = :name) AND" +
                "((:subscriptionType is NULL AND :isEmpty = false) OR s.subscriptionType = :subscriptionType) AND" +
                "((:messageType is NULL AND :isEmpty = false) OR s.messageType = :messageType)"
        )
})
@EqualsAndHashCode(exclude = {"conditions", "areas"})
@ToString(exclude = {"conditions", "areas"})
public class SubscriptionEntity implements Serializable {

    public static final String LIST_SUBSCRIPTION = "subscription.list";

    @Id
    @GeneratedValue(strategy = AUTO)
    private Long id;

    @NotNull
    @Enumerated(STRING)
    private SubscriptionType subscriptionType = SubscriptionType.UNKNOWN;

    @NotNull
    @Enumerated(STRING)
    private MessageType messageType = MessageType.UNKNOWN;

    @OneToMany(mappedBy = "subscription", cascade = ALL, orphanRemoval = true)
    private Set<ConditionEntity> conditions;

    @OneToMany(mappedBy = "subscription", cascade = ALL, orphanRemoval = true)
    private Set<AreaEntity> areas;

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
    private TriggerType trigger = TriggerType.UNKNOWN;

    private String delay;

    @Enumerated(STRING)
    private StateType state = StateType.UNKNOWN;

    public void addCondition(ConditionEntity condition) {
        conditions.add(condition);
        condition.setSubscription(this);
    }

    public void addArea(AreaEntity area) {
        areas.add(area);
        area.setSubscription(this);
    }

    public void removeCondition(ConditionEntity condition) {
        conditions.remove(condition);
        condition.setSubscription(null);
    }

    public void removeArea(AreaEntity area) {
        areas.remove(area);
        area.setSubscription(null);
    }

    @PrePersist
    private void prepersist() {
        setGuid(UUID.randomUUID().toString());
    }
}
