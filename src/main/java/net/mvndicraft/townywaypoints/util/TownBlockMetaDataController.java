package net.mvndicraft.townywaypoints.util;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import com.palmergames.bukkit.towny.object.TownBlock;
import com.palmergames.bukkit.towny.object.metadata.CustomDataField;
import com.palmergames.bukkit.towny.object.metadata.IntegerDataField;
import com.palmergames.bukkit.towny.object.metadata.StringDataField;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class TownBlockMetaDataController {

    public static final String statusKey = "townywaypoints_status";
    private static final String spawnWorldKey = "townywaypoints_spawn_world";
    private static final String spawnXKey = "townywaypoints_spawn_x";
    private static final String spawnYKey = "townywaypoints_spawn_y";
    private static final String spawnZKey = "townywaypoints_spawn_z";

    public static int getIdf(TownBlock townBlock, String key) {
        if (townBlock.hasMeta(key)) {
            CustomDataField<?> cdf = townBlock.getMetadata(key);
            if (cdf instanceof IntegerDataField)
                return ((IntegerDataField) cdf).getValue();
        }
        return 0;
    }

    public static void setIdf(TownBlock townBlock, String key, int num) {
        if (townBlock.hasMeta(key)) {
            CustomDataField<?> cdf = townBlock.getMetadata(key);
            if (num == 0 && cdf != null)
                townBlock.removeMetaData(cdf);
            else {
                if (cdf instanceof IntegerDataField) {
                    ((IntegerDataField) cdf).setValue(num);
                    townBlock.save();
                }
            }
        } else if (num != 0)
            townBlock.addMetaData(new IntegerDataField(key, num));
    }

    public static String getSdf(TownBlock townBlock, String key) {
        if (townBlock.hasMeta(key)) {
            CustomDataField<?> cdf = townBlock.getMetadata(key);
            if (cdf instanceof StringDataField)
                return ((StringDataField) cdf).getValue();
        }
        return "";
    }

    public static void setSdf(TownBlock townBlock, String key, String value) {
        if (townBlock.hasMeta(key)) {
            CustomDataField<?> cdf = townBlock.getMetadata(key);
            if (value.equals("") && cdf != null)
                townBlock.removeMetaData(cdf);
            else {
                if (cdf instanceof StringDataField) {
                    ((StringDataField) cdf).setValue(value);
                    townBlock.save();
                }
            }
        } else if (!value.equals(""))
            townBlock.addMetaData(new StringDataField(key, value));
    }

    public static void setSpawn(TownBlock townBlock, Location loc) {
        setSdf(townBlock, spawnWorldKey, loc.getWorld().getName());
        setIdf(townBlock, spawnXKey, loc.getBlockX());
        setIdf(townBlock, spawnYKey, loc.getBlockY());
        setIdf(townBlock, spawnZKey, loc.getBlockZ());
    }

    public static Location getSpawn(TownBlock townBlock) {
        return new Location(Bukkit.getServer().getWorld(getSdf(townBlock, spawnWorldKey)), getIdf(townBlock, spawnXKey), getIdf(townBlock, spawnYKey), getIdf(townBlock, spawnZKey));
    }

    public static boolean hasAccess(TownBlock townBlock, Player player) {
        String status = getSdf(townBlock, statusKey);
        Resident res = TownyAPI.getInstance().getResident(player);

        if (res == null)
            return false;

        switch (status) {
            case "allies" -> {
                Nation nation = res.getNationOrNull();
                Town destTown = townBlock.getTownOrNull();
                if (destTown == null)
                    return false;
                Nation destNation = destTown.getNationOrNull();
                if (nation == null || destNation == null)
                    return false;
                return nation.hasAlly(destNation);
            }
            case "nation" -> {
                Nation nation = res.getNationOrNull();
                Town destTown = townBlock.getTownOrNull();
                if (destTown == null)
                    return false;
                Nation destNation = destTown.getNationOrNull();
                if (nation == null || destNation == null)
                    return false;
                return nation.equals(destNation);
            }
            case "town" -> {
                Town town = res.getTownOrNull();
                Town destTown = townBlock.getTownOrNull();
                if (town == null || destTown == null)
                    return false;
                return town.equals(destTown);
            }
            case "none" -> {
                return false;
            }
            default -> {
                return true;
            }
        }
    }

    public static int numWaypointsWithAccess(Town town, Player player, String waypointName) {
        int count = 0;

        for (TownBlock townBlock : town.getTownBlocks()) {
            if (townBlock.getType().getName().equals(waypointName) && hasAccess(townBlock, player))
                count++;
        }

        return count;
    }
}
