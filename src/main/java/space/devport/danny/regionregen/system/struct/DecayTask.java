package space.devport.danny.regionregen.system.struct;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import space.devport.danny.regionregen.RegionRegenPlugin;
import space.devport.danny.regionregen.util.LocationUtil;

public class DecayTask implements Runnable {

    @Getter
    private BukkitTask task;

    @Getter
    private final Location location;

    @Getter
    private final Material material;

    @Getter
    private final int delay;

    public DecayTask(Block block, int delay) {
        this.location = block.getLocation().clone();
        this.material = block.getType();
        this.delay = delay;
    }

    public void start() {
        if (task != null) return;
        task = Bukkit.getScheduler().runTaskLater(RegionRegenPlugin.getInstance(), this, delay * 20L);
    }

    public void decay() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        Bukkit.getScheduler().runTask(RegionRegenPlugin.getInstance(), () -> {
            if (location.getWorld() == null) {
                RegionRegenPlugin.getInstance().getDecayManager().removeTask(this);
                return;
            }
            Block block = location.getBlock();
            if (block.getType().isAir()) {
                RegionRegenPlugin.getInstance().getDecayManager().removeTask(this);
                return;
            }
            boolean denyDrops = RegionRegenPlugin.getInstance().getConfig().getBoolean("temp-build.deny-drops", true);
            Material before = block.getType();
            if (denyDrops) {
                block.setType(Material.AIR, false);
            } else {
                location.getWorld().dropItemNaturally(location.clone().add(0.5, 0.5, 0.5), new ItemStack(before));
                block.setType(Material.AIR, false);
            }
            RegionRegenPlugin.getInstance().debug("Decayed block " + before.name() + " at " + LocationUtil.locationToString(location));
            RegionRegenPlugin.getInstance().getDecayManager().removeTask(this);
        });
    }

    public void forceDecay() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        Bukkit.getScheduler().runTask(RegionRegenPlugin.getInstance(), () -> {
            if (location.getWorld() == null) {
                RegionRegenPlugin.getInstance().getDecayManager().removeTask(this);
                return;
            }
            Block block = location.getBlock();
            if (block.getType().isAir()) {
                RegionRegenPlugin.getInstance().getDecayManager().removeTask(this);
                return;
            }
            boolean denyDrops = RegionRegenPlugin.getInstance().getConfig().getBoolean("temp-build.deny-drops", true);
            Material before = block.getType();
            if (denyDrops) {
                block.setType(Material.AIR, false);
            } else {
                location.getWorld().dropItemNaturally(location.clone().add(0.5, 0.5, 0.5), new ItemStack(before));
                block.setType(Material.AIR, false);
            }
            RegionRegenPlugin.getInstance().debug("Force decayed block " + before.name() + " at " + LocationUtil.locationToString(location));
            RegionRegenPlugin.getInstance().getDecayManager().removeTask(this);
        });
    }

    @Override
    public void run() {
        decay();
    }
}
