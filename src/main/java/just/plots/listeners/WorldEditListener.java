package just.plots.listeners;

import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.function.mask.RegionMask;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.World;
import just.plots.JustPlots;
import just.plots.Plot;
import just.plots.events.PlotDeletedEvent;
import just.plots.events.PlotEnterEvent;
import just.plots.events.PlotPlayerAddEvent;
import just.plots.events.PlotPlayerRemoveEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;

import java.util.UUID;

public class WorldEditListener implements Listener {

    private WorldEditPlugin worldEditPlugin = null;

    public WorldEditListener(JustPlots plots) {
        for (Plugin plugin : plots.getServer().getPluginManager().getPlugins()) {
            if (plugin instanceof WorldEditPlugin) {
                // Future proof incase the plugin name changes, or another plugin supports the worldedit api
                worldEditPlugin = (WorldEditPlugin) plugin;
            }
        }

        if (worldEditPlugin != null) {
            plots.getServer().getPluginManager().registerEvents(this, plots);

            for (Player player : Bukkit.getOnlinePlayers()) {
                setup(player);
            }
        }
    }

    private void setup(Player player) {
        if (JustPlots.getPlotWorld(player.getWorld()).isPlotWorld()) {
            Plot plot = JustPlots.getPlotAt(player);

            if (plot != null && plot.isAdded(player)) {
                setMask(player, plot);
            } else {
                setMask(player, null);
            }
        }
    }

    @EventHandler
    public void onJoinEvent(PlayerJoinEvent e) {
        setup(e.getPlayer());
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent e) {
        setup(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlotEnter(PlotEnterEvent e) {
        if (e.getPlot().isAdded(e.getPlayer())) {
            setMask(e.getPlayer(), e.getPlot());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlotPlayerAdded(PlotPlayerAddEvent e) {
        if (e.getPlayer() != null && JustPlots.getPlotAt(e.getPlayer()) == e.getPlot()) {
            setMask(e.getPlayer(), e.getPlot());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlotPlayerRemoved(PlotPlayerRemoveEvent e) {
        if (e.getPlayer() != null) {
            // Resetup the player after they have been remove incase the mask
            // was set to the plot they were removed from
            Bukkit.getScheduler().runTask(JustPlots.getPlugin(), () -> setup(e.getPlayer()));
        }
    }

    @EventHandler
    public void onPlotPlayerRemoved(PlotDeletedEvent e) {
        // Resetup players after the plot has been deleted incase the mask
        // was set to the plot they were removed from
        Player player;

        for (UUID added : e.getPlot().getAdded()) {
            if ((player = Bukkit.getPlayer(added)) != null) {
                setup(player);
            }
        }

        if ((player = Bukkit.getPlayer(e.getPlot().getOwner())) != null) {
            setup(player);
        }
    }

    private void setMask(Player player, Plot plot) {
        Location bottom, top;

        if (plot != null) {
            bottom = plot.getBottom();
            top = plot.getTop();
        } else {
            bottom = top = new Location(player.getWorld(), 0, -1, 0);
        }

        LocalSession localSession = worldEditPlugin.getSession(player);
        World world = localSession.getSelectionWorld();

        BlockVector3 pos1 = BlockVector3.at(bottom.getBlockX(), bottom.getBlockY(), bottom.getBlockZ());
        BlockVector3 pos2 = BlockVector3.at(top.getBlockX(), top.getBlockY(), top.getBlockZ());

        CuboidRegion cuboidRegion = new CuboidRegion(world, pos1, pos2);
        RegionMask regionMask = new RegionMask(cuboidRegion);
        localSession.setMask(regionMask);
    }

    private void removeMask(Player player) {
        LocalSession localSession = worldEditPlugin.getSession(player);
        localSession.setMask(null);
    }
}
