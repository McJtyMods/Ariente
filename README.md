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
