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

    public static boolean roadRestrictionsApply() {
        if (!TownyWaypointsSettings.getRoadRestrictionsBattleSessionOnly())
            return true;
        return isEnabled() && isBattleSessionActive();
    }
}
