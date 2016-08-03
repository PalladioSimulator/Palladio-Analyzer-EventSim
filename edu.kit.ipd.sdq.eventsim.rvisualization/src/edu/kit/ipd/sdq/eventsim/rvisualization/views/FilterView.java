package edu.kit.ipd.sdq.eventsim.rvisualization.views;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.beans.PojoObservables;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.databinding.viewers.ViewerProperties;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ExpandEvent;
import org.eclipse.swt.events.ExpandListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.wb.swt.ResourceManager;
import org.eclipse.wb.swt.SWTResourceManager;

import edu.kit.ipd.sdq.eventsim.measurement.Metadata;
import edu.kit.ipd.sdq.eventsim.rvisualization.Controller;
import edu.kit.ipd.sdq.eventsim.rvisualization.model.Entity;
import edu.kit.ipd.sdq.eventsim.rvisualization.model.FilterModel;
import edu.kit.ipd.sdq.eventsim.rvisualization.model.FilterSelectionModel;
import edu.kit.ipd.sdq.eventsim.rvisualization.model.TranslatableEntity;
import edu.kit.ipd.sdq.eventsim.rvisualization.util.Helper;
import swing2swt.layout.BorderLayout;

/**
 * Plug-ins main view for showing filter options.
 * 
 * @author Benjamin Rupp
 * @author Philipp Merkle
 *
 */
public class FilterView extends ViewPart {

    private DataBindingContext m_bindingContext;

    /**
     * The ID of the view as specified by the extension.
     */
    public static final String ID = "edu.kit.ipd.sdq.eventsim.rvisualization.filterview";

    private Controller ctrl;

    private Composite viewParent;

    /**
     * Maximum of simulation time span values.
     */
    private static final int TIMESPAN_MAX_VALUE = 1_000_000;

    private ComboViewer cmbMetric;
    private ComboViewer cmbTriggerType;
    private ComboViewer cmbTriggerInstance;
    private ComboViewer cmbAssemblyCtx;
    private ComboViewer cmbMeasuringPointFrom;
    private ComboViewer cmbMeasuringPointTo;
    private ComboViewer cmbDiagramType;
    private Spinner spnTimeSpanFrom;
    private Spinner spnTimeSpanTo;
    private Button btnTimespanReset;
    private Button btnPlot;
    private Label lblMeasurementsCount;

    private Composite cmpMetric;
    private Composite cmpTrigger;
    private Composite cmpAssemblyCtx;
    private Composite cmpMeasuringPoints;
    private Composite cmpTimeSpan;
    private Composite cmpCenter;

    private Group grpTriggerTypes;
    private Group grpTriggerInstances;

    private Button btnClearTriggerTypes;

    private Button btnClearTriggerInstances;
    private Scale scaleLower;
    private Scale scaleUpper;

    private ExpandItem xpiTrigger;
    private Text txtTriggerWarning1;
    private Text txtTriggerWarning2;
    private Text txtReduceSimulationTime;
    private Composite cmpTriggerWarning;

    private Button btnClearAssemblyContext;

    private FilterSelectionModel selectionModel;
    private FilterModel model;
    private Label lblMemoryConsumption;
    private Label lblMemory;
    private Button btnEnableTriggerInstance;

    private ExpandBar expandBar;

    private Map<String, ExpandItem> xpiMetadataMap = new HashMap<>();

    public FilterView() {
        this.selectionModel = new FilterSelectionModel();
        this.model = new FilterModel();
    }

    /**
     * This is a callback that will allow us to create the UI and initialize it.
     * 
     * @param parent
     *            Parent UI element.
     */
    public final void createPartControl(final Composite parent) {
        viewParent = parent;

        Composite cmpRoot = new Composite(parent, SWT.NONE);
        cmpRoot.setLayout(new BorderLayout(0, 0));

        createNorthBar(cmpRoot);
        createExpandBar(cmpRoot);
        createSouthBar(cmpRoot);

        showTriggerWarning(false, 0, 0);
        enableMetricsCombo(false);
        enableTriggerTypeCombo(false);
        enableTriggerInstanceCombo(false);
        enableAssemblyContextCombo(false);
        enableMeasuringPointsFromCombo(false);
        enableMeasuringPointsToCombo(false);
        enablePlotButton(false);

        // create controller
        ctrl = new Controller(this, selectionModel, model);
        ctrl.viewInitialized();

        initDataBindings();

        addEventHandler();
    }

