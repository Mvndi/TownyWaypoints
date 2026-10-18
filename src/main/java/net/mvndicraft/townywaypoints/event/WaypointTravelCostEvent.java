package net.mvndicraft.townywaypoints.event;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public final class WaypointTravelCostEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private static final String INVALID = "Calculated value must be finite and nonnegative.";
    private final UUID player;
    private double cost;

    public WaypointTravelCostEvent(UUID player, double cost) {
        super(!Bukkit.isPrimaryThread());
        this.player = player;
        setCost(cost);
    }

    public UUID getPlayerId() {
        return player;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double value) {
        if (!Double.isFinite(value) || value < 0) {
            throw new IllegalArgumentException(INVALID);
        }
        cost = value;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
