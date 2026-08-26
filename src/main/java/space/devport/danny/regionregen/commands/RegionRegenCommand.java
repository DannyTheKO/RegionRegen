package space.devport.danny.regionregen.commands;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import space.devport.danny.regionregen.RegionRegenPlugin;
import space.devport.danny.regionregen.system.struct.RegenerationTask;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

public class RegionRegenCommand implements CommandExecutor, TabCompleter {

    private final RegionRegenPlugin plugin;
    private final MessageManager messageManager;

    public RegionRegenCommand(RegionRegenPlugin plugin) {
        this.plugin = plugin;
        this.messageManager = plugin.getMessageManager();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(messageManager.prefixed("&7Usage: &f/" + label + " reload: To reload config"));
            sender.sendMessage(messageManager.prefixed("&7Usage: &f/" + label + " reset <world_name>: To instantly regeneration block"));
            return true;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "reload":
                if (!sender.hasPermission("regionregen.reload")) {
                    sender.sendMessage(messageManager.prefixed("&cYou don't have permission to do this."));
                    return true;
                }
                plugin.reload(sender);
                return true;
            case "reset":
                if (!sender.hasPermission("regionregen.reset")) {
                    sender.sendMessage(messageManager.prefixed("&cYou don't have permission to do this."));
                    return true;
                }
                performReset(sender, args);
                return true;
            default:
                sender.sendMessage(messageManager.prefixed("&cUnknown sub-command."));
                return true;
        }
    }

    private void performReset(CommandSender sender, String[] args) {

        World world = null;
        if (args.length > 1) {
            world = Bukkit.getWorld(args[1]);
            if (world == null) {
                messageManager.sendMessage(
                        sender,
                        "invalid-world",
                        "&cWorld &f%param% &cis invalid.",
                        "%param%", args[1]);
                return;
            }
        }

        int count = 0;
        for (RegenerationTask task : new HashSet<>(plugin.getRegenerationManager().getTasks())) {
            if (task.getLocation().getWorld() == null) continue;
            if (world == null || task.getLocation().getWorld().getName().equalsIgnoreCase(world.getName())) {
                task.regenerate();
                count++;
            }
        }

        String message = world == null ? "reset-done" : "reset-done-world";
        String def = world == null ? "&7Reset &f%count% &7regeneration tasks." : "&7Reset &f%count% &7regeneration tasks in world &f%world%";
        messageManager.sendMessage(sender, message, def,
                "%world%", world == null ? "all worlds" : world.getName(),
                "%count%", String.valueOf(count));
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            for (String sub : List.of("reload", "reset"))
                if (sub.startsWith(args[0].toLowerCase(Locale.ROOT)))
                    completions.add(sub);
        } else if (args.length == 2 && args[0].equalsIgnoreCase("reset")) {
            for (World world : Bukkit.getWorlds())
                if (world.getName().toLowerCase(Locale.ROOT).startsWith(args[1].toLowerCase(Locale.ROOT)))
                    completions.add(world.getName());
        }

        return completions;
    }
}
