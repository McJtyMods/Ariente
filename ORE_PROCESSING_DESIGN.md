# Ariente ore processing design

Design proposal, 2026-11-04. Current implementation references target Minecraft
1.20.1 / Forge on branch `1.20`.

The proposed direction is to give Ariente's metals vanilla-style raw item drops,
then offer a distinct flux refining process that recovers more metal from those
items. Raw metals and ordinary ingots provide the connection to other mods.
Ariente would need no new metal dusts for its core processing system.

The raw material direction comes from the design discussion. Refining machines,
progression, and example yields below remain proposals; they do not describe
implemented features or settled balance values.

## Current behavior

| Resources | Mining and processing in the current checkout |
| --- | --- |
| Silver, platinum, lithium, manganese | Ores drop their blocks. Each block smelts or blasts into one ingot. |
| Negarite and posirite | Ores drop their blocks. Each block smelts or blasts into four corresponding fuel dusts. |
| Silicon | Ore drops Silicon directly, with Fortune and Silk Touch support. Its internal item ID is `dust_silicon`. |

The existing Smelter executes furnace recipes using both flux components and
preserves their output quantities. Adding normal furnace recipes for raw metals
would therefore also make those inputs available to the Smelter. Increased
recovery would belong to a separate refining recipe type.

Current sources:
[ore drops](src/main/java/mcjty/ariente/datagen/LootTables.java),
[ore processing recipes](src/main/java/mcjty/ariente/datagen/Recipes.java),
[Smelter](src/main/java/mcjty/ariente/blocks/utility/SmelterTile.java), and
[existing gameplay guide](ARIENTE_GUIDE.md).

## Raw metal progression

Silver, platinum, lithium, and manganese would follow the familiar vanilla
pattern:

- Mining an ore normally yields raw metal. Fortune increases the drop.
- Silk Touch preserves the ore block.
- One raw metal smelts or blasts into one ingot. The existing Smelter follows
  the same furnace recipe.
- Flux refining offers improved recovery from raw metal.
- Optional raw storage blocks compress nine raw items and unpack into nine.

Keep the existing ore-block smelting recipes so stored ores and Silk Touch
blocks remain usable. Define any bonus refining recipes for ore blocks
separately from raw-item recipes.

Silicon already behaves like a directly mined resource and can retain that
role. Negarite and posirite need a separate decision because their dusts are
fuels. Changing their drops or recovery also changes the cost of supplying the
entire power system. The initial raw-metal change can cover the four metals
without changing those resources.