    /**
     * Passing the focus request.
     */
    public final void setFocus() {
        viewParent.setFocus();
    }

    public Controller getController() {
        return ctrl;
    }

    public Display getDisplay() {
        return viewParent != null ? viewParent.getDisplay() : Display.getDefault();
    }

    public void setMeasurementsCount(int count) {
        String formatted = String.format(Locale.US, "%,d", count);
        this.lblMeasurementsCount.setText(formatted);
    }

    public void setMemoryConsmption(int megabytes) {
        String formatted = String.format(Locale.US, "%,d", megabytes);
        this.lblMemory.setText(formatted + " MB");
    }

    /**
     * 
     * @param show
     * @param max
     *            the upper limit of triggers allowed to be displayed
     * @param current
     *            the current number of triggers within the selected simulation time span
     */
    public void showTriggerWarning(boolean show, int max, int current) {
        cmpTriggerWarning.setVisible(show);
        if (show) {
            txtTriggerWarning1.setText("Cannot display more than " + max + " triggers.");
            txtTriggerWarning2.setText("Triggers within time span: " + current);
        }
    }

    public void enableMetricsCombo(boolean enabled) {
        cmbMetric.getCombo().setEnabled(enabled);
    }

    public final void enableTriggerTypeCombo(boolean enabled) {
        cmbTriggerType.getCombo().setEnabled(enabled);
        btnClearTriggerTypes.setEnabled(enabled);
    }

    public void enableTriggerInstanceCombo(boolean enabled) {
        cmbTriggerInstance.getCombo().setEnabled(enabled);
        btnClearTriggerInstances.setEnabled(enabled);
    }

    public void enableAssemblyContextCombo(boolean enabled) {
        cmbAssemblyCtx.getCombo().setEnabled(enabled);
        btnClearAssemblyContext.setEnabled(enabled);

    }

    public void enableMeasuringPointsFromCombo(boolean enabled) {
        cmbMeasuringPointFrom.getCombo().setEnabled(enabled);
    }

    public void enableMeasuringPointsToCombo(boolean enabled) {
        cmbMeasuringPointTo.getCombo().setEnabled(enabled);
    }

    public void enableSimulationTimeComposite(boolean enabled) {
        Helper.setEnabledRecursive(cmpTimeSpan, enabled);
    }

    public void enableDiagramTypeCombo(boolean enable) {
        cmbDiagramType.getCombo().setEnabled(enable);
    }

    public void enablePlotButton(boolean enabled) {
        btnPlot.setEnabled(enabled);
    }

    private void createNorthBar(Composite cmpRoot) {
        Composite cmpNorth = new Composite(cmpRoot, SWT.NONE);
        cmpNorth.setLayoutData(BorderLayout.NORTH);
        cmpNorth.setLayout(new GridLayout(2, false));

        Label lblNumberOfMeasurements = new Label(cmpNorth, SWT.NONE);
        lblNumberOfMeasurements.setText("Number of measurements:");

        lblMeasurementsCount = new Label(cmpNorth, SWT.NONE);
        lblMeasurementsCount.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        lblMeasurementsCount.setText("0");

        lblMemoryConsumption = new Label(cmpNorth, SWT.NONE);
        lblMemoryConsumption.setText("Memory consumption:");

        lblMemory = new Label(cmpNorth, SWT.NONE);
        lblMemory.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        lblMemory.setText("0");
    }

    private void createSouthBar(Composite cmpRoot) {
        Composite cmpSouth = new Composite(cmpRoot, SWT.NONE);
        cmpSouth.setLayoutData(BorderLayout.SOUTH);
        cmpSouth.setLayout(new GridLayout(2, false));

        Label lblDiagramType = new Label(cmpSouth, SWT.NONE);
        lblDiagramType.setText("Diagram Type:");

        cmbDiagramType = new ComboViewer(cmpSouth, SWT.READ_ONLY);
        cmbDiagramType.getCombo().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        btnPlot = new Button(cmpSouth, SWT.NONE);
        btnPlot.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
        btnPlot.setImage(
                ResourceManager.getPluginImage("edu.kit.ipd.sdq.eventsim.rvisualization", "icons/chart_bar.png"));
        btnPlot.setText("Plot Diagram");
        m_bindingContext = initDataBindings();
    }

