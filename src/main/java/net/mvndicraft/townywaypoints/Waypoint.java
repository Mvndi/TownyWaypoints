package net.mvndicraft.townywaypoints;

import fr.formiko.mc.biomeutils.NMSBiomeUtils;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.tag.Tag;
import io.papermc.paper.registry.tag.TagKey;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.block.Biome;

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
    private final Set<Biome> allowedBiomes;

    public Waypoint(String name, String mapKey, double cost, double travelCost, int max, boolean sea,
            boolean travelWithVehicle, String permission, int max_distance, List<String> allowedBiomeTags,
            List<String> allowedBiomes) {
        this.name = name;
        this.mapKey = mapKey;
        this.cost = cost;
        this.travelCost = travelCost;
        this.max = max;
        this.sea = sea;
        this.travelWithVehicle = travelWithVehicle;
        this.permission = permission;
        this.max_distance = max_distance;

        Set<Biome> biomes = new HashSet<>();
        Registry<Biome> biomeRegistry = RegistryAccess.registryAccess().getRegistry(RegistryKey.BIOME);
        // Add each biomes from the config biome list and the config biome tag list
        biomes.addAll(allowedBiomes.stream().map(
                biomeKey -> biomeRegistry.get(NamespacedKey.fromString(NMSBiomeUtils.normalizeBiomeName(biomeKey))))
                .toList());
        allowedBiomeTags
                .forEach(biomeTagString -> biomes.addAll(getBiomesFromTag(NamespacedKey.fromString(biomeTagString))));

        this.allowedBiomes = biomes;
    }

    public static Collection<Biome> getBiomesFromTag(NamespacedKey tagKey) {
        Registry<Biome> biomeRegistry = RegistryAccess.registryAccess().getRegistry(RegistryKey.BIOME);
        Tag<Biome> biomeTag = biomeRegistry.getTag(TagKey.create(RegistryKey.BIOME, tagKey));
        if (biomeTag == null) {
            return Set.of();
        }
        return biomeTag.values().stream().map(key -> biomeRegistry.get(key)).toList();
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


    public boolean isAllowedBiome(Biome biome) {
        return allowedBiomes.contains(biome);
    }
}
