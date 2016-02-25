package edu.kit.ipd.sdq.eventsim.measurement.probe;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.apache.log4j.Logger;

import edu.kit.ipd.sdq.eventsim.measurement.MeasuringPoint;
import edu.kit.ipd.sdq.eventsim.measurement.ProbeConfiguration;
import edu.kit.ipd.sdq.eventsim.measurement.annotation.Probe;

/**
 * A factory for instances of {@link IProbe}. Maintains a map that maps (element, property)-pairs to probes; each
 * mapping expresses that the probe is capable to measure the specified property for the specified element. Mappings are
 * created by searching the classpath for classes annotated with {@link Probe}.
 * 
 * @author Philipp Merkle
 *
 * @param <C>
 */
public class ProbeFactory<C extends ProbeConfiguration> {

	private static final Logger log = Logger.getLogger(ProbeFactory.class);

	private C configuration;
	
	private ProbeLocator<C> probeLocator;

	public ProbeFactory(C configuration, ProbeLocator<C> probeLocator) {
		this.configuration = configuration;
		this.probeLocator = probeLocator;
	}

	public <E> IProbe<E> create(E element, String property, Object... measurementContexts) {
		// try finding a probe capable of probing elements of the given type.
		// start with the most specific element type.
		Class<? extends AbstractProbe<?, C>> probeClass = probeLocator.probeForType(element.getClass(), property);

		if (probeClass == null) {
			log.error(String.format(
					"No probe has been found capable of measuring property \"%s\" for elements of " + "type %s",
					element.getClass(), property));
			return IProbe.nullProbe(element, property, measurementContexts);
		}

		AbstractProbe<?, C> p = null;
		try {
			Constructor<? extends AbstractProbe<?, C>> c = probeClass.getConstructor(MeasuringPoint.class,
					configuration.getClass());
			p = c.newInstance(new MeasuringPoint<E>(element, property, measurementContexts), configuration);
			log.debug("Created probe " + p + " (element=" + element + ", property=" + property + ")");
		} catch (InstantiationException | IllegalAccessException | NoSuchMethodException | SecurityException
				| IllegalArgumentException | InvocationTargetException e) {
			log.error("Exception while invoking probe constructor.", e);
			return IProbe.nullProbe(element, property, measurementContexts);
		}

		return (IProbe<E>) p;
	}

}
