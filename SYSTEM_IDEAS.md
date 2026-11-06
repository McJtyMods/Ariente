# Ariente system ideas and data cable review

Design discussion, 2026-11-05. Current implementation references are for Minecraft
1.20.1 / Forge on branch `1.20`. Expansion ideas below are proposals. Ore
processing is being developed separately; its machines, recipes and balance
remain outside this proposal.

Ariente would benefit most from systems that connect its existing machines,
vehicles, security and holograms. My first choices are **remote item terminals,
data networks with useful controls, fluid automation, and an expanded Warper
network**. Together they give players reasons to build factories, control rooms
and distant outposts. Cameras, automated vehicle stations and utility drones can
then make those installations feel alive.

The strongest theme is recovering alien technology and gradually taking control
of it. A restored installation with named machines, cargo connections, security
zones and a working control desk feels particularly appropriate for Ariente.

## What the data cables currently do

**Data cables exist as blocks and network scaffolding, but there is no working
general data transport system using them in the current source.** They have no
implemented role in moving items, carrying display readings or connecting the
wireless signal devices.

| Part | Current behavior and source |
| --- | --- |
| Cable and connector items | `NETCABLE_DATA` and `CONNECTOR_DATA` are registered with the `DATA` cable color in [Registration](src/main/java/mcjty/ariente/setup/Registration.java) and [CableColor](src/main/java/mcjty/ariente/cables/CableColor.java). |
| Physical cables | They use the common placement, connection rendering, facade support and saved cable-ID machinery in [GenericCableBlock](src/main/java/mcjty/ariente/cables/GenericCableBlock.java), [GenericCableTileEntity](src/main/java/mcjty/ariente/cables/GenericCableTileEntity.java) and [PowerSenderSupport](src/main/java/mcjty/ariente/power/PowerSenderSupport.java). |
| Data transmission | There is no corresponding endpoint discovery, message routing, sensor-value exchange or command protocol for the `DATA` color. A cable ID alone does not implement those functions. |
| Flux transmission | [PowerType](src/main/java/mcjty/ariente/power/PowerType.java) recognizes Negarite, Posirite and Combined cables for flux. [PowerReceiverSupport](src/main/java/mcjty/ariente/power/PowerReceiverSupport.java) has no `DATA` case supplying a machine. |
| Redstone scaffolding | [ConnectorTileEntity](src/main/java/mcjty/ariente/cables/ConnectorTileEntity.java) saves side inputs and outputs. Its `inputFromSide` array is only stored/loaded, and there is no caller of `setPowerOut` in the current Ariente or ArienteWorld sources. [ConnectorBlock](src/main/java/mcjty/ariente/cables/ConnectorBlock.java) reads stored outputs, but no system propagates an input through a data cable to drive another connector. |
| Obtaining them | The built-in cable and connector entries in [BlueprintRecipeRegistry](src/main/java/mcjty/ariente/recipes/BlueprintRecipeRegistry.java) cover Negarite, Posirite and Combined variants. Data cable/connector recipes are absent; [PORTING.txt](PORTING.txt) also records that they were absent in the original recipe catalog. |

The unused redstone fields suggest that wired signaling was an intended
direction, but that is an inference from the remaining scaffolding. They do not
establish a completed feature or the full original design.

Several existing systems already work independently of data cables:

- **Wireless signals** use [RedstoneChannels](src/main/java/mcjty/ariente/blocks/utility/wireless/RedstoneChannels.java)
  and channel devices such as the
  [Signal Transmitter](src/main/java/mcjty/ariente/blocks/utility/wireless/SignalTransmitterTile.java).
  They exchange a redstone value through a shared channel registry.
- **Display modules** save a dimension, position and side, then resolve that
  target directly through [ModuleLink](src/main/java/mcjty/ariente/display/ModuleLink.java).
  A loaded target can therefore already be observed remotely, including in
  another dimension. Unloaded targets report an offline condition. That link
  does not use a cable route.
- **Automation Fields** discover their local nodes and route inventory items
  through [AutoFieldTile](src/main/java/mcjty/ariente/blocks/utility/autofield/AutoFieldTile.java).
