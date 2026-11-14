# Ariente

Ariente is a mod about technology and exploration

## Building and Jenkins

For Minecraft 1.21.1 / NeoForge, select Java 21 and **Use Gradle Wrapper** in
Jenkins (Gradle 8.14.2). Use tasks `clean build publish`, equivalent to:

```sh
./gradlew clean build publish
```

NeoGradle manages mapped dependencies through build tasks, so no ForgeGradle 5
cache workaround is needed. Set `local_maven` to the Jenkins Maven repository
directory, or override it with `-Pmavendir=...`. Without either setting, `publish`
writes to the local Maven cache. The publication includes the mod jar, API jar
and POM.

Standalone compilation uses the published Jenkins HoloGui artifact
`1.21-0.0.10-beta-6`. This compilation version is independent of the runtime
minimum `1.21-2.0.0` declared in `neoforge.mods.toml`. Jenkins's `BUILD_NUMBER`
is part of the Maven artifact version; it does not need to match the release
uploaded by ReleaseMe. All dependency artifact versions are centralized in the
matching MultiWorkspace `gradletools.gradle`; do not override them in the mod's
`gradle.properties`.

ArienteWorld similarly compiles against the published Ariente artifact
`1.21-0.0.31-alpha-2`, while requiring Ariente `1.21-2.0.0` at runtime. Release
HoloGui first, then Ariente, then ArienteWorld. A new Jenkins publication is not
required when those existing compilation artifacts provide the needed API.
An additional compilation repository can be supplied with
`-Pariente_dependency_repository=<repository URL>`.

## Gameplay and development

See [the gameplay and systems guide](ARIENTE_GUIDE.md) for the Forge 1.20.1
development port, including machines, power, progression, recipes, and current
limitations.

Rendering uses Minecraft and Forge APIs. Holograms, lasers, connected textures,
and emissive surfaces do not require Embeddium or Oculus. Shader packs can add
effects such as bloom; those extra effects are not part of the vanilla renderer.


Ordinary marble, marble-tech and black-tech decorative blocks mine no slower
than cobblestone with the same pickaxe. Stone and metal machines, cables and
multipart blocks also support normal pickaxe speed and harvest checks. The AI
Core takes about **10 seconds with an unenchanted netherite pickaxe** while
retaining blast resistance 800. Vanilla mining modifiers still apply.

The focused mining regression uses actual loaded tags and player mining progress.
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


Development target: Minecraft 1.21.1, NeoForge 21.1.215, Java 21, branch `1.21_neo`.
Build with `./gradlew build` standalone, or `./gradlew :Ariente:build` from MultiWorkspace.
The workspace uses the included McJtyLib and The One Probe sources. Ariente and ArienteWorld also use included HoloGui/Ariente sources. Published Maven artifacts are standalone fallbacks.
Unpublished port dependencies can be provided with `-Pariente_dependency_repository=<repository URL>`; `-Pgradletools_path=<file>` selects an explicit shared Gradle script.

The [NeoForge port checkpoint](../ArienteWorld/PORT_1_21.md) documents current commands and validation. Earlier Forge fixture commands in this file are historical.

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
`./gradlew :ArienteWorld:runGameTestServer -I Ariente/tools/recent_port_tests.gradle`.
The disposable test worlds and reports live under `build/verification/`. The checks cover
legacy saves and dimension travel, named card destinations, expiry/pinning, all 31
reset recipes in inventory/table slots, Door Marker previews, and cube landing geometry.
Add `-Precent_without_world` and run `:Ariente:runGameTestServer` to check Ariente
without the companion mod in a separate disposable world.