    private void createExpandBar(Composite cmpRoot) {
        cmpCenter = new Composite(cmpRoot, SWT.NONE);
        cmpCenter.setLayoutData(BorderLayout.CENTER);
        cmpCenter.setLayout(new FillLayout(SWT.HORIZONTAL));

        expandBar = new ExpandBar(cmpCenter, SWT.V_SCROLL);
        expandBar.addExpandListener(new ExpandListener() {
            @Override
            public void itemExpanded(final ExpandEvent e) {
                layoutView();
            }

            @Override
            public void itemCollapsed(final ExpandEvent e) {
                layoutView();
            }
        });

        createMetricExpandItem();
        createSimulationTimeExpandItem();
        createTriggerExpandItem();
        createAssemblyContextExpandItem();
        createMeasuringPointsExpandItem();
    }

    private void createSimulationTimeExpandItem() {
        ExpandItem xpiSimulationTime = new ExpandItem(expandBar, SWT.NONE);
        xpiSimulationTime.setText("When: Simulation Time Bounds");

        cmpTimeSpan = new Composite(expandBar, SWT.NONE);
        xpiSimulationTime.setControl(cmpTimeSpan);
        xpiSimulationTime.setHeight(150);
        GridLayout glCompositeTimeSpan = new GridLayout(2, false);
        cmpTimeSpan.setLayout(glCompositeTimeSpan);

        Label lblFrom = new Label(cmpTimeSpan, SWT.NONE);
        lblFrom.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        lblFrom.setText("Lower bound:");

        Label lblTo = new Label(cmpTimeSpan, SWT.NONE);
        lblTo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        lblTo.setText("Upper bound:");

        scaleLower = new Scale(cmpTimeSpan, SWT.NONE);
        scaleLower.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        scaleUpper = new Scale(cmpTimeSpan, SWT.NONE);
        scaleUpper.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        spnTimeSpanFrom = new Spinner(cmpTimeSpan, SWT.BORDER);
        spnTimeSpanFrom.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        spnTimeSpanFrom.setMaximum(TIMESPAN_MAX_VALUE);

        spnTimeSpanTo = new Spinner(cmpTimeSpan, SWT.BORDER);
        spnTimeSpanTo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        spnTimeSpanTo.setMaximum(TIMESPAN_MAX_VALUE);

        keepSameHeight(cmpTimeSpan, xpiSimulationTime);
        new Label(cmpTimeSpan, SWT.NONE);

        btnTimespanReset = new Button(cmpTimeSpan, SWT.NONE);
        btnTimespanReset.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        btnTimespanReset.setText("Reset");
    }

    private void createMeasuringPointsExpandItem() {
        ExpandItem xpiMeasuringPoints = new ExpandItem(expandBar, SWT.NONE);
        xpiMeasuringPoints.setExpanded(true);
        xpiMeasuringPoints.setText("Where: Measuring Point / Range");

        cmpMeasuringPoints = new Composite(expandBar, SWT.NONE);
        xpiMeasuringPoints.setControl(cmpMeasuringPoints);
        xpiMeasuringPoints.setHeight(100);
        GridLayout gl_cmpMeasuringPoints = new GridLayout(2, false);
        gl_cmpMeasuringPoints.marginBottom = 10;
        gl_cmpMeasuringPoints.marginTop = 10;
        cmpMeasuringPoints.setLayout(gl_cmpMeasuringPoints);

        Label lblMPFrom = new Label(cmpMeasuringPoints, SWT.NONE);
        lblMPFrom.setText("From:");

        cmbMeasuringPointFrom = new ComboViewer(cmpMeasuringPoints, SWT.READ_ONLY);
        cmbMeasuringPointFrom.getCombo().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        Label lblMPTo = new Label(cmpMeasuringPoints, SWT.NONE);
        lblMPTo.setText("To:");

        cmbMeasuringPointTo = new ComboViewer(cmpMeasuringPoints, SWT.READ_ONLY);
        cmbMeasuringPointTo.getCombo().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        keepSameHeight(cmpMeasuringPoints, xpiMeasuringPoints);
    }

