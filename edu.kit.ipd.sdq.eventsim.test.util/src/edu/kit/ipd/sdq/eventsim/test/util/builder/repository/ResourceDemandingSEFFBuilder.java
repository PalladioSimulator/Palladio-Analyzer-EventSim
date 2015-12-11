package edu.kit.ipd.sdq.eventsim.test.util.builder.repository;

import java.util.UUID;

import org.palladiosimulator.pcm.repository.OperationSignature;
import org.palladiosimulator.pcm.repository.RepositoryFactory;
import org.palladiosimulator.pcm.seff.AbstractAction;
import org.palladiosimulator.pcm.seff.ResourceDemandingSEFF;
import org.palladiosimulator.pcm.seff.SeffFactory;
import org.palladiosimulator.pcm.seff.StartAction;
import org.palladiosimulator.pcm.seff.StopAction;

import edu.kit.ipd.sdq.eventsim.test.util.builder.BuildingContext;

public class ResourceDemandingSEFFBuilder {

	private BuildingContext context;
	
	private AbstractAction lastAction;

	private ResourceDemandingSEFF seff;

	/**
	 * @param signature
	 *            the signature of the service described by the SEFF under creation.
	 */
	public ResourceDemandingSEFFBuilder(OperationSignature signature, BuildingContext context) {
		this.context = context;
		seff = SeffFactory.eINSTANCE.createResourceDemandingSEFF();
		seff.setDescribedService__SEFF(signature);
		context.add(seff);
	}

	public ResourceDemandingSEFFBuilder start(String name) {
		StartAction start = SeffFactory.eINSTANCE.createStartAction();
		start.setEntityName(name);
		enchain(start);
		return this;
	}

	public ResourceDemandingSEFFBuilder start() {
		return start(randomName());
	}

	public ResourceDemandingSEFFBuilder stop(String name) {
		StopAction stop = SeffFactory.eINSTANCE.createStopAction();
		stop.setEntityName(name);
		enchain(stop);
		return this;
	}

	public ResourceDemandingSEFFBuilder stop() {
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
