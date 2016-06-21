package edu.kit.ipd.sdq.eventsim.rvisualization;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.RList;

import edu.kit.ipd.sdq.eventsim.measurement.r.connection.ConnectionRegistry;
import edu.kit.ipd.sdq.eventsim.measurement.r.connection.RserveConnection;
import edu.kit.ipd.sdq.eventsim.measurement.r.jobs.EvaluationException;
import edu.kit.ipd.sdq.eventsim.measurement.r.jobs.EvaluationHelper;
import edu.kit.ipd.sdq.eventsim.rvisualization.filter.ConditionBuilder;
import edu.kit.ipd.sdq.eventsim.rvisualization.ggplot.Aesthetic;
import edu.kit.ipd.sdq.eventsim.rvisualization.ggplot.Geom;
import edu.kit.ipd.sdq.eventsim.rvisualization.ggplot.Ggplot;
import edu.kit.ipd.sdq.eventsim.rvisualization.ggplot.Theme;
import edu.kit.ipd.sdq.eventsim.rvisualization.model.DiagramType;
import edu.kit.ipd.sdq.eventsim.rvisualization.model.Entity;
import edu.kit.ipd.sdq.eventsim.rvisualization.model.FilterModel;
import edu.kit.ipd.sdq.eventsim.rvisualization.model.FilterSelectionModel;
import edu.kit.ipd.sdq.eventsim.rvisualization.model.Metric;
import edu.kit.ipd.sdq.eventsim.rvisualization.model.TranslatableEntity;

/**
 * Controls communication with R via RServe.
 * 
 * @author Benjamin Rupp
 * @author Philipp Merkle
 *
 */
public final class RController {

    private static final Logger LOG = LogManager.getLogger(RController.class);

    /**
     * Variable name which is used in R to store data from RDS file as data table.
     */
    public static final String CONTENT_VARIABLE = "mm";

    public static final String LOOKUP_TABLE_VARIABLE = "lookup";

    private static final String[] REQUIRED_LIBRARIES = new String[] { "data.table", "ggplot2", "XML", "svglite" };

    private FilterModel model;

    private FilterSelectionModel selectionModel;

    public RController(FilterModel model, FilterSelectionModel selectionModel) {
        this.model = model;
        this.selectionModel = selectionModel;
    }

    public void initialize() {
        loadLibraries();
        createEmptyDataTableIfMissing();
        buildLookupTable();
    }

    /**
     * Load all necessary R libraries.
     */
    private void loadLibraries() {
        LOG.trace("Loading libraries");
        for (String lib : REQUIRED_LIBRARIES) {
            String rCmd = "library('" + lib + "');";
            try {
                evalRCommand(rCmd);
            } catch (EvaluationException e) {
                LOG.error("Could not load R library " + lib + ". Use 'install.packages('" + lib
                        + "');' to install the library.");
            }
        }
    }

    private void createEmptyDataTableIfMissing() {
        String[] columns = new String[] { "what", "where.first.id", "where.first.name", "where.second.id",
                "where.second.name", "assemblycontext.id", "assemblycontext.name", "who.type" };
        for (int i = 0; i < columns.length; i++) {
            columns[i] += "=factor()";
        }
        String columnSpec = String.join(", ", columns);
        try {
            String rCmd = "if (!exists('" + CONTENT_VARIABLE + "')) {" + CONTENT_VARIABLE + " <- data.table("
                    + columnSpec + ")}";
            evalRCommand(rCmd);
        } catch (EvaluationException e) {
            throw new RuntimeException(e);
        }
    }

