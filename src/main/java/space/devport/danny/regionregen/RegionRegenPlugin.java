package space.devport.danny.regionregen;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.SetFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import lombok.Getter;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import space.devport.danny.regionregen.commands.MessageManager;
import space.devport.danny.regionregen.commands.RegionRegenCommand;
import space.devport.danny.regionregen.listeners.*;
import space.devport.danny.regionregen.system.DecayManager;
import space.devport.danny.regionregen.system.RegenerationManager;
import space.devport.danny.regionregen.util.MaterialMatcher;

public class RegionRegenPlugin extends JavaPlugin {

    private static final String BLOCK_REGEN_FLAG_NAME = "rr-block-regen";
    private static final String TEMP_BUILD_FLAG_NAME = "rr-temp-build";

    public static SetFlag<String> WG_BLOCK_REGEN_FLAG;
    public static StateFlag WG_TEMP_BUILD_FLAG;

    @Getter
    private static RegionRegenPlugin instance;

    @Getter
    private RegenerationManager regenerationManager;

    @Getter
    private DecayManager decayManager;

    @Getter
    private MaterialMatcher materialMatcher;

    @Getter
    private MessageManager messageManager;

    @Getter
    private boolean running = false;

    @Override
    public void onLoad() {

        if (getServer().getPluginManager().getPlugin("WorldGuard") == null) {
            err("Could not load, WorldGuard is not installed.");
            return;
        }

        running = registerFlag();
    }

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        regenerationManager = new RegenerationManager();
        decayManager = new DecayManager();
        materialMatcher = new MaterialMatcher();
        messageManager = new MessageManager(this);

        getServer().getPluginManager().registerEvents(new BreakListener(this, materialMatcher), this);
        getServer().getPluginManager().registerEvents(new PlaceListener(this), this);
        getServer().getPluginManager().registerEvents(new ExplosionListener(this), this);
        getServer().getPluginManager().registerEvents(new PhysicsListener(this), this);
        getServer().getPluginManager().registerEvents(new FireListener(this), this);
        getServer().getPluginManager().registerEvents(new LavaListener(this), this);
        getServer().getPluginManager().registerEvents(new PistonListener(this), this);

        RegionRegenCommand cmd = new RegionRegenCommand(this);
        getCommand("regionregen").setExecutor(cmd);
        getCommand("regionregen").setTabCompleter(cmd);
    }

    @Override
    public void onDisable() {
        if (regenerationManager != null)
            regenerationManager.clear();
        if (decayManager != null)
            decayManager.clear();
    }

    public void reload(CommandSender sender) {
        reloadConfig();

        sender.sendMessage(messageManager.prefixed("&7Reloaded."));
        if (!running)
            registerFlag();
    }

    private boolean registerFlag() {
        FlagRegistry WGRegistry = WorldGuard.getInstance().getFlagRegistry();

        try {
            SetFlag<String> blockRegenFlag = new SetFlag<>(BLOCK_REGEN_FLAG_NAME, new RegionRegen("RegionRegen"));
            WGRegistry.register(blockRegenFlag);
            WG_BLOCK_REGEN_FLAG = blockRegenFlag;

            StateFlag tempBuildFlag = new StateFlag(TEMP_BUILD_FLAG_NAME, false);
            WGRegistry.register(tempBuildFlag);
            WG_TEMP_BUILD_FLAG = tempBuildFlag;

        } catch (FlagConflictException e) {
            Flag<?> blockRegenFlag_existing = WGRegistry.get(BLOCK_REGEN_FLAG_NAME);
            if (blockRegenFlag_existing instanceof SetFlag) {
                WG_BLOCK_REGEN_FLAG = (SetFlag<String>) blockRegenFlag_existing;
            } else {
                err("Some other plugin registered the flag " + BLOCK_REGEN_FLAG_NAME + "... can't work. Disabling.");
                getServer().getPluginManager().disablePlugin(this);
                return false;
            }
            Flag<?> tempBuildFlag_existing = WGRegistry.get(TEMP_BUILD_FLAG_NAME);
            if (tempBuildFlag_existing instanceof StateFlag) {
                WG_TEMP_BUILD_FLAG = (StateFlag) tempBuildFlag_existing;
            } else if (tempBuildFlag_existing != null) {
                err("Some other plugin registered the flag " + TEMP_BUILD_FLAG_NAME + "... can't work. Disabling.");
                getServer().getPluginManager().disablePlugin(this);
                return false;
            }
        }
        return true;
    }

    public void debug(String message) {
        if (!getConfig().getBoolean("debug-enabled", false)) return;
        getLogger().info(messageManager.stripped(message));
    }

    public void warn(String message) {
        getLogger().warning(messageManager.stripped(message));
    }

    public void err(String message) {
        getLogger().severe(messageManager.stripped(message));
    }
}
