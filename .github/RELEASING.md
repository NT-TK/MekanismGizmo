# Releasing

This repository publishes two editions from two long-lived branches:

| Branch | Minecraft | Loader | Release tag |
|---|---|---|---|
| `1.21.1` | 1.21.1 | NeoForge 21.1.194+ | `v<version>-neoforge-1.21.1` |
| `1.20.1` | 1.20.1 | Forge 47.4.0+ | `v<version>-forge-1.20.1` |

Use a distinct tag for each edition. Both tags match their branch's Release workflow trigger.

## 1.21.1 / NeoForge

1. Set `mod_version` in `gradle.properties` to the version being released.
2. Add the same version to `CHANGELOG.md` and update the README if the requirements changed.
3. Run `gradlew.bat build` and check `build/libs/mekanismgizmo-1.21.1-<version>.jar`.
4. Commit the changes and push the `1.21.1` branch. Wait for the **Build** workflow to pass with Java 21.
5. Create and push an annotated tag:

   ```bat
   git tag -a v<version>-neoforge-1.21.1 -m "Mekanism Gizmo <version> for NeoForge 1.21.1"
   git push origin v<version>-neoforge-1.21.1
   ```

6. The **Release** workflow builds the jar and creates the GitHub release with the changelog. This release is created
   with `--latest=true`, so it is the repository's "Latest" release.
7. Check that `mekanismgizmo-1.21.1-<version>.jar` is attached and that the release body names NeoForge 1.21.1.

## 1.20.1 / Forge

Use the same steps on the `1.20.1` branch with Java 17, tag `v<version>-forge-1.20.1`, and keep that release as
a non-latest release so the 1.21.1 / NeoForge release stays the repository's "Latest" release.

## Manual release

```bat
gh release create v<version>-neoforge-1.21.1 build/libs/mekanismgizmo-1.21.1-<version>.jar ^
  --title "Mekanism Gizmo <version> (NeoForge 1.21.1)" --notes-file CHANGELOG.md --latest
```
