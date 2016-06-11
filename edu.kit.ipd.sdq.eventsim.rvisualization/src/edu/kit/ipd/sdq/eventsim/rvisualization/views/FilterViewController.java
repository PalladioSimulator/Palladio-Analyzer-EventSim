package edu.kit.ipd.sdq.eventsim.rvisualization.views;

import java.util.ArrayList;
import java.util.List;

import edu.kit.ipd.sdq.eventsim.rvisualization.Controller;
import edu.kit.ipd.sdq.eventsim.rvisualization.gui.GUIStrings;
import edu.kit.ipd.sdq.eventsim.rvisualization.model.DiagramType;
import edu.kit.ipd.sdq.eventsim.rvisualization.model.Entity;
import edu.kit.ipd.sdq.eventsim.rvisualization.model.Pair;

/**
 * Controller for filter view.
 * 
 * Delegates communication between plug-ins main controller ({@link Controller}) and filter view (
 * {@link FilterView}) and provides some conversion functionality ( e.g. in
 * {@link #getDiagramType()}).
 * 
 * @see FilterView
 * 
 * @author Benjamin Rupp
 * @author Philipp Merkle
 *
 */
public final class FilterViewController {

    private static FilterView filterView;

    /**
     * Currently available measuring points.
     * 
     * Store measuring point pairs for updating 'to' measuring points after the user selects another
     * 'from' measuring point.
     */
    private List<Pair<Entity>> measuringPoints;

    /**
     * Private constructor.
     * 
     * @param view
     *            View which should be controlled.
     */
    public FilterViewController(final FilterView view) {
        filterView = view;
    }

    public FilterView getView() {
        return filterView;
    }

    /**
     * Set the currently available metrics.
     * 
     * Converts technical strings from R file to GUI strings.
     * 
     * @see GUIStrings
     * 
     * @param metrics
     *            List of all available metrics with their technical names (e.g. QUEUE_LENGTH).
     */
    public void setMetrics(final List<String> metrics) {
        String[] m = GUIStrings.getGUIStrings(GUIStrings.getMetrics(), metrics);
        filterView.setMetrics(m);
    }

    /**
     * Get selected metric.
     * 
     * Converts GUI string to technical string used in R file.
     * 
     * @see GUIStrings
     * 
     * @return Currently selected metric as technical string (e.g. QUEUE_LENGTH).
     * @throws Exception
     *             If an invalid technical name was used.
     */
    public String getMetric() {
        return GUIStrings.getTechnicalName(GUIStrings.getMetrics(), filterView.getSelectedMetric());
    }

    /**
     * Get the currently selected measuring points.
     * 
     * @return Measuring point pair.
     */
    public Pair<Entity> getMeasuringPoints() {
        Pair<Entity> mp = new Pair<>(filterView.getSelectedMeasuringPointFrom(),
                filterView.getSelectedMeasuringPointTo());
        return mp;
    }

    /**
     * Set the available measuring points.
     * 
     * @param mp
     *            List of available measuring point pairs.
     */
    public void setMeasuringPoints(final List<Pair<Entity>> mp) {
        // Store current measuring point pairs for updating 'to' measuring
        // points after the user selects another 'from' measuring point.
        this.measuringPoints = mp;

        // Set 'from' measuring points
        Entity[] measuringPoints = new Entity[mp.size()];
        int i = 0;
        for (Pair<Entity> pair : mp) {
            measuringPoints[i] = pair.getFirst();
            i++;
        }
        filterView.setMeasuringPointsFrom(measuringPoints);
        filterView.setMeasuringPointsTo(new Entity[0]);

        // The available 'to' measuring points are set in the
        // setRelatedToMeasuringPoints() method which will be invoked after
        // changing the 'from' measuring points by the triggerMPFromChange()
        // method.
    }

    /**
     * Set the currently available diagram types.
     */
    public void setDiagramTypesFromEnum() {
        filterView.setDiagramTypes(DiagramType.values());
    }

    /**
     * Set trigger instances.
     * 
     * @param instances
     *            List of all available trigger instances.
     */
    public void setTriggerInstances(final List<Entity> instances) {
        Entity[] entities = new Entity[instances.size()];
        filterView.setTriggerInstances(instances.toArray(entities));
    }

    /**
     * Set the available assembly contexts. This method will add the default option 'all' to the
     * list of possible assembly contexts.
     * 
     * @param ctxs
     *            List of available assembly contexts.
     */
    public void setAssemblyContexts(final List<Entity> ctxs) {
        Entity[] entities = new Entity[ctxs.size()];
        filterView.setAssemblyContexts(ctxs.toArray(entities));
    }

    /**
     * Get related 'to' measuring points of the given 'from' measuring point.
     * 
     * @param fromMeasuringPointId
     *            Id of a 'from' measuring point.
     * @param mps
     *            List of available measuring point pairs.
     * @return List of readable strings (name and id) of the related 'to' measuring points.
     */
    private Entity[] getRelatedToMeasuringPoints(final String fromMeasuringPointId, final List<Pair<Entity>> mps) {

        List<Entity> availableToMeasuringPoints = new ArrayList<>();

        for (Pair<Entity> mp : mps) {
            if (mp.getFirst().getId().equals(fromMeasuringPointId) && mp.getSecond().getId() != null) {
                availableToMeasuringPoints.add(mp.getSecond());
            }
        }

        // Return list as an array of strings, because the GUI elements need
        // string arrays for updating the combo box items.
        Entity[] result = new Entity[availableToMeasuringPoints.size()];
        return availableToMeasuringPoints.toArray(result);

    }

    /**
     * Set the related 'to' measuring points of a given 'from' measuring point.
     * 
     */
    public void setRelatedToMeasuringPoints() {
        Entity from = filterView.getSelectedMeasuringPointFrom();
        Entity[] tos = getRelatedToMeasuringPoints(from.getId(), measuringPoints);
        filterView.setMeasuringPointsTo(tos);
    }

}
