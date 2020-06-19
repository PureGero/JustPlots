package just.plots;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import io.papermc.lib.PaperLib;
import just.plots.commands.JustPlotsCommand;
import just.plots.converters.PlotSquaredConverter;
import just.plots.database.Database;
import just.plots.database.PlotLoader;
import just.plots.database.SQLiteDatabase;
import just.plots.listeners.PaperListener;
import just.plots.listeners.PlayerListener;
import just.plots.listeners.PlotListener;
import just.plots.listeners.WorldEditListener;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;

public class JustPlots extends JavaPlugin {

    private static JustPlots plugin;

    private static Database database;

    private static HashMap<String, PlotWorld> plotWorlds = new HashMap<>();

    private static HashMap<UUID, TreeSet<Plot>> playerPlotListCache = new HashMap<>();

    public static JustPlots getPlugin() {
        return plugin;
    }

    @Override
    public void onEnable() {
        plugin = this;

        database = new SQLiteDatabase(new File(this.getDataFolder(), "plots.db"));
        database.createTables();

        new PlotSquaredConverter(this);

        new PlotLoader(this);

        new JustPlotsCommand(this);

        new PlotWorldGenerator();

        new PlayerListener(this);
        new PlotListener(this);

        if (hasWorldEdit()) {
            new WorldEditListener(this);
        }

        if (PaperLib.isPaper()) {
            new PaperListener(this);
        }
    }

    private boolean hasWorldEdit() {
        try {
            return Class.forName("com.sk89q.worldedit.bukkit.WorldEditPlugin") != null;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
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
        return getPlot(world, new PlotId(x, z));
    }

    public static Plot getPlot(String world, PlotId id) {
        return getPlotWorld(world).getPlot(id);
    }

    public static Plot getPlotAt(Entity entity) {
        return getPlotAt(entity.getLocation());
    }

    public static Plot getPlotAt(Location location) {
        PlotId id = getPlotIdAt(location);

        if (id == null) {
            return null;
        }

        return getPlotWorld(location.getWorld()).getPlot(id.getX(), id.getZ());
    }

    public static PlotId getPlotIdAt(Entity entity) {
        return getPlotIdAt(entity.getLocation());
    }

    public static PlotId getPlotIdAt(Location location) {
        if (location.getWorld() == null) {
            return null;
        }

        PlotWorld world = getPlotWorld(location.getWorld());

        if (!world.isPlotWorld()) {
            return null;
        }

        int x = (int) Math.floor((double) location.getBlockX() / (world.getPlotSize() + world.getRoadSize()));
        int z = (int) Math.floor((double) location.getBlockZ() / (world.getPlotSize() + world.getRoadSize()));

        int dx = Math.floorMod(location.getBlockX(), (world.getPlotSize() + world.getRoadSize()));
        int dz = Math.floorMod(location.getBlockZ(), (world.getPlotSize() + world.getRoadSize()));

        if (dx <= world.getRoadSize() / 2 || dx > world.getPlotSize() + world.getRoadSize() / 2 ||
                dz <= world.getRoadSize() / 2 || dz > world.getPlotSize() + world.getRoadSize() / 2) {
            // On the road
            return null;
        }

        return new PlotId(x, z);
    }

    public static Set<Plot> getPlotsIfCached(UUID uuid) {
        return playerPlotListCache.get(uuid);
    }

    public static Set<Plot> getPlots(Player player) {
        return getPlots(player.getUniqueId());
    }

    public static Set<Plot> getPlots(UUID uuid) {
        if (playerPlotListCache.containsKey(uuid)) {
            return playerPlotListCache.get(uuid);
        }

        TreeSet<Plot> plots = new TreeSet<>();

        for (PlotWorld world : plotWorlds.values()) {
            for (Plot plot : world.getPlots()) {
                if (plot.getOwner().equals(uuid)) {
                    plots.add(plot);
                }
            }
        }

        playerPlotListCache.put(uuid, plots);

        return plots;
    }

    public static Collection<PlotWorld> getPlotWorlds() {
        return plotWorlds.values();
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

    public static Plot claimPlot(String world, PlotId id, UUID owner) {
        return claimPlot(world, id.getX(), id.getZ(), owner);
    }

    public static Plot claimPlot(String world, int x, int z, UUID owner) {
        Plot plot = createPlot(world, x, z, owner);

        plot.claimWalls();
        plot.updateSign();

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
