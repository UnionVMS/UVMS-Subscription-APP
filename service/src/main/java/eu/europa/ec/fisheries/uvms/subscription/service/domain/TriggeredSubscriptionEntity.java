/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subscription.service.domain;

import static javax.persistence.GenerationType.AUTO;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;

/**
 * Capture data about a triggered subscription instance.
 */
@Entity
@Table(name = "triggered_subscription")
@Getter
@Setter
public class TriggeredSubscriptionEntity {
	@Id
	@Column(name = "id")
	@GeneratedValue(strategy = AUTO)
	private Long id;

	@Column(name = "source")
	private String source;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "creation_date")
	private Date creationDate;

	@ManyToOne
	@JoinColumn(name = "subscription_id")
	private SubscriptionEntity subscription;

	@OneToMany(orphanRemoval = true, cascade = CascadeType.ALL, mappedBy = "triggeredSubscription")
	private Set<TriggeredSubscriptionDataEntity> data = new HashSet<>();

	/**
	 * This is the inverse of what is referenced as "stop" in the specifications.
	 */
	@Column(name="active")
	private Boolean active;

	/**
	 * Referenced as incoming message id in specifications.
	 */
	@Column(name="trigger_message_id")
	private String triggerMessageId;
}
