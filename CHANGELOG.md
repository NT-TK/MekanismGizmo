# Changelog

All notable changes to Mekanism Gizmo are documented here. The format follows
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and the project uses
[Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.3.1] - 2026-11-28

This release is the **Minecraft 1.20.1 / Forge** edition of Mekanism Gizmo 0.3.1. The 1.21.1 / NeoForge edition is
published separately from the `1.21.1` branch.

### Added

- **Minecraft 1.20.1 / Forge port** of all eleven machines: Potion Brewer, Ice Maker, Electric Cobblestone Generator,
  Electric Basalt Generator, Item Collector, Fluid Injector, Liquid Heater, Machine Resetter, Wind Generator Fusion,
  Improved Gas Generator and Heat Generator Fusion.
- The four tiered Pipettes, the Improved Portable Teleporter and the Improved Configuration Card on 1.20.1.
- Sensible default side configurations for every machine. A freshly placed machine now accepts energy, items, fluids
  and gases on the expected faces without opening the side configuration tab first. Existing player configurations
  are never overwritten.
- `pack.mcmeta` with resource pack format 15, so 1.20.1 no longer reports missing pack metadata.

### Changed

- Ported the machine, GUI, JEI and recipe code from the 1.21.1 codebase to the 1.20.1 Mekanism 10.4 API:
  `FloatingLong`, typed gas stacks and tanks, `mekData` item NBT, Forge capabilities, Forge tags and the 1.20.1
  `BrewingRecipeRegistry`.
- Recipes use the 1.20.1 plural data directories (`recipes/`, `loot_tables/`, `tags/blocks/`), `forge:` common tags,
  and the 1.20.1 `oxidizing` recipe format.
- Loot tables copy the machine's `mekData` NBT (contents, energy, upgrades, side configuration and redstone setting)
  when the block is broken, so machines keep their contents exactly like Mekanism's own machines.
- The 1.20.1 build uses ForgeGradle 6, Java 17, Forge 47.4.0 and Mekanism 10.4.16.80. JEI 15.20 is a compile-only
  dependency.
- GitHub Actions now build the 1.20.1 branch with Java 17 and upload a branch-specific artifact.

### Fixed

- Forge 47.4.0 starts the mod correctly: `mods.toml` declares the FML language version `[1,)`, dependencies use
  `mandatory=true/false`, and the mod class has the no-argument constructor Forge expects.
- Removed the remaining 1.21-only startup and model errors, including the stale 1.21 test world that referenced
  `minecraft:trial_chambers`.
- Preserved the 0.3.1 gameplay fixes: generators push energy through `CableUtils.emit`, the Improved Gas Generator keeps
  a partially started burn, the Item Collector cannot move items for free, and the Potion Brewer pauses instead of
  starting a brew it cannot finish.
- The four Pipettes replace Mekanism 1.20.1's fixed sixteen-bucket contents handler with a tiered one, so their liquid
  and gas tanks really hold 2,000 / 4,000 / 8,000 / 16,000 mB and move 256 / 500 / 1,000 / 2,000 mB per operation.
- The Improved Portable Teleporter keeps its **10,000,000 FE per-jump ceiling** on 1.20.1. Its energy container reports
  the full simulated cost when the ceiling is affordable but only removes the ceiling, so Mekanism's own teleport packet
  and screen accept the jump while the item still pays at most 10,000,000 FE.

### Removed

- EMI integration is not included in the 1.20.1 edition. JEI is the supported recipe viewer on this branch.

## Release notes

- Minecraft 1.20.1, Forge 47.4.0+, Mekanism 10.4.16.x (Mekanism Generators 10.4.16.x is required by the Wind Generator
  Fusion).
- The release tag is `v0.3.1-forge-1.20.1`; the release jar is `mekanismgizmo-1.20.1-0.3.1.jar`.
- Use the **1.21.1** branch and its release for Minecraft 1.21.1 / NeoForge. Do not mix the two jars.