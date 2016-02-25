package edu.kit.ipd.sdq.eventsim.instrumentation.specification.entities;

import edu.kit.ipd.sdq.eventsim.instrumentation.description.action.ActionRule;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.InstrumentationRule;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.SetBasedInstrumentationRule;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.resource.ActiveResourceRep;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.resource.PassiveResourceRep;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.resource.ResourceRule;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.useraction.UserActionRule;
import edu.kit.ipd.sdq.eventsim.resources.entities.SimActiveResource;
import edu.kit.ipd.sdq.eventsim.resources.entities.SimPassiveResource;

/**
 * Wraps an {@link InstrumentationRule} and adds the types for which probes are
 * created (both in the instrumentation description and in the EventSim
 * simulation engine)
 * 
 * @author Henning Schulz
 *
 * @param
 * 			<P>
 *            the type of the model probe
 * @param <F>
 *            the type of the model calculator from probe
 * @param <T>
 *            the type of the model calculator to probe
 */
public class TypedInstrumentationRule<P, F, T> { // TODO remove type params!?

	private final InstrumentationRule decorated;

	private final Class<?> probedType;
	private final Class<?> calculatorFromType;
	private final Class<?> calculatorToType;
	private final Class<P> modelProbedType;
	private final Class<F> modelCalculatorFromType;
	private final Class<T> modelCalculatorToType;

	private final boolean useCalculatorsOnSingleEntity;

	public TypedInstrumentationRule(InstrumentationRule decorated, Class<?> probedType, Class<?> calculatorFromType,
			Class<?> calculatorToType, Class<P> modelProbedType, Class<F> modelCalculatorFromType,
			Class<T> modelCalculatorToType, boolean useCalculatorsOnSingleEntity) {
		this.decorated = decorated;
		this.probedType = probedType;
		this.calculatorFromType = calculatorFromType;
		this.calculatorToType = calculatorToType;
		this.modelProbedType = modelProbedType;
		this.modelCalculatorFromType = modelCalculatorFromType;
		this.modelCalculatorToType = modelCalculatorToType;
		this.useCalculatorsOnSingleEntity = useCalculatorsOnSingleEntity;
	}

	public static <P> TypedInstrumentationRule<P, P, P> fromSetBasedRule(SetBasedInstrumentationRule<P, ?> rule) {
		if (rule instanceof ActionRule || rule instanceof UserActionRule) {
			Class<?> probedType;
			if (rule instanceof ActionRule)
				probedType = ((ActionRule) rule).getActionType();
			else
				probedType = ((UserActionRule) rule).getUserActionType();
			Class<P> typedProbeType = (Class<P>) probedType;
			return new TypedInstrumentationRule<>(rule, typedProbeType, typedProbeType, typedProbeType, typedProbeType,
					typedProbeType, typedProbeType, false);
		} else if (rule instanceof ResourceRule) {
			ResourceRule<?> resourceRule = (ResourceRule<?>) rule;
			Class<?> probedType;
			Class<P> modelProbedType;
			if (resourceRule.getResourceSet().getResourceType().equals(ActiveResourceRep.class)) {
				probedType = SimActiveResource.class;
				modelProbedType = (Class<P>) ActiveResourceRep.class;
			} else {
				probedType = SimPassiveResource.class;
				modelProbedType = (Class<P>) PassiveResourceRep.class;
			}
			return new TypedInstrumentationRule<>(rule, probedType, probedType, probedType, modelProbedType,
					modelProbedType, modelProbedType, true);
		} else {
			return null;
		}
	}

	public String getName() {
		return decorated.getName();
	}

	public void setName(String name) {
		decorated.setName(name);
	}

	public Class<?> getProbedType() {
		return probedType;
	}

	public Class<?> getCalculatorFromType() {
		return calculatorFromType;
	}

	public Class<?> getCalculatorToType() {
		return calculatorToType;
	}

	public Class<P> getModelProbedType() {
		return modelProbedType;
	}

	public Class<F> getModelCalculatorFromType() {
		return modelCalculatorFromType;
	}

	public Class<T> getModelCalculatorToType() {
		return modelCalculatorToType;
	}

	public InstrumentationRule getDecorated() {
		return decorated;
	}

	public boolean useCalculatorsOnSingleEntity() {
		return useCalculatorsOnSingleEntity;
	}

}
