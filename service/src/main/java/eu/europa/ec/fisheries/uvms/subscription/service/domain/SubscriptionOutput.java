package eu.europa.ec.fisheries.uvms.subscription.service.domain;

import static javax.persistence.EnumType.STRING;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.ElementCollection;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.OrderColumn;
import javax.validation.constraints.NotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import eu.europa.fisheries.uvms.subscription.model.enums.OutgoingMessageType;
import eu.europa.fisheries.uvms.subscription.model.enums.SubscriptionVesselIdentifier;
import eu.europa.fisheries.uvms.subscription.model.enums.SubscriptionVesselIdentifierEnumSetConverter;
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
}
