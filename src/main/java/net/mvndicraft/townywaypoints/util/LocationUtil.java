package net.mvndicraft.townywaypoints.util;

import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import net.mvndicraft.townywaypoints.TownyWaypoints;
import net.mvndicraft.townywaypoints.Waypoint;
import net.mvndicraft.townywaypoints.settings.TownyWaypointsSettings;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

public class LocationUtil {
    public static boolean isSafe(Location location) {
        // Ensure the player's feet aren't in a block
        Block feet = location.getBlock();
        if (!feet.getType().isAir()) {
            return false; // not transparent (will suffocate)
        }

        // Ensure the player's head isn't in a block
        Block head = feet.getRelative(BlockFace.UP);
        if (!head.getType().isAir()) {
            return false; // not transparent (will suffocate)
        }

        // Ensure the block under the player is solid
        Block ground = feet.getRelative(BlockFace.DOWN);
        return ground.getType().isSolid(); // not solid
    }

    public static boolean isCloseEnough(Player player, Waypoint waypoint, Town town) {
        if (player.hasPermission(TownyWaypoints.ADMIN_PERMISSION) ){ // Admins can always teleport
            return true;
        }
        return town.getTownBlocks().stream()
                .anyMatch(townBlock -> townBlock.getType().getName().equals(waypoint.getName())
                        && getDistance(player, townBlock) <= getMaxDistance(waypoint));
    }
    public static int getMaxDistance(Waypoint waypoint) {
        return waypoint.getMaxDistance() != -1 ? waypoint.getMaxDistance()
                : TownyWaypointsSettings.getMaxDistance();
    }
    public static double getDistance(Player player, TownBlock townBlock) {
        Location loc1 = TownBlockMetaDataController.getSpawn(townBlock);
        Location loc2 = player.getLocation();
        // Not same world -> too far
        if(!loc1.getWorld().getUID().equals(loc2.getWorld().getUID())) {
            return Double.MAX_VALUE;
        }
        return loc1.distance(loc2);
    }
}
