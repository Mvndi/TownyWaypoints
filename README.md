# TownyWaypoints

![townywaypoints](https://github.com/ewof/TownyWaypoints/assets/26354814/876e138d-b011-4286-99bd-06f14cf4f86a)

Configurable plot types for Towny that players can teleport between. <br/>
Requires [Paper](https://github.com/PaperMC/Paper) or a fork of it. Supports [Folia](https://github.com/PaperMC/Folia). It will not work on Bukkit or Spigot.

### Traveling

Players can travel between different waypoints with `/twp travel <town> <waypoint> <plot name>` <br/>
When players teleport it uses Townys warmups, so if you have a teleport warmup in Towny / make movement or damage cancel the warmup those will apply when players attempt to travel to waypoints.

---

### Config

Default config:

```yaml
# This is the current version. Please do not edit.
version: '1.7'
# The language file you wish to use.
language: en_US.yml

waypoints:

  ############################################################
  # +------------------------------------------------------+ #
  # |                       Economy                        | #
  # +------------------------------------------------------+ #
  ############################################################

  economy:

    # The percentage of the travel cost that gets added to the waypoints town bank, the rest goes to the nation. If it has no nation then 100% goes to the town bank.
    # Disabled with value of -1
    split: '0.5'

  ############################################################
  # +------------------------------------------------------+ #
  # |                   Restrictions                       | #
  # +------------------------------------------------------+ #
  ############################################################

  restrictions:

    # The maximum number of blocks a player can travel between waypoints.
    # Disabled with value of -1
    max_distance: '2700'

    # The amount of seconds a player must wait between waypoint travels.
    cooldown: '300'

    # If true players can only teleport from one waypoint type to another.
    peer_to_peer: 'true'

```

- `waypoints.restrictions.max_distance` is the maximum number of blocks a player can travel via waypoints. A player cannot travel to a waypoint that is `max_distance` blocks away from their current location.
- `waypoints.restrictions.cooldown` is the amount of time in seconds a player must  way between waypoint travels.
- `waypoints.restrictions.peer_to_peer` when set to true means that players must be standing in a waypoint of the same type to teleport to another waypoint, meaning I must be standing in a stable plot if I want to travel to another stable waypoint.

---

### The `waypoints.yml` file

In `waypoints.yml` you define different types of waypoints.

```yaml
stable:
  name: "stable"
  mapKey: 'S' # A single character to be shown on the /towny map and /towny map hud.
  cost: 4500.0 # Cost to create the waypoint.
  travel_cost: 450 # Cost to travel to waypoint.
  max: 1 # Max number of plots of this type allowed per town.
  travel_with_vehicle: true # If true, player's vehicle will travel with the player.
  permission: townywaypoints.landpoint.stable # Permission node required to set a plot to a type of this waypoint, if no permission is set anyone can create this waypoint, grant it in townyperms.yml
  # The maximum number of blocks a player can travel between waypoints.
  # Uses the global value if -1
  max_distance: -1
  allowed_biome_tags: # List of biome tags this plot can be created on. Takes priority over allowed_biomes.
    - minecraft:is_overworld
  allowed_biomes:  # List of biomes this plot type can be created on. If it's not provided the plot type can be created on any biome.
    - FOREST
    - PLAINS
    - SUNFLOWER_PLAINS
    - TAIGA
    - BIRCH_FOREST
    - SAVANNA
    - WINDSWEPT_FOREST
    - WINDSWEPT_SAVANNA
    - FLOWER_FOREST
seaport:
  name: "seaport"
  mapKey: 'P' # A single character to be shown on the /towny map and /towny map hud.
  cost: 2500.0 # Cost to create the waypoint.
  travel_cost: 250 # Cost to travel to waypoint.
  max: 1 # Max number of plots of this type allowed per town.
  travel_with_vehicle: false # If true, player's vehicle will travel with the player.
  permission: townywaypoints.seapoint.seaport # Permission node required to set a plot to a type of this waypoint, if no permission is set anyone can create this waypoint, grant it in townyperms.yml
  # The maximum number of blocks a player can travel between waypoints.
  # Uses the global value if -1
  max_distance: 6000
  allowed_biome_tags: # List of biome tags this plot can be created on. Takes priority over allowed_biomes.
    - minecraft:is_beach 
  allowed_biomes: # List of biomes this plot type can be created on. If it's not provided the plot type can be created on any biome.
    - COLD_OCEAN
    - DEEP_COLD_OCEAN
    - DEEP_OCEAN
    - LUKEWARM_OCEAN
    - OCEAN
    - WARM_OCEAN
```

A `waypoints.yml` like this means two plot types will be available to players, stables and seaports.

Stables
- Have a map key of `S`
- Cost $4500 to create
- Cost $450 to travel to
- Are limited to 2 per town
- Can be created only by players with the `townywaypoints.landpoint.stable` permission
- Can be created in most land biomes
- Use the max distance of 2700 from config.yml

Seaports
- Have a map key of `P`
- Cost $2500 to create
- Cost $250 to travel to
- Are limited to 2 per town
- Can be created by all players
- Can only be created in beach and ocean biomes
- Have their own max distance of 6000 seperate the one in config.yml

A player designates a plot as a waypoint by doing `/plot set <waypoint type name>`. <br/>
They can change things about the waypoint with `/twp set <open> [value]`. <br/>
Waypoints travel can be open to everyone or limited to allies, nation members, town members, or noone (closed)  with `/twp set open <status>`. <br/>
The block a player gets teleported to on traveling to a waypoint can be changed with `/twp set spawn` a waypoints default spawn is where the player was standing when they designated the plot as a waypoint.

---

### Permission nodes

- `townywaypoints.admin` Allows use of the reload command and disables all permission checks.
- Towny's `towny.command.town.set.spawn` permission node is needed for a player to set the spawn of a waypoint.
- Towny's `towny.command.town.toggle.public` permission node is needed for a player to set the open status of a waypoint.

---

### Translations

If you'd like to help translating TownyWaypoints into the available languages or add entirely new languages, [we're on Crowdin](https://crowdin.com/project/townywaypoints)!

[![Crowdin](https://badges.crowdin.net/townywaypoints/localized.svg)](https://crowdin.com/project/townywaypoints)

---

### Links

- [Hangar](https://hangar.papermc.io/ewof/TownyWaypoints)
- [Modrinth](https://modrinth.com/plugin/townywaypoints)
- [Github](https://github.com/ewof/TownyWaypoints)

---

Inspired by [TownyPorts](https://github.com/darthpeti/TownyPorts/)
