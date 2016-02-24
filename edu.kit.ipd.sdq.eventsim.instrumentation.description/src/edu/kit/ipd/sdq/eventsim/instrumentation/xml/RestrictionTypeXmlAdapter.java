package edu.kit.ipd.sdq.eventsim.instrumentation.xml;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.InstrumentableRestriction;
import edu.kit.ipd.sdq.eventsim.instrumentation.utils.ClassRepository;

public class RestrictionTypeXmlAdapter extends XmlAdapter<String, Class<?>> {

	@Override
	public String marshal(Class<?> clazz) throws Exception {
		return clazz.getName();
	}

	@Override
	public Class<?> unmarshal(String className) throws Exception {
		List<Class<?>> classes = ClassRepository.filterClassesInBundles(c -> c.getName().equals(className),
				loadExtensions());
		return classes.get(0);
	}

	private String[] loadExtensions() {
		List<String> pckages = new ArrayList<>();
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint point = registry.getExtensionPoint(InstrumentableRestriction.EXTENSION_POINT_ID);

		for (IExtension extension : point.getExtensions()) {
			pckages.add(extension.getNamespaceIdentifier());
		}

		return pckages.toArray(new String[0]);
	}

}
