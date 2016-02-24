package edu.kit.ipd.sdq.eventsim.command.useraction;

import java.util.List;

import org.palladiosimulator.pcm.usagemodel.UsageScenario;

import edu.kit.ipd.sdq.eventsim.api.PCMModel;
import edu.kit.ipd.sdq.eventsim.command.ICommandExecutor;
import edu.kit.ipd.sdq.eventsim.command.IPCMCommand;

public class FindUsageScenarios implements IPCMCommand<List<UsageScenario>> {

	@Override
	public List<UsageScenario> execute(PCMModel model, ICommandExecutor<PCMModel> executor) {
		return model.getUsageModel().getUsageScenario_UsageModel();
	}

	@Override
	public boolean cachable() {
		return false;
	}

}
