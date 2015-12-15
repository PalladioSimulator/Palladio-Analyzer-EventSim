package edu.kit.ipd.sdq.eventsim.test.util.builder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.palladiosimulator.pcm.core.entity.Entity;

import edu.kit.ipd.sdq.eventsim.test.util.builder.repository.RepositoryBuilder;
import edu.kit.ipd.sdq.eventsim.test.util.builder.system.SystemBuilder;
import edu.kit.ipd.sdq.eventsim.test.util.builder.usage.UsageBuilder;

public class BuildingContext {

	private List<EObject> eObjectRoots;

	public BuildingContext() {
		eObjectRoots = new ArrayList<>();
	}

	public RepositoryBuilder newRepositoryModel() {
		return new RepositoryBuilder(this);
	}

	public SystemBuilder newSystemModel() {
		return new SystemBuilder(this);
	}

	public UsageBuilder newUsageModel() {
		return new UsageBuilder(this);
	}

	public void add(EObject object) {
		eObjectRoots.add(object);
	}

	// TODO
	public <T extends Entity> T lookup(EClass type, String name) {
		for (EObject root : eObjectRoots) {
			Collection<Object> childObjects = new ArrayList<>();
			root.eAllContents().forEachRemaining(childObjects::add);
			Collection<Entity> filteredChildObjects = EcoreUtil.getObjectsByType(childObjects, type);
			for (Entity e : filteredChildObjects) {
				if (e.getEntityName().equals(name)) {
					return (T) e;
				}
			}
		}
		throw new RuntimeException(
				String.format("Lookup could not find entity of type %s named %s", type.getName(), name));
	}

}
