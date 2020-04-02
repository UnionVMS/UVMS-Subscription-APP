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
import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.AUTO;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import java.util.UUID;

import eu.europa.ec.fisheries.wsdl.subscription.module.AreaType;
import eu.europa.ec.fisheries.wsdl.subscription.module.AreaValueType;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Table(name = "area")
@Entity
@Data
@NoArgsConstructor
@EqualsAndHashCode(exclude = {"id", "subscription"})
@ToString(exclude = {"subscription"})
public class AreaEntity {

    @Id
    @GeneratedValue(strategy = AUTO)
    private Long id;

    @Size(min = 36, max = 36)
    @Column(name = "area_guid", unique = true)
    @NotNull
    private String guid;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "subscription_id")
    @Valid
    @Setter(AccessLevel.MODULE)
    @Getter(AccessLevel.NONE)
    private SubscriptionEntity subscription;

    @Column(name = "area_type")
    @Enumerated(STRING)
    @NotNull
    private AreaType areaType;

    @Column(name = "area_value_type")
    @Enumerated(STRING)
    @NotNull
    private AreaValueType areaValueType;

    @NotNull
    private String value;

    @PrePersist
    private void prepersist() {
        setGuid(UUID.randomUUID().toString());
    }
}
