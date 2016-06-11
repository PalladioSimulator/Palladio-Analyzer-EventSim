package edu.kit.ipd.sdq.eventsim.rvisualization;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import edu.kit.ipd.sdq.eventsim.measurement.r.connection.AbstractConnectionStatusListener;
import edu.kit.ipd.sdq.eventsim.measurement.r.connection.ConnectionListener;
import edu.kit.ipd.sdq.eventsim.measurement.r.connection.ConnectionRegistry;
import edu.kit.ipd.sdq.eventsim.measurement.r.connection.ConnectionStatusListener;
import edu.kit.ipd.sdq.eventsim.measurement.r.connection.RserveConnection;
import edu.kit.ipd.sdq.eventsim.rvisualization.gui.ErrorDialog;
import edu.kit.ipd.sdq.eventsim.rvisualization.gui.GUIStrings;
import edu.kit.ipd.sdq.eventsim.rvisualization.model.DiagramType;
import edu.kit.ipd.sdq.eventsim.rvisualization.model.Entity;
import edu.kit.ipd.sdq.eventsim.rvisualization.model.MeasurementFilter;
import edu.kit.ipd.sdq.eventsim.rvisualization.model.Pair;
import edu.kit.ipd.sdq.eventsim.rvisualization.views.DiagramView;
import edu.kit.ipd.sdq.eventsim.rvisualization.views.FilterView;
import edu.kit.ipd.sdq.eventsim.rvisualization.views.FilterViewController;

/**
 * Controls data exchange between R and Application.
 * 
 * @author Benjamin Rupp
 * @author Philipp Merkle
 *
 */
public class Controller {

    private static final Logger LOG = LogManager.getLogger(Controller.class);

    private RController rCtrl;
    private FilterViewController viewCtrl;
    private FilterView view;

    /**
     * Maximum number of trigger instances to show in a combo box.
     */
    private static final int MAX_NUMBER_OF_TRIGGER_INSTANCES_TO_SHOW = 15;

    /**
     * If a non-aggregated diagram (e.g. a line graph) has more than DIAGRAM_SIZE_LIMIT values, a
     * warning will be shown to the user because the diagram output may cause an SVG
     * "Huge input lookup" error.
     */
    private static final int DIAGRAM_SIZE_LIMIT = 10_000;

    /**
     * Create an application controller.
     * 
     * The application controller creates a RController to manage the R connection and a
     * MeasurementView to open the main SWT dialog.
     */
    public Controller(FilterViewController viewController) {
        LOG.trace("Set up RController ...");
        viewCtrl = viewController;
        view = viewCtrl.getView();
        rCtrl = new RController();
    }

    public final void viewInitialized() {
        // initial population
        populateControls();

        // populateControlsOnConnectOrDisconnect();
    }

    // private void populateControlsOnConnectOrDisconnect() {
    // ConnectionStatusListener statusListener = new AbstractConnectionStatusListener() {
    //
    // @Override
    // public void connected() {
    // new Thread(() -> populateControls()).start();
    // }
    //
    // @Override
    // public void disconnected() {
    // new Thread(() -> populateControls()).start();
    // }
    //
    // };
    // if (ConnectionRegistry.instance().getConnection() == null) {
    // ConnectionRegistry.instance().addListener(new ConnectionListener() {
    //
    // @Override
    // public void connectionRemoved(RserveConnection connection) {
    // connection.removeListener(statusListener);
    // }
    //
    // @Override
    // public void connectionAdded(RserveConnection connection) {
    // connection.addListener(statusListener);
    // }
    //
    // });
    // } else {
    // ConnectionRegistry.instance().getConnection()
    // .addListener(statusListener);
    // }
    // }

    /**
     * Trigger a metric change.
     * 
     * Measuring points of the new selected metric will be delegated to the
     * {@link FilterViewController}.
     * 
     */
    public final void metricSelected() {
        populateTriggerTypes();
        populateAssemblyContexts();
        // populateMeasuringPoints();
    }

    /**
     * Trigger the change of the 'from' measuring point.
     * 
     * This method invokes the {@link #getRelatedToMeasuringPoints(String, List) method.
     */
    public void fromMeasuringPointSelected() {
        viewCtrl.setRelatedToMeasuringPoints();
    }

