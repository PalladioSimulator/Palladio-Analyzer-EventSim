package edu.kit.ipd.sdq.eventsim.rvisualization.ggplot;

import java.util.ArrayList;
import java.util.List;

public class Ggplot extends LayerImpl {

    private List<Layer> layers;

    public Ggplot() {
        super("ggplot");
        layers = new ArrayList<>();
    }

    @Override
    public Ggplot data(String variable) {
        super.data(variable);
        return this;
    }

    @Override
    public Ggplot map(Aesthetic aes, String variable) {
        super.map(aes, variable);
        return this;
    }

    public Ggplot add(Layer layer) {
        if (layer == null) {
            throw new NullPointerException("Layer may not be null");
        }
        layers.add(layer);
        return this;
    }

    @Override
    public String toPlot() {
        String[] layerPlots = new String[layers.size()];
        if (layerPlots.length == 0) {
            return super.toPlot();
        }
        int i = 0;
        for (Layer l : layers) {
            layerPlots[i] = l.toPlot();
            i++;
        }
        return super.toPlot() + " + " + String.join(" + ", layerPlots);
    }

}
