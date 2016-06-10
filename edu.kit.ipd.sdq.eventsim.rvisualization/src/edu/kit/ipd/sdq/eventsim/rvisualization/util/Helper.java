package edu.kit.ipd.sdq.eventsim.rvisualization.util;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Provides some helper methods.
 * 
 * @author Benjamin Rupp
 * @author Philipp Merkle
 *
 */
public final class Helper {

	/**
	 * Private constructor because methods are only accessed in a static way.
	 */
	private Helper() {
	}

	/**
	 * Get a map key by a given value.
	 * 
	 * @param <T>
	 *            Type of the keys.
	 * @param <E>
	 *            Type of the value.
	 * @param map
	 *            Map which should contain the given value.
	 * @param value
	 *            Value for which the key is needed.
	 * @return Key of the given value, null if no key was found.
	 */
	public static <T, E> T getKeyByValue(final Map<T, E> map, final E value) {
		for (Entry<T, E> entry : map.entrySet()) {
			if (Objects.equals(value, entry.getValue())) {
				return entry.getKey();
			}
		}
		return null;
	}

	/**
	 * Enables or disables a control and all of its direct and indirect children,
	 * if any.
	 * 
	 * @param control
	 *            the root control
	 * @param enabled
	 *            {@code true}, if controls are to be enabled; {@code false}
	 *            else
	 */
	public static void setEnabledRecursive(final Control control,
			final boolean enabled) {
		control.setEnabled(enabled);
		if (control instanceof Composite) {
			Composite composite = (Composite) control;
			for (Control child : composite.getChildren()) {
				setEnabledRecursive(child, enabled);
			}
		}
	}

}
