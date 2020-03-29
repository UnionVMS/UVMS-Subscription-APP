package eu.europa.ec.fisheries.uvms.subscription.service.domain;

import javax.persistence.AttributeConverter;
import java.util.AbstractCollection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * Convert an {@code EnumSet} to a bitset where bit at position N is set if the
 * enum value of ordinal N is in the set.
 *
 * @param <E> The type of the enumeration
 */
public class BaseEnumSetConverter<E extends Enum<E>> implements AttributeConverter<EnumSet<E>, Integer> {

	private static final Function<Enum<?>,Integer> BIT_POSITION = e -> 1 << e.ordinal();
	private static final BinaryOperator<Integer> OR = (a,b) -> a | b;

	private class ToEnumSetCollector implements Collector<E, EnumSet<E>, EnumSet<E>> {
		@Override
		public Supplier<EnumSet<E>> supplier() {
			return () -> EnumSet.noneOf(enumClass);
		}

		@Override
		public BiConsumer<EnumSet<E>, E> accumulator() {
			return AbstractCollection::add;
		}

		@Override
		public BinaryOperator<EnumSet<E>> combiner() {
			return (set1, set2) -> {
				EnumSet<E> result = EnumSet.copyOf(set1);
				result.addAll(set2);
				return result;
			};
		}

		@Override
		public Function<EnumSet<E>, EnumSet<E>> finisher() {
			return Function.identity();
		}

		@Override
		public Set<Characteristics> characteristics() {
			return Collections.emptySet();
		}
	}

	private Class<E> enumClass;

	public BaseEnumSetConverter(Class<E> enumClass) {
		Objects.requireNonNull(enumClass);
		this.enumClass = enumClass;
	}

	@Override
	public Integer convertToDatabaseColumn(EnumSet<E> attribute) {
		return attribute == null || attribute.isEmpty() ? 0 : attribute.stream().map(BIT_POSITION).reduce(0, OR);
	}

	@Override
	public EnumSet<E> convertToEntityAttribute(Integer dbData) {
		return dbData == null || dbData == 0 ? EnumSet.noneOf(enumClass) : EnumSet.allOf(enumClass).stream().filter(e -> (dbData & BIT_POSITION.apply(e)) != 0).collect(new ToEnumSetCollector());
	}
}
