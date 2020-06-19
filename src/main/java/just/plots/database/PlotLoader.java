package just.plots.database;

import just.plots.JustPlots;
import just.plots.Plot;
import org.bukkit.configuration.ConfigurationSection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.UUID;

public class PlotLoader implements Runnable {

    private final JustPlots plots;

    public PlotLoader(JustPlots plots) {
        this.plots = plots;

        loadPlotWorlds();

        // Load the plots async - Not really necessary
        //plots.getServer().getScheduler().runTaskAsynchronously(plots, this);
        run();
    }

    private void loadPlotWorlds() {
        ConfigurationSection worldsSection = plots.getConfig().getConfigurationSection("worlds");

        if (worldsSection != null) {
            for (String name : worldsSection.getKeys(false)) {
                ConfigurationSection config = worldsSection.getConfigurationSection(name);
                JustPlots.getPlotWorld(name).load(config);
            }
        }
    }

    private void loadPlots() {
        int counter = 0;

        try (PreparedStatement statement = JustPlots.getDatabase().prepareStatement("SELECT * FROM justplots_plots")) {
            ResultSet results = statement.executeQuery();

            while (results.next()) {
                String world = results.getString("world");
                int x = results.getInt("x");
                int z = results.getInt("z");
                String owner = results.getString("owner");
                Timestamp creation = results.getTimestamp("creation");
                try {
                    new Plot(world, x, z, UUID.fromString(owner), creation.getTime());
                } catch (Exception e) {
                    plots.getLogger().warning("Could not load plot " + world + ";" + x + ";" + z);
                    e.printStackTrace();
                }

                if (++counter % 10000 == 0) {
                    plots.getLogger().info("Loading plots... (" + counter + ")");
                }
            }
        } catch (SQLException e) {
            plots.getLogger().severe("FAILED TO LOAD PLOTS");
            e.printStackTrace();
            return;
        }
    }

    private void loadAdded() {
        int counter = 0;

        try (PreparedStatement statement = JustPlots.getDatabase().prepareStatement("SELECT * FROM justplots_added")) {
            ResultSet results = statement.executeQuery();

            while (results.next()) {
                String world = results.getString("world");
                int x = results.getInt("x");
                int z = results.getInt("z");
                String owner = results.getString("uuid");
                try {
                    Plot plot = JustPlots.getPlot(world, x, z);
                    plot.getAdded().add(UUID.fromString(owner));
                } catch (Exception e) {
                    plots.getLogger().warning("Could not load added player for plot " + world + ";" + x + ";" + z);
                    e.printStackTrace();
                }

                if (++counter % 10000 == 0) {
                    plots.getLogger().info("Loading added players... (" + counter + ")");
                }
            }
        } catch (SQLException e) {
            plots.getLogger().severe("FAILED TO LOAD ADDED PLAYERS");
            e.printStackTrace();
            return;
        }
    }

    @Override
    public void run() {
        long timer = System.currentTimeMillis();
        plots.getLogger().info("Loading plots...");

        loadPlots();
        loadAdded();

        plots.getLogger().info("Loaded plots (took " + (System.currentTimeMillis() - timer) + "ms)");
    }
}
