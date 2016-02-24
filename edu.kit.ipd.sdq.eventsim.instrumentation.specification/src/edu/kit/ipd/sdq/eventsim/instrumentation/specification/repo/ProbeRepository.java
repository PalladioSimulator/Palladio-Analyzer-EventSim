package edu.kit.ipd.sdq.eventsim.instrumentation.specification.repo;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.ProbeRepresentative;
import edu.kit.ipd.sdq.eventsim.instrumentation.specification.entities.TypedInstrumentationRule;
import edu.kit.ipd.sdq.eventsim.instrumentation.utils.ClassRepository;
import edu.kit.ipd.sdq.eventsim.measurement.annotation.Probe;

public class ProbeRepository {

	private static List<Probe> allProbes = new ArrayList<>();

	public static void init() {
		for (Class<?> probeClass : ClassRepository.filterAllLoadedClasses(ProbeRepository::hasProbeAnnotation)) {
			allProbes.add(probeClass.getAnnotation(Probe.class));
		}
	}

	public static <P> List<ProbeRepresentative<P>> getProbesFor(TypedInstrumentationRule<P, ?, ?> rule) {
		return getProbesFor(rule.getProbedType(), rule.getModelProbedType());
	}

	public static <F> List<ProbeRepresentative<F>> getCalculatorFromProbesFor(TypedInstrumentationRule<?, F, ?> rule) {
		return getProbesFor(rule.getCalculatorFromType(), rule.getModelCalculatorFromType());
	}

	public static <T> List<ProbeRepresentative<T>> getCalculatorToProbesFor(TypedInstrumentationRule<?, ?, T> rule) {
		return getProbesFor(rule.getCalculatorToType(), rule.getModelCalculatorToType());
	}

	public static <P> List<ProbeRepresentative<P>> getProbesFor(Class<?> probedType, Class<P> modelProbedType) {
		// filters probes capable to work with the passed type out of all
		// existing probe annotations and creates a ProbeRepresentative for each
		// of them
		return allProbes.stream().filter(p -> p.type().isAssignableFrom(probedType))
				.map(p -> new ProbeRepresentative<>(p.property(), modelProbedType)).collect(Collectors.toList());
	}

	private static boolean hasProbeAnnotation(Class<?> clazz) {
		if (clazz == null) {
			return false;
		}

		Probe a = clazz.getAnnotation(Probe.class);
		return a != null;
	}

}
