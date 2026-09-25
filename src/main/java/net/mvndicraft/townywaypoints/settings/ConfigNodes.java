package net.mvndicraft.townywaypoints.settings;

public enum ConfigNodes {
    VERSION(
            "version",
            "",
            "# This is the current version. Please do not edit."),
    DEBUG(
            "debug",
            "false",
            ""),
    LANGUAGE("language",
            "en_US.yml",
            "# The language file you wish to use."),
    TOWN_SPAWN_REQUIRE_ROAD_CONNECTION(
            "town_spawn.require_road_connection",
            "true"),
    ROAD_RESTRICTIONS_BATTLE_SESSION_ONLY(
            "road_restrictions.battle_session_only",
            "true",
            "",
            "# If true, road requirements for /twp and /t spawn only apply during SiegeWar battle sessions.",
            "# If false, they apply at all times."),
    WAYPOINTS("waypoints", "", ""),
    WAYPOINTS_TOWNY_ROADS_ENABLED(
            "waypoints.towny_roads.enabled",
            "false",
            "",
            "# If true a road will be required between towns used for waypoints.",
            "# Disabled with value of false"),
    WAYPOINTS_ECONOMY(
            "waypoints.economy",
            "",
            "",
            "############################################################",
            "# +------------------------------------------------------+ #",
            "# |                       Economy                        | #",
            "# +------------------------------------------------------+ #",
            "############################################################",
            ""),
    WAYPOINTS_ECONOMY_SPLIT(
            "waypoints.economy.split",
            "0.5",
            "",
            "# The percentage of the travel cost that gets added to the waypoints town bank, the rest goes to the nation. If it has no nation then 100% goes to the town bank.",
            "# Disabled with value of -1"),
    WAYPOINTS_RESTRICTIONS(
            "waypoints.restrictions",
            "",
            "",
            "############################################################",
            "# +------------------------------------------------------+ #",
            "# |                   Restrictions                       | #",
            "# +------------------------------------------------------+ #",
            "############################################################",
            ""),
    WAYPOINTS_RESTRICTIONS_MAX_DISTANCE(
            "waypoints.restrictions.max_distance",
            "2700",
            "",
            "# The maximum number of blocks a player can travel between waypoints.",
            "# Disabled with value of -1"),
    WAYPOINTS_RESTRICTIONS_COOLDOWN(
            "waypoints.restrictions.cooldown",
            "300",
            "",
            "# The amount of seconds a player must wait between waypoint travels."),
    WAYPOINTS_RESTRICTIONS_STABLE_COOLDOWN(
            "waypoints.restrictions.stable_cooldown",
            "300",
            "",
            "# Base cooldown in seconds for stable waypoint teleports. Reduced by stable_cooldown_road_reduction% per road the destination town has.",
            "# Disabled with value of -1"),
    WAYPOINTS_RESTRICTIONS_STABLE_COOLDOWN_ROAD_REDUCTION(
            "waypoints.restrictions.stable_cooldown_road_reduction",
            "15",
            "",
            "# Percentage of the stable cooldown removed per road connection the destination town has."),
    WAYPOINTS_RESTRICTIONS_STABLE_COOLDOWN_MIN_PERCENT(
            "waypoints.restrictions.stable_cooldown_min_percent",
            "30",
            "",
            "# The minimum cooldown as a percentage of the base stable cooldown. Prevents roads from reducing it to zero."),
    WAYPOINTS_RESTRICTIONS_PEER_TO_PEER(
            "waypoints.restrictions.peer_to_peer",
            "true",
            "",
            "# If true players can only teleport from one waypoint type to another.");

    private final String Root;
    private final String Default;
    private final String[] comments;

    ConfigNodes(String root, String def, String... comments) {

        this.Root = root;
        this.Default = def;
        this.comments = comments;
    }

    /**
     * Retrieves the root for a config option
     *
     * @return The root for a config option
     */
    public String getRoot() {

        return Root;
    }

    /**
     * Retrieves the default value for a config path
     *
     * @return The default value for a config path
     */
    public String getDefault() {

        return Default;
    }

    /**
     * Retrieves the comment for a config path
     *
     * @return The comments for a config path
     */
    public String[] getComments() {
        if (comments != null) {
            return comments;
        }

        String[] comments = new String[1];
        comments[0] = "";
        return comments;
    }
}
