/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subscription.service.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import java.util.Date;

import eu.europa.fisheries.uvms.subscription.model.enums.SubscriptionExecutionStatusType;
import lombok.Getter;
import lombok.Setter;

/**
 * Represent the execution event of a subscription.
 */
@Entity
@Table(name = "subscription_execution")
@Getter
@Setter
public class SubscriptionExecutionEntity {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /** The point in time at which the system should execute this. */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "requested_time")
    private Date requestedTime;

    /** The point in time when execution was queued. */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "queued_time")
    private Date queuedTime;

    /** The point in time when execution actually finished. */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "execution_time")
    private Date executionTime;

    /** The time this entry was creted. */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "creation_date")
    private Date creationDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private SubscriptionExecutionStatusType status;

    @ManyToOne
    @JoinColumn(name = "triggered_subscription_id")
    private TriggeredSubscriptionEntity triggeredSubscription;
}
