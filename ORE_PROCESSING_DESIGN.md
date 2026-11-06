# Ariente ore processing design

Implemented direction for Minecraft 1.20.1 / Forge, branch `1.20`.
The November 2026 purifier design supersedes the earlier proposal to refine raw
metals directly. Increased recovery happens when an **ore block becomes raw
metal**. Smelting remains one raw metal into one ingot.

## Material progression

| Resource | Mining | Ordinary processing | Purifier |
| --- | --- | --- | --- |
| Lithium, manganese, platinum, silver | One raw metal; Fortune increases drops; Silk Touch preserves the ore block | One raw metal or ore block smelts/blasts into one ingot | Ore blocks yield two to five raw metals by default |
| Vanilla and modded ores with raw-material forms | Existing mining behavior | Existing smelting behavior | Automatically discovered from matching ore/raw-material tags; includes deepslate variants |
| Negarite and posirite | Ore blocks | Four corresponding fuel dusts per ore block | Unsupported; fuel behavior is unchanged |
| Silicon | Silicon directly, with Fortune and Silk Touch support | Existing behavior unchanged; item ID `ariente:silicon` | Unsupported |

Ariente's four metals use the iron/gold-style vanilla Fortune rule. Fortune III
averages 2.2 raw items per ore, with a maximum of four. A one-tube purifier's
fixed yield of two is therefore an early processing option; longer lines offer
better recovery than Fortune on average. Five raw exceeds Fortune III's best
result. Silk Touch and Fortune are alternative mining routes, so Fortune does
not multiply the purifier's recovery.

No new metal dusts or intermediate inventory materials are needed. Raw storage
blocks are deferred. Ore-block furnace recipes remain available.

## Purifier installation

Build a **Purifier Start - tubes - Purifier Finish** line on one horizontal
axis. Both north/south and east/west lines work in either direction. At least
one tube is required; the default maximum is four. Each endpoint connects to
exactly one tube, and perpendicular tubes do not connect to each other.
Branches, bends, vertical lines, missing parts and overlong lines cannot run.
Tubes align with a neighboring endpoint or tube when placed against it.

- **Start:** one input stack, accepting supported ore blocks. All faces expose
  the inventory to automation. Supply both flux components here.
- **Finish:** one output stack, extractable by automation on every face.
  Automation cannot insert items here.
- **Tube:** a transparent octagonal glass shell, with a rotating particle band
  while active. Tube count determines recovery, processing time and flux cost.

Right-click either endpoint for a HoloGUI panel with its single machine slot
above the player's full inventory. Double-click valid player stacks to insert
at the Start; double-click the machine slot to collect items. Arrow buttons
also transfer stacks. Invalid input items remain visible but dimmed.

One ore is processed at a time. The ore visibly floats along the tubes and
cross-fades into the resulting raw metals across the whole journey. A short
line therefore transforms it faster. A rotating ring of red particles appears
around every active tube, perpendicular to the direction of travel. Paused
machines stop advancing the animation and emitting particles.

## Default balance

| Tubes | Raw per ore | Time | Each flux per active tick | Each flux per ore |
| --- | --- | --- | --- | --- |
| 1 | 2 | 4 seconds | 400 | 32,000 |
| 2 | 3 | 8 seconds | 800 | 128,000 |
| 3 | 4 | 12 seconds | 1,200 | 288,000 |
| 4 | 5 | 16 seconds | 1,600 | 512,000 |

Each tube adds 80 powered ticks and 400 of each flux per tick. Time and active
power both grow with length, so total flux per ore grows quadratically.
The existing power helper consumes equal amounts of negarite and posirite;
combined connectors are supported. No flux is consumed while idle or blocked.

Each generator tank supplies 1,000 of its flux per tick. Four tubes require
two tanks of each type for continuous full-speed operation. Smaller supplies
can advance intermittently using retained network flux without reducing yield.
A Smelter now consumes 200 of each flux per active tick (4,000 per ordinary
ingot at 10x speed). Two tanks per generator cover a four-tube line plus one
Smelter, even after a Power Combiner's 2% loss with its transfer rate set to 2,000.

The forcefield remains at `radius × 30` of each flux per tick (300 at radius 10),
plus 500 per panel buildup step. Its 60 panels each need 100 buildup steps,
for 3,000,000 of each flux to build a complete shield, excluding idle demand.
These construction/repair costs already make it a substantial consumer.

Existing world configs retain explicit values; set `purifierTicksPerTube = 80`,
`purifierPowerPerTube = 400` and `smelterPowerPerTick = 200` under `[power]` in
the world's `serverconfig/ariente-server.toml` to apply this balance to an
existing test world. Lower `purifierTicksPerTube` values process faster.

Server configuration under `power`:

