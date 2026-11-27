# Changelog

All notable changes to Mekanism Gizmo are documented here. The format follows
[Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and the project uses
[Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.3.1] - 2026-11-28

This release is the **Minecraft 1.21.1 / NeoForge** edition of Mekanism Gizmo 0.3.1. The 1.20.1 / Forge edition is
published separately from the `1.20.1` branch.

### Added

- **Improved Portable Teleporter** (`mekanismgizmo:improved_portable_teleporter`): a portable teleporter with a
  100,000,000 FE buffer, a 500,000 FE/t charge rate and a 10,000,000 FE cap per jump. It keeps Mekanism's frequency
  network, security, teleport packet and distance-based cost formula.
- **Improved Configuration Card** (`mekanismgizmo:improved_configurator`): copies side configuration, ejector,
  redstone control, security and frequencies like Mekanism's card, and additionally copies installed upgrades and
  the Digital Miner's item/tag/mod filters. Upgrades are installed from matching upgrade items in the player's
  inventory, all or nothing. Cards written by either item can be read by the other.
- Shared machine base (`GizmoMachineBlockEntity`) so configuration-card data is accepted across the addon's machine
  tiers and machine types.
- Animated Wind Generator Fusion front and an updated mod logo.

### Changed

- Improved Gas Generator keeps a partially started burn across fuel changes and world saves instead of losing the
  remainder, and now uses its direct 3D engine model with the flat cube as a fallback.
- Fluid Injector uses a compact per-tier GUI: the tank gauge stretches to the slot block and the player inventory
  only moves down when the extra container rows need the space.
- Improved Gas Generator, Wind Generator Fusion and Heat Generator Fusion now push generated energy through
  Mekanism's `CableUtils.emit`, so cables and adjacent energy acceptors receive power.
- The Item Collector now consumes a scan when a partial item transfer occurs, instead of moving items for free.
- Development self-check classes are excluded from release jars.

### Fixed

- Wind Generator Fusion shows 0 FE/t in dimensions tagged `mekanism:no_wind` instead of a misleading generation
  value, and its per-generator output is stored as a clamped `long` instead of an overflowing `int`.
- Heat Generator Fusion clears its heat capacitor to ambient when the last generator is removed, so reinserting
  generators cannot release stored heat as free power.
- Potion Brewer no longer converts blaze powder while disabled by redstone or security, compares the full ingredient
  stack for running brews, checks the gas needed for every brewable row before starting, and pauses instead of
  starting a cycle it cannot finish.
- Liquid Heater documentation now correctly describes its tank as forty operations' worth of sodium per tier.
- Fluid Injector and Potion Brewer JEI categories, recipe transfer and layout fixes.

## Release notes

- Minecraft 1.21.1, NeoForge 21.1.194+, Mekanism 10.7.19.x.
- Use the **1.20.1** branch and its `v<version>-forge-1.20.1` release for Minecraft 1.20.1 / Forge. Do not mix the two jars.
- The release tag is `v0.3.1-neoforge-1.21.1`; the release jar is `mekanismgizmo-0.3.1.jar`.
