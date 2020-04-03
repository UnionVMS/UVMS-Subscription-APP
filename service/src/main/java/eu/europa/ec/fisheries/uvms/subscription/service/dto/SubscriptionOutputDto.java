package eu.europa.ec.fisheries.uvms.subscription.service.dto;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import eu.europa.fisheries.uvms.subscription.model.enums.OutgoingMessageType;
import eu.europa.fisheries.uvms.subscription.model.enums.SubscriptionVesselIdentifier;
import lombok.Data;

/**
 * Configuration of the output of a subscription.
 */
@Data
public class SubscriptionOutputDto {

	private boolean alert;

	private List<String> emails = new ArrayList<>();

	private OutgoingMessageType messageType;

	private SubscriptionSubscriberDTO subscriber;

	private Boolean logbook;

	private Boolean consolidated;

	private EnumSet<SubscriptionVesselIdentifier> vesselIds = EnumSet.noneOf(SubscriptionVesselIdentifier.class);

	private Boolean generateNewReportId;

	private Integer history;
}
