package edu.kit.ipd.sdq.eventsim.rvisualization;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.IStateListener;
import org.eclipse.core.commands.State;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;

import edu.kit.ipd.sdq.eventsim.rvisualization.handlers.ShowStatisticsHandler;
import edu.kit.ipd.sdq.eventsim.rvisualization.model.DiagramModel;
import edu.kit.ipd.sdq.eventsim.rvisualization.model.DiagramType;
import edu.kit.ipd.sdq.eventsim.rvisualization.model.Entity;
import edu.kit.ipd.sdq.eventsim.rvisualization.model.FilterModel;
import edu.kit.ipd.sdq.eventsim.rvisualization.model.FilterSelectionModel;
import edu.kit.ipd.sdq.eventsim.rvisualization.model.StatisticsModel;
import edu.kit.ipd.sdq.eventsim.rvisualization.util.Procedure;
import edu.kit.ipd.sdq.eventsim.rvisualization.views.DiagramView;
import edu.kit.ipd.sdq.eventsim.rvisualization.views.ViewUtils;

/**
 * 
 * @author Benjamin Rupp
 * @author Philipp Merkle
 *
 */
public class DiagramController {

    private static final Logger LOG = LogManager.getLogger(DiagramController.class);

    private FilterModel filterModel;

    private FilterSelectionModel selectionModel;

    private DiagramModel diagramModel;

    private DiagramView view;

    private RController rCtrl;

    /**
     * If a non-aggregated diagram (e.g. a line graph) has more than DIAGRAM_SIZE_LIMIT values, a
     * warning will be shown to the user because the diagram output may cause an SVG
     * "Huge input lookup" error.
     */
    private static final int DIAGRAM_SIZE_LIMIT = 10_000;

    private static final String DIAGRAM_FILE_PREFIX = "diagram_";

    private static final String DIAGRAM_FILE_EXTENSION = ".svg";

    public DiagramController(FilterModel filterModel, FilterSelectionModel selectionModel, RController rCtrl) {
        this.filterModel = filterModel;
        this.selectionModel = selectionModel;
        this.rCtrl = rCtrl;
        this.diagramModel = createDiagramModel();
    }

    private DiagramModel createDiagramModel() {
        DiagramModel diagramModel = new DiagramModel();
        diagramModel.setDiagramType(DiagramType.valueOf(selectionModel.getDiagramType().getName()));
        diagramModel.setTitle(createDiagramTitle()); // TODO?
        diagramModel.setSubTitel(createDiagramSubTitle());
        diagramModel.setSubSubTitle(createDiagramSubSubTitle());
        return diagramModel;
    }

    public void plotDiagram() {
        // ensure that lower simulation time <= upper simulation time
        throwExceptionOnIncorrectSimulationTimeBounds();

        // Check whether the SVG output gets too huge and show warning.
        int diagramSize = rCtrl.getNumberOfDiagramValues();
        LOG.trace("DIAGRAM SIZE: " + diagramSize);

        if (!diagramModel.getDiagramType().isAggregating() && diagramSize > DIAGRAM_SIZE_LIMIT) {

            MessageDialog dialog = new MessageDialog(Display.getCurrent().getActiveShell(), "Huge diagram output", null,
                    "The diagram plot will contain more than " + DIAGRAM_SIZE_LIMIT + " values (" + diagramSize
                            + "). This may cause an SVG 'Huge output " + "error'. Do you want to continue or "
                            + "restrict the time span to reduce the number " + "of values used for the diagram plot?",
                    MessageDialog.WARNING, new String[] { "No, cancel diagram plot", "Yes, plot diagram" }, 0);

            int result = dialog.open();

            // Cancel plot process if user selected 'No'.
            if (result == 0) {
                return;
            }

        }

        // Save the diagram image in temp directory.
        String diagramPath = createTemporaryDiagramFilePath();
        LOG.trace("DIAGRAM PATH: " + diagramPath);

        // plot diagram to temporary file
        String filter = rCtrl.getFilterExpression(selectionModel);
        String plotCommand = rCtrl.plotDiagramToFile(diagramModel, diagramPath, filter);

        // Open new view to display the diagram.
        String viewTitle = createDiagramTitle();
        try {
            openDiagramView(viewTitle, diagramPath, plotCommand, filter);
        } catch (PartInitException e) {
            LOG.error("Could not open diagram view", e);
        }
    }

    private void throwExceptionOnIncorrectSimulationTimeBounds() {
        int lower = selectionModel.getSimulationTimeLower();
        int upper = selectionModel.getSimulationTimeUpper();
        if (lower > upper) {
            throw new RuntimeException(
                    "Cannot plot diagram with simulation time lower bound being greater than upper bound");
        }
    }

    private String createTemporaryDiagramFilePath() {
        File tempFile = null;
        try {
            tempFile = File.createTempFile(DIAGRAM_FILE_PREFIX, DIAGRAM_FILE_EXTENSION);
        } catch (IOException e) {
            throw new RuntimeException("Could not create temporary file for diagram image", e);
        }
        String imagePath = tempFile.getAbsolutePath();

        // Replace '\' with '/' because R cannot handle backslashes.
        imagePath = imagePath.replaceAll(Matcher.quoteReplacement("\\"), "/");

        return imagePath;
    }

