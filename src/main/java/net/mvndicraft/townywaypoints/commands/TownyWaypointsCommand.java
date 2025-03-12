package net.mvndicraft.townywaypoints.commands;

import javax.annotation.Nonnull;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.Translatable;
import com.palmergames.bukkit.towny.tasks.CooldownTimerTask;
import com.palmergames.paperlib.PaperLib;
import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import net.mvndicraft.townywaypoints.TownyWaypoints;
import net.mvndicraft.townywaypoints.Waypoint;
import net.mvndicraft.townywaypoints.settings.Settings;
import net.mvndicraft.townywaypoints.settings.TownyWaypointsSettings;
import net.mvndicraft.townywaypoints.util.Messaging;
import net.mvndicraft.townywaypoints.util.TownBlockMetaDataController;

import java.math.BigDecimal;

@CommandAlias("townywaypoints|twaypoints|twp")
public class TownyWaypointsCommand extends BaseCommand {
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
        if (townBlock == null) {
            Messaging.sendErrorMsg(player, Translatable.of("msg_err_not_in_townblock"));
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
    @CommandCompletion("@waypointed_towns @town_waypoints @waypoint_plot_names @nothing")
    @Description("Travel between different waypoints.")
    public static void onTravel(Player player, String townName, String waypointName, String waypointPlotName) {
        Town town = TownyAPI.getInstance().getTown(townName);

        if (town == null)
            return;

        TownBlock townBlock = null;
        for (TownBlock _townBlock : town.getTownBlocks()) {
            String plotName = _townBlock.getName();
            if (plotName.equals(""))
                plotName = Translatable.of("townywaypoints_plot_unnamed").defaultLocale();

            if (_townBlock.getType().getName().equals(waypointName) && plotName.equals(waypointPlotName)) {
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
        if (plotName.equals(""))
            plotName = Translatable.of("townywaypoints_plot_unnamed").defaultLocale();

        if (!admin && TownyWaypoints.getEconomy().balance("TownyWaypoints", player.getUniqueId()).doubleValue() - travelcost < 0) {
            Messaging.sendErrorMsg(player,
                    Translatable.of("msg_err_waypoint_travel_insufficient_funds", plotName, travelcost));
            return;
        }

        Location loc = TownBlockMetaDataController.getSpawn(townBlock);

        if (loc.getWorld() == null) {
            Messaging.sendErrorMsg(player, Translatable.of("msg_err_waypoint_spawn_not_set"));
            return;
        }

        double dist = player.getLocation().distance(loc);
        int dist_to = waypoint.getMaxDistance() != -1 ? waypoint.getMaxDistance()
                : TownyWaypointsSettings.getMaxDistance();

        if (!admin && (dist > dist_to)) {
            Messaging.sendErrorMsg(player, Translatable.of("msg_err_waypoint_travel_too_far", townBlock.getName(),
                    dist_to));
            return;
        }

        TownyAPI townyAPI = TownyAPI.getInstance();

        TownBlock playerTownBlock = townyAPI.getTownBlock(player);

        if (!admin && (playerTownBlock == null || TownyWaypointsSettings.getPeerToPeer()
                && !playerTownBlock.getType().getName().equals(waypointName))) {
            Messaging.sendErrorMsg(player, Translatable.of("msg_err_waypoint_p2p", waypointName, waypointName));
            return;
        }

        Resident res = townyAPI.getResident(player);
        if (res == null)
            return;

        int cooldown = CooldownTimerTask.getCooldownRemaining(player.getName(), "waypoint");
        if (admin || cooldown == 0) {
            TownyWaypoints.getEconomy().withdraw("TownyWaypoints", player.getUniqueId(), new BigDecimal(travelcost));
            if (admin)
                Messaging.sendMsg(player, Translatable.of("msg_waypoint_travel_warmup"));
            else
                Messaging.sendMsg(player, Translatable.of("msg_waypoint_travel_warmup_cost", travelcost));
            teleport(player, loc, waypoint.travelWithVehicle());

            if (TownyWaypointsSettings.getSplit() != -1 && (player.getGameMode() == GameMode.SURVIVAL
                    || player.getGameMode() == GameMode.ADVENTURE)) {
                double splitCostNation = travelcost * (1.0 - TownyWaypointsSettings.getSplit());
                double splitCostTown = travelcost * TownyWaypointsSettings.getSplit();

                town.getAccount().deposit(town.hasNation() ? splitCostTown : travelcost,
                        Translatable.of("msg_deposit_reason").toString());

                if (town.hasNation())
                    town.getNationOrNull().getAccount().deposit(splitCostNation,
                            Translatable.of("msg_deposit_reason").toString());
            }

            if (!CooldownTimerTask.hasCooldown(player.getName(), "waypoint"))
                CooldownTimerTask.addCooldownTimer(player.getName(), "waypoint", TownyWaypointsSettings.getCooldown());
        } else {
            Messaging.sendErrorMsg(player,
                    Translatable.of("msg_err_waypoint_travel_cooldown", cooldown, townBlock.getName()));
        }
    }

    private static void teleport(@Nonnull final Player player, @Nonnull Location loc, boolean travelWithVehicle) {
        Entity vehicle = player.getVehicle();
        boolean needToTpVehicle = travelWithVehicle && player.isInsideVehicle() && vehicle != null;

        if (needToTpVehicle) {
            vehicle.eject();
            PaperLib.teleportAsync(vehicle, loc, TeleportCause.COMMAND);
        }
        PaperLib.teleportAsync(player, loc, TeleportCause.COMMAND);
        if (needToTpVehicle)
            TownyWaypoints.getScheduler().runTask(loc, () -> vehicle.addPassenger(player));
    }
}
