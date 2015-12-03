package edu.kit.ipd.sdq.eventsim.workload;

import org.palladiosimulator.pcm.core.CoreFactory;
import org.palladiosimulator.pcm.core.PCMRandomVariable;
import org.palladiosimulator.pcm.usagemodel.ClosedWorkload;
import org.palladiosimulator.pcm.usagemodel.Delay;
import org.palladiosimulator.pcm.usagemodel.ScenarioBehaviour;
import org.palladiosimulator.pcm.usagemodel.Start;
import org.palladiosimulator.pcm.usagemodel.Stop;
import org.palladiosimulator.pcm.usagemodel.UsageModel;
import org.palladiosimulator.pcm.usagemodel.UsageScenario;
import org.palladiosimulator.pcm.usagemodel.UsagemodelFactory;

public class WorkloadModelHelper {

	public static Delay createStartDelayStopActionChainInBehaviour(ScenarioBehaviour b) {
		Start start = UsagemodelFactory.eINSTANCE.createStart();
		start.setScenarioBehaviour_AbstractUserAction(b);
	
		Delay delay = UsagemodelFactory.eINSTANCE.createDelay();
		delay.setScenarioBehaviour_AbstractUserAction(b);
		delay.setPredecessor(start);
	
		PCMRandomVariable delayTime = CoreFactory.eINSTANCE.createPCMRandomVariable();
		delayTime.setSpecification("1.42");
		delayTime.setDelay_TimeSpecification(delay);
	
		Stop stop = UsagemodelFactory.eINSTANCE.createStop();
		stop.setScenarioBehaviour_AbstractUserAction(b);
		stop.setPredecessor(delay);
		return delay;
	}

	public static ScenarioBehaviour createUsageScenarioWithClosedWorkloadInUsageModel(UsageModel um, int population) {
		UsageScenario s = UsagemodelFactory.eINSTANCE.createUsageScenario();
		s.setUsageModel_UsageScenario(um);
	
		ClosedWorkload w = UsagemodelFactory.eINSTANCE.createClosedWorkload();
		w.setUsageScenario_Workload(s);
		w.setPopulation(population);
	
		PCMRandomVariable thinkTime = CoreFactory.eINSTANCE.createPCMRandomVariable();
		thinkTime.setSpecification("0");
		thinkTime.setClosedWorkload_PCMRandomVariable(w);
	
		ScenarioBehaviour b = UsagemodelFactory.eINSTANCE.createScenarioBehaviour();
		b.setUsageScenario_SenarioBehaviour(s);
		return b;
	}

}
