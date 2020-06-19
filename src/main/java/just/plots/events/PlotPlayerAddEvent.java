package just.plots.events;

import just.plots.Plot;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

import java.util.UUID;

/**
 * Called before a player is added to a plot
 */
public class PlotPlayerAddEvent extends PlotEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private boolean cancelled = false;

    public PlotPlayerAddEvent(Plot plot, UUID player) {
        super(plot, player, true);
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
