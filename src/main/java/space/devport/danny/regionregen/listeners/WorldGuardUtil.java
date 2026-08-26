package space.devport.danny.regionregen.listeners;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.Location;
import space.devport.danny.regionregen.RegionRegenPlugin;

import java.util.Set;

public class WorldGuardUtil {

    public static Set<String> getRegenBlocks(Location location) {
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();
        ApplicableRegionSet regions = query.getApplicableRegions(BukkitAdapter.adapt(location));
        return regions.queryValue(null, RegionRegenPlugin.WG_BLOCK_REGEN_FLAG);
    }
}
