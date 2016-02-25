package edu.kit.ipd.sdq.eventsim.instrumentation.injection;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.osgi.framework.Bundle;
import org.palladiosimulator.pcm.usagemodel.AbstractUserAction;

import edu.kit.ipd.sdq.eventsim.api.PCMModel;
import edu.kit.ipd.sdq.eventsim.command.PCMModelCommandExecutor;
import edu.kit.ipd.sdq.eventsim.command.useraction.FindAllUserActionsByType;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.CalculatorRepresentative;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.InstrumentationDescription;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.ProbeRepresentative;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.useraction.UserActionRepresentative;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.useraction.UserActionRule;
import edu.kit.ipd.sdq.eventsim.measurement.MeasurementFacade;
import edu.kit.ipd.sdq.eventsim.measurement.MeasurementStorage;
import edu.kit.ipd.sdq.eventsim.measurement.Pair;
import edu.kit.ipd.sdq.eventsim.measurement.ProbeConfiguration;
import edu.kit.ipd.sdq.eventsim.measurement.annotation.Calculator;
import edu.kit.ipd.sdq.eventsim.measurement.calculator.BinaryCalculator;
import edu.kit.ipd.sdq.eventsim.measurement.osgi.BundleProbeLocator;
import edu.kit.ipd.sdq.eventsim.measurement.osgi.CalculatorFactory;
import edu.kit.ipd.sdq.eventsim.measurement.probe.IProbe;

/**
 * An instrumentor for user actions. For specification of a user action, it
 * deals with {@link AbstractUserAction}s.
 * 
 * @author Henning Schulz
 *
 * @param <C>
 *            the type of the {@code ProbeConfiguration}
 */
public class UserActionInstrumentor<C extends ProbeConfiguration>
		implements Instrumentor<UserActionRepresentative, C> {

	private final MeasurementStorage measurementStorage;
	private final CalculatorFactory calculatorFactory;
	private final InstrumentationDescription description;
	private final MeasurementFacade<C> measurementFacade;
	private final PCMModelCommandExecutor executor;

	public UserActionInstrumentor(MeasurementStorage measurementStorage, Bundle bundle,
			InstrumentationDescription description, PCMModel pcm, C configuration) {
		this.measurementStorage = measurementStorage;
		this.calculatorFactory = new CalculatorFactory(bundle);
		this.description = description;
		this.measurementFacade = new MeasurementFacade<C>(configuration, new BundleProbeLocator<>(bundle));
		this.executor = new PCMModelCommandExecutor(pcm);
	}

	@Override
	public void instrumentAll() {
		for (UserActionRule rule : description.getUserActionRules()) {
			for (AbstractUserAction action : executor
					.execute(new FindAllUserActionsByType<>(rule.getUserActionType()))) {
				instrumentActionWithRule(new UserActionRepresentative(action), rule);
			}
		}
	}

	@Override
	public void instrument(UserActionRepresentative action) {
		for (UserActionRule rule : description.getUserActionRules()) {
			instrumentActionWithRule(action, rule);
		}
	}

	private void instrumentActionWithRule(UserActionRepresentative action,
			UserActionRule rule) {
		if (!rule.affects(action)) {
			return;
		}

		Map<ProbeRepresentative, IProbe<AbstractUserAction>> createdProbes = new HashMap<>();

		for (ProbeRepresentative probeRep : rule.getProbes()) {
			// Create an EventSim probe corresponding to the probe
			// representative of the rule
			IProbe<AbstractUserAction> probe = measurementFacade.createProbe(action.getRepresentedUserAction(),
					probeRep.getMeasuredProperty());
			createdProbes.put(probeRep, probe);
		}

		for (CalculatorRepresentative calculatorRep : rule.getCalculators()) {
			// Instantiate the calculator
			BinaryCalculator<AbstractUserAction, AbstractUserAction> calculator = (BinaryCalculator<AbstractUserAction, AbstractUserAction>) calculatorFactory.create(
					calculatorRep.getMetric(), calculatorRep.getFromProbe().getProbedType(),
					calculatorRep.getToProbe().getProbedType());

			// Set the from- and to-probe
			measurementFacade.createCalculator(calculator)
					.from(action.getRepresentedUserAction(), calculatorRep.getFromProbe().getMeasuredProperty())
					.to(action.getRepresentedUserAction(), calculatorRep.getFromProbe().getMeasuredProperty());

			Calculator annotation = calculator.getClass().getAnnotation(Calculator.class);

			if (annotation == null) {
				// TODO: Error
				return;
			}

			if (annotation.type().isAssignableFrom(Pair.class)) {
				if (!(action.getRepresentedUserAction().getClass().isAssignableFrom(annotation.fromType())
						&& action.getRepresentedUserAction().getClass().isAssignableFrom(annotation.toType()))) {
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

		for (Entry<ProbeRepresentative, IProbe<AbstractUserAction>> probeEntry : createdProbes.entrySet()) {
			probeEntry.getValue().forEachMeasurement(m -> measurementStorage.put(m));
		}
	}

}
