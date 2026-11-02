# Ariente automation field status

Investigation date: 2026-11-02. Target: Minecraft 1.20.1 / Forge, Ariente branch
`1.20`, version `1.20-0.0.31-alpha`, commit
`c0c9687bb4e603807d71440dca6ece8242033601`.

The initial findings came from source inspection of Ariente and the local
McJtyLib checkout used by the multiworkspace build. A follow-up on the same date
migrated inventory capabilities to McJtyLib's `@Cap` annotations and supplied
missing automation slot definitions. The capability and slot-definition defects
recorded below are fixed in the working tree. The remaining sensor-color defects
are source-level findings that still need runtime reproduction. This document
describes the checkout, not a verified published release.

The automation field implements item logistics with filters, batches,
round-robin distribution, inventory sensors, and colored conditions. It can
express sorting, machine supply, and simple refill rules. Stock limits are
approximate, and apparent defects remain in sensor-color configuration.

| Capability | Implemented behavior |
| --- | --- |
| Item transfer | Output Item Nodes extract from their attached inventories; Input Item Nodes insert into theirs. The names describe inventory input/output. |
| Routing | An Output Node can send to any eligible Input Node in the same field. Both filters, both nodes' conditions, and destination capacity must permit the transfer. No item pipes or intermediate routing nodes are required. |
| Item filters | Each Input and Output Node has 12 sample slots. Matching any populated sample permits that item at that node. Both ends must accept it. An empty filter accepts nothing. |
| Matching options | Each item node can independently enable material-tag equivalence, exact damage matching, and exact NBT matching. The options apply to all samples on that node. |
| Batches | Output Nodes have a batch setting from 1 to 64. Under normal item-handler behavior, each transfer requests exactly that amount and waits if a source slot cannot supply it. |
| Round robin | A modifier behind an Output Node rotates batches among currently eligible destinations. Destinations that cannot accept a complete batch are skipped. |
| Inventory detection | A Sensor Item Node compares the total item count in its attached inventory against a threshold using `<`, `<=`, `>`, `>=`, `=`, or `!=`. |
| Conditions | Input, Output, and Sensor Nodes can require two colored conditions to be true and two to be false. All configured requirements must pass. |
| Visualization | The controller can display the field outline and animated transfers. Item movement happens immediately; animation does not impose travel time. |

The main execution path is in
[AutoFieldTile](src/main/java/mcjty/ariente/blocks/utility/autofield/AutoFieldTile.java),
especially `tickServer`, `tryProducer`, and `canInsert`. Producer and destination
selection are built by
[ProducerInfo](src/main/java/mcjty/ariente/blocks/utility/autofield/ProducerInfo.java)
and
[ConsumerInfo](src/main/java/mcjty/ariente/blocks/utility/autofield/ConsumerInfo.java).

A node accesses the adjacent block in its attachment direction and requests
`ForgeCapabilities.ITEM_HANDLER` for the contacted face. Consequently, compatible
targets are block inventories exposing that capability on that face. Vanilla
chests, barrels, hoppers, and furnaces provide suitable inventory interfaces;
other mods' storage and machines can participate if they expose the same
interface. These are compatibility expectations from the access code, not a
runtime-tested compatibility list. Machine insertion, extraction, and sided
restrictions still apply. There is no fallback to an unsided capability query or
a plain `Container` wrapper in the node access path.

Ariente's Storage, Negarite Generator, Posirite Generator, Auto Constructor, and
Blueprint Storage now expose their inventory handlers through `@Cap` fields,
without implementing `getCapability` themselves. Storage and both generators use
`CapType.ITEMS`, preserving their existing handler restrictions. Blueprint Storage
and Auto Constructor use `CapType.ITEMS_AUTOMATION`: all six blueprint slots allow
insertion and extraction, while the constructor allows insertion into its 18
ingredient slots and extraction from its six output slots. The ordinary
Constructor crafts using the player's inventory and does not expose a machine
inventory for the field.

The field does not directly access player inventories, dropped items, mobs,
inventory minecarts, or other entities. It has no nodes for fluids, energy
transfer, block placement or breaking, farming, or general world detection. Its
colors are internal conditions, with no direct bridge to redstone or the separate
wireless signal system.

See
[AbstractNodeTile.getConnectedItemHandler](src/main/java/mcjty/ariente/blocks/utility/autofield/AbstractNodeTile.java)
for the target lookup.

Filters are allowlists, with no blacklist, wildcard "everything", mod-name
filter, durability range, or quantity quota. Sample stack counts do not specify
stock targets. Samples are copied from the player's inventory without consuming
the original items. All three matching options are disabled by default.