- **City AI** uses city associations and the ArienteWorld AI system through
  [AICoreTile](src/main/java/mcjty/ariente/blocks/aicore/AICoreTile.java), rather
  than a player-built data-cable network.

The Java `network` package and display packets are client/server networking;
their existence does not mean that the in-world Data Cable carries them.

One implementation detail matters before building on this infrastructure:
`PowerSenderSupport.matchingColor` treats Combined as a wildcard without
excluding Data. Cable-ID traversal consequently does not establish a clean
boundary between data and power. A future data network should explicitly
separate those domains rather than assume that the current IDs already provide
a sound data topology. This is a source-level concern, not a proposed change in
this document.

## Give data cables a control and telemetry role

The useful purpose for a Data Cable would be a **wired network of named devices,
readings and commands**. A factory controller could read `Refinery output`,
`Negarite fuel`, `Cargo destination available` or `North entrance occupied`, then
act on those values.

A first implementation could have three device types:

1. A **Data Connector** reads an attached machine or inventory. Generic adapters
   could expose item counts, fluid contents and redstone; machine-specific
   adapters could expose working state, progress and faults.
2. A **Control Core** lists connected devices and runs simple rules.
3. A **Display Interface** exposes the readings and permitted commands to
   modular displays.

Start with comparisons, timers and explicit on/off actions. Examples are
"start a craft when fewer than 32 circuits remain", "raise an alarm after a
machine is blocked for 30 seconds", and "disable optional loads while the
shield lacks power". Hysteresis would let players set separate start and stop
thresholds, avoiding a machine switching every tick around one threshold.

The current [display module API](src/main/java/mcjty/ariente/display/DisplayModuleType.java)
already has an interaction hook, so buttons, switches, machine selectors and
acknowledgeable alarms are natural extensions. The meaningful addition is the
ability to control a coherent installation and inspect its state. Existing
Power and Storage modules already provide some remote observation.

Preserve saved direct display links and existing wireless redstone channels.
New data devices should add device discovery, richer readings, permissions,
rules and diagnostics. Later relay blocks could join distant data networks and
carry camera selections or security events. Item stacks and flux would still
use their own transport systems.

A player Control Core should be a separate restored or crafted device. The
existing hostile city AI Core participates in city progression; changing its
meaning would affect generated cities and conquest behavior.

## Remote item logistics

This is probably the most immediately useful expansion. Local Automation Fields
could feed an **Item Transfer Terminal**, while a second terminal delivers to a
field or inventory at another base. Players would build local production cells
and connect them through a small number of cargo links.

For a first version, use explicitly paired terminals in the same dimension.
Give them input/output buffers, filters, a transfer rate and a clear status:
ready, receiving buffer full, destination unloaded, missing power or invalid
link. Existing fields can move items into and out of those buffers, keeping
their local role useful.

Important behavior would be:

- Both flux components contribute to the operating cost, with a distance
  cost or a bounded upgrade scheme.
- Items remain safely at the source while the destination cannot accept them.
  Saving, unloading and breaking either endpoint must not duplicate or lose
  cargo.
- A link identifies a particular destination device. Replacing a block at the
  same coordinates must not silently redirect an old shipment.
- A player can publish a destination and decide who may send or receive there.
  Guessing an address should not grant inventory access.
- Stock targets and priorities can follow later. The first version only needs
  reliable filtered transfer between two endpoints.

Cross-dimension terminals would make a good advanced unlock, using Warper
technology or an upgraded terminal component. They need their own costs and
throughput rules; a player teleport and a continuous stream of cargo are
different operations even if they share an addressing system.

Keep a reason to use physical cargo transport. Carts can offer cheap bulk
movement along established routes, ships can support exploration and setting up
new outposts, and terminals can offer compact, expensive or rate-limited remote
delivery. An early unlimited interdimensional pipe would make much of that
infrastructure unnecessary.

## Expand Warpers into a travel network

Reusing Warper technology is a good fit. Add **named, linked destinations** so a
player can travel between a home base, a conquered city and a remote mining or
survey station. A destination list could be a modular display or a Warper's own
hologram.

