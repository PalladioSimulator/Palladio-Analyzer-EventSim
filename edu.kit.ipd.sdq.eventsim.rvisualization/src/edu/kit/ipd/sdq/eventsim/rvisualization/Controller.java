package edu.kit.ipd.sdq.eventsim.rvisualization;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import edu.kit.ipd.sdq.eventsim.measurement.r.connection.AbstractConnectionStatusListener;
import edu.kit.ipd.sdq.eventsim.measurement.r.connection.ConnectionListener;
import edu.kit.ipd.sdq.eventsim.measurement.r.connection.ConnectionRegistry;
import edu.kit.ipd.sdq.eventsim.measurement.r.connection.ConnectionStatusListener;
import edu.kit.ipd.sdq.eventsim.measurement.r.connection.RserveConnection;
import edu.kit.ipd.sdq.eventsim.rvisualization.model.DiagramType;
import edu.kit.ipd.sdq.eventsim.rvisualization.model.Entity;
import edu.kit.ipd.sdq.eventsim.rvisualization.model.FilterModel;
import edu.kit.ipd.sdq.eventsim.rvisualization.model.FilterSelectionModel;
import edu.kit.ipd.sdq.eventsim.rvisualization.model.TranslatableEntity;
import edu.kit.ipd.sdq.eventsim.rvisualization.util.Procedure;
import edu.kit.ipd.sdq.eventsim.rvisualization.views.DiagramView;
import edu.kit.ipd.sdq.eventsim.rvisualization.views.FilterView;

/**
 * Glues together the {@link FilterView} and {@link DiagramView} on the one side, and the
 * {@link FilterModel} and {@link FilterSelectionModel} on the other side.
 * <p>
 * Responsibilities of this controller include:
 * <ul>
 * <li>observe {@link FilterSelectionModel} for selection events (mainly triggered by the user), and
 * react to the selection by modifying the {@link FilterModel}. Data binding established by the
 * {@link FilterView} ensures that the view reflects the model at any time. Explicit view updates
 * triggered by this controller are not required, usually. Likewise, it is not necessary to query
 * the view's (selection) state because the (selection) model provides that information.</li>
 * <li>populate the {@link FilterModel} from measurements stored in R with the help of the
 * {@link RController}.</li>
 * <li>trigger diagram plots in R with the help of the {@link RController}.</li>
 * </ul>
 * 
 * @author Benjamin Rupp
 * @author Philipp Merkle
 *
 */
public class Controller {

    private static final Logger LOG = LogManager.getLogger(Controller.class);

    private FilterModel model;

    private FilterSelectionModel selectionModel;

    private FilterView view;

    private RController rCtrl;

    private SelectionHandler selectionHandler = new SelectionHandler();

    /**
     * Maximum number of trigger instances to show in a combo box.
     */
    private static final int MAX_TRIGGER_INSTANCES = 1_000;

    /**
     * If a non-aggregated diagram (e.g. a line graph) has more than DIAGRAM_SIZE_LIMIT values, a
     * warning will be shown to the user because the diagram output may cause an SVG
     * "Huge input lookup" error.
     */
    private static final int DIAGRAM_SIZE_LIMIT = 10_000;

    private static final String DIAGRAM_FILE_PREFIX = "diagram_";

    private static final String DIAGRAM_FILE_EXTENSION = ".svg";

