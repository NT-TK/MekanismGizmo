# Mekanism Gizmo

A small **Mekanism** addon for **Minecraft 1.21.1 / NeoForge** that adds five machines built on Mekanism's own machine
framework, so the GUI, side configuration, upgrades, security, ownership and JEI integration all behave exactly like
Mekanism's own machines.

日本語の説明は [こちら](#日本語)（English の下にあります）。

- Minecraft **1.21.1**, NeoForge **21.1.194+**, Mekanism **10.7.19**
- Mod ID: `mekanismgizmo` / Java package `jp.example.mekanismgizmo`
- JEI integration is optional (client side only)

## Machines

| Machine | ID | Description |
|---|---|---|
| Potion Brewer | `mekanismgizmo:potion_brewer` | Brews any vanilla/NeoForge brewing recipe. Blaze powder is turned into Mekanism blaze powder gas (100 mB per powder) and 5 mB is used for each brew of three bottles. |
| Ice Maker | `mekanismgizmo:ice_maker` | Turns 1,000 mB of water into one ice block. Water can come from buckets, pipes or tanks. |
| Electric Cobblestone Generator | `mekanismgizmo:cobblestone_generator` | Produces cobblestone from energy only - no items or fluids needed. |
| Item Collector | `mekanismgizmo:item_collector` | Collects dropped items in a configurable n x n x n box (1-9 blocks per axis with a -5..+5 offset), 27 output slots and an in-world area preview. |
| Fluid Injector | `mekanismgizmo:fluid_injector` | Fills or empties buckets, Mekanism fluid tanks and vanilla bottles from its internal tank (Fill / Empty / Both modes). |

## Tiers

Every machine except the Item Collector comes in **five steps**: the plain machine you craft first, then Mekanism's
four tiers. The tier is stored with Mekanism's own tier attributes, so **Mekanism's tier installers upgrade the machine
in place** and keep the contents, energy, tanks, side configuration, owner and upgrades.

| Step | Registry name | Parallel operations | Tier installer |
|---|---|---|---|
| Untiered | `ice_maker` | 1 | `mekanism:basic_tier_installer` (from tier: none) |
| Basic | `basic_ice_maker` | 2 | `mekanism:advanced_tier_installer` |
| Advanced | `advanced_ice_maker` | 3 | `mekanism:elite_tier_installer` |
| Elite | `elite_ice_maker` | 4 | `mekanism:ultimate_tier_installer` |
| Ultimate | `ultimate_ice_maker` | 5 | - |

(The example above uses the Ice Maker; the Potion Brewer, Cobblestone Generator and Fluid Injector follow the same
pattern.) Higher tiers can also be crafted directly from the previous step.

### What increases per tier

- **Parallel operations** 1 -> 2 -> 3 -> 4 -> 5, so throughput scales 1x -> 5x
- **Energy use** (FE/t) and the **internal energy buffer** scale with the same ratio, so the **cost per item stays
  constant** (ice 2,000 FE / cobblestone 200 FE / potion 400 FE / container 25 FE)
- **Tank capacity** scales as well: ice maker water 4,000 -> 20,000 mB, fluid injector 16,000 -> 80,000 mB,
  brewer gas 1,000 -> 5,000 mB
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

The Item Collector is a single tier machine (10 FE/t, 12,000 FE buffer, 27 output slots, area of 1-9 blocks per axis
with a -5..+5 offset).

## Recipes

The base machines use Mekanism's machine recipe ingredients: a steel casing in the centre, `mekanism:alloys/basic`
(redstone) in the corners and `c:circuits/basic` (basic control circuit) on the edges.

| Machine | Pattern |
|---|---|
| Potion Brewer | `ACA` / `BXB` / `ACA` - A = redstone, B = brewing stand, C = basic control circuit, X = steel casing |
| Ice Maker | `ACA` / `IXI` / `ACA` - I = ice |
| Electric Cobblestone Generator | `ALA` / `CXC` / `AWA` - L = lava bucket, W = water bucket |
| Item Collector | `ACA` / `HXS` / `ACA` - H = hopper, S = logistical sorter |
| Fluid Injector | `ABA` / `CXF` / `ABA` - B = glass bottle, F = basic fluid tank |

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
- an **Ice Maker** page, an **Electric Cobblestone Generator** page and a **Fluid Injector** page
- every tier as a recipe catalyst, and clickable progress arrows in the machine screens
- the **5 mB of blaze powder gas** one brew of three bottles needs, listed as a chemical ingredient on every
  potion brewer page

Page widths are measured from the localised text, so nothing spills out in any language.

### AE2

AE2 is optional and never required (the mod only *suggests* it). With AE2's JEI integration the potion brewer page can
be dropped into a Pattern Encoding Terminal: the bottle, the ingredient and the potion are copied into the pattern.

Plain AE2 patterns can only hold items and fluids, so the blaze powder gas is only encoded when a chemical capable AE2
addon (for example Applied Mekanistics) is installed. Without one, keep the machine's gas tank filled with blaze powder
(1 powder = 100 mB = 20 brews) and let the pattern provide the bottle and the ingredient.

## Requirements

| | |
|---|---|
| Minecraft | 1.21.1 |
| NeoForge | 21.1.194 or newer |
| Mekanism | 10.7.19.x |
| JEI | optional (client side only) |
| AE2 | optional (JEI pattern encoding) |

## Installation

1. Install NeoForge 21.1.x for Minecraft 1.21.1
2. Install Mekanism 10.7.19
3. Put `mekanismgizmo-<version>.jar` into your `mods` folder

## Building

```bat
gradlew.bat build
```

The jar is written to `build/libs/mekanismgizmo-<version>.jar`.

All dependencies are resolved automatically: NeoForge/Minecraft via ModDevGradle, Mekanism from
**Modrinth's Maven** (`maven.modrinth:mekanism:10.7.19.85`) and JEI (compile only) from BlameJared. No third party jars
are stored in the repository. CurseMaven works too - add `maven { url = 'https://cursemaven.com' }` and
`implementation 'curse.maven:mekanism-268560:<file-id>'` if you prefer it.

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

## 追加される機械

| 機械 | ID | 説明 |
|---|---|---|
| ポーション醸造機 | `mekanismgizmo:potion_brewer` | バニラ／NeoForgeの醸造レシピでポーションを醸造します。ブレイズパウダーはMekanismのガス（1個=100 mB）になり、3本の醸造ごとに5 mB消費します。 |
| 製氷機 | `mekanismgizmo:ice_maker` | 水1,000 mBから氷ブロック1個を作ります。バケツ・パイプ・タンクから給水できます。 |
| 電動丸石製造機 | `mekanismgizmo:cobblestone_generator` | 電力だけで丸石を作り続けます（アイテム・液体の入力は不要）。 |
| アイテム収集機 | `mekanismgizmo:item_collector` | 設定した n×n×n の範囲（各軸1〜9、オフセット−5〜+5）に落ちているアイテムを回収します。出力27枠、範囲プレビュー付き。 |
| 流体注入機 | `mekanismgizmo:fluid_injector` | 内部タンクからバケツ・Mekanismの液体タンク・バニラの瓶を「注入／回収／両方」で出し入れします。 |

## ティア

アイテム収集機以外の4機械は **「ティアなし → 基本 → 発展 → 精鋭 → 究極」の5段階**です。
最初にクラフトするのはティアなしの機械で、以後は **Mekanismのティアインストーラーでそのまま昇格**できます
（中身・エネルギー・タンク・面設定・所有者・アップグレードは保持されます）。

| 段階 | レジストリ名 | 並列数 | 使用するインストーラー |
|---|---|---|---|
| ティアなし | `ice_maker` | 1 | `mekanism:basic_tier_installer`（from = ティアなし） |
| 基本 | `basic_ice_maker` | 2 | `mekanism:advanced_tier_installer` |
| 発展 | `advanced_ice_maker` | 3 | `mekanism:elite_tier_installer` |
| 精鋭 | `elite_ice_maker` | 4 | `mekanism:ultimate_tier_installer` |
| 究極 | `ultimate_ice_maker` | 5 | — |

（例は製氷機。ポーション醸造機・電動丸石製造機・流体注入機も同じ構成です。）
各段階は前の段階の機械からクラフトすることもできます。

### ティアごとに増えるもの

- **並列数** 1 → 2 → 3 → 4 → 5（処理量も 1 → 5倍）
- **FE/t と内部バッファ**も同じ比率で増えるため、**1個あたりのコストは全ティアで一定**
  （氷 2,000 FE / 丸石 200 FE / ポーション 400 FE / コンテナ 25 FE）
- **タンク容量**も並列数に比例（製氷機の水 4,000 → 20,000 mB、流体注入機 16,000 → 80,000 mB、
  醸造機のガス 1,000 → 5,000 mB）
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

アイテム収集機は単一ティアです（10 FE/t、内部バッファ 12,000 FE、出力27スロット、範囲は各軸1〜9・オフセット−5〜+5）。

## レシピ

基本ティアのレシピは Mekanism の機械と同じ素材構成（鋼ケーシング＋基礎制御回路＋`mekanism:alloys/basic`＝赤石）です。

| 機械 | レシピ |
|---|---|
| ポーション醸造機 | `ACA` / `BXB` / `ACA` — A=赤石, B=醸造台, C=基礎制御回路, X=鋼ケーシング |
| 製氷機 | `ACA` / `IXI` / `ACA` — I=氷 |
| 電動丸石製造機 | `ALA` / `CXC` / `AWA` — L=溶岩バケツ, W=水バケツ |
| アイテム収集機 | `ACA` / `HXS` / `ACA` — H=ホッパー, S=ロジスティカルソーター |
| 流体注入機 | `ABA` / `CXF` / `ABA` — B=ガラス瓶, F=基本液体タンク |

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
- **製氷機**・**電動丸石製造機**・**流体注入機**のページ
- 全ティアのレシピカタリストと、機械画面の進行矢印クリックからのレシピ表示
- 3本ぶんの醸造に必要な**ブレイズパウダーガス 5 mB** を、各ページの入力（ガス）として表示

ページ幅は各言語の文字幅から自動で決まるため、日本語でも文字はみ出しません。

### AE2連携

AE2は任意（依存は `optional` 指定のみ）です。AE2のJEI連携を使うと、ポーション醸造機のページをパターンエンコード
ターミナルへドラッグしてパターン化できます（瓶・材料・ポーションがパターンに入ります）。

AE2のパターンはアイテムと液体しか扱えないため、ブレイズパウダーガスは**化学物質を扱えるAE2アドオン**
（例: Applied Mekanistics）が入っている場合のみパターンに入ります。無い場合は機械のガスタンクにブレイズパウダーを
入れておけばよく（パウダー1個 = 100 mB = 醸造20回）、パターンには瓶と材料だけを入れます。

## 動作環境

| | |
|---|---|
| Minecraft | 1.21.1 |
| NeoForge | 21.1.194 以降 |
| Mekanism | 10.7.19.x |
| JEI | 任意（クライアント側のみ） |
| AE2 | 任意（JEIのパターン作成連携） |

## 導入方法

1. Minecraft 1.21.1 用の NeoForge 21.1.x を導入
2. Mekanism 10.7.19 を導入
3. `mekanismgizmo-<version>.jar` を `mods` フォルダに入れる

## ビルド

```bat
gradlew.bat build
```

生成Jar: `build/libs/mekanismgizmo-<version>.jar`

依存はすべて自動解決されます（NeoForge/MinecraftはModDevGradle、Mekanismは**ModrinthのMaven**
`maven.modrinth:mekanism:10.7.19.85`、JEIはコンパイル時のみBlameJaredから）。リポジトリに第三者のJarは置いていません。
CurseMavenを使いたい場合は `maven { url = 'https://cursemaven.com' }` を追加し、
`implementation 'curse.maven:mekanism-268560:<file-id>'` に差し替えてください。

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
