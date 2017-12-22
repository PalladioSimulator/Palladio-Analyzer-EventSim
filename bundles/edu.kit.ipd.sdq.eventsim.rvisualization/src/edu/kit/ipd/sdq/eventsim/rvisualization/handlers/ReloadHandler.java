package edu.kit.ipd.sdq.eventsim.rvisualization.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import edu.kit.ipd.sdq.eventsim.rvisualization.views.FilterView;

/**
 * Refreshes the {@link FilterView} by populating their controls, mainly combo boxes, from latest
 * measurements.
 * 
 * @author Philipp Merkle
 *
 */
public class ReloadHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        FilterView view;
        try {
            view = (FilterView) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
                    .showView(FilterView.ID);
        } catch (PartInitException e) {
            throw new ExecutionException("Could not reload measurements", e);
        }

        view.getController().reload();

        return null;
    }

}
