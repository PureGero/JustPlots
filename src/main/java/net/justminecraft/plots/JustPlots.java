package net.justminecraft.plots;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import io.papermc.lib.PaperLib;
import net.justminecraft.plots.commands.JustPlotsCommand;
import net.justminecraft.plots.converters.PlotSquaredConverter;
import net.justminecraft.plots.database.Database;
import net.justminecraft.plots.database.PlotLoader;
import net.justminecraft.plots.database.SQLiteDatabase;
import net.justminecraft.plots.events.PlotClaimEvent;
import net.justminecraft.plots.listeners.PaperListener;
import net.justminecraft.plots.listeners.PlayerListener;
import net.justminecraft.plots.listeners.PlotListener;
import net.justminecraft.plots.listeners.WorldEditListener;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SingleLineChart;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    private JustPlotsCommand justPlotsCommand;
    private PlotWorldGenerator plotWorldGenerator;

    @Override
    public void onEnable() {
        plugin = this;

        PaperLib.suggestPaper(this);

        database = new SQLiteDatabase(new File(this.getDataFolder(), "plots.db"));
        database.createTables();

        new PlotSquaredConverter(this);

        new PlotLoader(this);

        justPlotsCommand = new JustPlotsCommand(this);

        plotWorldGenerator = new PlotWorldGenerator();

        new PlayerListener(this);
        new PlotListener(this);

        if (hasWorldEdit()) {
            new WorldEditListener(this);
        }

        if (PaperLib.isPaper()) {
            new PaperListener(this);
        }

        Metrics metrics = new Metrics(this, 10953);
        metrics.addCustomChart(new SingleLineChart("plot_counts", () -> plotWorlds.values().stream().mapToInt(plotWorld -> plotWorld.getPlots().size()).sum()));
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

    @Nullable
    @Override
    public ChunkGenerator getDefaultWorldGenerator(@NotNull String worldName, @Nullable String id) {
        return plotWorldGenerator;
    }

    /**
     * Will always return a PlotWorld. Check if it's a valid plot world with
     * {@code PlotWorld.isPlotWorld()}.
     */
    @NotNull
    public static PlotWorld getPlotWorld(@NotNull World world) {
        return getPlotWorld(world.getName());
    }

    /**
     * Will always return a PlotWorld. Check if it's a valid plot world with
     * {@code PlotWorld.isPlotWorld()}.
     */
    @NotNull
    public static PlotWorld getPlotWorld(@NotNull String world) {
        return plotWorlds.computeIfAbsent(world, PlotWorld::new);
    }

    /**
     * Returns true if the world is a valid plot world
     */
    public static boolean isPlotWorld(@NotNull World world) {
        return getPlotWorld(world).isPlotWorld();
    }

    /**
     * Get the plot in the specified world with the specified plot id
     * @param world The world to get the plot in
     * @param x The x component of the plot id
     * @param z The z compoment of the plot id
     * @return The plot in the world with the plot id, or null if no such plot
     * exists
     */
    @Nullable
    public static Plot getPlot(String world, int x, int z) {
        return getPlot(world, new PlotId(x, z));
    }

    /**
     * Get the plot in the specified world with the specified plot id
     * @param world The world to get the plot in
     * @param id The id of the plot
     * @return The plot in the world with the plot id, or null if no such plot
     * exists
     */
    @Nullable
    public static Plot getPlot(String world, PlotId id) {
        return getPlotWorld(world).getPlot(id);
    }
    
    /**
     * Get the plot the specified entity is standing on
     * @param entity The entity to get the plot that they're on
     * @return The plot at that location, or null if there is no plot at
     * location or if it is a road
     */
    @Nullable
    public static Plot getPlotAt(Entity entity) {
        return getPlotAt(entity.getLocation());
    }

    /**
     * Get the plot at the specified location
     * @param location The location of the plot to get
     * @return The plot at that location, or null if there is no plot at
     * location or if it is a road
     */
    @Nullable
    public static Plot getPlotAt(Location location) {
        PlotId id = getPlotIdAt(location);

        if (id == null) {
            return null;
        }

        Plot plot = getPlotWorld(location.getWorld()).getPlot(id.getX(), id.getZ());

        if (plot == null || !plot.inPlot(location)) {
            return null;
        }

        return plot;
    }

    @Nullable
    public static PlotId getPlotIdAt(Entity entity) {
        return getPlotIdAt(entity.getLocation());
    }

    @Nullable
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

        return new PlotId(x, z);
    }

    @Nullable
    public static Set<Plot> getPlotsIfCached(@NotNull UUID uuid) {
        return playerPlotListCache.get(uuid);
    }

    @NotNull
    public static Set<Plot> getPlots(@NotNull Player player) {
        return getPlots(player.getUniqueId());
    }

    @NotNull
    public static Set<Plot> getPlots(@NotNull UUID uuid) {
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

    /**
     * Return a list of a player's plots in a world. If world is null, it will
     * return all of the player's plots.
     */
    @NotNull
    public static List<Plot> getPlotsInWorld(@NotNull Player player, @Nullable World world) {
        return getPlotsInWorld(player, world == null ? null : world.getName());
    }

    /**
     * Return a list of a player's plots in a world. If world is null, it will
     * return all of the player's plots.
     */
    @NotNull
    public static List<Plot> getPlotsInWorld(@NotNull Player player, @Nullable String world) {
        return getPlotsInWorld(player.getUniqueId(), world);
    }

    /**
     * Return a list of a player's plots in a world. If world is null, it will
     * return all of the player's plots.
     */
    @NotNull
    public static List<Plot> getPlotsInWorld(@NotNull UUID uuid, @Nullable String world) {
        List<Plot> plotList = new ArrayList<>();

        for (Plot plot : getPlots(uuid)) {
            if (world == null || plot.getWorldName().equals(world)) {
                plotList.add(plot);
            }
        }

        return plotList;
    }

    public static Collection<PlotWorld> getPlotWorlds() {
        return plotWorlds.values();
    }

    public static Database getDatabase() {
        return database;
    }

    /**
     * Create a plot for the specified owner in the specified world with the
     * specified plot id. Will override any existing plot with the specified
     * plot id in the world.
     * @param world The world to create the plot in
     * @param x The x component of the plot id
     * @param z The z component of the plot id
     * @param owner The owner of this plot
     * @return The newly created plot
     */
    public static Plot createPlot(String world, int x, int z, UUID owner) {
        return createPlot(world, x, z, owner, System.currentTimeMillis());
    }

    /**
     * Create a plot for the specified owner in the specified world with the
     * specified plot id. Will override any existing plot with the specified
     * plot id in the world.
     * @param world The world to create the plot in
     * @param x The x component of the plot id
     * @param z The z component of the plot id
     * @param owner The owner of this plot
     * @param creation The creation time of the plot
     * @return The newly created plot
     */
    public static Plot createPlot(String world, int x, int z, UUID owner, long creation) {
        Plot plot = new Plot(world, x, z, owner, creation);
        plot.createInDatabase();
        return plot;
    }

    /**
     * Claim a plot for the specified owner in the specified world with the
     * specified plot id. This method will generate the plot borders and the
     * plot sign. Will override any existing plot with the specified plot id in
     * the world.
     * @param world The world to create the plot in
     * @param id The location of the plot
     * @param owner The owner of this plot
     * @return The newly created plot
     */ 
    public static Plot claimPlot(String world, PlotId id, UUID owner) {
        return claimPlot(world, id.getX(), id.getZ(), owner);
    }

    /**
     * Claim a plot for the specified owner in the specified world with the
     * specified plot id. This method will generate the plot borders and the
     * plot sign. Will override any existing plot with the specified plot id in
     * the world.
     * @param world The world to create the plot in
     * @param x The x component of the plot id
     * @param z The z component of the plot id
     * @param owner The owner of this plot
     * @return The newly created plot
     */
    public static Plot claimPlot(String world, int x, int z, UUID owner) {
        Plot plot = createPlot(world, x, z, owner);

        plot.claimWalls();
        plot.updateSign();

        PlotClaimEvent event = new PlotClaimEvent(plot, owner);
        Bukkit.getServer().getPluginManager().callEvent(event);

        return plot;
    }

    @NotNull
    public static String getUsername(@NotNull UUID uuid) {
        // Try Minecraft's player cache
        String name = Bukkit.getOfflinePlayer(uuid).getName();

        if (name != null) {
            return name;
        }

        // Try Essentials' player cache if it's installed
        Plugin essentials = Bukkit.getPluginManager().getPlugin("Essentials");
        if (essentials != null) {
            User user = ((Essentials) essentials).getUser(uuid);

            if (user != null) {
                name = user.getName();
            }
        }

        // Return the uuid if no username is found
        return name == null ? uuid.toString() : name;
    }

    /**
     * Return the maximum number of plots the player can claim
     * @param player The player to get the maximum number of plots for
     * @return The maximum numbers of plots for the player, or
     * Integer.MAX_VALUE if there is no limit
     */
    public static int getMaxPlots(Permissible player) {
        int maxPlots = -1;

        for (PermissionAttachmentInfo permission : player.getEffectivePermissions()) {
            if (permission.getPermission().startsWith("justplots.plots.") && permission.getValue()) {

                try {
                    int value = Integer.parseInt(permission.getPermission().substring("justplots.plots.".length()));

                    if (value > maxPlots) {
                        maxPlots = value;
                    }
                } catch (NumberFormatException ignored) {}

            }
        }

        if (maxPlots == -1) {
            // Not set, default to essentially infinite
            maxPlots = Integer.MAX_VALUE;
        }

        return maxPlots;
    }

    /**
     * Get the default generator for plot worlds
     * @return The generator used by plot worlds
     */
    public static PlotWorldGenerator getGenerator() {
        return getPlugin().plotWorldGenerator;
    }

    public static JustPlotsCommand getCommandExecuter() {
        return getPlugin().justPlotsCommand;
    }

}
