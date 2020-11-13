package edu.kit.ipd.sdq.eventsim.resources;

import javax.inject.Singleton;

import com.google.inject.Inject;

import de.uka.ipd.sdq.scheduler.SchedulerModel;
import de.uka.ipd.sdq.scheduler.factory.SchedulingFactory;
import de.uka.ipd.sdq.scheduler.resources.active.IResourceTableManager;

@Singleton
public class InjectableSchedulingFactory extends SchedulingFactory {

    @Inject
    public InjectableSchedulingFactory(SchedulerModel model, IResourceTableManager resourceTableManager) {
        super(model, resourceTableManager);
    }

}