    /**
     * Trigger a trigger change.
     */
    public final void triggerTypeSelected() {
        populateTriggerInstances();
        populateMeasuringPoints();
    }

    /**
     * Trigger a trigger instance change.
     */
    public final void triggerInstanceSelected() {
        populateMeasuringPoints();
    }

    /**
     * Trigger a trigger span change.
     */
    public final void triggerSimulationBoundsChanged() {

        populateTriggerInstances();
        populateMeasuringPoints();

    }

    /**
     * Trigger trigger reset.
     * 
     * Default values from RDS file will be delegated to the {@link gui.FilterViewController}.
     */
    public final void resetTriggerSimulationTimeBounds() {
        view.setTriggerStart(rCtrl.getSimulationTimeMin());
        view.setTriggerEnd(rCtrl.getSimulationTimeMax());
    }

    public final void resetSimulationTimeBounds() {
        populateSimulationTimeBounds();
    }

    /**
     * Trigger a assembly context change.
     */
    public final void assemblyContextSelected() {
        populateMeasuringPoints();
    }

    /**
     * Trigger diagram plot.
     * 
     * Filter properties from {@link FilterViewController} will be delegated to the
     * {@link RController}. A new {@link DiagramView} will be generated.
     */
    public final void plotDiagram() {

        try {

            DiagramType diagramType = viewCtrl.getDiagramType();
            String diagramTitle = createDiagramTitle();
            String shortDiagramTitle = createShortDiagramTitle();
            String diagramSubTitle = createDiagramSubTitle();
            Set<MeasurementFilter> filterSet = new HashSet<MeasurementFilter>();

            checkTimespanValues(view.getTimeSpanStart(), view.getTimeSpanEnd());

            filterSet = createFilterSet();
            printFilterInfo(filterSet, "Filter Set which is used to plot a " + diagramType + ".");

            // Save the diagram image in temp directory.
            String diagramImageFileExtension = ".svg";
            String diagramImageName = "diagram_";
            File tempFile = File.createTempFile(diagramImageName, diagramImageFileExtension);
            String diagramImagePath = tempFile.getAbsolutePath();

            // Replace '\' with '/' because R cannot handle backslashes.
            diagramImagePath = diagramImagePath.replaceAll(Matcher.quoteReplacement("\\"), "/");
            LOG.trace("DIAGRAM PATH: " + diagramImagePath);

            // Check whether the SVG output gets to huge and show warning.
            int diagramSize = rCtrl.getNumberOfDiagramValues(filterSet);
            LOG.trace("DIAGRAM SIZE: " + diagramSize);

            if (!diagramType.isAggregating() && diagramSize > DIAGRAM_SIZE_LIMIT) {

                MessageDialog dialog = new MessageDialog(Display.getCurrent().getActiveShell(), "Huge diagram output",
                        null,
                        "The diagram plot will contain more than " + DIAGRAM_SIZE_LIMIT + " values (" + diagramSize
                                + "). This may cause an SVG 'Huge output " + "error'. Do you want to continue or "
                                + "restrict the time span to reduce the number "
                                + "of values used for the diagram plot?",
                        MessageDialog.WARNING, new String[] { "No, cancel diagram plot", "Yes, plot diagram" }, 0);

                int result = dialog.open();

                // Cancel plot process if user selected 'No'.
                if (result == 0) {
                    return;
                }

            }

            try {
                // Show busy cursor.
                Display.getCurrent().getActiveShell().setCursor(Display.getDefault().getSystemCursor(SWT.CURSOR_WAIT));

                // Plot diagram using R.
                rCtrl.plotDiagram(diagramType, filterSet, diagramImagePath, shortDiagramTitle, diagramSubTitle);

                String rCommandString = rCtrl.getRCommandForDiagramPlot(diagramType, "dat", filterSet, "img",
                        shortDiagramTitle, diagramSubTitle);

                // Open new view to display the diagram.
                DiagramView view = (DiagramView) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
                        .showView(DiagramView.ID, diagramTitle, IWorkbenchPage.VIEW_ACTIVATE);

                view.setViewTitle(diagramTitle);
                view.setDiagramImage(diagramImagePath);
                view.setRCommandString(rCommandString);

            } finally {

                // Hide busy cursor. Regardless of whether the processing code
                // terminates normally or throws an exception.
                Display.getCurrent().getActiveShell().setCursor(Display.getDefault().getSystemCursor(SWT.CURSOR_ARROW));
            }

        } catch (IOException e) {
            LOG.error("Cannot read path of diagram image.", e);
        } catch (PartInitException e) {
            LOG.error("Cannot initialize view.", e);
        }

    }

