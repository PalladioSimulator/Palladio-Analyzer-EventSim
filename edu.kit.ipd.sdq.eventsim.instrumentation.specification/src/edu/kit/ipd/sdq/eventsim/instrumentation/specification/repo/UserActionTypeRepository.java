package edu.kit.ipd.sdq.eventsim.instrumentation.specification.repo;

import java.util.List;

import org.palladiosimulator.pcm.usagemodel.AbstractUserAction;

import edu.kit.ipd.sdq.eventsim.instrumentation.utils.ClassRepository;

public class UserActionTypeRepository {

	private static List<Class<?>> userActionTypes;

	public static List<Class<?>> getAllUserActionTypes() {
		if (userActionTypes == null) {
			load();
		}

		return userActionTypes;
	}

	public static void load() {
		// Get all interfaces extending AbstractUserAction
		userActionTypes = ClassRepository
				.filterAllLoadedClasses(c -> AbstractUserAction.class.isAssignableFrom(c) && c.isInterface());
	}

}
