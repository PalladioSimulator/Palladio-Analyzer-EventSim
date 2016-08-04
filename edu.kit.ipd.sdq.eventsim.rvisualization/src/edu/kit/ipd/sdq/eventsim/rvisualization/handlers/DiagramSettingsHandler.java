package edu.kit.ipd.sdq.eventsim.rvisualization.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import edu.kit.ipd.sdq.eventsim.rvisualization.DiagramController;
import edu.kit.ipd.sdq.eventsim.rvisualization.model.DiagramModel;
import edu.kit.ipd.sdq.eventsim.rvisualization.views.DiagramSettingsDialog;
import edu.kit.ipd.sdq.eventsim.rvisualization.views.DiagramView;
import edu.kit.ipd.sdq.eventsim.rvisualization.views.FilterView;
import edu.kit.ipd.sdq.eventsim.rvisualization.views.ViewUtils;

/**
 * Displays a settings dialog to configure the diagram displayed in the {@link FilterView}.
 * 
 * @author Philipp Merkle
 */
public class DiagramSettingsHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        DiagramView view = (DiagramView) HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().getActivePart();
        DiagramController diagramController = view.getController();
        DiagramModel diagramModel = diagramController.getDiagramModel();

        Shell shell = HandlerUtil.getActiveShell(event);
        DiagramSettingsDialog dialog = new DiagramSettingsDialog(shell, diagramModel);

        int result = dialog.open();
        if (result == Window.OK) {
            // TODO refresh vs. plot
            ViewUtils.withBusyCursor(() -> diagramController.plotDiagram());
        }

        return null;
    }

}
