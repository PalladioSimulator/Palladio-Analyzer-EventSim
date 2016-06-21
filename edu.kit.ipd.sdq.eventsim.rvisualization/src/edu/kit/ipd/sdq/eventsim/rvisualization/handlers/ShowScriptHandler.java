package edu.kit.ipd.sdq.eventsim.rvisualization.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import edu.kit.ipd.sdq.eventsim.rvisualization.views.DiagramView;
import edu.kit.ipd.sdq.eventsim.rvisualization.views.FilterView;
import edu.kit.ipd.sdq.eventsim.rvisualization.views.RCommandExportDialog;

/**
 * Opens a dialog that displays the R command used to generate the diagram currently shown in the
 * {@link FilterView}.
 * 
 * @author Benjamin Rupp
 * @author Philipp Merkle
 */
public class ShowScriptHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        DiagramView view = (DiagramView) HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().getActivePart();

        exportRCommand(view.getLastRCommand());

        return null;
    }

    private void exportRCommand(String command) {
        Display display = PlatformUI.getWorkbench().getDisplay();
        Shell shell = display.getActiveShell();

        // open dialog
        RCommandExportDialog dialog = new RCommandExportDialog(shell);
        dialog.setCommand(command);
        dialog.open();
    }

}
