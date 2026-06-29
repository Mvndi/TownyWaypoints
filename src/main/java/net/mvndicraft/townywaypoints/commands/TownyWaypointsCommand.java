package net.mvndicraft.townywaypoints.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.Translatable;
import com.palmergames.bukkit.towny.tasks.CooldownTimerTask;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import net.mvndicraft.townywaypoints.TownyWaypoints;
import net.mvndicraft.townywaypoints.VehicleTravelWarmup;
import net.mvndicraft.townywaypoints.Waypoint;
import net.mvndicraft.townywaypoints.hook.TownyRoadsHook;
import net.mvndicraft.townywaypoints.settings.Settings;
import net.mvndicraft.townywaypoints.settings.TownyWaypointsSettings;
import net.mvndicraft.townywaypoints.util.LocationUtil;
import net.mvndicraft.townywaypoints.util.Messaging;
import net.mvndicraft.townywaypoints.util.TownBlockMetaDataController;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

@CommandAlias("townywaypoints|twaypoints|twp")
public class TownyWaypointsCommand extends BaseCommand {
    static TownyAPI townyAPI = TownyAPI.getInstance();

    @Default
    @Description("Lists the version of the plugin")
    public static void onTownyWaypoints(CommandSender player) {
        player.sendMessage(TownyWaypoints.getInstance().toString());
    }

    @Subcommand("reload")
    @CommandPermission(TownyWaypoints.ADMIN_PERMISSION)
    @Description("Reloads the plugin config and locales.")
    public static void onReload(CommandSender player) {
        Settings.loadConfigAndLang();
        Messaging.sendMsg(player, Translatable.of("townywaypoints_msg_reload", TownyWaypoints.getInstance().getName()));
    }

    @Subcommand("set open")
    @Syntax("set <property> [<value>]")
    @CommandCompletion("@open_statuses @nothing")
    @Description("Change which people the plot is open to teleports from.")
    public static void onSetOpen(Player player, String status) {
        if (!player.hasPermission(TownyWaypoints.ADMIN_PERMISSION)
                && !player.hasPermission("towny.command.town.toggle.public")) {
            Messaging.sendErrorMsg(player, Translatable.of("msg_err_waypoint_set_open_insufficient_permission"));
            return;
        }

        TownBlock townBlock = TownyAPI.getInstance().getTownBlock(player);
        if (townBlock == null || !TownyWaypoints.getWaypoints().containsKey(townBlock.getTypeName())) {
            Messaging.sendErrorMsg(player, Translatable.of("msg_err_not_in_townblock"));
            return;
        }

        if (!TownyWaypoints.getInstance().getOpenStatuses().contains(status)) {
            Messaging.sendErrorMsg(player, Translatable.of("msg_err_status_invalid"));
            return;
        }

        TownBlockMetaDataController.setSdf(townBlock, TownBlockMetaDataController.statusKey, status);

        Messaging.sendMsg(player, Translatable.of("msg_status_set", status));
    }

    @Subcommand("set spawn")
    @Syntax("set <property> <value>")
    @Description("Set the block a player gets teleported to on arival for a waypoint plot.")
    public static void onSetSpawn(Player player) {
        if (!player.hasPermission(TownyWaypoints.ADMIN_PERMISSION)
                && !player.hasPermission("towny.command.town.set.spawn")) {
            Messaging.sendErrorMsg(player, Translatable.of("msg_err_waypoint_set_spawn_insufficient_permission"));
            return;
        }

        TownBlock townBlock = TownyAPI.getInstance().getTownBlock(player);
        if (townBlock == null) {
            Messaging.sendErrorMsg(player, Translatable.of("msg_err_not_in_townblock"));
            return;
        }

        Location loc = player.getLocation();

        Messaging.sendMsg(player, Translatable.of("msg_spawn_set", loc.toString()));
        TownBlockMetaDataController.setSpawn(townBlock, loc);
    }

