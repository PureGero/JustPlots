package just.plots;

import java.util.HashMap;

public class PlotWorld {
    private final String world;

    private final HashMap<PlotID, Plot> plots = new HashMap<>();

    public PlotWorld(String world) {
        this.world = world;
    }

    public Plot getPlot(int x, int z) {
        return plots.get(new PlotID(x, z));
    }

    public void addPlot(Plot plot) {
        plots.put(plot.getId(), plot);
    }
}
