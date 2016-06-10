package edu.kit.ipd.sdq.eventsim.rvisualization.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.handlers.HandlerUtil;

import edu.kit.ipd.sdq.eventsim.rvisualization.views.FilterView;

public class ReloadHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		FilterView view = (FilterView) HandlerUtil
				.getActiveWorkbenchWindow(event).getActivePage()
				.getActivePart();
		
		view.getController().populateControls();

		return null;
	}

}
