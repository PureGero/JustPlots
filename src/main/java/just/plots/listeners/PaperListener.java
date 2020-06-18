package just.plots.listeners;

import com.destroystokyo.paper.event.entity.EntityPathfindEvent;
import com.destroystokyo.paper.event.entity.SlimePathfindEvent;
import just.plots.JustPlots;
import just.plots.Plot;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PaperListener implements Listener {

    public PaperListener(JustPlots plots) {
        plots.getServer().getPluginManager().registerEvents(this, plots);
    }

    @EventHandler
    public void onEntityPathfind(EntityPathfindEvent event) {
        Plot from = JustPlots.getPlotAt(event.getEntity());
        Plot to = JustPlots.getPlotAt(event.getLoc());

        if (to != from) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityPathfind(SlimePathfindEvent event) {
        Block target = event.getEntity().getTargetBlock(4);

        if (target == null) {
            return;
        }

        Plot from = JustPlots.getPlotAt(event.getEntity());
        Plot to = JustPlots.getPlotAt(target.getLocation());

        if (to != from) {
            // Rotate the slime to go another way
            Location location = event.getEntity().getLocation();
            location.setYaw((float) (Math.random() * 360));
            event.getEntity().teleport(location);

            event.setCancelled(true);
        }
    }

}