    @Subcommand("travel")
    @Syntax("<town> <waypoint> <plot name>")
    @CommandCompletion("@reachable_waypointed_towns @town_waypoints @waypoint_plot_names @nothing")
    @Description("Travel between different waypoints.")
    public static void onTravel(Player player, String townName, String waypointName, String waypointPlotName) {
        Town town = TownyAPI.getInstance().getTown(townName);

        if (town == null)
            return;

        TownBlock townBlock = null;
        for (TownBlock _townBlock : town.getTownBlocks()) {
            String plotName = _townBlock.getName();
            if (plotName.isEmpty())
                plotName = Translatable.of("townywaypoints_plot_unnamed").defaultLocale();

            if (_townBlock.getType().getName().equals(waypointName)
                    && (plotName.equals(waypointPlotName) || waypointPlotName.isEmpty())) {
                townBlock = _townBlock;
                break;
            }
        }

        if (townBlock == null)
            return;

        Waypoint waypoint = TownyWaypoints.getWaypoints().get(waypointName);
        double travelcost = waypoint.getTravelCost();

        boolean admin = player.hasPermission(TownyWaypoints.ADMIN_PERMISSION);

        String plotName = townBlock.getName();
        if (plotName.isEmpty())
            plotName = Translatable.of("townywaypoints_plot_unnamed").defaultLocale();

        if (!admin && TownyWaypoints.getEconomy().balance("TownyWaypoints", player.getUniqueId()).doubleValue()
                - travelcost < 0) {
            Messaging.sendErrorMsg(player,
                    Translatable.of("msg_err_waypoint_travel_insufficient_funds", plotName, travelcost));
            return;
        }

        Location loc = TownBlockMetaDataController.getSpawn(townBlock);

        if (loc.getWorld() == null) {
            Messaging.sendErrorMsg(player, Translatable.of("msg_err_waypoint_spawn_not_set"));
            return;
        }
        if (!TownBlockMetaDataController.hasAccess(townBlock, player) && !admin) {
            Messaging.sendErrorMsg(player, Translatable.of("msg_err_no_access"));
            return;
        }

        double dist = LocationUtil.getDistance(player, townBlock);
        int maxDist = LocationUtil.getMaxDistance(waypoint);

        if (!admin && (dist > maxDist)) {
            Messaging.sendErrorMsg(player,
                    Translatable.of("msg_err_waypoint_travel_too_far", townBlock.getName(), maxDist));
            return;
        }


        TownBlock playerTownBlock = townyAPI.getTownBlock(player);

        if (!admin && (playerTownBlock == null || TownyWaypointsSettings.getPeerToPeer()
                && !playerTownBlock.getType().getName().equals(waypointName))) {
            Messaging.sendErrorMsg(player, Translatable.of("msg_err_waypoint_p2p", waypointName, waypointName));
            return;
        }

        if (!admin && waypoint.travelWithVehicle() && TownyRoadsHook.isEnabled()) {
            Town playerTown = playerTownBlock.getTownOrNull();
            if (playerTown != null && !playerTown.equals(town) && !TownyRoadsHook.areConnected(playerTown, town)) {
                Messaging.sendErrorMsg(player, Translatable.of("msg_err_waypoint_no_road"));
                return;
            }
        }

        Resident res = townyAPI.getResident(player);
        if (res == null)
            return;

        if (!admin && waypoint.travelWithVehicle()) {
            int stableBaseCooldownHours = TownyWaypointsSettings.getStableCooldown();
            if (stableBaseCooldownHours != -1) {
                int stableCooldown = CooldownTimerTask.getCooldownRemaining(player.getName(), "stable_waypoint");
                if (stableCooldown > 0) {
                    Messaging.sendErrorMsg(player, Translatable.of("msg_err_stable_waypoint_travel_cooldown",
                            (stableCooldown + 59) / 60, plotName));
                    return;
                }
            }
        }

        int cooldown = CooldownTimerTask.getCooldownRemaining(player.getName(), "waypoint");
        if (admin || cooldown == 0) {
            TownyWaypoints.getEconomy().withdraw("TownyWaypoints", player.getUniqueId(),
                    BigDecimal.valueOf(travelcost));
            if (admin)
                Messaging.sendMsg(player, Translatable.of("msg_waypoint_travel_warmup"));
            else
                Messaging.sendMsg(player, Translatable.of("msg_waypoint_travel_warmup_cost", travelcost));

            if (TownyWaypointsSettings.getSplit() != -1
                    && (player.getGameMode() == GameMode.SURVIVAL || player.getGameMode() == GameMode.ADVENTURE)) {
                double splitCostNation = travelcost * (1.0 - TownyWaypointsSettings.getSplit());
                double splitCostTown = travelcost * TownyWaypointsSettings.getSplit();

                town.getAccount().deposit(town.hasNation() ? splitCostTown : travelcost,
                        Translatable.of("msg_deposit_reason").toString());

                if (town.hasNation())
                    town.getNationOrNull().getAccount().deposit(splitCostNation,
                            Translatable.of("msg_deposit_reason").toString());
            }

            final String playerName = player.getName();
            final int regularCooldown = TownyWaypointsSettings.getCooldown();
            int stableSeconds = 0;
            if (waypoint.travelWithVehicle()) {
                int baseMinutes = TownyWaypointsSettings.getStableCooldown();
                if (baseMinutes != -1) {
                    int roadCount = TownyRoadsHook.isEnabled() ? TownyRoadsHook.getRoadCount(town) : 0;
                    double reduction = Math.min(1.0, roadCount * TownyWaypointsSettings.getStableCooldownRoadReduction() / 100.0);
                    int baseSeconds = (int) (baseMinutes * 60.0);
                    int minSeconds = (int) (baseSeconds * TownyWaypointsSettings.getStableCooldownMinPercent() / 100.0);
                    stableSeconds = Math.max(minSeconds, (int) (baseSeconds * (1.0 - reduction)));
                }
            }
            final int stableCooldownSeconds = stableSeconds;
            Runnable cooldownCallback = () -> {
                if (!CooldownTimerTask.hasCooldown(playerName, "waypoint"))
                    CooldownTimerTask.addCooldownTimer(playerName, "waypoint", regularCooldown);
                if (stableCooldownSeconds > 0 && !CooldownTimerTask.hasCooldown(playerName, "stable_waypoint"))
                    CooldownTimerTask.addCooldownTimer(playerName, "stable_waypoint", stableCooldownSeconds);
            };
            teleport(player, loc, waypoint.travelWithVehicle(), cooldownCallback, admin);
        } else {
            Messaging.sendErrorMsg(player,
                    Translatable.of("msg_err_waypoint_travel_cooldown", cooldown, townBlock.getName()));
        }
    }

