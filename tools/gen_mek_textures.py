"""Rebuilds the mekanismgizmo machine textures on top of Mekanism's own machine casing + port art.

Mekanism (MIT licensed, authors listed in META-INF/neoforge.mods.toml) ships the machine casing frame in
`mekanism:block/crusher/side.png` and the port plate / port LED in `mekanism:block/models/ports.png` and
`ports_led.png`.  Using those keeps the palette, the bevel widths and the 8x8 energy port pixel-exact with
the rest of Mekanism instead of approximating them.

Each generated face is: Mekanism casing frame (kept pixel for pixel) + our recessed panel + a per-machine
front icon / side vents / top fan / energy port.

Run:  python tools/gen_mek_textures.py
"""
import io
import random
import zipfile
from pathlib import Path

from PIL import Image

OUT = Path('src/main/resources/assets/mekanismgizmo/textures/block')
JAR = Path('libs/Mekanism-1.21.1-10.7.19.85.jar')

MACHINES = {
    'potion_brewer': 'potion',
    'ice_maker': 'ice',
    'cobblestone_generator': 'cobble',
    'basalt_generator': 'basalt',
    'fluid_injector': 'injector',
    'liquid_heater': 'heater',
}

# Tier LED colours, taken from mekanism:block/factory/led.png (the four strips of that texture)
TIERS = {
    '': ((0x8A, 0x8A, 0x8A), (0x6E, 0x6E, 0x6E)),           # untiered (plain steel, no tier colour)
    'basic_': ((0x27, 0xFF, 0xA1), (0x2A, 0xE0, 0x97)),     # basic  (mint)
    'advanced_': ((0xDD, 0x75, 0x5D), (0xCE, 0x50, 0x45)),  # advanced (salmon)
    'elite_': ((0x71, 0xE3, 0xE3), (0x2C, 0xDD, 0xDE)),     # elite  (cyan)
    'ultimate_': ((0xC8, 0x5A, 0xCB), (0xB9, 0x4E, 0xC4)),  # ultimate (magenta)
}

# Accents: (bright, dark, highlight)
ACCENT = {
    'potion': ((198, 100, 224), (142, 58, 168), (235, 182, 247)),
    'ice': ((143, 216, 245), (62, 143, 191), (226, 247, 255)),
    'cobble': ((232, 120, 36), (163, 78, 16), (255, 192, 122)),
    'basalt': ((104, 106, 116), (58, 59, 68), (176, 178, 190)),
    'lava': ((255, 138, 42), (198, 78, 12), (255, 214, 156)),
    'collector': ((191, 170, 118), (132, 116, 66), (228, 214, 174)),
    'injector': ((90, 190, 235), (46, 134, 184), (214, 242, 255)),
    'wind': ((198, 220, 233), (118, 146, 164), (241, 251, 255)),
    'resetter': ((230, 205, 96), (176, 148, 48), (252, 238, 168)),
    'heater': ((255, 132, 62), (188, 68, 26), (255, 208, 156)),
    'gasgen': ((176, 236, 96), (104, 166, 40), (232, 255, 186)),
}

# Our own micro palette for the panel and the icons (kept in Mekanism's grey range)
C = {
    'panel': (42, 42, 42),
    'panelD': (36, 36, 36),
    'panelL': (49, 49, 49),
    'shadow': (27, 27, 27),
    'hi': (62, 62, 62),
    'steel': (140, 140, 140),
    'glass': (58, 58, 58),
    'beam': (85, 85, 85),
}


def from_jar(path):
    with zipfile.ZipFile(JAR) as jar:
        return Image.open(io.BytesIO(jar.read(path))).convert('RGBA')


def put(img, x, y, colour):
    img.putpixel((x, y), colour + (255,))

