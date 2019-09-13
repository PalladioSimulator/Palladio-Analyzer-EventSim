package edu.kit.ipd.sdq.eventsim.extensionexample.launch;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import edu.kit.ipd.sdq.eventsim.modules.AbstractLaunchContribution;

public class ExampleModuleLaunchContribution extends AbstractLaunchContribution {

    private Text txtPrefix;

    /**
     * @wbp.parser.entryPoint
     */
    @Override
    public void createControl(Composite parent) {
        final ModifyListener modifyListener = new ModifyListener() {
            @Override
            public void modifyText(final ModifyEvent e) {
                setDirty(true);
                updateLaunchConfigurationDialog();
            }
        };
        
        final Composite container = new Composite(parent, SWT.NONE);
        this.setControl(container);
        container.setLayout(new GridLayout());

        Group grpExampleOptions = new Group(container, SWT.NONE);
        grpExampleOptions.setLayout(new GridLayout(2, false));
        grpExampleOptions.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        grpExampleOptions.setText("Example Options");

        Label lblCustomConsoleOutput = new Label(grpExampleOptions, SWT.NONE);
        lblCustomConsoleOutput.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblCustomConsoleOutput.setText("Custom prefix for console output:");

        txtPrefix = new Text(grpExampleOptions, SWT.BORDER);
        txtPrefix.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        txtPrefix.addModifyListener(modifyListener);
    }

    @Override
    public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
        configuration.setAttribute(ConfigurationConstants.CONSOLE_PREFIX,
                ConfigurationConstants.CONSOLE_PREFIX_DEFAULT);

    }

    @Override
    public void initializeFrom(ILaunchConfiguration configuration) {
        String prefix = "";
        try {
            prefix = configuration.getAttribute(ConfigurationConstants.CONSOLE_PREFIX,
                    ConfigurationConstants.CONSOLE_PREFIX_DEFAULT);
        } catch (CoreException e) {
            e.printStackTrace();
        }
        txtPrefix.setText(prefix);
    }

    @Override
    public void performApply(ILaunchConfigurationWorkingCopy configuration) {
        configuration.setAttribute(ConfigurationConstants.CONSOLE_PREFIX, txtPrefix.getText());
    }

    @Override
    public String getName() {
        // won't be used
        return null;
    }
}