The first version could support two linked player-built pads within one
dimension. Add a linking item, destination names, access rules and a powered
activation cost. Later upgrades could support several destinations and travel
between dimensions.

The existing [Warper](src/main/java/mcjty/ariente/blocks/utility/WarperTile.java)
has a specific role: charged entrance travel between the Overworld and
ArienteWorld, with return-location handling. Its pearl charge is an unlock
condition, not an existing per-trip flux bill. Preserve that behavior for
current worlds. New networked Warpers can introduce operating costs without
turning an old entrance into an unexpectedly unusable return route.

Require a destination to be discovered or placed before linking it. This
preserves exploration and prevents a travel menu from bypassing the initial
ArienteWorld entrance or unopened progression areas. Server-side checks should
verify access and a safe arrival space before charging for the trip. Any chunk
loading needed to complete arrival should be temporary and bounded.

A portable return beacon could be a later expedition tool: choose a previously
authorized destination, carry fuel, and accept a cooldown or a vulnerable
activation period. Vehicle transport through gates is a separate, larger
feature and can follow ordinary player travel.

## Extend local automation to fluids

**Fluid input/output nodes, storage tanks and a pump** would add another useful
factory resource without requiring a second local pipe system. Automation
Fields could route fluids between attached machines, with fluid filters,
transfer limits and tank-level conditions.

Fluid display support already exists in
[FluidDisplayModule](src/main/java/mcjty/ariente/display/FluidDisplayModule.java).
Actual fluid storage and transfer would therefore give those modules a stronger
role within an Ariente factory. The Negarite/Posirite generator tanks are
generation extensions, so introduce distinct liquid storage blocks with clear
names and visuals.

A first fluid release only needs tank-to-tank transfer and a way to obtain a
useful fluid. Choose the first consumer together with the ore-processing
design, or support fluid handlers supplied by other installed mods. Fluids
become interesting when they create supply, reuse or waste-handling decisions;
an otherwise unused tank and pump would add little.

Forge Energy transfer nodes are another possible integration layer. The
[Forge Energy display module](src/main/java/mcjty/ariente/display/ForgeEnergyDisplayModule.java)
can already read compatible external machines, but reading their energy is not
the same as supplying it. Optional FE automation should keep Ariente's paired
flux and any conversion rules explicit.

## Stock control and crafting on demand

The Constructor, Auto Constructor, blueprint library and storage blocks provide
most of the visible building blocks for a **crafting request terminal**. A
player could request 16 machine components, see missing ingredients, and watch
the available Auto Constructors produce them.

Start smaller than a universal crafting network: one controller, several named
inventories, a few Auto Constructors and explicit stock targets. Useful early
rules include keeping fuel dust in supply, maintaining a reserve of circuits,
and crafting replacement modules after taking them from storage.

The important distinction is between deciding that production is needed and
actually moving/consuming the right quantities. Current field conditions are
useful switches, but do not provide exact stock reservations or a dependency
planner. A request system would need to track reserved inputs, concurrent jobs,
available outputs and interrupted work. The interface should explain which
ingredient, machine, power supply or output space is preventing progress.

Later additions could include chained recipes, job queues and remote requests.
Existing storage blocks can remain the real inventories behind the terminal;
there is no need to introduce a new digital-storage mechanic just to offer a
unified search and request screen.

## Cameras and player tracking

These would make modular displays much more distinctive. I would separate
**detection and maps** from **live camera rendering**, because the first delivers
useful gameplay with much less rendering complexity.

An initial security sensor could report nearby players or selected entity
types, whether a zone is occupied, and the last detection time. Display modules
could show a local radar map, an authorized-player list, or an event log such as
"West gate opened" and "Unrecognized player detected in storage". Players could
also carry an optional tracking beacon for friendly position, ship location
and expedition rendezvous markers.

Make tracking a property of installed sensors or carried beacons. A range,
loaded sensor location and defined detection rules give the system a physical
presence. An eventual interaction with the Scramble armor module could create
counterplay, but it would be a new capability; its existing sentinel-related
behavior does not already provide that integration.

