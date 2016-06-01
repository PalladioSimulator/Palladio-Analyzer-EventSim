package edu.kit.ipd.sdq.eventsim.resources;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

import de.uka.ipd.sdq.scheduler.ISchedulingFactory;
import de.uka.ipd.sdq.scheduler.SchedulerModel;
import edu.kit.ipd.sdq.eventsim.api.IActiveResource;
import edu.kit.ipd.sdq.eventsim.api.IPassiveResource;
import edu.kit.ipd.sdq.eventsim.middleware.simulation.SimulationModel;

public class EventSimResourceModule extends AbstractModule {
	
	@Override
	protected void configure() {
		// bind interfaces of provided services to their implementation
		bind(IActiveResource.class).to(EventSimActiveResourceModel.class);
		bind(IPassiveResource.class).to(EventSimPassiveResourceModel.class);
		
		bind(SchedulerModel.class).to(SimulationModel.class).in(Singleton.class);
		bind(ISchedulingFactory.class).to(InjectableSchedulingFactory.class).in(Singleton.class);
	}

}
