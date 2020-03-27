package eu.europa.ec.fisheries.uvms.subscription.service.domain.search;

/**
 * Data describing the desired query ordering.
 *
 * @param <F> An enum describing the field on which to sort
 */
public interface OrderByData<F extends Enum> {
	DirectionType getDirection();
	F getField();
}
