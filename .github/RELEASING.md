# Releasing

1. Set `mod_version` in `gradle.properties` to the version being released.
2. Add the same version to `CHANGELOG.md` and update the README if the requirements changed.
3. Run `gradlew.bat build` and check `build/libs/mekanismgizmo-<version>.jar`.
4. Commit the changes and push the release branch. Wait for the **Build** workflow to pass.
5. Create and push an annotated tag:

   ```bat
   git tag -a v<version> -m "Mekanism Gizmo <version>"
   git push origin v<version>
   ```

6. The **Release** workflow builds the jar and creates the GitHub release with the changelog.
   Check that `mekanismgizmo-<version>.jar` is attached and that the release is marked latest.

To create the release manually instead:

```bat
gh release create v<version> build/libs/mekanismgizmo-<version>.jar ^
  --title "Mekanism Gizmo v<version>" --notes-file CHANGELOG.md
```
