package edu.kit.ipd.sdq.eventsim.measurement.osgi;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.osgi.framework.Bundle;

import edu.kit.ipd.sdq.eventsim.instrumentation.utils.ClassRepository;
import edu.kit.ipd.sdq.eventsim.measurement.Measurement;
import edu.kit.ipd.sdq.eventsim.measurement.MeasurementListener;
import edu.kit.ipd.sdq.eventsim.measurement.Pair;
import edu.kit.ipd.sdq.eventsim.measurement.annotation.Calculator;
import edu.kit.ipd.sdq.eventsim.measurement.calculator.BinaryCalculator;
import edu.kit.ipd.sdq.eventsim.measurement.probe.IProbe;

public class CalculatorFactory {

	private static final Logger log = Logger.getLogger(CalculatorFactory.class);

	private Map<MeasuredTypesAndMetric, Class<? extends BinaryCalculator<?, ?>>> calculatorsMap;

	public CalculatorFactory(Bundle bundle) {
		calculatorsMap = new HashMap<>();
		ClassRepository.filterClassesInBundle(bundle, this::isCalculatorType).stream()
				.map(c -> (Class<? extends BinaryCalculator<?, ?>>) c)
				.forEach(c -> calculatorsMap.put(createKeyFor(c), c));
	}

	private boolean isCalculatorType(Class<?> clazz) {
		if (!BinaryCalculator.class.isAssignableFrom(clazz))
			return false;

		Calculator a = clazz.getAnnotation(Calculator.class);
		if (a == null)
			return false;

		return true;
	}

	private MeasuredTypesAndMetric createKeyFor(Class<? extends BinaryCalculator<?, ?>> type) {
		Calculator a = type.getAnnotation(Calculator.class);
		if (Pair.class.isAssignableFrom(a.type())) {
			return new MeasuredTypesAndMetric(a.fromType(), a.toType(), a.metric());
		} else {
			return new MeasuredTypesAndMetric(a.type(), a.type(), a.metric());
		}
	}

	public BinaryCalculator<?, ?> create(String metric, Class<?> fromType, Class<?> toType) {
		// R = Pair<F, S>

		Class<? extends BinaryCalculator<?, ?>> calculatorClass = calculatorsMap
				.get(new MeasuredTypesAndMetric(fromType, toType, metric));

		if (calculatorClass == null) {
			for (Class<?> cf : allSupertypes(fromType)) {
				for (Class<?> ct : allSupertypes(toType)) {
					calculatorClass = calculatorsMap.get(new MeasuredTypesAndMetric(cf, ct, metric));
					if (calculatorClass != null) {
						break;
					}
				}
				if (calculatorClass != null) {
					break;
				}
			}
		}

		if (calculatorClass == null) {
			log.error(String.format("No calculaotr has been found capable of measuring metric \"%s\" for elements of "
					+ "type %s and %s", metric, fromType, toType));
			return nullCalculator();
		}

		BinaryCalculator<?, ?> calculator = null;

		try {
			Constructor<? extends BinaryCalculator<?, ?>> c = calculatorClass.getConstructor();
			calculator = c.newInstance();
			log.debug("Created calculator " + calculator + " (from=" + fromType + ", to=" + toType + ", metric="
					+ metric + ")");
		} catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			log.error("Exception while invoking probe constructor.", e);
			return nullCalculator();
		}

		return calculator;
	}

	private <F, S> BinaryCalculator<F, S> nullCalculator() {
		return new BinaryCalculator<F, S>() {

			@Override
			public void forEachMeasurement(MeasurementListener<Pair<F, S>> l) {
			}

			@Override
			public void setup(IProbe<F> fromProbe, IProbe<S> toProbe) {
			}

			@Override
			public Measurement<Pair<F, S>> calculate(Measurement<F> first, Measurement<S> second) {
				return null;
			}
		};
	}

	private <E> List<Class<?>> allSupertypes(Class<E> type) {
		List<Class<?>> superTypes = new ArrayList<>();

		List<Class<?>> currentTypes = Arrays.asList(type);

		while (!currentTypes.isEmpty()) {
			List<Class<?>> nextTypes = new ArrayList<>();

			for (Class<?> c : currentTypes) {
				nextTypes.addAll(Arrays.asList(c.getInterfaces()));

				Class<?> sc;
				if ((sc = c.getSuperclass()) != null) {
					nextTypes.add(sc);
				}
			}

			superTypes.addAll(nextTypes);
			currentTypes = nextTypes;
		}

		return superTypes;
	}

	private class MeasuredTypesAndMetric {

		private Class<?> fromType;
		private Class<?> toType;

		private String metric;

		public MeasuredTypesAndMetric(Class<?> fromType, Class<?> toType, String metric) {
			this.fromType = fromType;
			this.toType = toType;
			this.metric = metric;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((fromType == null) ? 0 : fromType.hashCode());
			result = prime * result + ((toType == null) ? 0 : toType.hashCode());
			result = prime * result + ((metric == null) ? 0 : metric.hashCode());
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
			MeasuredTypesAndMetric other = (MeasuredTypesAndMetric) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (fromType == null) {
				if (other.fromType != null)
					return false;
			} else if (!fromType.equals(other.fromType))
				return false;
			if (toType == null) {
				if (other.toType != null)
					return false;
			} else if (!toType.equals(other.toType))
				return false;
			if (metric == null) {
				if (other.metric != null)
					return false;
			} else if (!metric.equals(other.metric))
				return false;
			return true;
		}

		private CalculatorFactory getOuterType() {
			return CalculatorFactory.this;
		}

	}

}
