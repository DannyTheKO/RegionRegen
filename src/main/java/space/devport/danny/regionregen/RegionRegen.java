package space.devport.danny.regionregen;

import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.FlagContext;
import com.sk89q.worldguard.protection.flags.RegionGroup;
import org.jetbrains.annotations.Nullable;

public class RegionRegen extends Flag<String> {

    protected RegionRegen(String name) {
        super(name);
    }

    protected RegionRegen(String name, @Nullable RegionGroup defaultGroup) {
        super(name, defaultGroup);
    }

    @Override
    public String parseInput(FlagContext context) {
        String input = context.getUserInput().trim();
        return input.toUpperCase();
    }

    @Override
    public String unmarshal(@Nullable Object o) {
        if (o instanceof String) {
            return (String) o;
        } else {
            return null;
        }
    }

    @Override
    public Object marshal(String o) {
        return o;
    }
}
