package edu.kit.ipd.sdq.eventsim.instrumentation.specification.wizard;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.URI;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;

public class InstrumentationDescriptionCreationWizardPage extends WizardNewFileCreationPage {

    private final String fileExtension;

    public InstrumentationDescriptionCreationWizardPage(String pageName, IStructuredSelection selection,
            String fileExtension) {
        super(pageName, selection);
        this.fileExtension = fileExtension;
    }

    protected String getExtension() {
        return fileExtension;
    }

    public URI getURI() {
        return URI.createPlatformResourceURI(getFilePath().toString(), false);
    }

    protected IPath getFilePath() {
        IPath path = getContainerFullPath();
        if (path == null) {
            path = new Path(""); //$NON-NLS-1$
        }
        String fileName = getFileName();
        if (fileName != null) {
            path = path.append(fileName);
        }
        return path;
    }

    public void createControl(Composite parent) {
        super.createControl(parent);
        setFileName("my." + fileExtension);
        setPageComplete(validatePage());
    }

    protected boolean validatePage() {
        if (!super.validatePage()) {
            return false;
        }
        String extension = getExtension();
        if (extension != null && !getFilePath().toString().endsWith("." + extension)) {
            setErrorMessage("File name should have " + extension + " extension.");
            return false;
        }
        return true;
    }
    
}
