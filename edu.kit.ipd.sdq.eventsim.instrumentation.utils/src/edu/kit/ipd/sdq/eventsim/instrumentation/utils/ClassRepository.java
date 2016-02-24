package edu.kit.ipd.sdq.eventsim.instrumentation.utils;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.osgi.framework.Bundle;
import org.osgi.framework.wiring.BundleWiring;

public class ClassRepository {

	private static final Logger log = Logger.getLogger(ClassRepository.class);

	private static final String[] DEFAULT_PACKAGES = { "edu.kit.ipd.sdq.eventsim.*", "org.palladiosimulator.pcm.*" };

	public static List<Class<?>> filterAllLoadedClasses(ClassSelector selector) {
		return filterClassesInBundles(selector, DEFAULT_PACKAGES);
	}

	public static List<Class<?>> filterClassesInBundles(ClassSelector selector, String... bundlePackages) {
		List<Class<?>> classes = new ArrayList<>();

		for (Bundle bundle : Activator.getContext().getBundles()) {
			for (String pattern : bundlePackages) {
				if (matches(bundle.getSymbolicName(), pattern)) {
					classes.addAll(filterClassesInBundle(bundle, selector));
				}
			}
		}

		return classes;
	}

	public static List<Class<?>> filterClassesInBundles(ClassSelector selector, Bundle... bundles) {
		List<Class<?>> classes = new ArrayList<>();

		for (Bundle bundle : bundles) {
			classes.addAll(filterClassesInBundle(bundle, selector));
		}

		return classes;
	}

	public static List<Class<?>> filterClassesInBundle(Bundle bundle, ClassSelector selector) {
		List<Class<?>> classes = new ArrayList<>();

		Collection<String> classesInLocalBundle = bundle.adapt(BundleWiring.class).listResources("", "*.class",
				BundleWiring.FINDENTRIES_RECURSE + BundleWiring.LISTRESOURCES_LOCAL);

		for (String classString : classesInLocalBundle) {
			URL classURL = bundle.getEntry(classString);
			if (classURL != null) {
				String className = classURL.getPath().replaceAll("/", ".").replace(".class", "").replace(".bin.", "");
				if (className.startsWith(".")) {
					// remove first character
					className = className.substring(1);
				}

				Class<?> clazz = null;
				try {
					clazz = bundle.loadClass(className);
				} catch (ClassNotFoundException | NoClassDefFoundError e) {
					log.error("Local bundle classloader could not find class" + className);
					continue;
				} catch (IllegalAccessError e) {
					continue;
				}

				if (selector.select(clazz)) {
					classes.add(clazz);
				}
			} else {
				log.debug("Could not locate resource " + classString + " in local bundle");
			}
		}

		return classes;
	}

	/*
	 * matches a package name to a pattern, if the patternis the same like the
	 * package name except for a trailing wildcard
	 */
	private static boolean matches(String packageName, String pattern) {
		String compareString = pattern;

		if (pattern.contains("*")) {
			compareString = pattern.substring(0, pattern.indexOf("*"));

			if (compareString.endsWith(".")) {
				// matches also my.package to my.package.*
				compareString = compareString.substring(0, compareString.length() - 1);
			}
		}

		return packageName.startsWith(compareString);
	}

}