    private void buildLookupTable() {
        try {
            // TODO columns hard coded so far
            String rCmd = "if (nrow(" + CONTENT_VARIABLE + ") > 0) {";
            rCmd += LOOKUP_TABLE_VARIABLE + " <- " + CONTENT_VARIABLE
                    + "[, .(.N), by=.(what, where.first.id, where.first.name, where.second.id, where.second.name, assemblycontext.id, assemblycontext.name, who.type)]";
            rCmd += "} else {" + LOOKUP_TABLE_VARIABLE + " <- " + CONTENT_VARIABLE + "}";
            evalRCommand(rCmd);
        } catch (EvaluationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get all metrics which are included in the RDS file.
     * 
     * @return List of all available metrics.
     */
    public List<TranslatableEntity> getMetrics() {
        LOG.trace("Get available metrics");
        if (!isConnected()) {
            return Collections.emptyList();
        }

        String[] metrics = null;
        String rCmd = "levels(" + CONTENT_VARIABLE + "$what)";
        try {
            REXP exp = evalRCommand(rCmd);
            if (!exp.isNull()) {
                metrics = exp.asStrings();
            } else {
                return Collections.emptyList();
            }
        } catch (REXPMismatchException e) {
            LOG.error(e);
        } catch (EvaluationException e) {
            LOG.error("Could not read metrics from R", e);
        }

        List<TranslatableEntity> metricsList = new ArrayList<>();
        for (String m : metrics) {
            Metric metric = Metric.fromMeasurementsName(m);
            metricsList.add(new TranslatableEntity(metric.getNameInMeasurements(), metric.getName()));
        }

        return metricsList;
    }

    public List<Entity> getMeasuringPointsTo() {
        return getMeasuringPointsInternal(false);
    }

    public List<Entity> getMeasuringPointsFrom() {
        return getMeasuringPointsInternal(true);
    }

    /**
     * Get all measuring points based on the given set of filters.
     */
    private List<Entity> getMeasuringPointsInternal(boolean first) {
        if (!isConnected()) {
            return Collections.emptyList();
        }

        if (selectionModel.getMetric() == null) {
            return Collections.emptyList();
        }

        if (!first && selectionModel.getMeasuringPointFrom() == null) {
            return Collections.emptyList();
        }

        List<Entity> measuringPoints = new ArrayList<>();
        try {
            ConditionBuilder conditions = new ConditionBuilder(model, selectionModel).metric().triggerType().assembly();
            String projection = null;
            if (first) {
                projection = "list(id=where.first.id, name=where.first.name)";
            } else {
                conditions = conditions.from();
                projection = "list(id=where.second.id, name=where.second.name)";
            }
            String selection = conditions.build();
            String rCmd = unique(LOOKUP_TABLE_VARIABLE + "[" + selection + ", " + projection + "]", true);

            RList columnList = evalRCommand(rCmd).asList();
            if (columnList.size() == 0) {
                return Collections.emptyList();
            }

            String[] ids = columnList.at("id").asStrings();
            String[] names = columnList.at("name").asStrings();

            // Iterate over all entries and save information about measuring point.
            for (int i = 0; i < ids.length; i++) {
                measuringPoints.add(new Entity(ids[i], names[i]));
            }
        } catch (REXPMismatchException e) {
            LOG.error("Could not measuring points from R", e);
        } catch (EvaluationException e) {
            LOG.error("Could not measuring points from R", e);
        }
        return measuringPoints;
    }

    /**
     * Get all trigger instances based on the given set of filters.
     * 
     * @return List of all trigger instances as {@link Entity}.
     */
    public List<Entity> getTriggerInstances() {
        if (!isConnected()) {
            return Collections.emptyList();
        }

        ConditionBuilder conditions = new ConditionBuilder(model, selectionModel).metric().lowerTime().upperTime()
                .triggerType();
        String selection = conditions.build();
        String projection = "list(id=who.id, name=who.name)";
        String rCmd = CONTENT_VARIABLE + "[" + selection + ", " + projection + "]";

        ArrayList<Entity> triggerInstances = new ArrayList<Entity>();
        try {
            RList columnList = evalRCommand(rCmd).asList();
            if (columnList.size() == 0) {
                return Collections.emptyList();
            }
            String[] ids = columnList.at("id").asStrings();
            String[] names = columnList.at("name").asStrings();

            // Iterate over all instances and save information about name and id.
            for (int i = 0; i < ids.length; i++) {
                triggerInstances.add(new Entity(ids[i], names[i]));
            }
        } catch (REXPMismatchException e) {
            LOG.error("Could not read trigger instances from R", e);
        } catch (EvaluationException e) {
            LOG.error("Could not read trigger instances from R", e);
        }
        return triggerInstances;
    }

    /**
     * Get the number of trigger instances based on the given set of filters.
     * 
     * @return Number of available trigger instances.
     */
    public int getNumberOfTriggerInstances() {
        if (!isConnected()) {
            return 0;
        }

        int numInstances = 0;
        try {
            ConditionBuilder conditions = new ConditionBuilder(model, selectionModel).metric().lowerTime().upperTime()
                    .triggerType();
            String selection = conditions.build();
            String rCmd = "length(" + unique(CONTENT_VARIABLE + "[" + selection + "]$who.id)", true);
            numInstances = evalRCommand(rCmd).asInteger();
        } catch (REXPMismatchException e) {
            LOG.error("Could not read number of trigger instances from R", e);
        } catch (EvaluationException e) {
            LOG.error("Could not read number of trigger instances from R", e);
        }
        return numInstances;
    }

    /**
     * Get available triggers from the RDS file.
     * 
     * @return List of triggers if available, otherwise null.
     */
    public List<TranslatableEntity> getTriggerTypes() {
        if (!isConnected()) {
            return Collections.emptyList();
        }

        if (selectionModel.getMetric() == null) {
            return Collections.emptyList();
        }

        String selection = new ConditionBuilder(model, selectionModel).metric().build();
        String projection = "list(type=who.type)";
        String rCmd = unique(LOOKUP_TABLE_VARIABLE + "[" + selection + ", " + projection + "]", true);

        List<TranslatableEntity> triggerTypesList = new ArrayList<>();
        try {
            RList columnList = evalRCommand(rCmd).asList();
            if (columnList.size() == 0) {
                return Collections.emptyList();
            }
            String[] types = columnList.at("type").asStrings();

            for (String t : types) {
                // no translation used, currently
                triggerTypesList.add(new TranslatableEntity(t, t));
            }
        } catch (REXPMismatchException e) {
            LOG.error("Could not read trigger types from R", e);
        } catch (EvaluationException e) {
            LOG.error("Could not read trigger types from R", e);
        }
        return triggerTypesList;
    }

    /**
     * Get available assembly contexts from the RDS file.
     * 
     * @return List of assembly context elements if available, otherwise null.
     */
    public List<Entity> getAssemblyContexts() {
        if (!isConnected()) {
            return Collections.emptyList();
        }

        if (selectionModel.getMetric() == null) {
            return Collections.emptyList();
        }

        String selection = new ConditionBuilder(model, selectionModel).metric().triggerType().build();
        String projection = "list(id=assemblycontext.id, name=assemblycontext.name)";
        String rCmd = unique(LOOKUP_TABLE_VARIABLE + "[" + selection + ", " + projection + "]", true);

        List<Entity> assemblyContexts = new ArrayList<>();
        try {
            RList columnList = evalRCommand(rCmd).asList();
            if (columnList.size() == 0) {
                return Collections.emptyList();
            }
            String[] ids = columnList.at("id").asStrings();
            String[] names = columnList.at("name").asStrings();

            // Iterate over all entries and save information about name, id and type.
            for (int i = 0; i < ids.length; i++) {
                assemblyContexts.add(new Entity(ids[i], names[i]));
            }
        } catch (REXPMismatchException e) {
            LOG.error("Could not read assembly contexts from R", e);
        } catch (EvaluationException e) {
            LOG.error("Could not read assembly contexts from R", e);
        }
        return assemblyContexts;
    }

    /**
     * Get the number of values which are used for the diagram plot.
     * 
     * @return Number of values.
     */
    public int getNumberOfDiagramValues() {
        if (!isConnected()) {
            return 0;
        }

        int numberOfDiagramValues = 0;
        try {
            ConditionBuilder conditions = new ConditionBuilder(model, selectionModel).metric().lowerTime().upperTime()
                    .triggerType().triggerInstance().assembly().from().to();
            String selection = conditions.build();
            String projection = "";
            String rCmd = "nrow(" + CONTENT_VARIABLE + "[" + selection + ", " + projection + "])";
            REXP evaluated = evalRCommand(rCmd);
            numberOfDiagramValues = evaluated.asInteger();
        } catch (REXPMismatchException e) {
            LOG.error("Could not read number of diagram values from R", e);
        } catch (EvaluationException e) {
            LOG.error("Could not read number of diagram values from R", e);
        }

        return numberOfDiagramValues;

    }

    public int getMeasurementsCount() {
        if (!isConnected()) {
            return 0;
        }

        int count = 0;
        try {
            String rCmd = "nrow(" + CONTENT_VARIABLE + ")";
            REXP evaluated = evalRCommand(rCmd);
            count = evaluated.asInteger();
        } catch (REXPMismatchException e) {
            LOG.error("Could not read measurement count from R", e);
        } catch (EvaluationException e) {
            LOG.error("Could not read measurement count from R", e);
        }
        return count;
    }

    public int getMemoryConsumptionInMB() {
        if (!isConnected()) {
            return 0;
        }

        int memoryConsumption = 0;
        try {
            String rCmd = "tables(silent=TRUE)[NAME=='" + CONTENT_VARIABLE + "']$MB";
            REXP evaluated = evalRCommand(rCmd);
            memoryConsumption = Integer.parseInt(evaluated.asString());
        } catch (REXPMismatchException e) {
            LOG.error("Could not calculate memory consumption", e);
        } catch (EvaluationException e) {
            LOG.error("Could not calculate memory consumption", e);
        }
        return memoryConsumption;
    }

    /**
     * Get the simulation time maximum of current data loaded from RDS file and stored in
     * {@link #CONTENT_VARIABLE}.
     * 
     * @return Simulation time maximum.
     */
    public int getSimulationTimeMax() {
        if (!isConnected()) {
            return 0;
        }

        int simulationTimeMax = 0;
        try {
            REXP max = evalRCommand("max(" + CONTENT_VARIABLE + "$when)");
            simulationTimeMax = max.asInteger();
        } catch (REXPMismatchException e) {
            LOG.error("Could not read simulation time maximum from R", e);
        } catch (EvaluationException e) {
            LOG.error("Could not read simulation time maximum from R", e);
        }

        return simulationTimeMax;
    }

    /**
     * Get the simulation time minimum of current data loaded from RDS file and stored in
     * {@link #CONTENT_VARIABLE}.
     * 
     * @return Simulation time minimum.
     */
    public int getSimulationTimeMin() {
        if (!isConnected()) {
            return 0;
        }

        int simulationTimeMin = 0;
        try {
            REXP max = evalRCommand("min(" + CONTENT_VARIABLE + "$when)");
            simulationTimeMin = max.asInteger();
        } catch (REXPMismatchException e) {
            LOG.error("Could not read simulation time minimum from R", e);
        } catch (EvaluationException e) {
            LOG.error("Could not read simulation time minimum from R", e);
        }

        return simulationTimeMin;
    }

    private String unique(String cmd, boolean omitNA) {
        if (omitNA) {
            return "na.omit(unique(" + cmd + "))";
        } else {
            return "unique(" + cmd + ")";
        }
    }

    /**
     * Plot a diagram by R based on the given filters and stored in the given path.
     * 
     * @param type
     *            Type of the diagram ({@link DiagramType}).
     * @param diagramImagePath
     *            Path where the image of the diagram should be stored.
     * @param diagramTitle
     *            Diagram title.
     * @param diagramSubTitle
     *            Diagram sub title.
     * @throws Exception
     *             If invalid diagram type was used.
     */
    public String plotDiagramToFile(final DiagramType type, final String diagramImagePath, final String diagramTitle,
            final String diagramSubTitle) {

        ConditionBuilder conditions = new ConditionBuilder(model, selectionModel).metric().lowerTime().upperTime()
                .triggerType().triggerInstance().assembly().from().to();
        String selection = conditions.build();
        String projection = "";

        String rPlotDataVar = CONTENT_VARIABLE + "[" + selection + ", " + projection + "]";
        String rImageVar = "image";

        String plotCommand = getRCommandForDiagramPlot(type, rPlotDataVar, rImageVar, diagramTitle, diagramSubTitle);

        // Save plot to SVG file.
        String rCmd = plotCommand + "ggsave(file='" + diagramImagePath + "', plot=" + rImageVar
                + ", width=10, height=10);";

        // Important for responsive diagram images: Reopen the saved SVG file
        // and set the width and height attributes to 100%.
        rCmd += "svgImage<-xmlParse('" + diagramImagePath + "');";
        rCmd += "rootElement = xmlRoot(svgImage);";
        rCmd += "addAttributes(rootElement, 'width'='100%');";
        rCmd += "addAttributes(rootElement, 'height'='100%');";
        rCmd += "saveXML(rootElement, file='" + diagramImagePath + "');";
        rCmd += "rm(svgImage);rm(rootElement);";

        try {
            evalRCommand(rCmd);
        } catch (EvaluationException e) {
            LOG.error("Could not plot diagram via R", e);
        }

        return plotCommand;
    }

    /**
     * Get the R command string for plotting a diagram.
     * 
     * @param type
     *            Type of the diagram ({@link DiagramType}).
     * @param rPlotDataVar
     *            R variable which contains the filtered data.
     * @param rImageVar
     *            R variable where the image data should be stored.
     * @param diagramTitle
     *            Diagram title.
     * @param diagramSubTitle
     *            Diagram sub title.
     * @return R command string.
     * @throws Exception
     *             If a invalid diagram type was used.
     */
    private String getRCommandForDiagramPlot(final DiagramType type, final String rPlotDataVar, final String rImageVar,
            final String diagramTitle, final String diagramSubTitle) {
        return getDiagramSpecificPlotCommand(type, rPlotDataVar, rImageVar, diagramTitle, diagramSubTitle);
    }

    /**
     * Get the diagram specific R command string for plotting the diagram.
     * 
     * @param type
     *            Type of the diagram ({@link DiagramType}).
     * @param rPlotDataVar
     *            R variable which contains the filtered data.
     * @param rImageVar
     *            R variable where the image data should be stored.
     * @param diagramTitle
     *            Diagram title.
     * @param diagramSubTitle
     *            Diagram sub title.
     * @return R command string.
     * @throws Exception
     *             If an invalid diagram type was used.
     */
    private String getDiagramSpecificPlotCommand(final DiagramType type, final String rPlotDataVar,
            final String rImageVar, final String diagramTitle, final String diagramSubTitle) {
        Ggplot plot = new Ggplot().data(rPlotDataVar);

        switch (type) {
        case HISTOGRAM:
            plot.map(Aesthetic.X, "value");
            plot.add(Geom.HISTOGRAM.asLayer());
            break;
        case POINT_GRAPH:
            plot.map(Aesthetic.X, "when").map(Aesthetic.Y, "value");
            plot.add(Geom.POINT.asLayer());
            break;
        case CDF:
            plot.map(Aesthetic.X, "value");
            plot.add(Geom.ECDF.asLayer());
            break;
        default:
            throw new RuntimeException("Unsupported diagram type: " + type);
        }

        plot.add(new Theme("theme_bw")); // TODO
        String title = createTitle(diagramTitle, diagramSubTitle);
        return rImageVar + "=" + plot.toPlot() + " + " + title + "; ";
    }

    /**
     * Evaluate R command, handle exceptions and log performance information.
     * 
     * @param cmd
     *            the R command to be evaluated
     * @return the evaluation results as an R expression, see {@link REXP}
     * @throws EvaluationException
     */
    private REXP evalRCommand(final String cmd) throws EvaluationException {
        if (!isConnected()) {
            return null;
        }

        Long timeBefore = System.currentTimeMillis();
        LOG.debug("[R cmd] " + cmd);
        System.out.println("[R cmd] " + cmd);
        REXP evaluated = EvaluationHelper.evaluate(lookupConnection().getConnection(), cmd);
        Long timeAfter = System.currentTimeMillis();
        LOG.debug("R result in " + (timeAfter - timeBefore) + "ms.");
        return evaluated;
    }

    private String createTitle(String title, String subtitle) {
        return "ggtitle(expression(atop('" + title + "', " + "atop(italic('" + subtitle + "'), ''))))";
    }

    private RserveConnection lookupConnection() {
        return ConnectionRegistry.instance().getConnection();
    }

    public boolean isConnected() {
        return lookupConnection() != null && lookupConnection().isConnected();
    }

}