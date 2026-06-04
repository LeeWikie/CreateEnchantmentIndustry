# Create Enchantment Industry for Create-Fly

Fabric port of Create: Enchantment Industry targeting Minecraft 26.1.2 and Create-Fly.

## Requirements

- Java 25
- Minecraft 26.1.2
- Fabric Loader 0.19.2 or newer
- Fabric API 0.148.2+26.1.2
- Create-Fly 26.1.2 build jar

## Building

By default the Gradle build expects Create-Fly to be checked out next to this project and built at:

```text
../Create-Fly/build/libs/create-fly-26.1.2-6.0.9-3.jar
```

You can override that path with a Gradle property. Use the included wrapper, or an installed Gradle 9.4+ if the wrapper distribution download is unavailable in your environment:

```bash
./gradlew clean build -Pcreate_fly_jar=/absolute/path/to/create-fly-26.1.2-6.0.9-3.jar
# or
gradle clean build -Pcreate_fly_jar=/absolute/path/to/create-fly-26.1.2-6.0.9-3.jar
```

The main jar is generated under `build/libs/`.

## Current Scope

This port includes the core CEI content and systems for Create-Fly:

- Liquid Experience and Super Experience materials
- Experience Hatch and Experience Lantern
- Printer
- Mechanical Grindstone and Grindstone Drain
- Blaze Enchanter and Blaze Forger
- Printing and grinding recipes
- Fabric fluid storage, JSON config, Create-Fly movement/storage/arm behavior hooks, and safe optional integration omission

Optional JEI/Ponder/Curios/TLM/Aeronautics/Sable integrations are intentionally omitted until compatible Fabric/Create-Fly APIs are available.

## Dev Build

To produce a development jar with verbose diagnostic logging:

```bash
./gradlew clean build -Pmod_version=0.1.0-dev.1
```

This overrides `mod_version` at build time without modifying `gradle.properties`. The resulting jar version contains `dev`, which triggers `[CEI DEV DEBUG]` output in the Minecraft log.

### Dev Debug Logs

When the mod version contains `dev` (for example `0.1.0-dev.1`) or Fabric Loader reports a development environment, CEI logs `INFO`-level diagnostics prefixed with `[CEI DEV DEBUG]` during startup. Look for these lines in the Minecraft game log (`logs/latest.log`). The output includes:

- Mod version, environment type, Fabric Loader / Minecraft / Fabric API / Create versions
- Config file path and a summary of all settings (kinetics, fluids, enchantments, processing, stress)
- Registry and content overview (blocks, items, fluids, block entities, recipe types, behaviour hooks)
- No per-tick logging is added

The debug output is **disabled by default** in the stable release (`0.1.0`). Production jars without `dev` in the version string will not emit `[CEI DEV DEBUG]` lines.

## License

This fork follows the original Create Enchantment Industry license: LGPL-3.0-or-later. See `LICENSE.txt`.
