package edu.kit.ipd.sdq.eventsim.rvisualization.views;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import edu.kit.ipd.sdq.eventsim.rvisualization.model.DiagramModel;

public class DiagramSettingsViewer extends Composite {
    
    private DataBindingContext m_bindingContext;

    private Text txtTitle;
    
    private DiagramModel diagramModel;
    private Text txtSubTitle;
    private Text txtSubSubTitle;

    public DiagramSettingsViewer(Composite parent, int style, DiagramModel diagramModel) {
        super(parent, style);
        this.diagramModel = diagramModel;
        setLayout(new GridLayout(2, false));

        Label lblTitle = new Label(this, SWT.NONE);
        lblTitle.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblTitle.setText("Title:");

        txtTitle = new Text(this, SWT.BORDER);
        txtTitle.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        
        Label lblSubTitle = new Label(this, SWT.NONE);
        lblSubTitle.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblSubTitle.setText("Second Title:");
        
        txtSubTitle = new Text(this, SWT.BORDER);
        txtSubTitle.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        
        Label lblSubSubTitle = new Label(this, SWT.NONE);
        lblSubSubTitle.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblSubSubTitle.setText("Third Title:");
        
        txtSubSubTitle = new Text(this, SWT.BORDER);
        txtSubSubTitle.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        
        m_bindingContext = initDataBindings();
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
        bindingContext.bindValue(observeTextTxtTitleObserveWidget, titleDiagramModelObserveValue, new UpdateValueStrategy(UpdateValueStrategy.POLICY_ON_REQUEST), null);
        //
        IObservableValue observeTextTxtSubTitleObserveWidget = WidgetProperties.text(SWT.Modify).observe(txtSubTitle);
        IObservableValue subTitleDiagramModelObserveValue = BeanProperties.value("subTitle").observe(diagramModel);
        bindingContext.bindValue(observeTextTxtSubTitleObserveWidget, subTitleDiagramModelObserveValue, new UpdateValueStrategy(UpdateValueStrategy.POLICY_ON_REQUEST), null);
        //
        IObservableValue observeTextTxtSubSubTitleObserveWidget = WidgetProperties.text(SWT.Modify).observe(txtSubSubTitle);
        IObservableValue subSubTitleDiagramModelObserveValue = BeanProperties.value("subSubTitle").observe(diagramModel);
        bindingContext.bindValue(observeTextTxtSubSubTitleObserveWidget, subSubTitleDiagramModelObserveValue, new UpdateValueStrategy(UpdateValueStrategy.POLICY_ON_REQUEST), null);
        //
        return bindingContext;
    }
}
