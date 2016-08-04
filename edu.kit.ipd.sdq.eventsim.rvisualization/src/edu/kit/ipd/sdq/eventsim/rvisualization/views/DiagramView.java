package edu.kit.ipd.sdq.eventsim.rvisualization.views;

import java.io.File;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.part.ViewPart;

import edu.kit.ipd.sdq.eventsim.rvisualization.DiagramController;
import edu.kit.ipd.sdq.eventsim.rvisualization.util.Procedure;

/**
 * View used to display a generated diagram.
 * 
 * @author Benjamin Rupp
 * @author Philipp Merkle
 *
 */
public class DiagramView extends ViewPart {

    public static final String ID = "edu.kit.ipd.sdq.eventsim.rvisualization.diagramview";

    private static final Logger LOG = LogManager.getLogger(DiagramView.class);
    
    private DiagramController ctrl;
    
    private Composite viewParent;

    /** dispose listeners are notified right before this view is disposed */
    private List<Procedure> disposeListener;

    /**
     * SWT Browser for showing diagram image.
     */
    private Browser browser;

    /**
     * Path to the diagram image which will be displayed in the browser.
     */
    private String pathToDiagramImage;

    /**
     * R command string which was used to plot the diagram.
     */
    private String rCommandString;

    private String filterExpression;

    private SashForm sashForm;

    private StatisticsViewer statisticsViewer;

    /**
     * Create a new diagram view to show a diagram.
     * 
     * The diagram image must be set by using the {@link #setDiagramImage(String)} method and the R
     * command string by using the {@link #setRCommandString(String)}.
     */
    public DiagramView() {
        disposeListener = new CopyOnWriteArrayList<>();
    }

    public void addDisposeListener(Procedure p) {
        disposeListener.add(p);
    }

    public void removeDisposeListener(Procedure p) {
        disposeListener.remove(p);
    }

    public String getFilterExpression() {
        return filterExpression;
    }

    public void setFilterExpression(String filterExpression) {
        this.filterExpression = filterExpression;
    }

    public String getPathToDiagramImage() {
        return pathToDiagramImage;
    }

    public String getLastRCommand() {
        return rCommandString;
    }

    @Override
    public final void createPartControl(final Composite parent) {
        viewParent = parent;
        parent.setLayout(new FillLayout());

        sashForm = new SashForm(parent, SWT.NONE);

        // TODO: Check whether the browser is able to show SVG files!?
        // If SVG support is needed, use SWT.Mozilla (XULRunner)?!
        this.browser = new Browser(sashForm, SWT.NONE);

        Composite composite = new Composite(sashForm, SWT.NONE);
        composite.setLayout(new GridLayout(1, false));

        statisticsViewer = new StatisticsViewer(composite, SWT.BORDER);
        statisticsViewer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true, 1, 1));
        showStatisticsArea(false);
    }

    public StatisticsViewer getStatisticsViewer() {
        return statisticsViewer;
    }

    public void showStatisticsArea(boolean show) {
        if (show) {
            sashForm.setWeights(new int[] { 3, 1 });
        } else {
            sashForm.setWeights(new int[] { 1, 0 });
        }
    }

    @Override
    public void setFocus() {
    }

    @Override
    public final void dispose() {
        removeDiagramImage(this.pathToDiagramImage);
        disposeListener.forEach(listener -> listener.execute());
        super.dispose();
    }

    public void setController(DiagramController ctrl) {
        this.ctrl = ctrl;
    }

    /**
     * Set views diagram image.
     * 
     * @param pathToImage
     *            Path to an image.
     */
    public final void setDiagramImage(final String pathToImage) {

        this.browser.setUrl(pathToImage);

        if (this.pathToDiagramImage != null) {
            removeDiagramImage(this.pathToDiagramImage);
        }
        this.pathToDiagramImage = pathToImage;

    }

    /**
     * Set R command string which was used to plot the diagram.
     * 
     * @param rCmd
     *            R command string.
     */
    public final void setRCommandString(final String rCmd) {
        this.rCommandString = rCmd;
    }

    /**
     * Set views title.
     * 
     * @param title
     *            View title.
     */
    public final void setViewTitle(final String title) {
        this.setPartName(title);
    }

    /**
     * Remove a stored diagram image.
     * 
     * @param path
     *            Path to the diagram image to remove.
     */
    private void removeDiagramImage(final String path) {

        if (path == null || path.isEmpty()) {
            return;
        }

        File fileToRemove = new File(path);
        if (fileToRemove.exists() && !fileToRemove.isDirectory()) {
            fileToRemove.delete();
        }

    }
    
    public Display getDisplay() {
        return viewParent != null ? viewParent.getDisplay() : Display.getDefault();
    }
    
}
