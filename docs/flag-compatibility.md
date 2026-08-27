# Flag Compatibility — RegionRegen vs WorldGuard vs WGEFP

Sources: [`worldguard.enginehub.org/en/latest/regions/flags`](https://worldguard.enginehub.org/en/latest/regions/flags/) (WG 7.0.15+) and [`tinsware.github.io/wiki/.../flags-reference`](https://tinsware.github.io/wiki/docs/games/minecraft/plugins/worldguard-extraflags-plus/flags-reference) (WGEFP 4.4.5). Compared against RegionRegen `1.5.2`.

## 1. RegionRegen Custom Flags

| Flag | Type | File | Purpose |
|---|---|---|---|
| `rr-block-regen` | `Set<String>` `MATERIAL[:delay]` or `~REGEX[:delay]` | `RegionRegenPlugin.java:21` | Materials that regenerate after break; per-material delay overrides `events.break.default-delay` (`MaterialMatcher.java`). |
| `rr-temp-build` | `StateFlag` default `false` | `RegionRegenPlugin.java:22` | Marks region where placed blocks decay after `events.place.decay-time` (`PlaceListener.java`). |

## 2. WorldGuard Built-in Flags (71)

### Overrides

| Flag | Type | Default | Notes |
|---|---|---|---|
| `passthrough` | state | unset | `allow` = no protection. Use instead of `build=allow` which overrides lower-priority regions |
| `nonplayer-protection-domains` | set | — | Groups pistons/TNT for inter-region piston borders |

### Protection-Related (coupled with `build` flag)

| Flag | Description |
|---|---|
| `build` | Membership-based. Members can build, non-members cannot (implicit) |
| `block-break` / `block-place` / `interact` / `use` | Overrides of `build` |
| `pvp` / `chest-access` / `damage-animals` / `ride` / `sleep` / `tnt` / `vehicle-place` etc. | — |

> `block-break=deny` also blocks pistons (non-player associables). Limit to players with `-g nonmembers`.

### Mobs, Fire and Explosions

| Flag | Default | Replacement for RegionRegen |
|---|---|---|
| `creeper-explosion` / `other-explosion` / `ghast-fireball` / `enderdragon-block-damage` / `wither-damage` / `tnt` | `allow` | Alternative to `ExplosionListener` — `deny` prevents damage; RegionRegen *regenerates* after. Keep listener if regen preferred (see §4). |
| `fire-spread` | `allow` (high-freq) | **Replaces `FireListener`** (`BlockBurnEvent`). Requires `high-frequency-flags: true` in `config.yml` |
| `lava-fire` | `allow` | Lava igniting fire |
| `mob-spawning` / `deny-spawn` / `enderman-grief` etc. | `allow` | No overlap |

### Natural Events (high-freq where noted)

| Flag | Replaces RegionRegen |
|---|---|
| `lava-flow` / `water-flow` | **Replaces `LavaListener`** (`BlockFromToEvent` for lava). High-freq |
| `lightning` / `snow-fall` / `ice-form` / `leaf-decay` / `grass-growth` etc. | No overlap |

### Movement / Map Making / Misc

| Flag | Replaces RegionRegen |
|---|---|
| `pistons` | **Replaces `PistonListener`** — `deny` blocks ALL piston extend/retract in region (coarser than per-block check via `WorldGuardUtil.getRegenBlocks()`). Covers `nonplayer-protection-domains` correctly. |

## 3. WGEFP Flags (47)

| Flag | Overlap |
|---|---|
| `allow-block-break` / `deny-block-break` / `allow-block-place` / `deny-block-place` | **Not redundant** with `rr-block-regen` — WGEFP *prevents* break/place, RegionRegen *allows then restores*. Use `check-order` + `require-membership` to tune |
| `deny-item-drops` / `deny-item-pickup` | Complements `events.break.deny-drops` (per-material filter) |
| `disable-completely` / `disable-throw` / `permit-workbenches` etc. | No overlap |

## 4. Listener Matrix

| Listener | Event | Native Flag | Verdict |
|---|---|---|---|
| `BreakListener` | `BlockBreakEvent` | WG `block-break` / WGEFP `deny-block-break` | **KEEP** — unique regen + `deny-drops` + `excluded-blocks` + `obstruct-prevention` |
| `PlaceListener` | `BlockPlaceEvent` / `BucketEmpty/Fill` | WG `block-place` / WGEFP `deny-block-place` | **KEEP** — `DecayManager` decay is novel; `assign-to-decay` fixes placed→regen exploit |
| `PhysicsListener` | `FallingBlock` / `BlockFromTo(fluid)` / `BlockPhysics` | none (WG `water-flow` narrower) | **KEEP** — protects pending regen/decay locations |
| `ExplosionListener` | `EntityExplode` / `BlockExplode` | `creeper-explosion` / `other-explosion` / `tnt` / `ghast-fireball` / etc. | **KEEP** (user choice) — alternative strategy: WG `deny` vs regen. Listener is `enabled: false` by default; enable only if regen preferred over deny |
| `FireListener` | `BlockBurnEvent` | `fire-spread=deny` | **REMOVED** — use `/rg flag <region> fire-spread deny` |
| `LavaListener` | `BlockFromTo(LAVA)` | `lava-flow=deny` | **REMOVED** — use `/rg flag <region> lava-flow deny` (needs `high-frequency-flags: true`) |
| `PistonListener` | `PistonExtend/Retract` | `pistons=deny` | **REMOVED** — use `/rg flag <region> pistons deny` (blocks all pistons) |

## 5. Migration

```bash
# For each region that previously relied on the removed listeners:
/rg flag <region> fire-spread deny
/rg flag <region> lava-flow deny
/rg flag <region> pistons deny
# ensure plugins/WorldGuard/config.yml has high-frequency-flags: true for fire-spread/lava-flow
```

Remove from `config.yml`: `events.fire`, `events.lava`, `events.piston`. Kept sections: `break`, `place`, `explosion` (opt-in), `physics`.

## 6. Notes

* High-frequency flags add region-lookup cost — enable globally only if used.
* WG `build` + `pistons` interaction: pistons are non-player members; to allow pistons for specific regions use domain flags, not `-g`.
* `events.explosion` remains opt-in (`enabled: false`); if WG explosion flags are `deny`, no blocks break so regen never fires — choose one strategy per region.
