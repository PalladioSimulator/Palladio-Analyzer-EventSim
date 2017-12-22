package edu.kit.ipd.sdq.eventsim.rvisualization.views;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.beans.PojoObservables;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.databinding.viewers.ViewerProperties;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ListDialog;

import edu.kit.ipd.sdq.eventsim.rvisualization.model.DiagramModel;
import edu.kit.ipd.sdq.eventsim.rvisualization.model.TranslatableEntity;
import edu.kit.ipd.sdq.eventsim.rvisualization.model.VariableBinding;
import edu.kit.ipd.sdq.eventsim.rvisualization.model.VariableBindingModel;
import edu.kit.ipd.sdq.eventsim.rvisualization.model.provider.TranslatableEntityLabelProvider;
import edu.kit.ipd.sdq.eventsim.rvisualization.model.provider.VariableBindingLabelProvider;

public class DiagramSettingsViewer extends Composite {

    private DataBindingContext m_bindingContext;

    private Text txtTitle;

    private DiagramModel diagramModel;

    private VariableBindingModel bindingModel;

    private VariableBindingModel bindingModelOriginal;

    private Text txtSubTitle;
    private Text txtSubSubTitle;
    private Group grpVariableBindings;
    private List listUnboundVariables;
    private ListViewer listViewerUnboundVariables;
    private List listBoundVariables;
    private ListViewer listViewerBoundVariables;
    private Label lblUnboundVariables;
    private Label lblVariableBindings;
    private Button btnBind;
    private Button btnRemove;

    public DiagramSettingsViewer(Composite parent, int style, DiagramModel diagramModel,
            VariableBindingModel bindingModel) {
        super(parent, style);
        this.diagramModel = diagramModel;

        // only when the users presses OK, the copied binding model is applied to the original
        // binding model
        this.bindingModelOriginal = bindingModel;
        this.bindingModel = new VariableBindingModel(bindingModelOriginal);

        setLayout(new GridLayout(2, false));

        createTitle();
        createSubTitle();
        createSubSubTitle();
        createVariableBindingsGroup(diagramModel);

        // m_bindingContext = initDataBindings();
    }

