package edu.kit.ipd.sdq.eventsim.measurement;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class MeasuringPoint<E> {

	private E element;

	private String property;

	private Object[] contexts;

	public MeasuringPoint(E element, String property, Object... contexts) {
		this.element = element;
		this.property = property;
		this.contexts = contexts;
	}

	public MeasuringPoint(E element, Object... contexts) {
		this(element, "", contexts);
	}

	public E getElement() {
		return element;
	}

	public String getProperty() {
		return property;
	}

	public Object[] getContexts() {
		return contexts;
	}

	public MeasuringPoint<E> withAddedContexts(Object... contexts) {
		Set<Object> contextsSet = new HashSet<Object>();
		contextsSet.addAll(Arrays.asList(this.contexts));
		contextsSet.addAll(Arrays.asList(contexts));
		return new MeasuringPoint<>(this.element, this.property, contextsSet.toArray());
	}
	
	public MeasuringPoint<E> withProperty(String property) {
		return new MeasuringPoint<>(this.element, property, this.contexts);
	}
	
	public boolean equalsOrIsMoreSpecific(MeasuringPoint<E> other) {
		if (!other.element.equals(element)) {
			return false;
		}
		if (!other.property.equals(property)) {
			return false;
		}
		// check if measurement contexts of this class are a superset of the other class's contexts.
		Set<Object> localContexts = new HashSet<>();
		Collections.addAll(localContexts, contexts);
		for (Object c : other.contexts) {
			if (!localContexts.contains(c)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("MeasuringPoint [element=").append(element).append(", property=").append(property)
				.append(", contexts=").append(Arrays.toString(contexts)).append("]");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(contexts);
		result = prime * result + ((element == null) ? 0 : element.hashCode());
		result = prime * result + ((property == null) ? 0 : property.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MeasuringPoint<?> other = (MeasuringPoint<?>) obj;
		if (!Arrays.equals(contexts, other.contexts))
			return false;
		if (element == null) {
			if (other.element != null)
				return false;
		} else if (!element.equals(other.element))
			return false;
		if (property == null) {
			if (other.property != null)
				return false;
		} else if (!property.equals(other.property))
			return false;
		return true;
	}

}
