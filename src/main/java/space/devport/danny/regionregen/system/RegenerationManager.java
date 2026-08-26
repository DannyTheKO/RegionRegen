package space.devport.danny.regionregen.system;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import space.devport.danny.regionregen.RegionRegenPlugin;
import space.devport.danny.regionregen.system.struct.RegenerationTask;
import space.devport.danny.regionregen.util.LocationUtil;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class RegenerationManager {

    private final RegionRegenPlugin plugin;

    private final Set<RegenerationTask> regenerationTaskList = ConcurrentHashMap.newKeySet();
    private final Set<Location> activeLocationList = ConcurrentHashMap.newKeySet();

    public RegenerationManager() {
        this.plugin = RegionRegenPlugin.getInstance();
    }

    public boolean hasTaskAt(Location location) {
        return activeLocationList.contains(location);
    }

    public void startTask(Block block, int delay) {
        List<String> excludedBlocks = plugin.getConfig().getStringList("events.break.excluded-blocks");
        Material material = block.getType();
        for (String excluded : excludedBlocks) {
            if (material.name().equalsIgnoreCase(excluded)) {
                plugin.debug("Block " + material.name() + " is excluded from regeneration.");
                return;
            }
        }

        RegenerationTask task = new RegenerationTask(block, delay);
        this.regenerationTaskList.add(task);
        this.activeLocationList.add(block.getLocation());
        task.start();
        plugin.debug("Started regeneration task on location " + LocationUtil.locationToString(block.getLocation()));
    }

    public void removeTask(RegenerationTask task) {
        this.regenerationTaskList.remove(task);
        this.activeLocationList.remove(task.getLocation());
    }

    public void clear() {
        for (RegenerationTask task : regenerationTaskList) {
            if (task.getLocation().getWorld() != null) {
                task.getLocation().getBlock().setBlockData(task.getBlockData(), false);
            }
        }
        regenerationTaskList.clear();
        activeLocationList.clear();
    }

    public Set<RegenerationTask> getTasks() {
        return Collections.unmodifiableSet(regenerationTaskList);
    }
}
