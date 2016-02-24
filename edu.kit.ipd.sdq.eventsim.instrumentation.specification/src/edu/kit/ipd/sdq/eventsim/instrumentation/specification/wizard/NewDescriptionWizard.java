package edu.kit.ipd.sdq.eventsim.instrumentation.specification.wizard;

import javax.xml.bind.JAXBException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

import de.uka.ipd.sdq.pcm.gmf.repository.part.PalladioComponentModelCreationWizardPage;
import edu.kit.ipd.sdq.eventsim.instrumentation.description.core.InstrumentationDescription;
import edu.kit.ipd.sdq.eventsim.instrumentation.specification.editor.InstrumentationDescriptionEditor;
import edu.kit.ipd.sdq.eventsim.instrumentation.xml.DescriptionToXmlParser;

/**
 * A wizard for {@link InstrumentationDescription}s. Provides the ability to
 * create a new description via
 * 
 * <pre>
 * File -> New -> Other... -> EventSim Instrumentation
 *                         -> EventSim Instrumentation Description.
 * </pre>
 * 
 * @author Henning Schulz
 *
 */
public class NewDescriptionWizard extends Wizard implements INewWizard {

	private PalladioComponentModelCreationWizardPage fileCreationPage;

	private IStructuredSelection selection;

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
		setWindowTitle("Create new EventSim Instrumentation Description");
		setNeedsProgressMonitor(true);
	}

	@Override
	public void addPages() {
		fileCreationPage = new PalladioComponentModelCreationWizardPage("InstrumentationDescriptionFile", selection,
				"eventsim_instrumentation");
		fileCreationPage.setTitle("Select File Name");
		fileCreationPage.setDescription("Select the file name of the instrumentation description.");
		addPage(fileCreationPage);
	}

	@Override
	public boolean performFinish() {
		InstrumentationDescription description = new InstrumentationDescription();
		DescriptionToXmlParser parser = new DescriptionToXmlParser();
		IFile file = fileCreationPage.createNewFile();
		String path = file.getLocation().toFile().getAbsolutePath();
		try {
			parser.saveToFile(description, path);
		} catch (JAXBException e) {
			e.printStackTrace();
		}

		try {
			file.refreshLocal(IResource.DEPTH_ZERO, null);
		} catch (CoreException e) {
			e.printStackTrace();
		}

		IWorkbench wb = PlatformUI.getWorkbench();
		IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
		IWorkbenchPage page = win.getActivePage();
		try {
			page.openEditor(new FileEditorInput(file), InstrumentationDescriptionEditor.ID);
		} catch (PartInitException e) {
			e.printStackTrace();
		}

		return true;
	}

}
