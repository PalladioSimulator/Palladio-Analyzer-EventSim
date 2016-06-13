package edu.kit.ipd.sdq.eventsim.rvisualization;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.rosuda.REngine.REXP;
import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.Rserve.RserveException;

import edu.kit.ipd.sdq.eventsim.measurement.r.connection.ConnectionRegistry;
import edu.kit.ipd.sdq.eventsim.measurement.r.connection.RserveConnection;
import edu.kit.ipd.sdq.eventsim.measurement.r.jobs.EvaluationException;
import edu.kit.ipd.sdq.eventsim.measurement.r.jobs.EvaluationHelper;
import edu.kit.ipd.sdq.eventsim.rvisualization.ggplot.Aesthetic;
import edu.kit.ipd.sdq.eventsim.rvisualization.ggplot.Geom;
import edu.kit.ipd.sdq.eventsim.rvisualization.ggplot.Ggplot;
import edu.kit.ipd.sdq.eventsim.rvisualization.ggplot.Theme;
import edu.kit.ipd.sdq.eventsim.rvisualization.model.DiagramType;
import edu.kit.ipd.sdq.eventsim.rvisualization.model.Entity;
import edu.kit.ipd.sdq.eventsim.rvisualization.model.MeasurementFilter;
import edu.kit.ipd.sdq.eventsim.rvisualization.model.Pair;

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
    private static final String CONTENT_VARIABLE = "mm";

    private boolean librariesLoaded = false;

    private static final String[] REQUIRED_LIBRARIES = new String[] { "data.table", "ggplot2", "XML", "svglite" };

    private static final String DIAGRAM_THEME = "theme()";
    // "theme("
    // + "axis.text=element_text(size=20),"
    // + "axis.title=element_text(size=22, face='bold'),"
    // + "plot.title=element_text(size=24))";

    public RserveConnection getConnection() {
        return ConnectionRegistry.instance().getConnection();
    }

    /**
     * Load all necessary R libraries.
     */
    public void loadLibraries() {
        LOG.trace("Loading libraries");
        for (String lib : REQUIRED_LIBRARIES) {
            String rCmd = "library('" + lib + "');";
            try {
                evalRCommand(rCmd);
            } catch (EvaluationException e) {
                LOG.error("Could not load R library " + lib + ". Use 'install.packages('" + lib
                        + "');' to install the library.");
                e.printStackTrace();
            }
        }
    }

    /**
     * Get all metrics which are included in the RDS file.
     * 
     * @return List of all available metrics.
     */
    public List<String> getMetrics() {
        LOG.trace("Get available metrics");
        if (getConnection() == null || !getConnection().isConnected()) {
            return Collections.emptyList();
        }

        ArrayList<String> metricsList = new ArrayList<>();
        String rCmd = "levels(" + CONTENT_VARIABLE + "$what)";
        try {
            REXP exp = evalRCommand(rCmd);
            metricsList.addAll(Arrays.asList(exp.asStrings()));
        } catch (REXPMismatchException e) {
            LOG.error(e);
        } catch (EvaluationException e) {
            LOG.error("Could not read metrics from R", e);
        }

        return metricsList;
    }

    /**
     * Get all measuring points based on the given set of filters.
     * 
     * @param filterSet
     *            Set of filters ({@link MeasurementFilter}).
     * @return List of all measuring points with given metric.
     */
    public List<Pair<Entity>> getMeasuringPoints(final Set<MeasurementFilter> filterSet) {
        if (getConnection() == null || !getConnection().isConnected()) {
            return Collections.emptyList();
        }

        LOG.trace("Get available measuring points vom RDS file.");

        ArrayList<Pair<Entity>> mp = new ArrayList<>();

        try {

            String rDataTableVar = "dat";
            filterDataAndStoreInVariable(rDataTableVar, filterSet);

            // Save available measuring points in a variable called 'entries'.
            evalRCommand("entries <- unique(" + rDataTableVar + "[, list(a=where.first.id, "
                    + "b=where.first.name, c=where.second.id, " + "d=where.second.name)])");

            // Get number of measuring points.
            int numMeasuringPoints = evalRCommand("NROW(entries)").asInteger();

            // Iterate over all entries and save information about first and
            // second measuring point. Important: Start iteration with 1 because
            // R starts counting with 1.
            String entryFirstId = "";
            String entryFirstName = "";
            String entrySecondId = "";
            String entrySecondName = "";

            for (int i = 1; i <= numMeasuringPoints; i++) {

                entryFirstId = evalRCommand("entries[" + i + "]$a").asString();

                entryFirstName = evalRCommand("entries[" + i + "]$b").asString();

                entrySecondId = evalRCommand("entries[" + i + "]$c").asString();

                entrySecondName = evalRCommand("entries[" + i + "]$d").asString();

                LOG.trace("Measuring point " + i + " - from id: " + entryFirstId + " - from name: " + entryFirstName
                        + " - to id: " + entrySecondId + " - to name: " + entrySecondName);

                Entity from = new Entity(entryFirstId, entryFirstName);
                Entity to = new Entity(entrySecondId, entrySecondName);
                mp.add(new Pair<>(from, to));
            }

            // Delete R variables.
            evalRCommand("rm(" + rDataTableVar + ");");
            evalRCommand("rm(entries)");

        } catch (REXPMismatchException e) {
            e.printStackTrace();
        } catch (Exception e) {
            LOG.error("Could not read measuring points from R", e);
        }

        return mp;
    }

    /**
     * Get all trigger instances based on the given set of filters.
     * 
     * @param filterSet
     *            Set of filters ({@link MeasurementFilter}).
     * @return List of all trigger instances as {@link Entity}.
     */
    public List<Entity> getTriggerInstances(final Set<MeasurementFilter> filterSet) {
        if (getConnection() == null || !getConnection().isConnected()) {
            return Collections.emptyList();
        }

        ArrayList<Entity> triggerInstances = new ArrayList<Entity>();

        try {

            String rDataTableVar = "dat";
            filterDataAndStoreInVariable(rDataTableVar, filterSet);

            evalRCommand("instances <- unique(" + rDataTableVar + "[, list(id=who.id, name=who.name)]);");

            // Get number of measuring points.
            int numInstances = evalRCommand("NROW(instances)").asInteger();

            // Iterate over all instances and save information about name and
            // id. Important: Start iteration with 1 because R starts counting
            // with 1.
            String instanceName = "";
            String instanceId = "";

            for (int i = 1; i <= numInstances; i++) {

                instanceName = evalRCommand("instances[" + i + "]$name").asString();

                instanceId = evalRCommand("instances[" + i + "]$id").asString();

                triggerInstances.add(new Entity(instanceName, instanceId));
            }

            // Delete R variables.
            evalRCommand("rm(" + rDataTableVar + ")");
            evalRCommand("rm(instances)");

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
     * @param filterSet
     *            Set of filters ({@link MeasurementFilter}).
     * @return Number of available trigger instances.
     */
    public int getNumberOfTriggerInstances(final Set<MeasurementFilter> filterSet) {
        if (getConnection() == null || !getConnection().isConnected()) {
            return 0;
        }

        int numInstances = 0;

        try {

            String rDataTableVar = "dat";
            filterDataAndStoreInVariable(rDataTableVar, filterSet);

            evalRCommand("instances <- unique(" + rDataTableVar + "[, list(id=who.id, name=who.name)]);");

            // Get number of measuring points.
            numInstances = evalRCommand("NROW(instances)").asInteger();

            // Delete R variables.
            evalRCommand("rm(" + rDataTableVar + ")");
            evalRCommand("rm(instances)");

        } catch (REXPMismatchException e) {
            LOG.error("Could not read number of trigger instances from R", e);
        } catch (EvaluationException e) {
            LOG.error("Could not read number of trigger instances from R", e);
        }

        return numInstances;
    }

    /**
     * Get the simulation time maximum of current data loaded from RDS file and stored in
     * {@link #CONTENT_VARIABLE}.
     * 
     * @return Simulation time maximum.
     */
    public int getSimulationTimeMax() {
        if (getConnection() == null || !getConnection().isConnected()) {
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
        if (getConnection() == null || !getConnection().isConnected()) {
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

    /**
     * Get available triggers from the RDS file.
     * 
     * @return List of triggers if available, otherwise null.
     */
    public String[] getTriggers() {
        if (getConnection() == null || !getConnection().isConnected()) {
            return new String[0];
        }

        String[] triggers = null;
        String rCmd = "levels(" + CONTENT_VARIABLE + "$who.type)";
        try {
            REXP exp = evalRCommand(rCmd);
            triggers = exp.asStrings();
        } catch (REXPMismatchException e) {
            LOG.error("Could not read triggers from R", e);
        } catch (EvaluationException e) {
            LOG.error("Could not read triggers from R", e);
        }

        return triggers;
    }

    /**
     * Get available assembly contexts from the RDS file.
     * 
     * @return List of assembly context elements if available, otherwise null.
     */
    public List<Entity> getAssemblyContexts() {
        if (getConnection() == null || !getConnection().isConnected()) {
            return Collections.emptyList();
        }

        List<Entity> assemblyContexts = null;
        try {
            // Save available assembly contexts in a variable called 'entries'.
            evalRCommand("entries <- unique(" + CONTENT_VARIABLE + "[complete.cases(" + CONTENT_VARIABLE + "), list("
                    + "type=assemblycontext.type, " + "id=assemblycontext.id, " + "name=assemblycontext.name" + ")]);");

            // Get number of measuring points.
            int numAssemblyContexts = evalRCommand("length(entries)").asInteger();

            if (numAssemblyContexts == 0) {
                return Collections.emptyList();
            }
            assemblyContexts = new ArrayList<>();

            // Iterate over all entries and save information about name, id and
            // type. Important: Start iteration with 1 because R starts counting
            // with 1.
            for (int i = 1; i <= numAssemblyContexts; i++) {
                String name = evalRCommand("entries[" + i + "]$name").asString();
                String id = evalRCommand("entries[" + i + "]$id").asString();
                assemblyContexts.add(new Entity(id, name));
            }

            // Delete R variables.
            evalRCommand("rm(entries);");
        } catch (REXPMismatchException e) {
            LOG.error("Could not read assembly contexts from R", e);
        } catch (EvaluationException e) {
            LOG.error("Could not read assembly contexts from R", e);
        }

        return assemblyContexts != null ? assemblyContexts : Collections.emptyList();

    }

    public int getMeasurementsCount() {
        int count = 0;
        String rCmd = "length(" + CONTENT_VARIABLE + "$value)";
        try {
            REXP exp = evalRCommand(rCmd);
            count = exp.asInteger();
        } catch (REXPMismatchException e) {
            LOG.error("Could not read measurement count from R", e);
        } catch (EvaluationException e) {
            LOG.error("Could not read measurement count from R", e);
        }
        return count;
    }

    /**
     * Plot a diagram by R based on the given filters and stored in the given path.
     * 
     * @param type
     *            Type of the diagram ({@link DiagramType}).
     * @param filterSet
     *            Set of filters ({@link MeasurementFilter}).
     * @param diagramImagePath
     *            Path where the image of the diagram should be stored.
     * @param diagramTitle
     *            Diagram title.
     * @param diagramSubTitle
     *            Diagram sub title.
     * @throws Exception
     *             If invalid diagram type was used.
     */
    public void plotDiagram(final DiagramType type, final Set<MeasurementFilter> filterSet,
            final String diagramImagePath, final String diagramTitle, final String diagramSubTitle) {

        String rCmd = "";
        String rPlotDataVar = "dat";
        String rImageVar = "image";

        rCmd += getRCommandForDiagramPlot(type, rPlotDataVar, filterSet, rImageVar, diagramTitle, diagramSubTitle);

        // Save plot to SVG file.
        rCmd += "ggsave(file='" + diagramImagePath + "', plot=" + rImageVar + ", width=10, height=10);";
        rCmd += "rm(" + rPlotDataVar + ");";

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

    }

    /**
     * Get the R command string for plotting a diagram.
     * 
     * @param type
     *            Type of the diagram ({@link DiagramType}).
     * @param rPlotDataVar
     *            R variable which contains the filtered data.
     * @param filterSet
     *            Set of filters ({@link MeasurementFilter}).
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
    public String getRCommandForDiagramPlot(final DiagramType type, final String rPlotDataVar,
            final Set<MeasurementFilter> filterSet, final String rImageVar, final String diagramTitle,
            final String diagramSubTitle) {
        String rCmd = "";

        rCmd += getRCommandForFilteredData(rPlotDataVar, filterSet);
        rCmd += getDiagramSpecificPlotCommand(type, rPlotDataVar, rImageVar, diagramTitle, diagramSubTitle);

        return rCmd;
    }

    /**
     * Get the number of values which are used for the diagram plot.
     * 
     * @param filterSet
     *            Set of filters ({@link MeasurementFilter}).
     * @return Number of values.
     */
    public int getNumberOfDiagramValues(final Set<MeasurementFilter> filterSet) {
        if (getConnection() == null || !getConnection().isConnected()) {
            return 0;
        }

        int numberOfDiagramValues = 0;
        String rCmd = getRCommandForFilteredData("datForNumValues", filterSet);
        rCmd += "NROW(datForNumValues$value);";
        try {
            REXP num = evalRCommand(rCmd);
            numberOfDiagramValues = num.asInteger();
        } catch (REXPMismatchException e) {
            LOG.error("Could not read number of diagram values from R", e);
        } catch (EvaluationException e) {
            LOG.error("Could not read number of diagram values from R", e);
        }

        return numberOfDiagramValues;

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

        plot.add(new Theme("theme")); // TODO
        String title = createTitle(diagramTitle, diagramSubTitle);
        return rImageVar + "=" + plot.toPlot() + " + " + title + "; ";
    }

    /**
     * Evaluate R command, handle exceptions and log performance information.
     * 
     * @param cmd
     *            R command as String.
     * @return R expression {@link REXP}.
     * @throws EvaluationException
     * @throws RserveException
     *             If command could not be executed.
     */
    private REXP evalRCommand(final String cmd) throws EvaluationException {
        if (getConnection() == null || !getConnection().isConnected()) {
            return null;
        }

        // load libraries on first invocation
        if (!librariesLoaded) {
            // important: don't change the order of the following two
            // statements!
            librariesLoaded = true;
            loadLibraries();
        }

        Long timeBefore = System.currentTimeMillis();
        REXP exp = null;
        LOG.debug("[R cmd] " + cmd);
        exp = EvaluationHelper.evaluate(getConnection().getConnection(), cmd);
        Long timeAfter = System.currentTimeMillis();
        LOG.debug("R result in " + (timeAfter - timeBefore) + "ms.");
        return exp;
    }

    /**
     * Get R command string for a filtered data frame.
     * 
     * @param dataVar
     *            Variable name of the data frame in which the filtered data is stored. Use
     *            'rm(dataVar);' in your R command string to delete the data frame after working on
     *            it.
     * @param filterSet
     *            Set of filters which should be applied to the data stored in
     *            {@link #CONTENT_VARIABLE}.
     * @return R command as string.
     */
    private String getRCommandForFilteredData(final String dataVar, final Set<MeasurementFilter> filterSet) {

        String rCmd = dataVar + "<- data.table(" + CONTENT_VARIABLE + "[";

        int pos = filterSet.size();

        for (MeasurementFilter filter : filterSet) {

            if (filter.getCondition() == null) { // TODO correct?

                // Filter by R's 'NA' value.
                rCmd += "is.na(" + CONTENT_VARIABLE + "$" + filter.getProperty() + ")";

            } else {

                // Put filter strings together.
                // Filters have the following structure, e.g.:
                // mm$where.first.id == 'ABCD1234'
                rCmd += CONTENT_VARIABLE + "$" + filter.getProperty() + filter.getOperator() + filter.getCondition();

            }

            if (--pos != 0) {
                // Add a '&' to command string if another filter follows.
                rCmd += " & ";
            }
        }
        rCmd += ",]);";

        return rCmd;
    }

    private String createTitle(String title, String subtitle) {
        return "ggtitle(expression(atop('" + title + "', " + "atop(italic('" + subtitle + "'), ''))))";
    }

    /**
     * Apply a set of filter to the simulation data and store the filtered data as a data table in a
     * variable with the given name.
     * 
     * @param varName
     *            Name of the variable the filtered data should stored in.
     * @param filterSet
     *            A set of filters to apply on the simulation data.
     */
    private void filterDataAndStoreInVariable(final String varName, final Set<MeasurementFilter> filterSet) {
        String rCmdFilteredDataTable = getRCommandForFilteredData(varName, filterSet);
        try {
            evalRCommand(rCmdFilteredDataTable);
        } catch (EvaluationException e) {
            LOG.error(e);
        }
    }

}
