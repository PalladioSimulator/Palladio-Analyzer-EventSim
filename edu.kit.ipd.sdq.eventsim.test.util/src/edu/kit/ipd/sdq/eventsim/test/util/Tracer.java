package edu.kit.ipd.sdq.eventsim.test.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.palladiosimulator.pcm.core.entity.Entity;
import org.palladiosimulator.pcm.repository.Repository;
import org.palladiosimulator.pcm.seff.AbstractAction;
import org.palladiosimulator.pcm.seff.SeffPackage;
import org.palladiosimulator.pcm.usagemodel.AbstractUserAction;
import org.palladiosimulator.pcm.usagemodel.UsageModel;
import org.palladiosimulator.pcm.usagemodel.UsagemodelPackage;

import edu.kit.ipd.sdq.eventsim.launch.SimulationManager;
import edu.kit.ipd.sdq.eventsim.measurement.Measurement;
import edu.kit.ipd.sdq.eventsim.measurement.MeasurementFacade;
import edu.kit.ipd.sdq.eventsim.system.EventSimSystemModel;
import edu.kit.ipd.sdq.eventsim.workload.EventSimWorkloadModel;

public class Tracer {

	private final SimulationManager manager;

	private final List<TracedMeasurement> trace;

	public Tracer(SimulationManager manager) {
		this.manager = manager;
		this.trace = new ArrayList<>();
	}

	public Tracer instrumentUserActions(UsageModel model) {
		Collection<AbstractUserAction> actions = collectAllEObjectsOfType(model,
				UsagemodelPackage.eINSTANCE.getAbstractUserAction());

		// instrument each user action
		MeasurementFacade<?> measurementFacade = ((EventSimWorkloadModel) manager.getWorkload()).getMeasurementFacade();
		for (AbstractUserAction a : actions) {
			measurementFacade.createProbe(a, "before").forEachMeasurement(this::processMeasurement);
		}

		return this;
	}

	public Tracer instrumentSeffActions(Repository model) {
		Collection<AbstractAction> actions = collectAllEObjectsOfType(model, SeffPackage.eINSTANCE.getAbstractAction());

		// instrument each user action
		MeasurementFacade<?> measurementFacade = ((EventSimSystemModel) manager.getSystem()).getMeasurementFacade();
		for (AbstractAction a : actions) {
			measurementFacade.createProbe(a, "before").forEachMeasurement(this::processMeasurement);
		}

		return this;
	}

	private static <T> Collection<T> collectAllEObjectsOfType(EObject root, EClass type) {
		// recursively collect all EObjects contained in the UsageModel
		List<EObject> allEObjects = new ArrayList<>();
		root.eAllContents().forEachRemaining(allEObjects::add);

		// filter user actions
		Collection<T> actions = EcoreUtil.getObjectsByType(allEObjects, type);
		return actions;
	}

	private void processMeasurement(Measurement<?> measurement) {
		trace.add(new TracedMeasurement(measurement));
	}

	@Override
	public String toString() {
		return "Tracer [trace=" + trace + "]";
	}

	public TracedMeasurement firstInvocationOf(String name) {
		for (TracedMeasurement m : trace) {
			Entity action = (Entity) m.getMeasurement().getWhere().getElement();
			if (action.getEntityName().equals(name)) {
				return m;
			}
		}
		throw new RuntimeException("Tracer did not encounter an element named " + name);
	}

	public int invocationCount(String name) {
		int count = 0;
		for (TracedMeasurement m : trace) {
			AbstractUserAction action = (AbstractUserAction) m.getMeasurement().getWhere().getElement();
			if (action.getEntityName().equals(name)) {
				count++;
			}
		}
		return count;
	}

	public int size() {
		return trace.size();
	}

	public static class TracedMeasurement {

		private final static AtomicInteger sequenceGenerator = new AtomicInteger();

		private final Measurement<?> wrappedMeasurement;

		private final int sequenceNumber;

		private final long nanoTime;

		public TracedMeasurement(Measurement<?> wrappedMeasurement) {
			this.sequenceNumber = sequenceGenerator.getAndIncrement();
			this.wrappedMeasurement = wrappedMeasurement;
			this.nanoTime = System.nanoTime();
		}

		public long getNanoTime() {
			return nanoTime;
		}

		public int getSequenceNumber() {
			return sequenceNumber;
		}

		public Measurement<?> getMeasurement() {
			return wrappedMeasurement;
		}

		@Override
		public String toString() {
			return "TracedMeasurement [sequenceNumber=" + sequenceNumber + ", nanoTime=" + nanoTime
					+ ", wrappedMeasurement=" + wrappedMeasurement + "]";
		}

	}

}
