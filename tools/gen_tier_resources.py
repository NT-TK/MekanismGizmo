"""Generates the tier resources (blockstates, models, loot tables, recipes, lang, tags) for mekanismgizmo.

Run:  python tools/gen_tier_resources.py
"""
import io
import json
import os

TIERS = ['', 'basic_', 'advanced_', 'elite_', 'ultimate_']
MACHINES = ['potion_brewer', 'ice_maker', 'cobblestone_generator', 'basalt_generator', 'fluid_injector']
ASSETS = 'src/main/resources/assets/mekanismgizmo'
DATA = 'src/main/resources/data/mekanismgizmo'
COMPONENTS = ['mekanism:ejector', 'mekanism:owner', 'mekanism:redstone_control', 'mekanism:security',
              'mekanism:side_config', 'mekanism:upgrades', 'mekanism:energy', 'mekanism:items',
              'mekanism:fluids', 'mekanism:chemicals']


def write(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    io.open(path, 'w', encoding='utf-8', newline='\n').write(json.dumps(obj, indent=2) + '\n')


def names(machine):
    for tier in TIERS:
        yield tier, f'{tier}{machine}'


# --- blockstates / models / loot tables -----------------------------------------------------------
for machine in MACHINES:
    for _tier, name in names(machine):
        variants = {}
        for active in ('false', 'true'):
            for facing, y in (('north', None), ('south', 180), ('west', 270), ('east', 90)):
                variant = {'model': f'mekanismgizmo:block/{name}'}
                if y is not None:
                    variant['y'] = y
                variants[f'active={active},facing={facing}'] = variant
        write(f'{ASSETS}/blockstates/{name}.json', {'variants': variants})
        write(f'{ASSETS}/models/block/{name}.json', {
            'parent': 'minecraft:block/cube',
            'textures': {
                'particle': f'mekanismgizmo:block/{name}_front',
                'down': f'mekanismgizmo:block/{name}_bottom',
                'up': f'mekanismgizmo:block/{name}_top',
                'north': f'mekanismgizmo:block/{name}_front',
                'south': f'mekanismgizmo:block/{name}_back',
                'east': f'mekanismgizmo:block/{name}_side',
                'west': f'mekanismgizmo:block/{name}_side',
            },
        })
        write(f'{ASSETS}/models/item/{name}.json', {'parent': f'mekanismgizmo:block/{name}'})
        write(f'{DATA}/loot_table/blocks/{name}.json', {
            'type': 'minecraft:block',
            'pools': [{
                'bonus_rolls': 0.0,
                'entries': [{
                    'type': 'minecraft:item',
                    'name': f'mekanismgizmo:{name}',
                    'functions': [
                        {'function': 'minecraft:copy_name', 'source': 'block_entity'},
                        {'function': 'minecraft:copy_components', 'source': 'block_entity', 'include': COMPONENTS},
                    ],
                }],
                'name': 'main',
                'rolls': 1.0,
            }],
            'random_sequence': f'mekanismgizmo:blocks/{name}',
        })

# --- upgrade recipes: same shape as Mekanism's factory tier recipes --------------------------------
STEPS = {
    'basic_': {'alloy': 'mekanism:alloys/infused', 'circuit': 'c:circuits/advanced', 'ingot': 'c:ingots/osmium'},
    'advanced_': {'alloy': 'mekanism:alloys/reinforced', 'circuit': 'c:circuits/elite', 'ingot': 'c:ingots/gold'},
    'elite_': {'alloy': 'mekanism:alloys/atomic', 'circuit': 'c:circuits/ultimate', 'ingot': 'c:gems/diamond'},
    'ultimate_': {'alloy': 'mekanism:alloys/atomic', 'circuit': 'c:circuits/ultimate', 'ingot': 'c:gems/diamond'},
}
for machine in MACHINES:
    previous = machine
    for tier in TIERS[1:]:
        name = f'{tier}{machine}'
        step = STEPS[tier]
        write(f'{DATA}/recipe/{name}.json', {
            'type': 'minecraft:crafting_shaped',
            'category': 'misc',
            'key': {
                'A': {'tag': step['alloy']},
                'C': {'tag': step['circuit']},
                'I': {'tag': step['ingot']},
                'P': {'item': f'mekanismgizmo:{previous}'},
            },
            'pattern': ['ACA', 'IPI', 'ACA'],
            'result': {'count': 1, 'id': f'mekanismgizmo:{name}'},
        })
        previous = name

# --- pickaxe tag ----------------------------------------------------------------------------------
values = []
for machine in MACHINES:
    for _tier, name in names(machine):
        values.append(f'mekanismgizmo:{name}')
values.append('mekanismgizmo:item_collector')
write('src/main/resources/data/minecraft/tags/block/mineable/pickaxe.json', {'values': values})

# --- lang -----------------------------------------------------------------------------------------
EN_TIER = {'': '', 'basic_': 'Basic ', 'advanced_': 'Advanced ', 'elite_': 'Elite ', 'ultimate_': 'Ultimate '}
JA_TIER = {'': '', 'basic_': '\u57fa\u672c', 'advanced_': '\u767a\u5c55', 'elite_': '\u7cbe\u92ed',
           'ultimate_': '\u7a76\u6975'}
EN_NAMES = {
    'potion_brewer': 'Potion Brewer',
    'ice_maker': 'Ice Maker',
    'cobblestone_generator': 'Electric Cobblestone Generator',
    'basalt_generator': 'Electric Basalt Generator',
    'fluid_injector': 'Fluid Injector',
}
JA_NAMES = {
    'potion_brewer': '\u30dd\u30fc\u30b7\u30e7\u30f3\u91b8\u9020\u6a5f',
    'ice_maker': '\u88fd\u6c37\u6a5f',
    'cobblestone_generator': '\u96fb\u52d5\u4e38\u77f3\u88fd\u9020\u6a5f',
    'basalt_generator': '\u96fb\u52d5\u7384\u6b66\u5ca9\u88fd\u9020\u6a5f',
    'fluid_injector': '\u6d41\u4f53\u6ce8\u5165\u6a5f',
}
for lang, tier_names, machine_names in (('en_us', EN_TIER, EN_NAMES), ('ja_jp', JA_TIER, JA_NAMES)):
    path = f'{ASSETS}/lang/{lang}.json'
    data = json.loads(io.open(path, encoding='utf-8').read())
    for machine in MACHINES:
        for tier, name in names(machine):
            title = f'{tier_names[tier]}{machine_names[machine]}' if lang == 'en_us' \
                else f'{tier_names[tier]}{machine_names[machine]}'
            # The GUI title comes from TileEntityMekanism#getDisplayName -> "container.<namespace>.<block path>"
            data[f'container.mekanismgizmo.{name}'] = title
            data[f'block.mekanismgizmo.{name}'] = title
    io.open(path, 'w', encoding='utf-8', newline='\n').write(
        json.dumps(dict(sorted(data.items())), indent=2, ensure_ascii=False) + '\n')

print('tier resources generated')
