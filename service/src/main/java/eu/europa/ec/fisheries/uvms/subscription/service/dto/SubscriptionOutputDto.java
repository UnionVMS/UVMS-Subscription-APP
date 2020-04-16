package eu.europa.ec.fisheries.uvms.subscription.service.dto;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import eu.europa.fisheries.uvms.subscription.model.enums.OutgoingMessageType;
import eu.europa.fisheries.uvms.subscription.model.enums.SubscriptionVesselIdentifier;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Configuration of the output of a subscription.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionOutputDto {

	private boolean alert;

	private List<String> emails = new ArrayList<>();

	@NotNull
	private OutgoingMessageType messageType;

	@Valid
	private SubscriptionSubscriberDTO subscriber;

	private Boolean logbook;

	private Boolean consolidated;

	private EnumSet<SubscriptionVesselIdentifier> vesselIds = EnumSet.noneOf(SubscriptionVesselIdentifier.class);

	private Boolean generateNewReportId;

	@Min(1)
	private Integer history;
}
