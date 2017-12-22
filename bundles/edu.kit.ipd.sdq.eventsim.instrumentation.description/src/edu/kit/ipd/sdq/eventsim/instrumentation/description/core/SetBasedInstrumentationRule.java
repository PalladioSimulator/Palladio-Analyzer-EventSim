package edu.kit.ipd.sdq.eventsim.instrumentation.description.core;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

/**
 * Subsumes common implementations of instrumentation rules describing
 * instrumentation of a set of entities by both a set of probes and a set of
 * calculators. The set is specified indirectly by all entities of a specific
 * type which are not excluded by one of the restrictions.
 * 
 * @author Henning Schulz
 *
 * @param
 * 			<P>
 *            the proped type (the type for which probes are created, e.g.:
 *            AbstractAction)
 * @param <I>
 *            the instrumentable type (the type of entities that are used in
 *            instrumentable set, restrictions, etc., e.g.:
 *            ActionRepresentative)
 * 
 * @see InstrumentableRestriction
 * @see InstrumentableSet
 */
public abstract class SetBasedInstrumentationRule<P, I extends Instrumentable> implements InstrumentationRule {

	private Set<ProbeRepresentative> probes = new HashSet<>();

	private Set<CalculatorRepresentative> calculators = new HashSet<>();

	private String name;

	@XmlElementWrapper(name = "probes")
	@XmlElement(name = "probe")
	public Set<ProbeRepresentative> getProbes() {
		return probes;
	}

	public void setProbes(Set<ProbeRepresentative> probes) {
		this.probes = probes;
	}

	public void addProbe(ProbeRepresentative probe) {
		probes.add(probe);
	}

	public void removeProbe(ProbeRepresentative probe) {
		probes.remove(probe);
	}

	@XmlElementWrapper(name = "calculators")
	@XmlElement(name = "calculator")
	public Set<CalculatorRepresentative> getCalculators() {
		return calculators;
	}

	public void setCalculators(Set<CalculatorRepresentative> calculators) {
		this.calculators = calculators;
	}

	public void addCalculator(CalculatorRepresentative calculator) {
		calculators.add(calculator);
	}

	public void removeCalculator(CalculatorRepresentative calculator) {
		calculators.remove(calculator);
	}

	@XmlAttribute(name = "name")
	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	public abstract Class<? extends P> getProbedType();

	public abstract Class<I> getInstrumentableType();

	public abstract void addRestriction(InstrumentableRestriction<I> restriction);

	public abstract void removeRestriction(InstrumentableRestriction<I> restriction);

	public abstract List<InstrumentableRestriction<I>> getRestrictions();

}
