package net.justminecraft.plots.events;

import net.justminecraft.plots.Plot;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import java.util.UUID;

public abstract class PlotEvent extends Event {

    private Plot plot;
    private Player player;
    private UUID playerUniqueId;

    public PlotEvent(Plot plot, Player player) {
        this.plot = plot;
        this.player = player;
        this.playerUniqueId = player.getUniqueId();
    }

    public PlotEvent(Plot plot, UUID playerUniqueId) {
        this.plot = plot;
        this.player = Bukkit.getPlayer(playerUniqueId);
        this.playerUniqueId = playerUniqueId;
    }

    public PlotEvent(Plot plot, Player player, boolean async) {
        super(async);

        this.plot = plot;
        this.player = player;
        this.playerUniqueId = player.getUniqueId();
    }

    public PlotEvent(Plot plot, UUID playerUniqueId, boolean async) {
        super(async);

        this.plot = plot;
        this.player = Bukkit.getPlayer(playerUniqueId);
        this.playerUniqueId = playerUniqueId;
    }

    public Plot getPlot() {
        return plot;
    }

    public Player getPlayer() {
        return player;
    }

    public UUID getPlayerUniqueId() {
        return playerUniqueId;
    }
}
