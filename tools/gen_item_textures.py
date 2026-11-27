"""Generates the item sprites of the mod that are derived from Mekanism's own item art.

The ``improved_configurator`` (and its ``_encoded`` variant, shown while the card holds a recorded configuration)
and the ``improved_portable_teleporter`` are produced here: it is Mekanism's portable teleporter sprite with the
energy window recoloured to the mod's ultimate (magenta) tint and two studs added to the handle, so it reads as the
upgraded version of Mekanism's item while keeping the same silhouette and casing.
"""

import io
import os
import zipfile

from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
JAR = os.path.join(ROOT, 'libs', 'Mekanism-1.21.1-10.7.19.85.jar')
OUT = os.path.join(ROOT, 'src', 'main', 'resources', 'assets', 'mekanismgizmo', 'textures', 'item')

# Ultimate tier colour, the same magenta the tier textures use
ULTIMATE = (0xC8, 0x5A, 0xCB)
ULTIMATE_DARK = (0xB9, 0x4E, 0xC4)
ULTIMATE_LIGHT = (0xF0, 0xA8, 0xF2)


def mekanism_item(path):
    with zipfile.ZipFile(JAR) as jar:
        data = jar.read('assets/mekanism/textures/item/' + path)
    return Image.open(io.BytesIO(data)).convert('RGBA')


def mix(a, b, t):
    return tuple(int(round(a[i] + (b[i] - a[i]) * t)) for i in range(3)) + (255,)


def improved_portable_teleporter():
    img = mekanism_item('portable_teleporter.png').copy()
    for y in range(img.height):
        for x in range(img.width):
            r, g, b, a = img.getpixel((x, y))
            if a == 0:
                continue
            # The cyan/teal energy window of Mekanism's sprite
            if b > r + 20 and g > r + 20:
                lum = (0.299 * r + 0.587 * g + 0.114 * b) / 255.0
                # The window of Mekanism's sprite spans 0.70 - 0.96, so stretch that onto the magenta ramp to keep
                # the glossy highlight instead of flattening it
                t = min(1.0, max(0.0, (lum - 0.70) / 0.26))
                if t < 0.5:
                    colour = mix(ULTIMATE_DARK, ULTIMATE, t * 2)
                else:
                    colour = mix(ULTIMATE, ULTIMATE_LIGHT, (t - 0.5) * 2)
                img.putpixel((x, y), colour)
    # Two studs on the handle: the visual "mark 2" of the improved version
    for x, y in ((6, 12), (8, 12)):
        img.putpixel((x, y), ULTIMATE_LIGHT)
        img.putpixel((x, y + 1), ULTIMATE_DARK)
    return img


def improved_configurator():
    """Mekanism's configuration card with the tier colour: magenta LED and a tier band along the top edge."""
    img = mekanism_item('configuration_card.png').copy()
    for y in range(img.height):
        for x in range(img.width):
            r, g, b, a = img.getpixel((x, y))
            if a == 0:
                continue
            if g > r + 30 and g > b + 30:
                # the green status LED becomes the tier LED, still alternating light and dark like Mekanism's strip
                img.putpixel((x, y), ULTIMATE_LIGHT if x % 2 == 0 else ULTIMATE)
    # tier band along the top edge of the card, like the band on the machines
    for x in range(2, 12):
        img.putpixel((x, 3), ULTIMATE if x % 2 == 0 else ULTIMATE_DARK)
    return img


def improved_configurator_encoded():
    """The same card with its screen lit, mirroring Mekanism's ``configuration_card_encoded`` sprite.

    Mekanism swaps the card's model to an "encoded" sprite once it holds data, so a full card is recognisable in the
    inventory. The screen (the dark 5 x 5 panel at 3..7 / 5..9) becomes a magenta panel with a bright core and the same
    darker frame Mekanism uses, so the two cards read as siblings.
    """
    img = improved_configurator()
    for y in range(4, 11):
        for x in range(2, 9):
            r, g, b, a = img.getpixel((x, y))
            if a == 0:
                continue
            # only the screen pixels: the dark panel, not the light casing around it
            if max(r, g, b) > 0x80:
                continue
            edge = x in (2, 8) or y in (4, 10)
            if edge:
                img.putpixel((x, y), ULTIMATE_DARK if (x + y) % 2 else ULTIMATE)
            else:
                bright = ULTIMATE_LIGHT if y in (6, 8) else ULTIMATE
                img.putpixel((x, y), bright if x % 2 == 0 else mix(ULTIMATE, ULTIMATE_LIGHT, 0.55))
    return img


def main():
    os.makedirs(OUT, exist_ok=True)
    sprite = improved_portable_teleporter()
    for name, sprite in (('improved_portable_teleporter', improved_portable_teleporter()),
                         ('improved_configurator', improved_configurator()),
                         ('improved_configurator_encoded', improved_configurator_encoded())):
        path = os.path.join(OUT, name + '.png')
        sprite.save(path)
        print('wrote', path)


if __name__ == '__main__':
    main()
