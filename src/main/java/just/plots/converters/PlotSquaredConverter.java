package just.plots.converters;

import just.plots.PlotWorld;
import just.plots.database.Database;
import just.plots.JustPlots;
import just.plots.database.SQLiteDatabase;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.UUID;

public class PlotSquaredConverter {

    private final JustPlots plots;

    public PlotSquaredConverter(JustPlots plots) {
        this.plots = plots;

        File plotSquaredDir = new File(plots.getDataFolder().getParentFile(), "PlotSquared");

        if (plotSquaredDir.isDirectory()) {
            long timer = System.currentTimeMillis();
            plots.getLogger().info("Converting plots from PlotSquared...");

            convert(plotSquaredDir);

            if (!plotSquaredDir.renameTo(new File(plotSquaredDir.getParentFile(), "PlotSquared-Converted-To-JustPlots"))) {
                plots.getLogger().warning("Could not rename " + plotSquaredDir.getPath());
            }

            new PlotMeDateConverter(plots);

            plots.getLogger().info("Converted plots from PlotSquared (took " + (System.currentTimeMillis() - timer) + "ms)");
        }
    }

    private void convert(File plotSquaredDir) {
        loadWorlds(plotSquaredDir);

        try {
            Database database = new SQLiteDatabase(new File(plotSquaredDir, "storage.db"));

            loadPlots(database);

            loadHelpers(database);

            loadTrusted(database);

            database.closeConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadWorlds(File plotSquaredDir) {
        File worldsYml = new File(plotSquaredDir, "config/worlds.yml");

        YamlConfiguration worlds = YamlConfiguration.loadConfiguration(worldsYml);

        ConfigurationSection worldsSection = worlds.getConfigurationSection("worlds");

        if (worldsSection != null) {
            for (String name : worldsSection.getKeys(false)) {
                plots.getLogger().info("Converting plot world " + name + "'s settings...");

                try {
                    ConfigurationSection config = worldsSection.getConfigurationSection(name);
                    PlotWorld plotWorld = JustPlots.getPlotWorld(name);

                    plotWorld.setPlotSize(config.getInt("plot.size"));
                    plotWorld.setRoadSize(config.getInt("road.width"));
                    plotWorld.setFloorHeight(config.getInt("plot.height"));
                    plotWorld.setRoadInnerBlock(Bukkit.createBlockData(config.getString("road.block").replace(":100.0", "")));
                    plotWorld.setRoadOuterBlock(Bukkit.createBlockData(config.getString("road.block").replace(":100.0", "")));
                    plotWorld.setUnclaimedWall(Bukkit.createBlockData(config.getString("wall.block").replace(":100.0", "")));
                    plotWorld.setClaimedWall(Bukkit.createBlockData(config.getString("wall.block_claimed").replace(":100.0", "")));

                    plotWorld.save();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void loadPlots(Database database) throws SQLException {
        plots.getLogger().info("Converting plots...");

        int converted = 0;

        try (PreparedStatement statement = database.prepareStatement("SELECT * FROM plot")) {
            ResultSet results = statement.executeQuery();

            while (results.next()) {
                String world = results.getString("world");
                int x = results.getInt("plot_id_x") - 1;
                int z = results.getInt("plot_id_z") - 1;
                String owner = results.getString("owner");
                Timestamp timestamp = results.getTimestamp("timestamp");
                try {
                    JustPlots.createPlot(world, x, z, UUID.fromString(owner), timestamp.getTime());
                } catch (Exception e) {
                    plots.getLogger().warning("Could not convert plot " + world + ";" + x + ";" + z);
                    e.printStackTrace();
                }

                if (++converted % 1000 == 0) {
                    plots.getLogger().info("Converting plots... (" + converted + ")");
                }
            }
        }
    }

    private void loadHelpers(Database database) throws SQLException {
        plots.getLogger().info("Converting helpers...");

        int converted = 0;

        try (PreparedStatement statement = database.prepareStatement("SELECT * FROM plot_helpers JOIN plot ON plot_helpers.plot_plot_id = plot.id")) {
            ResultSet results = statement.executeQuery();

            while (results.next()) {
                String world = results.getString("world");
                int x = results.getInt("plot_id_x") - 1;
                int z = results.getInt("plot_id_z") - 1;
                String uuid = results.getString("user_uuid");
                try {
                    JustPlots.getPlot(world, x, z).addPlayer(UUID.fromString(uuid));
                } catch (Exception e) {
                    plots.getLogger().warning("Could not convert plot " + world + ";" + x + ";" + z);
                    e.printStackTrace();
                }

                if (++converted % 1000 == 0) {
                    plots.getLogger().info("Converting helpers... (" + converted + ")");
                }
            }
        }
    }

    private void loadTrusted(Database database) throws SQLException {
        plots.getLogger().info("Converting trusted...");

        int converted = 0;

        try (PreparedStatement statement = database.prepareStatement("SELECT * FROM plot_trusted JOIN plot ON plot_trusted.plot_plot_id = plot.id")) {
            ResultSet results = statement.executeQuery();

            while (results.next()) {
                String world = results.getString("world");
                int x = results.getInt("plot_id_x") - 1;
                int z = results.getInt("plot_id_z") - 1;
                String uuid = results.getString("user_uuid");
                try {
                    JustPlots.getPlot(world, x, z).addPlayer(UUID.fromString(uuid));
                } catch (Exception e) {
                    plots.getLogger().warning("Could not convert plot " + world + ";" + x + ";" + z);
                    e.printStackTrace();
                }

                if (++converted % 1000 == 0) {
                    plots.getLogger().info("Converting trusted... (" + converted + ")");
                }
            }
        }
    }
}
