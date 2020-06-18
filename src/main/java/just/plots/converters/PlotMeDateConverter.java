package just.plots.converters;

import just.plots.JustPlots;
import just.plots.Plot;
import just.plots.database.Database;
import just.plots.database.SQLiteDatabase;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.sql.*;
import java.util.UUID;

/**
 * Copies the plot dates from PlotMe if it exists. Useless for servers that
 * updated to PlotSquared as PlotSquared did not copy PlotMe's plot dates.
 */
public class PlotMeDateConverter {

    private final JustPlots plots;

    public PlotMeDateConverter(JustPlots plots) {
        this.plots = plots;

        File plotMeDir = new File(plots.getDataFolder().getParentFile(), "PlotMe");

        if (plotMeDir.isDirectory()) {
            long timer = System.currentTimeMillis();
            plots.getLogger().info("Converting plot dates from PlotMe...");

            convert(plotMeDir);

            if (!plotMeDir.renameTo(new File(plotMeDir.getParentFile(), "PlotMe-Converted-To-JustPlots"))) {
                plots.getLogger().warning("Could not rename " + plotMeDir.getPath());
            }

            plots.getLogger().info("Converted plot dates from PlotMe (took " + (System.currentTimeMillis() - timer) + "ms)");
        }
    }

    private void convert(File plotMeDir) {
        try {
            Database database = new SQLiteDatabase(new File(plotMeDir, "plots.db"));

            loadPlots(database);

            database.closeConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadPlots(Database database) throws SQLException {
        plots.getLogger().info("Converting plot dates...");

        int converted = 0;

        try (PreparedStatement statement = database.prepareStatement("SELECT * FROM plotmePlots")) {
            ResultSet results = statement.executeQuery();

            while (results.next()) {
                String world = results.getString("world");
                int x = results.getInt("idX") - 1;
                int z = results.getInt("idZ") - 1;
                try {
                    DataInputStream blobInputStream = new DataInputStream(new ByteArrayInputStream(results.getBytes("ownerId")));
                    UUID owner = new UUID(blobInputStream.readLong(), blobInputStream.readLong());
                    Date expiredDate = results.getDate("expireddate");

                    Plot plot = JustPlots.getPlot(world, x, z);

                    if (plot != null && plot.getOwner().equals(owner)) {
                        plot.setCreation(expiredDate.getTime());
                    }
                } catch (Exception e) {
                    plots.getLogger().warning("Could not convert plot dates " + world + ";" + x + ";" + z);
                    e.printStackTrace();
                }

                if (++converted % 1000 == 0) {
                    plots.getLogger().info("Converting plot dates... (" + converted + ")");
                }
            }
        }
    }
}