    @Subcommand("travel")
    @Syntax("<town> <waypoint> <plot name>")
    @CommandCompletion("@reachable_waypointed_towns @town_waypoints @waypoint_plot_names @nothing")
    @Description("Travel between different waypoints.")
    public static void onTravel(Player player, String townName, String waypointName) {
        onTravel(player, townName, waypointName, "");
    }

    private static void teleport(@Nonnull final Player player, @Nonnull Location loc, boolean travelWithVehicle, @Nonnull Runnable cooldownCallback, boolean admin) {
        Entity vehicle = player.getVehicle();
        boolean needToTpVehicle = travelWithVehicle && player.isInsideVehicle() && vehicle != null;

        if (needToTpVehicle) {
            closeTravelingPlayerVehicleInventory(vehicle, player);

            List<Entity> extraPassengers = new ArrayList<>();
            for (Entity passenger : vehicle.getPassengers()) {
                if (passenger != player)
                    extraPassengers.add(passenger);
            }

            boolean skipWarmup = admin || player.hasPermission("towny.admin.spawn.nowarmup");
            VehicleTravelWarmup.schedule(player, vehicle, extraPassengers, loc, cooldownCallback, skipWarmup);
            return;
        }

        TownyWaypoints.addPendingCooldownCallback(player.getUniqueId(), cooldownCallback);
        townyAPI.requestTeleport(player, loc);
    }

