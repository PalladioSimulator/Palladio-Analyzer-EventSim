package edu.kit.ipd.sdq.eventsim.rvisualization.views;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import edu.kit.ipd.sdq.eventsim.rvisualization.model.DiagramModel;

import org.eclipse.swt.layout.FillLayout;

public class DiagramSettingsDialog extends TitleAreaDialog  {

    private DiagramModel diagramModel;
    
    private DiagramSettingsViewer viewer;
    
    public DiagramSettingsDialog(Shell parentShell, DiagramModel diagramModel) {
        super(parentShell);
        this.diagramModel = diagramModel;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite contents = new Composite(parent, SWT.NONE);
        contents.setLayout(new FillLayout(SWT.HORIZONTAL));
        
        setTitle("Diagram Settings");
        
        viewer = new DiagramSettingsViewer(contents, SWT.NONE, diagramModel);
        
        return contents;
    }

    @Override
    protected void okPressed() {
        viewer.applyChanges();
        super.okPressed();
    }
    
    
    
    
}
