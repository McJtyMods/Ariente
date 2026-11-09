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

Publish HoloGui first, then Ariente, then build ArienteWorld. Standalone builds
currently use the published HoloGui version `1.21-0.0.10-beta-6`. Jenkins's
`BUILD_NUMBER` is appended to each mod's published version. When updating this
dependency, update `hologui_version` in both Ariente and ArienteWorld. It can also
be overridden with `-Phologui_version=<published HoloGui version>` in either job.
Include the build-number suffix from the upstream HoloGui job. An additional
dependency repository can be supplied with
`-Pariente_dependency_repository=<repository URL>`.

After Ariente's Jenkins build succeeds, set ArienteWorld's `ariente_version` to
that build's complete published version, or pass
`-Pariente_version=<published Ariente version>` when building ArienteWorld. The
unnumbered base version is not published by Jenkins.

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
