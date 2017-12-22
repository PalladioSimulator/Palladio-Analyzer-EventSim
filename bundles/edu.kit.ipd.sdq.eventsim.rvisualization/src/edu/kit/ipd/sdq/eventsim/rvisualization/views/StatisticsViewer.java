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
    private Text txtQuantile1;
    private Text txtQuantile2;
    private Text txtQuantile3;
    private Text txtQuantile4;
    private Text txtQuantile5;
    private Text txtQuantile6;
    private Text txtQuantile7;
    private Text txtQuantile8;
    private Text txtQuantile9;

    public StatisticsViewer(Composite parent, int style) {
        super(parent, style);
        setLayout(new GridLayout(2, false));

        Label lblObservations = new Label(this, SWT.NONE);
        lblObservations.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
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
        
        Label label_1 = new Label(this, SWT.SEPARATOR | SWT.HORIZONTAL);
        label_1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
        
        Label lblQuantile1 = new Label(this, SWT.NONE);
        lblQuantile1.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblQuantile1.setText("1st Quantile (10%):");
        
        txtQuantile1 = new Text(this, SWT.READ_ONLY);
        txtQuantile1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        
        Label lblQuantile2 = new Label(this, SWT.NONE);
        lblQuantile2.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblQuantile2.setText("2nd Quantile (20%):");
        
        txtQuantile2 = new Text(this, SWT.READ_ONLY);
        txtQuantile2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        
        Label lblQuantile3 = new Label(this, SWT.NONE);
        lblQuantile3.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblQuantile3.setText("3rd Quantile (30%):");
        
        txtQuantile3 = new Text(this, SWT.READ_ONLY);
        txtQuantile3.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        
        Label lblQuantile4 = new Label(this, SWT.NONE);
        lblQuantile4.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblQuantile4.setText("4th Quantile (40%):");
        
        txtQuantile4 = new Text(this, SWT.READ_ONLY);
        txtQuantile4.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        
        Label lblQuantile5 = new Label(this, SWT.NONE);
        lblQuantile5.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblQuantile5.setText("5th Quantile (50%):");
        
        txtQuantile5 = new Text(this, SWT.READ_ONLY);
        txtQuantile5.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        
        Label lblQuantile6 = new Label(this, SWT.NONE);
        lblQuantile6.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblQuantile6.setText("6th Quantile (60%):");
        
        txtQuantile6 = new Text(this, SWT.READ_ONLY);
        txtQuantile6.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        
        Label lblQuantile7 = new Label(this, SWT.NONE);
        lblQuantile7.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblQuantile7.setText("7th Quantile (70%):");
        
        txtQuantile7 = new Text(this, SWT.READ_ONLY);
        txtQuantile7.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        
        Label lblQuantile8 = new Label(this, SWT.NONE);
        lblQuantile8.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblQuantile8.setText("8th Quantile (80%):");
        
        txtQuantile8 = new Text(this, SWT.READ_ONLY);
        txtQuantile8.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        
        Label lblQuantile9 = new Label(this, SWT.NONE);
        lblQuantile9.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblQuantile9.setText("9th Quantile (90%):");
        
        txtQuantile9 = new Text(this, SWT.READ_ONLY);
        txtQuantile9.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        m_bindingContext = initDataBindings();
    }

    public StatisticsModel getModel() {
        return model;
    }
    protected DataBindingContext initDataBindings() {
        DataBindingContext bindingContext = new DataBindingContext();
        //
        IObservableValue observeTextTxtObservationsObserveWidget = WidgetProperties.text(SWT.Modify).observe(txtObservations);
        IObservableValue observationsModelObserveValue = BeanProperties.value("observations").observe(model);
        bindingContext.bindValue(observeTextTxtObservationsObserveWidget, observationsModelObserveValue, new UpdateValueStrategy(UpdateValueStrategy.POLICY_NEVER), null);
        //
        IObservableValue observeTextTxtMinObserveWidget = WidgetProperties.text(SWT.Modify).observe(txtMin);
        IObservableValue minModelObserveValue = BeanProperties.value("min").observe(model);
        bindingContext.bindValue(observeTextTxtMinObserveWidget, minModelObserveValue, new UpdateValueStrategy(UpdateValueStrategy.POLICY_NEVER), null);
        //
        IObservableValue observeTextTxtFirstQuartileObserveWidget = WidgetProperties.text(SWT.Modify).observe(txtFirstQuartile);
        IObservableValue firstQuartileModelObserveValue = BeanProperties.value("firstQuartile").observe(model);
        bindingContext.bindValue(observeTextTxtFirstQuartileObserveWidget, firstQuartileModelObserveValue, new UpdateValueStrategy(UpdateValueStrategy.POLICY_NEVER), null);
        //
        IObservableValue observeTextTxtMedianObserveWidget = WidgetProperties.text(SWT.Modify).observe(txtMedian);
        IObservableValue medianModelObserveValue = BeanProperties.value("median").observe(model);
        bindingContext.bindValue(observeTextTxtMedianObserveWidget, medianModelObserveValue, new UpdateValueStrategy(UpdateValueStrategy.POLICY_NEVER), null);
        //
        IObservableValue observeTextTxtMeanObserveWidget = WidgetProperties.text(SWT.Modify).observe(txtMean);
        IObservableValue meanModelObserveValue = BeanProperties.value("mean").observe(model);
        bindingContext.bindValue(observeTextTxtMeanObserveWidget, meanModelObserveValue, new UpdateValueStrategy(UpdateValueStrategy.POLICY_NEVER), null);
        //
        IObservableValue observeTextTxtThirdQuartileObserveWidget = WidgetProperties.text(SWT.Modify).observe(txtThirdQuartile);
        IObservableValue thirdQuartileModelObserveValue = BeanProperties.value("thirdQuartile").observe(model);
        bindingContext.bindValue(observeTextTxtThirdQuartileObserveWidget, thirdQuartileModelObserveValue, new UpdateValueStrategy(UpdateValueStrategy.POLICY_NEVER), null);
        //
        IObservableValue observeTextTxtMaxObserveWidget = WidgetProperties.text(SWT.Modify).observe(txtMax);
        IObservableValue maxModelObserveValue = BeanProperties.value("max").observe(model);
        bindingContext.bindValue(observeTextTxtMaxObserveWidget, maxModelObserveValue, new UpdateValueStrategy(UpdateValueStrategy.POLICY_NEVER), null);
        //
        IObservableValue observeTextTxtQuantile1ObserveWidget = WidgetProperties.text(SWT.Modify).observe(txtQuantile1);
        IObservableValue quantile1ModelObserveValue = BeanProperties.value("quantile1").observe(model);
        bindingContext.bindValue(observeTextTxtQuantile1ObserveWidget, quantile1ModelObserveValue, new UpdateValueStrategy(UpdateValueStrategy.POLICY_NEVER), null);
        //
        IObservableValue observeTextTxtQuantile2ObserveWidget = WidgetProperties.text(SWT.Modify).observe(txtQuantile2);
        IObservableValue quantile2ModelObserveValue = BeanProperties.value("quantile2").observe(model);
        bindingContext.bindValue(observeTextTxtQuantile2ObserveWidget, quantile2ModelObserveValue, new UpdateValueStrategy(UpdateValueStrategy.POLICY_NEVER), null);
        //
        IObservableValue observeTextTxtQuantile3ObserveWidget = WidgetProperties.text(SWT.Modify).observe(txtQuantile3);
        IObservableValue quantile3ModelObserveValue = BeanProperties.value("quantile3").observe(model);
        bindingContext.bindValue(observeTextTxtQuantile3ObserveWidget, quantile3ModelObserveValue, new UpdateValueStrategy(UpdateValueStrategy.POLICY_NEVER), null);
        //
        IObservableValue observeTextTxtQuantile4ObserveWidget = WidgetProperties.text(SWT.Modify).observe(txtQuantile4);
        IObservableValue quantile4ModelObserveValue = BeanProperties.value("quantile4").observe(model);
        bindingContext.bindValue(observeTextTxtQuantile4ObserveWidget, quantile4ModelObserveValue, new UpdateValueStrategy(UpdateValueStrategy.POLICY_NEVER), null);
        //
        IObservableValue observeTextTxtQuantile5ObserveWidget = WidgetProperties.text(SWT.Modify).observe(txtQuantile5);
        IObservableValue quantile5ModelObserveValue = BeanProperties.value("quantile5").observe(model);
        bindingContext.bindValue(observeTextTxtQuantile5ObserveWidget, quantile5ModelObserveValue, new UpdateValueStrategy(UpdateValueStrategy.POLICY_NEVER), null);
        //
        IObservableValue observeTextTxtQuantile6ObserveWidget = WidgetProperties.text(SWT.Modify).observe(txtQuantile6);
        IObservableValue quantile6ModelObserveValue = BeanProperties.value("quantile6").observe(model);
        bindingContext.bindValue(observeTextTxtQuantile6ObserveWidget, quantile6ModelObserveValue, new UpdateValueStrategy(UpdateValueStrategy.POLICY_NEVER), null);
        //
        IObservableValue observeTextTxtQuantile7ObserveWidget = WidgetProperties.text(SWT.Modify).observe(txtQuantile7);
        IObservableValue quantile7ModelObserveValue = BeanProperties.value("quantile7").observe(model);
        bindingContext.bindValue(observeTextTxtQuantile7ObserveWidget, quantile7ModelObserveValue, new UpdateValueStrategy(UpdateValueStrategy.POLICY_NEVER), null);
        //
        IObservableValue observeTextTxtQuantile8ObserveWidget = WidgetProperties.text(SWT.Modify).observe(txtQuantile8);
        IObservableValue quantile8ModelObserveValue = BeanProperties.value("quantile8").observe(model);
        bindingContext.bindValue(observeTextTxtQuantile8ObserveWidget, quantile8ModelObserveValue, new UpdateValueStrategy(UpdateValueStrategy.POLICY_NEVER), null);
        //
        IObservableValue observeTextTxtQuantile9ObserveWidget = WidgetProperties.text(SWT.Modify).observe(txtQuantile9);
        IObservableValue quantile9ModelObserveValue = BeanProperties.value("quantile9").observe(model);
        bindingContext.bindValue(observeTextTxtQuantile9ObserveWidget, quantile9ModelObserveValue, new UpdateValueStrategy(UpdateValueStrategy.POLICY_NEVER), null);
        //
        return bindingContext;
    }
}
