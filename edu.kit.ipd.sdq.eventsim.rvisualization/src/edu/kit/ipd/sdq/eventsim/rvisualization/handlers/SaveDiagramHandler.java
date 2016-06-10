package edu.kit.ipd.sdq.eventsim.rvisualization.handlers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import edu.kit.ipd.sdq.eventsim.rvisualization.views.DiagramView;
import edu.kit.ipd.sdq.eventsim.rvisualization.views.FilterView;

/**
 * Saves the diagram displayed in the {@link FilterView} to a location specified
 * by the user.
 * 
 * @author Benjamin Rupp
 * @author Philipp Merkle
 */
public class SaveDiagramHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		DiagramView view = (DiagramView) HandlerUtil
				.getActiveWorkbenchWindow(event).getActivePage()
				.getActivePart();

		exportDiagram(view.getPathToDiagramImage());

		return null;
	}

	private void exportDiagram(String pathToDiagramImage) {

		Shell shell = Display.getCurrent().getActiveShell();
		FileDialog dialog = new FileDialog(shell, SWT.SAVE);
		dialog.setFilterNames(new String[] { "All Files (*.*)" });
		dialog.setFilterExtensions(new String[] { "*.*" });
		dialog.setOverwrite(true);
		dialog.setText("Export diagram image");

		Path sourcePath = Paths.get(pathToDiagramImage);

		if (sourcePath == null) {
			return;
		}

		Path fileName = sourcePath.getFileName();

		if (fileName == null) {
			return;
		}

		String fileNameStr = fileName.toString();
		dialog.setFileName(fileNameStr);

		String destination = dialog.open();

		if (destination == null) {
			return;
		}

		Path destinationPath = new File(destination).toPath();

		try {

			Files.copy(sourcePath, destinationPath,
					StandardCopyOption.REPLACE_EXISTING);

		} catch (IOException e) {
			throw new RuntimeException(
					"Error while exporting diagram image to " + destination, e);
		}

	}

}
