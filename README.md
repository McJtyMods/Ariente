# Ariente

Ariente is a mod about technology and exploration

See [the gameplay and systems guide](ARIENTE_GUIDE.md) for the Forge 1.20.1
development port, including machines, power, progression, recipes, and current
limitations.

Rendering uses Minecraft and NeoForge APIs. Holograms, lasers, connected textures,
and emissive surfaces do not require Embeddium or Oculus. Shader packs can add
effects such as bloom; those extra effects are not part of the vanilla renderer.


Ordinary marble, marble-tech and black-tech decorative blocks mine no slower
than cobblestone with the same pickaxe. Stone and metal machines, cables and
multipart blocks also support normal pickaxe speed and harvest checks. The AI
Core takes about **10 seconds with an unenchanted netherite pickaxe** while
retaining blast resistance 800. Vanilla mining modifiers still apply.

The tutorial generator checks real player mining progress against loaded tags,
including decorative blocks versus cobblestone and the AI Core's 200-tick target.
From the sibling ArienteWorld checkout, use Java 25 and run:

```sh
python3 tools/create_tutorial.py
python3 tools/create_tutorial.py --verify --saves ../Multi_neo/workspace/run/saves
```

Storage and world integration GameTests run from `Multi_neo` with
`./gradlew :ArienteWorld:runGameTestServer`. The older `tools/mining_tests.gradle`
fixture belongs to the 1.21.1 baseline; use the tutorial checks on this branch.

The equipment regression runs `/ariente equip`, checks the installed modules and
power budgets, then uses all four armor panels to remove, reinstall, activate
and hotkey-toggle the modules. It includes the installed but disabled forcefield,
weapon module installation and rejection of armor in the wrong slot. Run from
`Multi_neo` with Java 25:

```sh
TMPDIR=/var/tmp ./gradlew -I ../Ariente_neo/tools/equipment_tests.gradle :Ariente:runGameTestServer
```

Outputs use `Ariente_neo/build/verification/equipment-tests/`. The 26.2 GUI
resolves armor slots through the player's equipment rules; NeoForge's optional
item slot override alone does not identify ordinary equippable armor. Existing
module data, activation states and hotkeys are preserved.

Development target: Minecraft 26.3, NeoForge 26.3.0.83, Java 25, branch `26.3_neo`.
Build with `./gradlew build` standalone, or `./gradlew :Ariente:build` from MultiWorkspace.
The workspace uses the included McJtyLib and The One Probe sources. These ports require McJtyLib **26.3-11.0.4** or newer. Ariente and ArienteWorld also use included HoloGui/Ariente sources. Published Maven artifacts are standalone fallbacks.
Unpublished port dependencies can be provided with `-Pariente_dependency_repository=<repository URL>`; `-Pgradletools_path=<file>` selects an explicit shared Gradle script.

The [earlier 26.1.2 port report](../ArienteWorld_neo/PORT_26_1_2.md) records the previous port’s migration and validation history. Earlier Forge fixture commands in this file are historical.

### Minecraft 26.2 port (previous branch)

This branch carries the latest committed 26.1.2 NeoForge implementation forward.
Holograms and procedural geometry use extracted batches and native 26.2 staged
vertex buffers; render submissions, screen access, camera extraction, colored
vanilla content and tag generators use the 26.2 APIs. World effects follow the
reversed depth buffer. The snapshot camera retains its own forward-depth projection.

Live cameras select loaded terrain through the mounted lens without moving the
player's chunk storage or changing its occlusion graph. Terrain fades completely
at the configured block radius, including the eight- and sixteen-block ranges,
so distant chunk edges do not flicker or leave black silhouettes.

Patchouli has no listed Minecraft 26.2 release. The tablet/manual remains optional
and reports its unavailability; this branch does not load an older Patchouli jar.
McJtyLib 26.2-10.0.3 supplies the model helpers required by these ports. Publish
McJtyLib, HoloGui and Ariente in that order before distributing the standalone
companion. Maven fallbacks use the matching 26.2 versions.

From `../Multi_neo`, with Java 25 selected:

```sh
./gradlew :McJtyLib:build :ModHoloGui:build :Ariente:build :ArienteWorld:build :workspace:build
./gradlew :Ariente:runClientData :ArienteWorld:runClientData
./gradlew :ArienteWorld:runGameTestServer
```

The existing ArienteWorld `PORT_26_1_2.md` report and its recorded results apply to the previous
Minecraft branch. Minecraft 26.2 validation is recorded below.

Workspace and standalone builds, both data generators, ten native GameTests,
McJtyLib's 11 JUnit tests, the integrated client smoke and the tutorial's camera
checks pass on 26.2. See the [ArienteWorld validation notes](../ArienteWorld_neo/README.md#minecraft-262-port)
for the test scope and playable Tutorial save.

### Minecraft 26.3 port

This branch ports the committed 26.2 NeoForge sources and tutorial tooling to
26.3. See the [26.3 port and verification report](../ArienteWorld_neo/PORT_26_3.md) for the
current build commands, tutorial save, test results and remaining manual checks.
