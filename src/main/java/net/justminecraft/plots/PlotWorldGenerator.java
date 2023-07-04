package net.justminecraft.plots;

import net.justminecraft.plots.util.WorldHeight;
import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PlotWorldGenerator extends ChunkGenerator {

    private static final double sqrt2 = Math.sqrt(2);

    private ArrayList<BlockPopulator> blockPopulators = new ArrayList<>();

    public PlotWorldGenerator() {
        for (PlotWorld plotWorld : JustPlots.getPlotWorlds()) {
            if (plotWorld.isPlotWorld() && Bukkit.getWorld(plotWorld.getWorld()) == null) {
                boolean firstTimeGenerating = !new File(plotWorld.getWorld()).isDirectory();

                World world = Bukkit.createWorld(new WorldCreator(plotWorld.getWorld()).generator(this));

                if (world == null) {
                    JustPlots.getPlugin().getLogger().warning("Failed to load world " + plotWorld.getWorld());
                    continue;
                }

                world.setKeepSpawnInMemory(false);

                if (firstTimeGenerating) {
                    world.setGameRule(GameRule.DISABLE_RAIDS, true);
                    world.setGameRule(GameRule.DO_FIRE_TICK, false);
                    world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
                    world.setGameRule(GameRule.DO_PATROL_SPAWNING, false);
                    world.setGameRule(GameRule.MOB_GRIEFING, false);

                    world.setSpawnLocation(0, plotWorld.getFloorHeight() + 1, 0);
                }
            }
        }
    }

    @NotNull
    public List<BlockPopulator> getDefaultPopulators(@NotNull World world) {
        return blockPopulators;
    }

    /**
     * Add a block populator to the plot world generator. This will also add the
     * block populator to existing worlds using the generator.
     * @param blockPopulator The block populator to add
     */
    public void addBlockPopulator(BlockPopulator blockPopulator) {
        blockPopulators.add(blockPopulator);

        for (World world : Bukkit.getWorlds()) {
            if (world.getGenerator() == this) {
                world.getPopulators().add(blockPopulator);
            }
        }
    }

    @NotNull
    @Override
    public ChunkData generateChunkData(@NotNull World world, @NotNull Random random, int cx, int cz, @NotNull BiomeGrid biomeGrid) {
        PlotWorld plotWorld = JustPlots.getPlotWorld(world);

        if (!plotWorld.isPlotWorld()) {
            throw new IllegalArgumentException("World " + world.getName() + " is not a PlotWorld");
        }

        ChunkData chunkData = createChunkData(world);

        generateBase(plotWorld, chunkData, world);

        generateBiomes(plotWorld, biomeGrid, world);

        generateRoad(plotWorld, cx, cz, chunkData);

        return chunkData;
    }

    private void generateBase(PlotWorld plotWorld, ChunkData chunkData, World world) {
        int miny = WorldHeight.getMinHeight(world);
        int floory = plotWorld.getFloorHeight();

        for (int x = 0; x < 16; x ++) {
            for (int z = 0; z < 16; z ++) {

                chunkData.setBlock(x, miny, z, Material.BEDROCK);

                for (int y = miny + 1; y < floory; y ++) {
                    chunkData.setBlock(x, y, z, Material.DIRT);
                }

                chunkData.setBlock(x, floory, z, Material.GRASS_BLOCK);

            }
        }
    }

    private void generateBiomes(PlotWorld plotWorld, BiomeGrid biomeGrid, World world) {
        int miny = WorldHeight.getMinHeight(world);
        int maxy = WorldHeight.getMaxHeight(world);

        for (int x = 0; x < 16; x ++) {
            for (int z = 0; z < 16; z ++) {
                for (int y = miny; y < maxy; y ++) {
                    biomeGrid.setBiome(x, y, z, Biome.PLAINS);
                }
            }
        }
    }

    private void generateRoad(PlotWorld plotWorld, int cx, int cz, ChunkData chunkData) {
        int roadSize = (int) Math.ceil(plotWorld.getRoadSize() / 2.0) - 1;
        int size = plotWorld.getPlotSize() + plotWorld.getRoadSize();

        for (int i = 0; i < 16; i ++) {
            for (int j = 0; j < 16; j++) {

                int x = cx << 4 | i;
                int y = plotWorld.getFloorHeight();
                int z = cz << 4 | j;

                int dx = Math.floorMod(x, size);
                int dz = Math.floorMod(z, size);

                if (dx > size / 2) {
                    dx = size - dx;

                    if (plotWorld.getRoadSize() % 2 == 0) {
                        dx -= 1;
                    }
                }

                if (dz > size / 2) {
                    dz = size - dz;

                    if (plotWorld.getRoadSize() % 2 == 0) {
                        dz -= 1;
                    }
                }

                if ((dx == roadSize && dz >= roadSize) || (dz == roadSize && dx >= roadSize)) {
                    chunkData.setBlock(i, y + 1, j, plotWorld.getUnclaimedWall());
                }

                int block = -1;

                if (dx <= roadSize && dz <= roadSize) {
                    double distance = Math.sqrt(dx * dx + dz * dz) / sqrt2;
                    block = (int) Math.floor(distance) % 2;
                } else if (dx < roadSize - 1 || dz < roadSize - 1) {
                    block = 0;
                } else if (dx < roadSize || dz < roadSize) {
                    block = 1;
                }

                if (block == 0) {
                    chunkData.setBlock(i, y, j, plotWorld.getRoadInnerBlock());
                } else if (block == 1) {
                    chunkData.setBlock(i, y, j, plotWorld.getRoadOuterBlock());
                }

            }
        }
    }

}
