

























































# Ariente: gameplay and systems guide

Minecraft **1.20.1 / Forge**, local `1.20` development branch. Reviewed **2026-10-31**.

This is a reference for Ariente and its ArienteWorld companion. It starts from the
[original wiki](https://mcjty.eu/docs/mods/ariente), but the descriptions, numbers,
recipes, and limitations below were checked against the current source. The wiki
describes the original design; this document explains how that design maps to the
current port.

**Fresh worlds are the target.** This is an unreleased, unfinished port. A feature
described as implemented has a current code path; that does not imply a complete
survival playthrough or multiplayer audit. Concrete missing functionality is
identified alongside the affected feature and collected in [Port gaps](#port-gaps).

## Contents

- [The overall progression](#the-overall-progression)
- [Controls and holographic interfaces](#controls-and-holographic-interfaces)
- [Exploring ArienteWorld](#exploring-arienteworld)
- [The dual power system](#the-dual-power-system)
- [Blueprints and construction](#blueprints-and-construction)
- [Storage Cube](#storage-cube)
- [Holo Storage Cubes](#holo-storage-cubes)
- [Ore Purifier](#ore-purifier)
- [Smelter](#smelter)
- [Automation fields](#automation-fields)
- [Doors, keys, locks, and wireless signals](#doors-keys-locks-and-wireless-signals)
- [Forcefield Generator](#forcefield-generator)
- [Power armor, modules, and sabres](#power-armor-modules-and-sabres)
- [Transport](#transport)
- [Building materials and lighting](#building-materials-and-lighting)
- [Configuration reference](#configuration-reference)
- [Recipe reference](#recipe-reference)
- [Troubleshooting](#troubleshooting)
- [Port gaps](#port-gaps)
- [Source map](#source-map)

## The overall progression

Ariente combines exploration with technology. Ariente supplies the machines,
power system, equipment, and construction system. ArienteWorld supplies the alien
dimension, cities, inhabitants, resources, and most of the intended progression.

The intended route is:

1. Make Ariente Pearls and locate an underground entrance in the Overworld.
2. Charge its Warper, then enter the Ariente dimension.
3. Explore, fight, obtain security cards and blueprints, and collect materials.
4. Use a Constructor beside Blueprint Storage to manufacture technology.
5. Build a supply of both negarite and posirite flux.
6. Automate construction and item movement, improve equipment, and tackle larger cities.

Ore drops and furnace/blast-furnace processing, colored cable/connector crafting,
Constructor JEI and the four powered movement modules are active. A full survival
playthrough remains required, and some original recipes/content are unfinished.
Without ArienteWorld, the default settings now enable Overworld ores and dungeon
blueprint loot as an alternative progression route.

## Controls and holographic interfaces

Most machines use **HoloGui** rather than a conventional full-screen container.

| Action | Use |
| --- | --- |
| Right-click a machine | Open its hologram, or move your already open panel in front of you while keeping its pin state. |
| Aim at a hologram control and left-click/attack | Press buttons, arrows, and toggles. |
| Double-click a displayed slot | Perform that panel's slot action: craft, transfer a blueprint, or set/clear a filter. |
| Hover over controls and items | Read tooltips, recipe requirements, and status. |
| X control at the top right | Close the panel, including a pinned panel. Mounted vehicle panels have no close control. |
| Question mark, left of X | Open the relevant Tablet manual entry. Shown only when Patchouli and an entry for the panel are available. |
| Pushpin, left of the question mark (or X) | Toggle pinning for a block's panel. A green, upright pin keeps it open when you look away; unpinning restarts the normal timeout. Equipment and mounted vehicle panels have no pin. |
| Numeric settings | Click a slider track, or hold left-click and aim along it to drag. Its `<` and `>` buttons change the value by exactly one. |
| Looping-arrow button | The whole button cycles the displayed choice; icon choices carry the same marker. |
| Single versus double fuel arrows | Transfer small versus large batches; these are inventory actions rather than numeric settings. |

The hologram is a real world entity. Stand where you can see its front and aim at
the actual button. Storage Cubes additionally have direct interaction on their
four or nine front-panel item areas; see [Storage Cube](#storage-cube).

The small rectangular window controls sit against the top-right border. Pinned
panels keep their state while switching pages, and can still be closed with X
or right-click. Pinning survives chunk reloads during the current session, and
clears when the world is reopened. Removing the associated block also removes
its panel.

Machines with a redstone-mode control can cycle between ignoring redstone,
requiring no signal, and requiring a signal. This control enables a machine; it
does not supply flux. Generators, the Auto Constructor, and Forcefield Generator
use this mechanism. Check the chosen mode before diagnosing a power problem.

Default equipment bindings are:

| Binding | Default | Purpose |
| --- | --- | --- |
| Configure armor | Top-row `0` | Open configuration for worn power armor or the held Enhanced Energy Sabre / Energy Lance. |
| Module hotkey 1 | Numpad `7` | Toggle modules assigned to group 1. |
| Module hotkey 2 | Numpad `8` | Toggle modules assigned to group 2. |
| Module hotkeys 3 and 4 | Unbound | Additional configurable toggle groups. |

Check Minecraft's Controls menu for conflicts or changed bindings. A hotkey can
toggle several modules across worn armor and the held Enhanced Energy Sabre or
Energy Lance. From a piece's panel or its help page, the configuration key returns
to equipment selection; pressing it on that menu closes the hologram.

Sources: [key bindings](src/main/java/mcjty/ariente/bindings/KeyBindings.java),
[armor configuration](src/main/java/mcjty/ariente/items/armor/ArmorGui.java),
[HoloGui interaction](../ModHoloGui/src/main/java/mcjty/hologui/gui/HoloGuiEntity.java).

## Exploring ArienteWorld

### Pearls and Warpers

An Ariente Pearl has two uses:

- **Use it in the air in the Overworld** to launch a locator toward a nearby
  underground entrance. The search examines seeded entrance sites within 19
  chunks in each horizontal direction and checks for an intact Warper. It does
  not point at an Ariente city. If no entrance is found, it reports that fact.
- **Right-click a Warper with it** to add one charge, up to the configured limit.
  The default requirement is **10 pearls**. These are material charges, unrelated
  to the negarite/posirite cable network.

A successful throw consumes one pearl in Survival. After about 80 ticks of
flight the locator either drops a pearl or disappears; the current code chooses
the drop in four of five cases. Bring spare pearls for locating and charging.

The Overworld entrance is a small underground room. By default, a chunk has a
0.002 chance of being an entrance candidate. These rooms are separate from the
large cities in the other dimension.

Open the charged Warper's hologram and press **Warp**. Ariente arrival buildings
contain a return Warper that starts charged. Charge is currently an unlock
threshold, not a per-trip fuel cost: normal travel does not decrement it.
The server also checks the charge when Warp is pressed; an already open
hologram cannot bypass an entrance's current charge requirement.

The return trip first tries the remembered entrance, provided its Warper and
landing space remain usable. Otherwise it tries a valid Overworld respawn
position and then world spawn. The Warper is a dimension-travel device, not a
general menu for selecting arbitrary player-built teleport destinations.
The remembered entrance survives reconnects, server restarts and death/respawn.

### Terrain, resources, and cities

The dimension has marble terrain, floating terrain, a purple sky, and six biome
profiles: plains, hills, rough terrain, ocean, forest, and city. Forests contain
the mod's tree varieties, and the landscape includes custom grass, bushes, and
flowers. These plants are primarily scenery in the present systems.

The following are **ore placement ranges**. Heights are inclusive placement-origin
ranges; veins can extend beyond them. Ariente resource ores require a stone-tier
pickaxe. Lithium, Manganese, Platinum and Silver ores drop raw metal, with
Fortune increasing the drops and Silk Touch preserving the ore block. Negarite
and Posirite ores drop themselves for processing. Silicon Ore drops Silicon,
with Fortune and Silk Touch support.

| Resource | Placement Y range | Attempts per chunk | Main role |
| --- | --- | --- | --- |
| Negarite | 6–29 | 5 | Negarite Dust, generator and equipment fuel. |
| Posirite | 6–29 | 5 | Posirite Dust, generator and equipment fuel. |
| Lithium | 8–19 | 2 | Advanced electronics and equipment. |
| Silver | 10–19 | 4 | Common structural ingredient. |
| Manganese | 20–49 | 3 | Circuits and generator components. |
| Silicon | 30–59 | 4 | Electronics; the item is displayed as Silicon. |
| Platinum | 30–79 | 4 | Armor and advanced machines. |
| Lapis | 10–29 | 4 | Looting-related construction. |
| Glowstone | 10–29 | 3 | Lighting and night vision. |

White and blue marble deposits use Y=16–69; black marble uses Y=10–39.
The companion generates the nine listed resources in its dimension. Ariente's
optional Overworld feature generates its seven resource ores plus white marble,
using the separate [Overworld distributions](#overworld-ore-datapacks) below. It
defaults on only when ArienteWorld is absent.

There are two different kinds of city layout:

- **Authored complexes** use named plans, equipment, security, and city AI.
- **Landscape districts** form sprawling streets, buildings, parks, towers, and
  walls. There are surface districts and covered underground districts. These
  are the large city landscapes shown in the original screenshots.

The authored complex plans are:

| Plan | Footprint in chunks | Placement |
| --- | --- | --- |
| `city0_0` | 1×1 | Surface. |
| `city0_1` | 1×1 | Surface. |
| `city1_1` | 2×2 | Surface. |
| `city1_2` | 2×2 | Surface. |
| `city2_1` | 3×3 | Surface, with authored lower sections. |
| `city3_1` | 4×4 | Underground complex. |
| `city4_1` | 3×3 | Floating ship complex. |
| `city5_1` | 9×9 | Large surface complex. |

Footprints do not mean every chunk is a solid building. Layouts include empty
cells, cellars, and different part variants. Complexes use a 16-chunk placement
grid; landscape districts follow the biome/city layout. Stations and intercity
transport tunnels are a further network, with station floors near Y=30 and
beams at Y=32. Landscape district streets are built near Y=65.

### Inhabitants, alerts, and conquering a complex

| Inhabitant | Behavior |
| --- | --- |
| Soldier | Armed hostile inhabitant; can report targets to its city. |
| Master Soldier | Stronger, regenerating soldier: 150 base health, fire and magic immunity. |
| Drone | Flying combat unit. |
| Sentinel Drone | Flying observer that detects players and raises city alerts. |

On Easy, Normal and Hard, a sabre-wielding soldier can kill an unarmored player
with one hit. Masters and foot guards in advanced complexes hit harder, but a
healthy player in full unenchanted netherite can survive one melee hit.
Combat drones belonging to `city2_1`, `city3_1`, `city4_1` and `city5_1` can kill
that same player with one laser hit. Protection enchantments still reduce laser
damage. Powered Armor Upgrades provide dedicated laser protection, allowing a
full upgraded suit to survive individual hits without enchantments or a
forcefield. Bring suitable equipment and use cover before alerting these bases.

Scout patrols require at least one surviving AI core at their launch base and
at newly selected destinations. Destroying a base's last core blocks new
launches and stops other patrols choosing it as a destination. Patrols already
en route keep their assigned flight, including across save/reload, and retire
after arriving at the captured base. Nearby observers see them leave before
they disappear. Local survey loops remain available from operational bases;
launch timing and the two-scout traffic limit are unchanged.

City biomes and the interiors of every dungeon type have natural soldier/master
spawning at light levels of **11 or lower**. Outside those areas, other biomes use
Overworld-style hostile spawn tables. Natural patrols use normal hostile mob caps
and spawning distances, independently of alerts and full-dungeon loading.
Natural spawning and city AI reinforcement waves are distinct mechanisms.
Authored guards and sentinels appear when the
complex initializes; mobile soldier reinforcements deploy after an alert. The
reinforcement quota counts mobile troops belonging to that complex. Guards,
authored masters, natural spawns and neighboring-city troops do not fill it.
Not every naturally spawned soldier belongs to a
city or can drop that city's security cards.

An alert is raised by **detection or hostile interaction**, not merely standing
near a building. Sentinels check line of sight and a roughly 50-block detection
range. Creative and Spectator players are excluded from their detection; an
operational Scramble upgrade also prevents sentinel detection. Combat units can
report the players they target, and attacking city defenses can raise alerts.

While alerted, the city can spawn reinforcements and activate its shield
generators. A peaceful ship normally has **no active enclosing shield**. The
default alert timeout is `400` city-AI steps. The original core counter advances
every 11 game ticks, giving about **220 seconds** at 20 TPS. Further sightings
refresh the alert. Keep the complex's
footprint loaded: the current AI waits until its required chunks are loaded.

Destroy **every AI Core** to defeat the authored city's AI. Destroying the last
valid core produces a card carrying that city's **storage access** key and marks
the city dead. This stops that city AI; it does not erase surviving mobs or
disable natural spawning in the biome or dungeon.
City generators stop receiving free fuel when the AI is defeated. They can
continue running on their remaining dust before they run out; capturing a city
does not immediately drain every machine's power. Supply fuel yourself to keep
using its generators.

**Alarm blocks** are city-controlled status panels: Safe, Alert, or Dead. Alert
mode also produces an alarm sound. They display the AI's state rather than
acting as independent proximity detectors; simply placing a crafted Alarm does
not create a new functioning city AI.

City-associated soldiers killed by a player have a default **20%** card-drop
chance. Within that drop, the code assigns a door/lock key in 40% of cases, a
forcefield key in 40%, and no security tag in the remaining 20%. A card's city
description alone does not prove that it grants a particular kind of access.

### City authoring and administration

These operator commands are intended for a disposable authoring world. Editing
requires creative mode in the Ariente dimension. Entering edit mode restores the
selected templates into their world positions; existing blocks inside those
bounds are replaced. For a station, stand on its grid around Y=30; for a city,
stand inside its footprint. Landscape plots are restored without rotation so
their template coordinates remain consistent.

```text
/arworld editmode
/arworld syncchunk
/arworld variantcreate example 2
/arworld variantset example
/arworld variantget
/arworld variantlist
/arworld variantswitch example 1
/arworld savecity
/arworld editmode reload
/arworld editmode off
```

Stand inside the part being edited when using `syncchunk` or `variantset`.
`syncchunk` copies that part's blocks and equipment settings to every other
occurrence of the same part in the session. Synchronize the intended copy before
saving if copies differ. Variant counts are 1..16; selection indices start at 0.
Save or synchronize edits before switching templates. Other operators cannot
open an overlapping session in the same city/station region.

`savecity` exports a complete asset array to
`config/ariente/exports/<plan-or-part>.json`. `editmode reload` previews that saved
export in the edit area. Sessions end on logout or server stop, so save first.
They use private asset copies; generation changes only after you install the
export's entries in `config/ariente/userassets.json` and restart. Preserve other
entries in that file. New templates affect future generation, not existing cities.
Exports preserve native block states, palette cells, variants, metadata, facade
states and equipment/spawner settings. They do not save arbitrary inventories or
runtime security keys.

`/arworld info` reports the current city, `/arworld pacify` clears its alert, and
`/arworld citycard` gives its storage access card after the full city initializes.
These are operator tools; arbitrary player key programming remains unfinished.

### Exploration commands

These commands require permission level 2/cheats. `/arworld` is an alias for
`/arienteworld`.

```text
/arienteworld findentrance
/arienteworld findcity
/arienteworld findcity city3_1
/arienteworld findcity city4_1
/arienteworld findcity city5_1
/arienteworld findcity surface
/arienteworld findcity underground
/arienteworld findcity station
```

`findentrance` searches seeded Overworld entrance sites within about 1,024
blocks. `findcity` searches authored complexes; `surface` and `underground`
instead search landscape districts. Search results include coordinates and a
suggested teleport command. The generic command does not promise the large
landscape city nearest to you. These commands locate sites without loading all
searched chunks; the pearl separately checks actual entrance Warpers.

For direct testing:

```text
/execute in arienteworld:ariente run tp @s 0 160 0
/execute in minecraft:overworld run tp @s 0 160 0
```

Sources: [Warper](src/main/java/mcjty/ariente/blocks/utility/WarperTile.java),
[pearl](src/main/java/mcjty/ariente/items/ArientePearlItem.java),
[world generation data](../ArienteWorld/src/main/resources/data/arienteworld/worldgen),
[city AI](../ArienteWorld/src/main/java/mcjty/arienteworld/ai/CityAI.java),
[soldier drops](src/main/java/mcjty/ariente/entities/soldier/SoldierEntity.java),
[commands](../ArienteWorld/src/main/java/mcjty/arienteworld/commands/ModCommands.java).

## The dual power system

### Flux is a continuing supply, not an RF battery

Ariente has two independent power components: **negarite flux** and **posirite
flux**. Machines normally need equal quantities of both. A demand of 100 means
**100 negarite plus 100 posirite**, not a total of 100 split between them.

The usable supply is limited by the weaker component. For example, 5,000
negarite and 1,000 posirite can satisfy 1,000 units of paired demand. The excess
negarite cannot substitute for posirite.

Generators contribute supply to cable networks every tick. Consumers share the
remaining supply. Network bookkeeping retains only current/recent tick amounts;
it is not long-term energy storage. Unused production is not a battery, and a
second connector on the same network does not duplicate that network's power.
There is no implemented RF/FE conversion or wireless flux-power supply.

Some machines have short-lived internal accumulation. For example, the
Automation Field saves up operation power but loses it when its paired supply
drops to zero. The **Flux Capacitor item is a crafting ingredient**, not a
placeable battery. An **Energy Holder stores fuel dust**, not cable flux.

### Generators and tanks

Build a vertical column of matching tanks directly above its generator:

```text
Negarite Tank          Posirite Tank
Negarite Tank          Posirite Tank
Negarite Generator     Posirite Generator
```

| Property | Current behavior |
| --- | --- |
| Fuel | Negarite Dust or Posirite Dust in the matching generator. |
| Tank requirement | At least one matching tank immediately above the generator. |
| Output | **1,000 flux per tick per matching tank** of that generator's power type. |
| Column rule | Count consecutive matching tanks upward; a gap or wrong block ends the column. |
| Fuel duration | About **600 ticks / 30 seconds** per dust item while enabled. |
| Additional tanks | Increase output; the current fuel-duration counter does not scale with tank count. |
| Fuel loading | Generator hologram arrows transfer 1 or 64 dust between the player and generator; item automation is also exposed. |
| Redstone | Can disable generation through the generator's mode setting. |

Fuel can still be consumed when no tanks are installed or when consumers do not
need the output. Do not treat an enabled, fueled generator as demand-controlled.
The tank blocks are generator extensions, not conventional liquid or battery
storage. Their animations/status reflect the generator below the column.

### Cables and connectors

| Network type | Purpose |
| --- | --- |
| Negarite Cable / Connector | Carries negarite. |
| Posirite Cable / Connector | Carries posirite. |
| Combined Cable / Connector | Carries both components in one network. |
| Data Cable / Connector | A separate cable type; it is not a substitute for flux or the Automation Field. General-purpose data functionality remains incomplete. |

**Use connectors at consuming machines.** An ordinary Net Cable transports the
network but reports that it cannot supply a receiver directly. A Connector can
expose the network to an adjacent machine. Direct contact with a generator can
also expose that generator's supply, but connector-ended wiring is the useful
general pattern.

```text
Negarite Generator -- Negarite Cable -- Negarite Connector --+
                                                          | Machine
Posirite Generator -- Posirite Cable -- Posirite Connector-+
```

The two connectors touch different faces of the same machine. Keep cable types
appropriate to their network. Do not assume that putting differently colored
cables together converts or merges the power types.

For a compact test, power an Auto Constructor with one generator and one tank
for each component. Supply its other requirements separately. Adding more tanks
or parallel generators increases supply; it does not add a permanent reserve.

### Power Combiner

The combiner receives paired power from **separate negarite and posirite
networks** and sends both components to adjacent **Combined Connectors**. It
does not use a combined network as its own input. Put a Combined Connector
against the combiner's output side/network, then run combined cable to a
Combined Connector touching the consumer.

The hologram sets the input transfer amount, from **0 to 20,000**. The current
default is **100** input and **98** output. Check **In** and **Out** in the
panel when tuning it. The implementation looks for adjacent connectors on all
six faces; it does not reserve one particular facing as a mandatory output port.

The implementation outputs `floor(input × 0.98)` of **each** component. Thus a
1,000 input consumes 1,000 negarite and 1,000 posirite, and supplies 980 of each
to the combined side. Multiple adjacent Combined Connectors divide this output;
they do not each receive the full amount. The older design/history includes
different claims about loss; **2% is what the current implementation does**.

### Facades

Use a **Facade** item on a normal block to copy its block state. Use that facade
on a cable or connector to cover it while retaining the network. A facade
already covering a cable is not replaced by simply applying another one.
Breaking a covered cable removes its facade first and keeps the cable and its
network. Survival drops the configured facade; creative removes it without a
drop. Break the exposed cable again to remove it. The facade item and its tooltip
show the selected block state.

Sources: [power accounting](src/main/java/mcjty/ariente/power/PowerSystem.java),
[receivers](src/main/java/mcjty/ariente/power/PowerReceiverSupport.java),
[generator](src/main/java/mcjty/ariente/blocks/generators/NegariteGeneratorTile.java),
[combiner](src/main/java/mcjty/ariente/blocks/generators/PowerCombinerTile.java),
[cable endpoints](src/main/java/mcjty/ariente/cables/NetCableTileEntity.java),
[facades](src/main/java/mcjty/ariente/facade/FacadeItemBlock.java).

## Blueprints and construction

### Blueprints

A Blueprint is an item encoding a **destination item stack**, not a freely
programmable crafting pattern. The Constructor finds the registered recipe for
that destination. Hovering a populated blueprint displays its result and
ingredients. A blank blueprint without destination data is not a recipe.

Blueprints are intended exploration rewards. City loot can contain explicit
blueprints or choose weighted random entries from the blueprint recipe catalog.
The catalog's rarity weights are relative selection weights, not independent
drop probabilities: Common=1, Uncommon=0.1, Rare=0.01, Extreme=0.001. A city's
loot settings separately decide whether and how often to choose a blueprint.

The active Constructor logic uses the hardcoded catalog in
`BlueprintRecipeRegistry`. The `ariente:constructor` recipe serializer exists,
but adding arbitrary JSON recipes is not equivalent to adding them to this
catalog. See the [recipe reference](#recipe-reference).

### Blueprint Storage

Blueprint Storage has an ordinary crafting recipe, so the first library does not
require a Constructor or another blueprint library. Use two Silver Ingots, two
Silicon, a Chest, a Glass Pane and three ordinary Marble blocks (colors may mix).
The existing Constructor recipe remains available too.

Blueprint Storage holds **six blueprint items**. Place it directly against a
Constructor or Auto Constructor, on any of the six faces. Those machines query
adjacent storage; there is no general wireless blueprint library.

Its hologram transfers blueprints between the player's inventory and the six
storage slots. Stored blueprints appear above your inventory, which uses three
nine-slot rows and a separated hotbar. Your whole inventory stays visible in its
actual positions. Blueprints are bright; other items are dimmed and cannot be
selected or transferred. Hover still identifies them. Double-click a blueprint
to transfer it.
Installed blueprints are recipe references and are **not consumed by crafting**.

The creative tab and JEI include one populated Blueprint per catalog recipe.
JEI shows the required blueprint alongside the ingredients and full output. A populated Storage Cube blueprint can
be made with this permission-level-2 command in **1.20.1**:

```text
/give @s ariente:blueprint{destination:'{"item":"ariente:storage"}'} 1
```

Replace the item ID with another valid catalog destination. The JSON stored in
`destination` uses `item` and, for multi-item results, `amount`. This is a
development aid, not a survival blueprint-writing mechanic.

### Constructor: manual crafting

1. Place a Constructor beside at least one Blueprint Storage.
2. Insert the desired populated blueprint into that storage.
3. Carry the required ingredients in your player inventory.
4. Open the Constructor hologram. Its upper area shows available blueprints/results;
   its lower area shows your whole inventory, with a separated hotbar. Needed
   ingredients are bright; other items are dimmed and cannot be selected.
5. Hover a result to see missing ingredients. **Double-click its blueprint** to
   craft. The machine consumes ingredients from your inventory and puts the
   result into your inventory, dropping it if necessary.

The manual Constructor does **not** require cable power. It does not draw its
ingredients from arbitrary neighboring chests. Missing ingredients and missing
adjacent blueprints are separate problems.

### Auto Constructor

The Auto Constructor uses the same neighboring blueprint library, but keeps
ingredients and products inside the machine:

| Property | Value |
| --- | --- |
| Ingredient slots | 18. |
| Output slots | 6. |
| Running power | 10 of **each** component per powered tick. |
| Additional craft cost | 100 of **each** component per successful craft. |
| Recipe selection | Cycles through slots in adjacent Blueprint Storage blocks. |
| Timing | Normally checks another slot every 10 powered ticks; empty blueprint slots use a shorter delay. |

The Ingredients page shows machine slots above your whole inventory, in three
nine-slot rows and a separated hotbar. Ingredients used by adjacent blueprints
are bright; other items are dimmed and cannot be selected or transferred. Hover
still identifies dimmed items. Double-click a bright stack to transfer it, or
select it and use the arrows.

Supply ingredients, remove products, attach power connectors, and set its
redstone mode appropriately. This is a cycling crafter, not a demand-driven
recursive autocrafting network. It does not automatically manufacture missing
subcomponents or fetch supplies from all nearby inventories.

New and existing output stacks receive the full recipe batch. The machine needs
space for the whole batch in one output slot and enough crafting power before it
consumes ingredients. Repeated ingredients reserve each input item only once.

Sources: [blueprint data](src/main/java/mcjty/ariente/items/BlueprintItem.java),
[catalog](src/main/java/mcjty/ariente/recipes/BlueprintRecipeRegistry.java),
[manual Constructor](src/main/java/mcjty/ariente/blocks/utility/ConstructorTile.java),
[Auto Constructor](src/main/java/mcjty/ariente/blocks/utility/AutoConstructorTile.java),
[Blueprint Storage](src/main/java/mcjty/ariente/blocks/utility/BlueprintStorageTile.java).

## Ore Purifier

Collect **metal ore blocks with Silk Touch**, then build a
**Purifier Start - tubes - Purifier Finish** line. Vanilla iron, gold and copper,
their deepslate variants, and modded ores are detected automatically when their
material tags identify a matching raw material. Keep the line straight
and horizontal, with one to four tubes between the endpoints. Each endpoint
connects to exactly one tube, in any horizontal direction. Tubes align with an
adjacent endpoint or tube when placed against it; they do not form bends.

| Tubes | Raw metals per ore | Time per ore | Each flux per active tick | Each flux per ore |
| --- | --- | --- | --- | --- |
| 1 | 2 | 4 seconds | 400 | 32,000 |
| 2 | 3 | 8 seconds | 800 | 128,000 |
| 3 | 4 | 12 seconds | 1,200 | 288,000 |
| 4 | 5 | 16 seconds | 1,600 | 512,000 |

These are defaults. Supply **both fluxes at the Start**. Every extra tube adds
80 powered ticks and 400 of each flux per active tick. An ore floats through
the transparent tubes, gradually fading into raw metal. Each working tube has
its own red particle streams orbiting with bright heads and fading trails.
Smelt or blast the resulting raw metals into ingots normally; there is no dust
intermediate.

One tank on each generator supplies 1,000 of each flux per tick. A four-tube
line needs **two tanks of each type for full speed**. With fewer tanks, the line
can use retained flux to advance more slowly, keeping its full raw-metal yield.
Two tanks of each type can sustain a full-speed four-tube line and one Smelter.
If using a Power Combiner, set its transfer rate to 2,000; after its 2% loss,
1,960 of each flux reaches the combined network against 1,800 consumed.

Right-click either endpoint for its hologram: a **single stack above your full
inventory**. Double-click a bright ore stack to insert at the Start, and
double-click the upper slot to retrieve its contents. Arrow buttons also
transfer items. Automation can insert at the Start and extract at the Finish
on every face; the Finish rejects insertion. Raw metals and fuel ores are not
purifier inputs. The purifier matches `forge:ores/<material>` item or block
tags to `forge:raw_materials/<material>` item tags. Insertion needs neither
power nor a completed line; both are only required for processing. Ores without
raw-material forms, such as diamond, coal and redstone, are not supported.

When several mods provide the same raw metal, `power.purifierPreferredOutputMods`
selects providers in priority order (default: `minecraft`, then `ariente`).
Other providers are sorted by registry ID for consistent results. Pack makers
can use `ariente:purifying` JSON recipes to override automatic conversions.
JEI shows the same effective conversions as the machine. The configured two-to-five
recovery is a fixed raw count per ore, including copper; it does not multiply
the ore's ordinary mining drops.

The Start keeps the ore until its complete raw-metal output can be delivered.
Missing flux, a broken line, or insufficient/incompatible output space pauses
processing without consuming flux or losing the ore. Progress survives saving
and loading. Changing the ore type, tube count or line direction restarts work.
All parts must be loaded; the purifier does not load chunks itself.

All three blocks have Constructor blueprints. Start and Finish use common
blueprints; each Tube uses an uncommon blueprint and costs **2 Advanced Circuits,
2 Flux Capacitors, 4 Platinum Ingots, 4 Lithium Ingots, 4 Manganese Ingots,
8 Glass, 8 Negarite Dust and 8 Posirite Dust**. No Nether Star is required.
JEI shows the blueprints and ore-purifying recipes, including yields and costs
for each available length.

Configure `power.purifierTicksPerTube`, `purifierPowerPerTube`,
`purifierMaxTubes`, `purifierBaseYield`, and `purifierExtraYieldPerTube` in the
world's `serverconfig/ariente-server.toml` under `[power]`. Timing defaults to
`purifierTicksPerTube = 80` (4 seconds per tube); lower values process faster.
Existing worlds retain their saved setting, so change it to 80 to use the new
default there. Client settings `purifier.ringPeriod`, `ringParticles`,
`ringStreams` and `ringTrailTicks` control orbit speed, density, stream count
and trail lifetime. Recovery is capped at one normal stack.

Source: [Purifier](src/main/java/mcjty/ariente/blocks/purifier/PurifierTile.java).

## Smelter

The **Smelter** runs furnace smelting recipes using Ariente power. Its recessed
black marble front shows **input slot / yellow lightning / output slot**. Each
slot holds one normal stack. It has no GUI and needs no furnace fuel.
The front has an animated glow while the machine is actively smelting.

- Right-click the left slot with a smeltable item to insert the held stack.
- Left-click either slot to retrieve one item; sneak-left-click retrieves up to
  a stack. Empty-hand right-click also retrieves items.
- Automation inserts through the **top, left, right, and front** faces and
  extracts through the **bottom and back**. Front/back follow the block's facing.
  Input faces reject extraction; output faces reject insertion.

Supply both Negarite and Posirite flux, either separately or through a combined
connector. Smelting defaults to **10 times the speed of a Vanilla furnace**: a
standard 200-tick recipe takes **20 powered ticks (1 second)**. By default,
each active tick consumes **200 of each flux**, or 4,000 of each for a standard
recipe. Set `power.smelterSpeed` and `power.smelterPowerPerTick` in the server
config to adjust the speed multiplier and per-tick cost independently.
No power is consumed while idle or when the output is blocked. Progress pauses
when power or output space is unavailable, and survives saving and reloading.
Changing to a different recipe resets progress.

Craft it with a **common Smelter blueprint** in a Constructor or Auto Constructor:
Black Marble, 3 Silver Ingots, Circuit, 2 Silicon, and a Furnace. The populated
blueprint works through adjacent Blueprint Storage like other machine patterns.

In Creative mode, left-clicking either front slot extracts items without breaking
the Smelter, even when the slot is empty. Holding that click and moving off the
slot stays protected until you release it. Start a fresh click on the casing to
break the block normally.

Source: [Smelter](src/main/java/mcjty/ariente/blocks/utility/SmelterTile.java).

## Storage Cube

A Storage Cube holds **four item types**, shown in four areas on its front.
Each type has **64 stack slots**: for a normally stackable 64-item material that
is **4,096 items per type**, or 16,384 across all four types. Actual capacity
depends on that item's stack limit.

The **Storage Cube (3x3)** holds **nine item types** in a 3x3 front-panel grid.
It keeps the same 64-stack capacity per type, up to **36,864 items** across all
nine areas for items stacking to 64. The original four-type cube remains available.
Both variants use the same controls, locking, item automation and six-way rotation.

Craft the 3x3 cube with its own populated blueprint in adjacent Blueprint Storage
using a Constructor or Auto Constructor. It needs **1 Black Marble, 6 Silver
Ingots, 1 Circuit, 2 Chests and 9 Glass Panes**. The blueprint stays in storage;
JEI shows the recipe and blueprint.

| Front-panel action | Result |
| --- | --- |
| Right-click an item area while holding an item | Insert the held stack into that type's storage. |
| Double right-click an established item area | Insert matching items from your inventory. |
| Left-click an item area | Extract one item. |
| Sneak-left-click an item area | Extract up to one normal stack. |
| Interact with a locked cube | Open its access hologram instead of transferring normally. |

The displayed icons and totals are synchronized to clients. Item automation sees
the grouped inventory through an item-handler capability. Locked cubes block
automation insertion and extraction on every face. A locked cube is an
access lock, not the drawer-style feature that remembers a filter in an empty
slot. City Storage Cubes can contain loot and require that city's storage key.

Present a matching Key Card directly to the cube to toggle its lock. The final
AI Core card supplies storage access; an ordinary door-access card is not the
same key. The present UI is not a complete player key-programming system.

In Creative mode, slot clicks on both the 2x2 and 3x3 cubes extract items without
breaking the block, including empty slots. A held slot click stays protected if
you drift onto the casing. Release it and start a fresh casing click to break
the cube normally.

Source: [Storage Cube](src/main/java/mcjty/ariente/blocks/utility/StorageTile.java).

## Holo Storage Cubes

Holo Storage Cubes are glass devices with **30, 60 or 120 ordinary stack slots**.
They accept any mixture of items, respecting each item's normal stack limit.
All six faces allow insertion and extraction through the item handler capability.
Locking blocks both automation operations, as well as player inventory transfers.
They need no flux, and breaking a cube preserves its inventory in the dropped item.

Right-click to open the holographic inventory. The upper grid shows a window of
nine columns by three rows from storage. The lower grid shows the player's main
inventory in three rows of nine, followed by the hotbar, with a separator between
the storage and player sections. Point at either vertical
side bar beside storage to scroll left or right one column at a time; no click is
needed, and scrolling continues while pointing at the bar. Click an arrow to jump
three columns, or hold it for faster scrolling.

Beside the **Storage** title, **A-Z** sorts the stored stacks by item name and
**Count** sorts them from largest to smallest, using item names to break ties.
Empty slots move to the end. Sorting preserves each stack and its item data,
leaves your inventory unchanged, and returns the view to the first column.

Each player has their own panel for a block. Right-click the block again to
reposition your existing panel; its pin remains as you set it. Multiple visible
storage panels continue updating independently, even when another panel closes.

- Hold left-click on a stack, aim at another slot and release to move it.
- Alternatively, click a source and then click its destination.
- Sneak-click a stack to transfer it immediately to the other inventory.
- Matching stacks merge up to their stack limit; full stacks can swap with different items.
- Click the selected source again to cancel. Right-click the panel or click **X** to close.
- Click **?** at the top-right to open the Holo Storage Cubes manual entry.

Items remain in their source until a transfer completes. A changed source or
destination cancels the move safely; this also protects transfers while automation
or another player is using the cube. Leaving or closing a panel during a drag
cannot strand an item on a cursor.

Up to eight different item representations drift slowly inside the glass. Each
lasts about five seconds, fading at both ends. Item counts weight their chance of
appearing. Animation and scrolling run locally. Nearby clients receive at most
sixteen weighted candidates, refreshed when contents change and every five seconds
for inventories with more item types. Actual inventory changes go only to active
panel viewers, as changed slots at most four times per second while idle.

All tiers have Constructor blueprints, with common, uncommon and rare blueprint
rarities respectively. Higher tiers use substantially more advanced materials.
Each is built from raw materials, so crafting never consumes a filled lower-tier
cube. See the recipe table below or JEI.

## Automation fields

### Area and node placement

An **Automation Field** controller moves items between inventories using small
multipart nodes. It does not move items through a network of item pipes.

Field Markers form a horizontally connected area at the controller's base
height. Place the controller next to a connected marker, and lay markers under
every column where nodes should participate. A filled rectangle is a convenient
layout. An outline alone does not make unmarked interior columns active: the
controller scans **above marker positions**, not every column inside the drawn
bounding box.

Set the field height in the controller's hologram; the adjustable range is
**1–256**. Nodes are discovered from the marker level through that height above
it. The controller offers outline and item-transfer display toggles. These are
display settings, not transfer permissions. Item-transfer animation is still
partly unported, so an invisible animation is not proof that no item moved.

Nodes attach to inventory faces, and several parts can share a block space.
Their orientation selects which adjacent inventory face/capability they access.
Keep the supporting field markers connected and the nodes inside the marked
columns. Hoppers and other automation still need an inventory that exposes a
compatible item handler on the attached face.

### Node roles

Names are from the **attached inventory's** point of view:

| Node | Role |
| --- | --- |
| Output Item Node | Extracts items **out of the source inventory** into the field. |
| Input Item Node | Inserts items **into the destination inventory** from the field. |
| Round Robin Node | Modifier on the back slot corresponding to an Output Node; rotates among eligible destinations. |
| Sensor Item Node | Counts items in its attached inventory and produces a colored boolean condition. |
| Field Marker | Associates its vertical column with the controller. It is not an item source or destination. |

### A minimal item-transfer example

1. Place two chests within the configured height and lay connected markers under
   the spaces where their attached nodes will sit. Marking only the chest
   columns is insufficient if the nodes occupy neighboring block spaces.
2. Attach an **Output** node to the source chest and an **Input** node to the
   destination chest.
3. Open each node's hologram and add the desired item to its filter by
   double-clicking that item in the lower player inventory. This uses three
   nine-slot rows and a separated hotbar, matching Holo Storage. Removing a
   displayed filter clears that filter entry. These are filter samples, not
   supplies for the transfer.
4. Supply the controller with both power components, using connectors.
5. Add matching items to the source chest and keep room in the destination.

Both producer and consumer lists are built from their filters. An empty filter
is **not** an automatic allow-all configuration. The Output Node's stack-size
setting is adjustable from **1 to 64**; it acts as a minimum batch size, so a
source holding fewer items can wait rather than transfer a partial batch.

The old **Ore Dictionary** icon now enables **material tag matching** at either
node. It matches resource-specific Forge tags such as `forge:ingots/silver`,
vanilla material families such as planks/logs/wool, and shared tags from other
mods. Broad tags such as `forge:ingots` do not make iron and gold interchangeable.
With tag matching disabled, filters require the same item. Damage and NBT flags
are independent at both ends; NBT matching excludes durability because the
damage flag controls that separately. Filter stack counts do not affect matching.

Round-robin modifiers fit behind Output Nodes and distribute batches among
matching destinations. Breaking a node selects the visible part and keeps other
parts in that block; its drop retains configuration. The hologram targets the
selected node, including when switching to Help. Transfer animations are sampled
visual hints; inventory counts establish what actually moved. Rejected inserts
return leftovers to the source.

### Sensors and conditions

A Sensor Item Node compares the **total item count** in its attached inventory
against a number, using `<`, `<=`, `>`, `>=`, `=`, or `<>` (not equal). It does not
currently provide an item-specific sensor filter. Choose its output dye color.

Other nodes can require up to **two positive** and **two negative** colored
conditions. Positive conditions must be true; negative conditions must be false.
If several sensors output the same color, any true sensor makes that color true.
These colors are field-local logical conditions, unrelated to flux cable colors
or numbered wireless redstone channels.

For example, a sensor on a destination inventory can output green when its
count is below a threshold. Requiring green on the source's Output Node makes
the transfer a refill system. Remember that this counts all items in the sensed
inventory, not just the transferred material.

### Power and throughput

With default settings:

- The controller can accumulate up to **400 paired units per tick**.
- Its internal reserve is capped at **3,200 paired units**.
- Moving a stack of `n` items costs **100 + 2 × (n − 1)** paired units.
- A single item costs 100; a 64-item batch costs 226.
- It can make several transfers per tick if enough reserve and valid work exist.
- If paired network supply falls to zero, the accumulated reserve is discarded.

The existing field supports item transfer and item-count conditions. It does not
provide fluid, RF, or generic energy automation.

Sources: [controller](src/main/java/mcjty/ariente/blocks/utility/autofield/AutoFieldTile.java),
[producer matching](src/main/java/mcjty/ariente/blocks/utility/autofield/ProducerInfo.java),
[consumer matching](src/main/java/mcjty/ariente/blocks/utility/autofield/ConsumerInfo.java),
[sensor](src/main/java/mcjty/ariente/blocks/utility/autofield/SensorItemNodeTile.java),
[node orientation](src/main/java/mcjty/ariente/blocks/utility/autofield/NodeOrientation.java).

## Doors, keys, locks, and wireless signals

### Door Markers

Place Door Markers across the floor of an opening. Each creates invisible door
blocks upward until it reaches a solid obstruction or the configured maximum:
**six segments above the marker** by default. The visible panels animate open
and closed; their collision follows the open/locked state.

An unlocked door opens for nearby **players or soldiers**, including players in
Creative. The detection box extends three blocks horizontally in each direction,
two below the marker, and six above it. A locked door stays closed. Door
proximity is unrelated to the city alert system's player-detection rules.

The marker's facing is the panel's normal, perpendicular to the opening's
width. Generated wide doors derive that orientation from their authored row of
markers before rotation/mirroring. Manually placed markers retain their placed
orientation. Keep an appropriate ceiling above them so the generated door does
not extend into unwanted open space.

### Key Cards and city security

A Key Card can carry one or more security tags. Different permissions use
different tags: door/lock access, storage access, and forcefield immunity are
separate. A printed city description is a label, not the permission itself.

Use a card on a block with a card slot, such as a Lock or Storage Cube. Matching
tags toggle access; incorrect tags produce a rejection sound. Forcefield
immunity instead checks for a suitable card **in the player's inventory**.
There is no complete survival UI here for creating arbitrary personal security
tags or cloning a city's keys onto blank cards.

### Lock

A Lock controls **Door Markers** and unkeyed **Storage Cubes**, including Holo
Storage Cubes, within a configurable box around itself. A storage device with
its own key only accepts a controller with that same key; an unrelated Lock or
Wireless Lock cannot override city storage access. The
current ranges are horizontal **1–20**, vertical **1–10**. This is not a general
lock for every inventory or machine in the area. A matching key card toggles
the Lock and its affected doors. A locked Lock's normal hologram denies access;
unlock it with a valid card before changing its ranges.
Its initial horizontal/vertical radii are **5/3** blocks.

City initialization assigns city locks their door-access key and locks them.
Avoid overlapping lock controllers when experimenting: each writes the nearby
door state rather than negotiating a combined permission policy.

### Wireless redstone

Numbered wireless channels store a signal value. They are **control signals**,
not wireless flux power. Use one clear source for a channel: multiple writers
write the same channel value rather than forming a documented signal-combining
network.

The channel registry is stored with the Overworld and shared across dimensions;
there is no distance-based radio range in this implementation. Devices still
need loaded chunks to update their inputs/outputs. Channels are not chunk loaders.

| Block | Function |
| --- | --- |
| Signal Transmitter | Publishes its redstone input on its channel. |
| Signal Receiver | Outputs the current value of its channel as redstone. |
| Wireless Button | Click to toggle its channel between 0 and 15; it is a latching switch, not a timed vanilla-button pulse. |
| Wireless Lock | Locks nearby Door Markers at channel value 0; unlocks them for a positive value. An absent channel value is treated as locked. |

To link devices, hold the relevant wireless block item:

1. **Right-click** an already placed channel device to copy that device's
   channel into the held item. A channel is allocated if needed.
2. Place the held device to use that channel.
3. To change an existing device, **sneak-right-click** it with the item carrying
   the desired channel. This writes the held channel to the placed device.

The message prints the channel number. This copy/write distinction matters:
normal right-click reads from the placed device; sneaking writes to it. Use an
empty/non-linking hand to operate a Wireless Button instead of accidentally
copying its channel. City-generated devices receive mapped channels during
initialization.

Sources: [doors](src/main/java/mcjty/ariente/blocks/utility/door/DoorMarkerTile.java),
[lock](src/main/java/mcjty/ariente/blocks/utility/LockTile.java),
[cards](src/main/java/mcjty/ariente/items/KeyCardItem.java),
[channel linking](src/main/java/mcjty/ariente/blocks/utility/wireless/SignalChannelTileEntity.java),
[wireless lock](src/main/java/mcjty/ariente/blocks/utility/wireless/WirelessLockTile.java).

## Holographic displays

Use the reusable **Holographic Display Base** blueprint in a Constructor or Auto
Constructor to make a base. Place
bases side by side at the same level and facing the same direction: they share
one vertical display, beginning directly above the thin plate in its block space.
Right-click any base to set the common height (1–10 blocks) and appearance.
The surface button cycles **Invisible**, **Transparent** and **Solid**. **Color...**
shows the selected color and opens a color picker; choosing a shade or pressing
its upper-right **x** returns to base settings. The initial color is the same pale
gray as the original transparent surface. Transparent mode adds a
**Transparency** slider from **0%** (opaque) to **100%** (fully clear, including scan
lines); the default is 87%. Color and transparency choices are remembered when
switching modes.

**Border** is a separate on/off cycle, enabled by default. It keeps the illuminated
frame even with an invisible background; disable it for floating modules without
a frame. All appearance settings apply across the joined display. Existing saved
presets retain their color and visible frame when converted to these controls.
Displays currently need no power. Height and transparency use the shared sliders
with precise arrow controls. Shrinking a display is refused if it would cut off
an installed module; recover those modules first.

Power, Storage and Text display modules have Constructor recipes and corresponding blueprints:

- **Power:** sneak-right-click an Ariente generator, tank, cable or powered machine
  to link it. Choose network flux, consumption per tick, generation per tick and
  fuel remaining, with numbers, a supply meter or a 32-second history. Ariente's
  tanks are generation chambers; the display reports actual network flux rather
  than a fictional energy buffer. Unsupported metrics appear as `n/a`.
- **Storage:** sneak-right-click an inventory to link the clicked side. Choose a
  list of the most stored items or an item grid, the number shown, columns and
  page. Matching items and tags are combined into stack totals. Lists show up to
  16 types; grids page through all types with up to 64 entries per page, subject
  to the display's height.
- **Text:** right-click to configure text, size and color. Left-click the text
  area to type multiple lines with normal selection, navigation, deletion and
  clipboard controls. Escape or clicking outside the editor commits the edit and
  returns control to the hologram. **Color...** opens a hue/shade picker; choosing
  a shade or pressing its upper-right **x** returns to text settings.

**Alien Text Modules** are decorative salvage with sixteen unreadable glyphs.
They have no crafting recipe or blueprint. Find them in ArienteWorld's city
buildings, floating dungeons, huge cube control desks and underground console
banks in newly generated ruins. Recover an installed module by sneak-right-clicking
it, or break its base to drop it. Symbols
and color stay with the item. Configure size and color as for ordinary text;
`0–9` and `A–F` select the sixteen glyphs, and other letters become alien symbols.
Spaces and line breaks remain blank.

For a first display, place two bases facing the same way and set their height to
two blocks. Link a Power module to a nearby generator, choose its readings, and
install it where the preview is green. Add a Text module for a label. Briefly
right-click an installed module to edit it, or hold and aim to move it. Keep its
footprint within the surface and clear of the other modules.

Right-click a held module to configure it before installation. Sneak-right-click
in the air clears its link. Aim at the front of a display: the module's rectangle
snaps to a small grid and turns green when it fits or red when it overlaps or
extends beyond the available surface. Right-click to install. The base under its
top-left corner owns it, including modules spanning several bases. Sneak-right-
click an installed module to recover its configured item. Press and release
right-click without dragging to open an installed module's settings, even with
empty hands. Hold right-click and aim to drag it, including across joins between
bases on the same screen. Its outline turns green where it fits and red where it
overlaps or leaves the surface. Release to place it; an illegal drop snaps back to
its previous position. Dragging never opens settings on release, even after
moving back to the starting position. Edits apply in place. If the new footprint
overlaps another module or extends beyond the display, the edited module is
returned to the player (or dropped nearby when inventory is full) with its updated
settings. Breaking a base also
returns modules that lose their supporting surface. Background visibility does
not affect modules; invisible surfaces can still be targeted.

Readings refresh once per second near players. Inventory snapshots are shared
between modules. Only changed module readings are synchronized to players tracking
the base; text and configuration are sent separately when the layout changes.
Unloaded targets are never force-loaded and show an offline status. Current
modules are read-only; the dispatch API already distinguishes left and right
screen clicks for future interactive modules.

## Forcefield Generator

The Forcefield Generator builds a spherical, triangular-panel shield around
itself. Its hologram controls **radius**, displays **field integrity**, and sets
its redstone mode.

| Setting or behavior | Default/current value |
| --- | --- |
| Radius | Starts at 10; GUI range 3–50 blocks. |
| Idle demand | `radius × 30` of each power component per tick. Radius 10 needs 300 of each. |
| Panels | 60 independently maintained panels. |
| Buildup | 100 successful buildup steps per panel. Each step costs another 500 of each component by default. |
| Finished panel life | 100. |
| Projectile hit | Intercepted projectiles reduce panel life by 10 in the present implementation. |
| Destroyed-panel delay | 100 ticks before rebuilding can begin. |
| Living-entity contact damage | 19 damage before other damage handling, configurable. |

Supply enough flux for **idle plus construction/repair**. A generator able to
pay idle usage can still rebuild slowly if little supply remains. A brief
startup/low-power grace period exists; sustained loss of power breaks down the
shield. Changing radius resets the field and requires rebuilding.

The shield intercepts projectiles and damages entities crossing active panels.
Destroyed panels create temporary holes, and destruction can cause an explosion.
It is not an ordinary solid-block wall or an unconditional invulnerability dome.

In generated cities, AI alert state switches shield generators on and off.
Players carrying the city's forcefield-immunity card avoid the city shield's
contact damage. City units can also have explicit immunity. The current player
generator GUI does **not** offer the old wiki's implied general-purpose interface
for assigning your own immunity key.

Source: [Forcefield Generator](src/main/java/mcjty/ariente/blocks/defense/ForceFieldTile.java).

## Power armor, modules, and sabres

Power armor has no durability and cannot wear out in combat, even with an empty
Energy Holder. Fuel powers its upgrade modules.

### Operator loadout

Run `/ariente equip` as a player with permission level 2 (operator/cheats) to
equip a full power suit and an Enhanced Energy Sabre. The hotbar also receives
an Energy Lance, an Energy Holder with one million fuel pairs and a full current
charge, and a fully charged Flux Ship. The off hand is cleared for lance use.
Replaced items return to the inventory or drop nearby when there is no room.

Every armor piece has active Energy Efficiency and Armor Improvement modules.
The boots also have Feather Falling; the chest has active Regeneration and an
installed, **disabled** Forcefield. Configure modules with the usual armor key.
Enabling that Forcefield alongside Armor and Regeneration uses **120/130** power
with Energy Efficiency, so all three abilities can operate together.

### Configuration and fuel

Wear the power armor piece, or hold the Enhanced Energy Sabre or Energy Lance in your main hand,
and press the configuration binding (default **0**). Select the equipment piece.
Its panel lets you:

1. Install a module from your inventory by clicking its module icon.
2. Enable/disable an installed module with its activation control.
3. Assign a module hotkey group.
4. Check the shared Energy Holder charge at the bottom of the panel.
5. Remove installed modules back into your inventory.

The columns are labeled **Mod**, **On** and **Key**. Module tooltips give a short
effect, compatible equipment and usage. The **Ariente Tablet → Equipment** entries
contain the full mechanics; the tables and explanations below provide the same
reference here. A panel's question mark opens the relevant Tablet entry when
Patchouli is available.

Modules fill two aligned columns from the top. Installed icons are bright.
Available inventory modules are dim;
unavailable modules are darker still. Removing a module dims its icon again.

Installing and activating are separate. An installed, disabled module should
not be counted as an active ability. Fuel and the current pair’s charge live in
one carried **Energy Holder**, shared by worn armor and the held modular weapon.
Module budgets remain **per equipment piece**.

Each active module contributes a usage number. A piece normally supports a
total of **100**. An enabled Energy module raises this to **130**. Exceeding the
piece's budget prevents its powered module updates. Modules also need the shared
holder: one Negarite Dust plus one Posirite Dust funds approximately
`30000 / usage` ticks for a single active piece,
or `40000 / usage` with Energy enabled, at the default settings.

Each active piece draws its fraction of a fuel pair every tick. Their draws add up;
unused equipment consumes nothing and keeps no personal fuel reserve. Removing the
holder stops powered modules even if its current pair still has charge.

For example, total usage 50 lasts roughly 600 ticks (30 seconds) per dust pair, or
800 ticks (40 seconds) when all participating pieces have Energy enabled. This
is not 50 dust items per tick. Energy's default benefit is a **100→130** budget
and **30000→40000** fuel-duration numerator on the piece carrying it: 30% more
module capacity and about one-third longer fuel duration at the same usage.

### Module reference

| Module | Piece | Usage | Effect/status in the current port |
| --- | --- | --- | --- |
| Energy | All armor; Enhanced Sabre and Lance | Special | Raises budget and fuel duration as above. |
| Armor | All armor | 20 | Adds 6 armor and 4 toughness while working, plus 20% protection against armor-piercing projectiles and magic per piece (80% with all four). |
| Nightvision | Helmet | 20 | Refreshes night vision while powered and enabled. |
| Scramble | Helmet | 50 | Prevents sentinel detection while its working flag is active. Does not pacify all hostile mobs. |
| Invisibility | Helmet, planned | 20 | Effect code/item exist, but its configuration entry is disabled; not a normally installable current feature. |
| Forcefield | Chestplate | 60 | Blocks projectiles and magic, including drone/lance lasers; absorbs 80% of explosions. Ranged/explosion hits start a shared cooldown with 50% ranged and 40% explosion protection. Halves melee damage and reflects half as Thorns damage. |
| Regeneration | Chestplate | 40 | Refreshes Regeneration III while powered/enabled. |
| Flight | Chestplate | 30 | Hold Space for upward thrust; no creative flight is granted. |
| Hover | Chestplate | 10 | With Flight active, holds altitude; sneak descends. |
| Speed | Leggings | 20 | Accelerates forward/back, with separate ground and flight caps. |
| Featherfalling | Boots | 20 | Cancels fall damage while working. |
| Step Assist | Boots | 10 | Raises normal step height to 1.902; preserves other step modifiers. |
| Power | Enhanced Sabre and Lance | 30 | Doubles the weapon's attack-damage modifier while working. |
| Inhibit | Enhanced Sabre and Lance | 30 | Suppresses a Master Soldier's regeneration for 200 ticks and removes beneficial effects from the target. |
| Looting | Enhanced Sabre and Lance | 20 | Applies Looting III while powered/enabled. |
| Fire | Enhanced Sabre and Lance | 30 | Applies Fire Aspect III while powered/enabled. |

An unupgraded suit uses leather's base armor values. With all four Armor Upgrades
working, the suit reaches **30 effective armor and 16 toughness**. Each working
piece also absorbs **20% of armor-piercing projectile and magic damage**, including
ship, lance and drone lasers; all four pieces absorb **80%**. This protection
applies before the forcefield. Ordinary arrows use physical armor protection;
falling, drowning and the void receive no energy protection. Losing fuel or
disabling a module removes its powered protection.

Against an unenchanted, fully upgraded suit with its forcefield off, normal
soldier melee deals about **2.4 hearts** and master/advanced guard melee about
**3.2 hearts**. Normal soldier lances deal about **3.12 hearts on Normal** or
**4.68 on Hard**; master lances remain dangerous at **6.24 hearts on Hard**.

Scout cannons deal **8 base damage** per shot, retain their five-shot-per-second
cadence and repeated attack runs, and still bypass ordinary damage immunity.
Four working Armor Upgrades reduce each scout hit to **0.8 hearts on Normal** or
**1.2 on Hard**. With every shot landing and no healing, that is thirteen hits
on Normal (2.4 seconds from the first hit) or nine on Hard (1.6 seconds).
Player-piloted ship cannons retain their unupgraded Energy Lance damage.

The personal forcefield is faintly visible from inside in first person. Absorbing
a ranged or magic hit or an explosion flashes the field red, then leaves a dimmer
red tint until it recovers. Third-person hit effects stay brighter throughout
cooldown. The tint and extra brightness fade back to normal during the last
three ticks (0.15 seconds). Recharge time follows the damage actually absorbed:
at least 10 ticks (half a second), or about 40 ticks (two seconds) for a fully
blocked 24-damage advanced city drone laser without Armor Upgrades. With all four
Armor Upgrades working, only 4.8 damage reaches the field, so the same laser uses
the minimum 10-tick recharge. Each subsequent ranged/explosion hit
resets the timer using the amount that the weakened field absorbs. Both kinds
of damage share the same cooldown; melee protection remains 50% throughout.
Melee retaliation uses the Thorns damage type, so the attacker's own defenses
can reduce the reflected damage. Reflected Thorns hits never retaliate again.
Other armor-bypassing damage, such as falling and drowning, remains unprotected.

Not every module has a blueprint recipe in the present catalog. Registered
items, configuration entries, working effects, and survival obtainability are
four different things. Focused survival-client checks now cover thrust, hover,
descent, Speed and Step Assist, including loss of movement on hotkey toggle,
unequip and fuel exhaustion. Enhanced Sabre updates follow the actual main hand
in any hotbar slot. Full survival progression and multiplayer still need testing.

### Energy Holder

The Energy Holder stores **both fuel dusts** and the remaining charge from the
currently burning pair. Carry it to power worn armor and your main-hand Enhanced
Energy Sabre or Energy Lance. No chestplate or Autofeed module is required.
Equipment panels show the holder’s total stored power at the bottom. Refill through the
holder’s own panel or enable Automatic collection.

Right-click a held holder to open its panel. The arrows move one dust or a stack
in or out. The panel, equipment panels and item tooltip show the same **Stored
power** total: **1,000 power per usable Negarite/Posirite pair**, plus the energy
remaining from the current pair. Twelve stored pairs and half a current pair
show **12,500**. The total keeps counting down across fuel-pair changes. Surplus
dust without its matching type does not add usable power. Energy Efficiency
makes the same stored power last longer.

- **Manual:** fuel enters through the GUI arrows.
- **Automatic:** scans inventory slots over successive ticks and collects loose
  Negarite/Posirite Dust. Both modes supply equipment automatically.

A main-hand holder takes priority, then an off-hand holder, otherwise the first
holder in the main inventory. Only the active holder collects and supplies fuel.
Separate holders never combine partial pairs. Starting another pair requires
both dusts; a failed refill consumes neither component nor the remaining charge.
The lance still needs an empty off hand; carry its holder in a normal inventory
slot.

Swapping equipment leaves fuel in the holder. Its charge survives saving and
item transfers. There is no player-stored reserve or fuel redistribution.

### Energy Sabres

The ordinary Energy Sabre is a melee weapon with a current +12 attack-damage
modifier and a -2.4 attack-speed modifier. The player's base attributes still
contribute to displayed totals. It uses durability and is not configured like
power armor.

The Enhanced Energy Sabre adds the module/fuel system. Keep Inhibit for Master
Soldiers: they regenerate strongly and are immune to fire and magic, so Fire is
not a substitute for suppressing their regeneration. Its current superclass
still supplies durability behavior; do not assume that fuel has replaced every
durability mechanic. Powered Looting/Fire modules preserve unrelated enchantments
and restore prior Looting/Fire levels when their boost stops.

### Energy Lance

The Energy Lance is a longer, two-handed relative of the sabre. Hold it in the
main hand with the off hand completely empty and right-click to fire a drone-style
laser. Shields, torches and any other off-hand item prevent firing. It fires once
per second, needs no ammunition and has **no durability**. Basic firing needs no
fuel; enabled upgrade modules use the same shared Energy Holder as power armor
and an Enhanced Energy Sabre.

Each laser deals 30% more raw damage than a fully charged ordinary Energy Sabre
hit (16.9 damage for an unmodified player, including the player's base damage).
It uses the drone laser's magic damage type; Master Soldiers resist magic.
Lasers stop at solid blocks. The lance does not give a sabre's melee bonus.
An Inhibit laser still suppresses Master Soldier regeneration and removes
beneficial effects despite their magic resistance.

Press the armor configuration key (normally `0`) while holding the lance and
select its weapon panel. Install, remove and enable Energy Efficiency, Power,
Inhibit, Looting and Fire modules, assign hotkey groups and check the shared stored power.
Power doubles the weapon damage bonus, producing 32.5 raw laser damage for an
unmodified player. Fire and Looting supply level III effects to laser hits;
Inhibit strips beneficial effects and suppresses Master Soldier regeneration
for 200 ticks. Shots retain these upgrades while in flight and across saves.
An occupied off hand stops module operation without consuming module fuel.

The matching reusable blueprint unlocks its Constructor and Auto Constructor
recipe. With ArienteWorld, both the weapon and its blueprint are rare storage-cube
loot in plans that already contain loot. Their weights are 0.005 and 0.01
respectively; these are relative weights, not per-cube percentages. One in five
new soldiers, guards and reinforcements carries a lance and uses ranged combat
within 24 blocks, holding the shaft with both hands. Lance and drone beams face
the camera, follow their projectile trajectory and remain visible head-on.
Soldier laser damage uses
their sabre damage floor and any city-plan strength, multiplied by 1.3.

Sources: [module types/costs](src/main/java/mcjty/ariente/api/ArmorUpgradeType.java),
[module fuel](src/main/java/mcjty/ariente/items/modules/ModuleSupport.java),
[configuration UI](src/main/java/mcjty/ariente/items/armor/ArmorGui.java),
[suit effects](src/main/java/mcjty/ariente/items/armor/PowerSuitEffects.java),
[movement hooks](src/main/java/mcjty/ariente/ClientForgeEventHandlers.java),
[damage handling](src/main/java/mcjty/ariente/ForgeEventHandlers.java),
[Enhanced Sabre](src/main/java/mcjty/ariente/items/EnhancedEnergySabreItem.java),
[Energy Lance](src/main/java/mcjty/ariente/items/EnergyLanceItem.java),
[Energy Holder](src/main/java/mcjty/ariente/items/EnergyHolderItem.java).

## Transport

### Flux Levitator and beams

Flux Beams are the rails for Flux Levitators. Straight, bent and sloped beams are
different blocks. Their thin selection outlines follow their models; they are
**intentionally non-solid**, including to players. Walking through a beam is
not evidence that the track is broken.

1. Build a continuous beam route with sufficient space for the vehicle and rider.
2. Use a **Flux Levitator item directly on a beam** to place the cart.
3. Right-click the vehicle without sneaking to board it.
4. Aim at an end hologram and click or drag the speed slider.
5. Use the red ball/stop control to request speed 0. Sneak to dismount normally.

The speed slider has a range of **-80 to 80**. Its outer arrows adjust by **1**. The two panels face opposite directions and interpret
their controls accordingly. The number is a control setting, not a promise of
that many blocks per second. Movement is capped, slows in bends, and depends on
track continuity. Slopes also limit travel speed automatically. Stop applies braking rather than teleporting the cart to rest.

No generator fuel or cable connection is required by the current cart movement
code. The beam is not a conventional powered rail. Flux Slope Beams rise one block
and can be chained for longer climbs. Their higher end points away from you when
placed: connect it to a beam one block higher. The cabin, its passenger seat and
both control panels follow the incline. Leave room above and beside the tilted
cabin, including at the top and bottom of each climb. Slopes have a common
Constructor blueprint with the same ingredients as straight beams. City AI can
dispatch soldier levitators on generated routes.

**Beam orientation:** a straight beam facing north/south runs east–west; one
facing east/west runs north–south. Default/north bends join west to south;
east bends join north to west; south bends join north to east; west bends join
south to east. Placement facing and travel direction are not identical labels.

Cart controls now render as attached HoloGui entities. Boarding, controls at
both ends, bends, all four slope directions, stopping and reloading on slopes
have dedicated checks. The tutorial authoring tool also drives its full city
circuit with a rider in both directions at slow and maximum settings. Arbitrary
route layouts and multiplayer still need in-game checks.

### Elevator and Level Markers

The **Elevator block** creates a vertical player-lift column. Its default height
is 9, adjustable through its hologram. Stand in the beam:

- With fewer than two detected floors, **jump rises** and **sneak descends**;
  otherwise it holds vertical movement.
- Place **Level Markers** at desired landing heights. It searches upward through
  its configured height and within two blocks horizontally of the elevator.
- With multiple marked floors, jump/sneak select the next higher/lower floor;
  the associated hologram also provides floor selection.

Height can be set from **1 to 256**. Right-click a Level Marker and click its
**Floor name** field to type a name (up to 32 characters). Press **Esc** or click
outside the field to save. Clear the field to return to a numbered floor.
Names are saved with the marker and appear beside the floor numbers in the
elevator hologram. If several markers share a landing height, its first named
marker supplies the label; unnamed markers do not hide a name.

The floor selector shows **five floors at a time**. Use its up/down arrows to
scroll, then click a floor to travel there. The list opens near your current
height, and each hologram keeps its own scroll position. Long names are shortened
in the list; the marker retains the full name.
The floor nearest your height has a **gray outline** around its row and brighter
text. The highlight updates as you travel.

The Elevator works automatically and requires no power supply.
Keep the shaft and landings clear. This block is separate from the Flux Elevator
cabin entity used by transport code.

### Flux Ship and cabin entities

The **Flux Ship** remains experimental, with piloting and cockpit instruments. Place
its item on a clear landing area with room for the full hull, then right-click
it to board. It carries one pilot:

| Control | Action |
| --- | --- |
| W / S | Fly forward / backward |
| A / D | Turn left / right |
| Up / Down | Rise / descend |
| Space | Hold to fire the front laser cannon |
| Backspace | Alternate descend control |
| Release controls | Brake and hover |
| Shift | Dismount |

Bindings can be changed in Controls. Existing saved fire bindings may retain V;
reset Fire ship laser or bind it to Space to use the new default. Space no longer
adds upward thrust. Mouse look steers the hull; hold Left Alt to look around
without steering it. The mounted hologram shows actual travel speed in km/h,
altitude as world Y, and your current key bindings. Its transparent pane and bright
text leave the view ahead visible. Acceleration and turning build
up gradually, with some momentum through turns. The hull banks into moving turns
and pitches with climbs and descents, then levels as it brakes into a hover.
Reversing uphill raises the tail. Your view follows turns while preserving free
look. First-person camera bank and pitch follow the ship; reduce or disable them
with `utility.fluxShipCameraTilt` (0–1) in `ariente-client.toml`. Third-person views
stay upright; the pilot model sits facing forward in the chair and tilts with the hull
while the camera can look around independently.

A sneaking hit on an unattended ship recovers the item
in survival, preserving its name and battery charge. Creative placement keeps the
item. Newly crafted ships and ships from the creative inventory start at **0%**;
their cockpit hologram says **No power**. Center a ship over a powered Flux Ship
Charger to recharge it using both flux types. Empty ships gently descend.
`/ariente equip` supplies an explicitly charged ship for operators.
Lance and ship lasers travel 20% faster. Ship cannon projectiles move at
**9.6 blocks per tick**; lance shots start at **1.8 blocks per tick**, with their
acceleration increased by the same percentage. Successful hull hits flash the
ship and its cannon red for half a second while the pilot receives the damage.
An abandoned ship, or one whose pilot has been defeated, descends at about
**four blocks per second** after slowing its previous motion. A living pilot
whose ship runs out of power retains the slower emergency descent.
The prototype has no automatic landing/navigation. The large floating ship-shaped
city is a generated structure. Flux Elevator cabin entities remain a separate
transport workflow.

Sources: [levitator item placement](src/main/java/mcjty/ariente/items/FluxLevitatorItem.java),
[cart movement](src/main/java/mcjty/ariente/entities/levitator/FluxLevitatorEntity.java),
[cart controls](src/main/java/mcjty/ariente/entities/levitator/FluxLevitatorGui.java),
[Elevator](src/main/java/mcjty/ariente/blocks/utility/ElevatorTile.java),
[ship item](src/main/java/mcjty/ariente/items/FluxShipItem.java).

## Building materials and lighting

| Family | Purpose |
| --- | --- |
| Marble | Gray, white, blue, black, lime, red, and dark blue variants. |
| Smooth, brick, pillar, and slab marble | Structural/decorative forms of those colors. |
| BlackTech and MarbleTech | Technological patterned panels; some use animated or glowing overlays. |
| Pattern blocks | Dots/lines decorative patterns. |
| Ramp | Five-step geometry for small stairs. |
| Slope | Wedge trim with four horizontal orientations, upright or upside down. |
| Glass/Blue Glass/Marble/Tech Fence | Pane-style connected barriers. |
| Reinforced Marble | Strong structural block; hardness 80, blast resistance 3,000. |
| Flux Glow | Light-emitting decorative block, light level 15. |
| Flat Light | Thin mounted light; lighting block rather than a power generator. |
| Facade | Copies a block state to cover cables/connectors. |

Marble shaping, dyeing, and many tech-block recipes use ordinary crafting.
Check the recipe JSON or normal crafting recipe viewer for the exact variant.
Those recipes coexist with blueprint construction; not every decorative block
requires a blueprint.

Connected textures, glowing overlays, and animation are handled natively by the
port. **CTM is not required.** Glow pixels are visual minimum-light overlays;
they do not automatically mean that a block emits world light, supplies power,
or performs an active machine function. See [TEXTURES.md](TEXTURES.md) for the
asset-authoring reference.

## Configuration reference

The configs are registered as Forge **server** and **client** configs. Normally
this means `ariente-server.toml` and `arienteworld-server.toml` under the world's
`serverconfig` directory, with client files in the instance's `config`
directory. Values below are defaults, not guaranteed server settings.

| Mod/category | Key | Default | Meaning |
| --- | --- | --- | --- |
| Ariente `utility` | `maxWarperCharges` | 10 | Pearl charge threshold. |
| Ariente `utility` | `maxDoorHeight` | 6 | Maximum generated segments above a Door Marker. |
| Ariente `utility` | `powersuitMaxPower` / `powersuitMaxPowerOptimized` | 100 / 130 | Per-piece module budget. |
| Ariente `utility` | `powersuitTicks` / `powersuitTicksOptimized` | 30000 / 40000 | Fuel-duration numerator, divided by module usage. |
| Ariente `utility` | `autoFieldPowerPerOperation` | 100 | Base paired transfer cost. |
| Ariente `utility` | `autoFieldFactorPerCombinedStack` | 2.0 | Additional paired cost per item beyond the first. |
| Ariente `utility` | `autoFieldAccumulatePerTick` | 400 | Maximum paired input per tick. |
| Ariente `utility` | `autoFieldMaxAccumulatedPower` | 3200 | Field reserve cap. |
| Ariente `power` | `forcefieldBuildupPower` | 500 | Paired cost per panel buildup step. |
| Ariente `power` | `smelterPowerPerTick` | 200 | Cost of each flux per active smelting tick. |
| Ariente `power` | `smelterSpeed` | 10 | Furnace recipe ticks processed per active smelting tick (1–200). |
| Ariente `damage` | `forcefieldDamage` | 19.0 | Contact damage. |
| Ariente `worldgen` | `overworldOregen` | DEFAULT | Enables optional Overworld marble and seven resource ores. |
| Ariente `worldgen` | `overworldDungeonLoot` | DEFAULT | Enables optional dungeon blueprints/equipment. |
| ArienteWorld `worldgen` | `overworldDungeonChance` | 0.002 | Per-chunk entrance candidacy. |
| ArienteWorld `worldgen` | `cityDungeonChance` | 0.7 | Seeded chance per complex grid spot; 0 disables complexes and 1 fills all candidates. Choose before creating a world. Districts, routes and arrival portals remain. |
| ArienteWorld `loot` | `soldierCitykeyChance` | 0.2 | City soldier key-card drop chance. |
| ArienteWorld `ai` | `alertTime` | 400 | City-AI steps; the current core cadence is 11 game ticks. |

Generator output/fuel duration, combiner loss/limit, storage capacity, and many
machine timing constants are currently hardcoded rather than adjustable through
these configs. Ariente's client utility settings include flight movement values,
and the movement hooks use them. Overworld ore and dungeon-loot settings accept
YES/NO/DEFAULT; DEFAULT enables them when ArienteWorld is absent. Dungeon loot
preserves vanilla drops and offers one extra weighted selection: no addition has
weight 100, blueprints/results use their configured weights, and each of the six
suit/sabre entries uses the suit weight. Defaults 20/0/0 give a one-in-six chance
of a blueprint addition per supported chest; recipe rarity then selects its type.

Sources: [Ariente config classes](src/main/java/mcjty/ariente/config),
[ArienteWorld config classes](../ArienteWorld/src/main/java/mcjty/arienteworld/config).

### Overworld ore datapacks

`overworldOregen` controls whether the features actually place blocks. `DEFAULT`
enables them without ArienteWorld, `YES` enables them even with the companion,
and `NO` disables them. The server config is checked at placement time, after
worldgen JSON is decoded; no config value is captured during registry setup.
For new worlds, put server defaults in `defaultconfigs/ariente-server.toml`.
Changes affect newly generated terrain; existing chunks are not regenerated.

Each ore has its own configured and placed feature, with the same ID in both
registries:

| Feature ID | Size | Attempts/chunk | Uniform Y range |
| --- | --- | --- | --- |
| `ariente:overworld_marble` | 15 | 2 | 16–69 |
| `ariente:overworld_lithium` | 3 | 2 | -48–19 |
| `ariente:overworld_manganese` | 5 | 3 | -16–49 |
| `ariente:overworld_platinum` | 5 | 4 | -16–79 |
| `ariente:overworld_silicon` | 5 | 4 | -16–59 |
| `ariente:overworld_silver` | 6 | 4 | -32–19 |
| `ariente:overworld_negarite` | 6 | 5 | -48–29 |
| `ariente:overworld_posirite` | 6 | 5 | -48–29 |

Size and attempt counts are generation parameters, not guaranteed block yields.
The seven resource ores replace both stone and deepslate; the original ore
blocks/textures are used in both. These defaults extend the original Overworld
ranges into modern deep terrain; ArienteWorld's own ore distributions are separate.

To customize, make a Minecraft 1.20.1 datapack (`pack_format: 15`) and copy the
files you want to change from
[data/ariente/worldgen](src/main/resources/data/ariente/worldgen) into the same
paths in your pack. For example:

- `data/ariente/worldgen/configured_feature/overworld_negarite.json`: change
  `config.size`, `config.targets` (replacement predicates and output states), or
  `discard_chance_on_air_exposure`. Keep `type: "ariente:overworld_ores"` to retain
  the late config and Overworld-only checks. The config uses vanilla ore fields.
- `data/ariente/worldgen/placed_feature/overworld_negarite.json`: change the count,
  height provider, or other vanilla placement modifiers. Its `feature` points to
  `ariente:overworld_negarite`.

The Forge biome modifier
`data/ariente/forge/biome_modifier/overworld_ores.json` adds all eight individual
placed features to Overworld biomes at `underground_ores`. Override it to change
biome selection or remove a distribution. The aggregate configured/placed key
`ariente:overworld_ores` remains registered for explicit placement, but is not
added by the default modifier. Adding the aggregate alongside its children will
duplicate generation. Install the pack before generating the terrain you want
to customize; restart after changing worldgen data.

## Recipe reference

### Ordinary crafting entry points

Patterns are shown as three crafting-grid rows. Tag ingredients accept items
actually present in the corresponding Forge item tag.

| Output | Grid | Ingredients |
| --- | --- | --- |
| Ariente Pearl | `QQQ / QEQ / QQQ` | Q = Nether Quartz; E = an item in `forge:ender_pearls`. |
| Constructor | `III / IPI / SSS` | I = iron ingot tag; P = platinum ingot tag; S = silver ingot tag. |
| Blueprint Storage | `SGS / QCQ / MMM` | S = silver ingot tag; G = Glass Pane; Q = silicon dust tag; C = Chest; M = ordinary marble tag, mixed colors accepted. |
| Enhanced Energy Sabre | `LLL / LSL / LLL` | L = lithium ingot tag; S = ordinary Energy Sabre. |

The Constructor also has a blueprint recipe, separate from its ordinary
crafting entry point. Raw Lithium, Raw Manganese, Raw Platinum and Raw Silver
smelt into one ingot each; their ore blocks still smelt into one ingot as well.
Both raw-metal and metal-ore recipes accept the corresponding material-specific
Forge item tags (`forge:raw_materials/<metal>` and `forge:ores/<metal>`).
Negarite and Posirite ores smelt into four dust each. Smelting takes 200 ticks;
blasting takes 100 ticks with the same yields and zero experience. The Smelter
uses the same furnace recipes and yields.

### Blueprint catalog

The tables below enumerate the current hardcoded catalog. An output is one item
unless a quantity is shown. The rarity column is the relative blueprint-pool
weight class, not a crafting success chance. **Flux Capacitor**, **Circuit**, and
**Advanced Circuit** are components consumed by other recipes, not independent
machines to place and power.

Cable and connector entries use distinct Negarite/Posirite/Combined items,
including matching connector ingredients. Data variants have no catalog recipe.

| Result | Rarity | Ingredients |
| --- | --- | --- |
| Holographic Display Base | Common | 4 × Silver Ingot, 2 × Circuit, 2 × Glass, 2 × Glowstone Dust |
| Holographic Power Module | Common | Circuit, 2 × Manganese Ingot, 2 × Negarite Dust, 2 × Posirite Dust |
| Holographic Storage Module | Common | Circuit, 2 × Silver Ingot, Chest |
| Holographic Text Module | Common | Circuit, 2 × Silicon, Glowstone Dust |
| Flux Capacitor | Rare | 2 × Lithium Ingot, Manganese Ingot, 6 × Negarite Dust, 6 × Posirite Dust |
| Energy Holder | Common | Silicon, 5 × Negarite Dust, 5 × Posirite Dust |
| Circuit | Common | Silicon, Manganese Ingot, 3 × Negarite Dust, 3 × Posirite Dust |
| Advanced Circuit | Rare | Circuit, 2 × Lithium Ingot, Manganese Ingot, 6 × Negarite Dust, 6 × Posirite Dust |
| Nightvision Upgrade Module | Uncommon | Circuit, Silver Ingot, Silicon, 10 × Glowstone Dust |
| Armor Upgrade Module | Rare | Circuit, Silver Ingot, Silicon, Platinum Ingot, 2 × Diamond |
| Energy Upgrade Module | Rare | Circuit, Silver Ingot, Silicon, Platinum Ingot, Lithium Ingot, 3 × Negarite Dust, 3 × Posirite Dust |
| Featherfalling Upgrade Module | Rare | Circuit, Silver Ingot, Silicon, Lithium Ingot, 5 × Feather |
| Fire Sabre Module | Uncommon | Circuit, Silver Ingot, Silicon, Magma Block, 5 × Flint and Steel |
| Flight Upgrade Module | Extreme | Advanced Circuit, Silver Ingot, Silicon, Platinum Ingot, Lithium Ingot, 2 × Ender Pearl, Nether Star |
| Forcefield Upgrade Module | Extreme | Advanced Circuit, Silver Ingot, Silicon, Lithium Ingot, Platinum Ingot, 5 × Negarite Dust, 5 × Posirite Dust, Nether Star |
| Inhibit Sabre Module | Rare | Advanced Circuit, Silver Ingot, Silicon, 4 × String |
| Looting Sabre Module | Rare | Circuit, Silver Ingot, Silicon, 10 × Blue Dye |
| Flux Levitator | Uncommon | Circuit, 10 × Silver Ingot, 2 × Manganese Ingot, Lithium Ingot |
| Energy Sabre | Common | 10 × Silver Ingot, 3 × Negarite Dust, 3 × Posirite Dust, Platinum Ingot, Lithium Ingot |
| Energy Lance | Rare | Energy Sabre, Advanced Circuit, 10 × Silver Ingot, 5 × Negarite Dust, 5 × Posirite Dust, 2 × Platinum Ingot, 2 × Lithium Ingot |
| Enhanced Energy Sabre | Rare | Energy Sabre, Circuit, 3 × Silicon, 5 × Negarite Dust, 5 × Posirite Dust, 3 × Platinum Ingot, 2 × Lithium Ingot |
| Power Leggings | Rare | 5 × Silver Ingot, 2 × Silicon, Platinum Ingot |
| Power Helmet | Rare | 4 × Silver Ingot, 2 × Silicon, Platinum Ingot |
| Power Chestplate | Rare | 7 × Silver Ingot, 2 × Silicon, 2 × Platinum Ingot |
| Power Boots | Rare | 3 × Silver Ingot, 2 × Silicon, Platinum Ingot |
| Forcefield Generator | Rare | Advanced Circuit, Flux Capacitor, 5 × Silver Ingot, 5 × Silicon, 3 × Platinum Ingot, 2 × Lithium Ingot, 10 × Posirite Dust, 10 × Negarite Dust, Nether Star |
| Negarite Flux Generator | Common | Circuit, 5 × Silver Ingot, 5 × Silicon, 2 × Manganese Ingot, 2 × Lithium Ingot, 40 × Negarite Dust |
| Posirite Flux Generator | Common | Circuit, 5 × Silver Ingot, 5 × Silicon, 2 × Manganese Ingot, 2 × Lithium Ingot, 40 × Posirite Dust |
| Power Combiner | Common | Circuit, 3 × Silver Ingot, 2 × Silicon, 20 × Negarite Dust, 20 × Posirite Dust |
| Constructor | Common | Circuit, 2 × Silver Ingot, Platinum Ingot, 4 × Silicon |
| Auto Constructor | Common | Constructor, 2 × Circuit, Silver Ingot, 2 × Platinum Ingot, 2 × Silicon |
| Storage Cube | Common | Black Marble, 3 × Silver Ingot, Chest, 4 × Glass Pane |
| Storage Cube (3x3) | Common | Black Marble, 6 × Silver Ingot, Circuit, 2 × Chest, 9 × Glass Pane |
| Holo Storage Cube I (30 stacks) | Common | 2 × Circuit, 8 × Silicon, 8 × Silver Ingot, 2 × Lithium Ingot, Ender Pearl, 6 × Glass, Black Marble |
| Holo Storage Cube II (60 stacks) | Uncommon | 4 × Advanced Circuit, 24 × Silicon, 24 × Silver Ingot, 12 × Platinum Ingot, 8 × Lithium Ingot, 4 × Ender Pearl, 2 × Flux Capacitor, 8 × Glass |
| Holo Storage Cube III (120 stacks) | Rare | 12 × Advanced Circuit, 64 × Silicon, 64 × Silver Ingot, 32 × Platinum Ingot, 24 × Lithium Ingot, 16 × Ender Pearl, 8 × Flux Capacitor, 16 × Glass |
| Smelter | Common | Black Marble, 3 × Silver Ingot, Circuit, 2 × Silicon, Furnace |
| Purifier Start | Common | Black Marble, 4 × Silver Ingot, 2 × Circuit, 2 × Silicon, Chest |
| Purifier Finish | Common | Black Marble, 4 × Silver Ingot, Circuit, Chest |
| Purifier Tube | Uncommon | 2 × Advanced Circuit, 2 × Flux Capacitor, 4 × Platinum Ingot, 4 × Lithium Ingot, 4 × Manganese Ingot, 8 × Glass, 8 × Negarite Dust, 8 × Posirite Dust |
| Blueprint Storage | Common | Black Marble, 3 × Silver Ingot, 2 × Silicon, Chest, 4 × Glass Pane |
| Elevator | Common | Circuit, Black Marble Slab, 3 × Silver Ingot |
| Negarite Tank | Common | 4 × Silver Ingot, Manganese Ingot, 4 × Glass, 20 × Negarite Dust |
| Posirite Tank | Common | 4 × Silver Ingot, Manganese Ingot, 4 × Glass, 20 × Posirite Dust |
| Flux Beam | Common | Black Marble, Silver Ingot, Negarite Dust, Posirite Dust |
| Flux Bend Beam | Common | Black Marble, Silver Ingot, Negarite Dust, Posirite Dust |
| Level Marker | Common | Black Marble Slab, Silver Ingot |
| Door Marker | Common | Black Marble Slab, 3 × Silicon, Silver Ingot |
| Alarm | Uncommon | Circuit, Black Marble, Silicon, Lithium Ingot, Glowstone Dust, Ender Pearl, Silver Ingot |
| Lock | Uncommon | Circuit, 2 × Silicon, Lithium Ingot, Silver Ingot |
| Signal Receiver | Uncommon | Circuit, Black Marble Slab, Silicon, Lithium Ingot, Ender Pearl, Silver Ingot |
| Signal Transmitter | Uncommon | Circuit, Black Marble Slab, 2 × Silicon, Lithium Ingot, Ender Pearl |
| Wireless Button | Uncommon | Circuit, Black Marble Slab, 2 × Silicon, Lithium Ingot, Ender Pearl |
| Wireless Lock | Uncommon | Circuit, Black Marble Slab, 4 × Silicon, 2 × Silver Ingot, Lithium Ingot, Ender Pearl |
| Slope | Common | Black Marble |
| 2 × Black Marble Slab | Common | Black Marble |
| Reinforced Marble | Uncommon | 4 × Black Marble, 4 × Obsidian |
| Automation Field | Uncommon | Circuit, Flux Capacitor, 3 × Silicon, Ender Pearl, 2 × Silver Ingot |
| Sensor Item Node | Uncommon | Circuit, 2 × Silicon, 2 × Silver Ingot |
| Input Item Node | Common | Circuit, 2 × Silicon, 2 × Silver Ingot |
| Output Item Node | Common | Circuit, 2 × Silicon, 2 × Silver Ingot |
| Round Robin Node | Uncommon | Circuit, Silicon, 2 × Silver Ingot |
| Field Marker | Common | Black Marble Slab, Silicon, 2 × Silver Ingot |
| Flux Glow | Common | Black Marble, 8 × Glowstone Dust |
| Flat Light | Common | Black Marble Slab, 8 × Glowstone Dust |

#### Cable and connector variants

| Result | Rarity | Ingredients |
| --- | --- | --- |
| 8 × Negarite Cable | Common | Silicon, Silver Ingot, 3 × Glass Pane, 2 × Negarite Dust |
| 8 × Posirite Cable | Common | Silicon, Silver Ingot, 3 × Glass Pane, 2 × Posirite Dust |
| 8 × Combined Cable | Common | Silicon, Silver Ingot, 3 × Glass Pane, 2 × Negarite Dust, 2 × Posirite Dust |
| Negarite Connector | Common | Negarite Cable, Silver Ingot, Silicon |
| Posirite Connector | Common | Posirite Cable, Silver Ingot, Silicon |
| Combined Connector | Common | Combined Cable, Silver Ingot, Silicon |


Power, Regeneration, Scramble, Speed and Step Assist modules have no Constructor
recipe, as in the original mod; authored city loot now supplies their module
items instead of unusable blueprints. Hover has no catalog recipe or confirmed
authored loot entry. Invisibility's installation control remains disabled.
City0_0 and the large city5_1 layout have no authored Storage Cubes.

Ordinary decorative recipes live in
[generated recipe data](src/generated/resources/data/ariente/recipes).
The blueprint tables are sourced from
[BlueprintRecipeRegistry.java](src/main/java/mcjty/ariente/recipes/BlueprintRecipeRegistry.java).

## Troubleshooting

| Symptom | Check |
| --- | --- |
| Pearl cannot find a dungeon | Use it in the Overworld; move to another area if no intact seeded entrance is within its search radius. `findentrance` searches farther for testing. |
| Warper says insufficient charge | Add pearls directly to that Warper; cable power does not charge it. |
| Generator appears fueled but machine does nothing | Matching tanks, redstone mode, both fuel types, output capacity, and connectors at the machine. |
| A cable touches the machine but it has no supply | Replace the endpoint with a matching Connector; a plain cable is not a supplying endpoint. |
| More negarite does not help | Posirite may be the limiting component, or vice versa. |
| Combiner consumes input but the output is inadequate | Check transfer setting, 2% loss, Combined Connectors, and competing consumers. |
| Shield does not appear around a peaceful ship | City shields activate on alerts. Test in Survival, non-Peaceful difficulty, in a sentinel's visible range, without active Scramble. |
| Shield has holes or builds slowly | Supply construction/repair power in addition to idle power; projectile damage affects individual panels. |
| City does not initialize/alert | Load its required footprint; Creative/Spectator proximity alone will not trigger sentinel alerts. |
| Card has the city's name but access fails | Check its security tags and whether you need door, storage, or shield access. |
| Constructor shows no recipes | Use populated blueprints in directly adjacent Blueprint Storage. |
| Constructor result is crossed out | Hover for missing ingredients; manual crafting takes them from the player's inventory. |
| Auto Constructor stalls | Check both power components, ingredient inventory, output space, neighboring blueprints, and redstone mode. |
| Smelter stalls | Check for a furnace recipe, both flux components, and room for the complete result in the right slot. Insert through top/sides/front; extract through bottom/back. |
| Automation Field moves nothing | Connected markers under node columns, height, correct source Output/destination Input roles, nonempty filters, full batch availability, conditions, and power. |
| Item-transfer animation is absent | Enable item rendering in the field hologram and check inventory counts; animation samples recent transfers. |
| Armor module is installed but does nothing | Activation, a carried Energy Holder with both fuel types or remaining charge, the per-piece module budget, and the ability's current implementation. |
| Energy Lance will not fire | Hold it in the main hand, leave the off hand completely empty, and wait for the one-second cooldown. Durability, ammunition and fuel do not limit basic shots. |
| Display module preview is red | Check the common display height, module size, overlap and front-facing placement. Enlarge the surface or reduce the module footprint. |
| Display readings are offline or `n/a` | Check the module's link, the clicked inventory side and whether the target is loaded. Unsupported power readings show `n/a`; displays do not load chunks. |
| Cart will not start | Board the cart, use an end panel's speed slider, and confirm continuous beam orientation/clearance. Controls are HoloGui entities, not a full-screen menu. |
| Elevator cannot find a floor | Marker height must be inside its scan and within two blocks horizontally; clear the lift column. |

## Port gaps

These are current implementation boundaries, not requirements to repair older
worlds.

| Area | What remains incomplete or needs validation |
| --- | --- |
| Survival resources | Ore drops, processing and mob lithium loot pass focused checks; a full survival playthrough remains. |
| Alternative progression without ArienteWorld | Deep/stone Overworld ores, default dungeon blueprints and the crafting bootstrap pass a client check without the companion. Standalone builds pass with staged HoloGui; public Maven availability is pending. |
| Recipe variants | Negarite/Posirite/Combined cable and connector recipes work. Data variants and several original modules still have no catalog entry. |
| Crafting integration | Constructor JEI and full manual/automatic batch yields pass focused checks. Crafting still uses the hardcoded catalog. |
| Armor movement | Flight, Hover, Speed and Step Assist pass focused survival-client checks. Invisibility's install control remains disabled. |
| Personal forcefield | Five dedicated regression tests cover protection, cooldowns and retaliation. Live client checks cover lasers and real skeleton arrows, front/back third-person views, visible cooldown tint and its fade, including with Embeddium/Oculus; flashes with multiple remote players remain a play-test item. |
| Equipment lifecycle | Fuel/effect checks and real dedicated-server hotkeys, equipment synchronization, reconnect and death/respawn pass with two clients. Multiplayer flight and mod interactions still need play testing. |
| Automation matching | Tag/NBT/damage filters, batch routing, round robin and rejected-insert recovery pass focused inventory checks; long multi-chunk networks remain an integration check. |
| Visual effects | Automation transfers, blueprint previews, facade items, ship hull, soldier armor and hologram tooltips are restored; combined-cable beam rendering remains an original limitation. |
| Facades | State display, removal without losing the cable/network, and exported facade properties pass focused checks. |
| Security provisioning | City keys work through authored/AI setup; there is no complete player-facing arbitrary key-programming workflow. |
| Elevator | Works without power; floor markers select landing heights. Long/multiplayer lift use remains an integration check. |
| Flux Ship / cabin workflows | Ship flight, charging, rendering, placement and save/load are restored; progression and advanced ship design remain unfinished. |
| Worldgen controls | Seeded complex chance, search/API agreement, original soil/rough surfaces, branching blue trees and hill/forest flowers pass focused checks. Quart sampling/native placement differences are documented in ArienteWorld's handoff; exploration play testing remains. |
| City editing tools | Creative operator sessions, sync, variants and JSON export/reload are restored; authoring sessions are temporary and require explicit saving. |
| General validation | 48 dedicated tests, full datagen, asset/model audits and focused two-client multiplayer checks pass. Full survival progression, sustained combat/spawn balance, real chunk unload/restart and multiplayer transport/portals remain. |

For implementation history and build commands, see [PORTING.txt](PORTING.txt) and
the [ArienteWorld checkpoint](../ArienteWorld/PORTING.txt).

## Source map

Use these as the maintenance reference when updating the guide:

| Topic | Source |
| --- | --- |
| Registered blocks/items and creative entries | [Registration](src/main/java/mcjty/ariente/setup/Registration.java). |
| Names and existing tooltip descriptions | [English translations](src/main/resources/assets/ariente/lang/en_us.json). |
| Power and cable behavior | [power](src/main/java/mcjty/ariente/power), [cables](src/main/java/mcjty/ariente/cables), [generators](src/main/java/mcjty/ariente/blocks/generators). |
| Machines, storage, doors, signals, automation | [utility blocks](src/main/java/mcjty/ariente/blocks/utility). |
| Holographic displays and extensible module types | [display](src/main/java/mcjty/ariente/display), [client rendering](src/main/java/mcjty/ariente/display/client). |
| Shield geometry and damage | [defense](src/main/java/mcjty/ariente/blocks/defense). |
| Equipment and module configuration | [items](src/main/java/mcjty/ariente/items), [upgrade types](src/main/java/mcjty/ariente/api/ArmorUpgradeType.java). |
| Blueprint recipes and ordinary recipes | [catalog](src/main/java/mcjty/ariente/recipes/BlueprintRecipeRegistry.java), [ordinary JSON](src/generated/resources/data/ariente/recipes), [recipe generator](src/main/java/mcjty/ariente/datagen/Recipes.java). |
| World terrain/population | [dimension](../ArienteWorld/src/main/java/mcjty/arienteworld/dimension), [worldgen data](../ArienteWorld/src/main/resources/data/arienteworld/worldgen). |
| Authored cities and loot settings | [city assets](../ArienteWorld/src/main/resources/assets/arienteworld/citydata), [city AI](../ArienteWorld/src/main/java/mcjty/arienteworld/ai/CityAI.java). |
| Verified regressions | [Personal forcefield tests](src/gameTest/java/mcjty/ariente), [companion GameTests](../ArienteWorld/src/gameTest/java/mcjty/arienteworld). |

When source and a historical tooltip/wiki disagree, use the current code as the
reference and label unfinished behavior explicitly. Update this guide as the
remaining systems are restored.
