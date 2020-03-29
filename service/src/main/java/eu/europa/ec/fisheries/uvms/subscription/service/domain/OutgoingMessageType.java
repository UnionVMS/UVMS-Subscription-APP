package eu.europa.ec.fisheries.uvms.subscription.service.domain;

/**
 * The outgoing message type of a subscription.
 */
public enum OutgoingMessageType {
	/** No outgoing message. */
	NONE,

	/** The system will send FA Report(s). */
	FA_REPORT,

	/** The system will create and send FA Query(ies). */
	FA_QUERY,

	/** The system will create and send a Position. */
	POSITION,

	/** Sale note. */
	SALE_NOTE,
}
