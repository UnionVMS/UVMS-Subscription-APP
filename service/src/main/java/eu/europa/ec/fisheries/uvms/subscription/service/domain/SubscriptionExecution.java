/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2020.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subscription.service.domain;

import static javax.persistence.EnumType.STRING;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Enumerated;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import java.io.Serializable;
import java.util.Date;

import eu.europa.fisheries.uvms.subscription.model.enums.SubscriptionTimeUnit;
import eu.europa.fisheries.uvms.subscription.model.enums.TriggerType;
import lombok.Data;

/**
 * Encapsulates instructions for how and when to execute a subscription.
 */
@Embeddable
@Data
public class SubscriptionExecution implements Serializable {

    @Enumerated(STRING)
    @NotNull
    @Column(name = "trigger_type")
    private TriggerType triggerType;

	@Column(name = "frequency")
	@Min(0)
    private Integer frequency;

	@Column(name = "frequency_unit")
	@Enumerated(STRING)
	private SubscriptionTimeUnit frequencyUnit;

	@Column(name = "immediate")
	private Boolean immediate;

	@Column(name = "time_expr")
	private String timeExpression;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "next_scheduled_execution")
	private Date nextScheduledExecution;
}
