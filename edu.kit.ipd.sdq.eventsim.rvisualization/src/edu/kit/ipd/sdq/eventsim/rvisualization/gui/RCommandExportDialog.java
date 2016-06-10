package edu.kit.ipd.sdq.eventsim.rvisualization.gui;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Button;

/**
 * Dialog for exporting R commands.
 * 
 * @author Benjamin Rupp
 *
 */
public class RCommandExportDialog extends Dialog {

	private String command;
	private Text rCommand;

	/**
	 * Create a R command export dialog.
	 * 
	 * @param parent
	 *            Parent element.
	 */
	public RCommandExportDialog(Shell parent) {
		super(parent);
	}

	/**
	 * Open the dialog.
	 * 
	 * @return Result object.
	 */
	public Object open() {

		Shell parent = getParent();
		Shell shlTest = new Shell(parent, SWT.DIALOG_TRIM | SWT.PRIMARY_MODAL);
		shlTest.setLayout(new GridLayout(1, false));
		shlTest.setSize(500, 250);
		shlTest.setText("Export R command");

		shlTest.setLocation(parent.getBounds().x
				+ (parent.getBounds().width - shlTest.getBounds().width) / 2,
				parent.getBounds().y + (parent.getBounds().height
						- shlTest.getBounds().height) / 2);

		Label lblTheFollowingR = new Label(shlTest, SWT.NONE);
		lblTheFollowingR.setLayoutData(
				new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		lblTheFollowingR.setText(
				"The following R command was used to plot the diagram:");

		Button btnCopyToClipboad = new Button(shlTest, SWT.NONE);
		btnCopyToClipboad.setLayoutData(
				new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		btnCopyToClipboad.setText("Copy to clipboad");

		btnCopyToClipboad.addListener(SWT.Selection, new Listener() {

			@Override
			public void handleEvent(Event event) {
				if (event.widget == btnCopyToClipboad) {

					Toolkit.getDefaultToolkit().getSystemClipboard()
							.setContents(new StringSelection(command), null);

				}
			}
		});

		rCommand = new Text(shlTest, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
		rCommand.setEditable(false);
		rCommand.setLayoutData(
				new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		rCommand.setText(command);

		shlTest.open();
		Display display = parent.getDisplay();
		while (!shlTest.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return shlTest;

	}

	/**
	 * Set command which should be displayed.
	 * 
	 * @param cmd
	 *            R command string.
	 */
	public void setCommand(String cmd) {
		this.command = cmd;

		if (this.rCommand != null) {
			this.rCommand.setText(cmd);
		}
	}
}