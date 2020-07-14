package eu.europa.ec.fisheries.uvms.subscription.rules.communication;

import eu.europa.fisheries.uvms.subscription.model.exceptions.ApplicationException;

/**
 * Fault returned from the rules module.
 */
public class RulesFaultException extends ApplicationException {
	private Integer code;
	private String fault;

	public RulesFaultException() {
	}

	public RulesFaultException(int code, String fault, String message) {
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
