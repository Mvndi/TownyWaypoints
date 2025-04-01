package net.mvndicraft.townywaypoints.util;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Translatable;
import com.palmergames.bukkit.towny.object.Translation;
import com.palmergames.bukkit.util.Colors;
import net.mvndicraft.townywaypoints.TownyWaypoints;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.Objects;

public class Messaging {
    final static String prefix = Translation.of("townywaypoints_plugin_prefix");

    public static void sendErrorMsg(CommandSender sender, Translatable message) {
        // Ensure the sender is not null (i.e. is an online player who is not an npc)
        if (sender != null)
            sender.sendMessage(prefix + Colors.Red + message.forLocale(sender));
    }

    public static void sendMsg(CommandSender sender, Translatable message) {
        // Ensure the sender is not null (i.e. is an online player who is not an npc)
        if (sender != null)
            sender.sendMessage(prefix + Colors.White + message.forLocale(sender));
    }

    public static void sendGlobalMessage(Translatable message) {
        TownyWaypoints.getInstance().getLogger().info(message.defaultLocale());
        Bukkit.getOnlinePlayers().stream()
                .filter(Objects::nonNull)
                .filter(p -> TownyAPI.getInstance().isTownyWorld(p.getLocation().getWorld()))
                .forEach(p -> sendMsg(p, message));
    }
}
