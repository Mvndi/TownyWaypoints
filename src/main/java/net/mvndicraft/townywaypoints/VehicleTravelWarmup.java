package net.mvndicraft.townywaypoints;

import com.palmergames.bukkit.towny.TownySettings;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import net.mvndicraft.townywaypoints.commands.TownyWaypointsCommand;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public final class VehicleTravelWarmup {

    private static final ConcurrentHashMap<UUID, PendingTravel> PENDING = new ConcurrentHashMap<>();

    private VehicleTravelWarmup() {
    }

    public static boolean hasPending(UUID playerId) {
        return PENDING.containsKey(playerId);
    }

    public static void cancel(UUID playerId) {
        PENDING.remove(playerId);
    }

    public static void schedule(Player player, Entity vehicle, List<Entity> extraPassengers, Location destination,
            Runnable onComplete, boolean skipWarmup) {
        cancel(player.getUniqueId());

        PendingTravel pending = new PendingTravel(
                vehicle.getUniqueId(),
                extraPassengers.stream().map(Entity::getUniqueId).collect(Collectors.toList()),
                destination.clone(),
                onComplete);
        PENDING.put(player.getUniqueId(), pending);

        long delayTicks = skipWarmup ? 0L : TownySettings.getTeleportWarmupTime() * 20L;
        Runnable execute = () -> {
            PendingTravel travel = PENDING.remove(player.getUniqueId());
            if (travel == null)
                return;

            Player onlinePlayer = Bukkit.getPlayer(player.getUniqueId());
            Entity onlineVehicle = Bukkit.getEntity(travel.vehicleId());
            if (onlinePlayer == null || !onlinePlayer.isOnline() || onlineVehicle == null || !onlineVehicle.isValid())
                return;

            List<Entity> extras = travel.extraPassengerIds().stream()
                    .map(Bukkit::getEntity)
                    .filter(Objects::nonNull)
                    .filter(entity -> entity.isValid() && !entity.isDead())
                    .collect(Collectors.toList());

            TownyWaypointsCommand.executeVehicleTeleport(onlinePlayer, onlineVehicle, extras, travel.destination(),
                    travel.onComplete());
        };

        if (delayTicks <= 0L)
            TownyWaypoints.getScheduler().runTask(player, execute);
        else
            TownyWaypoints.getScheduler().runTaskLater(player, execute, delayTicks);
    }

    record PendingTravel(UUID vehicleId, List<UUID> extraPassengerIds, Location destination, Runnable onComplete) {
    }
}