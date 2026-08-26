package space.devport.danny.regionregen.listeners;

import lombok.RequiredArgsConstructor;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBurnEvent;
import space.devport.danny.regionregen.RegionRegenPlugin;

import java.util.Set;

@RequiredArgsConstructor
public class FireListener implements Listener {

    private final RegionRegenPlugin plugin;

    @EventHandler
    public void onBlockBurn(BlockBurnEvent event) {
        if (!plugin.getConfig().getBoolean("events.fire.enabled", true)) return;

        Block block = event.getBlock();

        Set<String> regenBlocks = WorldGuardUtil.getRegenBlocks(block.getLocation());

        if (regenBlocks == null) return;

        event.setCancelled(true);
    }
}
