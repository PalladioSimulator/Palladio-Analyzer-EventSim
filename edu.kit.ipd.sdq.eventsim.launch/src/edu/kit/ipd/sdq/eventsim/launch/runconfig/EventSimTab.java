package edu.kit.ipd.sdq.eventsim.launch.runconfig;

import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.osgi.framework.Bundle;

import de.uka.ipd.sdq.workflow.launchconfig.tabs.TabHelper;
import edu.kit.ipd.sdq.eventsim.launch.Activator;
import edu.kit.ipd.sdq.eventsim.modules.ILaunchContribution;
import edu.kit.ipd.sdq.eventsim.modules.SimulationModule;
import edu.kit.ipd.sdq.eventsim.modules.SimulationModuleRegistry;

public class EventSimTab extends AbstractLaunchConfigurationTab {

    private Text instrumentationDescriptionLocation;
    private Table modulesTable;
    private Group grpModules;

    private SimulationModuleRegistry moduleRegistry;

    /** the set of enabled simulation modules, identified by their id */
    private Set<String> enabledModules;

    private Map<String, TableItem> tableItemsMap;

    private Image checkedImage;
    private Image uncheckedImage;
    private Image tabImage;

    public EventSimTab() {
        this.enabledModules = new HashSet<>();
        this.tableItemsMap = new HashMap<>();
        this.moduleRegistry = SimulationModuleRegistry.createFrom(Platform.getExtensionRegistry());
    }

    /**
     * @wbp.parser.entryPoint
     */
    @Override
    public void createControl(Composite parent) {
        checkedImage = getImage("plugin.png");
        uncheckedImage = getImage("plugin_disabled.png");
        tabImage = getImage("package_green.png");

        final ModifyListener modifyListener = new ModifyListener() {
            @Override
            public void modifyText(final ModifyEvent e) {
                setDirty(true);
                updateLaunchConfigurationDialog();
            }
        };

        Composite container = new Composite(parent, SWT.NONE);
        this.setControl(container);
        container.setLayout(new GridLayout());

        instrumentationDescriptionLocation = new Text(container, SWT.SINGLE | SWT.BORDER);
        TabHelper.createFileInputSection(container, modifyListener, "Instrumentation Description File",
                EventSimConfigurationConstants.INSTRUMENTATION_FILE_EXTENSION, instrumentationDescriptionLocation,
                "Select Instrumentation Description File", getShell(),
                EventSimConfigurationConstants.INSTRUMENTATION_FILE_DEFAULT);

        grpModules = new Group(container, SWT.NONE);
        grpModules.setLayout(new GridLayout(2, false));
        grpModules.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        grpModules.setText("Simulation Modules");

        modulesTable = new Table(grpModules, SWT.BORDER | SWT.CHECK | SWT.FULL_SELECTION);
        GridData gd_table = new GridData(SWT.FILL, SWT.FILL, false, true, 1, 1);
        gd_table.widthHint = 300;
        modulesTable.setLayoutData(gd_table);
        modulesTable.setHeaderVisible(true);
        modulesTable.setLinesVisible(true);

        TableColumn tblclmnModule = new TableColumn(modulesTable, SWT.NONE);
        tblclmnModule.setWidth(200);
        tblclmnModule.setText("Module");

        TableColumn tblclmnPriority = new TableColumn(modulesTable, SWT.NONE);
        tblclmnPriority.setWidth(100);
        tblclmnPriority.setText("Priority");

        Composite contributionsContainer = new Composite(grpModules, SWT.NONE);
        contributionsContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        StackLayout contributionsLayout = new StackLayout();
        contributionsContainer.setLayout(contributionsLayout);

        modulesTable.addListener(SWT.Selection, new Listener() {

            @Override
            public void handleEvent(Event event) {
                TableItem selectedItem = (TableItem) event.item;
                SimulationModule selectedModule = moduleRegistry.getModuleByName(selectedItem.getText(0));

                // raised by checking/unchecking?
                if (event.detail == SWT.CHECK) {
                    boolean checked = selectedItem.getChecked();
                    if (checked) {
                        enabledModules.add(selectedModule.getId());
                        selectedItem.setImage(checkedImage);
                    } else {
                        enabledModules.remove(selectedModule.getId());
                        selectedItem.setImage(uncheckedImage);
                    }
                    setDirty(true);
                    updateLaunchConfigurationDialog();
                } else {
                    // bring selected module's contribution to top in stack layout
                    if (selectedModule.getLaunchContribution() != null) {
                        contributionsLayout.topControl = selectedModule.getLaunchContribution().getControl();
                        contributionsContainer.layout();
                    }
                }
            }

        });

        for (SimulationModule module : moduleRegistry.getModules()) {
            TableItem tableItem = new TableItem(modulesTable, SWT.NONE);
            tableItem.setText(new String[] { module.getName(), Integer.toString(module.getPriority()) });
            tableItem.setImage(checkedImage);

            if (module.getLaunchContribution() != null) {
                ILaunchContribution contribution = module.getLaunchContribution();
                contribution.createControl(contributionsContainer);
                contribution.addDirtyListener((observable, arg) -> {
                    setDirty(true);
                    updateLaunchConfigurationDialog();
                });
            }
            tableItemsMap.put(module.getId(), tableItem);
        }

    }