Vanilla's raw metals were introduced to support Fortune on smeltable ores while
retaining smelting for Silk Touch blocks. See
[Mojang's explanation](https://www.minecraft.net/en-us/article/minecraft-snapshot-21w14a).

## Compatibility and material selection

Use material-specific item tags for recipe inputs. For this Forge 1.20.1 target,
silver provides the following example:

| Material form | Shared item tag | Proposed use |
| --- | --- | --- |
| Raw silver | `forge:raw_materials/silver` | Main input to refining and normal smelting. |
| Silver ingots | `forge:ingots/silver` | Finished material accepted in construction recipes. |
| Silver dust | `forge:dusts/silver` | Optional integration with an installed mod's dust. |
| Silver ore blocks | `forge:ores/silver` | Separately balanced recipes for ore blocks. |

Ariente's refinery could accept any raw silver in the shared tag. Another mod
could process Ariente's raw silver when it has a suitable recipe accepting that
tag. A player chooses a processing route at the raw stage:

| Route | Example yield per raw item |
| --- | --- |
| Furnace, blast furnace, or existing Ariente Smelter | One ingot. |
| Proposed Ariente flux refinery | Two ingots. |
| Another mod's processing chain | That mod's configured recipe and yield. |

Tags establish material equivalence; they do not create missing recipes or
choose an output item. A mod that has no lithium processing recipe will still
need an integration recipe. Broad tags such as `forge:raw_materials` are useful
for categories, but cannot identify the metal to produce. See the
[Forge tag conventions](https://docs.minecraftforge.net/en/1.20.1/resources/server/tags/).

Ariente already tags its ores, ingots, and existing dusts in
[ItemTags](src/main/java/mcjty/ariente/datagen/ItemTags.java). Its automation
[material matcher](src/main/java/mcjty/ariente/blocks/utility/autofield/ItemFilterMatcher.java)
already recognizes material-specific `forge:raw_materials/...` tags when tag
matching is enabled.

Refining recipes should have concrete output stacks and a predictable way to
select the preferred item for each metal. Vanilla ingots cover iron, gold, and
copper. Ariente's existing ingots provide standalone outputs for its own metals;
pack makers should be able to substitute another provider through recipe
overrides or a supported unification integration. Avoid choosing an arbitrary
first item from a tag.

Use JSON recipes and expose them through JEI. This gives pack makers control
over ingredients, yields, and outputs. Almost Unified selects preferred material
items and rewrites supported JSON recipes; a custom Ariente recipe format would
need its compatibility checked rather than assumed. See its
[overview](https://github.com/AlmostReliable/almostunified/wiki) and
[limitations](https://github.com/AlmostReliable/almostunified/wiki/Limitations).

Raw items can also overlap between mods. The straightforward standalone design
is to register Ariente's four raw metals and tag them correctly. A pack that
wants one visible raw item per metal can override ore loot to drop its preferred
provider. Recipe unification alone does not establish a common ore drop.

## Dusts without new Ariente metal dusts

The core route would be raw metal to ingots, with intermediate processing kept
inside the refining installation. This works with Ariente alone and introduces
no metal dust variants.

An optional integration could export an existing mod's clean metal dust instead
of ingots. For example, one raw silver could yield two selected silver dusts,
each smeltable into one ingot. That output is useful for factories that mix
dusts for alloys or already have a smelting line. Enable it only when a suitable
dust and finishing recipe exist, with an explicit preferred provider if several
are present. Ariente would still need no fallback metal dust.

Clean dust represents recovered metal and should receive normal finishing
yield. Sending it into Ariente's process must not apply the raw-metal recovery
bonus again. Likewise, the process must consume raw material and produce a
different form; accepting raw metal and returning more of the same raw metal
would allow repeated multiplication.

If a later design requires dusts even in standalone play, always registering
fallback dust items and selecting which ones recipes produce is a separate
option. It avoids producing duplicates, but still adds registry entries. Item
registration happens before datapack tags are available, so registering items
conditionally based on a loaded material tag is unsuitable for this purpose.

## Flux refining chamber

The preferred concept is a visible chamber that suspends raw material between
emitters and condenses recovered metal into ordinary ingots. Its internal
separation and condensation stages need no inventory items.

In the proposed fiction, negarite flux loosens the mineral structure while
posirite flux confines the useful metal. The player sees impurities separate
from the suspended material and metal collect at the output. This gives both
flux components a visible role in the process.

The installation could offer several choices:

- **Field arrangement:** focusing components around the chamber improve
  recovery, processing capacity, or speed.
- **Operating mode:** rapid extraction favors throughput; thorough extraction
  favors yield and uses more time or flux per batch.
- **Exploration progression:** recovered blueprints unlock chamber components
  or more efficient processing techniques.

The basic target could be deterministic doubling. Later components could
improve throughput or flux efficiency, keeping progression useful without
requiring ever larger material multipliers.

Configure the installation once, then let it automate. Holograms should show
the current recipe, batch yield, processing time, and missing flux or output
space. The visible field should explain its operating state. A small initial
installation can establish the mechanic before adding larger structures.

Ariente's current power helper consumes equal amounts of negarite and posirite
and limits usable supply by the weaker component. The first chamber can use
that existing behavior. Independent flux ratios or alternating single-component
phases would require additional power-handling support; those are optional
extensions. See
[PowerReceiverSupport](src/main/java/mcjty/ariente/power/PowerReceiverSupport.java).

## Other processing concepts

**Crystal growth** would feed raw material into a suspended metallic crystal.
The crystal grows visibly and is harvested into ingots. Batch size, field
strength, or focusing components could affect recovery. The crystal can remain
internal to the installation, avoiding a separate item for every metal.

**Residual recovery** would produce the main ingot output first, then recover
additional metal from material retained inside the installation. A second
component or stage could perform that recovery. Retained material must preserve
its metal identity and quantity through saving, extraction, and interruption.
This creates a processing chain without adding a waste item for every metal.

Both concepts can share the same raw inputs and ordinary outputs. They could
replace the chamber's visual mechanism or extend it later. The initial design
does not need all three systems.

## Yield and balance

Define doubling relative to one raw item. For a metal whose ordinary smelting
recipe gives one ingot, doubling means two ingots from that raw item.

Fortune increases the raw material obtained before refining, so its benefit
stacks with increased recovery. That makes metals more plentiful even before
considering later upgrades. Tune ore availability, refining time, flux cost,
and recovery together. Exact values remain open.

Ore blocks need explicit rates. An ore block is not universally equivalent to
one raw item: vanilla copper normally drops multiple raw pieces. Decide how
Silk Touch processing should compare with normal and Fortune mining rather
than deriving every ore-block rate from its furnace output.

If raw storage blocks are processed directly, their yield and material cost
must equal nine raw items. Compression must not introduce an extra recovery
bonus, and breaking the storage block must not grant Fortune drops.

Restrict increased recovery to supported raw materials and deliberately defined
ore recipes. Keep normal smelting for clean dusts, ingots, equipment recycling,
and other furnace inputs. A blanket multiplier over every smelting recipe would
make unrelated recipes and recycling paths part of ore doubling.

## Implementation sequence and open decisions

1. Add raw items for the four metals, update ore loot for Fortune and Silk
   Touch, add shared tags and normal smelting/blasting recipes, and retain the
   existing ore-block recipes. Decide whether raw storage blocks belong here.
2. Establish a small flux refining installation with JSON recipes, deterministic
   yields, ordinary ingot outputs, JEI support, and item automation.
3. Verify representative raw materials from other mods, then add optional dust
   export and support for preferred output providers where useful.
4. Expand field components, operating modes, and blueprint progression after
   the basic recovery and flux economy are balanced.

The main choices still to settle are:

- Whether negarite and posirite also gain raw forms, and how that affects fuel.
- Whether the first refinery processes only Ariente's metals or also vanilla
  metals and a selected set of other materials.
- Whether doubling is the initial recovery rate or an exploration unlock.
- Whether the first installation is one machine or several placed components.
- How ore-block recovery compares with raw processing and Fortune mining.
- Whether dust export is worthwhile for the first implementation.

Future validation should cover Fortune and Silk Touch drops, raw storage
round trips, normal furnace and Smelter outputs, blocked outputs and interrupted
refining, persistence of internal material, and interoperability with selected
mods. A survival playthrough should establish the actual cost and timing of
unlocking improved recovery.
