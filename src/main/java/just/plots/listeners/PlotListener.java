package just.plots.listeners;

import just.plots.JustPlots;
import just.plots.Plot;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.StructureGrowEvent;

import java.util.Iterator;

public class PlotListener implements Listener {

    public PlotListener(JustPlots plots) {
        plots.getServer().getPluginManager().registerEvents(this, plots);
    }

    private void playerModify(Player player, Block block, Cancellable cancellable) {
        if (!JustPlots.getPlotWorld(block.getWorld()).isPlotWorld()) {
            return; // Not a plot world
        }

        Plot plot = JustPlots.getPlotAt(block.getLocation());

        if ((plot == null || !plot.isAdded(player)) && !player.hasPermission("justplots.edit.other")) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                    new ComponentBuilder("You cannot build here").color(ChatColor.RED).create());

            cancellable.setCancelled(true);
        }
    }

    private void blockModify(Block from, Block to, Cancellable cancellable) {
        if (!JustPlots.getPlotWorld(to.getWorld()).isPlotWorld()) {
            return; // Not a plot world
        }

        Plot fromPlot = JustPlots.getPlotAt(from.getLocation());
        Plot toPlot = JustPlots.getPlotAt(to.getLocation());

        if (fromPlot != toPlot) {
            cancellable.setCancelled(true);
        }
    }

    private void blockModify(Block block, Cancellable cancellable) {
        if (!JustPlots.getPlotWorld(block.getWorld()).isPlotWorld()) {
            return; // Not a plot world
        }

        cancellable.setCancelled(true);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        playerModify(event.getPlayer(), event.getBlock(), event);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        playerModify(event.getPlayer(), event.getBlock(), event);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() != null) {
            playerModify(event.getPlayer(), event.getClickedBlock(), event);
        }
    }

    @EventHandler
    public void onBlockBurn(BlockBurnEvent event) {
        blockModify(event.getBlock(), event);
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        Plot fromPlot = JustPlots.getPlotAt(event.getBlock().getLocation());

        Iterator<Block> iterator = event.blockList().iterator();

        while (iterator.hasNext()) {
            Plot toPlot = JustPlots.getPlotAt(iterator.next().getLocation());

            if (fromPlot != toPlot) {
                iterator.remove();
            }
        }
    }

    @EventHandler
    public void onBlockFade(BlockFadeEvent event) {
        blockModify(event.getBlock(), event);
    }

    @EventHandler
    public void onBlockFertilize(BlockFertilizeEvent event) {
        Plot fromPlot = JustPlots.getPlotAt(event.getBlock().getLocation());

        Iterator<BlockState> iterator = event.getBlocks().iterator();

        while (iterator.hasNext()) {
            Plot toPlot = JustPlots.getPlotAt(iterator.next().getLocation());

            if (fromPlot != toPlot || (event.getPlayer() != null && !event.getPlayer().hasPermission("justplots.edit.other") && (toPlot == null || !toPlot.isAdded(event.getPlayer().getUniqueId())))) {
                iterator.remove();
            }
        }
    }

    @EventHandler
    public void onStructureGrow(StructureGrowEvent event) {
        Plot fromPlot = JustPlots.getPlotAt(event.getLocation());

        Iterator<BlockState> iterator = event.getBlocks().iterator();

        while (iterator.hasNext()) {
            Plot toPlot = JustPlots.getPlotAt(iterator.next().getLocation());

            if (fromPlot != toPlot || (event.getPlayer() != null && !event.getPlayer().hasPermission("justplots.edit.other") && (toPlot == null || !toPlot.isAdded(event.getPlayer().getUniqueId())))) {
                iterator.remove();
            }
        }
    }

    @EventHandler
    public void onBlockFromTo(BlockFromToEvent event) {
        blockModify(event.getBlock(), event.getToBlock(), event);
    }

    @EventHandler
    public void onBlockGrow(BlockGrowEvent event) {
        blockModify(event.getBlock(), event.getNewState().getBlock(), event);
    }

    @EventHandler
    public void onBlockIgnite(BlockIgniteEvent event) {
        if (event.getIgnitingBlock() != null) {
            blockModify(event.getIgnitingBlock(), event.getBlock(), event);
        }
    }

    @EventHandler
    public void onPistonExtend(BlockPistonExtendEvent event) {
        for (Block block : event.getBlocks()) {
            blockModify(event.getBlock(), block, event);
            blockModify(event.getBlock(), block.getRelative(event.getDirection()), event);
        }
    }

    @EventHandler
    public void onPistonRetract(BlockPistonRetractEvent event) {
        for (Block block : event.getBlocks()) {
            blockModify(event.getBlock(), block, event);
        }
    }

    @EventHandler
    public void onBlockSpread(BlockSpreadEvent event) {
        blockModify(event.getSource(), event.getBlock(), event);
    }

    @EventHandler
    public void onLeavesDecay(LeavesDecayEvent event) {
        blockModify(event.getBlock(), event);
    }

    @EventHandler
    public void onSpongeAbsorb(SpongeAbsorbEvent event) {
        Plot fromPlot = JustPlots.getPlotAt(event.getBlock().getLocation());

        Iterator<BlockState> iterator = event.getBlocks().iterator();

        while (iterator.hasNext()) {
            Plot toPlot = JustPlots.getPlotAt(iterator.next().getLocation());

            if (fromPlot != toPlot) {
                iterator.remove();
            }
        }
    }

}
