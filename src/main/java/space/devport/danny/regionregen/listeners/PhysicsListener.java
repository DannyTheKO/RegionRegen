package space.devport.danny.regionregen.listeners;

import lombok.RequiredArgsConstructor;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;
import space.devport.danny.regionregen.RegionRegenPlugin;

@RequiredArgsConstructor
public class PhysicsListener implements Listener {

    private final RegionRegenPlugin plugin;

    @EventHandler
    public void onPhysics(BlockPhysicsEvent event) {
        if (!plugin.getConfig().getBoolean("disable-physics", true)) return;

        Block changedBlock = event.getSourceBlock();

        if (plugin.getRegenerationManager().hasTaskAt(changedBlock.getLocation())) {
            event.setCancelled(true);
            return;
        }

        if (plugin.getDecayManager().hasTaskAt(changedBlock.getLocation())
                || plugin.getDecayManager().hasTaskAt(event.getBlock().getLocation())) {
            event.setCancelled(true);
        }
    }
}
