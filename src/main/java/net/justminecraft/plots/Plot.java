package net.justminecraft.plots;

import io.papermc.lib.PaperLib;
import net.justminecraft.plots.events.PlotDeletedEvent;
import net.justminecraft.plots.util.AsyncUtil;
import net.justminecraft.plots.util.PaperUtil;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class Plot implements Comparable<Plot> {
    private final PlotId plotId;
    private final String world;
    private final int x;
    private final int z;
    private final UUID owner;
    private long creation;
    private final PlotWorld plotWorld;

    private final HashSet<UUID> added = new HashSet<>();

    public Plot(String world, int x, int z, UUID owner, long creation) {
        this.plotId = new PlotId(x, z);
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

            try (PreparedStatement statement2 = JustPlots.getDatabase().prepareStatement(
                    "DELETE FROM justplots_added WHERE world = ? AND x = ? AND z = ?"
            )) {
                statement2.setString(1, world);
                statement2.setInt(2, x);
                statement2.setInt(3, z);
                statement2.executeUpdate();
            }

            Set<Plot> playerPlots = JustPlots.getPlotsIfCached(owner);
            if (playerPlots != null) {
                playerPlots.remove(this);
            }

            plotWorld.removePlot(this);

            Bukkit.getScheduler().runTask(JustPlots.getPlugin(), () -> Bukkit.getServer().getPluginManager().callEvent(new PlotDeletedEvent(this, owner)));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void setCreation(long time) {
        try (PreparedStatement statement = JustPlots.getDatabase().prepareStatement(
                "UPDATE justplots_plots SET creation = ? WHERE world = ? AND x = ? AND z = ?"
        )) {
            statement.setTimestamp(1, new Timestamp(time));
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

    public boolean isOwner(OfflinePlayer player) {
        return isOwner(player.getUniqueId());
    }

    public boolean isOwner(UUID uuid) {
        return owner.equals(uuid);
    }

    public boolean isAdded(OfflinePlayer player) {
        return isAdded(player.getUniqueId());
    }

    public boolean isAdded(UUID uuid) {
        return isOwner(uuid) || added.contains(uuid);
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

    public PlotId getId() {
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

    public HashSet<UUID> getAdded() {
        return added;
    }

    public long getCreation() {
        return creation;
    }

    public Location getHome() {
        Location bottom = getBottom();
        Location top = getTop();

        return new Location(Bukkit.getWorld(world),
                bottom.getBlockX() + (top.getBlockX() - bottom.getBlockX()) / 2.0 + 0.5,
                plotWorld.getFloorHeight() + 1,
                bottom.getBlockZ() - 1.5);
    }

    public String getCreationDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(getCreation());
        return calendar.get(Calendar.YEAR) + "-" + (calendar.get(Calendar.MONTH) + 1) + "-" + calendar.get(Calendar.DATE);
    }

    public Location getSign() {
        Location sign = getBottom();

        sign.add(-1, plotWorld.getFloorHeight() + 1, -2);

        return sign;
    }

    public CompletableFuture<Block> getSignBlockAsync() {
        CompletableFuture<Block> future = new CompletableFuture<>();

        Location signLoc = getSign();

        PaperLib.getChunkAtAsync(getSign()).thenAccept(chunk -> AsyncUtil.ensureMainThread(() -> {
            Block signBlock = chunk.getBlock(signLoc.getBlockX() & 0xF, signLoc.getBlockY(), signLoc.getBlockZ() & 0xF);
            future.complete(signBlock);
        }));

        return future;
    }

    public void updateSign() {
        getSignBlockAsync().thenAccept(signBlock -> {
            signBlock.setType(Material.OAK_WALL_SIGN, false);

            Sign sign = (Sign) signBlock.getState();
            sign.setLine(0, plotId.toString());
            sign.setLine(1, JustPlots.getUsername(owner));
            sign.setLine(3, getCreationDate());
            sign.update();
        });
    }

    public void clearSign() {
        getSignBlockAsync().thenAccept(signBlock -> {
            signBlock.setType(Material.AIR, false);
        });
    }

    public void claimWalls() {
        setWalls(plotWorld.getClaimedWall());
    }

    public void unclaimWalls() {
        setWalls(plotWorld.getUnclaimedWall());
    }

    private void setWalls(BlockData block) {
        ChunkBatcher chunkBatcher = new ChunkBatcher(Bukkit.getWorld(this.world));

        Location top = getTop();
        Location bottom = getBottom();

        int fromx = bottom.getBlockX() - 1;
        int fromz = bottom.getBlockZ() - 1;
        int tox = top.getBlockX() + 1;
        int toz = top.getBlockZ() + 1;

        for (int x = fromx; x <= tox; x++) {
            chunkBatcher.setBlock(x, plotWorld.getFloorHeight() + 1, fromz, block);
            chunkBatcher.setBlock(x, plotWorld.getFloorHeight() + 1, toz, block);
        }

        for (int z = fromz; z <= toz; z++) {
            chunkBatcher.setBlock(fromx, plotWorld.getFloorHeight() + 1, z, block);
            chunkBatcher.setBlock(tox, plotWorld.getFloorHeight() + 1, z, block);
        }

        chunkBatcher.run();
    }

    public void clear() {
        Location from = getBottom();
        Location to = getTop();

        ResetManager.reset(plotWorld, from.getBlockX(), from.getBlockZ(), to.getBlockX(), to.getBlockZ());

        clearEntities();
    }

    private void clearEntities() {
        World world = Bukkit.getWorld(this.world);

        if (world == null) {
            return;
        }

        AsyncUtil.ensureMainThread(() -> {
            for (Entity entity : world.getEntities()) {
                if (!(entity instanceof Player) && (inPlot(entity) || inPlot(PaperUtil.getOrigin(entity)))) {
                    entity.remove();
                }
            }
        });
    }

    public boolean inPlot(Entity entity) {
        return inPlot(entity.getLocation());
    }

    public boolean inPlot(Location location) {
        Location top = getTop();
        Location bottom = getBottom();

        return bottom.getX() <= location.getBlockX() && location.getBlockX() <= top.getX() &&
                bottom.getZ() <= location.getBlockZ() && location.getBlockZ() <= top.getZ();
    }

    public Location getBottom() {
        return new Location(Bukkit.getWorld(this.world),
                Math.ceil((plotWorld.getPlotSize() + plotWorld.getRoadSize()) * x + plotWorld.getRoadSize() / 2.0),
                0,
                Math.ceil((plotWorld.getPlotSize() + plotWorld.getRoadSize()) * z + plotWorld.getRoadSize() / 2.0));
    }

    public Location getTop() {
        return new Location(Bukkit.getWorld(this.world),
                Math.ceil((plotWorld.getPlotSize() + plotWorld.getRoadSize()) * x + plotWorld.getRoadSize() / 2.0 + plotWorld.getPlotSize() - 1),
                255,
                Math.ceil((plotWorld.getPlotSize() + plotWorld.getRoadSize()) * z + plotWorld.getRoadSize() / 2.0 + plotWorld.getPlotSize() - 1));
    }

    public Location getMiddle() {
        Location bottom = getBottom();
        Location top = getTop();

        return new Location(Bukkit.getWorld(world),
                bottom.getBlockX() + (top.getBlockX() - bottom.getBlockX()) / 2.0 + 0.5,
                plotWorld.getFloorHeight() + 1,
                bottom.getBlockZ() + (top.getBlockZ() - bottom.getBlockZ()) / 2.0 + 0.5);
    }

    public List<Player> getPlayersInPlot() {
        List<Player> players = new ArrayList<>();

        World world = Bukkit.getWorld(this.world);

        if (world != null) {
            for (Player player : world.getPlayers()) {
                if (inPlot(player)) {
                    players.add(player);
                }
            }
        }

        return players;
    }
}
