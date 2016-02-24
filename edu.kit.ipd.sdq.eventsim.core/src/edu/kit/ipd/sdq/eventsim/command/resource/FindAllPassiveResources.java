package edu.kit.ipd.sdq.eventsim.command.resource;

import java.util.ArrayList;
import java.util.List;

import org.palladiosimulator.pcm.allocation.AllocationContext;
import org.palladiosimulator.pcm.core.composition.AssemblyContext;
import org.palladiosimulator.pcm.repository.PassiveResource;

import edu.kit.ipd.sdq.eventsim.api.PCMModel;
import edu.kit.ipd.sdq.eventsim.command.ICommandExecutor;
import edu.kit.ipd.sdq.eventsim.command.IPCMCommand;

public class FindAllPassiveResources implements IPCMCommand<List<PassiveResourceContext>> {

	@Override
	public List<PassiveResourceContext> execute(PCMModel model, ICommandExecutor<PCMModel> executor) {
		List<PassiveResourceContext> result = new ArrayList<>();

		for (AllocationContext allocationCtx : model.getAllocationModel().getAllocationContexts_Allocation()) {
			AssemblyContext assemblyCtx = allocationCtx.getAssemblyContext_AllocationContext();

			for (PassiveResource res : executor.execute(new FindPassiveResourcesInAssemblyContext(assemblyCtx))) {
				result.add(new PassiveResourceContext(res, assemblyCtx));
			}
		}

		return result;
	}

	@Override
	public boolean cachable() {
		return false;
	}

}
