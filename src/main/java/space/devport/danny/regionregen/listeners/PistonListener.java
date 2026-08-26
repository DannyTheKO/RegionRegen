package space.devport.danny.regionregen.listeners;

import lombok.RequiredArgsConstructor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import space.devport.danny.regionregen.RegionRegenPlugin;

@RequiredArgsConstructor
public class PistonListener implements Listener {

    private final RegionRegenPlugin plugin;

    @EventHandler
    public void onPistonExtend(BlockPistonExtendEvent event) {
        if (!plugin.getConfig().getBoolean("events.piston.enabled", true)) return;

        BlockFace direction = event.getDirection();

        for (Block block : event.getBlocks()) {
            if (WorldGuardUtil.getRegenBlocks(block.getLocation()) != null) {
                event.setCancelled(true);
                return;
            }

            Block destination = block.getRelative(direction);
            if (WorldGuardUtil.getRegenBlocks(destination.getLocation()) != null) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onPistonRetract(BlockPistonRetractEvent event) {
        if (!plugin.getConfig().getBoolean("events.piston.enabled", true)) return;

        BlockFace direction = event.getDirection().getOppositeFace();

        for (Block block : event.getBlocks()) {
            if (WorldGuardUtil.getRegenBlocks(block.getLocation()) != null) {
                event.setCancelled(true);
                return;
            }

            Block destination = block.getRelative(direction);
            if (WorldGuardUtil.getRegenBlocks(destination.getLocation()) != null) {
                event.setCancelled(true);
                return;
            }
        }
    }
}