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
  private final int max_distance;
  private final List<String> allowedBiomeTags;
  private final List<String> allowedBiomes;

  public Waypoint(String name, String mapKey, double cost, double travelCost, int max, boolean sea, boolean travelWithVehicle, String permission, int max_distance, List<String> allowedBiomeTags, List<String> allowedBiomes) {
    this.name = name;
    this.mapKey = mapKey;
    this.cost = cost;
    this.travelCost = travelCost;
    this.max = max;
    this.sea = sea;
    this.travelWithVehicle = travelWithVehicle;
    this.permission = permission;
    this.max_distance = max_distance;
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
    return max_distance;
  }

  public List<String> getAllowedBiomeTags() {
    return allowedBiomeTags;
  }

  public List<String> getAllowedBiomes() {
    return allowedBiomes;
  }
}
