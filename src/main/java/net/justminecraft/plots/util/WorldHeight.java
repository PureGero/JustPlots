package net.justminecraft.plots.util;

import org.bukkit.World;

public class WorldHeight {
    
    public static int getMinHeight(World world) {
        try {
            // world.getMinHeight() since 28 Mar 2021
            return (int) world.getClass().getMethod("getMinHeight").invoke(world);
        } catch (Exception ignored) {}
        return 0;
    }
    
    public static int getMaxHeight(World world) {
        return world.getMaxHeight();
    }
    
}
