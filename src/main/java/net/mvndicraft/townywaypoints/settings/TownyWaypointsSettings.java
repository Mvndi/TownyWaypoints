package net.mvndicraft.townywaypoints.settings;

public class TownyWaypointsSettings {
    public static double getSplit() {
        return Settings.getDouble(ConfigNodes.WAYPOINTS_ECONOMY_SPLIT);
    }

    public static int getMaxDistance() {
        return Settings.getInt(ConfigNodes.WAYPOINTS_RESTRICTIONS_MAX_DISTANCE);
    }

    public static int getCooldown() {
        return Settings.getInt(ConfigNodes.WAYPOINTS_RESTRICTIONS_COOLDOWN);
    }

    public static boolean getPeerToPeer() {
        return Settings.getBoolean(ConfigNodes.WAYPOINTS_RESTRICTIONS_PEER_TO_PEER);
    }
}
