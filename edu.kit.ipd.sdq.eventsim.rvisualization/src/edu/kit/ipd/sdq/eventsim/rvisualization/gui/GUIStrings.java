package edu.kit.ipd.sdq.eventsim.rvisualization.gui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import edu.kit.ipd.sdq.eventsim.rvisualization.model.DiagramType;
import edu.kit.ipd.sdq.eventsim.rvisualization.util.Helper;

/**
 * Provides a mechanism to convert technical strings (e.g. QUEUE_LENGTH) to a more readable string
 * (e.g. queue length) which will be displayed on GUI.
 * 
 * @author Benjamin Rupp
 *
 */
public final class GUIStrings {

    private static final Logger LOG = LogManager.getLogger(GUIStrings.class);

    /**
     * Private constructor because methods are only accessed in a static way.
     */
    private GUIStrings() {
    }

    /**
     * Map of available metrics and their GUI strings.
     */
    private static Map<String, String> metrics;

    /**
     * Map of available diagram types and their GUI strings.
     */
    private static Map<DiagramType, String> diagramTypes;

    static {
        metrics = new HashMap<String, String>();
        metrics.put("HOLD_TIME", "Hold Time of Passive Resources");
        metrics.put("QUEUE_LENGTH", "Queue Length of Resources");
        metrics.put("RESOURCE_DEMAND", "Resource Demand");
        metrics.put("RESPONSE_TIME", "Response Time");
        metrics.put("TIME_SPAN", "Time Span (Response Time)");
        metrics.put("WAITING_TIME", "Waiting Time for Passive Resources");

    }

    /**
     * Get map of available metrics and their GUI strings.
     * 
     * @return Map of available metrics and their GUI strings.
     */
    public static Map<String, String> getMetrics() {
        return metrics;
    }

    /**
     * Get map of available diagram types and their GUI strings.
     * 
     * @return Map of available diagram types and their GUI strings.
     */
    public static Map<DiagramType, String> getDiagramTypes() {
        return diagramTypes;
    }

    /**
     * Get the string of a technical name which should be displayed on GUI.
     * 
     * @param <T>
     *            Type of technical name (e.g. String or {@link DiagramType}).
     * @param guiStrings
     *            Map with available GUI strings (e.g. {@link GUIStrings#metrics}).
     * @param technicalName
     *            Technical name of the GUI string.
     * @return GUI string which belongs to the technical name.
     */
    public static <T> String getGUIString(final Map<T, String> guiStrings, final T technicalName) {

        if (guiStrings.containsKey(technicalName)) {
            return guiStrings.get(technicalName);
        } else {
            // Technical name doesn't exist in map.
            // Add a new map entry with technical name as key and
            // the string of the technical name as value.
            // Needed to resolve the GUI string to a non-string value (e.g. an
            // Enum).
            guiStrings.put(technicalName, technicalName.toString());

            return technicalName.toString();
        }

    }

    /**
     * Get the strings of a list of technical names which should be displayed on GUI.
     * 
     * @param <T>
     *            Type of technical name (e.g. String or {@link DiagramType}).
     * @param guiStrings
     *            Map with available GUI strings (e.g. {@link GUIStrings#metrics}).
     * @param listOfTechnicalNames
     *            List of technical names of the GUI strings.
     * @return GUI strings which belong to the technical names.
     */
    public static <T> String[] getGUIStrings(final Map<T, String> guiStrings, final List<T> listOfTechnicalNames) {

        String[] m = new String[listOfTechnicalNames.size()];

        for (int i = 0; i < m.length; i++) {
            m[i] = getGUIString(guiStrings, listOfTechnicalNames.get(i));
        }

        return m;
    }

    /**
     * Get the technical name of a given GUI string.
     * 
     * @param <T>
     *            Type of technical name (e.g. String or {@link DiagramType}).
     * @param guiStrings
     *            Map with available GUI strings (e.g. {@link GUIStrings#metrics}).
     * @param guiString
     *            String which is displayed on GUI.
     * @return Technical name as String or {@code null} if no technical name was found.
     */
    public static <T> T getTechnicalName(final Map<T, String> guiStrings, final String guiString) {
        if (guiStrings.containsValue(guiString)) {
            // Return the key of the given value.
            return Helper.getKeyByValue(guiStrings, guiString);
        } else {
            return null;
        }

    }
}
