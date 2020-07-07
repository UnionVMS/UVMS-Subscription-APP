package eu.europa.fisheries.uvms.subscription.model.enums;

/**
 * The status of a triggered subscription.
 */
public enum TriggeredSubscriptionStatus {
	/**
	 * The triggered subscription is active. Executions should be executed and further executions scheduled as necessary.
	 */
	ACTIVE,
	/**
	 * The triggered subscription is inactive. Executions should not be executed (there shouldn't be any) or scheduled.
	 */
	INACTIVE,
	/**
	 * The stop conditions have been met. Any pending executions should be executed, but no more should be scheduled.
	 * If the start conditions are met again, it transitions back to the active state.
	 */
	STOPPED
}
