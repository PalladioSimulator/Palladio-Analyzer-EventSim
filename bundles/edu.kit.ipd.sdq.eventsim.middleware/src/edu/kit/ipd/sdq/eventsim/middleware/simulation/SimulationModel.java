package edu.kit.ipd.sdq.eventsim.middleware.simulation;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import de.uka.ipd.sdq.scheduler.SchedulerModel;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimEngineFactory;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationControl;
import de.uka.ipd.sdq.simulation.abstractsimengine.ISimulationModel;
import edu.kit.ipd.sdq.eventsim.api.ISimulationConfiguration;
import edu.kit.ipd.sdq.eventsim.api.ISimulationMiddleware;
import edu.kit.ipd.sdq.eventsim.api.events.SimulationStartEvent;
import edu.kit.ipd.sdq.eventsim.api.events.SimulationStopEvent;

/**
 * The simulation model is the core of an abstract simulation engine based
 * simulation. It controls the simulation process and basically represents the
 * central simulation timeline. For more information see {@code ISimulationModel}
 * 
 * @author Christoph Föhrdes
 */
@Singleton
public class SimulationModel extends SchedulerModel implements ISimulationModel {

	private ISimulationControl control;
	private ISimEngineFactory factory;
	
	@Inject
	private ISimulationMiddleware middleware;

	@Override
	public ISimulationControl getSimulationControl() {
		if (this.control == null) {
			this.control = this.factory.createSimulationControl();
		}
		return this.control;
	}

	@Override
	public void setSimulationControl(ISimulationControl control) {
		this.control = control;
	}

	@Override
	public void setSimulationEngineFactory(ISimEngineFactory factory) {
		this.factory = factory;
	}

	@Override
	public ISimEngineFactory getSimEngineFactory() {
		return this.factory;
	}

	@Override
	public ISimulationConfiguration getConfiguration() {
		return this.middleware.getSimulationConfiguration();
	}

	@Override
	public void init() {	
		this.middleware.triggerEvent(new SimulationStartEvent());
	}

	@Override
	public void finalise() {
		// after the simulation has stopped we trigger the finalize event for cleanup tasks
		this.middleware.triggerEvent(new SimulationStopEvent());
	}

}
