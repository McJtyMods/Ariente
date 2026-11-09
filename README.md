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

Outputs use `Ariente_neo/build/verification/equipment-tests/`. The 26.1.2 GUI
resolves armor slots through the player's equipment rules; NeoForge's optional
item slot override alone does not identify ordinary equippable armor. Existing
module data, activation states and hotkeys are preserved.

Development target: Minecraft 26.1.2, NeoForge 26.1.2.99, Java 25, branch `26.1.2_neo`.
Build with `./gradlew build` standalone, or `./gradlew :Ariente:build` from MultiWorkspace.
The workspace uses the included McJtyLib and The One Probe sources. These ports require McJtyLib **26.1.2-10.0.2** or newer. Ariente and ArienteWorld also use included HoloGui/Ariente sources. Published Maven artifacts are standalone fallbacks.
Unpublished port dependencies can be provided with `-Pariente_dependency_repository=<repository URL>`; `-Pgradletools_path=<file>` selects an explicit shared Gradle script.

The [26.1.2 port report](../ArienteWorld_neo/PORT_26_1_2.md) documents current commands and validation. Earlier Forge fixture commands in this file are historical.
