package net.mvndicraft.townywaypoints.hook;

import com.palmergames.bukkit.towny.object.Town;
import net.mvndicraft.townyroads.Road;
import net.mvndicraft.townyroads.TownyRoadsPlugin;
import net.mvndicraft.townywaypoints.settings.TownyWaypointsSettings;
import org.bukkit.Bukkit;
import org.bukkit.Location;

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

    public static Road getRoad(Location location) {
        return TownyRoadsPlugin.getInstance().getRoadManager().getRoadAt(location);
    }

    public static boolean areConnected(Town town, Road road) {
        // getRoad returns null when the player isn't stood on one, and the road manager
        // doesn't check for that before calling into it
        if (road == null)
            return false;

        if (road.getTownsView().contains(town))
            return road.isValid() && !road.isBlocked();

        return TownyRoadsPlugin.getInstance().getRoadManager().areConnected(town, road);
    }
}