    private void createVariableBindingsGroup(DiagramModel diagramModel) {
        grpVariableBindings = new Group(this, SWT.NONE);
        grpVariableBindings.setText("Variable Bindings");
        grpVariableBindings.setLayout(new GridLayout(2, false));
        grpVariableBindings.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

        lblUnboundVariables = new Label(grpVariableBindings, SWT.NONE);
        lblUnboundVariables.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
        lblUnboundVariables.setText("Unbound Variables:");

        lblVariableBindings = new Label(grpVariableBindings, SWT.NONE);
        lblVariableBindings.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
        lblVariableBindings.setText("Bindings:");

        listViewerUnboundVariables = new ListViewer(grpVariableBindings, SWT.BORDER | SWT.V_SCROLL);
        listUnboundVariables = listViewerUnboundVariables.getList();
        listUnboundVariables.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

        listViewerBoundVariables = new ListViewer(grpVariableBindings, SWT.BORDER | SWT.V_SCROLL);
        listBoundVariables = listViewerBoundVariables.getList();
        listBoundVariables.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

        btnBind = new Button(grpVariableBindings, SWT.NONE);
        btnBind.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (bindingModel.getSelectedUnboundVariable() == null) {
                    return;
                }

                // TODO move to controller?
                TranslatableEntity selectedBindingType = openBindingTypeSelectionDialog();
                if (selectedBindingType != null) {
                    TranslatableEntity selectedVariable = bindingModel.getSelectedUnboundVariable();
                    VariableBinding selectedBinding = new VariableBinding(selectedVariable, selectedBindingType);
                    bindingModel.addVariableBinding(selectedBinding);
                    bindingModel.removeAvailableBindingType(selectedBinding.getBindingType());
                    bindingModel.removeUnboundVariable(selectedBinding.getVariable());
                }
            }
        });
        btnBind.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        btnBind.setText("Bind...");

        btnRemove = new Button(grpVariableBindings, SWT.NONE);
        btnRemove.addSelectionListener(new SelectionAdapter() {
            // TODO move to controller?
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (bindingModel.getSelectedVariableBinding() == null) {
                    return;
                }

                VariableBinding selectedBinding = bindingModel.getSelectedVariableBinding();
                bindingModel.removeVariableBinding(selectedBinding);
                bindingModel.addAvailableBindingType(selectedBinding.getBindingType());
                bindingModel.addUnboundVariable(selectedBinding.getVariable());
            }
        });
        btnRemove.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        btnRemove.setText("Remove");

        m_bindingContext = initDataBindings();
    }

    private TranslatableEntity openBindingTypeSelectionDialog() {
        ListDialog dialog = new ListDialog(getParent().getShell());
        dialog.setHelpAvailable(false);
        dialog.setInput(bindingModel.getAvailableBindingTypes());
        dialog.setContentProvider(ArrayContentProvider.getInstance());
        dialog.setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
                return ((TranslatableEntity) element).getTranslation();
            }
        });
        dialog.setBlockOnOpen(true);
        dialog.setTitle("Select Binding Type");
        int result = dialog.open();

        if (result == Window.OK) {
            if (dialog.getResult().length == 1) { // multi selection not allowed
                TranslatableEntity selected = (TranslatableEntity) dialog.getResult()[0];
                return selected;
            }
            return null;
        } else {
            return null;
        }
    }

    private void createSubSubTitle() {
        Label lblSubSubTitle = new Label(this, SWT.NONE);
        lblSubSubTitle.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblSubSubTitle.setText("Third Title:");

        txtSubSubTitle = new Text(this, SWT.BORDER);
        txtSubSubTitle.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
    }

    private void createSubTitle() {
        Label lblSubTitle = new Label(this, SWT.NONE);
        lblSubTitle.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblSubTitle.setText("Second Title:");

        txtSubTitle = new Text(this, SWT.BORDER);
        txtSubTitle.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
    }

    private void createTitle() {
        Label lblTitle = new Label(this, SWT.NONE);
        lblTitle.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblTitle.setText("Title:");

        txtTitle = new Text(this, SWT.BORDER);
        txtTitle.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
    }

    public DiagramModel getDiagramModel() {
        return diagramModel;
    }

    public void setDiagramModel(DiagramModel diagramModel) {
        this.diagramModel = diagramModel;
    }

    public void applyChanges() {
        m_bindingContext.updateModels();

        bindingModelOriginal.setAvailableBindingTypes(bindingModel.getAvailableBindingTypes());
        bindingModelOriginal.setUnboundVariables(bindingModel.getUnboundVariables());
        bindingModelOriginal.setVariableBindings(bindingModel.getVariableBindings());
    }

    protected DataBindingContext initDataBindings() {
        DataBindingContext bindingContext = new DataBindingContext();
        //
        IObservableValue observeTextTxtTitleObserveWidget = WidgetProperties.text(SWT.Modify).observe(txtTitle);
        IObservableValue titleDiagramModelObserveValue = BeanProperties.value("title").observe(diagramModel);
        bindingContext.bindValue(observeTextTxtTitleObserveWidget, titleDiagramModelObserveValue,
                new UpdateValueStrategy(UpdateValueStrategy.POLICY_ON_REQUEST), null);
        //
        IObservableValue observeTextTxtSubTitleObserveWidget = WidgetProperties.text(SWT.Modify).observe(txtSubTitle);
        IObservableValue subTitleDiagramModelObserveValue = BeanProperties.value("subTitle").observe(diagramModel);
        bindingContext.bindValue(observeTextTxtSubTitleObserveWidget, subTitleDiagramModelObserveValue,
                new UpdateValueStrategy(UpdateValueStrategy.POLICY_ON_REQUEST), null);
        //
        IObservableValue observeTextTxtSubSubTitleObserveWidget = WidgetProperties.text(SWT.Modify)
                .observe(txtSubSubTitle);
        IObservableValue subSubTitleDiagramModelObserveValue = BeanProperties.value("subSubTitle")
                .observe(diagramModel);
        bindingContext.bindValue(observeTextTxtSubSubTitleObserveWidget, subSubTitleDiagramModelObserveValue,
                new UpdateValueStrategy(UpdateValueStrategy.POLICY_ON_REQUEST), null);
        //
        ObservableListContentProvider listContentProvider = new ObservableListContentProvider();
        IObservableMap observeMap = PojoObservables.observeMap(listContentProvider.getKnownElements(),
                TranslatableEntity.class, "translation");
        listViewerUnboundVariables.setLabelProvider(new TranslatableEntityLabelProvider(observeMap));
        listViewerUnboundVariables.setContentProvider(listContentProvider);
        //
        IObservableList unboundVariablesBindingModelObserveList = BeanProperties.list("unboundVariables")
                .observe(bindingModel);
        listViewerUnboundVariables.setInput(unboundVariablesBindingModelObserveList);
        //
        ObservableListContentProvider listContentProvider_1 = new ObservableListContentProvider();
        IObservableMap[] observeMaps = PojoObservables.observeMaps(listContentProvider_1.getKnownElements(),
                VariableBinding.class, new String[] { "variable", "bindingType" });
        listViewerBoundVariables.setLabelProvider(new VariableBindingLabelProvider(observeMaps));
        listViewerBoundVariables.setContentProvider(listContentProvider_1);
        //
        IObservableList variableBindingsBindingModelObserveList = BeanProperties.list("variableBindings")
                .observe(bindingModel);
        listViewerBoundVariables.setInput(variableBindingsBindingModelObserveList);
        //
        IObservableValue observeSingleSelectionListViewerUnboundVariables = ViewerProperties.singleSelection()
                .observe(listViewerUnboundVariables);
        IObservableValue selectedUnboundVariableBindingModelObserveValue = BeanProperties
                .value("selectedUnboundVariable").observe(bindingModel);
        bindingContext.bindValue(observeSingleSelectionListViewerUnboundVariables,
                selectedUnboundVariableBindingModelObserveValue, null, null);
        //
        IObservableValue observeSingleSelectionListViewerBoundVariables = ViewerProperties.singleSelection()
                .observe(listViewerBoundVariables);
        IObservableValue selectedVariableBindingBindingModelObserveValue = BeanProperties
                .value("selectedVariableBinding").observe(bindingModel);
        bindingContext.bindValue(observeSingleSelectionListViewerBoundVariables,
                selectedVariableBindingBindingModelObserveValue, null, null);
        //
        return bindingContext;
    }
}
