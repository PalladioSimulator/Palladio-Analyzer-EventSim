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

/**
 * 
 * @author Philipp Merkle
 * @see http://eclipsesource.com/blogs/2009/01/15/toggling-a-command-contribution/
 *
 */
public class ShowStatisticsHandler extends AbstractHandler implements IElementUpdater {

    public static final String COMMAND_ID = "edu.kit.ipd.sdq.eventsim.rvisualization.diagramview.statistics";

    public static final String TOGGLE_STATE_ID = "edu.kit.ipd.sdq.eventsim.rvisualization.diagramview.statistics.togglestate";

    private ICommandService commandService;

    public ShowStatisticsHandler() {
        commandService = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
    }

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        // toggle state
        State toggleState = getToggleState();
        toggleState.setValue(!(Boolean) toggleState.getValue());

        /*
         * actual handling of this toggle event takes place in the Controller class, which observes
         * the toggle state for state changes
         */

        // refresh toggle buttons
        commandService.refreshElements(event.getCommand().getId(), null);

        return null;
    }

    @Override
    public void updateElement(UIElement element, Map parameters) {
        State toggleState = getToggleState();
        boolean checked = (Boolean) toggleState.getValue();
        element.setChecked(checked);
    }

    private State getToggleState() {
        Command command = commandService.getCommand(COMMAND_ID);
        return command.getState(TOGGLE_STATE_ID);
    }

}
