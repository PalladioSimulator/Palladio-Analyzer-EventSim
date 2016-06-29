package edu.kit.ipd.sdq.eventsim.modules;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.layout.GridData;

public class DefaultLaunchContribution extends AbstractLaunchConfigurationTab {

    /**
     * @wbp.parser.entryPoint
     */
    @Override
    public void createControl(Composite parent) {
        final Composite container = new Composite(parent, SWT.NONE);
        this.setControl(container);
        container.setLayout(new GridLayout());

        Label message = new Label(container, SWT.NONE);
        message.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, true, 1, 1));
        message.setText("No configuration options available for chosen module.");
    }

    @Override
    public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
        // nothing to do
    }

    @Override
    public void initializeFrom(ILaunchConfiguration configuration) {
        // nothing to do
    }

    @Override
    public void performApply(ILaunchConfigurationWorkingCopy configuration) {
        // nothing to do
    }

    @Override
    public String getName() {
        // won't be called
        return null;
    }

}
