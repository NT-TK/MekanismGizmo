# libs

The build needs Mekanism's development jars here. They are **not** part of this repository (they are third-party
binaries), so drop them into this folder yourself:

| File | Required |
|---|---|
| `Mekanism-1.21.1-10.7.19.85.jar` | yes (compilation and runtime) |
| `MekanismAdditions-1.21.1-10.7.19.85.jar` | only for `runClient` |
| `MekanismGenerators-1.21.1-10.7.19.85.jar` | only for `runClient` |
| `MekanismTools-1.21.1-10.7.19.85.jar` | only for `runClient` |

Download Mekanism 10.7.19 for Minecraft 1.21.1 (CurseForge / Modrinth, MIT licensed) and copy the jar files with
exactly these names. `build.gradle` references them by name.
