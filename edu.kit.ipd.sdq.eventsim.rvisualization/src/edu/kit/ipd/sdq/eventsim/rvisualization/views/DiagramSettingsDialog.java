package edu.kit.ipd.sdq.eventsim.rvisualization.views;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import edu.kit.ipd.sdq.eventsim.rvisualization.model.DiagramModel;

import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;

public class DiagramSettingsDialog extends TitleAreaDialog {

    private DiagramModel diagramModel;

    private DiagramSettingsViewer viewer;

    public DiagramSettingsDialog(Shell parentShell, DiagramModel diagramModel) {
        super(parentShell);
        setShellStyle(getShellStyle() | SWT.RESIZE | SWT.MAX);
        this.diagramModel = diagramModel;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite area = (Composite) super.createDialogArea(parent);
        Composite contents = new Composite(area, SWT.NONE);
        contents.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        contents.setLayout(new FillLayout(SWT.HORIZONTAL));

        setHelpAvailable(false);
        setTitle("Diagram Settings");

        viewer = new DiagramSettingsViewer(contents, SWT.NONE, diagramModel);

        return area;
    }

    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText("Diagram Settings");
    }

    @Override
    protected void okPressed() {
        viewer.applyChanges();
        super.okPressed();
    }

}
