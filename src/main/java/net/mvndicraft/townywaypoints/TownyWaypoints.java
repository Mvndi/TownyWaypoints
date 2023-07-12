package net.mvndicraft.townywaypoints;

import net.mvndicraft.townywaypoints.listeners.TownyListener;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class TownyWaypoints extends JavaPlugin
{
  private static JavaPlugin plugin;

  protected static final ConcurrentHashMap<String, Waypoint> waypoints = new ConcurrentHashMap<>();

  @Override
  public void onEnable()
  {
    PluginManager plugMan = Bukkit.getPluginManager();

    TownyListener townyListener = new TownyListener();

    plugMan.registerEvents(townyListener, plugin);

    getLogger().info("enabled!");
  }

  @Override
  public void onLoad()
  {
    plugin = this;

    loadWaypoints();
  }

  public static void loadWaypoints()
  {
    File waypointsDataFile = new File(plugin.getDataFolder(), "waypoints.yml");

    if (!waypointsDataFile.exists())
      plugin.saveResource("waypoints.yml", true);

    FileConfiguration waypointsData = YamlConfiguration.loadConfiguration(waypointsDataFile);
    Set<String> waypointsConfig = waypointsData.getKeys(false);
    waypointsConfig.forEach(waypointConfig -> {
      ConfigurationSection waypointConfigSection = waypointsData.getConfigurationSection(waypointConfig);
      if (waypointConfigSection == null)
        return;
      Waypoint waypoint = createWaypoint(waypointConfigSection);
      TownyListener.registerPlot(waypoint.getName(), waypoint.getMapKey(), waypoint.getCost());
      waypoints.put(waypoint.getName(), waypoint);
    });
  }

  @Override
  public void onDisable()
  {
    getLogger().info("disabled!");
  }

  private static Waypoint createWaypoint(ConfigurationSection config)
  {
    return new Waypoint(
      config.getString("name"),
      config.getString("mapKey"),
      config.getDouble("cost"),
      config.getInt("max"),
      config.getBoolean("sea"),
      config.getString("permission")
    );
  }

  public static JavaPlugin getPlugin()
  {
    return plugin;
  }

  public static ConcurrentHashMap<String, Waypoint> getWaypoints()
  {
    return waypoints;
  }
}
