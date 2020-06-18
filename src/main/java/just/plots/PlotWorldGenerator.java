package just.plots;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.block.Biome;
import org.bukkit.generator.ChunkGenerator;

import java.util.Random;

public class PlotWorldGenerator extends ChunkGenerator {

    private static final double sqrt2 = Math.sqrt(2);

    public PlotWorldGenerator() {
        for (PlotWorld plotWorld : JustPlots.getPlotWorlds()) {
            if (plotWorld.isPlotWorld() && Bukkit.getWorld(plotWorld.getWorld()) == null) {
                World world = Bukkit.createWorld(new WorldCreator(plotWorld.getWorld()).generator(this));
                world.setKeepSpawnInMemory(false);
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
        int roadSize = (int) Math.floor(plotWorld.getRoadSize() / 2.0);
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
                }

                if (dz > size / 2) {
                    dz = size - dz;
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