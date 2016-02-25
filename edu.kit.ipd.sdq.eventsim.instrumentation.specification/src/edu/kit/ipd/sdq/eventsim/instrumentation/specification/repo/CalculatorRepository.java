package edu.kit.ipd.sdq.eventsim.instrumentation.specification.repo;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.CalculatorRepresentative;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.ProbeRepresentative;
import edu.kit.ipd.sdq.eventsim.instrumentation.specification.entities.TypedInstrumentationRule;
import edu.kit.ipd.sdq.eventsim.instrumentation.utils.ClassRepository;
import edu.kit.ipd.sdq.eventsim.measurement.annotation.Calculator;
import edu.kit.ipd.sdq.eventsim.measurement.annotation.ProbePair;

public class CalculatorRepository {

	private static List<Calculator> allCalculators = new ArrayList<>();

	public static void init() {
		for (Class<?> calculatorClass : ClassRepository
				.filterAllLoadedClasses(CalculatorRepository::hasCalculatorAnnotation)) {
			allCalculators.add(calculatorClass.getAnnotation(Calculator.class));
		}
	}

	public static List<CalculatorRepresentative> getCalculatorsFor(
			TypedInstrumentationRule<?, ?, ?> rule) {
		List<Calculator> relevantCalculators;

		if (rule.useCalculatorsOnSingleEntity()) {
			// retrieve all calculators that are applicable to the type
			// specified by the rule
			relevantCalculators = allCalculators.stream().filter(c -> c.type().isAssignableFrom(rule.getProbedType()))
					.collect(Collectors.toList());
		} else {
			// retrieve all calculators that are applicable to the from- and
			// to-type specified by the rule
			relevantCalculators = allCalculators.stream()
					.filter(c -> c.fromType().isAssignableFrom(rule.getCalculatorFromType())
							&& c.toType().isAssignableFrom(rule.getCalculatorToType()))
					.collect(Collectors.toList());
		}

		List<CalculatorRepresentative> calculators = new ArrayList<>();

		for (Calculator c : relevantCalculators) {
			List<ProbeRepresentative> fromProbes = ProbeRepository.getCalculatorFromProbesFor(rule);
			List<ProbeRepresentative> toProbes = ProbeRepository.getCalculatorToProbesFor(rule);

			ProbePair[] intendedProbes = c.intendedProbes();

			// Compute all possible and intended combinations of calculators and
			// probes
			for (ProbeRepresentative from : fromProbes) {
				for (ProbeRepresentative to : toProbes) {
					if (!from.equals(to)) {
						for (ProbePair pair : intendedProbes) {
							if (pair.from().equals(from.getMeasuredProperty())
									&& pair.to().equals(to.getMeasuredProperty()))
								calculators.add(new CalculatorRepresentative(c.metric(), from, to));
						}
					}
				}
			}
		}

		return calculators;
	}

	private static boolean hasCalculatorAnnotation(Class<?> clazz) {
		if (clazz == null) {
			return false;
		}

		Calculator a = clazz.getAnnotation(Calculator.class);
		return a != null;
	}

}
