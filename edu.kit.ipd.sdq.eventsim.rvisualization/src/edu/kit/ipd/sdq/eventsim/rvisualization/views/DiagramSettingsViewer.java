package edu.kit.ipd.sdq.eventsim.rvisualization.views;

import java.util.ArrayList;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.beans.PojoObservables;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.map.IObservableMap;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.StructuredSelection;
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

import edu.kit.ipd.sdq.eventsim.rvisualization.ggplot.Aesthetic;
import edu.kit.ipd.sdq.eventsim.rvisualization.model.DiagramModel;
import edu.kit.ipd.sdq.eventsim.rvisualization.model.TranslatableEntity;
import edu.kit.ipd.sdq.eventsim.rvisualization.model.VariableBinding;
import edu.kit.ipd.sdq.eventsim.rvisualization.model.provider.TranslatableEntityLabelProvider;
import edu.kit.ipd.sdq.eventsim.rvisualization.model.provider.VariableBindingValueProvider;

public class DiagramSettingsViewer extends Composite {

    private DataBindingContext m_bindingContext;

    private Text txtTitle;

    private DiagramModel diagramModel;
    private Text txtSubTitle;
    private Text txtSubSubTitle;
    private Group grpVariableBindings;
    private List list;
    private ListViewer listViewer;
    private List list_1;
    private ListViewer listViewer_1;
    private Label lblUnboundVariables;
    private Label lblVariableBindings;
    private Button btnBind;
    private Button btnRemove;

    public DiagramSettingsViewer(Composite parent, int style, DiagramModel diagramModel) {
        super(parent, style);
        this.diagramModel = diagramModel;
        setLayout(new GridLayout(2, false));

        createTitle();
        createSubTitle();
        createSubSubTitle();

        m_bindingContext = initDataBindings();
    }

    private TranslatableEntity openBindingTypeSelectionDialog() {
        ListDialog dialog = new ListDialog(getParent().getShell());
        dialog.setHelpAvailable(false);
        dialog.setInput(diagramModel.getDiagramType().getAesthetics()); // TODO translatableentity
        dialog.setContentProvider(ArrayContentProvider.getInstance());
        dialog.setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
                return ((Aesthetic) element).name(); // TODO human-readable representation?
            }
        });
        dialog.setBlockOnOpen(true);
        dialog.setTitle("Select Binding Type");
        int result = dialog.open();

        if (result == Window.OK) {
            if (dialog.getResult().length == 1) { // multi selection not allowed
                String selected = ((Aesthetic) dialog.getResult()[0]).name();
                return new TranslatableEntity(selected, selected); // TODO
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

        listViewer = new ListViewer(grpVariableBindings, SWT.BORDER | SWT.V_SCROLL);
        list = listViewer.getList();
        list.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

        listViewer_1 = new ListViewer(grpVariableBindings, SWT.BORDER | SWT.V_SCROLL);
        list_1 = listViewer_1.getList();
        list_1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

        btnBind = new Button(grpVariableBindings, SWT.NONE);
        btnBind.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                TranslatableEntity selected = openBindingTypeSelectionDialog();
                if (selected != null) {
                    StructuredSelection variableSelection = (StructuredSelection) listViewer.getSelection();
                    TranslatableEntity selectedVariable = (TranslatableEntity) variableSelection.getFirstElement();

                    VariableBinding binding = new VariableBinding(selectedVariable.getName(), selected.getName());

                    java.util.List<VariableBinding> bindings = new ArrayList<>();
                    if (diagramModel.getVariableBindings() != null) {
                        bindings = new ArrayList<>(diagramModel.getVariableBindings());
                    }
                    bindings.add(binding);
                    diagramModel.setVariableBindings(bindings);
                }
            }
        });
        btnBind.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        btnBind.setText("Bind...");

        btnRemove = new Button(grpVariableBindings, SWT.NONE);
        btnRemove.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        btnRemove.setText("Remove");
        m_bindingContext = initDataBindings();
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
        listViewer.setLabelProvider(new TranslatableEntityLabelProvider(observeMap));
        listViewer.setContentProvider(listContentProvider);
        //
        IObservableList unboundVariablesDiagramModelObserveList = BeanProperties.list("unboundVariables")
                .observe(diagramModel);
        listViewer.setInput(unboundVariablesDiagramModelObserveList);
        //
        ObservableListContentProvider listContentProvider_1 = new ObservableListContentProvider();
        IObservableMap[] observeMaps = PojoObservables.observeMaps(listContentProvider_1.getKnownElements(),
                VariableBinding.class, new String[] { "variable", "bindingType" });
        listViewer_1.setLabelProvider(new VariableBindingValueProvider(observeMaps));
        listViewer_1.setContentProvider(listContentProvider_1);
        //
        IObservableList variableBindingsDiagramModelObserveList = BeanProperties.list("variableBindings")
                .observe(diagramModel);
        listViewer_1.setInput(variableBindingsDiagramModelObserveList);
        //
        return bindingContext;
    }
}
