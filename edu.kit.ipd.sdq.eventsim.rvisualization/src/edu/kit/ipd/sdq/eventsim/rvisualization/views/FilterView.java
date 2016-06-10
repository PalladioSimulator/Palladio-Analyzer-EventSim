package edu.kit.ipd.sdq.eventsim.rvisualization.views;

import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ExpandEvent;
import org.eclipse.swt.events.ExpandListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.wb.swt.ResourceManager;

import edu.kit.ipd.sdq.eventsim.rvisualization.Controller;
import edu.kit.ipd.sdq.eventsim.rvisualization.model.Entity;
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

	private Combo cmbMetric;
	private Combo cmbTriggerType;
	private ComboViewer cmbTriggerInstance;
	private ComboViewer cmbAssemblyCtx;
	private ComboViewer cmbMeasuringPointFrom;
	private ComboViewer cmbMeasuringPointTo;
	private Combo cmbDiagramType;

	private Spinner spnTriggerFrom;
	private Spinner spnTriggerTo;
	private Spinner spnTimeSpanFrom;
	private Spinner spnTimeSpanTo;

	private Button btnTriggerReset;
	private Button btnTimespanReset;
	private Button btnPlot;

	private Label lblTriggerInstanceNumber;
	private Label lblTriggerInstanceDescription;
	private Label lblSimulationTimeSpanDescription;

	private Composite cmpMetric;
	private Composite cmpAssemblyCtx;
	private Composite cmpMeasuringPoints;
	private Composite cmpTimeSpan;
	private Composite cmpCenter;

	private Label lblMeasurementsCount;

	private static final LabelProvider ENTITY_LABEL_PROVIDER = new LabelProvider() {

		@Override
		public String getText(Object element) {
			Entity p = (Entity) element;
			return p.toString();
		}

	};

	private Group grpTriggerTypes;

	private Group grpTriggerInstances;

	public FilterView() {
	}

	public Controller getController() {
		return ctrl;
	}

	private Display getDisplay() {
		return viewParent != null ? viewParent.getDisplay()
				: Display.getDefault();
	}

	/**
	 * Set the currently available metrics.
	 * 
	 * @param metrics
	 *            Array of all available metrics with their technical names
	 *            (e.g. QUEUE_LENGTH).
	 */
	public final void setMetrics(final String[] metrics) {
		cmbMetric.setItems(metrics);
		cmbMetric.select(0);
		if (metrics.length > 0) {
			Helper.setEnabledRecursive(cmpMetric, true);
		} else {
			Helper.setEnabledRecursive(cmpMetric, false);
		}
	}

	/**
	 * Get selected metric.
	 * 
	 * @return Currently selected metric as GUI string (e.g. 'queue length').
	 */
	public final String getMetric() {
		return cmbMetric.getText();
	}

	/**
	 * Set the available 'from' measuring points. Select first item by default.
	 * 
	 * @param mp
	 *            List of available 'from' measuring points.
	 */
	public final void setFromMeasuringPoints(final Entity[] mp) {
		// Set or reset (in case of an empty array) the combo box items.
		cmbMeasuringPointFrom.setInput(mp);

		// Enable or disable the combo box.
		boolean enabled = mp.length > 0 ? true : false;
		Helper.setEnabledRecursive(cmpMeasuringPoints, enabled);
		btnPlot.setEnabled(enabled);
	}

	/**
	 * Get the currently selected 'from' measuring point.
	 * 
	 * @return Readable string of 'from' measuring point (contains name + id),
	 *         or {@code null} if no 'from' measuring points are available.
	 * 
	 * @see getIdFromReadableString(String)
	 */
	public final Entity getFromMeasuringPoint() {
		StructuredSelection selection = (StructuredSelection) cmbMeasuringPointFrom
				.getSelection();
		if (!selection.isEmpty()) {
			return (Entity) selection.getFirstElement();
		}
		return null;
	}

	/**
	 * Set the available 'to' measuring points. Select first item by default.
	 * 
	 * @param mp
	 *            List of available 'to' measuring points.
	 */
	public final void setToMeasuringPoints(final Entity[] mp) {
		cmbMeasuringPointTo.setInput(mp);

		boolean enabled = mp.length > 0 ? true : false;
		cmbMeasuringPointTo.getControl().setEnabled(enabled);
		if (enabled) {
			// select first element
			ISelection selection = new StructuredSelection(mp[0]);
			cmbMeasuringPointTo.setSelection(selection);
		}
	}

	/**
	 * Get the currently selected 'to' measuring point.
	 * 
	 * @return Readable string of 'to' measuring point (contains name + id), or
	 *         {@code null} if no 'to' measuring points are available.
	 * 
	 * @see getIdFromReadableString(String)
	 */
	public final Entity getToMeasuringPoint() {
		StructuredSelection selection = (StructuredSelection) cmbMeasuringPointTo
				.getSelection();
		if (!selection.isEmpty()) {
			return (Entity) selection.getFirstElement();
		}
		return null;
	}

	/**
	 * Get current start value of simulation time span.
	 * 
	 * @return Time span start value.
	 */
	public final int getTimeSpanStart() {
		return spnTimeSpanFrom.getSelection();
	}

	/**
	 * Set time span start value.
	 * 
	 * @param startValue
	 *            New time span start value.
	 */
	public final void setTimeSpanLower(final int startValue) {
		spnTimeSpanFrom.setSelection(startValue);
	}

	public final void setTimeSpanBounds(int lower, int upper) {
		spnTimeSpanFrom.setMinimum(lower);
		spnTimeSpanTo.setMinimum(lower);

		spnTimeSpanFrom.setMaximum(upper);
		spnTimeSpanTo.setMaximum(upper);
	}

	/**
	 * Get current end value of simulation time span.
	 * 
	 * @return Time span end value.
	 */
	public final int getTimeSpanEnd() {
		return spnTimeSpanTo.getSelection();
	}

	/**
	 * Set time span end value.
	 * 
	 * @param endValue
	 *            New time span end value.
	 */
	public final void setTimeSpanUpper(final int endValue) {
		spnTimeSpanTo.setSelection(endValue);
	}

	/**
	 * Set time span description.
	 * 
	 * @param description
	 *            Time span description.
	 */
	public final void setTimeSpanDescription(final String description) {
		lblSimulationTimeSpanDescription.setText(description);
	}

	public void setMeasurementsCount(int count) {
		this.lblMeasurementsCount.setText(Integer.toString(count));
	}

	/**
	 * Set the currently available diagram types.
	 * 
	 * @param diagramTypes
	 *            List of all available diagram types with their enums (
	 *            {@link edu.kit.ipd.sdq.eventsim.rvisualization.model.DiagramType}
	 *            ).
	 */
	public final void setDiagramTypes(final String[] diagramTypes) {

		cmbDiagramType.setItems(diagramTypes);
		cmbDiagramType.select(0);
	}

	/**
	 * Get selected diagram type.
	 * 
	 * @return Currently selected diagram type (
	 *         {@link edu.kit.ipd.sdq.eventsim.rvisualization.model.DiagramType}
	 *         ).
	 * @throws Exception
	 *             If an invalid technical name was used.
	 */
	public final String getDiagramType() {
		return cmbDiagramType.getText();
	}

	/**
	 * Set the available triggers.
	 * 
	 * @param trigger
	 *            List of all available triggers.
	 */
	public final void setTriggers(final String[] trigger) {
		cmbTriggerType.setItems(trigger);
	}

	/**
	 * Get selected trigger.
	 * 
	 * @return Currently selected trigger.
	 */
	public final String getTrigger() {
		return cmbTriggerType.getText().isEmpty() ? null
				: cmbTriggerType.getText();
	}

	/**
	 * Get current start value of trigger time span.
	 * 
	 * @return Time span start value.
	 */
	public final int getTriggerStart() {
		return spnTriggerFrom.getSelection();
	}

	/**
	 * Set trigger start value.
	 * 
	 * @param startValue
	 *            New trigger start value.
	 */
	public final void setTriggerStart(final int startValue) {
		spnTriggerFrom.setSelection(startValue);
	}

	/**
	 * Get current end value of trigger time span.
	 * 
	 * @return Trigger end value.
	 */
	public final int getTriggerEnd() {
		return spnTriggerTo.getSelection();
	}

	/**
	 * Set trigger end value.
	 * 
	 * @param endValue
	 *            New trigger end value.
	 */
	public final void setTriggerEnd(final int endValue) {
		spnTriggerTo.setSelection(endValue);
	}

	/**
	 * Set trigger instances.
	 * 
	 * @param instances
	 *            Array of all available trigger instances.
	 */
	public final void setTriggerInstances(final Entity[] instances) {
		cmbTriggerInstance.setInput(instances);
	}

	/**
	 * Get the selected trigger instance.
	 * 
	 * @return Currently selected trigger instance.
	 */
	public final Entity getTriggerInstance() {
		return (Entity) cmbTriggerInstance.getStructuredSelection()
				.getFirstElement();
	}

	public final void enableTriggerInstanceGroup(boolean enabled) {
		Helper.setEnabledRecursive(grpTriggerInstances, enabled);

	}

	public final void enableTriggerTypeGroup(boolean enabled) {
		Helper.setEnabledRecursive(grpTriggerTypes, enabled);
	}

	public final void enableMetricsComposite(boolean enabled) {
		Helper.setEnabledRecursive(cmpMetric, enabled);
	}

	public final void enableTriggerInstanceComboBox(boolean enabled) {
		cmbTriggerInstance.getCombo().setEnabled(enabled);
	}

	public void enableTimeSpanComposite(boolean enabled) {
		Helper.setEnabledRecursive(cmpTimeSpan, enabled);
	}

	public void enableAssemblyContextComposite(boolean enabled) {
		Helper.setEnabledRecursive(cmpAssemblyCtx, enabled);
	}

	public void enableMeasuringPointsComposite(boolean enabled) {
		Helper.setEnabledRecursive(cmpMeasuringPoints, enabled);
	}

	/**
	 * Set number of available instances in GUI.
	 * 
	 * @param number
	 *            Number of available trigger instance.
	 * @param warning
	 *            Print number as warning (red) or not.
	 */
	public final void setTriggerInstanceNumber(final String number,
			final boolean warning) {

		if (warning) {
			lblTriggerInstanceNumber
					.setForeground(getDisplay().getSystemColor(SWT.COLOR_RED));
		} else {
			lblTriggerInstanceNumber.setForeground(
					getDisplay().getSystemColor(SWT.COLOR_BLACK));
		}
		lblTriggerInstanceNumber.setText(number);
	}

	/**
	 * Set the number of currently available trigger instances on GUI.
	 * 
	 * @param number
	 *            Number to be set.
	 */
	public final void setTriggerInstanceNumber(final String number) {
		setTriggerInstanceNumber(number, false);
	}

	/**
	 * Set the trigger instance description.
	 * 
	 * @param description
	 *            Description to be set.
	 */
	public final void setTriggerInstanceDescription(final String description) {
		lblTriggerInstanceDescription.setText(description);
	}

	/**
	 * Set the currently available assembly contexts.
	 * 
	 * @param ctxs
	 *            Array of all available assembly contexts.
	 */
	public final void setAssemblyContexts(final Entity[] ctxs) {
		cmbAssemblyCtx.setInput(ctxs);
	}

	/**
	 * Get selected assembly context.
	 * 
	 * @return Currently selected assembly context.
	 */
	public final Entity getAssemblyContext() {
		return (Entity) cmbAssemblyCtx.getStructuredSelection()
				.getFirstElement();
	}

	/**
	 * Clears the trigger instance combo box.
	 */
	public void clearTriggerInstances() {
		setTriggerInstances(new Entity[0]);
	}

	/**
	 * This is a callback that will allow us to create the UI and initialize it.
	 * 
	 * @param parent
	 *            Parent UI element.
	 */
	public final void createPartControl(final Composite parent) {
		viewParent = parent;

		// create controller
		ctrl = new Controller(new FilterViewController(this));

		Composite cmpRoot = new Composite(parent, SWT.NONE);
		cmpRoot.setLayout(new BorderLayout(0, 0));

		createNorthBar(cmpRoot);
		createExpandBar(cmpRoot);
		createSouthBar(cmpRoot);

		addEventHandler();

		ctrl.viewInitialized();
	}

	private void createNorthBar(Composite cmpRoot) {
		Composite cmpNorth = new Composite(cmpRoot, SWT.NONE);
		cmpNorth.setLayoutData(BorderLayout.NORTH);
		cmpNorth.setLayout(new GridLayout(2, false));

		Label lblNumberOfMeasurements = new Label(cmpNorth, SWT.NONE);
		lblNumberOfMeasurements.setText("Number of measurements:");

		lblMeasurementsCount = new Label(cmpNorth, SWT.NONE);
		lblMeasurementsCount.setLayoutData(
				new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		lblMeasurementsCount.setText("0");
	}

	private void createSouthBar(Composite cmpRoot) {
		Composite cmpSouth = new Composite(cmpRoot, SWT.NONE);
		cmpSouth.setLayoutData(BorderLayout.SOUTH);
		cmpSouth.setLayout(new GridLayout(2, false));
		Label lblDiagramType = new Label(cmpSouth, SWT.NONE);
		lblDiagramType.setText("Diagram Type:");

		cmbDiagramType = new Combo(cmpSouth, SWT.READ_ONLY);
		cmbDiagramType.setLayoutData(
				new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		btnPlot = new Button(cmpSouth, SWT.NONE);
		btnPlot.setLayoutData(
				new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		btnPlot.setImage(ResourceManager.getPluginImage(
				"edu.kit.ipd.sdq.eventsim.rvisualization",
				"icons/chart_bar.png"));
		btnPlot.setText("Plot Diagram");
	}

	private void createExpandBar(Composite cmpRoot) {
		cmpCenter = new Composite(cmpRoot, SWT.NONE);
		cmpCenter.setLayoutData(BorderLayout.CENTER);
		cmpCenter.setLayout(new FillLayout(SWT.HORIZONTAL));

		ExpandBar expandBar = new ExpandBar(cmpCenter, SWT.V_SCROLL);
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

		createMetricExpandItem(expandBar);
		createTriggerExpandItem(expandBar);
		createAssemblyContextExpandItem(expandBar);
		createMeasuringPointsExpandItem(expandBar);
		createSimulationTimeExpandItem(expandBar);
	}

	private void createSimulationTimeExpandItem(ExpandBar expandBar) {
		ExpandItem xpiSimulationTime = new ExpandItem(expandBar, SWT.NONE);
		xpiSimulationTime.setText("When: Simulation Time Span");

		cmpTimeSpan = new Composite(expandBar, SWT.NONE);
		xpiSimulationTime.setControl(cmpTimeSpan);
		xpiSimulationTime.setHeight(100);
		GridLayout glCompositeTimeSpan = new GridLayout(5, false);
		cmpTimeSpan.setLayout(glCompositeTimeSpan);
		cmpTimeSpan.addListener(SWT.Resize,
				event -> getDisplay().asyncExec(() -> {
					if (cmpTimeSpan.isDisposed())
						return;
					Point size = cmpTimeSpan.computeSize(SWT.DEFAULT,
							SWT.DEFAULT);
					if (xpiSimulationTime.getHeight() != size.y) {
						xpiSimulationTime.setHeight(size.y);
					}
				}));

		Label lblFrom = new Label(cmpTimeSpan, SWT.NONE);
		lblFrom.setLayoutData(
				new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblFrom.setText("From");

		spnTimeSpanFrom = new Spinner(cmpTimeSpan, SWT.BORDER);
		spnTimeSpanFrom.setLayoutData(
				new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		spnTimeSpanFrom.setMaximum(TIMESPAN_MAX_VALUE);

		Label lblTo = new Label(cmpTimeSpan, SWT.NONE);
		lblTo.setLayoutData(
				new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblTo.setText("To");

		spnTimeSpanTo = new Spinner(cmpTimeSpan, SWT.BORDER);
		spnTimeSpanTo.setLayoutData(
				new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		spnTimeSpanTo.setMaximum(TIMESPAN_MAX_VALUE);

		btnTimespanReset = new Button(cmpTimeSpan, SWT.NONE);
		btnTimespanReset.setLayoutData(
				new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		btnTimespanReset.setText("Reset");

		lblSimulationTimeSpanDescription = new Label(cmpTimeSpan,
				SWT.NONE);
		lblSimulationTimeSpanDescription.setLayoutData(
				new GridData(SWT.FILL, SWT.CENTER, true, false, 5, 1));
	}

	private void createMeasuringPointsExpandItem(ExpandBar expandBar) {
		ExpandItem xpiMeasuringPoints = new ExpandItem(expandBar, SWT.NONE);
		xpiMeasuringPoints.setExpanded(true);
		xpiMeasuringPoints.setText("Where: Measuring Points");

		cmpMeasuringPoints = new Composite(expandBar, SWT.NONE);
		xpiMeasuringPoints.setControl(cmpMeasuringPoints);
		xpiMeasuringPoints.setHeight(100);
		cmpMeasuringPoints.setLayout(new GridLayout(2, false));
		cmpMeasuringPoints.addListener(SWT.Resize,
				event -> getDisplay().asyncExec(() -> {
					if (cmpMeasuringPoints.isDisposed())
						return;
					Point size = cmpMeasuringPoints
							.computeSize(SWT.DEFAULT, SWT.DEFAULT);
					if (xpiMeasuringPoints.getHeight() != size.y) {
						xpiMeasuringPoints.setHeight(size.y);
					}
				}));

		Label lblMPFrom = new Label(cmpMeasuringPoints, SWT.NONE);
		lblMPFrom.setText("From:");

		cmbMeasuringPointFrom = new ComboViewer(cmpMeasuringPoints,
				SWT.READ_ONLY);
		Combo combo = cmbMeasuringPointFrom.getCombo();
		combo.setLayoutData(
				new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		cmbMeasuringPointFrom
				.setContentProvider(ArrayContentProvider.getInstance());
		cmbMeasuringPointFrom.setLabelProvider(ENTITY_LABEL_PROVIDER);

		Label lblMPTo = new Label(cmpMeasuringPoints, SWT.NONE);
		lblMPTo.setText("To:");

		cmbMeasuringPointTo = new ComboViewer(cmpMeasuringPoints,
				SWT.READ_ONLY);
		Combo combo_1 = cmbMeasuringPointTo.getCombo();
		combo_1.setLayoutData(
				new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		cmbMeasuringPointTo.setContentProvider(ArrayContentProvider.getInstance());
		cmbMeasuringPointTo.setLabelProvider(ENTITY_LABEL_PROVIDER);
	}

	private void createAssemblyContextExpandItem(ExpandBar expandBar) {
		cmpAssemblyCtx = new Composite(expandBar, SWT.NONE);
		cmpAssemblyCtx.setLayout(new GridLayout(2, false));
		
		ExpandItem xpiAssemblyCtx = new ExpandItem(expandBar, 0);
		xpiAssemblyCtx.setText("Where: Assembly Context");
		xpiAssemblyCtx.setControl(cmpAssemblyCtx);
		xpiAssemblyCtx.setHeight(100);
		
		Label lblSelectAnAssembly = new Label(cmpAssemblyCtx, SWT.NONE);
		lblSelectAnAssembly.setLayoutData(
				new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
		lblSelectAnAssembly.setText("Select an assembly context.");

		cmbAssemblyCtx = new ComboViewer(cmpAssemblyCtx, SWT.READ_ONLY);
		cmbAssemblyCtx.getCombo().setLayoutData(
				new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		cmbAssemblyCtx.setContentProvider(ArrayContentProvider.getInstance());
		cmbAssemblyCtx.setLabelProvider(ENTITY_LABEL_PROVIDER);

		Button btnClear = new Button(cmpAssemblyCtx, SWT.NONE);
		btnClear.setText("Clear");
		btnClear.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				cmbAssemblyCtx.setSelection(StructuredSelection.EMPTY);
			}
		});
		
		keepSameHeight(cmpAssemblyCtx, xpiAssemblyCtx);
	}

	private void createTriggerExpandItem(ExpandBar expandBar) {
		Composite cmpTrigger = new Composite(expandBar, SWT.NONE);
		GridLayout layout = new GridLayout(1, false);
		cmpTrigger.setLayout(layout);

		ExpandItem xpiTrigger = new ExpandItem(expandBar, SWT.NONE);
		xpiTrigger.setText("Who: Trigger");
		xpiTrigger.setControl(cmpTrigger);
		xpiTrigger.setHeight(300);

		keepSameHeight(cmpTrigger, xpiTrigger);

		createTriggerTypesGroup(cmpTrigger);
		createTriggerInstancesGroup(cmpTrigger);
	}

	private void createTriggerInstancesGroup(Composite compositeTrigger) {
		grpTriggerInstances = new Group(compositeTrigger, SWT.NONE);
		grpTriggerInstances.setLayoutData(
				new GridData(SWT.FILL, SWT.CENTER, true, true, 1, 1));
		grpTriggerInstances.setText("Trigger instance");
		grpTriggerInstances.setLayout(new GridLayout(5, false));

		lblTriggerInstanceDescription = new Label(grpTriggerInstances,
				SWT.WRAP);
		lblTriggerInstanceDescription.setFont(JFaceResources.getFontRegistry()
				.getItalic(JFaceResources.DEFAULT_FONT));
		lblTriggerInstanceDescription.setLayoutData(
				new GridData(SWT.FILL, SWT.FILL, true, false, 5, 1));

		Label lblFromTrigger = new Label(grpTriggerInstances, SWT.NONE);
		lblFromTrigger.setText("From");

		spnTriggerFrom = new Spinner(grpTriggerInstances, SWT.BORDER);
		GridData gdSpinnerTriggerFrom = new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1);
		gdSpinnerTriggerFrom.widthHint = 60;
		spnTriggerFrom.setLayoutData(gdSpinnerTriggerFrom);
		spnTriggerFrom.setMaximum(TIMESPAN_MAX_VALUE);

		Label lblToTrigger = new Label(grpTriggerInstances, SWT.NONE);
		lblToTrigger.setText("To");

		spnTriggerTo = new Spinner(grpTriggerInstances, SWT.BORDER);
		GridData gdSpinnerTriggerTo = new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1);
		gdSpinnerTriggerTo.widthHint = 60;
		spnTriggerTo.setLayoutData(gdSpinnerTriggerTo);
		spnTriggerTo.setMaximum(TIMESPAN_MAX_VALUE);

		btnTriggerReset = new Button(grpTriggerInstances, SWT.NONE);
		btnTriggerReset.setLayoutData(
				new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		btnTriggerReset.setText("Reset");

		Label lblTriggerAvailableInstances = new Label(grpTriggerInstances,
				SWT.WRAP);
		lblTriggerAvailableInstances.setText("available instances:");
		lblTriggerAvailableInstances.setLayoutData(
				new GridData(SWT.LEFT, SWT.CENTER, false, true, 2, 1));
		lblTriggerAvailableInstances.setForeground(
				Display.getCurrent().getSystemColor(SWT.COLOR_BLACK));

		lblTriggerInstanceNumber = new Label(grpTriggerInstances, SWT.NONE);
		lblTriggerInstanceNumber.setLayoutData(
				new GridData(SWT.FILL, SWT.CENTER, false, true, 2, 1));
		new Label(grpTriggerInstances, SWT.NONE);

		cmbTriggerInstance = new ComboViewer(grpTriggerInstances,
				SWT.READ_ONLY);
		cmbTriggerInstance.getCombo().setLayoutData(
				new GridData(SWT.FILL, SWT.CENTER, true, true, 5, 1));
		cmbTriggerInstance
				.setContentProvider(ArrayContentProvider.getInstance());
		cmbTriggerInstance.setLabelProvider(ENTITY_LABEL_PROVIDER);
	}

	private void createTriggerTypesGroup(Composite compositeTrigger) {
		grpTriggerTypes = new Group(compositeTrigger, SWT.NONE);
		grpTriggerTypes.setLayoutData(
				new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		grpTriggerTypes.setText("Trigger type");
		grpTriggerTypes.setLayout(new GridLayout(2, false));

		Label lblSelectTriggerType = new Label(grpTriggerTypes, SWT.NONE);
		lblSelectTriggerType.setLayoutData(
				new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		lblSelectTriggerType.setSize(349, 14);
		lblSelectTriggerType.setText("Select a trigger type.");
		new Label(grpTriggerTypes, SWT.NONE);

		cmbTriggerType = new Combo(grpTriggerTypes, SWT.READ_ONLY);
		cmbTriggerType.setLayoutData(
				new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		cmbTriggerType.setSize(349, 22);

		Button btnClear = new Button(grpTriggerTypes, SWT.NONE);
		btnClear.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				cmbTriggerType.deselectAll();
			}
		});
		btnClear.setText("Clear");
	}

	private void createMetricExpandItem(ExpandBar expandBar) {
		ExpandItem xpiMetric = new ExpandItem(expandBar, SWT.NONE);
		xpiMetric.setExpanded(true);
		xpiMetric.setText("What: Metric");

		cmpMetric = new Composite(expandBar, SWT.NONE);
		xpiMetric.setControl(cmpMetric);
		cmpMetric.setLayout(new GridLayout(1, false));
		cmpMetric.addListener(SWT.Resize,
				event -> getDisplay().asyncExec(() -> {
					if (cmpMetric.isDisposed())
						return;
					Point size = cmpMetric.computeSize(SWT.DEFAULT,
							SWT.DEFAULT);
					if (xpiMetric.getHeight() != size.y) {
						xpiMetric.setHeight(size.y);
					}
				}));

		Label lblSelectTheMetric = new Label(cmpMetric, SWT.NONE);
		lblSelectTheMetric.setLayoutData(
				new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
		lblSelectTheMetric.setText("Select the metric to be plotted.");

		cmbMetric = new Combo(cmpMetric, SWT.READ_ONLY);
		cmbMetric.setLayoutData(
				new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		xpiMetric.setHeight(
				xpiMetric.getControl().computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
	}

	/**
	 * Passing the focus request.
	 */
	public final void setFocus() {
		viewParent.setFocus();
	}

	/**
	 * Add event handler.
	 * 
	 * Adds event handler for buttons, keys and modify listeners for combo
	 * boxes.
	 */
	private void addEventHandler() {
		btnPlot.addListener(SWT.Selection, l -> ctrl.plotDiagram());
		btnTimespanReset.addListener(SWT.Selection,
				l -> ctrl.resetSimulationTimeBounds());
		btnTriggerReset.addListener(SWT.Selection,
				l -> ctrl.resetTriggerSimulationTimeBounds());

		ModifyListener triggerSpinnerModifyListener = new ModifyListener() {

			private Timer timer = new Timer();
			private final long timerDelay = 900;

			@Override
			public void modifyText(final ModifyEvent e) {

				if (timer != null) {
					timer.cancel();
				}

				timer = new Timer();

				// Set timer to delay the modify event to prevent multiple
				// events in a row.
				TimerTask task = new TimerTask() {
					@Override
					public void run() {

						Display.getDefault().asyncExec(new Runnable() {
							public void run() {
								ctrl.triggerSimulationBoundsChanged();
							}
						});
						timer.cancel();
					}
				};
				timer.schedule(task, timerDelay);

			}
		};

		spnTriggerFrom.addModifyListener(triggerSpinnerModifyListener);
		spnTriggerTo.addModifyListener(triggerSpinnerModifyListener);

		cmbMetric.addModifyListener(l -> ctrl.metricSelected());

		cmbTriggerType.addModifyListener(l -> ctrl.triggerTypeSelected());

		cmbTriggerInstance.addSelectionChangedListener(
				l -> ctrl.triggerInstanceSelected());

		cmbMeasuringPointFrom.addSelectionChangedListener(
				l -> ctrl.fromMeasuringPointSelected());

		cmbAssemblyCtx.addSelectionChangedListener(
				l -> ctrl.assemblyContextSelected());
	}

	private void keepSameHeight(Composite observedComposite,
			ExpandItem exandItemToResize) {
		observedComposite.addListener(SWT.Resize,
				event -> getDisplay().asyncExec(() -> {
					if (observedComposite.isDisposed())
						return;
					Point size = observedComposite.computeSize(SWT.DEFAULT,
							SWT.DEFAULT);
					if (exandItemToResize.getHeight() != size.y) {
						exandItemToResize.setHeight(size.y);
					}
				}));
	}

	/**
	 * Re-layout the view.
	 */
	private void layoutView() {
		getDisplay().asyncExec(new Runnable() {
			public void run() {
				viewParent.layout();
			}
		});
	}
}
