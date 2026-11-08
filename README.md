# Ariente

Ariente is a mod about technology and exploration

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
./gradlew -I Ariente/tools/mining_tests.gradle -Pariente_runs :ArienteWorld:runGameTestServer
```

It checks all physical blocks, the decorative variants against cobblestone,
the AI Core's 200-tick target and existing ore tool tiers. Test outputs and the
disposable server world are isolated under `build/verification/`.
