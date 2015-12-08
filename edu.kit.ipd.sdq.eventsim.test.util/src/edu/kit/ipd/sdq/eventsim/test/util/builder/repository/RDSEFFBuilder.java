package edu.kit.ipd.sdq.eventsim.test.util.builder.repository;

import java.util.UUID;

import org.palladiosimulator.pcm.repository.OperationSignature;
import org.palladiosimulator.pcm.seff.AbstractAction;
import org.palladiosimulator.pcm.seff.ResourceDemandingSEFF;
import org.palladiosimulator.pcm.seff.SeffFactory;
import org.palladiosimulator.pcm.seff.StartAction;
import org.palladiosimulator.pcm.seff.StopAction;

public class RDSEFFBuilder {

	private AbstractAction lastAction;

	private ResourceDemandingSEFF seff;

	/**
	 * @param signature
	 *            the signature of the service described by the SEFF under creation.
	 */
	public RDSEFFBuilder(OperationSignature signature) {
		seff = SeffFactory.eINSTANCE.createResourceDemandingSEFF();
		seff.setDescribedService__SEFF(signature);
	}

	public RDSEFFBuilder start(String name) {
		StartAction start = SeffFactory.eINSTANCE.createStartAction();
		start.setEntityName(name);
		enchain(start);
		return this;
	}

	public RDSEFFBuilder start() {
		return start(randomName());
	}

	public RDSEFFBuilder stop(String name) {
		StopAction stop = SeffFactory.eINSTANCE.createStopAction();
		stop.setEntityName(name);
		enchain(stop);
		return this;
	}

	public RDSEFFBuilder stop() {
		return stop(randomName());
	}

	public ResourceDemandingSEFF buildSeff() {
		return seff;
	}

	private void enchain(AbstractAction action) {
		action.setResourceDemandingBehaviour_AbstractAction(seff);
		action.setPredecessor_AbstractAction(lastAction);
		lastAction = action;
	}

	private String randomName() {
		return UUID.randomUUID().toString();
	}

}
