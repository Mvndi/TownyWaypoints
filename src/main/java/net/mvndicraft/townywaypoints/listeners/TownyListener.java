package net.mvndicraft.townywaypoints.listeners;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownySettings;
import com.palmergames.bukkit.towny.event.PlotPreChangeTypeEvent;
import com.palmergames.bukkit.towny.event.TownBlockTypeRegisterEvent;
import com.palmergames.bukkit.towny.event.TranslationLoadEvent;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.TownBlockData;
import com.palmergames.bukkit.towny.object.TownBlockType;
import com.palmergames.bukkit.towny.object.TownBlockTypeHandler;
import com.palmergames.bukkit.towny.object.Translatable;
import com.palmergames.bukkit.towny.object.TranslationLoader;
import fr.formiko.mc.biomeutils.NMSBiomeUtils;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;
import net.mvndicraft.townywaypoints.TownyWaypoints;
import net.mvndicraft.townywaypoints.VehicleTravelWarmup;
import net.mvndicraft.townywaypoints.Waypoint;
import net.mvndicraft.townywaypoints.hook.TownyRoadsHook;
import net.mvndicraft.townywaypoints.util.Messaging;
import net.mvndicraft.townywaypoints.util.TownBlockMetaDataController;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.plugin.Plugin;

public final class TownyListener implements Listener {
    private final static TownyWaypoints instance = TownyWaypoints.getInstance();

    public static void registerPlot(String name, String mapKey, double cost) {
        if (TownBlockTypeHandler.exists(name))
            return;

        TownBlockType townBlockType = new TownBlockType(name, new TownBlockData() {
            @Override
            public String getMapKey() {
                return mapKey;
            }

            @Override
            public double getCost() {
                return cost;
            }
        });

        try {
            instance.getLogger().info("registering new plot type " + name);
            TownBlockTypeHandler.registerType(townBlockType);

        } catch (TownyException e) {
            Bukkit.getLogger().severe(e.getMessage());
        }
    }

    private static boolean biomeAllowed(Location loc, Waypoint waypoint) {
        if (!waypoint.getAllowedBiomeTags().isEmpty())
            for (String tag : waypoint.getAllowedBiomeTags()) {
                if (NMSBiomeUtils.matchTag(loc, tag))
                    return true;
            }
        if (waypoint.getAllowedBiomes().isEmpty())
            return waypoint.getAllowedBiomeTags().isEmpty();

        return waypoint.getAllowedBiomes().contains(loc.getBlock().getBiome().toString());
    }

    @EventHandler
    public void onTownyLoadTownBlockTypes(TownBlockTypeRegisterEvent event) {
        TownyWaypoints.loadWaypoints();
    }

    private int getPlotTypeCount(Town town, String name) {
        int count = 0;

        for (TownBlock townBlock : town.getTownBlocks()) {
            if (townBlock.getType().getName().equalsIgnoreCase(name))
                count++;
        }

        return count;
    }

    @EventHandler
    public void onPlotPreChangeTypeEvent(PlotPreChangeTypeEvent event) throws NotRegisteredException {
        TownBlock townBlock = event.getTownBlock();
        String plotTypeName = event.getNewType().getName();

        if (!TownyWaypoints.getWaypoints().containsKey(plotTypeName))
            return;
        Waypoint waypoint = TownyWaypoints.getWaypoints().get(plotTypeName);

        World world = townBlock.getWorld().getBukkitWorld();
        if (world == null)
            return;
        Player player = event.getResident().getPlayer();
        if (player == null)
            return;

        if (!waypoint.getPermission().isEmpty() && !player.hasPermission(waypoint.getPermission())) {
            event.setCancelMessage(Translatable
                    .of("msg_err_waypoint_create_insufficient_permission", waypoint.getName()).defaultLocale());
            event.setCancelled(true);
            return;
        }

        if (TownyWaypoints.getEconomy().balance("TownyWaypoints", player.getUniqueId()).doubleValue()
                - waypoint.getCost() <= 0) {
            event.setCancelMessage(Translatable
                    .of("msg_err_waypoint_create_insufficient_funds", waypoint.getName(), waypoint.getCost())
                    .defaultLocale());
            event.setCancelled(true);
            return;
        }

        Location loc = player.getLocation();

        if (!biomeAllowed(loc, waypoint)) {
            event.setCancelMessage(
                    Translatable.of("msg_err_biome_not_allowed", loc.getBlock().getBiome().toString()).defaultLocale());
            event.setCancelled(true);
            return;
        }

        int max = waypoint.getMax();

        if (getPlotTypeCount(townBlock.getTown(), plotTypeName) >= max) {
            event.setCancelMessage(Translatable.of("msg_err_max_plots", max).defaultLocale());
            event.setCancelled(true);
            return;
        }

        TownBlockMetaDataController.setSpawn(townBlock, player.getLocation());
        Messaging.sendMsg(player, Translatable.of("msg_spawn_set", loc.toString()));
        String status = Translatable.of("open_status_nonenemies").defaultLocale();
        TownBlockMetaDataController.setSdf(townBlock, TownBlockMetaDataController.statusKey, status);
        Messaging.sendMsg(player, Translatable.of("msg_status_set", status));
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        Runnable callback = TownyWaypoints.takePendingCooldownCallback(uuid);
        if (callback != null)
            callback.run();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!TownySettings.isMovementCancellingSpawnWarmup())
            return;

