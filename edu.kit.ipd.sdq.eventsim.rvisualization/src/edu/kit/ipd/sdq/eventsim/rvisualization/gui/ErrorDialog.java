package edu.kit.ipd.sdq.eventsim.rvisualization.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;

/**
 * Show error dialog for user feedback.
 * 
 * @author Benjamin Rupp
 *
 */
public class ErrorDialog {

    /**
     * Error dialog message.
     */
    private String errorMsg;

    /**
     * Error dialog title.
     */
    private String errorTitle;

    /**
     * Create an error dialog with the given error message.
     * 
     * @param title
     *            Error message title which should be displayed.
     * @param msg
     *            Error message which should be displayed.
     */
    public ErrorDialog(final String title, final String msg) {
        this.errorTitle = title;
        this.errorMsg = msg;

        int style = SWT.ICON_ERROR | SWT.OK;
        MessageBox mBox = new MessageBox(Display.getCurrent().getActiveShell(), style);

        mBox.setText(errorTitle);
        mBox.setMessage(errorMsg);
        mBox.open();
    }

}
