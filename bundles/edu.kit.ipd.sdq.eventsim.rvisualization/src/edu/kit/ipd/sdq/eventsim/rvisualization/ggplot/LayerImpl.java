package edu.kit.ipd.sdq.eventsim.rvisualization.ggplot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class LayerImpl implements Layer {

    private String representation;

    private String dataVariable;

    private Map<Aesthetic, String> aestheticMappings;

    private Map<String, String> parameters;

    public LayerImpl(String representation) {
        this.representation = representation;
        aestheticMappings = new HashMap<>();
        parameters = new HashMap<>();
    }

    @Override
    public Layer data(String variable) {
        this.dataVariable = variable;
        return this;
    }

    @Override
    public Layer map(Aesthetic aes, String variable) {
        aestheticMappings.put(aes, variable);
        return this;
    }

    @Override
    public Layer param(String name, String value) {
        parameters.put(name, value);
        return this;
    }

    @Override
    public String toPlot() {
        String dataPlot = plotData();
        String aesPlot = plotAesthetics();
        String parametersPlot = plotParameters();

        List<String> plotList = new ArrayList<>();
        if (!dataPlot.isEmpty()) {
            plotList.add(dataPlot);
        }
        if (!aesPlot.isEmpty()) {
            plotList.add(aesPlot);
        }
        if (!parametersPlot.isEmpty()) {
            plotList.add(parametersPlot);
        }
        
        String[] plots = new String[plotList.size()];
        plotList.toArray(plots);

        return representation + "(" + String.join(", ", plots) + ")";
    }

    private String plotData() {
        if (dataVariable == null || dataVariable.isEmpty()) {
            return "";
        }
        return "data=" + dataVariable;
    }

    private String plotAesthetics() {
        String[] mappings = new String[aestheticMappings.size()];
        if (mappings.length == 0) {
            return "";
        }
        int i = 0;
        for (Entry<Aesthetic, String> entry : aestheticMappings.entrySet()) {
            String aesthetic = entry.getKey().toPlot();
            String variable = entry.getValue();
            mappings[i] = aesthetic + "=" + variable;
            i++;
        }
        return "aes(" + String.join(", ", mappings) + ")";
    }
    
    private String plotParameters() {
        String[] params = new String[parameters.size()];
        if (params.length == 0) {
            return "";
        }
        int i = 0;
        for (Entry<String, String> entry : parameters.entrySet()) {
            String name = entry.getKey();
            String value = entry.getValue();
            params[i] = name + "='" + value + "'";
            i++;
        }
        return String.join(", ", params);
    }

}
