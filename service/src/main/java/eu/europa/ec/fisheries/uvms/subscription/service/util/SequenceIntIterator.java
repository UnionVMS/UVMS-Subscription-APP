package eu.europa.ec.fisheries.uvms.subscription.service.util;

import java.util.PrimitiveIterator;

import lombok.AllArgsConstructor;

/**
 * An {@code int} iterator giving an infinite sequence of {@code int} primitives.
 */
@AllArgsConstructor
public class SequenceIntIterator implements PrimitiveIterator.OfInt {
	private int curval;

	@Override public int nextInt() {
		return curval++;
	}

	@Override public boolean hasNext() {
		return true;
	}
}
