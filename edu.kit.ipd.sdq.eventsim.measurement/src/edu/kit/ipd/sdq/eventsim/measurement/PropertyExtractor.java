package edu.kit.ipd.sdq.eventsim.measurement;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.apache.log4j.Logger;

public class PropertyExtractor {

	private static final Logger log = Logger.getLogger(PropertyExtractor.class);

	private Map<Class<? extends Object>, Function<Object, String>> extractorMap;

	public PropertyExtractor() {
		extractorMap = new HashMap<>();
	}

	public void add(Class<? extends Object> elementClass, Function<Object, String> extractionFunction) {
		extractorMap.put(elementClass, extractionFunction);
	}

	public boolean contains(Class<? extends Object> elementClass) {
		return extractorMap.containsKey(elementClass);
	}

	public Function<Object, String> get(Class<? extends Object> elementClass) {
		return extractorMap.get(elementClass);
	}

	private Function<Object, String> extractorForType(Class<?> type) {
		if (type == null) {
			return null;
		}
		if (type.equals(Object.class)) {
			return null;
		}
		if (contains(type)) {
			return get(type);
		} else {
			Function<Object, String> x = extractorForType(type.getSuperclass());
			if (x != null) {
				return x;
			}

			for (Class<?> iface : type.getInterfaces()) {
				x = extractorForType(iface);
				if (x != null) {
					return x;
				}
			}
		}
		return null;
	}

	public String extractFrom(Object o) {
		Function<Object, String> extractor = get(o.getClass());
		if (extractor == null) {
			// try to find extractor for one of the type's supertypes (classes + interfaces)
			extractor = extractorForType(o.getClass());
			if (extractor != null) {
				// found extractor for a supertype -> store that mapping to prevent the same lookup over and over
				// again
				add(o.getClass(), extractor);
			} else {
				// fallback
				log.warn("Could not find property extractor for class " + o.getClass() + ".");
				return o.toString();
			}
		}
		return extractor.apply(o);
	}

}
