package space.devport.danny.regionregen.system;

import org.bukkit.Location;
import org.bukkit.block.Block;
import space.devport.danny.regionregen.RegionRegenPlugin;
import space.devport.danny.regionregen.system.struct.DecayTask;
import space.devport.danny.regionregen.util.LocationUtil;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class DecayManager {

    private final RegionRegenPlugin plugin;

    private final Set<DecayTask> taskList = ConcurrentHashMap.newKeySet();
    private final Set<Location> activeLocationList = ConcurrentHashMap.newKeySet();

    public DecayManager() {
        this.plugin = RegionRegenPlugin.getInstance();
    }

    public boolean hasTaskAt(Location location) {
        return activeLocationList.contains(location);
    }

    public void startDecay(Block block, int delay) {
        Location loc = block.getLocation().clone();
        if (hasTaskAt(loc)) return;
        DecayTask task = new DecayTask(block, delay);
        taskList.add(task);
        activeLocationList.add(task.getLocation());
        task.start();
        plugin.debug("Started decay task for " + block.getType().name() + " at " + LocationUtil.locationToString(loc) + " delay=" + delay + "s");
    }

    public void removeTask(DecayTask task) {
        taskList.remove(task);
        activeLocationList.remove(task.getLocation());
    }

    public void cancelAt(Location location) {
        for (DecayTask task : taskList) {
            if (task.getLocation().equals(location)) {
                if (task.getTask() != null) task.getTask().cancel();
                removeTask(task);
                plugin.debug("Cancelled decay task at " + LocationUtil.locationToString(location));
                break;
            }
        }
    }

    public void clear() {
        for (DecayTask task : taskList) {
            if (task.getTask() != null) task.getTask().cancel();
        }
        taskList.clear();
        activeLocationList.clear();
    }

    public Set<DecayTask> getTasks() {
        return Collections.unmodifiableSet(taskList);
    }
}
