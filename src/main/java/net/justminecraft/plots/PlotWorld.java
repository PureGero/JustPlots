package net.justminecraft.plots;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Collection;
import java.util.HashMap;

public class PlotWorld {
    private final String world;

    private int plotSize = 0;
    private int roadSize = 7;
    private int floorHeight = 64;
    private BlockData roadOuterBlock = Material.BIRCH_PLANKS.createBlockData();
    private BlockData roadInnerBlock = Material.OAK_PLANKS.createBlockData();
    private BlockData unclaimedWall = Material.QUARTZ_SLAB.createBlockData();
    private BlockData claimedWall = Material.SMOOTH_STONE_SLAB.createBlockData();

    private int autoClaimDistance = 0;

    private final HashMap<PlotId, Plot> plots = new HashMap<>();

    public PlotWorld(String world) {
        this.world = world;
    }

    public String getWorld() {
        return world;
    }

    public Plot getPlot(int x, int z) {
        return getPlot(new PlotId(x, z));
    }

    public Plot getPlot(PlotId plotID) {
        return plots.get(plotID);
    }

    public void addPlot(Plot plot) {
        plots.put(plot.getId(), plot);
    }

    public boolean isPlotWorld() {
        return plotSize > 0;
    }

    public int getPlotSize() {
        return plotSize;
    }

    public int getRoadSize() {
        return roadSize;
    }

    public int getFloorHeight() {
        return floorHeight;
    }

    public Collection<Plot> getPlots() {
        return plots.values();
    }

    public void removePlot(Plot plot) {
        plots.remove(plot.getId());

        int distance = Math.max(Math.abs(plot.getId().getX()), Math.abs(plot.getId().getZ()));
        if (distance < autoClaimDistance) {
            autoClaimDistance = distance;
        }
    }

    public void setPlotSize(int plotSize) {
        this.plotSize = plotSize;
    }

    public void setRoadSize(int roadSize) {
        this.roadSize = roadSize;
    }

    public void setFloorHeight(int floorHeight) {
        this.floorHeight = floorHeight;
    }

    public BlockData getRoadOuterBlock() {
        return roadOuterBlock;
    }

    public void setRoadOuterBlock(BlockData roadOuterBlock) {
        this.roadOuterBlock = roadOuterBlock;
    }

    public BlockData getRoadInnerBlock() {
        return roadInnerBlock;
    }

    public void setRoadInnerBlock(BlockData roadInnerBlock) {
        this.roadInnerBlock = roadInnerBlock;
    }

    public BlockData getUnclaimedWall() {
        return unclaimedWall;
    }

    public void setUnclaimedWall(BlockData unclaimedWall) {
        this.unclaimedWall = unclaimedWall;
    }

    public BlockData getClaimedWall() {
        return claimedWall;
    }

    public void setClaimedWall(BlockData claimedWall) {
        this.claimedWall = claimedWall;
    }

    public PlotId nextAutoClaimPlot() {
        if (!isPlotWorld()) {
            return null;
        }

        while (true) {
            for (int x = -autoClaimDistance; x <= autoClaimDistance; x++) {
                PlotId id = new PlotId(x, autoClaimDistance);
                if (getPlot(id) == null) {
                    return id;
                }
            }

            for (int z = autoClaimDistance; z >= -autoClaimDistance; z--) {
                PlotId id = new PlotId(autoClaimDistance, z);
                if (getPlot(id) == null) {
                    return id;
                }
            }

            for (int x = autoClaimDistance; x >= -autoClaimDistance; x--) {
                PlotId id = new PlotId(x, -autoClaimDistance);
                if (getPlot(id) == null) {
                    return id;
                }
            }

            for (int z = -autoClaimDistance; z <= autoClaimDistance; z++) {
                PlotId id = new PlotId(-autoClaimDistance, z);
                if (getPlot(id) == null) {
                    return id;
                }
            }

            autoClaimDistance++;
        }
    }

    public void load(ConfigurationSection config) {
        try {
            plotSize = config.getInt("plot_size");
            roadSize = config.getInt("road_size");
            floorHeight = config.getInt("floor_height");
            roadInnerBlock = Bukkit.createBlockData(config.getString("road_inner_block"));
            roadOuterBlock = Bukkit.createBlockData(config.getString("road_outer_block"));
            unclaimedWall = Bukkit.createBlockData(config.getString("wall_unclaimed"));
            claimedWall = Bukkit.createBlockData(config.getString("wall_claimed"));
        } catch (Exception e) {
            JustPlots.getPlugin().getLogger().severe("FAILED TO LOAD CONFIGURATION FOR WORLD " + world);
            e.printStackTrace();
        }
    }

    public void save() {
        FileConfiguration pluginConfig = JustPlots.getPlugin().getConfig();

        ConfigurationSection worlds = pluginConfig.getConfigurationSection("worlds");

        if (worlds == null) {
            worlds = pluginConfig.createSection("worlds");
        }

        ConfigurationSection config = worlds.getConfigurationSection(world);

        if (config == null) {
            config = worlds.createSection(world);
        }

        config.set("plot_size", plotSize);
        config.set("road_size", roadSize);
        config.set("floor_height", floorHeight);
        config.set("road_inner_block", roadInnerBlock.getAsString());
        config.set("road_outer_block", roadOuterBlock.getAsString());
        config.set("wall_unclaimed", unclaimedWall.getAsString());
        config.set("wall_claimed", claimedWall.getAsString());

        JustPlots.getPlugin().saveConfig();
    }
}
