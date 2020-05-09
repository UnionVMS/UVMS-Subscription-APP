/**
 * Classes and interfaces related to the scheduling of a subscription.
 * <p>
 * After a subscription is triggered and after each execution completes,
 * the system must schedule the next execution according to the configured rules.
 * This is represented by the {@link eu.europa.ec.fisheries.uvms.subscription.service.domain.SubscriptionExecutionEntity}.
 * Additionally, there needs to be a component that checks periodically for pending
 * instances of {@code SubscriptionExecutionEntity} that need to be handled.
 */
package eu.europa.ec.fisheries.uvms.subscription.service.scheduling;