# --- icons: drawn inside 4..11 / 4..11 (8x8) -------------------------------------------------------
def icon_potion(img):
    bright, dark, hi = ACCENT['potion']
    for x in range(5, 11):
        put(img, x, 4, C['steel'])
    for y in range(5, 7):
        put(img, 7, y, C['steel'])
        put(img, 8, y, C['glass'])
    for y in range(7, 12):
        for x in range(4, 12):
            if y == 11:
                colour = C['shadow']
            elif y == 7 or x == 4 or x == 11:
                colour = C['steel']
            elif (x, y) in ((5, 8), (5, 9), (6, 8)):
                colour = hi
            elif x >= 9:
                colour = dark
            else:
                colour = bright
            put(img, x, y, colour)
    put(img, 7, 10, hi)
    put(img, 8, 10, dark)


def icon_ice(img):
    bright, dark, hi = ACCENT['ice']
    cx, cy = 7, 7
    for i in range(4, 12):
        put(img, i, cy, bright)
        put(img, cx, i, bright)
    for d in range(-3, 4):
        if d:
            put(img, cx + d, cy + d, dark if abs(d) > 2 else bright)
            put(img, cx + d, cy - d, dark if abs(d) > 2 else bright)
    for x, y in ((4, 5), (4, 9), (11, 5), (11, 9), (5, 4), (9, 4), (5, 10), (9, 10)):
        put(img, x, y, bright)
    for x, y in ((cx, cy), (cx + 1, cy), (cx, cy + 1), (cx + 1, cy + 1)):
        put(img, x, y, hi)
    for x, y in ((cx, cy - 2), (cx, cy + 2), (cx - 2, cy), (cx + 2, cy)):
        put(img, x, y, dark)


def icon_cobble(img):
    bright, dark, hi = ACCENT['cobble']
    blocks = [(4, 4, 3, 3), (8, 4, 4, 3), (4, 8, 4, 3), (9, 8, 3, 3)]
    for bx, by, bw, bh in blocks:
        for y in range(by, by + bh):
            for x in range(bx, bx + bw):
                if x == bx or y == by:
                    colour = C['beam'] if (x, y) != (bx, by) else C['steel']
                elif x == bx + bw - 1 or y == by + bh - 1:
                    colour = C['shadow']
                elif (x + 2 * y) % 5 == 0:
                    colour = hi
                elif (x * y) % 7 == 0:
                    colour = dark
                else:
                    colour = bright
                put(img, x, y, colour)


def icon_basalt(img):
    """Three basalt columns of different heights with a lava seam glowing between them."""
    bright, dark, hi = ACCENT['basalt']
    lava = ACCENT['lava'][0]
    for x0, top in ((4, 6), (7, 4), (10, 7)):
        for y in range(top, 12):
            put(img, x0, y, hi if y == top else bright)
            put(img, x0 + 1, y, hi if y == top else dark)
        put(img, x0, 11, C['shadow'])
        put(img, x0 + 1, 11, C['shadow'])
    # lava glowing up out of the gaps between the columns
    for y in range(9, 12):
        put(img, 6, y, lava)
    put(img, 6, 9, ACCENT['lava'][2])
    put(img, 9, 10, ACCENT['lava'][1])
    put(img, 9, 11, lava)


def icon_collector(img):
    bright, dark, hi = ACCENT['collector']
    for x in range(4, 12):
        put(img, x, 4, bright)
        put(img, x, 5, dark if x in (4, 11) else bright)
    for i in range(1, 5):
        put(img, 4 + i, 4 + i, bright)
        put(img, 11 - i, 4 + i, bright)
    for y in range(8, 11):
        put(img, 7, y, dark)
        put(img, 8, y, dark)
    for x in range(6, 10):
        put(img, x, 11, bright)
        put(img, x, 10, hi if x in (6, 9) else dark)


def icon_wind(img):
    """A four bladed rotor around a steel hub, seen from the front like the blades of a wind generator."""
    bright, dark, hi = ACCENT['wind']
    arms = (
        ((7, 4), (7, 5), (7, 6)),      # up
        ((9, 7), (10, 7), (11, 7)),    # right
        ((8, 11), (8, 10), (8, 9)),    # down
        ((6, 8), (5, 8), (4, 8)),      # left
    )
    for cells in arms:
        for i, (x, y) in enumerate(cells):
            put(img, x, y, hi if i == 0 else (bright if i == 1 else dark))
    # hub
    for x, y in ((7, 7), (8, 7), (7, 8), (8, 8)):
        put(img, x, y, C['steel'] if (x + y) % 2 else C['beam'])


