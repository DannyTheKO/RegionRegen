package space.devport.danny.regionregen.listeners;

import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFromToEvent;
import space.devport.danny.regionregen.RegionRegenPlugin;

import java.util.Set;

@RequiredArgsConstructor
public class LavaListener implements Listener {

    private final RegionRegenPlugin plugin;

    @EventHandler
    public void onBlockFromTo(BlockFromToEvent event) {
        if (!plugin.getConfig().getBoolean("events.lava.enabled", true)) return;

        Block source = event.getBlock();
        if (source.getType() != Material.LAVA) return;

        Block destination = event.getToBlock();

        Set<String> regenBlocks = WorldGuardUtil.getRegenBlocks(destination.getLocation());

        if (regenBlocks == null) return;

        event.setCancelled(true);
    }
}
