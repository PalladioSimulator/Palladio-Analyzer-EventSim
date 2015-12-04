package edu.kit.ipd.sdq.eventsim.workload.tests.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.palladiosimulator.pcm.usagemodel.AbstractUserAction;
import org.palladiosimulator.pcm.usagemodel.UsageModel;
import org.palladiosimulator.pcm.usagemodel.UsagemodelPackage;

import edu.kit.ipd.sdq.eventsim.measurement.Measurement;
import edu.kit.ipd.sdq.eventsim.measurement.MeasurementFacade;

public class Tracer {

	private final MeasurementFacade<?> measurementFacade;

	private List<TracedMeasurement> trace;

	public Tracer(MeasurementFacade<?> measurementFacade) {
		this.measurementFacade = measurementFacade;
		this.trace = new ArrayList<>();
	}

	public void instrumentAllUserActions(UsageModel model) {
		// recursively collect all EObjects contained in the UsageModel
		List<EObject> allEObjects = new ArrayList<>();
		model.eAllContents().forEachRemaining(allEObjects::add);

		// filter user actions
		Collection<AbstractUserAction> actions = EcoreUtil.getObjectsByType(allEObjects,
				UsagemodelPackage.eINSTANCE.getAbstractUserAction());

		// instrument each user action
		for (AbstractUserAction a : actions) {
			measurementFacade.createProbe(a, "before").forEachMeasurement(this::processMeasurement);
		}
	}

	private void processMeasurement(Measurement<?, ?> measurement) {
		trace.add(new TracedMeasurement(measurement));
	}

	@Override
	public String toString() {
		return "Tracer [trace=" + trace + "]";
	}

	public TracedMeasurement firstInvocationOf(String name) {
		for (TracedMeasurement m : trace) {
			AbstractUserAction action = (AbstractUserAction) m.getMeasurement().getWhere().getElement();
			if (action.getEntityName().equals(name)) {
				return m;
			}
		}
		throw new RuntimeException("Tracer did not encounter an element named " + name);
	}
	
	public int size() {
		return trace.size();
	}

	public static class TracedMeasurement {

		private final static AtomicInteger sequenceGenerator = new AtomicInteger();

		private final Measurement<?, ?> wrappedMeasurement;

		private final int sequenceNumber;

		private final long nanoTime;

		public TracedMeasurement(Measurement<?, ?> wrappedMeasurement) {
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

		public Measurement<?, ?> getMeasurement() {
			return wrappedMeasurement;
		}

		@Override
		public String toString() {
			return "TracedMeasurement [sequenceNumber=" + sequenceNumber + ", nanoTime=" + nanoTime
					+ ", wrappedMeasurement=" + wrappedMeasurement + "]";
		}

	}

}
