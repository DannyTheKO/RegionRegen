package space.devport.danny.regionregen.system.struct;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.scheduler.BukkitTask;
import space.devport.danny.regionregen.RegionRegenPlugin;
import space.devport.danny.regionregen.util.LocationUtil;

import java.util.Collection;

public class RegenerationTask implements Runnable {

    @Getter
    private BukkitTask task;

    @Getter
    private final Location location;

    @Getter
    private final BlockData blockData;

    @Getter
    private final int delay;

    public RegenerationTask(Block block, int delay) {
        this.location = block.getLocation();
        this.blockData = block.getBlockData();
        this.delay = delay;
    }

    public void start() {
        if (task != null) return;
        task = Bukkit
                .getScheduler()
                .runTaskLater(
                        RegionRegenPlugin.getInstance(),
                        this,
                        delay * 20L
                );
    }

    public void regenerate() {

        if (task != null) {
            task.cancel();
            task = null;
        }

        Bukkit.getScheduler().runTask(RegionRegenPlugin.getInstance(), () -> {
            // Guard for null world
            if (location.getWorld() == null) {
                RegionRegenPlugin.getInstance().getRegenerationManager().removeTask(this);
                return;
            }

            if (RegionRegenPlugin.getInstance().getConfig().getBoolean("obstruct-prevention.enabled", false)) {
                double range = RegionRegenPlugin.getInstance().getConfig().getDouble("obstruct-prevention.radius", 1);
                Collection<Entity> nearbyEntities = location.getWorld().getNearbyEntities(location, range, range, range);

                if (nearbyEntities.stream().anyMatch(e -> e.getType() == EntityType.PLAYER)) {
                    Bukkit.getScheduler().runTaskLater(RegionRegenPlugin.getInstance(), this::regenerate, 20L);
                    RegionRegenPlugin.getInstance().debug("Someone is in the way, trying again.");
                    return;
                }
            }

            location.getBlock().setBlockData(blockData, false);
            RegionRegenPlugin.getInstance()
                    .debug("Regenerated block on location " + LocationUtil.locationToString(location));

            RegionRegenPlugin.getInstance().getRegenerationManager().removeTask(this);
        });
    }

    public void forceRegenerate() {

        if (task != null) {
            task.cancel();
            task = null;
        }

        Bukkit.getScheduler().runTask(RegionRegenPlugin.getInstance(), () -> {
            location.getBlock().setBlockData(blockData, false);
            RegionRegenPlugin.getInstance()
                    .debug("Forces regenerate block on location" + LocationUtil.locationToString(location));

            RegionRegenPlugin.getInstance().getRegenerationManager().removeTask(this);
        });
    }

    @Override
    public void run() {
        regenerate();
    }
}