    private void openDiagramView(String title, String imagePath, String rCommand, String filter)
            throws PartInitException {
        DiagramView view = (DiagramView) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
                .showView(DiagramView.ID, title, IWorkbenchPage.VIEW_ACTIVATE);
        view.setViewTitle(title);
        view.setDiagramImage(imagePath);
        view.setRCommandString(rCommand);
        view.setController(this);
        view.setFilterExpression(filter);

        StatisticsSelectionHandler statisticsHandler = new StatisticsSelectionHandler(view);

        // register as toggle listener
        ICommandService commandService = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
        Command command = commandService.getCommand(ShowStatisticsHandler.COMMAND_ID);
        State toggleState = command.getState(ShowStatisticsHandler.TOGGLE_STATE_ID);
        toggleState.addListener(statisticsHandler);

        // once view gets disposed, deregister view from toggle state
        view.addDisposeListener(new Procedure() {
            @Override
            public void execute() {
                toggleState.removeListener(statisticsHandler);
                view.removeDisposeListener(this);
            }
        });

        // if statistics are already enabled
        if ((Boolean) toggleState.getValue()) {
            statisticsHandler.reloadStatistics();
            view.showStatisticsArea(true);
        }
    }

    /**
     * Create the diagram title based on the filter settings.
     * 
     * @return Diagram title.
     */
    private String createDiagramTitle() {

        String diagramTitle = "";

        diagramTitle += createShortDiagramTitle();

        DiagramType diagramType = DiagramType.valueOf(selectionModel.getDiagramType().getName());
        String diagramName = diagramType.getShortName();
        try {
            diagramTitle += " [" + diagramName + "]";
        } catch (Exception e) {
            LOG.error("Cannot get selected diagram type " + "to create diagram title.");
        }

        return diagramTitle;
    }

    /**
     * Create the short version of the diagram title.
     * 
     * @return Short diagram title.
     */
    private String createShortDiagramTitle() {
        String diagramTitle = selectionModel.getMetric().getTranslation() + " of "
                + selectionModel.getMeasuringPointFrom().getName();
        // TODO also consider "to" measuring point
        return diagramTitle;
    }

    /**
     * Create the diagram subtitle based on the filter settings.
     * 
     * @return Diagram subtitle.
     */
    private String createDiagramSubTitle() {
        Entity measuringPoint = selectionModel.getMeasuringPointFrom();
        return "ID: " + measuringPoint.getId();
    }

    private String createDiagramSubSubTitle() {
        String diagramSubSubTitle = "Simulation time span: ";
        diagramSubSubTitle += selectionModel.getSimulationTimeLower();
        diagramSubSubTitle += " - ";
        diagramSubSubTitle += selectionModel.getSimulationTimeUpper();
        return diagramSubSubTitle;
    }

    private void withBusyCursor(Procedure p) {
        ViewUtils.withBusyCursor(p);
    }

    private final class StatisticsSelectionHandler implements IStateListener {

        private DiagramView diagramView;

        public StatisticsSelectionHandler(DiagramView diagramView) {
            this.diagramView = diagramView;
        }

        @Override
        public void handleStateChange(State state, Object oldValue) {
            boolean enabled = (boolean) state.getValue();
            boolean previouslyEnabled = (boolean) oldValue;
            if (enabled && !previouslyEnabled) {
                withBusyCursor(() -> {
                    reloadStatistics();
                    diagramView.showStatisticsArea(true);
                });
            } else if (!enabled) {
                diagramView.showStatisticsArea(false);
            }

        }

        public void reloadStatistics() {
            double[] statistics = rCtrl.getStatistics(diagramView.getFilterExpression());
            StatisticsModel statisticsModel = diagramView.getStatisticsViewer().getModel();

            if (statistics != null && statistics.length == 16) {
                int observations = (int) statistics[0];
                double mean = statistics[1];

                double min = statistics[2];
                double firstQuartile = statistics[3];
                double median = statistics[4];
                double thirdQuartile = statistics[5];
                double max = statistics[6];

                double quantile1 = statistics[7];
                double quantile2 = statistics[8];
                double quantile3 = statistics[9];
                double quantile4 = statistics[10];
                double quantile5 = statistics[11];
                double quantile6 = statistics[12];
                double quantile7 = statistics[13];
                double quantile8 = statistics[14];
                double quantile9 = statistics[15];

                statisticsModel.setObservations(observations);
                statisticsModel.setMin(min);
                statisticsModel.setFirstQuartile(firstQuartile);
                statisticsModel.setMedian(median);
                statisticsModel.setMean(mean);
                statisticsModel.setThirdQuartile(thirdQuartile);
                statisticsModel.setMax(max);

                statisticsModel.setQuantile1(quantile1);
                statisticsModel.setQuantile2(quantile2);
                statisticsModel.setQuantile3(quantile3);
                statisticsModel.setQuantile4(quantile4);
                statisticsModel.setQuantile5(quantile5);
                statisticsModel.setQuantile6(quantile6);
                statisticsModel.setQuantile7(quantile7);
                statisticsModel.setQuantile8(quantile8);
                statisticsModel.setQuantile9(quantile9);
            }
        }

    }

}
