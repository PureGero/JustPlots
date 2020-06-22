package net.justminecraft.plots;

import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.generator.ChunkGenerator;

import java.io.File;
import java.util.Random;

public class PlotWorldGenerator extends ChunkGenerator {

    private static final double sqrt2 = Math.sqrt(2);

    public PlotWorldGenerator() {
        for (PlotWorld plotWorld : JustPlots.getPlotWorlds()) {
            if (plotWorld.isPlotWorld() && Bukkit.getWorld(plotWorld.getWorld()) == null) {
                boolean firstTimeGenerating = !new File(plotWorld.getWorld()).isDirectory();

                World world = Bukkit.createWorld(new WorldCreator(plotWorld.getWorld()).generator(this));
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

    @Override
    public ChunkData generateChunkData(World world, Random random, int cx, int cz, BiomeGrid biomeGrid) {
        PlotWorld plotWorld = JustPlots.getPlotWorld(world);

        if (!plotWorld.isPlotWorld()) {
            throw new IllegalArgumentException("World " + world.getName() + " is not a PlotWorld");
        }

        ChunkData chunkData = createChunkData(world);

        generateBase(plotWorld, chunkData);

        generateBiomes(plotWorld, biomeGrid);

        generateRoad(plotWorld, cx, cz, chunkData);

        return chunkData;
    }

    private void generateBase(PlotWorld plotWorld, ChunkData chunkData) {
        for (int x = 0; x < 16; x ++) {
            for (int z = 0; z < 16; z ++) {

                chunkData.setBlock(x, 0, z, Material.BEDROCK);

                for (int y = 1; y < plotWorld.getFloorHeight(); y ++) {
                    chunkData.setBlock(x, y, z, Material.DIRT);
                }

                chunkData.setBlock(x, plotWorld.getFloorHeight(), z, Material.GRASS_BLOCK);

            }
        }
    }

    private void generateBiomes(PlotWorld plotWorld, BiomeGrid biomeGrid) {
        for (int x = 0; x < 16; x ++) {
            for (int z = 0; z < 16; z ++) {
                for (int y = 0; y < 256; y ++) {
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
