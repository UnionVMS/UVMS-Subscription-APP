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
import static javax.persistence.CascadeType.MERGE;
import static javax.persistence.EnumType.STRING;
import static javax.persistence.GenerationType.AUTO;
import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;

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
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import eu.europa.ec.fisheries.uvms.commons.domain.DateRange;
import eu.europa.ec.fisheries.uvms.subscription.service.mapper.SubscriptionParser;
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
@EqualsAndHashCode(exclude = {"id"})
public class SubscriptionEntity implements Serializable {

    public static final String LIST_SUBSCRIPTION = "subscription.list";

    @Id
    @GeneratedValue(strategy = AUTO)
    private Long id;

    @NotNull
    @Enumerated(STRING)
    @Column(name = "subscription_type")
    private SubscriptionType subscriptionType;

    @NotNull
    @Enumerated(STRING)
    @Column(name = "message_type")
    private MessageType messageType;

    @OneToMany(mappedBy = "subscription", cascade = ALL, orphanRemoval = true)
    @Valid
    private Set<ConditionEntity> conditions = new HashSet<>();

    @OneToMany(mappedBy = "subscription", cascade = MERGE, orphanRemoval = true)
    @Valid
    private Set<AreaEntity> areas = new HashSet<>();

    @Column(unique = true)
    @NotNull
    private String name;

    @Size(min = 36, max = 36)
    @Column(name = "subscription_guid", unique = true)
    @NotNull
    private String guid;

    private String description;

    @NotNull
    private boolean enabled;

    @Embedded
    @Valid
    private DateRange validityPeriod = new DateRange(new Date(), new Date(Long.MAX_VALUE));

    @NotNull
    private String organisation;

    @NotNull
    @Column(name = "end_point")
    private String endPoint;

    @NotNull
    private String channel;

    @Enumerated(STRING)
    @NotNull
    @Column(name = "trigger_type")
    private TriggerType triggerType;

    private String delay;

    @Enumerated(STRING)
    @NotNull
    @Column(name = "state_type")
    private StateType stateType;

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

    public static SubscriptionEntity random(){
        SubscriptionEntity subscriptionEntity = new SubscriptionEntity();
        subscriptionEntity.setChannel(randomAlphabetic(100));
        subscriptionEntity.setDescription(randomAlphabetic(200));
        subscriptionEntity.setEndPoint(randomAlphabetic(100));
        subscriptionEntity.setName(randomAlphabetic(40));
        subscriptionEntity.setOrganisation(randomAlphabetic(40));
        subscriptionEntity.setEnabled(new Random().nextBoolean());
        subscriptionEntity.setMessageType(MessageType.values()[new Random().nextInt(MessageType.values().length)]);
        subscriptionEntity.setStateType(StateType.values()[new Random().nextInt(StateType.values().length)]);
        subscriptionEntity.setTriggerType(TriggerType.values()[new Random().nextInt(TriggerType.values().length)]);
        subscriptionEntity.setSubscriptionType(SubscriptionType.values()[new Random().nextInt(SubscriptionType.values().length)]);
        return subscriptionEntity;
    }

    public String toExpression(ConditionType type){
        return SubscriptionParser.parseCondition(type, this);
    }
}
