package eu.europa.fisheries.uvms.subscription.model.exceptions;

/**
 * Signals an error during the execution of a subscription.
 */
public class ExecutionException extends ApplicationException {
	public ExecutionException() {
	}

	public ExecutionException(String message) {
		super(message);
	}

	public ExecutionException(String message, Throwable cause) {
		super(message, cause);
	}

	public ExecutionException(Throwable cause) {
		super(cause);
	}
}
