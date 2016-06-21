package edu.kit.ipd.sdq.eventsim.rvisualization.util;

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
     * Enables or disables a control and all of its direct and indirect children, if any.
     * 
     * @param control
     *            the root control
     * @param enabled
     *            {@code true}, if controls are to be enabled; {@code false} else
     */
    public static void setEnabledRecursive(final Control control, final boolean enabled) {
        control.setEnabled(enabled);
        if (control instanceof Composite) {
            Composite composite = (Composite) control;
            for (Control child : composite.getChildren()) {
                setEnabledRecursive(child, enabled);
            }
        }
    }

}
