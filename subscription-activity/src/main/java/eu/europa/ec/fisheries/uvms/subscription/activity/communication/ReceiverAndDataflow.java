package eu.europa.ec.fisheries.uvms.subscription.activity.communication;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Contains a receiver symbolic name and a dataflow URN.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReceiverAndDataflow {
	private String receiver;
	private String dataflow;
}
