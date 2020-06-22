package net.justminecraft.plots.events;

import net.justminecraft.plots.Plot;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

/**
 * Called before a player enters a plot
 */
public class PlotClearEvent extends PlotEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    private boolean cancelled = false;

    private boolean willBeDeleted = false;

    public PlotClearEvent(Plot plot, Player player) {
        super(plot, player, true);
    }

    public PlotClearEvent(Plot plot, Player player, boolean willBeDeleted) {
        super(plot, player, true);

        this.willBeDeleted = willBeDeleted;
    }

    /**
     * Returns true if the plot will be deleted after it is cleared. False if
     * the plot will remain with the same owner.
     */
    public boolean willBeDeleted() {
        return willBeDeleted;
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