The GUI's ore option uses shared item tags. Examples include Minecraft planks
and Forge material tags such as `forge:ingots/silver`. Broad Forge categories
such as `forge:ingots` are deliberately excluded. The matcher restricts accepted
Minecraft and Forge tags, while accepting shared tags from other namespaces, so
equivalence depends on the items' actual tags. There is no tag-name editor.
NBT comparison excludes the `Damage` entry because damage has its own switch;
otherwise it compares the complete remaining tag rather than selected keys.

See
[ItemFilterMatcher](src/main/java/mcjty/ariente/blocks/utility/autofield/ItemFilterMatcher.java),
[AbstractItemNodeTile](src/main/java/mcjty/ariente/blocks/utility/autofield/AbstractItemNodeTile.java),
and
[SimpleItemHandler](src/main/java/mcjty/ariente/blocks/utility/autofield/SimpleItemHandler.java).

Sensors count individual items of every type across all slots exposed by the
attached inventory handler. They do not count occupied slots, measure free
capacity or fullness percentage, or filter by item, tag, or NBT. On a machine
that exposes ingredients and products together, both contribute to the count.
A dedicated material inventory is therefore the clearest way to express a
material stock target. The threshold control ranges from 1 to
`Integer.MAX_VALUE`; an empty inventory can be detected with `count < 1`.

There are 16 colored conditions per field. If multiple enabled sensors produce
the same color, any true sensor makes the color true: they are combined with OR.
The requirements on an individual node are combined with AND, including the
negated requirements. Sensors can themselves depend on colors, allowing chained
logic. Recursive evaluation initializes a color to false to break recursion;
feedback loops should not be treated as a supported latch or memory system.
There are no explicit timers, counters, or scripting facilities.

For example, a destination sensor can produce green when `count < 64`, and an
Output Node can require green before sending. This is a refill condition, not an
exact cap:

- A destination with 63 items can receive a batch of 64, reaching 127 if there is
  room. The transfer is not reduced to the one missing item.
- Sensor results are cached through a pass over all producers, then cleared
  before the next pass. One producer also processes multiple source slots before
  its conditions are checked again. Multiple transfers can therefore use the
  same pre-transfer sensor result.
- Even batches of one can overshoot a threshold when multiple source slots or
  nodes supply the inventory during that pass.
- The same behavior limits source rules such as "leave at least N items".

See
[SensorItemNodeTile.sense](src/main/java/mcjty/ariente/blocks/utility/autofield/SensorItemNodeTile.java)
and `canNodeWork`, `getSensorOutput`, and `tickServer` in
[AutoFieldTile](src/main/java/mcjty/ariente/blocks/utility/autofield/AutoFieldTile.java).

Other transfer and layout limitations are:

- A batch must come from one source slot. Two slots containing 32 items each
  cannot satisfy a batch setting of 64. Items with smaller maximum stack sizes
  also require a suitably small batch setting.
- One destination inventory must be able to accept the complete batch in a
  simulated insertion. The batch may occupy several slots in that inventory,
  but is not divided among destination nodes.
- Without round robin, destinations are considered in a deterministic position
  and multipart-slot order, and the first eligible destination wins. There is no
  configurable priority, weighted ratio, or automatic balancing by stock level.
- There is no per-node transfer-rate control beyond batch size and conditions.
  The field's shared power budget limits throughput.
- Field Markers must form a horizontally connected footprint beside the
  controller at its base height. Each node column needs a marker underneath;
  marking only a perimeter does not cover the interior. Nodes beside inventories
  may need markers in different columns from the inventories themselves.
- Height is configurable from 1 to 256. Discovery scans the marker columns from
  their base through the configured height. I found no explicit horizontal-size
  or node-count cap, but larger fields increase discovery and routing work.
- The outline is a bounding box; holes in the marker footprint are not covered
  just because they fall inside that box.
- Operation is confined to one dimension and currently loaded chunks. The field
  provides no chunk loading; chunk load/unload events refresh its discovery
  caches.

The controller requires both negarite and posirite flux. Defaults are:

| Setting | Default |
| --- | --- |
| Accumulation per tick | Up to 400 of each flux |
| Internal operating reserve | 3,200 paired units |
| Transfer cost | `100 + 2 * (item count - 1)` paired units |
| One-item transfer | 100 of each flux |
| Transfer of 64 items | 226 of each flux |