def icon_sponge(img):
    """A sponge: the machine wipes everything a machine item carries."""
    bright, dark, hi = ACCENT['resetter']
    for y in range(4, 12):
        for x in range(4, 12):
            if x in (4, 11) and y in (4, 11):
                continue
            if x in (4, 11) or y in (4, 11):
                colour = hi if x == 4 or y == 4 else dark
            else:
                colour = bright
            put(img, x, y, colour)
    for x, y in ((5, 5), (8, 5), (6, 7), (9, 7), (5, 9), (8, 9), (10, 6), (7, 10)):
        put(img, x, y, dark)


def icon_droplet(img):
    bright, dark, hi = ACCENT['injector']
    rows = {4: (7, 8), 5: (6, 9), 6: (6, 9), 7: (5, 10), 8: (5, 10), 9: (4, 11), 10: (5, 10), 11: (6, 9)}
    for y, (a, b) in rows.items():
        for x in range(a, b + 1):
            colour = bright
            if x == a or y == 11:
                colour = dark
            elif y <= 6 and x == b:
                colour = dark
            if (x, y) == (6, 8):
                colour = hi
            put(img, x, y, colour)
    for x in (6, 8):
        put(img, x, 12, C['shadow'])


def icon_heater(img):
    """Three glowing heating bars with a coil between them: sodium going in gets hot."""
    bright, dark, hi = ACCENT['heater']
    for y in (4, 6, 8, 10):
        for x in range(4, 12):
            put(img, x, y, bright if 4 < x < 11 else dark)
    # the coil that connects the bars, zig-zagging from one side to the other
    for x, y in ((10, 5), (5, 7), (10, 9), (5, 11)):
        put(img, x, y, dark)
    for x, y in ((7, 5), (8, 5), (6, 7), (9, 7), (8, 9), (7, 9)):
        put(img, x, y, hi)


def icon_gasgen(img):
    """A flame: the gas that goes in is burned for power."""
    bright, dark, hi = ACCENT['gasgen']
    rows = {4: (7, 8), 5: (6, 9), 6: (6, 9), 7: (5, 10), 8: (5, 10), 9: (5, 10), 10: (6, 9), 11: (7, 8)}
    for y, (a, b) in rows.items():
        for x in range(a, b + 1):
            colour = dark if (x == a or x == b) and y > 5 else bright
            put(img, x, y, colour)
    for x, y in ((7, 8), (8, 8), (7, 9), (8, 9), (7, 10), (8, 10)):
        put(img, x, y, hi)


ICONS = {
    'potion': icon_potion,
    'ice': icon_ice,
    'cobble': icon_cobble,
    'basalt': icon_basalt,
    'collector': icon_collector,
    'injector': icon_droplet,
    'wind': icon_wind,
    'resetter': icon_sponge,
    'heater': icon_heater,
    'gasgen': icon_gasgen,
}


# --- faces -----------------------------------------------------------------------------------------
def casing(seed):
    """Mekanism machine casing (crusher side) with our own recessed panel in the middle."""
    img = from_jar('assets/mekanism/textures/block/crusher/side.png').copy()
    rnd = random.Random(seed)
    for y in range(3, 13):
        for x in range(3, 13):
            roll = rnd.random()
            shade = C['panel']
            if roll < 0.16:
                shade = C['panelD']
            elif roll < 0.30:
                shade = C['panelL']
            put(img, x, y, shade)
    for i in range(3, 13):
        put(img, i, 3, C['shadow'])
        put(img, 3, i, C['shadow'])
        put(img, i, 12, C['hi'])
        put(img, 12, i, C['hi'])
    return img


