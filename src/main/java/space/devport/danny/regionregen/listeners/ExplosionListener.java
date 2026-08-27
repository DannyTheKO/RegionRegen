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

import space.devport.danny.regionregen.util.MaterialMatcher;

import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
public class ExplosionListener implements Listener {

    private final RegionRegenPlugin plugin;

    private final MaterialMatcher materialMatcher;

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

        int delaySeconds = plugin.getBreakDelay();
        List<String> excludedBlocks = plugin.getConfig().getStringList("events.break.excluded-blocks");

        for (Block originalBlock : listBlock) {
            if (plugin.getDecayManager().hasTaskAt(originalBlock.getLocation())) continue;

            boolean excluded = false;
            for (String pattern : excludedBlocks) {
                if (materialMatcher.matches(pattern, originalBlock.getType())) {
                    excluded = true;
                    break;
                }
            }
            if (excluded) continue;

            ApplicableRegionSet regions = query.getApplicableRegions(BukkitAdapter.adapt(originalBlock.getLocation()));

            Set<String> regenBlocks = regions.queryValue(null, RegionRegenPlugin.WG_BLOCK_REGEN_FLAG);
            if (regenBlocks == null) continue;

            for (String blockSyntax : regenBlocks) {
                MaterialMatcher.BlockSpec spec = materialMatcher.parse(blockSyntax, delaySeconds);
                if (spec == null) continue;
                if (!materialMatcher.matches(spec.materialPattern(), originalBlock.getType())) continue;

                int effectiveDelay = spec.delaySeconds();
                if (!plugin.getRegenerationManager().hasTaskAt(originalBlock.getLocation())) {
                    plugin.getRegenerationManager().startTask(originalBlock, effectiveDelay);
                }
                hasFlaggedBlock = true;
                break;
            }
        }

        return hasFlaggedBlock;
    }
}
