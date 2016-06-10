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
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
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

	private Listener btnListener;
	private ModifyListener triggerSpinnerModifyListener;

	private ModifyListener metricModify;
	private ModifyListener triggerModify;

	/**
	 * Maximum of simulation time span values.
	 */
	private static final int TIMESPAN_MAX_VALUE = 1_000_000;

	private Combo filterComboMetric;
	private ComboViewer filterComboMPFrom;
	private ComboViewer filterComboMPTo;
	private Combo filterComboTrigger;
	private ComboViewer filterComboTriggerInstance;
	private ComboViewer filterComboAssemblyContext;
	private Combo filterComboDiagramType;

	private Spinner spinnerTriggerFrom;
	private Spinner spinnerTriggerTo;
	private Spinner spinnerTimeSpanFrom;
	private Spinner spinnerTimeSpanTo;

	private Button btnTriggerReset;
	private Button btnTimespanReset;
	private Button btnPlot;

	private Label lblTriggerInstanceNumber;
	private Label lblTriggerInstanceDescription;
	private Label lblSimulationTimeSpanDescription;

	private Composite compositeMetrics;
	private Composite compositeAssemblyCtx;
	private Composite compositeTrigger;
	private Composite compositeMeasuringPoints;
	private Composite compositeTimeSpan;
	private Composite compositeCenter;

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
		filterComboMetric.setItems(metrics);
		filterComboMetric.select(0);
		if (metrics.length > 0) {
			Helper.setEnabledRecursive(compositeMetrics, true);
		} else {
			Helper.setEnabledRecursive(compositeMetrics, false);
		}
	}

	/**
	 * Get selected metric.
	 * 
	 * @return Currently selected metric as GUI string (e.g. 'queue length').
	 */
	public final String getMetric() {
		return filterComboMetric.getText();
	}

	/**
	 * Set the available 'from' measuring points. Select first item by default.
	 * 
	 * @param mp
	 *            List of available 'from' measuring points.
	 */
	public final void setFromMeasuringPoints(final Entity[] mp) {
		// Set or reset (in case of an empty array) the combo box items.
		filterComboMPFrom.setInput(mp);

		// Enable or disable the combo box.
		boolean enabled = mp.length > 0 ? true : false;
		Helper.setEnabledRecursive(compositeMeasuringPoints, enabled);
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
		StructuredSelection selection = (StructuredSelection) filterComboMPFrom
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
		filterComboMPTo.setInput(mp);

		boolean enabled = mp.length > 0 ? true : false;
		filterComboMPTo.getControl().setEnabled(enabled);
		if (enabled) {
			// select first element
			ISelection selection = new StructuredSelection(mp[0]);
			filterComboMPTo.setSelection(selection);
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
		StructuredSelection selection = (StructuredSelection) filterComboMPTo
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
		return spinnerTimeSpanFrom.getSelection();
	}

	/**
	 * Set time span start value.
	 * 
	 * @param startValue
	 *            New time span start value.
	 */
	public final void setTimeSpanLower(final int startValue) {
		spinnerTimeSpanFrom.setSelection(startValue);
	}

	public final void setTimeSpanBounds(int lower, int upper) {
		spinnerTimeSpanFrom.setMinimum(lower);
		spinnerTimeSpanTo.setMinimum(lower);

		spinnerTimeSpanFrom.setMaximum(upper);
		spinnerTimeSpanTo.setMaximum(upper);
	}

	/**
	 * Get current end value of simulation time span.
	 * 
	 * @return Time span end value.
	 */
	public final int getTimeSpanEnd() {
		return spinnerTimeSpanTo.getSelection();
	}

	/**
	 * Set time span end value.
	 * 
	 * @param endValue
	 *            New time span end value.
	 */
	public final void setTimeSpanUpper(final int endValue) {
		spinnerTimeSpanTo.setSelection(endValue);
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

		filterComboDiagramType.setItems(diagramTypes);
		filterComboDiagramType.select(0);
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
		return filterComboDiagramType.getText();
	}

	/**
	 * Set the available triggers.
	 * 
	 * @param trigger
	 *            List of all available triggers.
	 */
	public final void setTriggers(final String[] trigger) {
		filterComboTrigger.setItems(trigger);
	}

	/**
	 * Get selected trigger.
	 * 
	 * @return Currently selected trigger.
	 */
	public final String getTrigger() {
		return filterComboTrigger.getText().isEmpty() ? null
				: filterComboTrigger.getText();
	}

	/**
	 * Get current start value of trigger time span.
	 * 
	 * @return Time span start value.
	 */
	public final int getTriggerStart() {
		return spinnerTriggerFrom.getSelection();
	}

	/**
	 * Set trigger start value.
	 * 
	 * @param startValue
	 *            New trigger start value.
	 */
	public final void setTriggerStart(final int startValue) {
		spinnerTriggerFrom.setSelection(startValue);
	}

	/**
	 * Get current end value of trigger time span.
	 * 
	 * @return Trigger end value.
	 */
	public final int getTriggerEnd() {
		return spinnerTriggerTo.getSelection();
	}

	/**
	 * Set trigger end value.
	 * 
	 * @param endValue
	 *            New trigger end value.
	 */
	public final void setTriggerEnd(final int endValue) {
		spinnerTriggerTo.setSelection(endValue);
	}

	/**
	 * Set trigger instances.
	 * 
	 * @param instances
	 *            Array of all available trigger instances.
	 */
	public final void setTriggerInstances(final Entity[] instances) {
		filterComboTriggerInstance.setInput(instances);
	}

	/**
	 * Get the selected trigger instance.
	 * 
	 * @return Currently selected trigger instance.
	 */
	public final Entity getTriggerInstance() {
		return (Entity) filterComboTriggerInstance.getStructuredSelection()
				.getFirstElement();
	}

	public final void enableTriggerInstanceGroup(boolean enabled) {
		Helper.setEnabledRecursive(grpTriggerInstances, enabled);

	}

	public final void enableTriggerTypeGroup(boolean enabled) {
		Helper.setEnabledRecursive(grpTriggerTypes, enabled);
	}

	public final void enableMetricsComposite(boolean enabled) {
		Helper.setEnabledRecursive(compositeMetrics, enabled);
	}

	public final void enableTriggerInstanceComboBox(boolean enabled) {
		filterComboTriggerInstance.getCombo().setEnabled(enabled);
	}

	public void enableTimeSpanComposite(boolean enabled) {
		Helper.setEnabledRecursive(compositeTimeSpan, enabled);
	}

	public void enableAssemblyContextComposite(boolean enabled) {
		Helper.setEnabledRecursive(compositeAssemblyCtx, enabled);
	}

	public void enableMeasuringPointsComposite(boolean enabled) {
		Helper.setEnabledRecursive(compositeMeasuringPoints, enabled);
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
		filterComboAssemblyContext.setInput(ctxs);
	}

	/**
	 * Get selected assembly context.
	 * 
	 * @return Currently selected assembly context.
	 */
	public final Entity getAssemblyContext() {
		return (Entity) filterComboAssemblyContext.getStructuredSelection()
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

		ctrl = new Controller(new FilterViewController(this));

		addListenerObject(this);

		// Create scrolled container.
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new BorderLayout(0, 0));

		Composite compositeSouth = new Composite(container, SWT.NONE);
		compositeSouth.setLayoutData(BorderLayout.SOUTH);
		compositeSouth.setLayout(new GridLayout(2, false));
		Label lblDiagramType = new Label(compositeSouth, SWT.NONE);
		lblDiagramType.setText("Diagram Type:");

		filterComboDiagramType = new Combo(compositeSouth, SWT.READ_ONLY);
		filterComboDiagramType.setLayoutData(
				new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		btnPlot = new Button(compositeSouth, SWT.NONE);
		btnPlot.setLayoutData(
				new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		btnPlot.setImage(ResourceManager.getPluginImage(
				"edu.kit.ipd.sdq.eventsim.rvisualization",
				"icons/chart_bar.png"));
		btnPlot.setText("Plot Diagram");

		compositeCenter = new Composite(container, SWT.NONE);
		compositeCenter.setLayoutData(BorderLayout.CENTER);
		compositeCenter.setLayout(new FillLayout(SWT.HORIZONTAL));
		ExpandBar filterExpandMetric = new ExpandBar(compositeCenter,
				SWT.V_SCROLL);

		ExpandItem xpndtmWhatMetric = new ExpandItem(filterExpandMetric,
				SWT.NONE);
		xpndtmWhatMetric.setExpanded(true);
		xpndtmWhatMetric.setText("What: Metric");

		filterExpandMetric.addExpandListener(new ExpandListener() {

			@Override
			public void itemExpanded(final ExpandEvent e) {
				layoutView();
			}

			@Override
			public void itemCollapsed(final ExpandEvent e) {
				layoutView();
			}
		});

		compositeMetrics = new Composite(filterExpandMetric, SWT.NONE);
		xpndtmWhatMetric.setControl(compositeMetrics);
		xpndtmWhatMetric.setHeight(xpndtmWhatMetric.getControl()
				.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
		compositeMetrics.setLayout(new GridLayout(1, false));

		Label lblSelectTheMetric = new Label(compositeMetrics, SWT.NONE);
		lblSelectTheMetric.setLayoutData(
				new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
		lblSelectTheMetric.setText("Select the metric to be plotted.");

		filterComboMetric = new Combo(compositeMetrics, SWT.READ_ONLY);
		filterComboMetric.setLayoutData(
				new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		ExpandItem xpndtmWhatTrigger = new ExpandItem(filterExpandMetric,
				SWT.NONE);
		xpndtmWhatTrigger.setText("Who: Trigger");

		compositeTrigger = new Composite(filterExpandMetric, SWT.NONE);
		xpndtmWhatTrigger.setControl(compositeTrigger);
		GridLayout glCompositeTrigger = new GridLayout(1, false);
		compositeTrigger.setLayout(glCompositeTrigger);

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

		filterComboTrigger = new Combo(grpTriggerTypes, SWT.READ_ONLY);
		filterComboTrigger.setLayoutData(
				new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		filterComboTrigger.setSize(349, 22);

		Button btnClear = new Button(grpTriggerTypes, SWT.NONE);
		btnClear.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				filterComboTrigger.deselectAll();
			}
		});
		btnClear.setText("Clear");

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

		spinnerTriggerFrom = new Spinner(grpTriggerInstances, SWT.BORDER);
		GridData gdSpinnerTriggerFrom = new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1);
		gdSpinnerTriggerFrom.widthHint = 60;
		spinnerTriggerFrom.setLayoutData(gdSpinnerTriggerFrom);
		spinnerTriggerFrom.setMaximum(TIMESPAN_MAX_VALUE);

		Label lblToTrigger = new Label(grpTriggerInstances, SWT.NONE);
		lblToTrigger.setText("To");

		spinnerTriggerTo = new Spinner(grpTriggerInstances, SWT.BORDER);
		GridData gdSpinnerTriggerTo = new GridData(SWT.FILL, SWT.CENTER, true,
				false, 1, 1);
		gdSpinnerTriggerTo.widthHint = 60;
		spinnerTriggerTo.setLayoutData(gdSpinnerTriggerTo);
		spinnerTriggerTo.setMaximum(TIMESPAN_MAX_VALUE);

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

		filterComboTriggerInstance = new ComboViewer(grpTriggerInstances,
				SWT.READ_ONLY);
		filterComboTriggerInstance.getCombo().setLayoutData(
				new GridData(SWT.FILL, SWT.CENTER, true, true, 5, 1));
		filterComboTriggerInstance
				.setContentProvider(ArrayContentProvider.getInstance());
		filterComboTriggerInstance.setLabelProvider(ENTITY_LABEL_PROVIDER);
		xpndtmWhatTrigger.setHeight(250);

		ExpandItem expandItem = new ExpandItem(filterExpandMetric, 0);
		expandItem.setText("Where: Assembly Context");

		compositeAssemblyCtx = new Composite(filterExpandMetric, SWT.NONE);
		expandItem.setControl(compositeAssemblyCtx);
		compositeAssemblyCtx.setLayout(new GridLayout(2, false));

		Label lblSelectAnAssembly = new Label(compositeAssemblyCtx, SWT.NONE);
		lblSelectAnAssembly.setLayoutData(
				new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
		lblSelectAnAssembly.setText("Select an assembly context.");

		filterComboAssemblyContext = new ComboViewer(compositeAssemblyCtx,
				SWT.READ_ONLY);
		filterComboAssemblyContext.getCombo().setLayoutData(
				new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Button btnNewButton = new Button(compositeAssemblyCtx, SWT.NONE);
		btnNewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				filterComboAssemblyContext
						.setSelection(StructuredSelection.EMPTY);
			}
		});
		btnNewButton.setText("Clear");
		filterComboAssemblyContext
				.setContentProvider(ArrayContentProvider.getInstance());
		filterComboAssemblyContext.setLabelProvider(ENTITY_LABEL_PROVIDER);
		expandItem.setHeight(64);

		ExpandItem xpndtmWhereMeasuringPoints = new ExpandItem(
				filterExpandMetric, SWT.NONE);
		xpndtmWhereMeasuringPoints.setExpanded(true);
		xpndtmWhereMeasuringPoints.setText("Where: Measuring Points");

		compositeMeasuringPoints = new Composite(filterExpandMetric, SWT.NONE);
		xpndtmWhereMeasuringPoints.setControl(compositeMeasuringPoints);
		compositeMeasuringPoints.setLayout(new GridLayout(2, false));

		Label lblMPFrom = new Label(compositeMeasuringPoints, SWT.NONE);
		lblMPFrom.setText("From:");

		filterComboMPFrom = new ComboViewer(compositeMeasuringPoints,
				SWT.READ_ONLY);
		Combo combo = filterComboMPFrom.getCombo();
		combo.setLayoutData(
				new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		filterComboMPFrom
				.setContentProvider(ArrayContentProvider.getInstance());
		filterComboMPFrom.setLabelProvider(ENTITY_LABEL_PROVIDER);

		Label lblMPTo = new Label(compositeMeasuringPoints, SWT.NONE);
		lblMPTo.setText("To:");

		filterComboMPTo = new ComboViewer(compositeMeasuringPoints,
				SWT.READ_ONLY);
		Combo combo_1 = filterComboMPTo.getCombo();
		combo_1.setLayoutData(
				new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		filterComboMPTo.setContentProvider(ArrayContentProvider.getInstance());
		filterComboMPTo.setLabelProvider(ENTITY_LABEL_PROVIDER);

		xpndtmWhereMeasuringPoints.setHeight(60);

		ExpandItem xpndtmWhenSimulationTime = new ExpandItem(filterExpandMetric,
				SWT.NONE);
		xpndtmWhenSimulationTime.setText("When: Simulation Time Span");

		compositeTimeSpan = new Composite(filterExpandMetric, SWT.NONE);
		xpndtmWhenSimulationTime.setControl(compositeTimeSpan);
		GridLayout glCompositeTimeSpan = new GridLayout(5, false);
		compositeTimeSpan.setLayout(glCompositeTimeSpan);

		Label lblFrom = new Label(compositeTimeSpan, SWT.NONE);
		lblFrom.setLayoutData(
				new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblFrom.setText("From");

		spinnerTimeSpanFrom = new Spinner(compositeTimeSpan, SWT.BORDER);
		spinnerTimeSpanFrom.setLayoutData(
				new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		spinnerTimeSpanFrom.setMaximum(TIMESPAN_MAX_VALUE);

		Label lblTo = new Label(compositeTimeSpan, SWT.NONE);
		lblTo.setLayoutData(
				new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblTo.setText("To");

		spinnerTimeSpanTo = new Spinner(compositeTimeSpan, SWT.BORDER);
		spinnerTimeSpanTo.setLayoutData(
				new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		spinnerTimeSpanTo.setMaximum(TIMESPAN_MAX_VALUE);

		btnTimespanReset = new Button(compositeTimeSpan, SWT.NONE);
		btnTimespanReset.setLayoutData(
				new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		btnTimespanReset.setText("Reset");

		lblSimulationTimeSpanDescription = new Label(compositeTimeSpan,
				SWT.NONE);
		lblSimulationTimeSpanDescription.setLayoutData(
				new GridData(SWT.FILL, SWT.CENTER, true, false, 5, 1));
		new Label(compositeTimeSpan, SWT.NONE);
		new Label(compositeTimeSpan, SWT.NONE);
		new Label(compositeTimeSpan, SWT.NONE);
		new Label(compositeTimeSpan, SWT.NONE);
		new Label(compositeTimeSpan, SWT.NONE);
		xpndtmWhenSimulationTime.setHeight(60);

		Composite compositeNorth = new Composite(container, SWT.NONE);
		compositeNorth.setLayoutData(BorderLayout.NORTH);
		compositeNorth.setLayout(new GridLayout(2, false));

		Label lblNumberOfMeasurements = new Label(compositeNorth, SWT.NONE);
		lblNumberOfMeasurements.setText("Number of measurements:");

		lblMeasurementsCount = new Label(compositeNorth, SWT.NONE);
		lblMeasurementsCount.setLayoutData(
				new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		lblMeasurementsCount.setText("0");

		addEventHandler();

		Helper.setEnabledRecursive(compositeMeasuringPoints, false);

		ctrl.viewInitialized();
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

		btnListener = new Listener() {
			@Override
			public void handleEvent(final Event event) {
				if (event.widget == btnPlot) {
					ctrl.plotDiagram();
				} else if (event.widget == btnTimespanReset) {
					ctrl.resetSimulationTimeBounds();
				} else if (event.widget == btnTriggerReset) {
					ctrl.resetTriggerSimulationTimeBounds();
				}
			}
		};
		btnPlot.addListener(SWT.Selection, btnListener);
		btnTimespanReset.addListener(SWT.Selection, btnListener);
		btnTriggerReset.addListener(SWT.Selection, btnListener);

		triggerSpinnerModifyListener = new ModifyListener() {

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

		spinnerTriggerFrom.addModifyListener(triggerSpinnerModifyListener);
		spinnerTriggerTo.addModifyListener(triggerSpinnerModifyListener);

		// TODO: Don't trigger if combo box element has not changed!
		metricModify = new ModifyListener() {

			@Override
			public void modifyText(final ModifyEvent e) {

				// Ignore combo box initialization.
				if (e.time > 0) {
					ctrl.metricSelected();

				}

			}
		};
		filterComboMetric.addModifyListener(metricModify);

		triggerModify = new ModifyListener() {

			@Override
			public void modifyText(final ModifyEvent e) {
				ctrl.triggerTypeSelected();
			}
		};
		filterComboTrigger.addModifyListener(triggerModify);

		filterComboTriggerInstance.addSelectionChangedListener(
				l -> ctrl.triggerInstanceSelected());

		filterComboMPFrom.addSelectionChangedListener(
				e -> ctrl.fromMeasuringPointSelected());

		filterComboAssemblyContext.addSelectionChangedListener(
				l -> ctrl.assemblyContextSelected());
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