def led_bar(img, x, y, w, tier):
    bright, dark = TIERS[tier]
    for i in range(w):
        put(img, x + i, y, bright if i % 2 == 0 else dark)


def front_face(name, accent, tier):
    img = casing(name + '-front')
    ICONS[accent](img)
    # Tier LED strip, like the front panel LED of Mekanism's factories
    led_bar(img, 4, 13, 8, tier)
    return img


def side_face(name):
    img = casing(name + '-side')
    # ventilation grooves, like the lower band of Mekanism machine sides
    for y in (5, 7, 9, 11):
        for x in range(4, 12):
            put(img, x, y, C['shadow'])
            put(img, x, y + (0 if y == 11 else 1), C['hi'] if y < 11 else C['beam'])
    return img


def top_face(name, accent):
    img = casing(name + '-top')
    bright, dark, _ = ACCENT[accent]
    import math
    for y in range(3, 13):
        for x in range(3, 13):
            rx, ry = x - 7.5, y - 7.5
            r = math.hypot(rx, ry)
            if r > 4.6:
                continue
            put(img, x, y, C['beam'] if r > 4.0 else C['shadow'])
    for i in range(4, 12):
        put(img, i, 7, C['beam'])
        put(img, i, 8, C['panelD'])
        put(img, 7, i, C['beam'])
        put(img, 8, i, C['panelD'])
    for x, y in ((7, 7), (8, 7), (7, 8), (8, 8)):
        put(img, x, y, C['steel'] if (x + y) % 2 else C['beam'])
    # corner bolts of the fan housing
    for x, y in ((5, 5), (10, 5), (5, 10), (10, 10)):
        put(img, x, y, C['beam'])
        put(img, x, y - 1 if y == 10 else y + 1, C['shadow'])
    return img


def back_face(name, accent):
    img = casing(name + '-back')
    plate = from_jar('assets/mekanism/textures/block/models/ports.png').crop((8, 8, 16, 16))
    leds = from_jar('assets/mekanism/textures/block/models/ports_led.png').crop((8, 8, 16, 16))
    img.alpha_composite(plate, (4, 4))
    img.alpha_composite(leds, (4, 4))
    bright, dark, _ = ACCENT[accent]
    put(img, 7, 13, bright)
    put(img, 8, 13, dark)
    return img


def bottom_face(name):
    img = casing(name + '-bottom')
    for i in range(4, 12):
        put(img, i, 7, C['beam'])
        put(img, i, 8, C['shadow'])
        put(img, 7, i, C['beam'])
        put(img, 8, i, C['shadow'])
    for x, y in ((4, 4), (11, 4), (4, 11), (11, 11)):
        put(img, x, y, C['steel'])
    return img


def main():
    OUT.mkdir(parents=True, exist_ok=True)
    count = 0
    for prefix, _ in TIERS.items():
        for name, accent in MACHINES.items():
            base = f'{prefix}{name}'
            front_face(base, accent, prefix).save(OUT / f'{base}_front.png')
            side_face(base).save(OUT / f'{base}_side.png')
            top_face(base, accent).save(OUT / f'{base}_top.png')
            back_face(base, accent).save(OUT / f'{base}_back.png')
            bottom_face(base).save(OUT / f'{base}_bottom.png')
            count += 5
    # The single tier machines (the item collector, the wind generator fusion and the machine resetter)
    for base, accent in (('item_collector', 'collector'), ('wind_fusion', 'wind'), ('machine_resetter', 'resetter'),
                         ('improved_gas_generator', 'gasgen')):
        front_face(base, accent, '').save(OUT / f'{base}_front.png')
        side_face(base).save(OUT / f'{base}_side.png')
        top_face(base, accent).save(OUT / f'{base}_top.png')
        back_face(base, accent).save(OUT / f'{base}_back.png')
        bottom_face(base).save(OUT / f'{base}_bottom.png')
        count += 5
    print('wrote', count, 'textures to', OUT)


if __name__ == '__main__':
    main()
