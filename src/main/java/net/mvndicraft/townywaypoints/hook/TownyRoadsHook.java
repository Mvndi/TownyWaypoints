package net.mvndicraft.townywaypoints.hook;

import com.palmergames.bukkit.towny.object.Town;
import net.mvndicraft.townyroads.TownyRoadsPlugin;
import net.mvndicraft.townywaypoints.settings.TownyWaypointsSettings;
import org.bukkit.Bukkit;

public final class TownyRoadsHook {
    private TownyRoadsHook() {}

    public static boolean isEnabled() {
        return Bukkit.getPluginManager().isPluginEnabled("TownyRoads") && TownyWaypointsSettings.getTownyRoadEnabled();
    }

    public static boolean areConnected(Town town1, Town town2) {
        return TownyRoadsPlugin.getInstance().getRoadManager().areConnected(town1, town2);
    }

    public static int getRoadCount(Town town) {
        return TownyRoadsPlugin.getInstance().getRoadManager().getRoadsByTown(town).size();
    }
}