    public static void executeVehicleTeleport(@Nonnull Player player, @Nonnull Entity vehicle,
            @Nonnull List<Entity> extraPassengers, @Nonnull Location loc, @Nonnull Runnable cooldownCallback) {
        closeTravelingPlayerVehicleInventory(vehicle, player);

        vehicle.teleportAsync(loc, TeleportCause.COMMAND)
                .thenRun(() -> TownyWaypoints.getScheduler().runTask(loc, () -> {
                    if (!vehicle.getPassengers().contains(player))
                        player.teleportAsync(loc, TeleportCause.COMMAND).thenRun(() -> vehicle.addPassenger(player));

                    Location vehicleLoc = vehicle.getLocation();
                    for (Entity passenger : extraPassengers) {
                        if (!passenger.isValid() || passenger.isDead())
                            continue;
                        passenger.teleport(vehicleLoc);
                        if (!vehicle.getPassengers().contains(passenger))
                            passenger.teleportAsync(loc, TeleportCause.COMMAND)
                                    .thenRun(() -> vehicle.addPassenger(passenger));
                    }

                    closeTravelingPlayerVehicleInventory(vehicle, player);
                    cooldownCallback.run();
                }));
    }

    private static void closeTravelingPlayerVehicleInventory(@Nonnull Entity vehicle, @Nonnull Player player) {
        player.getScheduler().run(TownyWaypoints.getInstance(), task -> {
            if (player.getOpenInventory().getTopInventory().getHolder() == vehicle)
                player.closeInventory();
        }, null);
    }

    @Subcommand("list")
    @Syntax("<waypoint> <int>")
    @CommandCompletion("@waypoints @waypoints_pages @nothing")
    @Description("Display the list of waypoints.")
    public static void onList(Player player, String waypointName, Integer page) {
        Location location = player.getLocation();
        // Get the 10 closest waypoints for page 1. Then 10 to 19 for page 2 etc.
        List<TownBlock> waypointTownBlocks = TownyAPI.getInstance().getTownBlocks().stream()
                .filter(tb -> tb.getType().getName().equals(waypointName)).filter(TownBlock::hasTown).filter(tb -> TownBlockMetaDataController.getSpawn(tb).getWorld() != null)
                .sorted(Comparator.comparingDouble(tb -> TownBlockMetaDataController.getSpawn(tb).distance(location)))
                .toList();

        if (waypointTownBlocks.isEmpty()) {
            Messaging.sendErrorMsg(player, Translatable.of("msg_err_waypoint_not_found", waypointName));
        } else {
            int maxPage = Math.floorDiv(waypointTownBlocks.size(), 10);
            if (page < 1) {
                page = 1;
            } else if (page > maxPage) {
                page = maxPage;
            }
            String tenValues = waypointTownBlocks.stream().skip((page - 1L) * 10).limit(10).filter(tb -> tb.getTownOrNull() != null && TownBlockMetaDataController.getSpawn(tb).getWorld() != null)
                    .map(tb -> tb.getTownOrNull().getName() + " " + tb.getName() + " "
                            + ((int) TownBlockMetaDataController.getSpawn(tb).distance(location)) + "m"
                            + (TownBlockMetaDataController.hasAccess(tb, player) ? " (accessible)" : ""))
                    .collect(Collectors.joining("\n"));
            String message = page + "/" + maxPage + "\n" + tenValues;
            Messaging.sendMsg(player, Translatable.of("msg_page", message));
        }
    }

    @Subcommand("list")
    @Syntax("<waypoint> <int>")
    @CommandCompletion("@waypoints @waypoints_pages @nothing")
    @Description("Display the list of waypoints.")
    public static void onList(Player player, String waypointName) {
        onList(player, waypointName, 1);
    }
}