Live cameras could then add fixed views, pan/tilt controls and selectable feeds
on a display. Begin with one same-dimension feed and a limited update rate.
Viewing a remote world area requires the client to have appropriate scene
data, not merely a server-side list of entities. Cross-dimension views, multiple
simultaneous feeds and off-screen rendering make this one of the more demanding
ideas. Displays should show a disconnected or unloaded feed clearly instead of
presenting an old image as current.

## Player security and access management

There is room for a useful player-owned security system alongside the current
locks, alarms and city key cards. A **Key Card Encoder** and a security terminal
could define groups such as owner, builder, cargo operator and visitor, then
assign access to doors, storage, Warpers, displays and remote terminals.

This fills an existing gap: the current key cards and security tags support city
mechanics, while the [gameplay guide](ARIENTE_GUIDE.md) notes the absence of a
complete survival interface for issuing arbitrary personal security tags.
Player-issued permissions should have their own ownership rules and must not
manufacture access to an unconquered city's systems.

A small first release would let the owner issue and revoke a card, assign a few
doors, and trigger an alarm for an unrecognized sensor detection. Data-linked
security zones, visitor time limits, event logs and a control-room map could
follow. Exposing controls on a public hologram should not grant every viewer
permission to operate the attached machines.

## Power infrastructure and diagnostics

A **network analyzer** would be useful even before another major machine.
Extend the Tablet or add a diagnostic mode that identifies connected networks,
available Negarite and Posirite, missing connections, machine demand and the
reason a consumer is stopped. It should answer practical questions such as
"why is the shield flickering when the refinery starts?"

Ariente's cable power is currently a continuing supply, not a long-term battery.
The generator tanks raise generation, the Flux Capacitor item is a crafting
component, and the Energy Holder stores equipment fuel. The
[power section of the guide](ARIENTE_GUIDE.md) describes those distinctions.

A new **paired flux accumulator** could provide short backup periods and cover
bursts of demand. It would explicitly store both components, have a limited
charge/discharge rate and expose its reserve to displays. Fuel consumption,
charging cost and any losses need balancing around the existing generation
model; it should not turn excess or repeatedly converted flux into free fuel.

Branch switches and load priorities are another useful addition. Players could
reserve supply for security or travel, then let refining and bulk crafting use
what remains. Demand-controlled generation could reduce wasted fuel. The
existing free doors and elevators can keep their current basic operation;
advanced automation does not require adding a power bill to every convenience.

## Vehicle stations and utility drones

Vehicles gain value when bases have useful places for them to go.

- **Cargo cart stations:** load and unload a cargo cart, select a destination,
  wait for a full load or a timer, and return along a defined route. Begin with
  two stations and predictable track layouts before adding junction routing.
- **Ship docking stations:** mark a landing location, show available charging
  and cargo services, and offer a return waypoint. Assisted docking could come
  before autonomous flight.
- **Utility drones:** choose one bounded first job, such as moving supplies
  between two docking pads or constructing a small approved structure from a
  supplied inventory. Give the drone a home, operating range and clear behavior
  when its destination becomes unavailable.

These can share station names, access rules and status modules. Carts offer
physical bulk logistics; ships connect exploratory outposts; drones offer local
or specialized assistance. General drone navigation, mining, combat, cargo and
building should be separate expansions so the first useful drone does not
depend on solving all of them.

## Other promising systems

