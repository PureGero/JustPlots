package net.justminecraft.plots.events;

import net.justminecraft.plots.Plot;
import org.bukkit.event.HandlerList;

import java.util.UUID;

/**
 * Called after a plot has been claimed
 */
public class PlotClaimEvent extends PlotEvent {

    private static final HandlerList handlers = new HandlerList();

    public PlotClaimEvent(Plot plot, UUID player) {
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
