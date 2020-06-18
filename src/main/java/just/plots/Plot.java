package just.plots;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Plot implements Comparable<Plot> {
    private final PlotID plotId;
    private final String world;
    private final int x;
    private final int z;
    private final UUID owner;
    private long creation;
    private final PlotWorld plotWorld;

    private final HashSet<UUID> added = new HashSet<>();

    public Plot(String world, int x, int z, UUID owner, long creation) {
        this.plotId = new PlotID(x, z);
        this.world = world;
        this.x = x;
        this.z = z;
        this.owner = owner;
        this.creation = creation;

        this.plotWorld = JustPlots.getPlotWorld(world);
        this.plotWorld.addPlot(this);

        Set<Plot> playerPlots = JustPlots.getPlotsIfCached(owner);
        if (playerPlots != null) {
            playerPlots.add(this);
        }
    }

    public void createInDatabase() {
        try (PreparedStatement statement = JustPlots.getDatabase().prepareStatement(
                "INSERT OR IGNORE INTO justplots_plots (world, x, z, owner, creation) VALUES (?, ?, ?, ?, ?)"
        )) {
            statement.setString(1, world);
            statement.setInt(2, x);
            statement.setInt(3, z);
            statement.setString(4, owner.toString());
            statement.setTimestamp(5, new Timestamp(creation));
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void delete() {
        try (PreparedStatement statement = JustPlots.getDatabase().prepareStatement(
                "DELETE FROM justplots_plots WHERE world = ? AND x = ? AND z = ?"
        )) {
            statement.setString(1, world);
            statement.setInt(2, x);
            statement.setInt(3, z);
            statement.executeUpdate();

            Set<Plot> playerPlots = JustPlots.getPlotsIfCached(owner);
            if (playerPlots != null) {
                playerPlots.remove(this);
            }

            plotWorld.removePlot(this);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void setCreation(long time) {
        try (PreparedStatement statement = JustPlots.getDatabase().prepareStatement(
                "UPDATE justplots_plots SET creation = ? WHERE world = ? AND x = ? AND z = ?"
        )) {
            statement.setTimestamp(1, new Timestamp(creation));
            statement.setString(2, world);
            statement.setInt(3, x);
            statement.setInt(4, z);
            statement.executeUpdate();

            creation = time;

            Set<Plot> playerPlots = JustPlots.getPlotsIfCached(owner);
            if (playerPlots != null) {
                // Re-add it to fix ordering
                playerPlots.remove(this);
                playerPlots.add(this);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isAdded(UUID uuid) {
        return added.contains(uuid);
    }

    public void addPlayer(UUID uuid) {
        try (PreparedStatement statement = JustPlots.getDatabase().prepareStatement(
                "INSERT OR IGNORE INTO justplots_added (world, x, z, uuid) VALUES (?, ?, ?, ?)"
        )) {
            statement.setString(1, world);
            statement.setInt(2, x);
            statement.setInt(3, z);
            statement.setString(4, uuid.toString());
            statement.executeUpdate();

            added.add(uuid);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void removePlayer(UUID uuid) {
        try (PreparedStatement statement = JustPlots.getDatabase().prepareStatement(
                "DELETE FROM justplots_added WHERE world = ? AND x = ? AND z = ? AND uuid = ?"
        )) {
            statement.setString(1, world);
            statement.setInt(2, x);
            statement.setInt(3, z);
            statement.setString(4, uuid.toString());
            statement.executeUpdate();

            added.remove(uuid);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return world + ";" + plotId.toString();
    }

    @Override
    public int hashCode() {
        return world.hashCode() ^ plotId.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Plot && ((Plot) other).world.equals(world) && ((Plot) other).plotId.equals(plotId);
    }

    @Override
    public int compareTo(Plot other) {
        long diff = creation - other.creation;

        if (diff > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }

        if (diff < Integer.MIN_VALUE) {
            return Integer.MIN_VALUE;
        }

        if (diff == 0 && !equals(other)) {
            return hashCode() - other.hashCode();
        }

        return (int) diff;
    }

    public PlotID getId() {
        return plotId;
    }

    public String getWorldName() {
        return world;
    }

    public PlotWorld getPlotWorld() {
        return plotWorld;
    }

    public UUID getOwner() {
        return owner;
    }

    public Set<UUID> getAdded() {
        return added;
    }

    public long getCreation() {
        return creation;
    }

    public Location getHome() {
        return new Location(Bukkit.getWorld(world),
                (plotWorld.getPlotSize() + plotWorld.getRoadSize()) * x + plotWorld.getRoadSize() / 2.0 + plotWorld.getPlotSize() / 2.0 + 0.5,
                plotWorld.getFloorHeight() + 1,
                (plotWorld.getPlotSize() + plotWorld.getRoadSize()) * z + plotWorld.getRoadSize() / 2.0 - 0.5);
    }
}
