package edu.kit.ipd.sdq.eventsim.instrumentation.specification.repo;

import java.util.List;

import org.palladiosimulator.pcm.seff.AbstractAction;

import edu.kit.ipd.sdq.eventsim.instrumentation.utils.ClassRepository;

public class ActionTypeRepository {

	private static List<Class<?>> actionTypes;

	public static List<Class<?>> getAllActionTypes() {
		if (actionTypes == null) {
			load();
		}

		return actionTypes;
	}

	public static void load() {
		// Get all interfaces extending AbstractAction
		actionTypes = ClassRepository
				.filterAllLoadedClasses(c -> AbstractAction.class.isAssignableFrom(c) && c.isInterface());
	}

}
