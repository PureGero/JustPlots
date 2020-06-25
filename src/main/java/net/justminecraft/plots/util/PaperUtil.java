package net.justminecraft.plots.util;

import io.papermc.lib.PaperLib;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.awt.print.Paper;

public class PaperUtil {

    public static Location getOrigin(Entity entity) {
        return entity.getOrigin();
    }

    public static void teleportAsync(Entity entity, Location location, PlayerTeleportEvent.TeleportCause command) {
        if (PaperLib.isPaper()) {
            PaperLib.teleportAsync(entity, location, command);
        } else {
            AsyncUtil.ensureMainThread(() -> PaperLib.teleportAsync(entity, location, command));
        }
    }
}
