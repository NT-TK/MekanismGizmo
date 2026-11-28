# Mekanism Gizmo

[![Releases](https://img.shields.io/badge/releases-NeoForge%201.21.1-EA7A1B.svg)](https://github.com/NT-TK/MekanismGizmo/releases?q=neoforge-1.21.1)
[![Build](https://github.com/NT-TK/MekanismGizmo/actions/workflows/build.yml/badge.svg?branch=1.21.1)](https://github.com/NT-TK/MekanismGizmo/actions/workflows/build.yml)
[![License: MIT](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-3C8527.svg)](https://www.minecraft.net/)
[![NeoForge](https://img.shields.io/badge/NeoForge-21.1.194%2B-EA7A1B.svg)](https://neoforged.net/)

A small **Mekanism** addon for **Minecraft 1.21.1 / NeoForge** that adds eleven machines built on Mekanism's own machine
framework, so the GUI, side configuration, upgrades, security, ownership and JEI integration all behave exactly like
Mekanism's own machines. It also adds four tiered pipettes, an improved portable teleporter and an improved configuration card.

日本語の説明は [こちら](#日本語)（English の下にあります）。

- Minecraft **1.21.1**, NeoForge **21.1.194+**, Mekanism **10.7.19**
- Mod ID: `mekanismgizmo` / Java package `jp.example.mekanismgizmo`
- JEI integration is optional (client side only)

## Download

The NeoForge 1.21.1 jar is attached to releases tagged `v<version>-neoforge-1.21.1`; see the
[NeoForge 1.21.1 releases](https://github.com/NT-TK/MekanismGizmo/releases?q=neoforge-1.21.1). Download
`mekanismgizmo-1.21.1-<version>.jar`, put it in your `mods` folder, and start Minecraft 1.21.1 with NeoForge and Mekanism
10.7.19 installed. The Minecraft 1.20.1 Forge edition is published separately from the `1.20.1` branch, so do not
install its jar on 1.21.1. See [Requirements](#requirements) and [Installation](#installation) for details.

Release notes are in [CHANGELOG.md](CHANGELOG.md). Please report bugs through
[GitHub Issues](https://github.com/NT-TK/MekanismGizmo/issues).

## Machines

| Machine | ID | Description |
|---|---|---|
| Potion Brewer | `mekanismgizmo:potion_brewer` | Brews any vanilla/NeoForge brewing recipe. Blaze powder is turned into Mekanism blaze powder gas (100 mB per powder) and 5 mB is used for each brew of three bottles. |
| Ice Maker | `mekanismgizmo:ice_maker` | Turns 1,000 mB of water into one ice block. Water can come from buckets, pipes or tanks. |
| Electric Cobblestone Generator | `mekanismgizmo:cobblestone_generator` | Produces cobblestone from energy only - no items or fluids needed. |
| Electric Basalt Generator | `mekanismgizmo:basalt_generator` | Produces basalt from energy only - no items or fluids needed. |
| Item Collector | `mekanismgizmo:item_collector` | Collects dropped items in a configurable n x n x n box (1-9 blocks per axis with a -5..+5 offset), 27 output slots and an in-world area preview. |
| Fluid Injector | `mekanismgizmo:fluid_injector` | Fills or empties buckets, Mekanism fluid tanks and vanilla bottles from its internal tank (Fill / Empty / Both modes). |
| Liquid Heater | `mekanismgizmo:liquid_heater` | Heats sodium into superheated sodium with energy only: one operation turns 500 mB of sodium into 500 mB of superheated sodium and every tier runs one more operation in parallel. |
| Machine Resetter | `mekanismgizmo:machine_resetter` | Strips every stored value from a machine item: owner, security, item/fluid/gas contents, stored energy, upgrades and side configuration. The result is exactly the item you get from crafting it. |
| Wind Generator Fusion | `mekanismgizmo:wind_fusion` | Fuses Mekanism wind generators into one block: 27 slots that each hold a stack, so up to 1,728 generators. Every generator inside produces exactly the power it would produce at that position, so 3 generators give 3 generators' worth of power and a full machine gives 1,728 times that. Compressed wind generators (Compact Mekanism Machines, x2 - x532480) count as the generators they replace. |
| Improved Gas Generator | `mekanismgizmo:improved_gas_generator` | A gas burning generator with 100 times the fuel tank (1,800,000 mB) and 100 times the internal buffer (20,000,000 FE) of Mekanism's own, burning with Mekanism's own formula with a ten times higher ceiling: a full ethene tank burns 64 mB/t instead of 6.4 mB/t. |
| Heat Generator Fusion | `mekanismgizmo:heat_fusion` | Fuses Mekanism heat generators into one block: 27 slots that each hold a stack, so up to 1,728 generators, and every one of them produces what a heat generator produces at that position from the surrounding lava and the lava in the tank. Compressed heat generators (Compact Mekanism Machines) count as the generators they replace. |

## Tiers

Every machine except the Item Collector, the Wind Generator Fusion, the Heat Generator Fusion, the Machine Resetter and the Improved Gas Generator comes in **five steps**: the plain machine you
craft first, then Mekanism's four tiers. The tier is stored with Mekanism's own tier attributes, so **Mekanism's tier installers upgrade the machine
in place** and keep the contents, energy, tanks, side configuration, owner and upgrades.

| Step | Registry name | Parallel operations | Tier installer |
|---|---|---|---|
| Untiered | `ice_maker` | 1 | `mekanism:basic_tier_installer` (from tier: none) |
| Basic | `basic_ice_maker` | 2 | `mekanism:advanced_tier_installer` |
| Advanced | `advanced_ice_maker` | 3 | `mekanism:elite_tier_installer` |
| Elite | `elite_ice_maker` | 4 | `mekanism:ultimate_tier_installer` |
| Ultimate | `ultimate_ice_maker` | 5 | - |

(The example above uses the Ice Maker; the Potion Brewer, Cobblestone Generator, Basalt Generator, Fluid Injector and
Liquid Heater follow the same pattern.) Higher tiers can also be crafted directly from the previous step.

### What increases per tier

- **Parallel operations** 1 -> 2 -> 3 -> 4 -> 5, so throughput scales 1x -> 5x
- **Energy use** (FE/t) and the **internal energy buffer** scale with the same ratio, so the **cost per item stays
  constant** (ice 2,000 FE / cobblestone 200 FE / basalt 200 FE / potion 400 FE / container 25 FE)
- **Tank capacity** scales as well: ice maker water 4,000 -> 20,000 mB, fluid injector 16,000 -> 80,000 mB,
  brewer gas 1,000 -> 5,000 mB, liquid heater 20,000 -> 100,000 mB per tank
- **Slots**: every tier adds the parallel slots (the brewer adds a whole brewing row of three bottles and three outputs)

#### Ice Maker

| Tier | Parallel | FE/t | Buffer | Water tank | Slots | Throughput | vs untiered |
|---|---|---|---|---|---|---|---|
| Untiered | 1 | 20 | 12,000 | 4,000 | 3 | 1 ice / 5 s (12/min) | x1 |
| Basic | 2 | 40 | 24,000 | 8,000 | 4 | 2 ice / 5 s (24/min) | x2 |
| Advanced | 3 | 60 | 36,000 | 12,000 | 5 | 3 ice / 5 s (36/min) | x3 |
| Elite | 4 | 80 | 48,000 | 16,000 | 6 | 4 ice / 5 s (48/min) | x4 |
| Ultimate | 5 | 100 | 60,000 | 20,000 | 7 | 5 ice / 5 s (60/min) | x5 |

#### Electric Cobblestone Generator

| Tier | Parallel | FE/t | Buffer | Slots | Throughput | vs untiered |
|---|---|---|---|---|---|---|
| Untiered | 1 | 10 | 12,000 | 2 | 1 / s (60/min) | x1 |
| Basic | 2 | 20 | 24,000 | 3 | 2 / s (120/min) | x2 |
| Advanced | 3 | 30 | 36,000 | 4 | 3 / s (180/min) | x3 |
| Elite | 4 | 40 | 48,000 | 5 | 4 / s (240/min) | x4 |
| Ultimate | 5 | 50 | 60,000 | 6 | 5 / s (300/min) | x5 |

#### Electric Basalt Generator

Same numbers as the cobblestone generator: 200 FE per basalt block (10 FE/t for 20 ticks), one basalt per parallel unit
per second.

| Tier | Parallel | FE/t | Buffer | Slots | Throughput | vs untiered |
|---|---|---|---|---|---|---|
| Untiered | 1 | 10 | 12,000 | 2 | 1 / s (60/min) | x1 |
| Basic | 2 | 20 | 24,000 | 3 | 2 / s (120/min) | x2 |
| Advanced | 3 | 30 | 36,000 | 4 | 3 / s (180/min) | x3 |
| Elite | 4 | 40 | 48,000 | 5 | 4 / s (240/min) | x4 |
| Ultimate | 5 | 50 | 60,000 | 6 | 5 / s (300/min) | x5 |

#### Potion Brewer

| Tier | Parallel | Bottles | FE/t | Buffer | Gas tank | Slots | Throughput | vs untiered |
|---|---|---|---|---|---|---|---|---|
| Untiered | 1 | 3 | 3 | 12,000 | 1,000 | 9 | 3 potions / 20 s (9/min) | x1 |
| Basic | 2 | 6 | 6 | 24,000 | 2,000 | 15 | 6 potions / 20 s (18/min) | x2 |
| Advanced | 3 | 9 | 9 | 36,000 | 3,000 | 21 | 9 potions / 20 s (27/min) | x3 |
| Elite | 4 | 12 | 12 | 48,000 | 4,000 | 27 | 12 potions / 20 s (36/min) | x4 |
| Ultimate | 5 | 15 | 15 | 60,000 | 5,000 | 33 | 15 potions / 20 s (45/min) | x5 |

One parallel unit is one brewing row of three bottles (like a brewing stand), so each tier adds **three bottle slots
and three potion output slots**.

#### Fluid Injector

| Tier | Parallel | FE/t | Buffer | Fluid tank | Slots | Throughput | vs untiered |
|---|---|---|---|---|---|---|---|
| Untiered | 1 | 25 | 12,000 | 16,000 | 3 | 1 container / tick (1,200/min) | x1 |
| Basic | 2 | 50 | 24,000 | 32,000 | 5 | 2 containers / tick (2,400/min) | x2 |
| Advanced | 3 | 75 | 36,000 | 48,000 | 7 | 3 containers / tick (3,600/min) | x3 |
| Elite | 4 | 100 | 48,000 | 64,000 | 9 | 4 containers / tick (4,800/min) | x4 |
| Ultimate | 5 | 125 | 60,000 | 80,000 | 11 | 5 containers / tick (6,000/min) | x5 |

Unlike the other tiered machines, the Fluid Injector has no SPEED or ENERGY upgrade support: its throughput is set by
the tier alone. A configuration card therefore skips recorded SPEED/ENERGY upgrades when you paste onto this machine.

The Item Collector is a single tier machine (10 FE/t, 12,000 FE buffer, 27 output slots, area of 1-9 blocks per axis
with a -5..+5 offset). The Wind Generator Fusion, the Heat Generator Fusion, the Machine Resetter and the Improved
Gas Generator are single tier machines as well.

#### Liquid Heater

| Tier | Parallel | FE/t | Buffer | Sodium tank | Superheated tank | Throughput | vs untiered |
|---|---|---|---|---|---|---|---|
| Untiered | 1 | 625 | 12,000 | 20,000 | 20,000 | 500 mB / s (25 mB/t) | x1 |
| Basic | 2 | 1,250 | 24,000 | 40,000 | 40,000 | 1,000 mB / s (50 mB/t) | x2 |
| Advanced | 3 | 1,875 | 36,000 | 60,000 | 60,000 | 1,500 mB / s (75 mB/t) | x3 |
| Elite | 4 | 2,500 | 48,000 | 80,000 | 80,000 | 2,000 mB / s (100 mB/t) | x4 |
| Ultimate | 5 | 3,125 | 60,000 | 100,000 | 100,000 | 2,500 mB / s (125 mB/t) | x5 |

The conversion is **25 FE per mB** (25,600 FE per bucket) and always 1:1, so a bucket of sodium becomes a bucket of
superheated sodium. Sodium is normally only obtainable from a fission reactor's coolant loop, so the machine is a way
to produce superheated sodium (for a Thermoelectric Boiler) without a reactor.

## Models

The other 34 machines use the original **flat 16 x 16 cube models** and their original face textures; the Wind
Generator Fusion front is the one face that keeps an animation. Two pieces are deliberately kept from the previous
version: that **animated Wind Generator Fusion front** and the **Improved Gas Generator's direct 3D engine model**.

- flat models: `assets/mekanismgizmo/models/block/<machine>.json`; each parents `minecraft:block/cube` directly and
  uses the `<machine>_front/top/side/back/bottom` 16 x 16 textures in `assets/mekanismgizmo/textures/block/`
- the front face is `north`; the standard blockstate `y` rotations turn it to the machine's facing
- the Improved Gas Generator keeps `improved_gas_generator_3d.json` and its 32 x 32 atlas
  `textures/block/model/improved_gas_generator.png`; the flat `improved_gas_generator.json` cube is kept as a fallback
- the Wind Generator Fusion keeps the 8 frame animated `wind_fusion_front.png` (16 x 128) and its `.mcmeta`; its block
  model itself is the normal flat cube
- tiers keep their original face textures; each tier has its own front/top/side/back/bottom set
- there is no composite tier-LED overlay and no dedicated 3D atlas or particle for the other machines
- tier resources are generated by `python tools/gen_tier_resources.py`

## Wind Generator Fusion

The Wind Generator Fusion machine is a single tier machine that works like a combiner for wind generators: put wind
generators into its 3 x 9 slot grid and each one produces exactly what a Mekanism wind generator would produce at the
same position.

- every slot holds a full stack of 64, so a full machine fuses 1,728 wind generators
- 1 generator = 1 generator's output, 2 = twice that, a full machine (1,728) = 1,728 times that
- the compressed wind generators of [Compact Mekanism Machines](https://github.com/nanaios/CompactMekaniasmMachines)
  are accepted too (`compressed_wind_generator_x2` up to `_x532480`, and the older `wind_generator_xN` names). Because
  those blocks produce exactly N times what a wind generator produces at the same position, each one counts as N
  generators, and the maximum on the screen follows the most compressed item inside
- the slots are internal storage, not an input: you fill them by hand and pipes can neither insert nor extract them
  (the back face still lets cables charge or drain items through the energy slot)
- the output uses Mekanism's own wind formula (`windGenerationMin` -> `windGenerationMax` between `windGenerationMinY`
  and `windGenerationMaxY`), so height, sky access and the dimension behave like a real wind generator
- the machine only counts what is actually inside, so a partly filled machine produces partially
- 184,025,088,000,000 FE internal buffer (Mekanism's wind generator storage of 200,000 FE, once per fusable
  generator, so 27 stacks of the largest compressed generator fit), energy is exported on every face that is
  configured as `Energy`
- an energy slot lets the buffer charge items, and the screen shows the fused count and the current FE/t
- the **rotor on the front face is animated**: the front texture is an 8 frame strip (`wind_fusion_front.png` plus
  its `.mcmeta`, 11.25 degrees per frame, so one blade position per loop), played on the front face of the flat cube
- the rest of the machine stays the original flat cube; there is no 3D nacelle or body

## Machine Resetter

Since Minecraft 1.21 Mekanism keeps everything a machine contains on its item: the owner, the security mode, the item,
fluid and gas contents, the stored energy, the installed upgrades, the side configuration and the redstone setting.
Breaking a machine keeps all of that, so a machine that was picked up in another world - or one you got from a friend
or a loot chest - arrives with someone else's settings and contents.

The Machine Resetter takes such an item, spends 200 FE on it and hands back the item with its defaults only:

- input slot (machine item) -> progress arrow -> output slot (clean machine item)
- the input must be a Mekanism machine item (a block item that carries machine data); casings, ordinary items and
  items with only fluid or gas tanks such as the pipettes are rejected
- the result is the item's own default instance, so nothing of the old machine survives - not even the owner
- 200 FE and 20 ticks at base speed per operation, with the usual SPEED and ENERGY upgrades; a whole stack is reset in
  one operation

## Improved Gas Generator

A gas burning generator with a hundred times the fuel tank and a hundred times the internal energy buffer of
Mekanism's gas generator, burning its fuel with **Mekanism's own formula** - only with a higher ceiling, so a full
tank tops out at 64 mB/t instead of Mekanism's 6.4 mB/t.

Mekanism's `TileEntityGasGenerator#getToUse()` in one place:

```
burnTicksPerTick = ceil(cap * fill)                 // cap = 256 in Mekanism, 2,560 here
toUse = min(fuel inside, burnTicksPerTick)          // limited by what is in the tank
toUse = min(toUse, bufferRoom / energyPerTick)      // limited by the energy buffer
energy = energyPerTick * toUse                      // one burn tick is one tick of generation
burnRate = toUse / burnTicks                        // the mB/t the screen shows
```

| | Mekanism gas generator | Improved Gas Generator |
|---|---|---|
| Fuel tank | 18,000 mB | 1,800,000 mB |
| Energy buffer | 200,000 FE | 20,000,000 FE |
| Burn tick ceiling | 256 per tick | 2,560 per tick |
| Burn rate at a full tank | hydrogen 256 mB/t, ethene 6.4 mB/t | hydrogen 2,560 mB/t, ethene 64 mB/t |
| Energy per mB | the fuel's own value | the same value (hydrogen 200 FE/mB, ethene 28,200 FE/mB) |

| Fuel | Full tank | Half a tank | Quarter tank |
|---|---|---|---|
| Hydrogen - 200 FE/mB | 2,560 mB/t - 512,000 FE/t | 1,280 mB/t - 256,000 FE/t | 640 mB/t - 128,000 FE/t |
| Ethene - 28,200 FE/mB | 64 mB/t - 1,804,800 FE/t | 32 mB/t - 902,400 FE/t | 16 mB/t - 451,200 FE/t |

- the fuel value per mB is read from Mekanism's own `chemicalFuel` data map (with the legacy chemical attribute as the
  fallback), so every gas Mekanism accepts works here with exactly its own value and its own burn time - hydrogen
  200 FE/mB, ethene 28,200 FE/mB, and anything another mod adds to that data map
- **the burn rate grows with the fill level**, because that is what Mekanism's own formula does
  (`ceil(256 * stored / capacity)`): a full tank burns at the full rate, a quarter tank at a quarter of it
- the screen shows a **burn rate in mB/t** and the produced FE/t next to Mekanism's FE per mB (for example
  `Burn Rate: 32.0 mB/t` and `902,400 FE/t - 28,200 FE/mB`), and the energy tab repeats the output
- **SPEED** upgrades multiply the burn ceiling with Mekanism's own speed formula (eight upgrades = x10, so a full
  ethene tank burns 640 mB/t = 18,048,000 FE/t); MUFFLING is supported as well
- gas is accepted on every face and through the gas container slot, the produced energy is exported on every face
  configured as `Energy`, and an energy slot lets the buffer charge items
- fuel is only burned while the buffer has room, so nothing is wasted
- the ceiling is the single constant `BURN_TICKS_PER_TICK` in `ImprovedGasGeneratorBlockEntity`: 2,560 gives the numbers
  above, Mekanism's own 256 gives exactly a gas generator's numbers (ethene 6.4 mB/t)
- recipe: `ACA` / `GXG` / `ACA` - A = atomic alloy, C = ultimate control circuit, G = gas burning generator,
  X = steel casing
- the block keeps its **direct 3D engine model**, the permitted exception to the no-copied-Mekanism-models rule:
  `models/block/improved_gas_generator_3d.json` with the 32 x 32 `textures/block/model/improved_gas_generator.png`.
  The pipes, cylinders, port plate and magenta recolour are retained from the earlier gas generator design
- the flat fallback is `improved_gas_generator.json` plus the original
  `improved_gas_generator_front/side/top/back/bottom.png` textures. The blockstate (all 8 variants, same `y` rotations)
  and the item model point at `mekanismgizmo:block/improved_gas_generator_3d`; to switch back, point both at
  `mekanismgizmo:block/improved_gas_generator`

## Heat Generator Fusion

The wind fusion machine's counterpart for Mekanism's **heat generator**. Each inserted heat generator produces exactly
what a heat generator produces at this position, so one inserted generator gives one generator's worth of power and a
full machine gives 1,728 times that (27 slots, a full stack each). Compressed heat generators of Compact Mekanism
Machines count as the number of generators they replace, so one stack of them can be worth far more than 64.

Heating is Mekanism's own, taken from `GeneratorsConfig`: every lava block next to the machine adds
`heatGenerationLava` (30), an ultra warm dimension adds `heatGenerationNether` (100), and lava in the internal tank is
burned at `heatGenerationFluidRate` (10 mB/t) for `heatGeneration` (200) - so the classic setup (lava under the machine
plus a lava tank) gives one generator's real output instead of an invented number.

| | Value |
|---|---|
| Slots | 27 (3 rows of 9), a full stack each |
| Generators in a full machine | 1,728 (more with compressed generators) |
| Lava tank | 1,536,000 mB (64 heat generator tanks) |
| Energy buffer | 160,000 FE per fused generator |
| Measured output, lava below + lava in the tank | 139 FE/t per generator |
| Measured temperature | ~739 K (a real heat generator sits around 751 K) |
| 64 generators | 8,896 FE/t |
| A full machine (1,728) | 240,192 FE/t |

- the temperature, the heat capacity and the losses are a single heat generator's, so the machine's temperature behaves
  like a real heat generator's and the power is simply multiplied by the number of fused generators
- lava is accepted on every face, the produced energy is exported on every face configured as `Energy`, and an energy
  slot on the back lets the buffer charge items
- the generator slots are **internal storage**: pipes cannot pull the generators back out, only the player can
- **MUFFLING** is supported; the machine has no energy cost of its own
- the screen shows how many generators are fused out of how many could be, the current output and the temperature
- recipe: `AHA` / `HPH` / `AHA` - A = basic alloy, H = heat generator, P = steel casing

## Pipettes (gauge droppers)

Mekanism's gauge dropper is the only way to empty or fill a machine's tank by hand, but it holds one bucket. The four
pipettes behave exactly like it - pick one up in a machine screen and click a tank gauge to fill or empty the machine,
sneak + right click to void the contents - and every tier holds more and moves more per click:

| Item | ID | Capacity (liquid / gas) |
|---|---|---|
| Basic Pipette | `mekanismgizmo:basic_pipette` | 2,000 mB |
| Advanced Pipette | `mekanismgizmo:advanced_pipette` | 4,000 mB |
| Elite Pipette | `mekanismgizmo:elite_pipette` | 8,000 mB |
| Ultimate Pipette | `mekanismgizmo:ultimate_pipette` | 16,000 mB |

A liquid tank and a gas tank are attached separately, so one pipette can carry both at the same time, and the
contents stay on the item when it is dropped, stored or previewed in JEI.

| Item | Pattern |
|---|---|
| Basic Pipette | ` O ` / `GDG` / `GGG` - O = osmium ingot, G = glass pane, D = Mekanism gauge dropper |
| Advanced / Elite / Ultimate | `ACA` / `IPI` / `ACA` - the same tier ingredients as the machines, P = previous pipette |

The item sprite is Mekanism's own gauge dropper texture: only the 15 pixels of the black rubber bulb are tinted with
the tier colour (the glass part is unchanged), so a pipette looks like a Mekanism item next to the rest of the mod.

## Improved Portable Teleporter

Mekanism's portable teleporter with **a hundred times the energy buffer**: 100,000,000 FE instead of 1,000,000 FE, so
it can hold enough for even the longest jumps and for a whole trip before it needs charging again.

It is the same item in every other respect, because it is a subclass of Mekanism's:

- the same screen, the same **frequency network** as the teleporter blocks, so it links to the same teleporters
- the same teleport packet and Mekanism's own distance based energy cost (including the cross dimension multiplier),
  but **a single teleport never costs more than 10,000,000 FE** - Mekanism's cost formula is capped, so even the longest
  jump is paid with at most that much (a jump that costs less than the cap costs exactly what Mekanism charges). The cap
  is part of this item's energy container only, so Mekanism's own portable teleporter keeps its uncapped cost
- the same owner / security handling, the same energy bar and tooltip, and the same charging from energy tablets,
  cubes and charging stations
- the charge rate is scaled by the same factor (500,000 FE/t instead of 5,000 FE/t), so the bigger buffer fills in the
  same time as Mekanism's rather than taking a hundred times as long

| | Mekanism Portable Teleporter | Improved Portable Teleporter |
|---|---|---|
| Buffer | 1,000,000 FE | 100,000,000 FE |
| Charge rate | 5,000 FE/t | 500,000 FE/t |
| Cost per jump | Mekanism's distance based formula | the same formula, **capped at 10,000,000 FE** |

| Item | ID | Pattern |
|---|---|---|
| Improved Portable Teleporter | `mekanismgizmo:improved_portable_teleporter` | `ACA` / `PTP` / `ACA` - A = atomic alloy, C = ultimate control circuit, P = portable teleporter, T = teleportation core |

The sprite is Mekanism's own portable teleporter texture with the energy window recoloured to the ultimate (magenta)
tint and two studs added to the handle.

The item is charged by the same things that charge Mekanism's gear (energy tablets, energy cubes, the charging unit of
the MekaSuit, and so on).

## Improved Configuration Card

Mekanism's configuration card copies a machine's side configuration, ejector, redstone control and frequencies. The
improved card copies all of that **and** the two things the normal one leaves behind: the **installed upgrades** and the
**digital miner's filter list**.

**Sneak + right click** a machine to record its configuration, **right click** another machine to apply it, and
**sneak + right click in the air** to clear the card. That is Mekanism's own card, click for click: the item extends
`ItemConfigurationCard`, and the record is stored in Mekanism's `CONFIGURATION_DATA` component with Mekanism's own
`data_name` / `data_type` keys, inside the very tag `getConfigurationData` produces. A card written by this item can
therefore be pasted by Mekanism's card and the other way around, both cards warn with Mekanism's own
"Unequal configuration data formats." message, and the card even switches to a lit sprite while it holds data, like
Mekanism's `configuration_card_encoded`.

| Copied | Notes |
|---|---|
| Side configuration | every transmission the machine has (item, energy, fluid, chemical, heat) |
| Ejector, redstone control, security | the same data Mekanism's own card carries |
| Frequencies | teleporter / quantum entangloporter / QIO frequencies, with their owner |
| Upgrades | installed counts per upgrade; the machine has to support them |
| Digital miner filters | item, tag and mod filters plus their replacement settings |

- **the configuration itself always pastes**, exactly like Mekanism's card: side configuration, ejector, redstone
  control, security and frequencies are injected into the machine you click with Mekanism's own
  `setConfigurationData`, so a card can never silently do nothing
- **the upgrades are paid with the upgrade items in your inventory, all or nothing among themselves.** If you carry
  enough of every recorded upgrade they are installed and the items are consumed; if even one is short, no upgrade is
  installed (the paste itself still went through) and the chat lists each upgrade with the amount needed and the amount
  you carry. Applying the same card a second time costs nothing, because the upgrades are already installed
- a recorded upgrade the machine does not support is named in the chat and skipped instead of aborting the paste
- the side configuration and frequencies are applied when the machines are the same block, or when both are Mekanism
  style machine blocks (which is what makes cross tier copying work); a machine only takes the transmissions it has
- the tooltip shows Mekanism's own "Data: <machine>" line, then the recorded upgrades and whether digital miner filters
  are included
- **Mekanism's own configuration card works on every machine of this addon as well** - and across tiers of the same
  machine, which the normal card refuses because it only allows the exact same block. Every machine here accepts a card
  recorded from any Mekanism-style machine block, and applies only the transmissions it actually has
- the reverse direction follows the target: this card is rejected by a Mekanism machine whose
  `isConfigurationDataCompatible` only accepts its own block. Use Mekanism's card for those machines

| Item | ID | Pattern |
|---|---|---|
| Improved Configuration Card | `mekanismgizmo:improved_configurator` | `ACA` / `CTC` / `ACA` - A = atomic alloy, C = ultimate control circuit, T = Mekanism configuration card |

The sprite is Mekanism's own configuration card with the tier colour: the status LED and a band along the top edge are
tinted to the ultimate magenta, and the recorded variant lights the card's screen in the same magenta, mirroring
Mekanism's `configuration_card_encoded`. Both are generated by `python tools/gen_item_textures.py`.

## Recipes

The base machines use Mekanism's machine recipe ingredients: a steel casing in the centre, `mekanism:alloys/basic`
(redstone) in the corners and `c:circuits/basic` (basic control circuit) on the edges.

| Machine | Pattern |
|---|---|
| Potion Brewer | `ACA` / `BXB` / `ACA` - A = redstone, B = brewing stand, C = basic control circuit, X = steel casing |
| Ice Maker | `ACA` / `IXI` / `ACA` - I = ice |
| Electric Cobblestone Generator | `ALA` / `CXC` / `AWA` - L = lava bucket, W = water bucket |
| Electric Basalt Generator | `ALA` / `CXC` / `ABA` - L = lava bucket, B = blue ice |
| Item Collector | `ACA` / `HXS` / `ACA` - H = hopper, S = logistical sorter |
| Fluid Injector | `ABA` / `CXF` / `ABA` - B = glass bottle, F = basic fluid tank |
| Wind Generator Fusion | `AWA` / `WPW` / `AWA` - A = redstone, W = wind generator, P = steel casing |
| Machine Resetter | `ACA` / `WXW` / `ACA` - A = redstone, C = basic control circuit, W = sponge, X = steel casing |
| Liquid Heater | `ACA` / `HXH` / `ACA` - A = infused alloy, C = advanced control circuit, H = magma block, X = steel casing |
| Improved Gas Generator | `ACA` / `GXG` / `ACA` - A = atomic alloy, C = ultimate control circuit, G = gas burning generator, X = steel casing |
| Heat Generator Fusion | `AHA` / `HPH` / `AHA` - A = basic alloy, H = heat generator, P = steel casing |

Tier upgrades follow Mekanism's factory tier recipes (`ACA` / `IPI` / `ACA`, P = previous machine):

| Upgrade | Alloy | Circuit | Item |
|---|---|---|---|
| Untiered -> Basic | `mekanism:alloys/infused` | `c:circuits/advanced` | osmium ingot |
| Basic -> Advanced | `mekanism:alloys/reinforced` | `c:circuits/elite` | gold ingot |
| Advanced -> Elite | `mekanism:alloys/atomic` | `c:circuits/ultimate` | diamond |
| Elite -> Ultimate | `mekanism:alloys/atomic` | `c:circuits/ultimate` | diamond |

Additional recipes:

- Oxidizing (`mekanism:oxidizing`) and Chemical Conversion (`mekanism:chemical_conversion`): 1 blaze powder ->
  100 mB of `mekanismgizmo:blaze_powder` gas

## JEI

JEI support is optional and client side only. When JEI is installed the mod adds

- a **Potion Brewer** page with one entry per bottle + ingredient, built from the game's own brewing data (all vanilla,
  NeoForge and data pack recipes) together with the recipe transfer ("+") button
- an **Ice Maker** page, an **Electric Cobblestone Generator** page, an **Electric Basalt Generator** page and a
  **Fluid Injector** page
- every tier as a recipe catalyst, and clickable progress arrows in the machine screens
- the **5 mB of blaze powder gas** one brew of three bottles needs, listed as a chemical ingredient on every
  potion brewer page

Page widths are measured from the localised text, so nothing spills out in any language.

### EMI

EMI is optional and client side only. EMI has its own native pages for the five machines (potion brewer, ice maker,
electric cobblestone generator, electric basalt generator and fluid injector), including the **5 mB of blaze powder
gas** on every potion brewer page, and every tier is a workstation of its category. When EMI is installed the JEI
pages are not registered, so the recipes are never listed twice.

## Requirements

| | |
|---|---|
| Minecraft | 1.21.1 |
| NeoForge | 21.1.194 or newer |
| Mekanism | 10.7.19.x |
| Mekanism Generators | 10.7.19.x (required by the Wind Generator Fusion) |
| Compact Mekanism Machines | optional (its compressed wind generators can go into the Wind Generator Fusion) |
| Mekanism: More Machine | optional (no integration needed: its machine items are machine items, so the Machine Resetter cleans them like any other) |
| JEI | optional (client side only) |
| EMI | optional (client side only) |

## Installation

1. Install NeoForge 21.1.x for Minecraft 1.21.1
2. Install Mekanism 10.7.19
3. Put the NeoForge 1.21.1 `mekanismgizmo-1.21.1-<version>.jar` from a `v<version>-neoforge-1.21.1` release into your `mods` folder

## Building

```bat
gradlew.bat build
```

The jar is written to `build/libs/mekanismgizmo-1.21.1-<version>.jar`.

All dependencies are resolved automatically: NeoForge/Minecraft via ModDevGradle, Mekanism from
**Modrinth's Maven** (`maven.modrinth:mekanism:10.7.19.85`) and JEI (compile only) from BlameJared. No third party jars
are stored in the repository. CurseMaven works too - add `maven { url = 'https://cursemaven.com' }` and
`implementation 'curse.maven:mekanism-268560:<file-id>'` if you prefer it.

Maintainers: the tag-driven GitHub release process is documented in [.github/RELEASING.md](.github/RELEASING.md).

## Credits

- Built on [Mekanism](https://github.com/mekanism/Mekanism) - MIT, (c) Aidancbrady, Thommy101, Thiakil, pupnewfster,
  dizzyd. Machine casing, port and LED art reused from Mekanism, and the machine framework (block/tile/GUI classes)
  comes from Mekanism.
- Author: **NT-TK**

## License

MIT - see [LICENSE](LICENSE). Copyright (c) 2025 NT-TK.

---

# 日本語

NeoForge 1.21.1 / Mekanism 10.7.19 対応の Mekanism アドオンです。mod ID・Javaパッケージ・リソースnamespaceは
`mekanismgizmo` です。Mekanism本体の機械フレームワーク（ブロック／ブロックエンティティ／GUI／面設定／
アップグレード／セキュリティ／JEI連携）をそのまま使っているので、操作感はMekanismの機械と同じです。
機械11種に加えて、4段階のティア付きスポイト、改良型ポータブルテレポーター、改良型設定カードも追加します。

NeoForge 1.21.1 版は `v<version>-neoforge-1.21.1` タグのリリースに添付しています。
[NeoForge 1.21.1 のリリース一覧](https://github.com/NT-TK/MekanismGizmo/releases?q=neoforge-1.21.1) からダウンロードしてください。
Minecraft 1.20.1 Forge 版は `1.20.1` ブランチから別のタグでリリースしているので、1.21.1 にはそのjarを入れないでください。
変更点は [CHANGELOG.md](CHANGELOG.md) にまとめています。

## 追加される機械

| 機械 | ID | 説明 |
|---|---|---|
| ポーション醸造機 | `mekanismgizmo:potion_brewer` | バニラ／NeoForgeの醸造レシピでポーションを醸造します。ブレイズパウダーはMekanismのガス（1個=100 mB）になり、3本の醸造ごとに5 mB消費します。 |
| 製氷機 | `mekanismgizmo:ice_maker` | 水1,000 mBから氷ブロック1個を作ります。バケツ・パイプ・タンクから給水できます。 |
| 電動丸石製造機 | `mekanismgizmo:cobblestone_generator` | 電力だけで丸石を作り続けます（アイテム・液体の入力は不要）。 |
| 電動玄武岩製造機 | `mekanismgizmo:basalt_generator` | 電力だけで玄武岩を作り続けます（アイテム・液体の入力は不要）。 |
| アイテム収集機 | `mekanismgizmo:item_collector` | 設定した n×n×n の範囲（各軸1〜9、オフセット−5〜+5）に落ちているアイテムを回収します。出力27枠、範囲プレビュー付き。 |
| 流体注入機 | `mekanismgizmo:fluid_injector` | 内部タンクからバケツ・Mekanismの液体タンク・バニラの瓶を「注入／回収／両方」で出し入れします。 |
| 液体加熱器 | `mekanismgizmo:liquid_heater` | 電力だけでナトリウムを加熱し、加熱されたナトリウム（SUPERHEATED_SODIUM）を作ります。1回の操作でナトリウム500 mBを加熱されたナトリウム500 mBに変換し、ティアが上がるごとに並列数が増えます。 |
| 機械リセット機 | `mekanismgizmo:machine_resetter` | 機械アイテムに保存されたデータ（所有者・セキュリティ・中身・液体・ガス・エネルギー・アップグレード・面設定）をすべて消去します。クラフトしたてのアイテムとまったく同じ状態になります。 |
| 風力発電融合機 | `mekanismgizmo:wind_fusion` | Mekanismの風力発電機を1ブロックに融合します。スロットは27個でそれぞれスタック（64個）まで入るので、最大1,728台。入れた発電機1台ごとに、その場所での風力発電機1台分の発電をするので、3台なら3台分、満杯なら1,728台分になります。Compact Mekanism Machines の圧縮風力発電機（x2〜x532480）は、それが代替する台数分として数えます。 |
| 改良型ガス発電機 | `mekanismgizmo:improved_gas_generator` | Mekanismのガス発電機のガスタンクと内部バッファを100倍（1,800,000 mB / 20,000,000 FE）にしたガス発電機。燃焼の計算式はMekanism本体のままで上限だけ10倍にしてあり、エチレン満タンで 64 mB/t（本家は6.4 mB/t）燃やします。 |
| 熱発電融合機 | `mekanismgizmo:heat_fusion` | Mekanismの熱発電機を1ブロックに融合します。スロットは27個でそれぞれスタック（64個）まで入るので、最大1,728台。入れた1台ごとに、周囲の溶岩とタンクの溶岩から、その場所での熱発電機1台分の発電をします。Compact Mekanism Machines の圧縮熱発電機は、それが代替する台数分として数えます。 |

## ティア

アイテム収集機・風力発電融合機・熱発電融合機・機械リセット機・改良型ガス発電機以外の6機械は **「ティアなし → 基本 → 発展 → 精鋭 → 究極」の5段階**です。
最初にクラフトするのはティアなしの機械で、以後は **Mekanismのティアインストーラーでそのまま昇格**できます
（中身・エネルギー・タンク・面設定・所有者・アップグレードは保持されます）。

| 段階 | レジストリ名 | 並列数 | 使用するインストーラー |
|---|---|---|---|
| ティアなし | `ice_maker` | 1 | `mekanism:basic_tier_installer`（from = ティアなし） |
| 基本 | `basic_ice_maker` | 2 | `mekanism:advanced_tier_installer` |
| 発展 | `advanced_ice_maker` | 3 | `mekanism:elite_tier_installer` |
| 精鋭 | `elite_ice_maker` | 4 | `mekanism:ultimate_tier_installer` |
| 究極 | `ultimate_ice_maker` | 5 | — |

（例は製氷機。ポーション醸造機・電動丸石製造機・電動玄武岩製造機・流体注入機・液体加熱器も同じ構成です。）
各段階は前の段階の機械からクラフトすることもできます。

### ティアごとに増えるもの

- **並列数** 1 → 2 → 3 → 4 → 5（処理量も 1 → 5倍）
- **FE/t と内部バッファ**も同じ比率で増えるため、**1個あたりのコストは全ティアで一定**
  （氷 2,000 FE / 丸石 200 FE / 玄武岩 200 FE / ポーション 400 FE / コンテナ 25 FE / ナトリウム 1 mB あたり 25 FE）
- **タンク容量**も並列数に比例（製氷機の水 4,000 → 20,000 mB、流体注入機 16,000 → 80,000 mB、
  醸造機のガス 1,000 → 5,000 mB、液体加熱器のタンク 20,000 → 100,000 mB）
- **スロット**は並列数ぶん増加（醸造機は「瓶3本＋出力3本」の行が1段ごとに増えます）

### ティアごとの数値

#### 製氷機

| ティア | 並列 | FE/t | 内部バッファ | 水タンク | スロット | 処理能力 | 対ティアなし |
|---|---|---|---|---|---|---|---|
| ティアなし | 1 | 20 | 12,000 | 4,000 | 3 | 1個 / 5秒（12/分） | ×1 |
| 基本 | 2 | 40 | 24,000 | 8,000 | 4 | 2個 / 5秒（24/分） | ×2 |
| 発展 | 3 | 60 | 36,000 | 12,000 | 5 | 3個 / 5秒（36/分） | ×3 |
| 精鋭 | 4 | 80 | 48,000 | 16,000 | 6 | 4個 / 5秒（48/分） | ×4 |
| 究極 | 5 | 100 | 60,000 | 20,000 | 7 | 5個 / 5秒（60/分） | ×5 |

#### 電動丸石製造機

| ティア | 並列 | FE/t | 内部バッファ | スロット | 処理能力 | 対ティアなし |
|---|---|---|---|---|---|---|
| ティアなし | 1 | 10 | 12,000 | 2 | 1個 / 秒（60/分） | ×1 |
| 基本 | 2 | 20 | 24,000 | 3 | 2個 / 秒（120/分） | ×2 |
| 発展 | 3 | 30 | 36,000 | 4 | 3個 / 秒（180/分） | ×3 |
| 精鋭 | 4 | 40 | 48,000 | 5 | 4個 / 秒（240/分） | ×4 |
| 究極 | 5 | 50 | 60,000 | 6 | 5個 / 秒（300/分） | ×5 |

#### 電動玄武岩製造機

数値は電動丸石製造機と同じです（玄武岩1個＝200 FE、10 FE/t で20 tick）。1並列あたり毎秒1個。

| ティア | 並列 | FE/t | 内部バッファ | スロット | 処理能力 | 対ティアなし |
|---|---|---|---|---|---|---|
| ティアなし | 1 | 10 | 12,000 | 2 | 1個 / 秒（60/分） | ×1 |
| 基本 | 2 | 20 | 24,000 | 3 | 2個 / 秒（120/分） | ×2 |
| 発展 | 3 | 30 | 36,000 | 4 | 3個 / 秒（180/分） | ×3 |
| 精鋭 | 4 | 40 | 48,000 | 5 | 4個 / 秒（240/分） | ×4 |
| 究極 | 5 | 50 | 60,000 | 6 | 5個 / 秒（300/分） | ×5 |

#### ポーション醸造機

| ティア | 並列 | 瓶 | FE/t | 内部バッファ | ガスタンク | スロット | 処理能力 | 対ティアなし |
|---|---|---|---|---|---|---|---|---|
| ティアなし | 1 | 3 | 3 | 12,000 | 1,000 | 9 | 3本 / 20秒（9/分） | ×1 |
| 基本 | 2 | 6 | 6 | 24,000 | 2,000 | 15 | 6本 / 20秒（18/分） | ×2 |
| 発展 | 3 | 9 | 9 | 36,000 | 3,000 | 21 | 9本 / 20秒（27/分） | ×3 |
| 精鋭 | 4 | 12 | 12 | 48,000 | 4,000 | 27 | 12本 / 20秒（36/分） | ×4 |
| 究極 | 5 | 15 | 15 | 60,000 | 5,000 | 33 | 15本 / 20秒（45/分） | ×5 |

1並列＝醸造台と同じ「3本ずつ」なので、1段ごとに瓶スロットと出力スロットが3つずつ増えます。

#### 流体注入機

| ティア | 並列 | FE/t | 内部バッファ | 液体タンク | スロット | 処理能力 | 対ティアなし |
|---|---|---|---|---|---|---|---|
| ティアなし | 1 | 25 | 12,000 | 16,000 | 3 | 1個 / tick（1,200/分） | ×1 |
| 基本 | 2 | 50 | 24,000 | 32,000 | 5 | 2個 / tick（2,400/分） | ×2 |
| 発展 | 3 | 75 | 36,000 | 48,000 | 7 | 3個 / tick（3,600/分） | ×3 |
| 精鋭 | 4 | 100 | 48,000 | 64,000 | 9 | 4個 / tick（4,800/分） | ×4 |
| 究極 | 5 | 125 | 60,000 | 80,000 | 11 | 5個 / tick（6,000/分） | ×5 |

ほかのティア機械と異なり、流体注入機はSPEED／ENERGYアップグレードに対応していません。処理量はティアだけで決まり、
設定カードを貼っても記録されたSPEED／ENERGYアップグレードはスキップされます。

アイテム収集機・風力発電融合機・熱発電融合機・機械リセット機・改良型ガス発電機は単一ティアです。アイテム収集機は 10 FE/t、内部バッファ 12,000 FE、出力27スロット、範囲は各軸1〜9・オフセット−5〜+5 です。

#### 液体加熱器

| ティア | 並列数 | FE/t | 内部バッファ | ナトリウムタンク | 加熱ナトリウムタンク | 処理量 | ティアなし比 |
|---|---|---|---|---|---|---|---|
| ティアなし | 1 | 625 | 12,000 | 20,000 | 20,000 | 500 mB / 秒（25 mB/t） | x1 |
| 基本 | 2 | 1,250 | 24,000 | 40,000 | 40,000 | 1,000 mB / 秒（50 mB/t） | x2 |
| 発展 | 3 | 1,875 | 36,000 | 60,000 | 60,000 | 1,500 mB / 秒（75 mB/t） | x3 |
| 精鋭 | 4 | 2,500 | 48,000 | 80,000 | 80,000 | 2,000 mB / 秒（100 mB/t） | x4 |
| 究極 | 5 | 3,125 | 60,000 | 100,000 | 100,000 | 2,500 mB / 秒（125 mB/t） | x5 |

変換は **1 mB あたり 25 FE**（バケツ1杯で 25,600 FE）で、ナトリウムと加熱されたナトリウムは常に 1:1 です。
ナトリウムは本来なら核分裂炉の冷却材ループでしか得られないので、この機械は原子炉なしで加熱されたナトリウム
（熱電ボイラー用）を作る手段になります。ナトリウムは前面以外の面（化学物質設定で `Input` の面）からパイプで入れ、
加熱されたナトリウムは `Output` の面から取り出します。

## 改良型ガス発電機

Mekanismのガス発電機のガスタンクと内部バッファを100倍にし、燃焼は **Mekanism本体と同じ計算式** で行うガス発電機です。
違うのは上限だけで、満タン時の燃焼速度は本家の 6.4 mB/t に対して **64 mB/t** になります。

Mekanism の `TileEntityGasGenerator#getToUse()` はこの1か所に集約されています:

```
burnTicksPerTick = ceil(cap * fill)                 // cap は本家 256、当MODは 2,560
toUse = min(タンク残量, burnTicksPerTick)            // タンクに入っている分まで
toUse = min(toUse, バッファの空き / energyPerTick)    // 内部バッファに入る分まで
energy = energyPerTick * toUse                      // burn tick 1 = 発電1tick分
burnRate = toUse / burnTicks                        // 画面に出る mB/t
```

| | Mekanismのガス発電機 | 改良型ガス発電機 |
|---|---|---|
| ガスタンク | 18,000 mB | 1,800,000 mB |
| 内部バッファ | 200,000 FE | 20,000,000 FE |
| burn tick 上限 | 毎tick 256 | 毎tick 2,560 |
| 満タン時の燃焼速度 | 水素 256 mB/t、エチレン 6.4 mB/t | 水素 2,560 mB/t、エチレン 64 mB/t |
| 1 mBあたりのエネルギー | 燃料本来の値 | 同じ値（水素 200 FE/mB、エチレン 28,200 FE/mB） |

| 燃料 | 満タン | 半分 | 1/4 |
|---|---|---|---|
| 水素 — 200 FE/mB | 2,560 mB/t・512,000 FE/t | 1,280 mB/t・256,000 FE/t | 640 mB/t・128,000 FE/t |
| エチレン — 28,200 FE/mB | 64 mB/t・1,804,800 FE/t | 32 mB/t・902,400 FE/t | 16 mB/t・451,200 FE/t |

- 燃料1 mBあたりのエネルギーは Mekanism 本体の `chemicalFuel` データマップ（旧ケミカル属性もフォールバック）から
  読むので、Mekanism が受け入れるガスはすべて本家と同じ値・同じ燃焼時間で使えます
- **燃焼速度はタンク残量に比例して上がります**。これは本家の式（`ceil(256 * stored / capacity)`）そのもので、
  満タンなら上限、1/4なら上限の1/4になります
- 画面には本家と同じく **燃焼速度（mB/t）** と **発電量（FE/t）・1 mBあたりの FE** を表示します
  （例: 「燃焼速度: 32.0 mB/t」「902,400 FE/t・28,200 FE/mB」）。エネルギー情報タブにも現在の発電量を表示します
- **SPEEDアップグレード**はMekanismの速度式で上限を掛けます（8個で×10、満タンのエチレンで 640 mB/t・18,048,000 FE/t）。
  MUFFLINGアップグレードにも対応しています
- ガスはどの面からでも（ガス容器スロットからも）入れられ、発電したエネルギーは `Energy` に設定したすべての面から出力します。
  エネルギースロットでバッファからアイテムを充電できます
- 内部バッファに空きがあるときだけ燃やすので、満タンで燃料を無駄にしません
- 上限は `ImprovedGasGeneratorBlockEntity.BURN_TICKS_PER_TICK` の1定数だけです（2,560 で上表、本家と同じ 256 にすると
  エチレン 6.4 mB/t の本家と完全に同じ数値になります）
- レシピ: `ACA` / `GXG` / `ACA` — A=原子合金, C=究極制御回路, G=ガス燃焼発電機, X=鋼ケーシング
- ブロックは**既存の直接3Dモデル**を残しています。これはMekanismの3Dモデル持ち込みが許可された例外です：
  `models/block/improved_gas_generator_3d.json` と32×32の `textures/block/model/improved_gas_generator.png`。
  配管・シリンダー・ポートプレートとマゼンタへの塗り替えは以前のガス発電機デザインをそのまま使っています
- **フラットなフォールバックも残しています**：`models/block/improved_gas_generator.json` と、変更していない
  `improved_gas_generator_front/side/top/back/bottom.png`。ブロックステート（8バリアント、`y` 回転はそのまま）と
  アイテムモデルは `mekanismgizmo:block/improved_gas_generator_3d` を指しています。戻すときは両方を
  `mekanismgizmo:block/improved_gas_generator` に変えるだけです

## モデル

改良型ガス発電機以外の34台は、元の**フラットな16×16キューブモデル**と元の面テクスチャを使います（風力発電融合機の
前面だけは残したアニメーション）。次の2つは前のバージョンから意図的に残しています：**風力発電融合機の
アニメーション前面**と、**改良型ガス発電機の直接3Dエンジンモデル**です。

- フラットモデル: `assets/mekanismgizmo/models/block/<machine>.json`。`minecraft:block/cube` を直接parentにし、
  `assets/mekanismgizmo/textures/block/` の `<machine>_front/top/side/back/bottom` 16×16テクスチャを使います
- 前面は `north` です。ブロックステートの標準の `y` 回転で機械の向きに合わせます
- 改良型ガス発電機は `improved_gas_generator_3d.json` と32×32のアトラス
  `textures/block/model/improved_gas_generator.png` を残しています。フラットな `improved_gas_generator.json`
  キューブはフォールバックとして残しています
- 風力発電融合機は8フレームの `wind_fusion_front.png`（16×128）と `.mcmeta` を残しています。ブロックモデル自体は
  通常のフラットキューブです
- ティアごとに元の前面・上面・側面・背面・底面テクスチャがあります
- 他の機械用の複合ティアLEDラッパー、専用3Dアトラス、専用パーティクルはありません
- ティア用リソースは `python tools/gen_tier_resources.py` で生成します

## 風力発電融合機

風力発電融合機は、風力発電機をまとめる「融合器」のような単一ティアの機械です。3×9のスロットに風力発電機を入れると、
入れた1台ごとに、Mekanismの風力発電機をその場所に置いたときと同じ発電量を出します。

- 1スロットに64個（スタック）まで入るので、満杯で1,728台
- [Compact Mekanism Machines](https://github.com/nanaios/CompactMekaniasmMachines) の圧縮風力発電機
  （`compressed_wind_generator_x2`〜`_x532480`、旧名の `wind_generator_xN` も可）も入ります。圧縮発電機は
  「その場所での風力発電機 N 台分」をそのまま発電するので、1個＝N台分として数えます。画面の最大値も、入っている
  中で最も圧縮率の高いアイテムに合わせて変わります
- 1台＝風力発電機1台分、2台＝2台分、満杯（1,728台）＝1,728台分
- スロットは「入力」ではなく機械の内部収納です。手で出し入れする専用で、パイプは出し入れできません
  （背面だけはケーブルからエネルギースロット経由でアイテムを充放電できます）
- 発電量は Mekanism 本体の計算式（`windGenerationMin`〜`windGenerationMax` を `windGenerationMinY`〜`windGenerationMaxY` で補間）をそのまま使うため、高さ・天空・ディメンションの扱いも本家と同じです
- 実際に入っている台数だけ発電するので、途中までしか入っていなければその分だけ発電します
- 内部バッファは 184,025,088,000,000 FE（風力発電機の蓄電 200,000 FE × 最大台数分。最大圧縮の発電機を27スタック入れられる量）。`Energy` に設定したすべての面から出力します
- エネルギースロットでバッファからアイテムを充電でき、画面には融合中の台数と現在の発電量（FE/t）を表示します
- 前面のローターは8フレームの `wind_fusion_front.png`（16×128）＋`.mcmeta`（frametime 2、1フレーム11.25度）で、
  フラットキューブの前面にそのまま再生されます。3Dナセルはありません

## 熱発電融合機

風力発電融合機の熱発電機版です。入れた熱発電機1台ごとに、Mekanismの熱発電機をその場所に置いたときと同じ発電をします。
満杯（3×9スロット × 64個 = 1,728台）で1,728台分の発電になります。Compact Mekanism Machines の圧縮熱発電機は
「その台数分」として数えるので、1スタックで64台を大きく超えることもあります。

加熱の仕様は Mekanism 本体の `GeneratorsConfig` の値をそのまま使います。隣接する溶岩1ブロックにつき
`heatGenerationLava`（30）、超高温ディメンションで `heatGenerationNether`（100）、内部タンクの溶岩は
`heatGenerationFluidRate`（10 mB/t）で燃やして `heatGeneration`（200）です。つまり「真下に溶岩＋タンクに溶岩」
という定番の置き方で、本家の熱発電機と同じ数値になります。

| | 値 |
|---|---|
| スロット | 27（9×3）、1スロット64個まで |
| 満杯時の台数 | 1,728台（圧縮版を入れればさらに増加） |
| 溶岩タンク | 1,536,000 mB（熱発電機のタンク24,000 mB × 64） |
| 内部バッファ | 融合1台につき 160,000 FE |
| 実測（真下に溶岩＋タンクに溶岩） | 1台あたり 139 FE/t |
| 実測温度 | 約739 K（本家の熱発電機は約751 K） |
| 64台 | 8,896 FE/t |
| 満杯（1,728台） | 240,192 FE/t |

- 熱容量・放熱・温度計算は熱発電機1台分をそのまま使い、発電量だけを台数倍するので、温度は本家の熱発電機と同じように振る舞います
- 溶岩はどの面からでも受け入れ、発電したエネルギーは `Energy` に設定したすべての面から出力します。背面のエネルギースロットでバッファからアイテムを充電できます
- 発電機スロットは**内部収納**です。パイプで抜き取ることはできず、手で出し入れする専用です
- **MUFFLING** に対応しています（機械自身はエネルギーを使いません）
- 画面には「融合中の台数 / 最大台数」、現在の発電量、温度を表示します
- レシピ: `AHA` / `HPH` / `AHA` — A=基本合金, H=熱発電機, P=鋼ケーシング

## 機械リセット機

Minecraft 1.21 以降の Mekanism は、機械の中身（所有者・セキュリティ・アイテム／液体／ガス・蓄電・アップグレード・面設定・レッドストーン設定）を**アイテム側に保存**します。そのため、機械を壊して持ち帰っても設定や中身がそのまま付いてきます。

機械リセット機はそうしたアイテムを入れて 200 FE を消費するだけで、**クラフトしたての状態**に戻して出力します。

- 入力スロット（機械アイテム）→ 進捗矢印 → 出力スロット（リセット済みアイテム）
- 入力できるのは Mekanism の機械アイテム（機械データを持つブロックアイテム）のみ。鋼ケーシングや普通のアイテム、スポイトのような液体・ガスタンクだけのアイテムは入りません
- 出力は「そのアイテムのデフォルト状態」そのものなので、所有者を含めて古いデータは一切残りません
- 1回の処理で 200 FE・基本20 tick（SPEED／ENERGYアップグレード対応）。スタックで入れた場合はまとめて1回で処理します

## スポイト（ゲージドロッパー）

Mekanismのゲージドロッパーは機械のタンクを手で出入れする唯一の手段ですが、容量は1バケツ分しかありません。
4種類のスポイトはそれとまったく同じ使い勝手（機械の画面でスポイトを持ち上げてタンクをクリック／スニーク＋右クリックで中身を廃棄）で、
ティアごとに容量と1回の移動量が増えます。

| アイテム | ID | 容量（液体／ガス） |
|---|---|---|
| 基本スポイト | `mekanismgizmo:basic_pipette` | 2,000 mB |
| 発展スポイト | `mekanismgizmo:advanced_pipette` | 4,000 mB |
| 精鋭スポイト | `mekanismgizmo:elite_pipette` | 8,000 mB |
| 究極スポイト | `mekanismgizmo:ultimate_pipette` | 16,000 mB |

液体タンクとガスタンクは別々に付くので、1本で両方を同時に持てます。中身はアイテムのコンポーネントなので、
落としても・チェストに入れても・JEIで見ても保持されます。

| アイテム | レシピ |
|---|---|
| 基本スポイト | ` O ` / `GDG` / `GGG` — O=オスミウムインゴット, G=ガラス板, D=Mekanismのゲージドロッパー |
| 発展／精鋭／究極スポイト | `ACA` / `IPI` / `ACA` — 機械と同じティア素材、P=前のティアのスポイト |

アイテムのテクスチャは Mekanism のゲージドロッパーそのままで、黒いゴム部分の15ピクセルだけをティア色に置き換えています
（ガラス部分は無変更）。Mekanism のアイテムと並べても違和感のない見た目です。

## 改良型ポータブルテレポーター

Mekanism のポータブルテレポーターの**蓄電量を100倍**にしたものです（1,000,000 FE → **100,000,000 FE**）。
長距離・別ディメンションへの移動でも、充電し直さずに何度もテレポートできます。

中身は Mekanism のポータブルテレポーターのサブクラスなので、それ以外はすべて本家と同じです。

- 画面・**周波数ネットワーク**（テレポーター設置ブロックと同じ周波数）・リンク方法は本家と共通
- テレポート処理と距離ベースのエネルギー消費（ディメンション跨ぎの倍率込み）もMekanism本体の計算をそのまま使いますが、**1回のテレポートは最大 10,000,000 FE** までに制限しています。本家の計算式でそれ以上のコストになる超長距離でも、実際に引かれるのは 10,000,000 FE までです（上限未満のコストは本家とまったく同じ）。この上限は改良型のエネルギーコンテナだけのものなので、本家のポータブルテレポーターは従来どおり上限なしです
- 所有者／セキュリティ、エネルギーゲージ、ツールチップ、エネルギータブレットや発電機・充電台からの充電も本家と同じ
- 充電速度も同じ倍率（5,000 FE/t → **500,000 FE/t**）にしたので、容量が100倍でも満充電までの時間は本家と同じです

| | Mekanism ポータブルテレポーター | 改良型ポータブルテレポーター |
|---|---|---|
| 蓄電量 | 1,000,000 FE | **100,000,000 FE** |
| 充電速度 | 5,000 FE/t | **500,000 FE/t** |
| 1回のテレポートコスト | 本家の距離ベース計算式 | 同じ計算式を**最大 10,000,000 FE に制限** |

| アイテム | ID | レシピ |
|---|---|---|
| 改良型ポータブルテレポーター | `mekanismgizmo:improved_portable_teleporter` | `ACA` / `PTP` / `ACA` — A=原子合金, C=究極制御回路, P=ポータブルテレポーター, T=テレポーテーションコア |

テクスチャは Mekanism のポータブルテレポーターのままで、エネルギー窓だけを究極（マゼンタ）色に置き換え、
持ち手に2つのスタッドを追加しています。

## 改良型設定カード

Mekanism のコンフィギュレーションカードは「面設定・イジェクター・レッドストーン制御・周波数」をコピーしますが、
**アップグレード**と**デジタルマイナーのフィルター**はコピーできません。改良型はその両方までコピーします。

**スニーク＋右クリック**で機械の設定を記録、**右クリック**で別の機械に適用、**スニーク＋素手同然に空中右クリック**で
カードを消去します。これは Mekanism 本家のカードとまったく同じ操作です（このアイテムは本家の `ItemConfigurationCard`
のサブクラスで、記録は本家の `CONFIGURATION_DATA` コンポーネントに本家と同じ `data_name` / `data_type` 付きで
`getConfigurationData` のタグをそのまま保存します）。そのため**このカードで記録した内容は本家カードでも貼れ、
本家カードで記録した内容もこのカードで貼れます**。形式が合わないときは本家と同じ「Unequal configuration data
formats.」を表示し、データを持つと本家の `configuration_card_encoded` と同じようにスプライトが点灯版に切り替わります。

| コピーする内容 | 備考 |
|---|---|
| 面設定 | その機械が持つ全トランスミッション（アイテム・エネルギー・液体・ガス・熱） |
| イジェクター・レッドストーン制御・セキュリティ | 本家カードと同じデータ |
| 周波数 | テレポーター／量子エンタングロポーター／QIO の周波数（所有者込み） |
| アップグレード | アップグレードごとの個数。機械が対応している必要あり |
| デジタルマイナーのフィルター | アイテム／タグ／Mod フィルターと置換設定 |

- **設定そのものは必ず貼られます**（本家カードと同じ挙動）。面設定・イジェクター・レッドストーン制御・セキュリティ・
  周波数は本家の `setConfigurationData` でそのまま書き込むので、「貼ったのに何も起きない」ことがありません
- **アップグレードはインベントリのアップグレードアイテムで支払い、アップグレード同士でオール・オア・ナッシング**です。
  記録された全アップグレード分のアイテムがあれば装着して消費し、1つでも足りなければ**アップグレードだけ**を見送ります
  （設定の貼り付けは成功しています）。その場合はチャットに「必要な数 / 所持数」をアップグレードごとに表示します。
  同じカードをもう一度適用しても、すでに取り付け済みなのでアイテムは消費されません
- 記録されたアップグレードに機械が対応していない場合は、そのアップグレード名をチャットに出して**スキップ**します（貼り付け全体は中止しません）
- 面設定と周波数は、同じブロック同士、または**Mekanism 式の機械ブロック同士**（これがティア間コピーを可能にしています）に適用し、
  機械は自分が持っているトランスミッションだけを反映します
- ツールチップは本家と同じ「データ: <機械名>」の行に続けて、記録されたアップグレードとデジタルマイナーの
  フィルター有無を表示します
- **Mekanism 本家のコンフィギュレーションカードも、このMODの全機械で使えるようにしました。**しかも同じ機械の別ティア間でもコピーできます
  （本家カードは「まったく同じブロック」しか許可しないため、基本ティア→究極ティアができませんでした）。このMODの機械は
  Mekanism製の機械ブロックから記録したカードを受け入れ、自分が持っているトランスミッションだけを反映します
- 逆向きは貼り付け先の判定に従います。本家機械の `isConfigurationDataCompatible` が自分のブロックだけを許可している場合、
  このMODのカードは拒否されます。そのような機械には本家カードを使ってください

| アイテム | ID | レシピ |
|---|---|---|
| 改良型設定カード | `mekanismgizmo:improved_configurator` | `ACA` / `CTC` / `ACA` — A=原子合金, C=究極制御回路, T=Mekanismのコンフィギュレーションカード |

テクスチャは Mekanism のコンフィギュレーションカードのままで、ステータスLEDと上端の帯を究極（マゼンタ）色にしています。
データを記録すると本家の `configuration_card_encoded` と同じように画面がマゼンタで点灯したスプライトに切り替わります。
どちらも `python tools/gen_item_textures.py` が生成します。

## レシピ

基本ティアのレシピは Mekanism の機械と同じ素材構成（鋼ケーシング＋基礎制御回路＋`mekanism:alloys/basic`＝赤石）です。

| 機械 | レシピ |
|---|---|
| ポーション醸造機 | `ACA` / `BXB` / `ACA` — A=赤石, B=醸造台, C=基礎制御回路, X=鋼ケーシング |
| 製氷機 | `ACA` / `IXI` / `ACA` — I=氷 |
| 電動丸石製造機 | `ALA` / `CXC` / `AWA` — L=溶岩バケツ, W=水バケツ |
| 電動玄武岩製造機 | `ALA` / `CXC` / `ABA` — L=溶岩バケツ, B=青氷 |
| アイテム収集機 | `ACA` / `HXS` / `ACA` — H=ホッパー, S=ロジスティカルソーター |
| 流体注入機 | `ABA` / `CXF` / `ABA` — B=ガラス瓶, F=基本液体タンク |
| 風力発電融合機 | `AWA` / `WPW` / `AWA` — A=赤石, W=風力発電機, P=鋼ケーシング |
| 機械リセット機 | `ACA` / `WXW` / `ACA` — A=赤石, C=基礎制御回路, W=スポンジ, X=鋼ケーシング |
| 液体加熱器 | `ACA` / `HXH` / `ACA` — A=強化合金, C=発展制御回路, H=マグマブロック, X=鋼ケーシング |
| 改良型ガス発電機 | `ACA` / `GXG` / `ACA` — A=原子合金, C=究極制御回路, G=ガス燃焼発電機, X=鋼ケーシング |
| 熱発電融合機 | `AHA` / `HPH` / `AHA` — A=基本合金, H=熱発電機, P=鋼ケーシング |

ティア昇格は Mekanism の工場ティアレシピと同じ `ACA` / `IPI` / `ACA`（P＝前のティアの機械）です。

| 昇格 | A（合金） | C（回路） | I（素材） |
|---|---|---|---|
| ティアなし → 基本 | `mekanism:alloys/infused` | `c:circuits/advanced` | オスミウムインゴット |
| 基本 → 発展 | `mekanism:alloys/reinforced` | `c:circuits/elite` | 金インゴット |
| 発展 → 精鋭 | `mekanism:alloys/atomic` | `c:circuits/ultimate` | ダイヤモンド |
| 精鋭 → 究極 | `mekanism:alloys/atomic` | `c:circuits/ultimate` | ダイヤモンド |

その他:

- 化学酸化機（`mekanism:oxidizing`）と化学変換機（`mekanism:chemical_conversion`）:
  ブレイズパウダー1個 → `mekanismgizmo:blaze_powder` 100 mB

## 実装（Mekanism標準機能の利用）

- ブロック: `mekanism.common.block.prefab.BlockTile` + `mekanism.common.content.blocktype.Machine`
- ブロックエンティティ: `mekanism.common.tile.base.TileEntityMekanism`（`TileEntityConfigurableMachine` 継承、
  `ISideConfiguration` / `ITierUpgradable` 実装）
- コンテナ: `mekanism.common.inventory.container.tile.MekanismTileContainer`
- 画面: `mekanism.client.gui.GuiConfigurableTile`
  （面設定タブ、アップグレード、レッドストーン、セキュリティ、エネルギー情報タブ、
  `GuiChemicalGauge` / `GuiFluidGauge` / `GuiProgress` / `GuiVerticalPowerBar`）
- スロット: `InputInventorySlot` / `OutputInventorySlot` / `EnergyInventorySlot` / `FluidInventorySlot`
- エネルギー: `MachineEnergyContainer`（FE/J 表示、SPEED/ENERGY アップグレード各最大8）
- 破壊時は Mekanism のルートテーブル（`minecraft:copy_name` + `mekanism:*` コンポーネントのコピー）で
  中身・設定を保持したまま回収できます
- コンパレーター出力: `MekanismUtils.redstoneLevelFromContents`（ガス量・水量・出力の有無）
- コンフィギュレーター／コンフィギュレーションカード／自動搬出（ejector）／`active` 状態も Mekanism 標準の処理
- 輸出入: `IItemHandler`、`IChemicalHandler`、Mekanism `IFluidHandler`、`IStrictEnergyHandler` / `IEnergyStorage`

## テクスチャとアイコン

- 機械の外装は Mekanism の `crusher/side` のケーシング、背面のエネルギー供給ポートは
  `models/ports` + `ports_led` の 8×8 プレートをそのまま使っています（`tools/gen_mek_textures.py` で生成）
- ティアは前面LEDの色で表現（ティアなし=灰、基本=ミント、発展=サーモン、精鋭=シアン、究極=マゼンタ）
- Modアイコンは `src/main/resources/logo.png`（128×128、ポーション醸造機のアイソメトリック、
  `tools/gen_logo.py` で生成）。クリエイティブタブのアイコンも同じブロックです

## JEI連携

JEIは任意（クライアント側のみ）です。導入すると以下が追加されます。

- **ポーション醸造機**の専用ページ（ゲーム本体の醸造データから生成するので、バニラ・NeoForge・データパックの
  醸造レシピがすべて表示されます）と「+」のレシピ転送
- **製氷機**・**電動丸石製造機**・**電動玄武岩製造機**・**流体注入機**のページ
- 全ティアのレシピカタリストと、機械画面の進行矢印クリックからのレシピ表示
- 3本ぶんの醸造に必要な**ブレイズパウダーガス 5 mB** を、各ページの入力（ガス）として表示

ページ幅は各言語の文字幅から自動で決まるため、日本語でも文字はみ出しません。

### EMI連携

EMIは任意（クライアント側のみ）です。EMI用に5機械の専用ページ（ポーション醸造機・製氷機・電動丸石製造機・
電動玄武岩製造機・流体注入機）を用意しており、醸造機のページには**ブレイズパウダーガス 5 mB** も入ります。全ティアが各カテゴリの
ワークステーションとして登録されます。EMIが入っている場合はJEI側のページを登録しないため、レシピが二重に
表示されることはありません。

## 動作環境

| | |
|---|---|
| Minecraft | 1.21.1 |
| NeoForge | 21.1.194 以降 |
| Mekanism | 10.7.19.x |
| Mekanism Generators | 10.7.19.x（風力発電融合機で必須） |
| Compact Mekanism Machines | 任意（圧縮風力発電機を風力発電融合機に入れられます） |
| Mekanism: More Machine | 任意（連携不要：追加される機械アイテムもMekanismの機械アイテムなので、機械リセット機でそのままリセットできます） |
| JEI | 任意（クライアント側のみ） |
| EMI | 任意（クライアント側のみ） |

## 導入方法

1. Minecraft 1.21.1 用の NeoForge 21.1.x を導入
2. Mekanism 10.7.19 を導入
3. `v<version>-neoforge-1.21.1` リリースの NeoForge 1.21.1 用 `mekanismgizmo-1.21.1-<version>.jar` を `mods` フォルダに入れる

## ビルド

```bat
gradlew.bat build
```

生成Jar: `build/libs/mekanismgizmo-1.21.1-<version>.jar`

依存はすべて自動解決されます（NeoForge/MinecraftはModDevGradle、Mekanismは**ModrinthのMaven**
`maven.modrinth:mekanism:10.7.19.85`、JEIはコンパイル時のみBlameJaredから）。リポジトリに第三者のJarは置いていません。
CurseMavenを使いたい場合は `maven { url = 'https://cursemaven.com' }` を追加し、
`implementation 'curse.maven:mekanism-268560:<file-id>'` に差し替えてください。

リリース手順（GitHub Actions・タグ作成）は [.github/RELEASING.md](.github/RELEASING.md) を参照してください。

クライアント起動:

```bat
gradlew.bat runClient
```

## 作者

NT-TK

## ライセンス

MIT（[LICENSE](LICENSE) を参照）。Copyright (c) 2025 NT-TK。

Mekanism から流用した機械の筐体・ポート・LEDのテクスチャ、および機械フレームワークのクラスは
Mekanism 本体の MIT ライセンス（Copyright (c) Aidancbrady, Thommy101, Thiakil, pupnewfster, dizzyd）に従います。
