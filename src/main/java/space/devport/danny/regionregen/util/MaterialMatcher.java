package space.devport.danny.regionregen.util;

import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class MaterialMatcher {

    private static final Logger LOGGER = Logger.getLogger("MaterialMatcher");

    private final Map<String, Pattern> regexCache = new HashMap<>();

    public record BlockSpec(String materialPattern, int delaySeconds) {}

    public BlockSpec parse(String blockSyntax, int defaultDelay) {
        String[] arr = blockSyntax.split(":");
        if (arr[0].isEmpty()) return null;

        String materialPattern = arr[0].toUpperCase();
        int delay = defaultDelay;

        if (arr.length > 1) {
            try {
                delay = Integer.parseInt(arr[1]);
            } catch (NumberFormatException e) {
                LOGGER.warning(arr[1] + " is not a number, using default.");
            }
        }

        return new BlockSpec(materialPattern, delay);
    }

    public boolean matches(String materialPattern, Material blockMaterial) {
        // Regex type
        if (materialPattern.startsWith("~")) {
            String regex = materialPattern.substring(1);
            try {
                Pattern pattern = regexCache.computeIfAbsent(regex, Pattern::compile);
                return pattern.matcher(blockMaterial.name()).matches();
            } catch (PatternSyntaxException e) {
                LOGGER.warning("{ " + regex + " } is an invalid regex pattern!");
                return false;
            }
        } else {
            // Exact type
            Material material = Material.getMaterial(materialPattern);
            return material != null && material == blockMaterial;
        }
    }
}
