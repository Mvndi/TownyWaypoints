package net.mvndicraft.townywaypoints;

import co.aikar.commands.PaperCommandManager;
import com.github.Anon8281.universalScheduler.UniversalScheduler;
import com.github.Anon8281.universalScheduler.scheduling.schedulers.TaskScheduler;
import com.google.common.collect.ImmutableList;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.TownBlockTypeCache;
import com.palmergames.bukkit.towny.object.TownBlockTypeHandler;
import com.palmergames.bukkit.towny.object.Translatable;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;
import net.milkbowl.vault2.economy.Economy;
import net.mvndicraft.townywaypoints.commands.TownyWaypointsCommand;
import net.mvndicraft.townywaypoints.listeners.TownyListener;
import net.mvndicraft.townywaypoints.settings.Settings;
import net.mvndicraft.townywaypoints.util.LocationUtil;
import net.mvndicraft.townywaypoints.util.TownBlockMetaDataController;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class TownyWaypoints extends JavaPlugin {
    public static final String ADMIN_PERMISSION = "townywaypoints.admin";
    protected static final ConcurrentHashMap<String, Waypoint> waypoints = new ConcurrentHashMap<>();
    private static TownyWaypoints instance;
    private static Economy economy;
    private static TaskScheduler scheduler;
    private static final String BIOME_TAGS_KEY = "allowed_biome_tags";
    private static final String BIOME_KEY = "allowed_biomes";
    private List<String> open_statuses;

    public static TownyWaypoints getInstance() {
        return instance;
    }

    public static Economy getEconomy() {
        return economy;
    }

    public static TaskScheduler getScheduler() {
        return scheduler;
    }

    public static ConcurrentHashMap<String, Waypoint> getWaypoints() {
        return waypoints;
    }

    public static void loadWaypoints() {
        File waypointsDataFile = new File(instance.getDataFolder(), "waypoints.yml");

        if (!waypointsDataFile.exists())
            instance.saveResource("waypoints.yml", true);

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

    private static Waypoint createWaypoint(ConfigurationSection config) {
        return new Waypoint(config.getString("name"), config.getString("mapKey"), config.getDouble("cost"),
                config.getDouble("travel_cost"), config.getInt("max"), config.getBoolean("sea"),
                config.getBoolean("travel_with_vehicle"), config.getString("permission"), config.getInt("max_distance"),
                config.contains(instance.BIOME_TAGS_KEY) ? config.getStringList(instance.BIOME_TAGS_KEY)
                        : new ArrayList<>(),
                config.contains(instance.BIOME_KEY) ? config.getStringList(instance.BIOME_KEY) : new ArrayList<>());
    }

    @Override
    public void onEnable() {
        PluginManager plugMan = Bukkit.getPluginManager();

        if (!setupEconomy()) {
            getLogger().severe("Disabled due to no Vault dependency found!");
            plugMan.disablePlugin(this);
            return;
        }

        Settings.loadConfigAndLang();

        PaperCommandManager manager = new PaperCommandManager(instance);
        manager.registerCommand(new TownyWaypointsCommand());
        manager.getCommandCompletions().registerAsyncCompletion("reachable_waypointed_towns", c -> {
            Player player = c.getContextValue(Player.class, 0);
            ArrayList<String> towns = new ArrayList<>();
            TownyAPI.getInstance().getTowns().forEach(town -> getWaypoints().keySet().forEach(waypoint -> {
                if (town.getTownBlockTypeCache().getNumTownBlocks(TownBlockTypeHandler.getType(waypoint),
                        TownBlockTypeCache.CacheType.ALL) > 0
                        && TownBlockMetaDataController.numWaypointsWithAccess(town, player, waypoint) > 0
                        && LocationUtil.isCloseEnough(player, getWaypoints().get(waypoint), town)) {
                    towns.add(town.getName());
                }
            }));
            return towns;
        });
        manager.getCommandCompletions().registerAsyncCompletion("town_waypoints", c -> {
            Player player = c.getContextValue(Player.class, 0);
            Town town = TownyAPI.getInstance().getTown(c.getContextValue(String.class, 1));
            ArrayList<String> waypoints = new ArrayList<>();

            if (town == null)
                return waypoints;

            getWaypoints().keySet().forEach(waypoint -> {
                if (town.getTownBlockTypeCache().getNumTownBlocks(TownBlockTypeHandler.getType(waypoint),
                        TownBlockTypeCache.CacheType.ALL) > 0
                        && TownBlockMetaDataController.numWaypointsWithAccess(town, player, waypoint) > 0) {
                    waypoints.add(waypoint);
                }
            });
            return waypoints;
        });
        manager.getCommandCompletions().registerAsyncCompletion("waypoints_pages", c -> {
            Player player = c.getContextValue(Player.class, 0);
            Location location = player.getLocation();
            String waypointName = c.getContextValue(String.class, 1);
            int possibilities = (int) TownyAPI.getInstance().getTownBlocks().stream()
                    .filter(tb -> tb.getType().getName().equals(waypointName)).filter(TownBlock::hasTown).filter(tb -> TownBlockMetaDataController.getSpawn(tb).getWorld() != null)
                    .count();
            int maxPage = Math.floorDiv(possibilities, 10);
            return IntStream.rangeClosed(1, maxPage).mapToObj(String::valueOf).toList();
        });
        manager.getCommandCompletions().registerAsyncCompletion("waypoints", c -> getWaypoints().keySet());
        manager.getCommandCompletions().registerAsyncCompletion("waypoint_plot_names", c -> {
            Player player = c.getContextValue(Player.class, 0);
            Town town = TownyAPI.getInstance().getTown(c.getContextValue(String.class, 1));

            ArrayList<String> plots = new ArrayList<>();

            if (town == null)
                return plots;

            town.getTownBlocks().forEach(townBlock -> {
                if (townBlock.getType().getName().equals(c.getContextValue(String.class, 2))
                        && TownBlockMetaDataController.hasAccess(townBlock, player)) {
                    if (townBlock.getName().equals("")) {
                        plots.add(Translatable.of("townywaypoints_plot_unnamed").defaultLocale());
                    } else {
                        plots.add(townBlock.getName());
                    }
                }
            });

            return plots;
        });
        open_statuses = ImmutableList.of(Translatable.of("open_status_all").defaultLocale(),
                Translatable.of("open_status_allies").defaultLocale(),
                Translatable.of("open_status_nation").defaultLocale(),
                Translatable.of("open_status_town").defaultLocale(),
                Translatable.of("open_status_none").defaultLocale());
        manager.getCommandCompletions().registerAsyncCompletion("open_statuses", c -> open_statuses);

        TownyListener townyListener = new TownyListener();
        plugMan.registerEvents(townyListener, instance);

        getLogger().info("enabled!");
    }

    @Override
    public void onLoad() {
        instance = this;
        scheduler = UniversalScheduler.getScheduler(instance);
        loadWaypoints();
    }

    @Override
    public void onDisable() {
        getLogger().info("disabled!");
    }

    private boolean setupEconomy() {
        if (this.getServer().getPluginManager().getPlugin("Vault") != null) {
            RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
            if (rsp != null)
                economy = rsp.getProvider();
            return true;
        } else {
            getLogger().severe("Vault not found. Please download Vault to use TownyWaypoints.");
            return false;
        }
    }

    public String getVersion() {
        return instance.getPluginMeta().getVersion();
    }

    public List<String> getOpenStatuses() {
        return open_statuses;
    }
}
