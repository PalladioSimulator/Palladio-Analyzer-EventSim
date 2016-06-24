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

import edu.kit.ipd.sdq.eventsim.rvisualization.model.StatisticsModel;

public class StatisticsViewer extends Composite {
    
    private DataBindingContext m_bindingContext;
    
    private Text txtObservations;
    private Text txtMin;
    private Text txtFirstQuartile;
    private Text txtMedian;
    private Text txtMean;
    private Text txtThirdQuartile;
    private Text txtMax;

    private StatisticsModel model = new StatisticsModel();

    public StatisticsViewer(Composite parent, int style) {
        super(parent, style);
        setLayout(new GridLayout(2, false));

        Label lblObservations = new Label(this, SWT.NONE);
        lblObservations.setText("Observations:");

        txtObservations = new Text(this, SWT.READ_ONLY);
        txtObservations.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        Label label = new Label(this, SWT.SEPARATOR | SWT.HORIZONTAL);
        label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

        Label lblMin = new Label(this, SWT.NONE);
        lblMin.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblMin.setText("Min:");

        txtMin = new Text(this, SWT.READ_ONLY);
        txtMin.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        Label lblFirstQuartile = new Label(this, SWT.NONE);
        lblFirstQuartile.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblFirstQuartile.setText("1st Quartile:");

        txtFirstQuartile = new Text(this, SWT.READ_ONLY);
        txtFirstQuartile.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        Label lblMedian = new Label(this, SWT.NONE);
        lblMedian.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblMedian.setText("Median:");

        txtMedian = new Text(this, SWT.READ_ONLY);
        txtMedian.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        Label lblMean = new Label(this, SWT.NONE);
        lblMean.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblMean.setText("Mean:");

        txtMean = new Text(this, SWT.READ_ONLY);
        txtMean.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        Label lblThirdQuartile = new Label(this, SWT.NONE);
        lblThirdQuartile.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblThirdQuartile.setText("3rd Quartile:");

        txtThirdQuartile = new Text(this, SWT.READ_ONLY);
        txtThirdQuartile.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        Label lblMax = new Label(this, SWT.NONE);
        lblMax.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblMax.setText("Max:");

        txtMax = new Text(this, SWT.READ_ONLY);
        txtMax.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        m_bindingContext = initDataBindings();
    }

    public StatisticsModel getModel() {
        return model;
    }

    protected DataBindingContext initDataBindings() {
        DataBindingContext bindingContext = new DataBindingContext();
        //
        IObservableValue observeTextTxtObservationsObserveWidget = WidgetProperties.text(SWT.Modify)
                .observe(txtObservations);
        IObservableValue observationsModelObserveValue = BeanProperties.value("observations").observe(model);
        bindingContext.bindValue(observeTextTxtObservationsObserveWidget, observationsModelObserveValue,
                new UpdateValueStrategy(UpdateValueStrategy.POLICY_NEVER), null);
        //
        IObservableValue observeTextTxtMinObserveWidget = WidgetProperties.text(SWT.Modify).observe(txtMin);
        IObservableValue minModelObserveValue = BeanProperties.value("min").observe(model);
        bindingContext.bindValue(observeTextTxtMinObserveWidget, minModelObserveValue,
                new UpdateValueStrategy(UpdateValueStrategy.POLICY_NEVER), null);
        //
        IObservableValue observeTextTxtFirstQuartileObserveWidget = WidgetProperties.text(SWT.Modify)
                .observe(txtFirstQuartile);
        IObservableValue firstQuartileModelObserveValue = BeanProperties.value("firstQuartile").observe(model);
        bindingContext.bindValue(observeTextTxtFirstQuartileObserveWidget, firstQuartileModelObserveValue,
                new UpdateValueStrategy(UpdateValueStrategy.POLICY_NEVER), null);
        //
        IObservableValue observeTextTxtMedianObserveWidget = WidgetProperties.text(SWT.Modify).observe(txtMedian);
        IObservableValue medianModelObserveValue = BeanProperties.value("median").observe(model);
        bindingContext.bindValue(observeTextTxtMedianObserveWidget, medianModelObserveValue,
                new UpdateValueStrategy(UpdateValueStrategy.POLICY_NEVER), null);
        //
        IObservableValue observeTextTxtMeanObserveWidget = WidgetProperties.text(SWT.Modify).observe(txtMean);
        IObservableValue meanModelObserveValue = BeanProperties.value("mean").observe(model);
        bindingContext.bindValue(observeTextTxtMeanObserveWidget, meanModelObserveValue,
                new UpdateValueStrategy(UpdateValueStrategy.POLICY_NEVER), null);
        //
        IObservableValue observeTextTxtThirdQuartileObserveWidget = WidgetProperties.text(SWT.Modify)
                .observe(txtThirdQuartile);
        IObservableValue thirdQuartileModelObserveValue = BeanProperties.value("thirdQuartile").observe(model);
        bindingContext.bindValue(observeTextTxtThirdQuartileObserveWidget, thirdQuartileModelObserveValue,
                new UpdateValueStrategy(UpdateValueStrategy.POLICY_NEVER), null);
        //
        IObservableValue observeTextTxtMaxObserveWidget = WidgetProperties.text(SWT.Modify).observe(txtMax);
        IObservableValue maxModelObserveValue = BeanProperties.value("max").observe(model);
        bindingContext.bindValue(observeTextTxtMaxObserveWidget, maxModelObserveValue,
                new UpdateValueStrategy(UpdateValueStrategy.POLICY_NEVER), null);
        //
        return bindingContext;
    }
}
