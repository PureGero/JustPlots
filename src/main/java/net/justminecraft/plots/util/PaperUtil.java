package net.justminecraft.plots.util;

import io.papermc.lib.PaperLib;
import net.justminecraft.plots.JustPlots;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.metadata.FixedMetadataValue;

import java.awt.print.Paper;

public class PaperUtil {

    public static Location getOrigin(Entity entity) {
        if (PaperLib.isPaper()) {
            return entity.getOrigin();
        }

        if (!entity.hasMetadata("justplots.origin")) {
            entity.setMetadata("justplots.origin", new FixedMetadataValue(JustPlots.getPlugin(), entity.getLocation()));
        }

        return (Location) entity.getMetadata("justplots.origin").get(0).value();
    }

    public static void teleportAsync(Entity entity, Location location, PlayerTeleportEvent.TeleportCause command) {
        if (PaperLib.isPaper()) {
            PaperLib.teleportAsync(entity, location, command);
        } else {
            AsyncUtil.ensureMainThread(() -> PaperLib.teleportAsync(entity, location, command));
        }
    }
}
