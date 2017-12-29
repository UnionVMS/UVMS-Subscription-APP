/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */

package eu.europa.ec.fisheries.uvms.subscription.service.domain;

import static eu.europa.ec.fisheries.uvms.commons.date.DateUtils.END_OF_TIME;
import static eu.europa.ec.fisheries.uvms.commons.date.DateUtils.nowUTC;
import static eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionEntity.BY_NAME;
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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import eu.europa.ec.fisheries.uvms.commons.domain.DateRange;
import eu.europa.ec.fisheries.uvms.subscription.service.mapper.SubscriptionParser;
import eu.europa.ec.fisheries.wsdl.subscription.module.MessageType;
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
                " WHERE " +
                "((:channel is NULL) OR (UPPER(cast(s.channel as string)) LIKE CONCAT('%', UPPER(cast(:channel as string)), '%'))) AND " +
                "((:organisation is NULL) OR (UPPER(cast(s.organisation as string)) LIKE CONCAT('%', UPPER(cast(:organisation as string)), '%'))) AND " +
                "((:endPoint is NULL) OR (UPPER(cast(s.endPoint as string)) LIKE CONCAT('%', UPPER(cast(:endPoint as string)), '%'))) AND " +
                "((:enabled is NULL) OR s.enabled = :enabled) AND " +
                "((:name is NULL) OR (UPPER(cast(s.name as string)) LIKE CONCAT('%', UPPER(cast(:name as string)), '%'))) AND " +
                "((:subscriptionType is NULL) OR (UPPER(cast(s.subscriptionType as string)) = UPPER(cast(:subscriptionType as string)))) AND " +
                "((:messageType is NULL) OR (UPPER(cast(s.messageType as string)) LIKE CONCAT('%', UPPER(cast(:messageType as string)), '%'))) AND " +
                "((:accessibility is NULL) OR (UPPER(cast(s.accessibility as string)) = UPPER(cast(:accessibility as string)))) AND " +
                "((:description is NULL) OR (UPPER(cast(s.description as string)) LIKE CONCAT('%', UPPER(cast(:description as string)), '%'))) AND " +
                "(cast(:startDate as timestamp) <= s.validityPeriod.startDate AND cast(:endDate as timestamp) >= s.validityPeriod.endDate) "
        ),
        @NamedQuery(name = BY_NAME, query = "SELECT s FROM SubscriptionEntity s " +
                "LEFT JOIN FETCH s.conditions c " +
                "LEFT JOIN FETCH s.areas a " +
                "WHERE s.name = :name")
})
@EqualsAndHashCode(exclude = {"id"})
public class SubscriptionEntity implements Serializable {

    public static final String LIST_SUBSCRIPTION = "subscription.listSubscriptions";
    public static final String BY_NAME = "subscription.byName";

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

    @NotNull
    @Enumerated(STRING)
    private AccessibilityType accessibility;

    private String description;

    @NotNull
    @JsonProperty("isActive")
    private boolean enabled;

    @Embedded
    @Valid
    @JsonUnwrapped
    private DateRange validityPeriod = new DateRange(new Date(), new Date(Long.MAX_VALUE));

    @NotNull
    private String organisation;

    @NotNull
    @Column(name = "end_point")
    private String endPoint;

    @NotNull
    @JsonProperty("communicationChannel")
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
        if (validityPeriod == null){
            validityPeriod = new DateRange(new Date(), new Date(Long.MAX_VALUE));
        }
        if (validityPeriod.getStartDate() == null){
            validityPeriod.setStartDate(nowUTC().toDate());
        }
        if (validityPeriod.getEndDate() == null){
            validityPeriod.setEndDate(END_OF_TIME.toDate());
        }
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
        subscriptionEntity.setAccessibility(AccessibilityType.values()[new Random().nextInt(AccessibilityType.values().length)]);
        return subscriptionEntity;
    }

    public String toExpression(ConditionType type){
        return SubscriptionParser.parseCondition(type, this);
    }
}
