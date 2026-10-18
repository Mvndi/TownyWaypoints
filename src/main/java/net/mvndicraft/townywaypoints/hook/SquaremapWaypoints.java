package net.mvndicraft.townywaypoints.hook;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.TownBlock;
import net.mvndicraft.townywaypoints.util.TownBlockMetaDataController;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import xyz.jpenilla.squaremap.api.Key;
import xyz.jpenilla.squaremap.api.MapWorld;
import xyz.jpenilla.squaremap.api.Point;
import xyz.jpenilla.squaremap.api.SimpleLayerProvider;
import xyz.jpenilla.squaremap.api.Squaremap;
import xyz.jpenilla.squaremap.api.WorldIdentifier;
import xyz.jpenilla.squaremap.api.marker.Marker;
import xyz.jpenilla.squaremap.api.marker.MarkerOptions;

public final class SquaremapWaypoints {
    private static final String PORT = "seaport";
    private static final String STABLE = "stable";
    private static final String PORT_LABEL = "Ports";
    private static final String STABLE_LABEL = "Stables";
    private static final Key PORT_LAYER = Key.of("townywaypoints-ports");
    private static final Key STABLE_LAYER = Key.of("townywaypoints-stables");
    private static final Key PORT_ICON = Key.of("townywaypoints-port");
    private static final Key STABLE_ICON = Key.of("townywaypoints-stable");
    private static final int ICON_SIZE = 24;
    private final Plugin plugin;
    private final Map<String, Layers> worlds = new HashMap<>();

    public SquaremapWaypoints(Plugin plugin) {
        this.plugin = plugin;
    }

    public void update() {
        final Squaremap squaremap = Bukkit.getServicesManager().load(Squaremap.class);
        if (squaremap == null) {
            return;
        }
        this.registerIcons(squaremap);
        this.worlds.values().forEach(Layers::clear);
        for (final TownBlock townBlock : TownyAPI.getInstance().getTownBlocks()) {
            final String type = townBlock.getType().getName();
            if (!type.equalsIgnoreCase(PORT) && !type.equalsIgnoreCase(STABLE)) {
                continue;
            }
            final Location location = TownBlockMetaDataController.getSpawn(townBlock);
            final World world = location.getWorld();
            if (world == null || townBlock.getTownOrNull() == null) {
                continue;
            }
            squaremap.getWorldIfEnabled(WorldIdentifier.parse(world.getKey().toString())).ifPresent(mapWorld ->
                this.addMarker(mapWorld, townBlock, location, type)
            );
        }
    }

    private void addMarker(MapWorld world, TownBlock townBlock, Location location, String type) {
        final Layers layers = this.layers(world);
        final String id = type + "-" + townBlock.getWorld().getName().replaceAll("[^a-zA-Z0-9._-]", "_")
            + "-" + townBlock.getX() + "-" + townBlock.getZ();
        final String tooltip = townBlock.getTownOrNull().getName() + " " + type;
        final Marker marker = Marker.icon(
            Point.point(location.getX(), location.getZ()),
            type.equalsIgnoreCase(PORT) ? PORT_ICON : STABLE_ICON,
            ICON_SIZE,
            ICON_SIZE
        ).markerOptions(MarkerOptions.builder().hoverTooltip(tooltip).build());
        (type.equalsIgnoreCase(PORT) ? layers.ports() : layers.stables()).addMarker(Key.of(id), marker);
    }

    private Layers layers(MapWorld world) {
        return this.worlds.computeIfAbsent(world.identifier().asString(), ignored -> {
            final SimpleLayerProvider ports = SimpleLayerProvider.builder(PORT_LABEL).build();
            final SimpleLayerProvider stables = SimpleLayerProvider.builder(STABLE_LABEL).build();
            world.layerRegistry().register(PORT_LAYER, ports);
            world.layerRegistry().register(STABLE_LAYER, stables);
            return new Layers(ports, stables);
        });
    }

    private void registerIcons(Squaremap squaremap) {
        this.registerIcon(squaremap, PORT_ICON, "icons/port.png");
        this.registerIcon(squaremap, STABLE_ICON, "icons/stable.png");
    }

    private void registerIcon(Squaremap squaremap, Key key, String resource) {
        if (squaremap.iconRegistry().hasEntry(key)) {
            return;
        }
        try (InputStream stream = this.plugin.getResource(resource)) {
            if (stream != null) {
                final BufferedImage image = ImageIO.read(stream);
                if (image != null) {
                    squaremap.iconRegistry().register(key, image);
                }
            }
        } catch (IOException exception) {
            this.plugin.getLogger().warning("Could not load squaremap waypoint icon " + resource);
        }
    }

    private record Layers(SimpleLayerProvider ports, SimpleLayerProvider stables) {
        private void clear() {
            this.ports.clearMarkers();
            this.stables.clearMarkers();
        }
    }
}
