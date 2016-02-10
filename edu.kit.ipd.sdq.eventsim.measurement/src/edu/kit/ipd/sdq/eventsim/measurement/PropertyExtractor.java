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
		if (contains(type)) {
			return get(type);
		} else {
			for (Class<?> iface : type.getInterfaces()) {
				Function<Object, String>  extractor = extractorForType(iface);
				if (extractor != null) {
					return extractor;
				}
			}
			return extractorForType(type.getSuperclass());
		}
		
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
