package edu.kit.ipd.sdq.eventsim.rvisualization;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.RList;

import edu.kit.ipd.sdq.eventsim.measurement.Metadata;
import edu.kit.ipd.sdq.eventsim.measurement.r.connection.ConnectionRegistry;
import edu.kit.ipd.sdq.eventsim.measurement.r.connection.RserveConnection;
import edu.kit.ipd.sdq.eventsim.measurement.r.jobs.EvaluationException;
import edu.kit.ipd.sdq.eventsim.measurement.r.jobs.EvaluationHelper;
import edu.kit.ipd.sdq.eventsim.measurement.r.utils.RHelper;
import edu.kit.ipd.sdq.eventsim.rvisualization.filter.ConditionBuilder;
import edu.kit.ipd.sdq.eventsim.rvisualization.ggplot.Aesthetic;
import edu.kit.ipd.sdq.eventsim.rvisualization.ggplot.Geom;
import edu.kit.ipd.sdq.eventsim.rvisualization.ggplot.Ggplot;
import edu.kit.ipd.sdq.eventsim.rvisualization.ggplot.Theme;
import edu.kit.ipd.sdq.eventsim.rvisualization.model.DiagramModel;
import edu.kit.ipd.sdq.eventsim.rvisualization.model.DiagramType;
import edu.kit.ipd.sdq.eventsim.rvisualization.model.Entity;
import edu.kit.ipd.sdq.eventsim.rvisualization.model.FilterModel;
import edu.kit.ipd.sdq.eventsim.rvisualization.model.FilterSelectionModel;
import edu.kit.ipd.sdq.eventsim.rvisualization.model.TranslatableEntity;
import edu.kit.ipd.sdq.eventsim.rvisualization.model.VariableBinding;
import edu.kit.ipd.sdq.eventsim.rvisualization.util.Helper;

/**
 * Controls communication with R via RServe.
 * 
 * @author Benjamin Rupp
 * @author Philipp Merkle
 *
 */
public final class RController {

    private static final String PLOT_MAIN_COLOR = "#4699dd"; // light blue

    private static final Logger LOG = LogManager.getLogger(RController.class);

    /**
     * Variable name which is used in R to store data from RDS file as data table.
     */
    public static final String CONTENT_VARIABLE = "mm";

    public static final String LOOKUP_TABLE_VARIABLE = "lookup";

    private static final String IMAGE_VARIABLE = "image";

    /**
     * by convention, any metadata column is expected to carry the specified prefix in order to
     * recognize metadata columns automatically
     */
    public static final String METADATA_COLUMN_NAME_PREFIX = "m.";

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
            List<String> groupByColumnsList = new ArrayList<>();
            groupByColumnsList.add("what");
            groupByColumnsList.add("where.first.id");
            groupByColumnsList.add("where.first.name");
            groupByColumnsList.add("where.second.id");
            groupByColumnsList.add("where.second.name");
            if (RHelper.hasColumn(lookupConnection().getConnection(), CONTENT_VARIABLE, "assemblycontext.id")) {
                groupByColumnsList.add("assemblycontext.id");
                groupByColumnsList.add("assemblycontext.name");
            }
            groupByColumnsList.add("who.type");
            String[] groupByColumns = groupByColumnsList.toArray(new String[groupByColumnsList.size()]);

            String rCmd = "if (nrow(" + CONTENT_VARIABLE + ") > 0) {";
            rCmd += LOOKUP_TABLE_VARIABLE + " <- " + CONTENT_VARIABLE + "[, .(.N), by=.("
                    + String.join(",", groupByColumns) + ")]";
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

        ConditionBuilder conditions = new ConditionBuilder(model, selectionModel).metric();
        String selection = conditions.build();
        String projection = "what";
        String rCmd = unique(LOOKUP_TABLE_VARIABLE + "[" + selection + ", " + projection + "]", true);

