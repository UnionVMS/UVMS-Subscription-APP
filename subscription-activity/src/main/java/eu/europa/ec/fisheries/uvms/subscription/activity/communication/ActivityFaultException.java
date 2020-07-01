package eu.europa.ec.fisheries.uvms.subscription.activity.communication;

import eu.europa.fisheries.uvms.subscription.model.exceptions.ApplicationException;

/**
 * Fault returned from the activity module.
 */
public class ActivityFaultException extends ApplicationException {
	private Integer code;
	private String fault;

	@SuppressWarnings("unused")
	public ActivityFaultException() {
	}

	public ActivityFaultException(int code, String fault, String message) {
		super(message);
		this.code = code;
		this.fault = fault;
	}

	public Integer getCode() {
		return code;
	}

	public String getFault() {
		return fault;
	}
}