| System | Useful first scope | How it fits Ariente |
| --- | --- | --- |
| Geological scanner | Survey an area and mark likely resource sites or unusual underground structures. | Gives ships and expeditions destinations and makes remote supply bases worthwhile. |
| Site extractor | A powered, bounded mining installation at a surveyed site, with cargo output and clear exhaustion rules. | Adds a resource source for logistics. Keep it separate from the ore-processing chain and preserve reasons to explore for deposits. |
| Holographic construction projector | Show a saved structure as a ghost preview and list missing materials. Automated placement can follow later. | Uses the visual identity of holograms and helps players build stations, fields and factories. Structure plans would be distinct from existing item-recipe blueprints. |
| Salvage and repair station | Recover a limited portion of components from unwanted equipment, or service compatible gear and modules. | Gives captured alien equipment value after the player has better replacements. Recycling must lose value across crafting loops. |
| Technology analyzer | Analyze recovered artifacts to reveal a blueprint or a clue to a related ruin. | Connects exploration to technology without requiring a large research tree. Preserve the value and reusability of existing blueprints. |
| Central storage terminal | Search named physical inventories and request a stack from a local collection point. | Builds on Storage Cubes and displays while keeping cargo, capacity and transport visible. |
| Agricultural field modules | Plant, harvest and replant crops within a bounded field using supplied seeds and output storage. | Makes the Automation Field useful for another kind of local production. Alien crops or biological materials could support later recipes. |
| Environmental controller | Maintain a small cultivation area with supplied water and a defined environmental effect. | Offers a more distinctive alternative to another generic machine tier, once there is a real crop or process that benefits. |
| Outpost anchor | Keep a small, explicitly selected operating area active at a continuing cost, with server limits. | Supports remote production, cargo stations and sensors. Add it after remote systems have reliable offline behavior. |

The construction projector, diagnostics and personal security tools are good
smaller additions. Autonomous extraction, large research trees and general
purpose drones have a greater chance of becoming separate projects. I would
introduce them when the logistics and control systems can already give their
outputs a purpose.

## Suggested order

| Stage | Additions | Result for the player |
| --- | --- | --- |
| 1 | Remote item-terminal pair; diagnostic Tablet mode; a small set of data endpoints and interactive display controls. | Link an outpost to a base and understand why a machine or shipment is waiting. The cargo pair can ship independently of the full data controller. |
| 2 | Fluid field nodes and real tanks; stock targets; simple controller rules. | Build a factory that maintains supplies and responds to demand. Coordinate the first fluid consumer with ore processing. |
| 3 | Linked Warper destinations; personal access management; entity sensors and map modules. | Travel between established bases and manage an installation from its control room. |
| 4 | Cargo cart stations, ship docking, a bounded outpost anchor and a first utility drone. | Establish a network of active sites with visible traffic and maintenance needs. |
| 5 | Live camera feeds, more autonomous vehicles, advanced extraction or artifact research. | Add ambitious features to infrastructure that already has useful jobs. |

Within stage 1, my first concrete feature would be **two powered item terminals
connected to existing Automation Fields**. For data cables, the first convincing
demonstration would be **a control desk that reads a machine's status and can
start or stop a permitted operation**. Both produce an immediately understandable
improvement without requiring every later system.

Two examples show how the proposed pieces could work together:

- A surveyed resource outpost extracts material into a local field. A cargo
  station or transfer terminal supplies the main base. Its control room shows
  stock and faults, and the player flies out or uses a discovered Warper to
  service it.
- A processing base maintains circuit stock through its Auto Constructors.
  A low fuel reserve suspends optional production, a sensor raises an entrance
  alarm, and an authorized player uses a display to inspect the affected area
  and acknowledge the event.

## Design choices that should carry across these systems

**Keep the three jobs distinct.** Flux cables supply power, Automation Fields
handle local resources, and data networks carry readings and control. Cargo
terminals or vehicles connect distant resource endpoints. This lets each
system grow without making the others obsolete.

**Show a reason when something stops.** Missing either flux component,
unavailable destination, full inventory, absent permission and missing recipe
are different conditions. Report them on the device and on its display module.
Mark stale readings with an age or offline state.

**Treat remote operation and chunk loading as separate features.** A saved link
does not make a machine tick. Start with loaded endpoints, then add explicit,
bounded loading where the gameplay needs it. Teleport arrival may need temporary
loading; remote factories need a separately controlled operating policy.

**Cache discovery and limit recurring work.** Data values, stock queries and
camera updates should refresh at appropriate rates. A larger screen or more
stations should not cause every device to rescan the world every tick.

**Keep ownership and progression consistent.** A remote click needs the same
authority as an on-site action. Player-built systems should work without
ArienteWorld; recovered city technology can provide an additional progression
route when the companion is installed.

**Develop one useful loop at a time.** A named pair of terminals, a readable
sensor and a reliable control button are enough for an early release. Larger
networks, richer rules and multiple dimensions can extend those working pieces.
