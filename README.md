# Ariente

Ariente is a mod about technology and exploration

See [the gameplay and systems guide](ARIENTE_GUIDE.md) for the Forge 1.20.1
development port, including machines, power, progression, recipes, and current
limitations.

Rendering uses Minecraft and Fabric APIs. Holograms, lasers, connected textures,
and emissive surfaces do not require Embeddium or Oculus. Shader packs can add
effects such as bloom; those extra effects are not part of the vanilla renderer.


Ordinary marble, marble-tech and black-tech decorative blocks mine no slower
than cobblestone with the same pickaxe. Stone and metal machines, cables and
multipart blocks also support normal pickaxe speed and harvest checks. The AI
Core takes about **10 seconds with an unenchanted netherite pickaxe** while
retaining blast resistance 800. Vanilla mining modifiers still apply.


## Fabric development

Targets Minecraft 26.2 with Java 25, on local branch `26.2_fabric`.
Build with `./gradlew build` in this checkout, or `./gradlew :Ariente:build`
from the sibling `Multi_fabric` workspace. Included McJtyLib, TOP, HoloGui and
Ariente checkouts are used in compilation and development runs. Isolated builds
need matching Fabric Maven artifacts; `-Pgradletools_path=<file>` selects an
explicit shared build script.

Requires McJtyLib **26.2-10.0.3-fabric** or newer. HoloGui, Ariente and
ArienteWorld have separate common/client Fabric entrypoints and use native
Fabric networking, events, registry access and transactional storage.

From `ArienteWorld_fabric`, `python3 tools/create_tutorial.py` builds and checks
the playable tutorial. `--verify` restarts its saved world; `--saves` installs a
copy for the Fabric workspace client. Datagen uses `:Ariente:runDatagen`.

Optional Patchouli manuals use McJtyLib's existing Fabric integration, which
currently reports that manuals are unavailable. The Tutorial save includes a
written guide.

It checks all physical blocks, the decorative variants against cobblestone,
the AI Core's 200-tick target and existing ore tool tiers. Test outputs and the
disposable server world are isolated under `build/verification/`.

## Warper destinations

Warpers can be made with a rare Constructor blueprint; see the
[Warper material list](ARIENTE_GUIDE.md#teleportation-cards-and-named-warpers).

Teleportation Cards are Constructor blueprint recipes: four Ender Pearls, two
Silver Ingots and one Circuit. Name a Warper in its hologram, then sneak-right-click
it with the card to save its address. Right-click the card to choose among up to
64 destinations, with names, coordinates, dimensions and a captured 32×32 preview.
Selecting refreshes the destination name; opening the card also refreshes names
of Warpers already loaded on the server. The source Warper hologram and The One
Probe show the destination name with its coordinates; TOP also shows the source
Warper name. Destination names are cached for unloaded targets.

Apply the selected address by right-clicking another Warper with the card, then
step into its rotating miniature. Players, unmounted mobs and dropped items can
travel within or across dimensions. Keep the arrival space clear. Arrivals remain
exempt from that receiving pad until they leave, preventing reciprocal links from
bouncing travelers back. A removed destination fails safely; replacing the Warper
at the same coordinates restores the address without re-encoding the card.

Configuration alone never starts expiry. Successful travel starts a 600-tick
(30-second) delay, restarted by each later successful teleport. Pin disables expiry;
unpin uses the last successful teleport, so an elapsed delay clears immediately.
Clear restores the original behavior. With Ariente World installed, Overworld and
Ariente pads retain the charged Warp to Ariente / Warp to Overworld buttons and
existing return-trip data.

The preview reuses camera block geometry, captured once per selection rather than
streaming a remote world. It shows connected surfaces in a 32×32 footprint, from
four blocks below to 27 above the pad, with bounded palette/block counts. The card
stores only addresses and names; terrain is sent separately and one selected
snapshot is cached per player. Applying a card reuses that snapshot when available.
Warpers persist their snapshot and settings. No remote chunks stay force-loaded.

Run the recent port regressions from the workspace with
`./gradlew :ArienteWorld:runRecentTests -I ../ArienteWorld_fabric/tools/recent_port_tests.gradle`.
The disposable test worlds and reports live under `build/verification/`. The checks cover
legacy saves and dimension travel, named card destinations, expiry/pinning, all 31
reset recipes in inventory/table slots, Door Marker previews, and cube landing geometry.
