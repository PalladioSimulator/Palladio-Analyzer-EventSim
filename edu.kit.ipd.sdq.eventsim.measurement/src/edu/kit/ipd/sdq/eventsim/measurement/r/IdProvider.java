package edu.kit.ipd.sdq.eventsim.measurement.r;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.apache.log4j.Logger;

public class IdProvider {

	private static final Logger log = Logger.getLogger(IdProvider.class);
	
	private Map<Class<? extends Object>, Function<Object, String>> idExtractorMap;
	
	public IdProvider() {
		idExtractorMap = new HashMap<>();
	}
	
	public void add(Class<? extends Object> elementClass, Function<Object, String> extractionFunction) {
		idExtractorMap.put(elementClass, extractionFunction);
	}
	
	public boolean contains(Class<? extends Object> elementClass) {
		return idExtractorMap.containsKey(elementClass);
	}
	
	public Function<Object, String> get(Class<? extends Object> elementClass) {
		return idExtractorMap.get(elementClass);
	}
	
	private Function<Object, String> findIdExtractorForType(Class<?> type) {
		if (type == null) {
			return null;
		}
		if (type.equals(Object.class)) {
			return null;
		}
		if (contains(type)) {
			return get(type);
		} else {
			Function<Object, String> x = findIdExtractorForType(type.getSuperclass());
			if (x != null) {
				return x;
			}

			for (Class<?> iface : type.getInterfaces()) {
				x = findIdExtractorForType(iface);
				if (x != null) {
					return x;
				}
			}
		}
		return null;
	}
	
	public String toIdString(Object o) {
		Function<Object, String> extractor = get(o.getClass());
		if (extractor == null) {
			// try to find extractor for one of the type's supertypes (classes + interfaces)
			extractor = findIdExtractorForType(o.getClass());
			if (extractor != null) {
				// found extractor for a supertype -> store that mapping to prevent the same lookup over and over
				// again
				add(o.getClass(), extractor);
			} else {
				// fallback
				log.warn("Could not find id extractor for class " + o.getClass() + ".");
				return o.toString();
			}
		}
		return extractor.apply(o);
	}
	
}
