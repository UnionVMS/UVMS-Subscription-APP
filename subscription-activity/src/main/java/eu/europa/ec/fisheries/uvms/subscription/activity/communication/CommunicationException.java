package eu.europa.ec.fisheries.uvms.subscription.activity.communication;

/**
 * Thrown to indicate a communication system-level error.
 */
public class CommunicationException extends RuntimeException {
	public CommunicationException() {
	}

	public CommunicationException(String message) {
		super(message);
	}

	public CommunicationException(String message, Throwable cause) {
		super(message, cause);
	}

	public CommunicationException(Throwable cause) {
		super(cause);
	}
}
