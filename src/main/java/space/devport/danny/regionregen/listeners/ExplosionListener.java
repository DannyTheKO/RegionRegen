package space.devport.danny.regionregen.listeners;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import lombok.RequiredArgsConstructor;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import space.devport.danny.regionregen.RegionRegenPlugin;

import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
public class ExplosionListener implements Listener {

    private final RegionRegenPlugin plugin;

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        if (!plugin.getConfig().getBoolean("events.explosion.enabled", true)) return;

        boolean flagged = handleExplosion(event.blockList());
        if (flagged && plugin.getConfig().getBoolean("events.explosion.deny-drops", true)) {
            event.setYield(0f);
        }
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        if (!plugin.getConfig().getBoolean("events.explosion.enabled", true)) return;

        boolean flagged = handleExplosion(event.blockList());
        if (flagged && plugin.getConfig().getBoolean("events.explosion.deny-drops", true)) {
            event.setYield(0f);
        }
    }

    private boolean handleExplosion(List<Block> listBlock) {
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();

        boolean hasFlaggedBlock = false;

        int delaySeconds = plugin.getConfig().getInt("default-delay", 10);

        for (Block originalBlock : listBlock) {
            ApplicableRegionSet regions = query.getApplicableRegions(BukkitAdapter.adapt(originalBlock.getLocation()));

            Set<String> regenBlocks = regions.queryValue(null, RegionRegenPlugin.WG_BLOCK_REGEN_FLAG);
            if (regenBlocks == null) continue;

            if (!plugin.getRegenerationManager().hasTaskAt(originalBlock.getLocation())) {
                plugin.getRegenerationManager().startTask(originalBlock, delaySeconds);
            }

            hasFlaggedBlock = true;
        }

        return hasFlaggedBlock;
    }
}
