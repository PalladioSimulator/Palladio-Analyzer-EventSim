package edu.kit.ipd.sdq.eventsim.instrumentation.specification.restriction;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import de.uka.ipd.sdq.workflow.launchconfig.tabs.TabHelper;
import edu.kit.ipd.sdq.eventsim.instrumentation.specification.editor.InstrumentationDescriptionEditor;

public class RestrictionUIHelper {

	public static void createLoadModelSection(Composite parent, Shell shell, String[] fileExtensions,
			TextChosenListener listener) {
		Label label = new Label(parent, SWT.NONE);
		label.setText("Please select a model on which this instrumentation should base on.");
		label.setFont(InstrumentationDescriptionEditor.getActive().getOrCreateFont(label, SWT.BOLD));
		Text repositoryUriText = new Text(parent, SWT.NONE);
		TabHelper.createFileInputSection(parent, e -> listener.textChosen(repositoryUriText.getText()),
				"Repository Model", fileExtensions, repositoryUriText, "Select the Repository Model", shell, null);
	}

}
