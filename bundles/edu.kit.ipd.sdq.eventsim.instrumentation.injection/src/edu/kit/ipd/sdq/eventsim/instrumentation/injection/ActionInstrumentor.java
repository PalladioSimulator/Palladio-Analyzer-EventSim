package edu.kit.ipd.sdq.eventsim.instrumentation.injection;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.osgi.framework.Bundle;
import org.palladiosimulator.pcm.seff.AbstractAction;

import edu.kit.ipd.sdq.eventsim.api.PCMModel;
import edu.kit.ipd.sdq.eventsim.command.PCMModelCommandExecutor;
import edu.kit.ipd.sdq.eventsim.command.action.ActionContext;
import edu.kit.ipd.sdq.eventsim.command.action.FindAllActionsByType;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.action.ActionRepresentative;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.action.ActionRule;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.CalculatorRepresentative;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.InstrumentationDescription;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.ProbeRepresentative;
import edu.kit.ipd.sdq.eventsim.measurement.MeasurementFacade;
import edu.kit.ipd.sdq.eventsim.measurement.MeasurementStorage;
import edu.kit.ipd.sdq.eventsim.measurement.Pair;
import edu.kit.ipd.sdq.eventsim.measurement.ProbeConfiguration;
import edu.kit.ipd.sdq.eventsim.measurement.annotation.Calculator;
import edu.kit.ipd.sdq.eventsim.measurement.calculator.BinaryCalculator;
import edu.kit.ipd.sdq.eventsim.measurement.osgi.CalculatorFactory;
import edu.kit.ipd.sdq.eventsim.measurement.probe.IProbe;

/**
 * An instrumentor for actions. For specification of an action, it deals with
 * {@link ActionRepresentative}s.
 * 
 * @author Henning Schulz
 *
 * @param <C>
 *            the type of the {@code ProbeConfiguration}
 */
public class ActionInstrumentor<C extends ProbeConfiguration> implements Instrumentor<ActionRepresentative, C> {

	private final MeasurementStorage measurementStorage;
	private final CalculatorFactory calculatorFactory;
	private final InstrumentationDescription description;
	private final MeasurementFacade<C> measurementFacade;
	private final PCMModelCommandExecutor executor;

	public ActionInstrumentor(MeasurementStorage measurementStorage, Bundle bundle,
			InstrumentationDescription description, PCMModel pcm, MeasurementFacade<C> measurementFacade) {
		this.measurementStorage = measurementStorage;
		this.calculatorFactory = new CalculatorFactory(bundle);
		this.description = description;
		this.measurementFacade = measurementFacade;
		this.executor = new PCMModelCommandExecutor(pcm);
	}

	@Override
	public void instrumentAll() {
		for (ActionRule rule : description.getActionRules()) {
			for (ActionContext<?> action : executor.execute(new FindAllActionsByType<>(rule.getActionType()))) {
				instrumentActionWithRule(new ActionRepresentative(action.getAction(),
						action.getAllocationContext(), action.getAssemblyContext()), rule);
			}
		}
	}

	@Override
	public void instrument(ActionRepresentative action) {
		for (ActionRule rule : description.getActionRules()) {
			instrumentActionWithRule(action, rule);
		}
	}

	private void instrumentActionWithRule(ActionRepresentative action,
			ActionRule rule) {
		if (!rule.affects(action)) {
			return;
		}

		Map<ProbeRepresentative, IProbe<AbstractAction>> createdProbes = new HashMap<>();

		for (ProbeRepresentative probeRep : rule.getProbes()) {
			// Create an EventSim probe corresponding to the probe
			// representative of the rule
			IProbe<AbstractAction> probe = measurementFacade.createProbe(action.getRepresentedAction(),
					probeRep.getMeasuredProperty(), action.getAssemblyContext());
			createdProbes.put(probeRep, probe);
		}

		for (CalculatorRepresentative calculatorRep : rule.getCalculators()) {
			// Instantiate the calculator
			BinaryCalculator<AbstractAction, AbstractAction> calculator = (BinaryCalculator<AbstractAction, AbstractAction>) calculatorFactory
					.create(calculatorRep.getMetric(), calculatorRep.getFromProbe().getProbedType(),
							calculatorRep.getToProbe().getProbedType());

			// Set the from- and to-probe			
			measurementFacade.createCalculator(calculator)
					.from(action.getRepresentedAction(), calculatorRep.getFromProbe().getMeasuredProperty(),
							action.getAssemblyContext())
					.to(action.getRepresentedAction(), calculatorRep.getToProbe().getMeasuredProperty(),
							action.getAssemblyContext());

			Calculator annotation = calculator.getClass().getAnnotation(Calculator.class);

			if (annotation == null) {
				// TODO: Error
				return;
			}

			if (annotation.type().isAssignableFrom(Pair.class)) {
				if (!(action.getRepresentedAction().getClass().isAssignableFrom(annotation.fromType())
						&& action.getRepresentedAction().getClass().isAssignableFrom(annotation.toType()))) {
					// TODO: error!
				}

				calculator.forEachMeasurement(m -> measurementStorage.put(m));
			} else {
				calculator.forEachMeasurement(m -> measurementStorage.put(m));
			}

			// probes used by a calculator should not produce measurements by
			// itselves
			createdProbes.remove(calculatorRep.getFromProbe());
			createdProbes.remove(calculatorRep.getToProbe());
		}

		for (Entry<ProbeRepresentative, IProbe<AbstractAction>> probeEntry : createdProbes.entrySet()) {
			probeEntry.getValue().forEachMeasurement(m -> measurementStorage.put(m));
		}
	}

}
