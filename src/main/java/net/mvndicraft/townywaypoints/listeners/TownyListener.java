package net.mvndicraft.townywaypoints.listeners;

import com.palmergames.bukkit.towny.event.PlotPreChangeTypeEvent;
import com.palmergames.bukkit.towny.event.TownBlockTypeRegisterEvent;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.exceptions.TownyException;
import com.palmergames.bukkit.towny.object.*;
import fr.formiko.mc.biomeutils.NMSBiomeUtils;
import net.mvndicraft.townywaypoints.TownyWaypoints;
import net.mvndicraft.townywaypoints.Waypoint;
import net.mvndicraft.townywaypoints.util.Messaging;
import net.mvndicraft.townywaypoints.util.TownBlockMetaDataController;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public final class TownyListener implements Listener
{
  private final static TownyWaypoints instance = TownyWaypoints.getInstance();
  public static void registerPlot(String name, String mapKey, double cost)
  {
    if (TownBlockTypeHandler.exists(name))
      return;

    TownBlockType townBlockType = new TownBlockType(name, new TownBlockData() {
      @Override
      public String getMapKey()
      {
        return mapKey;
      }

      @Override
      public double getCost()
      {
        return cost;
      }
    });

    try {
      instance.getLogger().info("registering new plot type " + name);
      TownBlockTypeHandler.registerType(townBlockType);
    } catch (TownyException e) {
      Bukkit.getLogger().severe(e.getMessage());
    }
  }

  @EventHandler
  public void onTownyLoadTownBlockTypes(TownBlockTypeRegisterEvent event)
  {
    TownyWaypoints.loadWaypoints();
  }

  private int getPlotTypeCount(Town town, String name)
  {
    int count = 0;

    for (TownBlock townBlock : town.getTownBlocks())
    {
      if (townBlock.getType().getName().equalsIgnoreCase(name))
        count++;
    }

    return count;
  }

  private static boolean biomeAllowed(Location loc, Waypoint waypoint) {
    if (!waypoint.getAllowedBiomeTags().isEmpty())
      for (String tag : waypoint.getAllowedBiomeTags()) {
        if (NMSBiomeUtils.matchTag(loc, tag))
          return true;
      }
    if (waypoint.getAllowedBiomes().isEmpty())
      return waypoint.getAllowedBiomeTags().isEmpty();

    return waypoint.getAllowedBiomes().contains(loc.getBlock().getBiome().toString());
  }

  @EventHandler
  public void onPlotPreChangeTypeEvent(PlotPreChangeTypeEvent event) throws NotRegisteredException {
    TownBlock townBlock = event.getTownBlock();
    String plotTypeName = event.getNewType().getName();

    if (!TownyWaypoints.getWaypoints().containsKey(plotTypeName))
        return;
    Waypoint waypoint = TownyWaypoints.getWaypoints().get(plotTypeName);

    World world = townBlock.getWorld().getBukkitWorld();
    if (world == null)
      return;
    Player player = event.getResident().getPlayer();
    if (player == null)
      return;

   if (!waypoint.getPermission().isEmpty() && !player.hasPermission(waypoint.getPermission())) {
     event.setCancelMessage(Translatable.of("msg_err_waypoint_create_insufficient_permission",waypoint.getName()).defaultLocale());
     event.setCancelled(true);
     return;
   }

   if (TownyWaypoints.getEconomy().balance("TownyWaypoints", player.getUniqueId()).doubleValue() - waypoint.getCost() <= 0) {
     event.setCancelMessage(Translatable.of("msg_err_waypoint_create_insufficient_funds",waypoint.getName(), waypoint.getCost()).defaultLocale());
     event.setCancelled(true);
     return;
   }

    Location loc = player.getLocation();
    if (!biomeAllowed(loc, waypoint)) {
     event.setCancelMessage(Translatable.of("msg_err_biome_not_allowed", loc.getBlock().getBiome().toString()).defaultLocale());
     event.setCancelled(true);
     return;
    }

    int max = waypoint.getMax();

    if (getPlotTypeCount(townBlock.getTown(), plotTypeName) >= max) {
      event.setCancelMessage(Translatable.of("msg_err_max_plots",max).defaultLocale());
      event.setCancelled(true);
      return;
    }

    Messaging.sendMsg(player, Translatable.of("msg_spawn_set", loc.toString()));
    TownBlockMetaDataController.setSpawn(townBlock, player.getLocation());
  }
}
