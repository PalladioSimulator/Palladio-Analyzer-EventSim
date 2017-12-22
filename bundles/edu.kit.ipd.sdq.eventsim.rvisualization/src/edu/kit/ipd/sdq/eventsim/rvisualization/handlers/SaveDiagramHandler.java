package edu.kit.ipd.sdq.eventsim.rvisualization.handlers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import edu.kit.ipd.sdq.eventsim.rvisualization.views.DiagramView;
import edu.kit.ipd.sdq.eventsim.rvisualization.views.FilterView;

/**
 * Saves the diagram displayed in the {@link FilterView} to a location specified by the user.
 * 
 * @author Benjamin Rupp
 * @author Philipp Merkle
 */
public class SaveDiagramHandler extends AbstractHandler {

    private static final String FORMAT_PARAMETER_ID = "edu.kit.ipd.sdq.eventsim.rvisualization.diagramview.save.format";

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        DiagramView view = (DiagramView) HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().getActivePart();

        String format = event.getParameter(FORMAT_PARAMETER_ID);
        if (format == null) {
            format = "SVG";
        }

        switch (format) {
        case "SVG":
            String existingDiagramLocation = view.getPathToDiagramImage();
            openFileChooserDialog("Save diagram as SVG...", new SVGHandler(existingDiagramLocation));
            break;
        }

        return null;
    }

    private void openFileChooserDialog(String title, SaveAsHandler handler) {
        // prepare dialog
        Shell shell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
        FileDialog dialog = new FileDialog(shell, SWT.SAVE);
        dialog.setFilterNames(new String[] { "All Files (*.*)" });
        dialog.setFilterExtensions(new String[] { "*.*" });
        dialog.setOverwrite(true);
        dialog.setText(title);

        // open dialog and get selected file path
        String destination = dialog.open();
        if (destination == null) {
            return;
        }
        Path destinationPath = Paths.get(destination);

        // invoke handler
        handler.saveAs(destinationPath);
    }

    private static interface SaveAsHandler {

        public void saveAs(Path destination);

    }

    private static class SVGHandler implements SaveAsHandler {

        private String existingDiagramLocation;

        public SVGHandler(String existingDiagramLocation) {
            this.existingDiagramLocation = existingDiagramLocation;
        }

        @Override
        public void saveAs(Path destination) {
            Path source = Paths.get(existingDiagramLocation);
            if (source == null) {
                // TODO show warning
                return;
            }
            try {
                Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new RuntimeException("Error while exporting diagram image to " + destination, e);
            }
        }

    }

}
