# RegionRegen
Simple WorldGuard flag that regenerates broken blocks after configured time.

**TODO**
- [ ] Paste block mode using WorldEdit or FAWE
- [X] Disable Physics Option
- [ ] Permission Bypass Mode

- Block Regen Mode
  - [X] Prevent Block Regen on player proximity mode
    - Start timer when player break
  - [ ] Prevent Block Regen if player still in region (Cooldown)
    - Start cooldown when player leave the region
    - Reset cooldown if player enter the region
  - Event
    - [X] On Break Event
    - [X] On Burn Event
    - [X] On Lava Event
    - [X] On Explosion Event
    - [X] On Piston Event

- Temporary Build Mode
  - [ ] Decay Block On Timer
    - Use onPlace event by the player
  - Prevent Place Specific Block (blacklist)
    - [ ] String Mode
    - [ ] Regex Mode
    - [ ] Block Tag Mode

**Usage**

Add flag `rr-block-regen` to a region 
- using with string syntax: `<BLOCK_TYPE>:<regeneration-delay-in-seconds>, ...`
  - WorldGuard command example: `/region flag <region-name> rr-block-regen DIRT:10, STONE:5`

- using with regex syntax: `~<BLOCK_TYPE>:<regeneration-delay-in-seconds>, ...`
  - WorldGuard command example: `/region flag <region-name> rr-block-regen ~.*_BLOCK:10, ~.*_GRASS_.*:5`

The delay is not required, ex.: `/region [...] DIRT, STONE:5`

**Features**

- Includes an anti-obstruct mechanism (block won't regenerate when there's a player on the location)

```yaml
obstruct-prevention:
  enabled: true
  radius: 1
```

- Deny the block from dropping item and exp drops.

```yaml
deny-drops: true
```

