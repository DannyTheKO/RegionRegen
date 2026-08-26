package space.devport.danny.regionregen.listeners;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import lombok.RequiredArgsConstructor;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import space.devport.danny.regionregen.RegionRegenPlugin;

@RequiredArgsConstructor
public class PlaceListener implements Listener {

    private final RegionRegenPlugin plugin;

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();

        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        ApplicableRegionSet regions = query.getApplicableRegions(BukkitAdapter.adapt(block.getLocation()));

        StateFlag.State state = regions.queryValue(WorldGuardPlugin.inst().wrapPlayer(event.getPlayer()), RegionRegenPlugin.WG_TEMP_BUILD_FLAG);
        if (state != StateFlag.State.ALLOW) return;

        if (plugin.getDecayManager().hasTaskAt(block.getLocation())) return;

        int delay = plugin.getConfig().getInt("temp-build.decay-time", 30);
        plugin.getDecayManager().startDecay(block, delay);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (plugin.getDecayManager().hasTaskAt(block.getLocation())) {
            plugin.getDecayManager().cancelAt(block.getLocation());
        }
    }
}
