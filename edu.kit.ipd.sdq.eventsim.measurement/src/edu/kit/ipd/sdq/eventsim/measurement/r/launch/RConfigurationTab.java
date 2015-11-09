package edu.kit.ipd.sdq.eventsim.measurement.r.launch;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class RConfigurationTab extends AbstractLaunchConfigurationTab {

	private static final String DEFAULT_RDS_FILENAME = "eventsim-results.rds";
	
	private Button createRdsCheckbox;
	private Label rdsLocationLabel;
	private Text rdsLocationField;
	private Button browseButton;

	protected String rdsFilePath;
	protected boolean createRdsFile;

	@Override
	public void createControl(final Composite parent) {
		final Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new GridLayout());
		setControl(container);

		final ModifyListener modifyListener = new ModifyListener() {
			@Override
			public void modifyText(final ModifyEvent e) {
				RConfigurationTab.this.setDirty(true);
				RConfigurationTab.this.updateLaunchConfigurationDialog();
			}
		};

		final Group group = new Group(container, SWT.NONE);
		group.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		final GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		group.setLayout(layout);
		group.setText("Rserve configuration");

		createRdsCheckbox = new Button(group, SWT.CHECK);
		createRdsCheckbox.setSelection(false);
		createRdsCheckbox.setText("Export measurements into RDS file after simulation.");
		createRdsCheckbox.setLayoutData(new GridData(SWT.DEFAULT, SWT.DEFAULT, false, false, 3, SWT.DEFAULT));
		createRdsCheckbox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				createRdsFile = createRdsCheckbox.getSelection();
				if(createRdsFile) {
					enableRdsFileSelection(true);
				} else {
					enableRdsFileSelection(false);
				}
				RConfigurationTab.this.setDirty(true);
				RConfigurationTab.this.updateLaunchConfigurationDialog();
			}
		});
		
		rdsLocationLabel = new Label(group, SWT.NONE);
		rdsLocationLabel.setText("RDS file:");
		rdsLocationLabel.setEnabled(false);

		rdsLocationField = new Text(group, SWT.BORDER | SWT.READ_ONLY);
		rdsLocationField.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		rdsLocationField.addModifyListener(modifyListener);
		rdsLocationField.setEnabled(false);

		browseButton = new Button(group, SWT.NONE);
		browseButton.setText("Browse...");
		browseButton.setEnabled(false);
		browseButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				FileDialog dialog = new FileDialog(e.display.getActiveShell(), SWT.SAVE);
				dialog.setFilterNames(new String[] { "RDS Files" });
				dialog.setFilterExtensions(new String[] { "*.rds" });
				dialog.setFileName(DEFAULT_RDS_FILENAME);
				String absolutePathString = dialog.open();
				if (absolutePathString != null) {
					rdsFilePath = absolutePathString;
					rdsLocationField.setText(rdsFilePath);
				} else {
					// cancelled dialog, do nothing
				}
			}
		});

	}
	
	private void enableRdsFileSelection(boolean enabled) {
		rdsLocationLabel.setEnabled(enabled);
		rdsLocationField.setEnabled(enabled);
		browseButton.setEnabled(enabled);
	}

	@Override
	public String getName() {
		// this name must match the recorder name provided in the extension point; otherwise, validation via
		// isValid(...) won't be called.
		return "Rserve";
	}

	@Override
	public void initializeFrom(final ILaunchConfiguration configuration) {
		try {
			createRdsFile = configuration.getAttribute(RConfigurationConstants.CREATE_RDS_FILE_KEY, false);
			rdsFilePath = configuration.getAttribute(RConfigurationConstants.RDS_FILE_PATH_KEY, createTempFileName());
		} catch (final CoreException e) {
			createRdsFile = false;
			rdsFilePath = createTempFileName();
		}
		createRdsCheckbox.setSelection(createRdsFile);
		rdsLocationField.setText(rdsFilePath);
		enableRdsFileSelection(createRdsFile);
	}

	@Override
	public void performApply(final ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(RConfigurationConstants.CREATE_RDS_FILE_KEY, createRdsFile);
		configuration.setAttribute(RConfigurationConstants.RDS_FILE_PATH_KEY, rdsFilePath);
	}

	@Override
	public void setDefaults(final ILaunchConfigurationWorkingCopy configuration) {
		// won't be invoked given the current implementation of de.uka.ipd.sdq.workflow.launchconfig.tabs.TabHelper
		// because the launch framework calls setDefault before (!) actually constructing the control. So this tab does
		// not exist at the time of setting the parent's defaults and no delegation to this control can happen.
	}

	private String createTempFileName() {
		try {
			File tempFile = File.createTempFile("eventsim-results", ".rds");
			rdsFilePath = tempFile.getAbsolutePath();
			rdsLocationField.setText(rdsFilePath);
			return tempFile.getAbsolutePath();
		} catch (IOException e) {
			throw new RuntimeException("Could not create temporary file.");
		}
	}

	@Override
	public boolean isValid(final ILaunchConfiguration launchConfig) {
		// nothing to do
		return true;
	}

}
