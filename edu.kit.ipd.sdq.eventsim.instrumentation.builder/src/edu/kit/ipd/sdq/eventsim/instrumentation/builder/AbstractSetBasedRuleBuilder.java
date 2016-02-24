package edu.kit.ipd.sdq.eventsim.instrumentation.builder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.CalculatorRepresentative;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.ProbeRepresentative;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.SetBasedInstrumentationRule;

/**
 * Subsumes implementations of builders for {@link SetBasedInstrumentationRule}
 * s.
 * 
 * @author Henning Schulz
 *
 * @param
 * 			<P>
 *            the probe type
 */
public abstract class AbstractSetBasedRuleBuilder<P>
		implements ProbeAndCalculatorBuilder<P>, CalculatorRepBuilderFrom<P>, CalculatorRepBuilderTo<P> {

	private static final Logger LOGGER = Logger.getLogger(AbstractSetBasedRuleBuilder.class);

	private final Map<String, ProbeRepresentative<P>> probesPerProperty = new HashMap<>();
	private final Set<CalculatorRepresentative<P, P>> calculators = new HashSet<>();

	private CalculatorRepresentative<P, P> currentCalculator;

	protected abstract ProbeRepresentative<P> createProbeRepresentative(String measuredProperty);

	@Override
	public ProbeAndCalculatorBuilder<P> addProbe(String measuredProperty) {
		ProbeRepresentative<P> probe = createProbeRepresentative(measuredProperty);
		probesPerProperty.put(measuredProperty, probe);
		return this;
	}

	@Override
	public CalculatorRepBuilderFrom<P> addCalculator(String metric) {
		currentCalculator = new CalculatorRepresentative<>(metric);
		return this;
	}

	@Override
	public CalculatorRepBuilderTo<P> from(String fromProperty) {
		ProbeRepresentative<P> fromProbe = probesPerProperty.get(fromProperty);

		if (fromProbe == null) {
			fromProbe = createProbeRepresentative(fromProperty);
			probesPerProperty.put(fromProperty, fromProbe);
			LOGGER.warn(String.format("No probe for property \"%s\" has been specified, yet. Creating a new one...",
					fromProperty));
		}

		currentCalculator.setFromProbe(fromProbe);

		return this;
	}

	@Override
	public ProbeAndCalculatorBuilder<P> to(String toProperty) {
		ProbeRepresentative<P> toProbe = probesPerProperty.get(toProperty);

		if (toProbe == null) {
			toProbe = createProbeRepresentative(toProperty);
			probesPerProperty.put(toProperty, toProbe);
			LOGGER.warn(String.format("No probe for property \"%s\" has been specified, yet. Creating a new one...",
					toProperty));
		}

		currentCalculator.setToProbe(toProbe);
		calculators.add(currentCalculator);
		currentCalculator = null;

		return this;
	}

	protected Map<String, ProbeRepresentative<P>> getProbesPerProperty() {
		return probesPerProperty;
	}

	protected Set<ProbeRepresentative<P>> getProbes() {
		return probesPerProperty.entrySet().stream().map(entry -> entry.getValue()).collect(Collectors.toSet());
	}

	protected Set<CalculatorRepresentative<P, P>> getCalculators() {
		return calculators;
	}

}
