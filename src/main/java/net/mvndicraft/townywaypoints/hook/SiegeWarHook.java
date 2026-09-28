package net.mvndicraft.townywaypoints.hook;

import com.gmail.goosius.siegewar.SiegeWarAPI;
import net.mvndicraft.townywaypoints.settings.TownyWaypointsSettings;
import org.bukkit.Bukkit;

public final class SiegeWarHook {
    private SiegeWarHook() {}

    public static boolean isEnabled() {
        return Bukkit.getPluginManager().isPluginEnabled("SiegeWar");
    }

    public static boolean isBattleSessionActive() {
        return SiegeWarAPI.isBattleSessionActive();
    }

    public static boolean townSpawnRoadRestrictionsApply() {
        return roadRestrictionsApply(TownyWaypointsSettings.getTownSpawnBattleSessionOnly());
    }

    public static boolean waypointRoadRestrictionsApply() {
        return roadRestrictionsApply(TownyWaypointsSettings.getWaypointsRoadsBattleSessionOnly());
    }

    private static boolean roadRestrictionsApply(boolean battleSessionOnly) {
        if (!battleSessionOnly)
            return true;
        return isEnabled() && isBattleSessionActive();
    }
}
