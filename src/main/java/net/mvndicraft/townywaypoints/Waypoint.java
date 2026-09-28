package net.mvndicraft.townywaypoints;

import java.util.List;

public final class Waypoint {
    private final String name;
    private final String mapKey;
    private final double cost;
    private final double travelCost;
    private final int max;
    private final boolean sea;
    private final boolean travelWithVehicle;
    private final String permission;
    private final int maxDistance;
    private final int minHomeBlockDistance;
    private final List<String> allowedBiomeTags;
    private final List<String> allowedBiomes;

    public Waypoint(String name, String mapKey, double cost, double travelCost, int max, boolean sea,
            boolean travelWithVehicle, String permission, int maxDistance, int minHomeBlockDistance, List<String> allowedBiomeTags,
            List<String> allowedBiomes) {
        this.name = name;
        this.mapKey = mapKey;
        this.cost = cost;
        this.travelCost = travelCost;
        this.max = max;
        this.sea = sea;
        this.travelWithVehicle = travelWithVehicle;
        this.permission = permission;
        this.maxDistance = maxDistance;
        this.minHomeBlockDistance = minHomeBlockDistance;
        this.allowedBiomeTags = allowedBiomeTags;
        this.allowedBiomes = allowedBiomes;
    }

    public String getName() {
        return name;
    }

    public String getMapKey() {
        return mapKey;
    }

    public double getCost() {
        return cost;
    }

    public double getTravelCost() {
        return travelCost;
    }

    public int getMax() {
        return max;
    }

    public boolean isSea() {
        return sea;
    }

    public boolean travelWithVehicle() {
        return travelWithVehicle;
    }

    public String getPermission() {
        return permission;
    }

    public int getMaxDistance() {
        return maxDistance;
    }

    public int getMinHomeBlockDistance() {
        return minHomeBlockDistance;
    }

    public boolean isTooCloseToHomeBlock(int homeBlockDistance) {
        return minHomeBlockDistance > 0 && homeBlockDistance >= 0 && homeBlockDistance < minHomeBlockDistance;
    }

    public List<String> getAllowedBiomeTags() {
        return allowedBiomeTags;
    }

    public List<String> getAllowedBiomes() {
        return allowedBiomes;
    }
}
