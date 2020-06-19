package just.plots.listeners;

import com.destroystokyo.paper.event.entity.EntityPathfindEvent;
import com.destroystokyo.paper.event.entity.SlimePathfindEvent;
import just.plots.JustPlots;
import just.plots.Plot;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PaperListener implements Listener {

    public PaperListener(JustPlots plots) {
        plots.getServer().getPluginManager().registerEvents(this, plots);
    }

    /**
     * Only works on Paper. Get the plot that the entity originated from.
     */
    private Plot getPlotOrigin(Entity entity) {
        Location origin = entity.getOrigin();

        if (origin != null) {
            return JustPlots.getPlotAt(origin);
        } else {
            return JustPlots.getPlotAt(entity);
        }
    }

    private double squareDistance(Location loc1, Location loc2) {
        return Math.max(
                Math.abs(loc1.getX() - loc2.getX()),
                Math.abs(loc1.getZ() - loc2.getZ())
        );
    }

    @EventHandler
    public void onEntityPathfind(EntityPathfindEvent event) {
        if (!JustPlots.isPlotWorld(event.getEntity().getWorld())) {
            return;
        }

        Plot from = getPlotOrigin(event.getEntity());
        Plot to = JustPlots.getPlotAt(event.getLoc());

        if (to != from) {

            if (from != null && from != JustPlots.getPlotAt(event.getEntity())) {
                // Don't cancel if they're getting closer to their origin plot
                Location middle = from.getMiddle();
                double entityDistance = squareDistance(event.getEntity().getLocation(), middle);
                double locDistance = squareDistance(event.getLoc(), middle);

                if (locDistance <= entityDistance) {
                    return;
                }
            }

            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityPathfind(SlimePathfindEvent event) {
        if (!JustPlots.isPlotWorld(event.getEntity().getWorld())) {
            return;
        }

        Block target = event.getEntity().getTargetBlock(4);

        if (target == null) {
            return;
        }

        Plot from = getPlotOrigin(event.getEntity());
        Plot to = JustPlots.getPlotAt(target.getLocation());

        if (to != from) {

            if (from != null && from != JustPlots.getPlotAt(event.getEntity())) {
                // Don't cancel if they're getting closer to their origin plot
                Location middle = from.getMiddle();
                double entityDistance = squareDistance(event.getEntity().getLocation(), middle);
                double locDistance = squareDistance(target.getLocation(), middle);

                if (locDistance <= entityDistance) {
                    return;
                }
            }

            // Rotate the slime to go another way
            Location location = event.getEntity().getLocation();
            location.setYaw((float) (Math.random() * 360));
            event.getEntity().teleport(location);

            event.setCancelled(true);
        }
    }

}
