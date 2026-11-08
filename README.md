# Ariente

Ariente is a mod about technology and exploration

See [the gameplay and systems guide](ARIENTE_GUIDE.md) for the Forge 1.20.1
development port, including machines, power, progression, recipes, and current
limitations.

Rendering uses Minecraft and Forge APIs. Holograms, lasers, connected textures,
and emissive surfaces do not require Embeddium or Oculus. Shader packs can add
effects such as bloom; those extra effects are not part of the vanilla renderer.


Development target: Minecraft 1.21.1, NeoForge 21.1.215, Java 21, branch `1.21_neo`.
Build with `./gradlew build` standalone, or `./gradlew :Ariente:build` from MultiWorkspace.
The workspace uses the included McJtyLib and The One Probe sources. Ariente and ArienteWorld also use included HoloGui/Ariente sources. Published Maven artifacts are standalone fallbacks.
Unpublished port dependencies can be provided with `-Pariente_dependency_repository=<repository URL>`; `-Pgradletools_path=<file>` selects an explicit shared Gradle script.

The [NeoForge port checkpoint](../ArienteWorld/PORT_1_21.md) documents current commands and validation. Earlier Forge fixture commands in this file are historical.
