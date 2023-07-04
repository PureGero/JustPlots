package net.justminecraft.plots;

import io.papermc.lib.PaperLib;
import net.justminecraft.plots.util.AsyncUtil;
import net.justminecraft.plots.util.WorldHeight;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;

/**
 * A chunk based resetter. Spilts the work into chunks and resets a few chunks
 * each tick.
 */
public class ResetManager {
    public static void reset(PlotWorld plotWorld, int fromx, int fromz, int tox, int toz) {
        // TODO Use WorldEdit if installed

        double i = 1;

        for (int cx = fromx >> 4; cx <= tox >> 4; cx++) {
            for (int cz = fromz >> 4; cz <= toz >> 4; cz++) {
                int minx = Math.max(cx << 4, fromx);
                int minz = Math.max(cz << 4, fromz);
                int maxx = Math.min(cx << 4 | 15, tox);
                int maxz = Math.min(cz << 4 | 15, toz);

                Bukkit.getScheduler().runTaskLaterAsynchronously(JustPlots.getPlugin(), new ChunkToReset(plotWorld, minx, minz, maxx, maxz), (int) (i += 0.5));
            }
        }

        JustPlots.getPlugin().getLogger().info("Resetting plot in " + (int) i + " ticks (" + (int) i / 20 + " seconds)");
    }

    private static class ChunkToReset implements Runnable {

        private final PlotWorld plotWorld;
        private final int minx;
        private final int minz;
        private final int maxx;
        private final int maxz;

        public ChunkToReset(PlotWorld plotWorld, int minx, int minz, int maxx, int maxz) {
            this.plotWorld = plotWorld;
            this.minx = minx;
            this.minz = minz;
            this.maxx = maxx;
            this.maxz = maxz;
        }

        @Override
        public void run() {
            World world = Bukkit.getWorld(plotWorld.getWorld());

            PaperLib.getChunkAtAsync(world, minx >> 4, minz >> 4, false).thenAccept(chunk -> {
                if (chunk == null) {
                    return; // Not generated
                }

                AsyncUtil.ensureMainThread(() -> {
                    int miny = WorldHeight.getMinHeight(world);
                    int maxy = WorldHeight.getMaxHeight(world);
                    for (int x = minx; x <= maxx; x++) {
                        for (int z = minz; z <= maxz; z++) {
                            for (int y = miny; y < maxy; y++) {
                                Block block = chunk.getBlock(x & 0xF, y, z & 0xF);

                                Material material = Material.AIR;
                                if (y == miny) {
                                    material = Material.BEDROCK;
                                } else if (y < plotWorld.getFloorHeight()) {
                                    material = Material.DIRT;
                                } else if (y == plotWorld.getFloorHeight()) {
                                    material = Material.GRASS_BLOCK;
                                }

                                if (!block.getType().equals(material)) {
                                    block.setType(material, false);
                                }

                                world.setBiome(x, y, z, Biome.PLAINS);
                            }
                        }
                    }
                });
            });
        }
    }
}
