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

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

import eu.europa.fisheries.uvms.subscription.model.enums.CompositeType;
import eu.europa.fisheries.uvms.subscription.model.enums.ConditionType;
import eu.europa.fisheries.uvms.subscription.model.enums.RelationalOperatorType;
import eu.europa.ec.fisheries.wsdl.subscription.module.CriteriaType;
import eu.europa.ec.fisheries.wsdl.subscription.module.MessageType;
import eu.europa.ec.fisheries.wsdl.subscription.module.SubCriteriaType;
import eu.europa.ec.fisheries.wsdl.subscription.module.ValueType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Data
@Table(name = "condition")
@NoArgsConstructor
@EqualsAndHashCode(exclude = {"id", "subscription", "position"})
@ToString(exclude = "subscription")
public class ConditionEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(name="subscription_id")
    private SubscriptionEntity subscription;

    @NotNull
    @Enumerated(STRING)
    @Column(name = "condition_type")
    private ConditionType conditionType;

    @NotNull
    private Integer position = 0;

    @Column(name = "start_operator")
    private String startOperator;

    @Enumerated(STRING)
    @Column(name = "criteria_type")
    private CriteriaType criteriaType;

    @Enumerated(STRING)
    @Column(name = "sub_criteria_type")
    private SubCriteriaType subCriteriaType;

    @Enumerated(STRING)
    protected RelationalOperatorType condition;

    @Enumerated(STRING)
    @Column(name = "message_type")
    private MessageType messageType;

    @Enumerated(STRING)
    @Column(name = "value_type")
    private ValueType valueType;

    private String value;

    @Column(name = "end_operator")
    private String endOperator;

    @Enumerated(STRING)
    @Column(name = "composite_type")
    private CompositeType compositeType;
}
