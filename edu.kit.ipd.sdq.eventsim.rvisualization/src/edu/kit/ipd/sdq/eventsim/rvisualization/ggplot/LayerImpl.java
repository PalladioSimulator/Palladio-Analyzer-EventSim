package edu.kit.ipd.sdq.eventsim.rvisualization.ggplot;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class LayerImpl implements Layer {

    private String representation;

    private String dataVariable;

    private Map<Aesthetic, String> aestheticMappings;

    public LayerImpl(String representation) {
        this.representation = representation;
        aestheticMappings = new HashMap<>();
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
    public String toPlot() {
        String dataPlot = plotData();
        String aesPlot = plotAesthetics();

        String result = representation + "(";
        if (!dataPlot.isEmpty()) {
            result += dataPlot;
            if (!aesPlot.isEmpty()) {
                result += ", ";
            }
        }
        if (!aesPlot.isEmpty()) {
            result += aesPlot;
        }
        result += ")";
        return result;
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

}