| Setting | Default | Meaning |
| --- | --- | --- |
| `purifierTicksPerTube` | 80 | Powered traversal ticks per tube (4 seconds); lower values process faster |
| `purifierPowerPerTube` | 400 | Each flux per active tick per tube |
| `purifierMaxTubes` | 4 | Maximum supported straight-line length |
| `purifierBaseYield` | 2 | Raw output with one tube |
| `purifierExtraYieldPerTube` | 1 | Additional raw output per extra tube |
| `purifierPreferredOutputMods` | `["minecraft", "ariente"]` | Preferred raw-material provider namespaces, in priority order |

Final recovery is capped at 64 raw items. Client configuration under `purifier`
controls `ringPeriod` (20 ticks per revolution), `ringParticles` (six particles
per tube every two client ticks; zero disables them), `ringStreams` (two moving
heads) and `ringTrailTicks` (12 ticks). Sparks themselves orbit the tube, with
bright heads and fading trails, and neighboring rings have staggered phases.

## Work, interruptions and inventory safety

The ore remains in the Start's input until processing finishes and the entire
output fits in the Finish. Input consumption and output insertion happen
together on the server. The moving item is a visual representation, not a
second inventory item.

Missing power, an invalid line, or blocked/incompatible output pauses work
without consuming flux. Restoring the same line resumes it. Changing the input
material, tube count or travel direction restarts processing. Progress and the
input inventory survive saving/loading and are retained by the Start's block
loot. Finish block loot preserves its output inventory.

Every part of the line must be loaded. Detection never loads missing chunks.
Tubes hold visual snapshots only; the Start owns processing. Removing an
endpoint or losing controller updates stops stale tube animations.

## Recipes and compatibility

The purifier automatically discovers matching `forge:ores/<material>` and
`forge:raw_materials/<material>` tags. Ore tags may be attached to items or
blocks; inputs must be block items. This handles vanilla iron, gold and copper,
deepslate variants, Ariente's four metals and properly tagged ores from any
installed mod without individual recipe definitions. An ore needs neither
power nor a completed line to be inserted.

When multiple raw items share a material tag, providers in
`power.purifierPreferredOutputMods` take priority. The default prefers Minecraft,
then Ariente; remaining providers are ordered by full registry ID. Selection is
therefore deterministic on the server and client. Tag reloads invalidate the
discovery cache, and preference changes rebuild it. Ores whose tags identify
multiple different raw materials require an explicit recipe to resolve them.

The machine applies its configured raw count per ore, rather than multiplying
the ordinary mining drops. Copper therefore uses the same default two-to-five
count despite its higher normal mining yield. Raw materials and ingots cannot
be processed again. Ores without corresponding raw-material tags, including
diamond, coal, redstone, silicon and Ariente's fuel ores, remain unsupported.

Optional `ariente:purifying` JSON recipes override automatic conversions. A
recipe has an ore ingredient and one concrete raw-metal output unit, for example:

```json
{
  "type": "ariente:purifying",
  "ingredient": { "tag": "forge:ores/silver" },
  "result": { "item": "ariente:raw_silver" }
}
```

Explicit recipes take precedence over automatic discovery and can supply
missing integrations or select a particular output item. JEI combines these
overrides with automatically discovered conversions, excluding overridden
inputs from the automatic entries so its outputs match the machine.

The existing `forge:ores/<metal>`, `forge:raw_materials/<metal>` and
`forge:ingots/<metal>` item tags establish material equivalence. Ordinary metal
smelting/blasting accepts the ore and raw-material tags. JEI has a purifying
category showing the input, recovery, duration and flux cost for each length.
The existing Smelter still executes ordinary furnace recipes at their normal
yields; it does not apply increased recovery to unrelated furnace inputs.

## Blueprints

All three blocks are built through the existing Constructor/Auto Constructor
blueprint system and its normal blueprint pools.

| Block | Blueprint rarity | Ingredients |
| --- | --- | --- |
| Start | Common | Black Marble, 4 Silver Ingots, 2 Circuits, 2 Silicon, Chest |
| Finish | Common | Black Marble, 4 Silver Ingots, Circuit, Chest |
| Tube | Uncommon | 2 Advanced Circuits, 2 Flux Capacitors, 4 Platinum Ingots, 4 Lithium Ingots, 4 Manganese Ingots, 8 Glass, 8 Negarite Dust, 8 Posirite Dust |

Each extra tube therefore represents a substantial construction investment.
No Nether Stars are required.

## Validation and later work

Server checks cover all horizontal directions and default lengths, exact
recovery/time/power, invalid connections, changed lengths, interrupted work,
saving/loading, partial/full inventories, automation, and blueprints. An opt-in
client fixture exercises real powered one/four-tube lines and captures their
ore-to-raw transition, particle rings and endpoint panels.

A survival playthrough is still needed to tune blueprint availability, tube
costs and flux consumption. Copper's normal two-to-five raw drops differ from
Ariente's baseline and need consideration when tuning the shared recovery.
Additional upgrades, catalysts, branching, raw storage blocks, dust export and
unification integrations are outside this initial implementation.
