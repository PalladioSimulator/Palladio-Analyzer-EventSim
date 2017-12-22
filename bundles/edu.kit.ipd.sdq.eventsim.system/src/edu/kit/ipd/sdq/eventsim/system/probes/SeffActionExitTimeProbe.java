package edu.kit.ipd.sdq.eventsim.system.probes;

import org.palladiosimulator.pcm.seff.AbstractAction;

import edu.kit.ipd.sdq.eventsim.interpreter.listener.ITraversalListener;
import edu.kit.ipd.sdq.eventsim.measurement.Measurement;
import edu.kit.ipd.sdq.eventsim.measurement.MeasuringPoint;
import edu.kit.ipd.sdq.eventsim.measurement.annotation.Probe;
import edu.kit.ipd.sdq.eventsim.measurement.probe.AbstractProbe;
import edu.kit.ipd.sdq.eventsim.system.SystemMeasurementConfiguration;
import edu.kit.ipd.sdq.eventsim.system.entities.Request;

@Probe(type = AbstractAction.class, property = "after")
public class SeffActionExitTimeProbe<E extends AbstractAction>
        extends AbstractProbe<E, SystemMeasurementConfiguration> {

    public SeffActionExitTimeProbe(MeasuringPoint<E> p, SystemMeasurementConfiguration configuration) {
        super(p, configuration);

        configuration.getInterpreterConfiguration().addTraversalListener(getMeasuringPoint().getElement(),
                new ITraversalListener<AbstractAction, Request>() {

                    @Override
                    public void before(AbstractAction action, Request request) {
                        // nothing to do
                    }

                    @Override
                    public void after(AbstractAction action, Request request) {
                        // process the currently observed measurement only when it originates from a
                        // measurement context
                        // equal to or more specific than this probe's measurement context.
                        if (!p.equalsOrIsMoreSpecific(getMeasuringPoint())) {
                            return;
                        }

                        // build measurement
                        double simTime = request.getModel().getSimulationControl().getCurrentSimulationTime();
                        Measurement<E> m = new Measurement<>("CURRENT_TIME", getMeasuringPoint(), request, simTime,
                                simTime);

                        // store
                        cache.put(m);

                        // notify
                        measurementListener.forEach(l -> l.notify(m));
                    }
                });

    }

}