    private void createAssemblyContextExpandItem() {
        cmpAssemblyCtx = new Composite(expandBar, SWT.NONE);
        GridLayout gl_cmpAssemblyCtx = new GridLayout(2, false);
        gl_cmpAssemblyCtx.marginBottom = 10;
        gl_cmpAssemblyCtx.marginTop = 10;
        cmpAssemblyCtx.setLayout(gl_cmpAssemblyCtx);

        ExpandItem xpiAssemblyCtx = new ExpandItem(expandBar, 0);
        xpiAssemblyCtx.setText("Where: Assembly Context");
        xpiAssemblyCtx.setControl(cmpAssemblyCtx);
        xpiAssemblyCtx.setHeight(100);

        cmbAssemblyCtx = new ComboViewer(cmpAssemblyCtx, SWT.READ_ONLY);
        cmbAssemblyCtx.getCombo().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        btnClearAssemblyContext = new Button(cmpAssemblyCtx, SWT.NONE);
        btnClearAssemblyContext.setText("Clear");
        btnClearAssemblyContext.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                cmbAssemblyCtx.setSelection(StructuredSelection.EMPTY);
            }
        });

        keepSameHeight(cmpAssemblyCtx, xpiAssemblyCtx);
    }

    private void createTriggerExpandItem() {
        cmpTrigger = new Composite(expandBar, SWT.NONE);
        GridLayout gl_cmpTrigger = new GridLayout(1, false);
        cmpTrigger.setLayout(gl_cmpTrigger);

        xpiTrigger = new ExpandItem(expandBar, SWT.NONE);
        xpiTrigger.setText("Who: Trigger");
        xpiTrigger.setControl(cmpTrigger);
        xpiTrigger.setHeight(250);

        keepSameHeight(cmpTrigger, xpiTrigger);

        createTriggerTypesGroup(cmpTrigger);
        createTriggerInstancesGroup(cmpTrigger);
    }

    private void createTriggerInstancesGroup(Composite compositeTrigger) {
        grpTriggerInstances = new Group(compositeTrigger, SWT.NONE);
        grpTriggerInstances.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
        grpTriggerInstances.setText("Trigger instance");
        GridLayout gl_grpTriggerInstances = new GridLayout(2, false);
        grpTriggerInstances.setLayout(gl_grpTriggerInstances);

        btnEnableTriggerInstance = new Button(grpTriggerInstances, SWT.CHECK);
        btnEnableTriggerInstance.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
        btnEnableTriggerInstance.setText("Enable trigger instance selection");

        cmbTriggerInstance = new ComboViewer(grpTriggerInstances, SWT.READ_ONLY);
        cmbTriggerInstance.getCombo().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        btnClearTriggerInstances = new Button(grpTriggerInstances, SWT.NONE);
        btnClearTriggerInstances.setText("Clear");

        cmpTriggerWarning = new Composite(grpTriggerInstances, SWT.NONE);
        FillLayout fl_cmpTriggerWarning = new FillLayout(SWT.VERTICAL);
        fl_cmpTriggerWarning.spacing = 3;
        cmpTriggerWarning.setLayout(fl_cmpTriggerWarning);
        cmpTriggerWarning.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

        txtTriggerWarning1 = new Text(cmpTriggerWarning, SWT.READ_ONLY);
        txtTriggerWarning1.setText("Cannot display more than <x> triggers.");

        txtTriggerWarning2 = new Text(cmpTriggerWarning, SWT.READ_ONLY);
        txtTriggerWarning2.setText("Triggers within time span: <y>");

        txtReduceSimulationTime = new Text(cmpTriggerWarning, SWT.READ_ONLY);
        txtReduceSimulationTime.setFont(SWTResourceManager.getFont("Segoe UI", 9, SWT.BOLD));
        txtReduceSimulationTime.setText("Reduce simulation time span.");
        txtTriggerWarning1.addListener(SWT.Modify, event -> {
            adjustHeight(cmpTrigger, xpiTrigger);
        });
    }

    private void createTriggerTypesGroup(Composite compositeTrigger) {
        grpTriggerTypes = new Group(compositeTrigger, SWT.NONE);
        grpTriggerTypes.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1));
        grpTriggerTypes.setText("Trigger type");
        grpTriggerTypes.setLayout(new GridLayout(2, false));

        cmbTriggerType = new ComboViewer(grpTriggerTypes, SWT.READ_ONLY);
        cmbTriggerType.getCombo().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        btnClearTriggerTypes = new Button(grpTriggerTypes, SWT.NONE);
        btnClearTriggerTypes.setText("Clear");
    }

    private void createMetricExpandItem() {
        cmpMetric = new Composite(expandBar, SWT.NONE);
        GridLayout gl_cmpMetric = new GridLayout(1, false);
        gl_cmpMetric.marginBottom = 10;
        gl_cmpMetric.marginTop = 10;
        cmpMetric.setLayout(gl_cmpMetric);

        ExpandItem xpiMetric = new ExpandItem(expandBar, SWT.NONE);
        xpiMetric.setExpanded(true);
        xpiMetric.setText("What: Metric");
        xpiMetric.setControl(cmpMetric);
        xpiMetric.setHeight(50);

        cmbMetric = new ComboViewer(cmpMetric, SWT.READ_ONLY);
        cmbMetric.getCombo().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        keepSameHeight(cmpMetric, xpiMetric);
    }

    public void createMetadataExpandItem(String name, List<Metadata> metadataLevels) {
        Composite cmpMetadata = new Composite(expandBar, SWT.NONE);
        GridLayout gl_cmpMetadata = new GridLayout(1, false);
        gl_cmpMetadata.marginBottom = 10;
        gl_cmpMetadata.marginTop = 10;
        cmpMetadata.setLayout(gl_cmpMetadata);

        ExpandItem xpiMetadata = new ExpandItem(expandBar, SWT.NONE);
        xpiMetadata.setExpanded(true);
        xpiMetadata.setText("Metadata: " + name);
        xpiMetadata.setControl(cmpMetadata);
        xpiMetadata.setHeight(50); // TODO needed until keepSameHeight works (see below)

        ComboViewer cmbMetadata = new ComboViewer(cmpMetadata, SWT.READ_ONLY);
        cmbMetadata.getCombo().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        cmbMetadata.setContentProvider(ArrayContentProvider.getInstance());
        cmbMetadata.setInput(metadataLevels);
        cmbMetadata.setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
                Metadata m = (Metadata) element;
                return m.getValue().toString();
            }
        });

        cmpMetadata.layout();

        // TODO doesn't work currently. Why?
        keepSameHeight(cmpMetadata, xpiMetadata);

        // keep map if expand items to be able to dispose them
        xpiMetadataMap.putIfAbsent(name, xpiMetadata);

        // notify controller once selected item changes
        cmbMetadata.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                StructuredSelection selection = (StructuredSelection) event.getSelection();
                Metadata m = (Metadata) selection.getFirstElement();
                ctrl.metadataSelectionChanged(m);
            }
        });
    }

    public void clearMetadataExpandItems() {
        for (ExpandItem xpi : xpiMetadataMap.values()) {
            ctrl.metadataSelectionCleared();
            xpi.getControl().dispose();
            xpi.dispose();
        }
        xpiMetadataMap.clear();
    }

    /**
     * Add event handler.
     * 
     * Adds event handler for buttons, keys and modify listeners for combo boxes.
     */
    private void addEventHandler() {
        btnPlot.addListener(SWT.Selection, l -> ctrl.plotDiagram());
        btnTimespanReset.addListener(SWT.Selection, l -> ctrl.resetSimulationTimeBounds());
        btnClearTriggerTypes.addListener(SWT.Selection, l -> ctrl.clearSelectionTriggerTypes());
        btnClearTriggerInstances.addListener(SWT.Selection, l -> ctrl.clearSelectionTriggerInstances());
    }

    private void keepSameHeight(Composite observedComposite, ExpandItem exandItemToResize) {
        observedComposite.addListener(SWT.Resize, event -> getDisplay().asyncExec(() -> {
            adjustHeight(observedComposite, exandItemToResize);
        }));
    }

    private void adjustHeight(Composite observedComposite, ExpandItem exandItemToResize) {
        if (observedComposite.isDisposed())
            return;
        Point size = observedComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        if (exandItemToResize.getHeight() != size.y) {
            exandItemToResize.setHeight(size.y);
        }
    }

    /**
     * Re-layout the
     */
    private void layoutView() {
        getDisplay().asyncExec(new Runnable() {
            public void run() {
                viewParent.layout();
            }
        });
    }

    protected DataBindingContext initDataBindings() {
        DataBindingContext bindingContext = new DataBindingContext();
        //
        ObservableListContentProvider listContentProvider = new ObservableListContentProvider();
        IObservableMap[] observeMap = PojoObservables.observeMaps(listContentProvider.getKnownElements(), Entity.class,
                new String[] { "name", "id" });
        cmbMeasuringPointFrom.setLabelProvider(new EntityLabelProvider(observeMap));
        cmbMeasuringPointFrom.setContentProvider(listContentProvider);
        //
        IObservableList measuringPointsFromModelObserveList = BeanProperties.list("measuringPointsFrom").observe(model);
        cmbMeasuringPointFrom.setInput(measuringPointsFromModelObserveList);
        //
        IObservableValue observeSingleSelectionFilterViewgetCmbMeasuringPointFrom = ViewerProperties.singleSelection()
                .observe(cmbMeasuringPointFrom);
        IObservableValue measuringPointFromSelectionModelObserveValue = BeanProperties.value("measuringPointFrom")
                .observe(selectionModel);
        bindingContext.bindValue(observeSingleSelectionFilterViewgetCmbMeasuringPointFrom,
                measuringPointFromSelectionModelObserveValue, null, null);
        //
        ObservableListContentProvider listContentProvider_1 = new ObservableListContentProvider();
        IObservableMap observeMap_1 = PojoObservables.observeMap(listContentProvider_1.getKnownElements(),
                TranslatableEntity.class, "translation");
        cmbMetric.setLabelProvider(new TranslatableEntityLabelProvider(observeMap_1));
        cmbMetric.setContentProvider(listContentProvider_1);
        //
        IObservableList metricsModelObserveList = BeanProperties.list("metrics").observe(model);
        cmbMetric.setInput(metricsModelObserveList);
        //
        IObservableValue observeSingleSelectionFilterViewgetCmbMetric = ViewerProperties.singleSelection()
                .observe(cmbMetric);
        IObservableValue metricSelectionModelObserveValue = BeanProperties.value("metric").observe(selectionModel);
        bindingContext.bindValue(observeSingleSelectionFilterViewgetCmbMetric, metricSelectionModelObserveValue, null,
                null);
        //
        ObservableListContentProvider listContentProvider_2 = new ObservableListContentProvider();
        IObservableMap observeMap_2 = PojoObservables.observeMap(listContentProvider_2.getKnownElements(),
                TranslatableEntity.class, "translation");
        cmbTriggerType.setLabelProvider(new TranslatableEntityLabelProvider(observeMap_2));
        cmbTriggerType.setContentProvider(listContentProvider_2);
        //
        IObservableList triggerTypesModelObserveList = BeanProperties.list("triggerTypes").observe(model);
        cmbTriggerType.setInput(triggerTypesModelObserveList);
        //
        IObservableValue observeSingleSelectionFilterViewgetCmbTriggerType = ViewerProperties.singleSelection()
                .observe(cmbTriggerType);
        IObservableValue triggerTypeSelectionModelObserveValue = BeanProperties.value("triggerType")
                .observe(selectionModel);
        bindingContext.bindValue(observeSingleSelectionFilterViewgetCmbTriggerType,
                triggerTypeSelectionModelObserveValue, null, null);
        //
        ObservableListContentProvider listContentProvider_4 = new ObservableListContentProvider();
        IObservableMap observeMap_4 = PojoObservables.observeMap(listContentProvider_4.getKnownElements(), Entity.class,
                "name");
        cmbAssemblyCtx.setLabelProvider(new EntityLabelProvider(observeMap_4));
        cmbAssemblyCtx.setContentProvider(listContentProvider_4);
        //
        IObservableList assemblyContextsModelObserveList = BeanProperties.list("assemblyContexts").observe(model);
        cmbAssemblyCtx.setInput(assemblyContextsModelObserveList);
        //
        IObservableValue observeSingleSelectionFilterViewgetCmbAssemblyCtx = ViewerProperties.singleSelection()
                .observe(cmbAssemblyCtx);
        IObservableValue assemblyContextSelectionModelObserveValue = BeanProperties.value("assemblyContext")
                .observe(selectionModel);
        bindingContext.bindValue(observeSingleSelectionFilterViewgetCmbAssemblyCtx,
                assemblyContextSelectionModelObserveValue, null, null);
        //
        ObservableListContentProvider listContentProvider_5 = new ObservableListContentProvider();
        IObservableMap observeMap_5 = PojoObservables.observeMap(listContentProvider_5.getKnownElements(), Entity.class,
                "name");
        cmbMeasuringPointTo.setLabelProvider(new EntityLabelProvider(observeMap_5));
        cmbMeasuringPointTo.setContentProvider(listContentProvider_5);
        //
        IObservableList measuringPointsToModelObserveList = BeanProperties.list("measuringPointsTo").observe(model);
        cmbMeasuringPointTo.setInput(measuringPointsToModelObserveList);
        //
        IObservableValue observeSingleSelectionFilterViewgetCmbMeasuringPointTo = ViewerProperties.singleSelection()
                .observe(cmbMeasuringPointTo);
        IObservableValue measuringPointToSelectionModelObserveValue = BeanProperties.value("measuringPointTo")
                .observe(selectionModel);
        bindingContext.bindValue(observeSingleSelectionFilterViewgetCmbMeasuringPointTo,
                measuringPointToSelectionModelObserveValue, null, null);
        //
        IObservableValue observeMinFilterViewgetSpnTimeSpanFromObserveWidget = WidgetProperties.minimum()
                .observe(spnTimeSpanFrom);
        IObservableValue simulationTimeMinModelObserveValue = BeanProperties.value("simulationTimeMin").observe(model);
        bindingContext.bindValue(observeMinFilterViewgetSpnTimeSpanFromObserveWidget,
                simulationTimeMinModelObserveValue, null, null);
        //
        IObservableValue observeMaxFilterViewgetSpnTimeSpanFromObserveWidget = WidgetProperties.maximum()
                .observe(spnTimeSpanFrom);
        IObservableValue simulationTimeUpperSelectionModelObserveValue = BeanProperties.value("simulationTimeUpper")
                .observe(selectionModel);
        bindingContext.bindValue(observeMaxFilterViewgetSpnTimeSpanFromObserveWidget,
                simulationTimeUpperSelectionModelObserveValue, null, null);
        //
        IObservableValue observeSelectionFilterViewgetSpnTimeSpanFromObserveWidget = WidgetProperties.selection()
                .observeDelayed(300, spnTimeSpanFrom);
        IObservableValue simulationTimeLowerSelectionModelObserveValue = BeanProperties.value("simulationTimeLower")
                .observe(selectionModel);
        bindingContext.bindValue(observeSelectionFilterViewgetSpnTimeSpanFromObserveWidget,
                simulationTimeLowerSelectionModelObserveValue, null, null);
        //
        IObservableValue observeMinFilterViewgetSpnTimeSpanToObserveWidget = WidgetProperties.minimum()
                .observe(spnTimeSpanTo);
        bindingContext.bindValue(observeMinFilterViewgetSpnTimeSpanToObserveWidget,
                simulationTimeLowerSelectionModelObserveValue, null, null);
        //
        IObservableValue observeMaxFilterViewgetSpnTimeSpanToObserveWidget = WidgetProperties.maximum()
                .observe(spnTimeSpanTo);
        IObservableValue simulationTimeMaxModelObserveValue = BeanProperties.value("simulationTimeMax").observe(model);
        bindingContext.bindValue(observeMaxFilterViewgetSpnTimeSpanToObserveWidget, simulationTimeMaxModelObserveValue,
                null, null);
        //
        IObservableValue observeSelectionFilterViewgetSpnTimeSpanToObserveWidget = WidgetProperties.selection()
                .observeDelayed(300, spnTimeSpanTo);
        bindingContext.bindValue(observeSelectionFilterViewgetSpnTimeSpanToObserveWidget,
                simulationTimeUpperSelectionModelObserveValue, null, null);
        //
        IObservableValue observeMinFilterViewgetScaleLowerObserveWidget = WidgetProperties.minimum()
                .observe(scaleLower);
        bindingContext.bindValue(observeMinFilterViewgetScaleLowerObserveWidget, simulationTimeMinModelObserveValue,
                null, null);
        //
        IObservableValue observeMaxFilterViewgetScaleLowerObserveWidget = WidgetProperties.maximum()
                .observe(scaleLower);
        bindingContext.bindValue(observeMaxFilterViewgetScaleLowerObserveWidget,
                simulationTimeUpperSelectionModelObserveValue, null, null);
        //
        IObservableValue observeSelectionFilterViewgetScaleLowerObserveWidget = WidgetProperties.selection()
                .observeDelayed(300, scaleLower);
        bindingContext.bindValue(observeSelectionFilterViewgetScaleLowerObserveWidget,
                simulationTimeLowerSelectionModelObserveValue, null, null);
        //
        IObservableValue observeMinFilterViewgetScaleUpperObserveWidget = WidgetProperties.minimum()
                .observe(scaleUpper);
        bindingContext.bindValue(observeMinFilterViewgetScaleUpperObserveWidget,
                simulationTimeLowerSelectionModelObserveValue, null, null);
        //
        IObservableValue observeMaxFilterViewgetScaleUpperObserveWidget = WidgetProperties.maximum()
                .observe(scaleUpper);
        bindingContext.bindValue(observeMaxFilterViewgetScaleUpperObserveWidget, simulationTimeMaxModelObserveValue,
                null, null);
        //
        IObservableValue observeSelectionFilterViewgetScaleUpperObserveWidget = WidgetProperties.selection()
                .observeDelayed(300, scaleUpper);
        bindingContext.bindValue(observeSelectionFilterViewgetScaleUpperObserveWidget,
                simulationTimeUpperSelectionModelObserveValue, null, null);
        //
        ObservableListContentProvider listContentProvider_3 = new ObservableListContentProvider();
        IObservableMap[] observeMaps = PojoObservables.observeMaps(listContentProvider_3.getKnownElements(),
                Entity.class, new String[] { "name", "id" });
        cmbTriggerInstance.setLabelProvider(new EntityLabelProvider(observeMaps));
        cmbTriggerInstance.setContentProvider(listContentProvider_3);
        //
        IObservableList triggerInstancesModelObserveList = BeanProperties.list("triggerInstances").observe(model);
        cmbTriggerInstance.setInput(triggerInstancesModelObserveList);
        //
        IObservableValue observeSingleSelectionCmbTriggerInstance = ViewerProperties.singleSelection()
                .observe(cmbTriggerInstance);
        IObservableValue triggerInstanceSelectionModelObserveValue = BeanProperties.value("triggerInstance")
                .observe(selectionModel);
        bindingContext.bindValue(observeSingleSelectionCmbTriggerInstance, triggerInstanceSelectionModelObserveValue,
                null, null);
        //
        IObservableValue observeSelectionBtnEnableTriggerInstanceObserveWidget = WidgetProperties.selection()
                .observe(btnEnableTriggerInstance);
        IObservableValue triggerInstanceSelectionEnabledSelectionModelObserveValue = BeanProperties
                .value("triggerInstanceSelectionEnabled").observe(selectionModel);
        bindingContext.bindValue(observeSelectionBtnEnableTriggerInstanceObserveWidget,
                triggerInstanceSelectionEnabledSelectionModelObserveValue, null, null);
        //
        ObservableListContentProvider listContentProvider_6 = new ObservableListContentProvider();
        IObservableMap[] observeMaps_1 = PojoObservables.observeMaps(listContentProvider_6.getKnownElements(),
                TranslatableEntity.class, new String[] { "name", "translation" });
        cmbDiagramType.setLabelProvider(new TranslatableEntityLabelProvider(observeMaps_1));
        cmbDiagramType.setContentProvider(listContentProvider_6);
        //
        IObservableList diagramTypesModelObserveList = BeanProperties.list("diagramTypes").observe(model);
        cmbDiagramType.setInput(diagramTypesModelObserveList);
        //
        IObservableValue observeSingleSelectionCmbDiagramType = ViewerProperties.singleSelection()
                .observe(cmbDiagramType);
        IObservableValue diagramTypeSelectionModelObserveValue = BeanProperties.value("diagramType")
                .observe(selectionModel);
        bindingContext.bindValue(observeSingleSelectionCmbDiagramType, diagramTypeSelectionModelObserveValue, null,
                null);
        //
        return bindingContext;
    }
}
