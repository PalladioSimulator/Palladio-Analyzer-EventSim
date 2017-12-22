package edu.kit.ipd.sdq.eventsim.rvisualization.util;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import edu.kit.ipd.sdq.eventsim.rvisualization.model.TranslatableEntity;

/**
 * Provides some helper methods.
 * 
 * @author Benjamin Rupp
 * @author Philipp Merkle
 *
 */
public final class Helper {

    private static final String METRICS_EXTENSION_POINT_ID = "edu.kit.ipd.sdq.eventsim.rvisualization.metriclabels";

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

    public static Map<String, TranslatableEntity> getMetricsLabelExtensions() {
        IConfigurationElement[] config = Platform.getExtensionRegistry()
                .getConfigurationElementsFor(METRICS_EXTENSION_POINT_ID);

        Map<String, TranslatableEntity> entities = new HashMap<>();
        for (IConfigurationElement c : config) {
            String name = c.getAttribute("name");
            String label = c.getAttribute("label");
            entities.put(name, new TranslatableEntity(name, label));
        }
        return entities;
    }

}
