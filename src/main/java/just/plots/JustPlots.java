package just.plots;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import just.plots.commands.JustPlotsCommand;
import just.plots.converters.PlotSquaredConverter;
import just.plots.database.Database;
import just.plots.database.SQLiteDatabase;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.UUID;

public class JustPlots extends JavaPlugin {

    private static Database database;

    private static HashMap<String, PlotWorld> plotWorlds = new HashMap<>();

    @Override
    public void onEnable() {

        database = new SQLiteDatabase(new File(this.getDataFolder(), "plots.db"));
        database.createTables();

        new PlotSquaredConverter(this);

        new JustPlotsCommand(this);

    }

    @Override
    public void onDisable() {
        getLogger().info("Closing database connection");
        database.closeConnection();
    }

    /**
     * Will always return a PlotWorld. Check if it's a valid plot world with
     * {@code PlotWorld.isPlotWorld()}.
     */
    public static PlotWorld getPlotWorld(World world) {
        return getPlotWorld(world.getName());
    }

    /**
     * Will always return a PlotWorld. Check if it's a valid plot world with
     * {@code PlotWorld.isPlotWorld()}.
     */
    public static PlotWorld getPlotWorld(String world) {
        PlotWorld plotWorld = plotWorlds.get(world);

        if (plotWorld == null) {
            plotWorld = new PlotWorld(world);
            plotWorlds.put(world, plotWorld);
        }

        return plotWorld;
    }

    public static Plot getPlot(String world, int x, int z) {
        return getPlotWorld(world).getPlot(x, z);
    }

    public static Plot getPlotAt(Entity entity) {
        return getPlotAt(entity.getLocation());
    }

    public static Plot getPlotAt(Location location) {
        if (location.getWorld() == null) {
            return null;
        }

        PlotWorld world = getPlotWorld(location.getWorld());

        if (!world.isPlotWorld()) {
            return null;
        }

        int x = location.getBlockX() / (world.getPlotSize() + world.getRoadSize()) + 1;
        int z = location.getBlockZ() / (world.getPlotSize() + world.getRoadSize()) + 1;

        int dx = Math.floorMod(location.getBlockX(), (world.getPlotSize() + world.getRoadSize()));
        int dz = Math.floorMod(location.getBlockZ(), (world.getPlotSize() + world.getRoadSize()));

        if (dx <= world.getRoadSize() / 2 || dx > world.getPlotSize() + world.getRoadSize() / 2 ||
                dz <= world.getRoadSize() / 2 || dz > world.getPlotSize() + world.getRoadSize() / 2) {
            // On the road
            return null;
        }

        return world.getPlot(x, z);
    }

    public static Database getDatabase() {
        return database;
    }

    public static Plot createPlot(String world, int x, int z, UUID owner) {
        return createPlot(world, x, z, owner, System.currentTimeMillis());
    }

    public static Plot createPlot(String world, int x, int z, UUID owner, long creation) {
        Plot plot = new Plot(world, x, z, owner, creation);
        plot.createInDatabase();
        return plot;
    }

    public static String getUsername(UUID uuid) {
        String name = Bukkit.getOfflinePlayer(uuid).getName();

        if (name != null) {
            return name;
        }

        Plugin essentials = Bukkit.getPluginManager().getPlugin("Essentials");
        if (essentials != null) {
            User user = ((Essentials) essentials).getUser(uuid);

            if (user != null) {
                name = user.getName();
            }
        }

        return name;
    }

}
