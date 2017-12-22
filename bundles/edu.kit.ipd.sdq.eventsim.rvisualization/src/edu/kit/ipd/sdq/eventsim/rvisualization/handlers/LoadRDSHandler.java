package edu.kit.ipd.sdq.eventsim.rvisualization.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import edu.kit.ipd.sdq.eventsim.rvisualization.views.FilterView;

/**
 * Loads measurements from an RDS file specified by the user.
 * 
 * @author Philipp Merkle
 */
public class LoadRDSHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        FilterView view = (FilterView) HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().getActivePart();

        String selectedFile = openFileChooserDialog("Load measurements from RDS file...");
        if (selectedFile != null) {
            view.getController().loadRDS(selectedFile);
            view.getController().reload();
        }

        return null;
    }

    private String openFileChooserDialog(String title) {
        // prepare dialog
        Shell shell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
        FileDialog dialog = new FileDialog(shell, SWT.OPEN);
        dialog.setFilterNames(new String[] { "R data object serialization (*.rds)" });
        dialog.setFilterExtensions(new String[] { "*.rds" });
        dialog.setText(title);

        // open dialog and get selected file path
        String selectedFile = dialog.open();

        return selectedFile;
    }

}
