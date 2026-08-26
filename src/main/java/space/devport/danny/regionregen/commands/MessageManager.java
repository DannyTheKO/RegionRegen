package space.devport.danny.regionregen.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import space.devport.danny.regionregen.RegionRegenPlugin;

public class MessageManager {

    private final RegionRegenPlugin plugin;

    public MessageManager(RegionRegenPlugin plugin) {
        this.plugin = plugin;
    }

    public String getMessage(String path, String def) {
        return plugin.getConfig().getString("messages." + path, def);
    }

    public String prefixed(String message) {
        String prefix = plugin.getConfig().getString("plugin-prefix", "");
        return ChatColor.translateAlternateColorCodes('&', prefix + message);
    }

    public void sendMessage(CommandSender sender, String path, String def, String... replacements) {
        String message = getMessage(path, def);

        for (int i = 0; i < replacements.length - 1; i += 2)
            message = message.replace(replacements[i], replacements[i + 1]);
        sender.sendMessage(prefixed(message));
    }

    public String stripped(String message) {
        String prefix = plugin.getConfig().getString("plugin-prefix", "");
        return ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', prefix + message));
    }
}
