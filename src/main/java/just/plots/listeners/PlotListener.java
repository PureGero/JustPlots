package just.plots.listeners;

import io.papermc.lib.PaperLib;
import just.plots.JustPlots;
import just.plots.Plot;
import just.plots.util.PaperUtil;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Dispenser;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.world.StructureGrowEvent;

import java.util.Iterator;

public class PlotListener implements Listener {

    public PlotListener(JustPlots plots) {
        plots.getServer().getPluginManager().registerEvents(this, plots);
    }

    private void playerModify(Player player, Block block, Cancellable cancellable) {
        playerModify(player, block.getLocation(), cancellable);
    }

    private void playerModify(Player player, Entity entity, Cancellable cancellable) {
        playerModify(player, entity.getLocation(), cancellable);
    }

    private void playerModify(Player player, Location location, Cancellable cancellable) {
        if (!JustPlots.getPlotWorld(location.getWorld()).isPlotWorld()) {
            return; // Not a plot world
        }

        Plot plot = JustPlots.getPlotAt(location);

        if ((plot == null || !plot.isAdded(player)) && !player.hasPermission("justplots.edit.other")) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                    new ComponentBuilder("You cannot build here").color(ChatColor.RED).create());

            cancellable.setCancelled(true);
        }
    }

    private void entityModify(Entity entity, Block block, Cancellable cancellable) {
        entityModify(entity, block.getLocation(), cancellable);
    }

    private void entityModify(Entity entity, Location location, Cancellable cancellable) {
        if (!JustPlots.getPlotWorld(location.getWorld()).isPlotWorld()) {
            return; // Not a plot world
        }

        Plot originPlot = JustPlots.getPlotAt(entity);
        if (PaperLib.isPaper()) {
            originPlot = JustPlots.getPlotAt(PaperUtil.getOrigin(entity));
        }

        Plot plot = JustPlots.getPlotAt(location);

        if (plot == null || originPlot != plot) {
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

        if (event.getClickedBlock() == null) {
            Block block = event.getPlayer().getTargetBlock(5);
            if (block != null && !block.isEmpty()) {
                playerModify(event.getPlayer(), block, event);
            }
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
    public void onEntityExplode(EntityExplodeEvent event) {
        Plot fromPlot = JustPlots.getPlotAt(event.getEntity());

        if (PaperLib.isPaper()) {
            // Use their origin plot instead of wherever they are now
            fromPlot = JustPlots.getPlotAt(PaperUtil.getOrigin(event.getEntity()));
        }

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
        if (event.getIgnitingEntity() instanceof Player) {
            playerModify((Player) event.getIgnitingEntity(), event.getBlock(), event);
        } else if (event.getIgnitingEntity() != null) {
            entityModify(event.getIgnitingEntity(), event.getBlock(), event);
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

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        Entity damager = getSource(event.getDamager());
        if (damager instanceof Player && !(event.getEntity() instanceof Player)) {
            playerModify((Player) damager, event.getEntity(), event);
        }
    }

    @EventHandler
    public void onVehicleDamage(VehicleDamageEvent event) {
        Entity attacker = getSource(event.getAttacker());
        if (attacker instanceof Player) {
            playerModify((Player) attacker, event.getVehicle(), event);
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        playerModify(event.getPlayer(), event.getRightClicked(), event);
    }

    @EventHandler
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
        playerModify(event.getPlayer(), event.getRightClicked(), event);
    }

    @EventHandler
    public void onHangingBreak(HangingBreakByEntityEvent event) {
        Entity remover = getSource(event.getRemover());
        if (remover instanceof Player) {
            playerModify((Player) remover, event.getEntity(), event);
        }
    }

    @EventHandler
    public void onBlockDispense(BlockDispenseEvent event) {
        Block facing = event.getBlock().getRelative(((Directional) event.getBlock().getBlockData()).getFacing());
        blockModify(event.getBlock(), facing, event);
    }

    @EventHandler
    public void onPlayerFish(PlayerFishEvent event) {
        if (event.getCaught() != null) {
            playerModify(event.getPlayer(), event.getCaught(), event);
        }
    }

    private Entity getSource(Entity entity) {
        if (entity instanceof Projectile && ((Projectile) entity).getShooter() instanceof Entity) {
            return getSource((Entity) ((Projectile) entity).getShooter());
        }

        return entity;
    }

}
