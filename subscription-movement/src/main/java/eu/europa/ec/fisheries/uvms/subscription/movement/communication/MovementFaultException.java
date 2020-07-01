package eu.europa.ec.fisheries.uvms.subscription.movement.communication;

import eu.europa.fisheries.uvms.subscription.model.exceptions.ApplicationException;

/**
 * Fault returned from the movement module.
 * See the {@code eu.europa.ec.fisheries.schema.movement.common.v1.ExceptionType}.
 */
public class MovementFaultException extends ApplicationException {
	private Integer code;
	private String fault;

	@SuppressWarnings("unused")
	public MovementFaultException() {
	}

	public MovementFaultException(int code, String fault, String message) {
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
