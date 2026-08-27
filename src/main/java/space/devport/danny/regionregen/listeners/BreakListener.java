package space.devport.danny.regionregen.listeners;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import lombok.RequiredArgsConstructor;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import space.devport.danny.regionregen.RegionRegenPlugin;
import space.devport.danny.regionregen.util.MaterialMatcher;

import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
public class BreakListener implements Listener {

    private final RegionRegenPlugin plugin;

    private final MaterialMatcher materialMatcher;

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        if (!plugin.getConfig().getBoolean("events.break.enabled", true)) return;

        Block originalBlock = event.getBlock();
        Player player = event.getPlayer();

        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);
        ApplicableRegionSet regions = query.getApplicableRegions(BukkitAdapter.adapt(originalBlock.getLocation()));

        if (plugin.getDecayManager().hasTaskAt(originalBlock.getLocation())) {
            plugin.getDecayManager().cancelAt(originalBlock.getLocation());
            return;
        }

        Set<String> regenBlocks = regions.queryValue(localPlayer, RegionRegenPlugin.WG_BLOCK_REGEN_FLAG);

        // Null Gate
        if (regenBlocks == null) return;

        List<String> excludedBlocks = plugin.getConfig().getStringList("events.break.excluded-blocks");
        for (String pattern : excludedBlocks) {
            if (materialMatcher.matches(pattern, originalBlock.getType())) return;
        }

        int delaySeconds = plugin.getBreakDelay();

        for (String blockSyntax : regenBlocks) {

            // Parse the spec: "DIRT:10" → BlockSpec("DIRT", 10)
            MaterialMatcher.BlockSpec spec = materialMatcher.parse(blockSyntax, delaySeconds);
            if (spec == null) continue;

            // Check if block matches the pattern (exact or regex)
            if(!materialMatcher.matches(spec.materialPattern(), originalBlock.getType())) continue;

            int effectiveDelay = spec.delaySeconds();

            if (plugin.getConfig().getBoolean("events.break.deny-drops", true)) {
                event.setDropItems(false);
                event.setExpToDrop(0);
            }

            if(!plugin.getRegenerationManager().hasTaskAt(originalBlock.getLocation())) {
                plugin.getRegenerationManager().startTask(originalBlock, effectiveDelay);
            }

            break;
        }
    }
}
