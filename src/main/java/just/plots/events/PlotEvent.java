package just.plots.events;

import just.plots.Plot;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

public abstract class PlotEvent extends Event {

    private Plot plot;
    private Player player;

    public PlotEvent(Plot plot, Player player) {
        this.plot = plot;
        this.player = player;
    }

    public Plot getPlot() {
        return plot;
    }

    public Player getPlayer() {
        return player;
    }

}