    /**
     * @see http://www.vogella.com/tutorials/EclipseJFaceTable/article.html
     */
    private static Image getImage(String file) {
        Bundle bundle = Activator.getDefault().getBundle();
        URL url = FileLocator.find(bundle, new Path("icons/" + file), null);
        ImageDescriptor image = ImageDescriptor.createFromURL(url);
        return image.createImage();
    }

    @Override
    public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
        // delegate to simulation module contributions
        moduleRegistry.getModules().forEach(m -> m.getLaunchContribution().setDefaults(configuration));

        // enable all simulation modules
        enabledModules = new HashSet<>(getSimulationModulesEnabledDefault());

        configuration.setAttribute(EventSimConfigurationConstants.INSTRUMENTATION_FILE,
                EventSimConfigurationConstants.INSTRUMENTATION_FILE_DEFAULT);
    }

    private Set<String> getSimulationModulesEnabledDefault() {
        return moduleRegistry.getModules().stream().map(m -> m.getId()).collect(Collectors.toSet());
    }

    @Override
    public void initializeFrom(ILaunchConfiguration configuration) {
        try {
            // delegate to simulation module contributions
            moduleRegistry.getModules().forEach(m -> m.getLaunchContribution().initializeFrom(configuration));

            // enabled simulation modules
            enabledModules = new HashSet<>(configuration.getAttribute(EventSimConfigurationConstants.ENABLED_MODULES,
                    getSimulationModulesEnabledDefault()));
            // uncheck all table items, first
            tableItemsMap.values().forEach(item -> {
                item.setChecked(false);
                item.setImage(uncheckedImage);
            });
            for (String moduleId : enabledModules) {
                TableItem item = tableItemsMap.get(moduleId);
                item.setChecked(true);
                item.setImage(checkedImage);
            }

            instrumentationDescriptionLocation
                    .setText(configuration.getAttribute(EventSimConfigurationConstants.INSTRUMENTATION_FILE,
                            EventSimConfigurationConstants.INSTRUMENTATION_FILE_DEFAULT));
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void performApply(ILaunchConfigurationWorkingCopy configuration) {
        // delegate to simulation module contributions
        moduleRegistry.getModules().forEach(m -> m.getLaunchContribution().performApply(configuration));

        // save enabled modules
        configuration.setAttribute(EventSimConfigurationConstants.ENABLED_MODULES, enabledModules);

        configuration.setAttribute(EventSimConfigurationConstants.INSTRUMENTATION_FILE,
                instrumentationDescriptionLocation.getText());

    }

    @Override
    public String getName() {
        return "EventSim";
    }

    @Override
    public Image getImage() {
        return tabImage;
    }

    @Override
    public void dispose() {
        // delegate to simulation module contributions
        moduleRegistry.getModules().forEach(m -> m.getLaunchContribution().dispose());

        if (checkedImage != null) {
            checkedImage.dispose();
        }
        if (uncheckedImage != null) {
            uncheckedImage.dispose();
        }
        super.dispose();
    }

}
