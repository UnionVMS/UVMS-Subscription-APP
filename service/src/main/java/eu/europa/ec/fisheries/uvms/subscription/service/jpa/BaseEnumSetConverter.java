/*
 Developed by the European Commission - Directorate General for Maritime Affairs and Fisheries @ European Union, 2015-2016.

 This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can redistribute it
 and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of
 the License, or any later version. The IFDM Suite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 details. You should have received a copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.subscription.service.jpa;

import javax.persistence.AttributeConverter;
import java.util.AbstractCollection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.IntBinaryOperator;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.stream.Collector;

/**
 * Convert an {@code EnumSet} to a bitset where bit at position N is set if the
 * enum value of ordinal N is in the set.
 *
 * @param <E> The type of the enumeration
 */
public class BaseEnumSetConverter<E extends Enum<E>> implements AttributeConverter<EnumSet<E>, Integer> {

	private static final ToIntFunction<Enum<?>> BIT_POSITION = e -> 1 << e.ordinal();
	private static final IntBinaryOperator OR = (a,b) -> a | b;

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

	private final Class<E> enumClass;

	public BaseEnumSetConverter(Class<E> enumClass) {
		Objects.requireNonNull(enumClass);
		this.enumClass = enumClass;
	}

	@Override
	public Integer convertToDatabaseColumn(EnumSet<E> attribute) {
		return attribute == null || attribute.isEmpty() ? 0 : attribute.stream().mapToInt(BIT_POSITION).reduce(0, OR);
	}

	@Override
	public EnumSet<E> convertToEntityAttribute(Integer dbData) {
		return dbData == null || dbData == 0 ? EnumSet.noneOf(enumClass) : EnumSet.allOf(enumClass).stream().filter(e -> (dbData & BIT_POSITION.applyAsInt(e)) != 0).collect(new ToEnumSetCollector());
	}
}
