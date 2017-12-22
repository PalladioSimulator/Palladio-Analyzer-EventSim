package edu.kit.ipd.sdq.eventsim.instrumentation.specification;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import edu.kit.ipd.sdq.eventsim.instrumentation.specification.repo.CalculatorRepository;
import edu.kit.ipd.sdq.eventsim.instrumentation.specification.repo.ProbeRepository;

public class Activator implements BundleActivator {
	
	public static final String PLUGIN_ID = "";

	private static BundleContext context;

	public static BundleContext getContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.
	 * BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		ProbeRepository.init();
		CalculatorRepository.init();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		Activator.context = null;
	}

}