        Player player = event.getPlayer();
        if (!VehicleTravelWarmup.hasPending(player.getUniqueId()))
            return;
        if (player.isInsideVehicle())
            return;

        Location to = event.getTo();
        Location from = event.getFrom();
        if (to == null)
            return;
        if (from.getBlockX() == to.getBlockX() && from.getBlockY() == to.getBlockY()
                && from.getBlockZ() == to.getBlockZ())
            return;

        VehicleTravelWarmup.cancel(player.getUniqueId());
        Messaging.sendErrorMsg(player, Translatable.of("msg_err_teleport_cancelled"));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!TownySettings.isDamageCancellingSpawnWarmup())
            return;
        if (!(event.getEntity() instanceof Player player))
            return;
        if (!VehicleTravelWarmup.hasPending(player.getUniqueId()))
            return;

        VehicleTravelWarmup.cancel(player.getUniqueId());
        Messaging.sendErrorMsg(player, Translatable.of("msg_err_teleport_cancelled_damage"));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        TownyWaypoints.takePendingCooldownCallback(uuid);
        VehicleTravelWarmup.cancel(uuid);
    }

    // @EventHandler(ignoreCancelled = true)
    // public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
    //     String[] args = event.getMessage().substring(1).split("\\s+");
    //     if (args.length < 2)
    //         return;
    //     String base = args[0].toLowerCase();
    //     if (!base.equals("t") && !base.equals("town"))
    //         return;
    //     if (!args[1].equalsIgnoreCase("spawn"))
    //         return;

    //     Player player = event.getPlayer();
    //     if (player.hasPermission(TownyWaypoints.ADMIN_PERMISSION))
    //         return;

    //     Resident resident = TownyAPI.getInstance().getResident(player);
    //     if (resident == null || !resident.hasTown())
    //         return;

    //     Town playerTown;
    //     try {
    //         playerTown = resident.getTown();
    //     } catch (NotRegisteredException e) {
    //         return;
    //     }

    //     TownBlock currentBlock = TownyAPI.getInstance().getTownBlock(player);
    //     if (currentBlock == null) {
    //         event.setCancelled(true);
    //         Messaging.sendErrorMsg(player, Translatable.of("msg_err_town_spawn_not_in_connected_town"));
    //         return;
    //     }

    //     Town currentTown = currentBlock.getTownOrNull();
    //     if (currentTown == null) {
    //         event.setCancelled(true);
    //         Messaging.sendErrorMsg(player, Translatable.of("msg_err_town_spawn_not_in_connected_town"));
    //         return;
    //     }

    //     if (currentTown.equals(playerTown))
    //         return;

    //     if (TownyRoadsHook.isEnabled() && TownyRoadsHook.areConnected(playerTown, currentTown))
    //         return;

    //     event.setCancelled(true);
    //     Messaging.sendErrorMsg(player, Translatable.of("msg_err_town_spawn_not_in_connected_town"));
    // }

    @EventHandler(ignoreCancelled = true)
    public void onTownyLoadLanguages(TranslationLoadEvent event) {
        Plugin plugin = TownyWaypoints.getInstance();
        Path langFolderPath = Paths.get(plugin.getDataFolder().getPath()).resolve("lang");
        TranslationLoader loader = new TranslationLoader(langFolderPath, plugin, TownyWaypoints.class);
        loader.load();
        Map<String, Map<String, String>> translations = loader.getTranslations();

        for (String language : translations.keySet())
            for (Map.Entry<String, String> map : translations.get(language).entrySet())
                event.addTranslation(language, map.getKey(), map.getValue());
    }
}