    public final void populateControls() {
        populateMeasurementsCount();
        populateMetrics();
        populateSimulationTimeBounds();
        populateDiagramTypes();
        populateTriggerTypes();
        populateTriggerInstances();
        populateAssemblyContexts();
        populateMeasuringPoints();
    }

    private void populateMeasurementsCount() {
        int count = 0;
        if (isConnected()) {
            count = rCtrl.getMeasurementsCount();
        }
        view.setMeasurementsCount(count);
    }

    /**
     * Invoke this method each time a filter has changed.
     * 
     * This method delegates the available measuring points due to the filter change from the
     * {@link RController} to the {@link FilterViewController}.
     */
    private void populateMeasuringPoints() {
        if (!isConnected()) {
            view.enableMeasuringPointsComposite(false);
            return;
        }

        Set<MeasurementFilter> filterSet = createFilterSetFromView();
        List<Pair<Entity>> mp = rCtrl.getMeasuringPoints(filterSet);
        viewCtrl.setMeasuringPoints(mp);
        view.enableMeasuringPointsComposite(true);
    }

    private void populateTriggerTypes() {
        if (!isConnected()) {
            view.enableTriggerTypeGroup(false);
            return;
        }

        String[] triggers = rCtrl.getTriggers();
        view.setTriggers(triggers);

        String description = "Restrict simulation scope to reduce the number " + "of available trigger instances (max. "
                + MAX_NUMBER_OF_TRIGGER_INSTANCES_TO_SHOW + " instances allowed).";
        resetTriggerSimulationTimeBounds();
        view.enableTriggerInstanceGroup(false);
        view.clearTriggerInstances();
        view.setTriggerInstanceDescription(description);
        view.enableTriggerTypeGroup(true);
    }

    /**
     * Invoke this method to set the available trigger instances on the GUI.
     * 
     * This method delegates the available trigger instances due to the filter change from the
     * {@link RController} to the {@link FilterViewController}.
     */
    private void populateTriggerInstances() {
        if (!isConnected()) {
            view.enableTriggerInstanceGroup(false);
            return;
        }

        try {
            String metric = viewCtrl.getMetric();
            String triggerType = view.getTrigger();
            int triggerStart = view.getTriggerStart();
            int triggerEnd = view.getTriggerEnd();

            // return if no trigger type is selected.
            if (triggerType == null) {
                view.clearTriggerInstances();
                view.enableTriggerInstanceGroup(false);
                return;
            }

            // Create filter set.
            Set<MeasurementFilter> filterSet = new HashSet<MeasurementFilter>();
            filterSet.add(new MeasurementFilter("what", "==", metric));
            filterSet.add(new MeasurementFilter("who.type", "==", triggerType));
            filterSet.add(new MeasurementFilter("when", ">=", triggerStart));
            filterSet.add(new MeasurementFilter("when", "<=", triggerEnd));

            int numTriggerInstances = 0;

            try {
                // Show busy cursor.
                Display.getCurrent().getActiveShell().setCursor(Display.getDefault().getSystemCursor(SWT.CURSOR_WAIT));

                numTriggerInstances = rCtrl.getNumberOfTriggerInstances(filterSet);

            } finally {

                // Hide busy cursor. Regardless of whether the processing code
                // terminates normally or throws an exception.
                Display.getCurrent().getActiveShell().setCursor(Display.getDefault().getSystemCursor(SWT.CURSOR_ARROW));
            }

            LOG.trace("::: Number of trigger instances: " + numTriggerInstances);

            // Return if no trigger instances are available.
            if (numTriggerInstances <= 0) {
                view.enableTriggerInstanceGroup(false);
                return;
            }

            // Show available trigger instances on GUI.
            view.enableTriggerInstanceGroup(true);

            if (numTriggerInstances <= MAX_NUMBER_OF_TRIGGER_INSTANCES_TO_SHOW) {

                view.enableTriggerInstanceComboBox(true); // TODO necessary?
                view.setTriggerInstanceNumber(Integer.toString(numTriggerInstances));

                // Set trigger instances.
                List<Entity> triggerInstances = rCtrl.getTriggerInstances(filterSet);
                viewCtrl.setTriggerInstances(triggerInstances);

            } else {

                view.clearTriggerInstances();

                // Show user info with the number of available instances.
                view.setTriggerInstanceNumber(Integer.toString(numTriggerInstances), true);

            }

        } catch (Exception e) {
            LOG.warn("Could not get current metric: " + e.getMessage());
        }

    }

