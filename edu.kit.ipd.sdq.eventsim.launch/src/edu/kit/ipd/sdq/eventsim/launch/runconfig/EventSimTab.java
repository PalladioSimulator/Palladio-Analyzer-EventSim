package edu.kit.ipd.sdq.eventsim.launch.runconfig;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import de.uka.ipd.sdq.workflow.launchconfig.tabs.TabHelper;

public class EventSimTab extends AbstractLaunchConfigurationTab {

    private Text instrumentationDescriptionLocation;

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

        instrumentationDescriptionLocation = new Text(container, SWT.SINGLE | SWT.BORDER);
        TabHelper.createFileInputSection(container, modifyListener, "Instrumentation Description File",
                EventSimConfigurationConstants.INSTRUMENTATION_FILE_EXTENSION, instrumentationDescriptionLocation,
                "Select Instrumentation Description File", getShell(),
                EventSimConfigurationConstants.INSTRUMENTATION_FILE_DEFAULT);
    }

    @Override
    public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
        configuration.setAttribute(EventSimConfigurationConstants.INSTRUMENTATION_FILE,
                EventSimConfigurationConstants.INSTRUMENTATION_FILE_DEFAULT);
    }

    @Override
    public void initializeFrom(ILaunchConfiguration configuration) {
        try {
            instrumentationDescriptionLocation
                    .setText(configuration.getAttribute(EventSimConfigurationConstants.INSTRUMENTATION_FILE,
                            EventSimConfigurationConstants.INSTRUMENTATION_FILE_DEFAULT));
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void performApply(ILaunchConfigurationWorkingCopy configuration) {
        configuration.setAttribute(EventSimConfigurationConstants.INSTRUMENTATION_FILE,
                instrumentationDescriptionLocation.getText());

    }

    @Override
    public String getName() {
        return "EventSim";
    }
}
