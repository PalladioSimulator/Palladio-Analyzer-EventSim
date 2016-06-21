package edu.kit.ipd.sdq.eventsim.rvisualization.filter;

import java.util.ArrayList;
import java.util.List;

import edu.kit.ipd.sdq.eventsim.rvisualization.model.Entity;
import edu.kit.ipd.sdq.eventsim.rvisualization.model.FilterModel;
import edu.kit.ipd.sdq.eventsim.rvisualization.model.FilterSelectionModel;
import edu.kit.ipd.sdq.eventsim.rvisualization.model.TranslatableEntity;

public class ConditionBuilder {

    private FilterModel model;

    private FilterSelectionModel selectionModel;

    private List<String> conditions;

    public ConditionBuilder(FilterModel model, FilterSelectionModel selectionModel) {
        this.model = model;
        this.selectionModel = selectionModel;
        this.conditions = new ArrayList<>();
    }

    public ConditionBuilder metric() {
        TranslatableEntity metric = selectionModel.getMetric();
        if (metric != null) {
            conditions.add("what == " + inQuotes(metric.getName()));
        }
        return this;
    }

    public ConditionBuilder lowerTime() {
        int lower = selectionModel.getSimulationTimeLower();
        int min = model.getSimulationTimeMin();
        if (lower > min) {
            conditions.add("when > " + lower);
        }
        return this;
    }

    public ConditionBuilder upperTime() {
        int upper = selectionModel.getSimulationTimeUpper();
        int max = model.getSimulationTimeMax();
        if (upper < max) {
            conditions.add("when < " + upper);
        }
        return this;
    }

    public ConditionBuilder from() {
        Entity measuringPoint = selectionModel.getMeasuringPointFrom();
        if (measuringPoint != null) {
            conditions.add("where.first.id == " + inQuotes(measuringPoint.getId()));
        }
        return this;
    }

    public ConditionBuilder to() {
        Entity measuringPoint = selectionModel.getMeasuringPointTo();
        if (measuringPoint != null) {
            conditions.add("where.second.id == " + inQuotes(measuringPoint.getId()));
        }
        return this;
    }

    public ConditionBuilder assembly() {
        Entity assemblyContext = selectionModel.getAssemblyContext();
        if (assemblyContext != null) {
            conditions.add("assemblycontext.id == " + inQuotes(assemblyContext.getId()));
        }
        return this;
    }

    public ConditionBuilder triggerType() {
        TranslatableEntity type = selectionModel.getTriggerType();
        if (type != null) {
            conditions.add("who.type == " + inQuotes(type.getName()));
        }
        return this;
    }

    public ConditionBuilder triggerInstance() {
        Entity instance = selectionModel.getTriggerInstance();
        if (instance != null) {
            conditions.add("who.id == " + inQuotes(instance.getId()));
        }
        return this;
    }

    private String inQuotes(String string) {
        return "'" + string + "'";
    }

    // public String build() {
    // String filter = "";
    // boolean excludeEmptyConditions = false; // TODO true
    // for (int i = conditions.length - 1; i >= 0; i--) {
    // if (excludeEmptyConditions) {
    // if (conditions[i].matches("unique\\(.*\\)")) {
    // continue;
    // } else {
    // excludeEmptyConditions = false;
    // }
    // }
    // filter = conditions[i] + "," + filter;
    // }
    // // remove trailing ","
    // if (filter.length() > 0) {
    // filter = filter.substring(0, filter.length() - 1);
    // }
    // System.out.println(".(" + filter + ")");
    // return ".(" + filter + ")";
    // }

    public String build() {
        String[] conditionsArray = conditions.toArray(new String[conditions.size()]);
        return String.join(" & ", conditionsArray);
    }

    // public static String createKeys(String dataTableVar) {
    // String cmd = "setkey(" + dataTableVar
    // + ", what, where.first.id, where.second.id, assemblycontext.id, who.type, who.id)";
    // System.out.println(cmd);
    // return cmd;
    // }

}