    private void populateDiagramTypes() {
        List<DiagramType> types = Arrays.asList(DiagramType.values());
        viewCtrl.setDiagramTypes(types);
    }

    private void populateMetrics() {
        if (!isConnected()) {
            view.enableMetricsComposite(false);
            return;
        }

        List<String> metrics = rCtrl.getMetrics();
        viewCtrl.setMetrics(metrics);
        view.enableMetricsComposite(true);
    }

    private void populateAssemblyContexts() {
        if (!isConnected()) {
            view.enableAssemblyContextComposite(false);
            return;
        }

        List<Entity> assemblyContexts = rCtrl.getAssemblyContexts();
        viewCtrl.setAssemblyContexts(assemblyContexts);
        view.enableAssemblyContextComposite(true);
    }

    /**
     * Trigger time span reset.
     * 
     * Default values from RDS file will be delegated to the {@link gui.FilterViewController}.
     */
    private final void populateSimulationTimeBounds() {
        if (!isConnected()) {
            view.enableTimeSpanComposite(false);
            return;
        }

        int lower = rCtrl.getSimulationTimeMin();
        int upper = rCtrl.getSimulationTimeMax();
        view.setTimeSpanBounds(lower, upper);

        view.setTimeSpanLower(lower);
        view.setTimeSpanUpper(upper);

        view.setTimeSpanDescription("Simulation starts at " + rCtrl.getSimulationTimeMin() + " and ends at "
                + rCtrl.getSimulationTimeMax());
        view.enableTimeSpanComposite(true);
    }

    /**
     * Create a set of filters based on the current filter settings.
     * 
     * @return Set of relevant filters.
     * @throws Exception
     *             If an error occurs while collecting the filter settings.
     */
    private Set<MeasurementFilter> createFilterSet() {

        Set<MeasurementFilter> filterSet = new HashSet<MeasurementFilter>();

        // Get filter settings.
        String metric = viewCtrl.getMetric();
        Pair<Entity> mp = viewCtrl.getMeasuringPoints();
        String trigger = view.getTrigger();
        Entity triggerInstance = view.getTriggerInstance();
        Entity assemblyContext = view.getAssemblyContext();
        int timespanStart = view.getTimeSpanStart();
        int timespanEnd = view.getTimeSpanEnd();

        // Add metric filter.
        filterSet.add(new MeasurementFilter("what", "==", metric));

        // Add measuring point filter.
        if (mp.getFirst() != null) {

            filterSet.add(new MeasurementFilter("where.first.id", "==", mp.getFirst().getId()));

        }

        if (mp.getSecond() != null) {

            filterSet.add(new MeasurementFilter("where.second.id", "==", mp.getSecond().getId()));

        }

        // Add time span filter.
        filterSet.add(new MeasurementFilter("when", ">=", timespanStart));
        filterSet.add(new MeasurementFilter("when", "<=", timespanEnd));

        // Add trigger filter.
        if (trigger != null) {
            filterSet.add(new MeasurementFilter("who.type", "==", trigger));
        }
        if (triggerInstance != null) {
            filterSet.add(new MeasurementFilter("who.id", "==", triggerInstance.getId()));
        }

        // Add assembly context filter.
        if (assemblyContext != null) {
            filterSet.add(new MeasurementFilter("assemblycontext.id", "==", assemblyContext.getId()));
        }

        return filterSet;
    }

