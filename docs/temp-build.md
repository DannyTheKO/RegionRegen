# Temporary Build Mode

A WorldGuard flag-based feature that creates **temporary building zones**. When a region has the `rr-temp-build` flag set to `allow`, any blocks players place inside that region automatically **decay and disappear** after a configurable time period.

## Quick Start

1. Create a WorldGuard region:
   ```
   /region define <region-name> <player> <cuboid>
   ```

2. Enable Temporary Build Mode on the region:
   ```
   /region flag <region-name> rr-temp-build allow
   ```

3. Players can now place blocks inside the region. Placed blocks will decay after the configured time.

## Configuration

```yaml
temp-build:
  # Time in seconds before placed blocks decay and disappear.
  decay-time: 30

  # Whether decaying blocks should drop items when they disappear.
  drop-items: true

  # Whether blocks in the region should be protected from explosions.
  protect-explosions: true

  # Whether fire can burn blocks in the region.
  allow-fire-burn: false

  # Whether fire can ignite blocks in the region.
  allow-fire-ignite: false

  # Whether fire and mushroom blocks can spread in the region.
  allow-block-spread: false

  # Whether players can replace non-collidable blocks in the region.
  allow-replace-non-collidable: false

  # Whether liquids can flow into/within the region.
  allow-liquid-flow: false

  # Whether players can replace liquids in the region.
  # WARNING: Setting this to false also prevents placing blocks on water.
  allow-replace-liquids: true

  # List of block materials that cannot be placed in the region.
  blocked-blocks:
    - LAVA
```

## Permissions

| Permission | Default | Description |
|---|---|---|
| `regionregen.reload` | `op` | Reload the plugin configuration |
| `regionregen.reset` | `op` | Force-regenerate all pending blocks (both regen and decay) |

## Events Handled

### `blockregen.BlockRegenListener` — Break Events
| Event | Behavior |
|---|---|
| **BlockBreakEvent** | Checks WorldGuard flags, starts regeneration task for broken blocks. Denies drops if configured. |
| **EntityExplodeEvent** | Schedules regeneration for all blocks in explosion. Denies drops if configured. |
| **BlockExplodeEvent** | Same as above for block-based explosions. |
| **BlockBurnEvent** | Cancels fire burning if `allow-fire-burn` is false. |
| **BlockFromToEvent** | Cancels lava flow into `rr-block-regen` regions if `lava` is false. |
| **BlockPhysicsEvent** | Cancels physics on blocks with active regeneration tasks. |
| **BlockPistonExtendEvent** | Cancels piston movement involving blocks in regen regions. |
| **BlockPistonRetractEvent** | Same as above for piston retraction. |

### `tempbuild.TempBuildListener` — Place Events
| Event | Behavior |
|---|---|
| **BlockPlaceEvent** | Registers placed blocks for decay. Rejects blocked blocks, solid replacements. |
| **PlayerBucketEmptyEvent** | Registers liquid/powder snow placed via bucket for decay. |
| **EntityChangeBlockEvent** | Prevents entities from changing tracked temp-build blocks. |
| **BlockFromToEvent** | Cancels liquid flow into temp-build regions if `allow-liquid-flow` is false. |
| **BlockIgniteEvent** | Cancels fire ignition if `allow-fire-ignite` is false. |
| **BlockSpreadEvent** | Cancels fire/mushroom spread if `allow-block-spread` is false. |

## Flag Coexistence with `rr-block-regen`

Both flags can be active on the same region simultaneously:

- **`rr-block-regen`** — Blocks that are **broken** (by players, explosions, etc.) regenerate after a delay.
- **`rr-temp-build`** — Blocks that are **placed** by players decay after a timer.

These flags operate on opposite triggers, so they do not conflict:
- A player **breaks** a block in a region with both flags → the block regenerates (handled by `rr-block-regen`).
- A player **places** a block in a region with both flags → the block decays after the timer (handled by `rr-temp-build`).
- An **explosion** in a region with both flags → tracked temp-build blocks are protected, other blocks regenerate.
- **Fire** in a region with both flags → burn is cancelled if either system says so.
- **Pistons** in a region with both flags → movement is cancelled.

If you want only one behavior, set only the corresponding flag:
- `/region flag <name> rr-block-regen DIRT:10` — Only regeneration, no decay.
- `/region flag <name> rr-temp-build allow` — Only decay, no regeneration.

## Example Setups

### PvP Arena — Temporary Cover

Allow players to place cover blocks that disappear after 15 seconds:

```
/region flag pvp-arena rr-temp-build allow
```

```yaml
temp-build:
  decay-time: 15
  drop-items: false
  blocked-blocks:
    - LAVA
    - TNT
```

### Creative Plot — Auto-Cleanup

Allow builders to test builds that auto-clean up after 5 minutes:

```
/region flag build-zone rr-temp-build allow
```

```yaml
temp-build:
  decay-time: 300
  drop-items: true
  allow-liquid-flow: false
  allow-fire-burn: false
```

### Mini-Game — Both Regen and Decay

A region where destroyed terrain regenerates AND placed blocks decay:

```
/region flag minigame rr-block-regen DIRT:10,STONE:5
/region flag minigame rr-temp-build allow
```

```yaml
# rr-block-regen handles broken blocks
# rr-temp-build handles placed blocks
temp-build:
  decay-time: 20
  drop-items: false
```

## Technical Details

- The plugin tracks placed blocks using a `ConcurrentHashMap`-backed `Set<TempBuildTask>` and `Set<Location>`.
- A `BukkitRunnable` is scheduled for each placed block to remove it after the configured delay.
- Bisected blocks (doors, tall grass, etc.) are handled — placing the bottom half also registers the top half for decay.
- The decay timer starts when the block is placed, not when the player leaves the region.
- Both systems share the same `excluded-blocks` list for blocks that should never be affected.
- The simplified design uses per-block scheduling rather than a central tick loop, matching the BlockRegen pattern.
