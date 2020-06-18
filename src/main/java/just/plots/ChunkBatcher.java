package just.plots;

import io.papermc.lib.PaperLib;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A chunk based block modifier. Spilts the work into chunks and loads each
 * chunk async before modifying it.
 */
public class ChunkBatcher implements Runnable {

    private final World world;

    private final HashMap<Point, ChunkToModify> chunks = new HashMap<>();

    public ChunkBatcher(World world) {
        this.world = world;
    }

    public void setBlock(int x, int y, int z, BlockData blockData) {
        ChunkToModify chunk = chunks.computeIfAbsent(new Point((x >> 4), (z >> 4)), key -> new ChunkToModify(x >> 4, z >> 4));
        chunk.addBlock(x & 0xF, y, z & 0xF, blockData);
    }

    public void run() {
        for (ChunkToModify chunk : chunks.values()) {
            chunk.run();
        }
    }


    private class BlockToModify {

        private final int x;
        private final int y;
        private final int z;
        private final BlockData blockData;

        public BlockToModify(int x, int y, int z, BlockData blockData) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.blockData = blockData;
        }
    }

    private class ChunkToModify implements Runnable {

        private final int cx;
        private final int cz;
        private final List<BlockToModify> blocksToModify = new ArrayList<>();

        public ChunkToModify(int cx, int cz) {
            this.cx = cx;
            this.cz = cz;
        }

        public void addBlock(int x, int y, int z, BlockData blockData) {
            blocksToModify.add(new BlockToModify(x, y, z, blockData));
        }

        @Override
        public void run() {
            PaperLib.getChunkAtAsync(world, cx, cz).thenAccept(chunk -> {
                for (BlockToModify block : blocksToModify) {
                    chunk.getBlock(block.x, block.y, block.z).setBlockData(block.blockData, false);
                }
            });
        }
    }
}