    private Set<MeasurementFilter> createFilterSetFromView() {
        // Get current filter settings and create set of filters.
        String triggerType = view.getTrigger();
        Entity triggerInstance = view.getTriggerInstance();
        String metric = viewCtrl.getMetric();
        Entity assemblyContext = view.getAssemblyContext();

        LOG.trace("_");
        LOG.trace("Creating filter set based on the following information:");
        LOG.trace("Trigger: " + triggerType);
        LOG.trace("Trigger instance: " + triggerInstance);
        LOG.trace("Metric: " + metric);
        LOG.trace("Assembly context: " + assemblyContext);
        LOG.trace("_");

        Set<MeasurementFilter> filterSet = new HashSet<MeasurementFilter>();

        filterSet.add(new MeasurementFilter("what", "==", metric));

        // If 'all triggers' are selected no trigger filter will be applied.
        if (triggerType != null) {
            filterSet.add(new MeasurementFilter("who.type", "==", triggerType));
        }

        // If 'all trigger instances' are selected no trigger instance
        // filter will be applied.
        if (triggerInstance != null) {
            filterSet.add(new MeasurementFilter("who.id", "==", triggerInstance.getId()));
        }

        // If 'all assembly contexts' are selected no assembly context
        // filter will be applied.
        if (assemblyContext != null) {
            filterSet.add(new MeasurementFilter("assemblycontext.id", "==", assemblyContext.getId()));
        }
        return filterSet;
    }

    /**
     * Create the diagram title based on the filter settings.
     * 
     * @return Diagram title.
     */
    private String createDiagramTitle() {

        String diagramTitle = "";
        Pair<Entity> mp = viewCtrl.getMeasuringPoints();

        diagramTitle += createShortDiagramTitle();

        if (mp.getSecond() != null) {
            diagramTitle += " to " + mp.getSecond();
        }

        try {
            diagramTitle += " (" + GUIStrings.getGUIString(GUIStrings.getDiagramTypes(), viewCtrl.getDiagramType())
                    + ")";
        } catch (Exception e) {
            LOG.error("Cannot get selected diagram type " + "to create diagram title.");
        }

        return diagramTitle;
    }

    /**
     * Create the short version of the diagram title.
     * 
     * @return Short diagram title.
     */
    private String createShortDiagramTitle() {

        String diagramTitle = "";
        Pair<Entity> mp = viewCtrl.getMeasuringPoints();

        try {

            diagramTitle += GUIStrings.getGUIString(GUIStrings.getMetrics(), viewCtrl.getMetric());

        } catch (Exception e) {
            LOG.error("Cannot get selected metric " + "to create the diagram title.");
        }

        diagramTitle += " of " + mp.getFirst();

        return diagramTitle;
    }

    /**
     * Create the diagram subtitle based on the filter settings.
     * 
     * @return Diagram subtitle.
     */
    private String createDiagramSubTitle() {
        String diagramSubTitle = "";

        diagramSubTitle += "Simulation time span: ";
        diagramSubTitle += view.getTimeSpanStart();
        diagramSubTitle += " - ";
        diagramSubTitle += view.getTimeSpanEnd();

        return diagramSubTitle;
    }

    /**
     * Print filter information for logging purposes.
     * 
     * @param filterSet
     *            Current set of filters.
     * @param description
     *            Short description, e.g. the usage of the filter set.
     */
    private void printFilterInfo(Set<MeasurementFilter> filterSet, String description) {
        LOG.trace("---------------- Filter Set ------------------");
        LOG.trace(description);
        for (MeasurementFilter filter : filterSet) {
            LOG.trace(filter.getProperty() + " " + filter.getOperator() + " " + filter.getCondition());
        }
        LOG.trace("----------------------------------------------");
    }

    private boolean isConnected() {
        return rCtrl.getConnection() != null && rCtrl.getConnection().isConnected();
    }

    /**
     * Check start and end values of a time span.
     * 
     * @param timespanStart
     *            Start value.
     * @param timespanEnd
     *            End value.
     */
    private void checkTimespanValues(int timespanStart, int timespanEnd) {
        if (timespanEnd <= timespanStart) {
            LOG.error("Cannot plot diagram! " + "Time span to-value have to be greater than the "
                    + "time span from-value.");

            new ErrorDialog("Error: Invalid time span values!",
                    "Time span to-value have to be greater than the " + "time span from-value!");

            return;
        }
    }
}