        String[] metricNames = null;
        // String rCmd = "levels(" + CONTENT_VARIABLE + "$what)";
        try {
            REXP exp = evalRCommand(rCmd);
            if (!exp.isNull()) {
                metricNames = exp.asStrings();
            } else {
                return Collections.emptyList();
            }
        } catch (REXPMismatchException e) {
            LOG.error(e);
        } catch (EvaluationException e) {
            LOG.error("Could not read metrics from R", e);
        }

        // for each metric found in measurements, look up the corresponding label to be
        // displayed in the GUI
        List<TranslatableEntity> metricsList = new ArrayList<>();
        Map<String, TranslatableEntity> metricsMap = Helper.getMetricsLabelExtensions();
        for (String name : metricNames) {
            TranslatableEntity entity = metricsMap.get(name);
            if (entity == null) {
                // if no label is supplied via an extension, use the metric's technical name
                entity = new TranslatableEntity(name, name);
            }
            metricsList.add(entity);
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

    public List<TranslatableEntity> getMetadataNames() {
        if (!isConnected()) {
            return Collections.emptyList();
        }

        String[] typeNames = null;
        try {
            String rCmd = "colnames(" + CONTENT_VARIABLE + ")";
            typeNames = evalRCommand(rCmd).asStrings();
        } catch (REXPMismatchException e) {
            LOG.error("Could not read metadata column names from R", e);
        } catch (EvaluationException e) {
            LOG.error("Could not read metadata column names from R", e);
        }

        List<TranslatableEntity> metadataTypes = new ArrayList<>();
        for (String type : typeNames) {
            if (type.startsWith(METADATA_COLUMN_NAME_PREFIX)) {
                // TODO consider translation (via extension point?)
                metadataTypes.add(new TranslatableEntity(type, type));
            }
        }

        return metadataTypes;
    }

    public List<Metadata> getMetadata(String name) {
        if (!isConnected()) {
            return Collections.emptyList();
        }

        if (selectionModel.getMetric() == null) {
            return Collections.emptyList();
        }

        String selection = new ConditionBuilder(model, selectionModel).metric().build();
        String rCmd = CONTENT_VARIABLE + "[" + selection + ", .(.N), by=.(" + name + ")]";
        try {
            RList columnList = evalRCommand(rCmd).asList();
            String[] values = columnList.at(name).asStrings();
            if (values.length == 1 && values[0] == null) {
                return Collections.emptyList();
            } else {
                List<Metadata> metadata = new ArrayList<>();
                for (String value : values) {
                    metadata.add(new Metadata(name, value));
                }
                return metadata;
            }
        } catch (REXPMismatchException e) {
            LOG.error("Could not read metadata column names from R", e);
        } catch (EvaluationException e) {
            LOG.error("Could not read metadata column names from R", e);
        }
        return null;
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
                    .triggerType().triggerInstance().assembly().from().to().metadata();
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
            memoryConsumption = Integer.parseInt(evaluated.asString().trim().replace(",", "").replace(".", ""));
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

    public double[] getStatistics(String expression) {
        if (!isConnected()) {
            return null;
        }

        try {
            String rCmd = expression + "[, .(count=.N, mean=mean(.SD$value, na.rm=TRUE), "
                    + "quartiles=quantile(.SD$value, probs = seq(0, 1, 0.25), na.rm=TRUE), "
                    + "quantiles=quantile(.SD$value, probs=seq(0.1, 0.9, by=0.1), na.rm=TRUE))]";
            RList statistics = evalRCommand(rCmd).asList();

            double[] count = statistics.at("count").asDoubles(); // 1 row result size
            double[] mean = statistics.at("mean").asDoubles(); // 1 row result size
            double[] quartiles = statistics.at("quartiles").asDoubles(); // 5 rows result size
            double[] quantiles = statistics.at("quantiles").asDoubles(); // 9 rows result size

            double[] stats = new double[1 + 1 + 5 + 9];
            int offset = 0;
            stats[0] = count[0];
            stats[1] = mean[0];

            // quartiles
            offset = 2;
            for (int i = 0; i < 5; i++) {
                stats[i + offset] = quartiles[i];
            }

            // quantiles
            offset = offset + 5;
            for (int i = 0; i < 9; i++) {
                stats[i + offset] = quantiles[i];
            }
            return stats;
        } catch (REXPMismatchException e) {
            LOG.error("Could not calculate statistics via R", e);
        } catch (EvaluationException e) {
            LOG.error("Could not calculate statistics via R", e);
        }
        return null;
    }

    public String getFilterExpression(FilterSelectionModel selectionModel) {
        ConditionBuilder conditions = new ConditionBuilder(model, selectionModel).metric().lowerTime().upperTime()
                .triggerType().triggerInstance().assembly().from().to().metadata();
        String selection = conditions.build();
        String projection = "";
        return CONTENT_VARIABLE + "[" + selection + ", " + projection + "]";
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
    public String plotDiagramToFile(DiagramModel diagramModel, final String diagramImagePath, String filterExpression) {
        String plotCommand = getDiagramSpecificPlotCommand(diagramModel, filterExpression, IMAGE_VARIABLE);

        // Save plot to SVG file.
        String rCmd = plotCommand + "ggsave(file='" + diagramImagePath + "', plot=" + IMAGE_VARIABLE
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
    private String getDiagramSpecificPlotCommand(DiagramModel diagramModel, final String rPlotDataVar,
            final String rImageVar) {
        Ggplot plot = new Ggplot().data(rPlotDataVar);

        switch (diagramModel.getDiagramType()) {
        case HISTOGRAM:
            plot.map(Aesthetic.X, "value");
            plot.add(Geom.HISTOGRAM.asLayer()); // .param("fill", PLOT_MAIN_COLOR).param("color",
                                                // "white")
            break;
        case POINT_GRAPH:
            plot.map(Aesthetic.X, "when").map(Aesthetic.Y, "value");
            plot.add(Geom.POINT.asLayer()); // .param("color", PLOT_MAIN_COLOR)
            break;
        case CDF:
            plot.map(Aesthetic.X, "value");
            plot.add(Geom.ECDF.asLayer()); // .param("color", PLOT_MAIN_COLOR)
            break;
        case BAR:
            plot = new Ggplot().data(addDurationColumn(rPlotDataVar));
            plot.map(Aesthetic.X, "when").map(Aesthetic.Y, "value");
            plot.add(Geom.BAR.asLayer().param("stat", "identity").map(Aesthetic.WIDTH, "duration")); // .param("fill",PLOT_MAIN_COLOR)
            break;
        case LINE:
            plot.map(Aesthetic.X, "when").map(Aesthetic.Y, "value");
            plot.add(Geom.LINE.asLayer()); // .param("color", PLOT_MAIN_COLOR)
            break;
        default:
            throw new RuntimeException("Unsupported diagram type: " + diagramModel.getDiagramType());
        }

        if (diagramModel.getVariableBindings() != null) {
            for (VariableBinding binding : diagramModel.getVariableBindings()) {
                Aesthetic aes = Aesthetic.valueOf(binding.getBindingType());
                plot.map(aes, binding.getVariable());
            }
        }

        plot.add(new Theme("theme_bw")); // TODO
        String title = createTitle(diagramModel.getTitle(), diagramModel.getSubTitle(), diagramModel.getSubSubTitle());
        return rImageVar + "=" + plot.toPlot() + " + " + title + "; ";
    }

    private String addDurationColumn(String expression) {
        return expression + "[, duration := shift(.SD$when, 1, type='lead') - when]";
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

    private String createTitle(String title, String subtitle, String subsubtitle) {
        return "ggtitle(expression(atop('" + title + "', " + "atop('" + subtitle + "', atop('" + subsubtitle + "')))))";
    }

    private RserveConnection lookupConnection() {
        return ConnectionRegistry.instance().getConnection();
    }

    public boolean isConnected() {
        return lookupConnection() != null && lookupConnection().isConnected();
    }

}
