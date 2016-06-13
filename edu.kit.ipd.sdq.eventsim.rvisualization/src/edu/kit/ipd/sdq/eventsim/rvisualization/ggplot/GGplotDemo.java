package edu.kit.ipd.sdq.eventsim.rvisualization.ggplot;

public class GGplotDemo {

    public static void main(String[] args) {
        Ggplot plot = new Ggplot().data("mm").map(Aesthetic.X, "when").add(Geom.POINT.asLayer().map(Aesthetic.COLOR, "green"));
        System.out.println(plot.toPlot());
    }
    
}
