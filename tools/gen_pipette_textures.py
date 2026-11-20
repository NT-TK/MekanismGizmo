"""Recolours Mekanism's gauge dropper into the tiered pipettes.

The pipette is Mekanism's own item texture (`mekanism:item/gauge_dropper`) with one change: the black rubber bulb at
the bottom right is tinted with the tier colour, so the glass part stays pixel for pixel identical to Mekanism's art.
The bulb keeps its shading - the darkest outline stays dark, the lit top edge becomes the bright tier colour.

Run:  python tools/gen_pipette_textures.py
"""
import io
import zipfile
from pathlib import Path

from PIL import Image

OUT = Path('src/main/resources/assets/mekanismgizmo/textures/item')
JAR = Path('libs/Mekanism-1.21.1-10.7.19.85.jar')
SOURCE = 'assets/mekanism/textures/item/gauge_dropper.png'

# Tier colours, the same ones the machine front LEDs use (bright, dark)
TIERS = {
    'basic': ((0x27, 0xFF, 0xA1), (0x1F, 0xB3, 0x72)),      # mint
    'advanced': ((0xDD, 0x75, 0x5D), (0xA8, 0x45, 0x3A)),   # salmon
    'elite': ((0x71, 0xE3, 0xE3), (0x22, 0xA8, 0xA9)),      # cyan
    'ultimate': ((0xC8, 0x5A, 0xCB), (0x93, 0x3D, 0x96)),   # magenta
}

# The bulb's shading runs from the 35 outline to the 128 highlight; anything lighter is glass and is left alone.
DARK = 35
LIGHT = 128
GLASS_LEVEL = 150


def base_texture():
    with zipfile.ZipFile(JAR) as jar:
        return Image.open(io.BytesIO(jar.read(SOURCE))).convert('RGBA')


def lerp(a, b, t):
    return tuple(round(a[i] + (b[i] - a[i]) * t) for i in range(3))


def tint(img, bright, dark):
    out = img.copy()
    pixels = out.load()
    for y in range(out.height):
        for x in range(out.width):
            r, g, b, a = pixels[x, y]
            if a == 0 or max(r, g, b) >= GLASS_LEVEL:
                continue
            t = (max(r, g, b) - DARK) / (LIGHT - DARK)
            t = min(1.0, max(0.0, t))
            shade = lerp(dark, bright, t)
            pixels[x, y] = (*shade, a)
    return out


def main():
    OUT.mkdir(parents=True, exist_ok=True)
    base = base_texture()
    for tier, (bright, dark) in TIERS.items():
        tint(base, bright, dark).save(OUT / f'{tier}_pipette.png')
    print('wrote', len(TIERS), 'pipette textures to', OUT)


if __name__ == '__main__':
    main()
