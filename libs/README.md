# libs (optional)

The build resolves Mekanism from **Modrinth's Maven** (`maven.modrinth:mekanism:10.7.19.85`), so this folder can stay
empty and nothing has to be committed here. Any `*.jar` in this folder is ignored by git.

If you prefer an offline build, drop the jars listed below into this folder and replace the dependencies in
`build.gradle` with `implementation files('libs/Mekanism-1.21.1-10.7.19.85.jar')` / `runtimeOnly files(...)`:

| File | Needed for |
|---|---|
| `Mekanism-1.21.1-10.7.19.85.jar` | compiling and running |
| `MekanismAdditions-1.21.1-10.7.19.85.jar` | `runClient` |
| `MekanismGenerators-1.21.1-10.7.19.85.jar` | `runClient` |
| `MekanismTools-1.21.1-10.7.19.85.jar` | `runClient` |
