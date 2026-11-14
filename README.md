## Fabric 1.21.1 development

This branch is `1.21_fabric` and requires Java 21. In MultiWorkspace, build this mod with `./gradlew :Ariente:build` from the workspace root. Individual checkouts use the same `gradletools.gradle` driver, preferring local peer sources before the Jenkins/Maven fallback. Before the remote branch is available, select the workspace script with `-Pgradletools_path=../gradletools.gradle` and use `--gradle-user-home ../.gradle-user-home`.

# Ariente

Ariente is a mod about technology and exploration

## Building and Jenkins

Use Java 21 and the Gradle 8.14.2 wrapper for Fabric 1.21.1. Jenkins can run
`clean build publish`. Set `local_maven` to the Maven repository directory, or
use `-Pmavendir=...`; otherwise publishing uses the local Maven cache. The
publication includes the remapped mod jar, API jar and POM.

The shared `1.21_fabric/gradletools.gradle` controls Fabric dependencies and
prefers included local peers. Isolated builds use Fabric Maven coordinates.
Jenkins appends `-BUILD_NUMBER` to this mod's version; runtime minimums are
in `fabric.mod.json`. Dependency revision defaults and command-line overrides
are documented in the MultiWorkspace README.

Publish HoloGui before Ariente, then ArienteWorld, so standalone jobs can resolve their Fabric dependencies.

## Gameplay and development

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

## Historical NeoForge checks

These scripts retain their original NeoGradle configuration and are not enabled by the Fabric build. The focused mining regression uses actual loaded tags and player mining progress.
Run from the matching multiworkspace with its Java version selected:

```sh
./gradlew -I Ariente/tools/mining_tests.gradle :ArienteWorld:runGameTestServer
```

It checks all physical blocks, the decorative variants against cobblestone,
the AI Core's 200-tick target and existing ore tool tiers. Test outputs and the
disposable server world are isolated under `build/verification/`.

The equipment regression runs `/ariente equip`, checks the installed modules and
power budgets, then uses all four armor panels to remove, reinstall, activate
and hotkey-toggle the modules. It includes the installed but disabled forcefield,
weapon module installation and rejection of armor in the wrong slot. Run from
`Multi21_neo` with Java 21:

```sh
TMPDIR=/var/tmp ./gradlew -I Ariente/tools/equipment_tests.gradle :Ariente:runGameTestServer
```

Outputs use `Ariente/build/verification/equipment-tests/`. The 1.21.1 equip
command and armor panels pass this check without gameplay code changes.


Development target: Minecraft 1.21.1, Fabric, Java 21, branch `1.21_fabric`.
Build with `./gradlew build` standalone, or `./gradlew :Ariente:build` from MultiWorkspace.
The workspace uses the included McJtyLib and The One Probe sources. Ariente and ArienteWorld also use included HoloGui/Ariente sources. Published Maven artifacts are standalone fallbacks.
`-Pgradletools_path=<file>` selects an explicit shared Gradle script; dependency artifact versions can be overridden as described in the MultiWorkspace README.

The [NeoForge port checkpoint](../ArienteWorld/PORT_1_21.md) and the Forge fixture commands above are historical references. Fabric visual validation uses ArienteWorld's tutorial generator and preview.

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
`./gradlew :ArienteWorld:runRecentTests -I ArienteWorld/tools/recent_port_tests.gradle`.
The disposable test worlds and reports live under `build/verification/`. The checks cover
legacy saves and dimension travel, named card destinations, expiry/pinning, all 31
reset recipes in inventory/table slots, Door Marker previews, and cube landing geometry.
