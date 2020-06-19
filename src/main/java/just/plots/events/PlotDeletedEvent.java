package just.plots.events;

import just.plots.Plot;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

import java.util.UUID;

/**
 * Called after a plot has been deleted
 */
public class PlotDeletedEvent extends PlotEvent {

    private static final HandlerList handlers = new HandlerList();

    public PlotDeletedEvent(Plot plot, UUID player) {
        super(plot, player);
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
