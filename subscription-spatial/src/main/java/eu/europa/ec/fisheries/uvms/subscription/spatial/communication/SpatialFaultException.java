package eu.europa.ec.fisheries.uvms.subscription.spatial.communication;

import eu.europa.fisheries.uvms.subscription.model.exceptions.ApplicationException;

/**
 * Fault returned from the spatial module.
 */
public class SpatialFaultException extends ApplicationException {
	private String code;

	@SuppressWarnings("unused")
	public SpatialFaultException() {
	}

	public SpatialFaultException(String code, String message, Throwable throwable) {
		super(message,throwable);
		this.code = code;
	}

	public String getCode() {
		return code;
	}
}
