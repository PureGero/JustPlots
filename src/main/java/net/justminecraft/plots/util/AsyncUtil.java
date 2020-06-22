package net.justminecraft.plots.util;

import net.justminecraft.plots.JustPlots;
import org.bukkit.Bukkit;

public class AsyncUtil {

    public static void ensureMainThread(Runnable runnable) {
        if (Bukkit.isPrimaryThread()) {
            runnable.run();
        } else {
            Bukkit.getScheduler().runTask(JustPlots.getPlugin(), runnable);
        }
    }

}
