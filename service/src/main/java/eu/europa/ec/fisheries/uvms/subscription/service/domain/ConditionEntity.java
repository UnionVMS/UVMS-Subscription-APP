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

import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.io.Serializable;

import eu.europa.ec.fisheries.wsdl.subscription.module.CriteriaType;
import eu.europa.ec.fisheries.wsdl.subscription.module.DataType;
import eu.europa.ec.fisheries.wsdl.subscription.module.SubCriteriaType;
import eu.europa.ec.fisheries.wsdl.subscription.module.ValueType;
import lombok.Data;

@Entity
@Data
@Table(name = "condition")
public class ConditionEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(name="subscription_id")
    private SubscriptionEntity subscription;

    @Enumerated(STRING)
    private ConditionType conditionType = ConditionType.UNKNOWN;

    @Enumerated(STRING)
    private DataType dataType = DataType.UNKNOWN;

    @Enumerated(STRING)
    private CriteriaType criteriaType = CriteriaType.UNKNOWN;

    @Enumerated(STRING)
    private SubCriteriaType subCriteriaType = SubCriteriaType.UNKNOWN;

    @Enumerated(STRING)
    private ValueType valueType = ValueType.UNKNOWN;

    private String value;

    @Enumerated(STRING)
    private CompositeType compositeType = CompositeType.UNKNOWN;

}
