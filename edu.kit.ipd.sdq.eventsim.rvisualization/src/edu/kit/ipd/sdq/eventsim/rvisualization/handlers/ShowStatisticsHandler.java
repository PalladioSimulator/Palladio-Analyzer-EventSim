package edu.kit.ipd.sdq.eventsim.rvisualization.handlers;

import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.State;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.menus.UIElement;

public class ShowStatisticsHandler extends AbstractHandler implements IElementUpdater {

    private ICommandService commandService;

    // TODO caching allowed?
    private State toggleState;

    public ShowStatisticsHandler() {
        commandService = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
        Command command = commandService.getCommand("edu.kit.ipd.sdq.eventsim.rvisualization.diagramview.statistics");
        toggleState = command.getState("edu.kit.ipd.sdq.eventsim.rvisualization.diagramview.statistics.togglestate");
    }

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        // toggle state
        toggleState.setValue(!(Boolean) toggleState.getValue());
        commandService.refreshElements(event.getCommand().getId(), null);

        return null;
    }

    @Override
    public void updateElement(UIElement element, Map parameters) {
        boolean checked = (Boolean) toggleState.getValue();
        element.setChecked(checked);
    }

}
