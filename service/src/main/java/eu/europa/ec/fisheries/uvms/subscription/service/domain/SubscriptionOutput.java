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

import javax.persistence.*;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;

import eu.europa.ec.fisheries.uvms.commons.domain.DateRange;
import eu.europa.fisheries.uvms.subscription.model.enums.SubscriptionTimeUnit;
import eu.europa.fisheries.uvms.subscription.model.enums.OutgoingMessageType;
import eu.europa.fisheries.uvms.subscription.model.enums.SubscriptionVesselIdentifier;
import eu.europa.ec.fisheries.uvms.subscription.service.jpa.SubscriptionVesselIdentifierEnumSetConverter;
import lombok.Data;

/**
 * Configuration of the output of a subscription.
 */
@Embeddable
@Data
public class SubscriptionOutput implements Serializable {
	@Column(name = "alert")
	private boolean alert;

	@ElementCollection
	@CollectionTable(
			name = "subscription_emails",
			joinColumns = {
					@JoinColumn(name = "subscription_id")
			}
	)
	@OrderColumn(name = "index")
	@Column(name = "email")
	private List<String> emails = new ArrayList<>();

	@Column(name = "message_type")
	@NotNull
	@Enumerated(STRING)
	private OutgoingMessageType messageType;

	@Embedded
	private SubscriptionSubscriber subscriber;

	@Column(name = "has_email")
	private Boolean hasEmail;

	@Embedded
	private SubscriptionEmailConfiguration emailConfiguration;

	@Column(name = "logbook")
	private Boolean logbook;

	@Column(name = "consolidated")
	private Boolean consolidated;

	@Column(name = "vessel_ids")
	@Convert(converter = SubscriptionVesselIdentifierEnumSetConverter.class)
	private EnumSet<SubscriptionVesselIdentifier> vesselIds = EnumSet.noneOf(SubscriptionVesselIdentifier.class);

	@Column(name = "gen_new_report_id")
	private Boolean generateNewReportId;

	@Column(name = "history")
	private Integer history;

	@Column(name = "history_unit")
	@Enumerated(STRING)
	private SubscriptionTimeUnit historyUnit;

	@Embedded
	@AttributeOverrides({
			@AttributeOverride(name = "startDate", column = @Column(name = "query_start_date")),
			@AttributeOverride(name = "endDate", column = @Column(name = "query_end_date"))
	})
	@Valid
	private DateRange queryPeriod = new DateRange(new Date(), new Date(Long.MAX_VALUE));

}
