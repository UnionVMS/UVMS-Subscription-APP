/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */

package eu.europa.ec.fisheries.uvms.subscription.service.domain;

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
import javax.persistence.Table;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

import eu.europa.ec.fisheries.uvms.commons.domain.DateRange;
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
        @NamedQuery(name = SubscriptionEntity.LIST_SUBSCRIPTION, query =
                "SELECT DISTINCT s FROM SubscriptionEntity s " +
                "LEFT JOIN FETCH s.conditions c " +
                "LEFT JOIN FETCH s.areas a " +
                " WHERE " +
                        "((:channel is NULL) OR (((:strict = false) AND UPPER(cast(s.channel as string)) LIKE CONCAT('%', UPPER(cast(:channel as string)), '%')) OR ((:strict = true) AND s.channel = :channel))) AND " +
                        "((:organisation is NULL) OR (((:strict = false) AND UPPER(cast(s.organisation as string)) LIKE CONCAT('%', UPPER(cast(:organisation as string)), '%')) OR ((:strict = true) AND s.organisation = :organisation))) AND " +
                        "((:endPoint is NULL) OR (((:strict = false) AND UPPER(cast(s.endPoint as string)) LIKE CONCAT('%', UPPER(cast(:endPoint as string)), '%')) OR ((:strict = true) AND s.endPoint = :endPoint))) AND " +
                "((:enabled is NULL) OR s.enabled = :enabled) AND " +
                "((:name is NULL) OR (UPPER(cast(s.name as string)) LIKE CONCAT('%', UPPER(cast(:name as string)), '%'))) AND " +
                "((:subscriptionType is NULL) OR (UPPER(cast(s.subscriptionType as string)) = UPPER(cast(:subscriptionType as string)))) AND " +
                "((:messageType is NULL) OR (UPPER(cast(s.messageType as string)) LIKE CONCAT('%', UPPER(cast(:messageType as string)), '%'))) AND " +
                "((:accessibility is NULL) OR (UPPER(cast(s.accessibility as string)) = UPPER(cast(:accessibility as string)))) AND " +
                "((:description is NULL) OR (UPPER(cast(s.description as string)) LIKE CONCAT('%', UPPER(cast(:description as string)), '%'))) AND " +
                "(cast(:startDate as timestamp) <= s.validityPeriod.startDate OR cast(:endDate as timestamp) <= s.validityPeriod.endDate) "
        ),
        @NamedQuery(name = SubscriptionEntity.BY_NAME, query = "SELECT s FROM SubscriptionEntity s " +
                "LEFT JOIN FETCH s.conditions c " +
                "LEFT JOIN FETCH s.areas a " +
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
//    @OneToMany(mappedBy = "subscription", cascade = MERGE, orphanRemoval = true)
//    @Valid
//    private Set<AreaEntity> areas = new HashSet<>();

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

    // TODO: Start conditions
    // TODO: Stop conditions
}
