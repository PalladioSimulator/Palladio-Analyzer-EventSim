package edu.kit.ipd.sdq.eventsim.rvisualization.ggplot;

public interface Layer extends Plottable {

    Layer data(String variable);
    
    Layer map(Aesthetic aes, String variable);
    
}