Batching therefore substantially reduces flux use per item. The reserve allows
bursts, so throughput is not a fixed number of transfers each tick. Losing either
flux clears the reserve. The cost and accumulation values are server-configurable;
the table describes defaults rather than a particular world's configuration.
See
[UtilityConfiguration](src/main/java/mcjty/ariente/config/UtilityConfiguration.java)
and
[PowerReceiverSupport](src/main/java/mcjty/ariente/power/PowerReceiverSupport.java).

The initial investigation identified three implementation concerns. The first
two are now fixed; the third remains open:

1. **Fixed: Ariente inventory machines were invisible to the field's sided
   capability lookup.** At the investigated commit, the node called
   `getCapability(capability, side)`, while Storage, both generators, Auto
   Constructor, and Blueprint Storage only overrode `getCapability(capability)`
   to return their item handlers. McJtyLib's inherited sided method dispatches
   through its annotated-capability mechanism and superclass; it does not call
   these single-argument overrides. Replacing those overrides with `@Cap` fields
   supplies the missing route for both transfers and sensors. See the annotated
   handlers in
   [StorageTile](src/main/java/mcjty/ariente/blocks/utility/StorageTile.java),
   [NegariteGeneratorTile](src/main/java/mcjty/ariente/blocks/generators/NegariteGeneratorTile.java),
   [PosiriteGeneratorTile](src/main/java/mcjty/ariente/blocks/generators/PosiriteGeneratorTile.java),
   [AutoConstructorTile](src/main/java/mcjty/ariente/blocks/utility/AutoConstructorTile.java),
   and
   [BlueprintStorageTile](src/main/java/mcjty/ariente/blocks/utility/BlueprintStorageTile.java),
   together with
   [McJtyLib's GenericTileEntity](../McJtyLib/src/main/java/mcjty/lib/tileentity/GenericTileEntity.java).

2. **Fixed: Auto Constructor and Blueprint Storage lacked automation slot
   definitions.** Their `AutomationFilterItemHander` wrappers consult the
   container factory's input/output slot definitions. The original factories
   specified only a slot count, without registering those definitions. In the
   inspected McJtyLib implementation, querying an undefined slot throws
   `IllegalStateException("Bad slot specified!")`. The factories now define each
   slot's input/output permissions, so annotation-based automation can insert and
   extract without that exception. See the factories in the two
   machine classes above,
   [AutomationFilterItemHander](../McJtyLib/src/main/java/mcjty/lib/container/AutomationFilterItemHander.java),
   and
   [ContainerFactory](../McJtyLib/src/main/java/mcjty/lib/container/ContainerFactory.java).

3. **Open: sensor output-color changes have cache and null-handling problems.** The
   shared color click handler marks the node dirty but does not notify the field
   to rebuild its cached grouping of sensors by output color. A sensor can
   therefore remain grouped under its old color until another change invalidates
   that cache. The same handler cycles from black to no color (`null`), while the
   sensor's GUI getter and NBT writer call `ordinal()` without checking for null,
   creating an apparent crash path. See `changeColor` in
   [AbstractNodeTile](src/main/java/mcjty/ariente/blocks/utility/autofield/AbstractNodeTile.java),
   the output-color control and NBT writer in
   [SensorItemNodeTile](src/main/java/mcjty/ariente/blocks/utility/autofield/SensorItemNodeTile.java),
   and `findSensorNodes` in
   [AutoFieldTile](src/main/java/mcjty/ariente/blocks/utility/autofield/AutoFieldTile.java).

The follow-up added
[AutoFieldInventoryTests](src/gameTest/java/mcjty/ariente/AutoFieldInventoryTests.java)
to exercise all-face and unsided inventory access, preservation of inventory
contents and blueprint data across save/load, blueprint and constructor slot
permissions, and a powered field moving a blueprint from Ariente Storage into
Blueprint Storage and back. The transfer test also checks that a Sensor Item Node
detects blueprint storage becoming occupied and empty. These three tests passed
in the full game-test run and in an isolated run containing only
`AutoFieldInventoryTests` (3/3). The Java 17 multiworkspace `:Ariente:build` task passed.
The full suite finished at 40/41, with a failure in the existing holographic
display test `growthOverlapsAnotherBaseAndFullInventoryDrops`.

Existing
[AutoFieldLifecycleTests](src/gameTest/java/mcjty/ariente/AutoFieldLifecycleTests.java)
cover marker ownership and chunk lifecycle behavior. The follow-up does not
establish exhaustive filtering, round-robin, stock-limit, or sensor-color
correctness. The sensor-color defects and approximate stock-limit behavior were
not changed by the capability migration.
