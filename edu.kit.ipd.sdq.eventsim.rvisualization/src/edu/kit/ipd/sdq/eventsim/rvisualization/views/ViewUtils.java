package edu.kit.ipd.sdq.eventsim.rvisualization.views;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import edu.kit.ipd.sdq.eventsim.rvisualization.util.Procedure;

public class ViewUtils {

    private ViewUtils() {
        // prevents instantiation by clients
    }
    
    public static void withBusyCursor(Procedure p) {
        Display display = PlatformUI.getWorkbench().getDisplay();
        Shell shell = display.getActiveShell();
        try {
            if (shell != null) {
                shell.setCursor(display.getSystemCursor(SWT.CURSOR_WAIT));
            }
            p.execute();
        } finally {
            if (shell != null) {
                shell.setCursor(display.getSystemCursor(SWT.CURSOR_ARROW));
            }
        }
    }

}