    /**
     * Create an application controller.
     * 
     * The application controller creates a RController to manage the R connection and a
     * MeasurementView to open the main SWT dialog.
     */
    public Controller(FilterView view, FilterSelectionModel selectionModel, FilterModel model) {
        this.view = view;
        this.selectionModel = selectionModel;
        this.model = model;

        rCtrl = new RController(model, selectionModel);

        // observe model to disable empty controls (enable non-empty controls, respectively)
        model.addPropertyChangeListener(new DisableEmptyControlsHandler());
        model.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                boolean enabled = model.getSimulationTimeMax() > model.getSimulationTimeMin();
                view.enableSimulationTimeComposite(enabled);
            }
        });

        // observe selection model and react to selection events
        selectionModel.addPropertyChangeListener(selectionHandler);

        // observe selection model to enable/disable plot button according to current selection
        selectionModel.addPropertyChangeListener(event -> enableOrDisablePlotButton());
    }

    public final void viewInitialized() {
        // initial population
        withBusyCursor(() -> reload());

        // setup handler that triggers reload on connect/disconnect events
        reloadOnConnectOrDisconnect();
    }

    public final void reload() {
        model.clear();
        selectionModel.clear();

        if (rCtrl.isConnected()) {
            rCtrl.initialize();

            reloadMeasurementsCount();
            reloadMemoryConsumption();
            reloadDiagramTypes();
            selectionHandler.reloadMetrics();
            reloadSimulationTimeBounds();

            selectFirstMetric();
            selectFirstDiagramType();
        } else {
            view.setMeasurementsCount(0);
            view.setMemoryConsmption(0);
        }
    }

    private void reloadMeasurementsCount() {
        int count = rCtrl.getMeasurementsCount();
        view.setMeasurementsCount(count);
    }

    private void reloadMemoryConsumption() {
        int consumption = rCtrl.getMemoryConsumptionInMB();
        view.setMemoryConsmption(consumption);
    }

    /**
     * Trigger time span reset.
     * 
     * Default values from RDS file will be delegated to the {@link gui.FilterViewController}.
     */
    private final void reloadSimulationTimeBounds() {
        int min = rCtrl.getSimulationTimeMin();
        int max = rCtrl.getSimulationTimeMax();
        model.setSimulationTimeMin(min);
        model.setSimulationTimeMax(max);

        selectionModel.setSimulationTimeLower(min);
        selectionModel.setSimulationTimeUpper(max);
    }

    private void reloadDiagramTypes() {
        List<TranslatableEntity> diagramTypes = new ArrayList<>();
        for (DiagramType type : DiagramType.values()) {
            // TODO translation unused, currently
            diagramTypes.add(new TranslatableEntity(type.name(), type.getName()));
        }
        model.setDiagramTypes(diagramTypes);
    }

    private void enableOrDisablePlotButton() {
        Entity mpFrom = selectionModel.getMeasuringPointFrom();
        Entity mpTo = selectionModel.getMeasuringPointTo();

        boolean hasMeasuringPointTo = model.getMeasuringPointsTo() == null ? false
                : !model.getMeasuringPointsTo().isEmpty();

        if (mpFrom != null && !hasMeasuringPointTo) {
            view.enablePlotButton(true);
        } else if (mpFrom != null && mpTo != null) {
            view.enablePlotButton(true);
        } else {
            view.enablePlotButton(false);
        }
    }

    private void selectFirstMetric() {
        List<TranslatableEntity> metrics = model.getMetrics();
        if (metrics != null && !metrics.isEmpty()) {
            selectionModel.setMetric(metrics.get(0));
        }
    }

    private void selectFirstDiagramType() {
        List<TranslatableEntity> types = model.getDiagramTypes();
        if (types != null && !types.isEmpty()) {
            selectionModel.setDiagramType(types.get(0));
        }
    }

    public final void resetSimulationTimeBounds() {
        int min = model.getSimulationTimeMin();
        int max = model.getSimulationTimeMax();

        selectionModel.setSimulationTimeLower(min);
        selectionModel.setSimulationTimeUpper(max);
    }

    public void clearSelectionTriggerTypes() {
        clearSelectionTriggerInstances();
        selectionModel.setTriggerType(null);
    }

    public void clearSelectionTriggerInstances() {
        selectionModel.setTriggerInstance(null);
    }

    /**
     * Plots the diagram according to the current {@link FilterSelectionModel}. The generated
     * diagram will be opened in a new {@link DiagramView}.
     */
    public final void plotDiagram() {
        withBusyCursor(() -> plotDiagramInternal());
    }

    private void plotDiagramInternal() {
        TranslatableEntity selectedDiagramType = selectionModel.getDiagramType();
        DiagramType diagramType = DiagramType.valueOf(selectedDiagramType.getName());

        // ensure that lower simulation time <= upper simulation time
        throwExceptionOnIncorrectSimulationTimeBounds();

        // Check whether the SVG output gets too huge and show warning.
        int diagramSize = rCtrl.getNumberOfDiagramValues();
        LOG.trace("DIAGRAM SIZE: " + diagramSize);

        if (!diagramType.isAggregating() && diagramSize > DIAGRAM_SIZE_LIMIT) {

            MessageDialog dialog = new MessageDialog(Display.getCurrent().getActiveShell(), "Huge diagram output", null,
                    "The diagram plot will contain more than " + DIAGRAM_SIZE_LIMIT + " values (" + diagramSize
                            + "). This may cause an SVG 'Huge output " + "error'. Do you want to continue or "
                            + "restrict the time span to reduce the number " + "of values used for the diagram plot?",
                    MessageDialog.WARNING, new String[] { "No, cancel diagram plot", "Yes, plot diagram" }, 0);

            int result = dialog.open();

            // Cancel plot process if user selected 'No'.
            if (result == 0) {
                return;
            }

        }

        // Save the diagram image in temp directory.
        String diagramPath = createTemporaryDiagramFilePath();
        LOG.trace("DIAGRAM PATH: " + diagramPath);

        // plot diagram to temporary file
        String title = createShortDiagramTitle();
        String subTitle = createDiagramSubTitle();
        String plotCommand = rCtrl.plotDiagramToFile(diagramType, diagramPath, title, subTitle);

        // Open new view to display the diagram.
        String viewTitle = createDiagramTitle();
        try {
            openDiagramView(viewTitle, diagramPath, plotCommand);
        } catch (PartInitException e) {
            LOG.error("Could not open diagram view", e);
        }
    }

    private void throwExceptionOnIncorrectSimulationTimeBounds() {
        int lower = selectionModel.getSimulationTimeLower();
        int upper = selectionModel.getSimulationTimeUpper();
        if (lower > upper) {
            throw new RuntimeException(
                    "Cannot plot diagram with simulation time lower bound being greater than upper bound");
        }
    }

    private String createTemporaryDiagramFilePath() {
        File tempFile = null;
        try {
            tempFile = File.createTempFile(DIAGRAM_FILE_PREFIX, DIAGRAM_FILE_EXTENSION);
        } catch (IOException e) {
            throw new RuntimeException("Could not create temporary file for diagram image", e);
        }
        String imagePath = tempFile.getAbsolutePath();

        // Replace '\' with '/' because R cannot handle backslashes.
        imagePath = imagePath.replaceAll(Matcher.quoteReplacement("\\"), "/");

        return imagePath;
    }

    private void openDiagramView(String title, String imagePath, String rCommand) throws PartInitException {
        DiagramView view = (DiagramView) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
                .showView(DiagramView.ID, title, IWorkbenchPage.VIEW_ACTIVATE);
        view.setViewTitle(title);
        view.setDiagramImage(imagePath);
        view.setRCommandString(rCommand);
    }

    /**
     * Create the diagram title based on the filter settings.
     * 
     * @return Diagram title.
     */
    private String createDiagramTitle() {

        String diagramTitle = "";

        diagramTitle += createShortDiagramTitle();

        DiagramType diagramType = DiagramType.valueOf(selectionModel.getDiagramType().getName());
        String diagramName = diagramType.getShortName();
        try {
            diagramTitle += " [" + diagramName + "]";
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
        Entity mp = selectionModel.getMeasuringPointFrom();

        // diagramTitle += GUIStrings.getGUIString(GUIStrings.getMetrics(), viewCtrl.getMetric());
        diagramTitle += selectionModel.getMetric().getTranslation();

        diagramTitle += " of " + mp;

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
        diagramSubTitle += selectionModel.getSimulationTimeLower();
        diagramSubTitle += " - ";
        diagramSubTitle += selectionModel.getSimulationTimeUpper();

        return diagramSubTitle;
    }

    private void reloadOnConnectOrDisconnect() {
        ConnectionStatusListener statusListener = new AbstractConnectionStatusListener() {

            @Override
            public void connected() {
                view.getDisplay().asyncExec(() -> withBusyCursor(() -> reload()));
            }

            @Override
            public void disconnected() {
                view.getDisplay().asyncExec(() -> withBusyCursor(() -> reload()));
            }

        };
        if (ConnectionRegistry.instance().getConnection() == null) {
            ConnectionRegistry.instance().addListener(new ConnectionListener() {

                @Override
                public void connectionRemoved(RserveConnection connection) {
                    connection.removeListener(statusListener);
                }

                @Override
                public void connectionAdded(RserveConnection connection) {
                    connection.addListener(statusListener);
                }

            });
        } else {
            ConnectionRegistry.instance().getConnection().addListener(statusListener);
        }
    }

    public void withBusyCursor(Procedure p) {
        Display display = view.getDisplay();
        Shell shell = display.getActiveShell();
        try {
            if (shell != null) {
                shell.setCursor(display.getSystemCursor(SWT.CURSOR_WAIT));
            }
            p.execute();
        } finally {
            if (shell != null) {
                shell.setCursor(display.getSystemCursor(SWT.CURSOR_ARROW));
            }
        }
    }

    private final class DisableEmptyControlsHandler implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            boolean enable = evt.getNewValue() != null ? true : false;
            switch (evt.getPropertyName()) {
            case FilterModel.METRICS_PROPERTY:
                view.enableMetricsCombo(enable);
                break;
            case FilterModel.TRIGGER_TYPES_PROPERTY:
                view.enableTriggerTypeCombo(enable);
                break;
            case FilterModel.TRIGGER_INSTANCES_PROPERTY:
                view.enableTriggerInstanceCombo(enable);
                break;
            case FilterModel.ASSEMBLY_CONTEXTS_PROPERTY:
                view.enableAssemblyContextCombo(enable);
                break;
            case FilterModel.MEASURING_POINTS_FROM_PROPERTY:
                view.enableMeasuringPointsFromCombo(enable);
                break;
            case FilterModel.MEASURING_POINTS_TO_PROPERTY:
                view.enableMeasuringPointsToCombo(enable);
                break;
            case FilterModel.SIMULATION_TIME_MAX_PROPERTY:
                break;
            }
        }
    }

    private final class SelectionHandler implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            switch (evt.getPropertyName()) {
            case FilterSelectionModel.METRIC_PROPERTY:
                withBusyCursor(() -> metricSelected());
                break;
            case FilterSelectionModel.TRIGGER_TYPE_PROPERTY:
                withBusyCursor(() -> triggerTypeSelected());
                break;
            case FilterSelectionModel.TRIGGER_INSTANCE_PROPERTY:
                withBusyCursor(() -> triggerInstanceSelected());
                break;
            case FilterSelectionModel.TRIGGER_INSTANCE_SELECTION_ENABLED:
                withBusyCursor(() -> triggerInstanceSelectionEnabledOrDisabled());
                break;
            case FilterSelectionModel.ASSEMBLY_CONTEXT_PROPERTY:
                withBusyCursor(() -> assemblyContextSelected());
                break;
            case FilterSelectionModel.MEASURING_POINT_FROM_PROPERTY:
                withBusyCursor(() -> measuringPointFromSelected());
                break;
            case FilterSelectionModel.MEASURING_POINT_TO_PROPERTY:
                withBusyCursor(() -> measuringPointToSelected());
                break;
            case FilterSelectionModel.SIMULATION_TIME_LOWER_PROPERTY:
                withBusyCursor(() -> simulationTimeLowerChanged());
                break;
            case FilterSelectionModel.SIMULATION_TIME_UPPER_PROPERTY:
                withBusyCursor(() -> simulationTimeUpperChanged());
                break;
            }
        }

        public final void metricSelected() {
            reloadTriggerTypes();
            reloadTriggerInstances();
            reloadAssemblyContexts();
            reloadMeasuringPointsFrom();
            reloadMeasuringPointsTo();
        }

        public void measuringPointFromSelected() {
            reloadMeasuringPointsTo();

            // select first measuring point "to", if any
            if (model.getMeasuringPointsTo() != null && !model.getMeasuringPointsTo().isEmpty()) {
                selectionModel.setMeasuringPointTo(model.getMeasuringPointsTo().get(0));
            }
        }

        public void measuringPointToSelected() {
            // TODO
        }

        public final void triggerTypeSelected() {
            reloadTriggerInstances();
            reloadAssemblyContexts();
            reloadMeasuringPointsFrom();
            reloadMeasuringPointsTo();
        }

        public final void triggerInstanceSelected() {
            // do nothing
        }

        public final void assemblyContextSelected() {
            reloadMeasuringPointsFrom();
            reloadMeasuringPointsTo();
        }

        public void simulationTimeUpperChanged() {
            reloadTriggerInstances();
        }

        public void simulationTimeLowerChanged() {
            reloadTriggerInstances();
        }

        private void triggerInstanceSelectionEnabledOrDisabled() {
            reloadTriggerInstances();

            // hide trigger warning, if visible
            if (!selectionModel.isTriggerInstanceSelectionEnabled()) {
                view.showTriggerWarning(false, 0, 0);
            }
        }

        private void reloadMetrics() {
            List<TranslatableEntity> metrics = rCtrl.getMetrics();
            model.setMetrics(metrics);
        }

        private void reloadMeasuringPointsFrom() {
            List<Entity> measuringPointsFrom = rCtrl.getMeasuringPointsFrom();
            model.setMeasuringPointsFrom(measuringPointsFrom);
        }

        private void reloadMeasuringPointsTo() {
            List<Entity> measuringPointsTo = rCtrl.getMeasuringPointsTo();
            model.setMeasuringPointsTo(measuringPointsTo);
        }

        private void reloadTriggerTypes() {
            List<TranslatableEntity> triggerTypes = rCtrl.getTriggerTypes();
            model.setTriggerTypes(triggerTypes);
        }

        private void reloadTriggerInstances() {
            model.setTriggerInstances(null);
            TranslatableEntity triggerType = selectionModel.getTriggerType();

            if (!selectionModel.isTriggerInstanceSelectionEnabled()) {
                return;
            }

            // return if no trigger type is selected.
            if (triggerType == null) {
                return;
            }

            // calculate number of trigger instances under current filter settings
            int numTriggerInstances = rCtrl.getNumberOfTriggerInstances();

            // Return if no trigger instances are available.
            if (numTriggerInstances == 0) {
                return;
            }

            if (numTriggerInstances <= MAX_TRIGGER_INSTANCES) {
                view.showTriggerWarning(false, MAX_TRIGGER_INSTANCES, numTriggerInstances);
                // view.enableTriggerInstanceComboBox(true); // TODO necessary?
                // view.setTriggerInstanceNumber(Integer.toString(numTriggerInstances));

                // Set trigger instances.
                List<Entity> triggerInstances = rCtrl.getTriggerInstances();
                model.setTriggerInstances(triggerInstances);
                // viewCtrl.setTriggerInstances(triggerInstances);

            } else {
                // view.setTriggerInstanceNumber(Integer.toString(numTriggerInstances), true);
                model.setTriggerInstances(null);

                // Show user info with the number of available instances.
                view.showTriggerWarning(true, MAX_TRIGGER_INSTANCES, numTriggerInstances);
            }
        }

        private void reloadAssemblyContexts() {
            List<Entity> assemblyContexts = rCtrl.getAssemblyContexts();
            model.setAssemblyContexts(assemblyContexts);
        }

    }

}
