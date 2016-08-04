package edu.kit.ipd.sdq.eventsim.rvisualization;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import edu.kit.ipd.sdq.eventsim.measurement.Metadata;
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
import edu.kit.ipd.sdq.eventsim.rvisualization.views.ViewUtils;

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
        DiagramController diagramCtrl = new DiagramController(model, selectionModel, rCtrl);
        withBusyCursor(() -> diagramCtrl.plotDiagram());
    }

    private void withBusyCursor(Procedure p) {
        ViewUtils.withBusyCursor(p);
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
                enable = model.getSimulationTimeMax() > model.getSimulationTimeMin();
                view.enableSimulationTimeComposite(enable);
                break;
            case FilterModel.DIAGRAM_TYPES_PROPERTY:
                view.enableDiagramTypeCombo(enable);
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
            reloadMetadata();
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

            // return if trigger instance selection has not been enabled by the user
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
                // hide trigger warning, if shown
                view.showTriggerWarning(false, MAX_TRIGGER_INSTANCES, numTriggerInstances);

                // reload trigger instances from R
                List<Entity> triggerInstances = rCtrl.getTriggerInstances();
                model.setTriggerInstances(triggerInstances);

            } else {
                model.setTriggerInstances(null);

                // show trigger warning with the number of trigger instances currently selected
                view.showTriggerWarning(true, MAX_TRIGGER_INSTANCES, numTriggerInstances);
            }
        }

        private void reloadAssemblyContexts() {
            List<Entity> assemblyContexts = rCtrl.getAssemblyContexts();
            model.setAssemblyContexts(assemblyContexts);
        }

        private void reloadMetadata() {
            view.clearMetadataExpandItems();
            model.setMetadataTypes(rCtrl.getMetadataNames());

            // add one expand item per metadata type (identified by its name)
            for (TranslatableEntity metadataType : model.getMetadataTypes()) {
                List<Metadata> metadataLevels = rCtrl.getMetadata(metadataType.getName());
                if (!metadataLevels.isEmpty()) {
                    view.createMetadataExpandItem(metadataType, metadataLevels);
                }
            }
        }

    }

    public void metadataSelectionChanged(TranslatableEntity metadataType, Metadata metadataLevel) {
        Map<TranslatableEntity, String> metadataMap = new HashMap<>();
        if (selectionModel.getMetadata() != null) {
            metadataMap.putAll(selectionModel.getMetadata());
        }
        
        metadataMap.put(metadataType, metadataLevel.getValue().toString());
        selectionModel.setMetadata(metadataMap);
    }

    public void metadataSelectionCleared() {
        selectionModel.setMetadata(null);
    }

}
