package space.devport.danny.regionregen.util;

import org.bukkit.Location;

public final class LocationUtil {

    private LocationUtil() {
    }

    public static String locationToString(Location location) {
        return String.format("%s, %.2f, %.2f, %.2f",
                location.getWorld() == null ? "null" : location.getWorld().getName(),
                location.getX(), location.getY(), location.getZ());
    }
}
