package edu.kit.ipd.sdq.eventsim.command.resource;

import java.util.ArrayList;
import java.util.List;

import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.repository.BasicComponent;
import org.palladiosimulator.pcm.repository.PassiveResource;
import org.palladiosimulator.pcm.repository.RepositoryComponent;
import org.palladiosimulator.pcm.repository.RepositoryPackage;

import edu.kit.ipd.sdq.eventsim.api.PCMModel;
import edu.kit.ipd.sdq.eventsim.command.ICommandExecutor;
import edu.kit.ipd.sdq.eventsim.command.IPCMCommand;
import edu.kit.ipd.sdq.eventsim.exceptions.unchecked.EventSimException;

public class FindPassiveResourcesInAssemblyContext implements IPCMCommand<List<PassiveResource>> {

	private AssemblyContext assemblyContext;

	public FindPassiveResourcesInAssemblyContext(AssemblyContext assemblyContext) {
		this.assemblyContext = assemblyContext;
	}

	@Override
	public List<PassiveResource> execute(PCMModel model, ICommandExecutor<PCMModel> executor) {
		BasicComponent basicComponent = findBasicComponent(assemblyContext);

		List<PassiveResource> result = new ArrayList<>();

		for (PassiveResource res : basicComponent.getPassiveResource_BasicComponent()) {
			result.add(res);
		}

		return result;
	}

	/**
	 * Returns the component that is encapsulated in the given assembly context.
	 * If this component is not a BasicComponent, an exception is thrown.
	 * 
	 * @param assemblyCtx
	 *            the assembly context
	 * @return the component, if the encapsulated component is a BasicComponent
	 */
	private BasicComponent findBasicComponent(AssemblyContext assemblyCtx) {
		RepositoryComponent component = assemblyCtx.getEncapsulatedComponent__AssemblyContext();
		if (RepositoryPackage.eINSTANCE.getBasicComponent().isInstance(component)) {
			return (BasicComponent) component;
		} else {
			throw new EventSimException("Currently only BasicComponents are supported.");
		}
	}

	@Override
	public boolean cachable() {
		return false;
	}

}
