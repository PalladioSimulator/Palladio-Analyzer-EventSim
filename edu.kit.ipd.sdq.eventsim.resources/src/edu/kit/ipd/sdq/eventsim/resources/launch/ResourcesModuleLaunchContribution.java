package edu.kit.ipd.sdq.eventsim.resources.launch;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import edu.kit.ipd.sdq.eventsim.modules.AbstractLaunchContribution;

public class ResourcesModuleLaunchContribution extends AbstractLaunchContribution {

    /**
     * @wbp.parser.entryPoint
     */
    @Override
    public void createControl(Composite parent) {
        final Composite container = new Composite(parent, SWT.NONE);
        this.setControl(container);
        container.setLayout(new GridLayout());
        
        Group grpMeasurements = new Group(container, SWT.NONE);
        grpMeasurements.setLayout(new GridLayout(1, false));
        grpMeasurements.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        grpMeasurements.setText("Measurement Options");
        
        Button btnCalcResourceUtilization = new Button(grpMeasurements, SWT.CHECK);
        btnCalcResourceUtilization.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
        btnCalcResourceUtilization.setText("Calculate Resource Utilization Metric");
        
        Button btnCalcMeanQueueLength = new Button(grpMeasurements, SWT.CHECK);
        btnCalcMeanQueueLength.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
        btnCalcMeanQueueLength.setText("Calculate Mean Queue Length Metric");
        
        Button btnDeleteRawQueueLength = new Button(grpMeasurements, SWT.CHECK);
        btnDeleteRawQueueLength.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
        btnDeleteRawQueueLength.setText("Delete Raw Queue Length Observations");
    }

    @Override
    public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
        // TODO Auto-generated method stub

    }

    @Override
    public void initializeFrom(ILaunchConfiguration configuration) {
        // TODO Auto-generated method stub

    }

    @Override
    public void performApply(ILaunchConfigurationWorkingCopy configuration) {
        // TODO Auto-generated method stub

    }

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return null;
    }
}
