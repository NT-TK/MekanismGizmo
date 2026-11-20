"""Builds the mod icon (logo.png) from the potion brewer block textures.

The cube is drawn as a classic 2:1 isometric Minecraft block using the machine's own front/side/top textures,
so the icon always matches the in-game block.

Run:  python tools/gen_logo.py
"""
import numpy as np
from PIL import Image

TEXTURES = 'src/main/resources/assets/mekanismgizmo/textures/block'
OUT = 'src/main/resources/logo.png'
SIZE = 128

def load(name):
    return np.asarray(Image.open(f'{TEXTURES}/{name}.png').convert('RGB'), dtype=np.float32)

front = load('potion_brewer_front')
side = load('potion_brewer_side')
top = load('potion_brewer_top')

canvas = np.zeros((SIZE, SIZE, 4), dtype=np.float32)
canvas[..., 3] = 0.0

def draw_face(texture, origin, e1, e2, shade):
    """Fills the parallelogram origin + u*e1 + v*e2 (u, v in [0, 16)) with the texture."""
    o = np.array(origin, dtype=np.float32)
    e1 = np.array(e1, dtype=np.float32)
    e2 = np.array(e2, dtype=np.float32)
    det = e1[0] * e2[1] - e1[1] * e2[0]
    corners = np.array([o, o + 16 * e1, o + 16 * e2, o + 16 * e1 + 16 * e2])
    x0, y0 = np.floor(corners.min(axis=0)).astype(int)
    x1, y1 = np.ceil(corners.max(axis=0)).astype(int)
    for y in range(max(0, y0), min(SIZE, y1 + 1)):
        for x in range(max(0, x0), min(SIZE, x1 + 1)):
            d = np.array([x + 0.5, y + 0.5], dtype=np.float32) - o
            u = (d[0] * e2[1] - d[1] * e2[0]) / det
            v = (e1[0] * d[1] - e1[1] * d[0]) / det
            if 0 <= u < 16 and 0 <= v < 16:
                texel = texture[int(v), int(u)] * shade
                canvas[y, x, :3] = np.clip(texel, 0, 255)
                canvas[y, x, 3] = 255.0

# Classic 2:1 isometric block: half width 48, half height 24, edge height 48
A = (64, 8)     # top vertex
B = (112, 32)   # right vertex
C = (64, 56)    # bottom vertex of the top face
D = (16, 32)    # left vertex
V = 48          # vertical edge length

# Top face: A -> B (u) and A -> D (v)
draw_face(top, A, ((B[0] - A[0]) / 16, (B[1] - A[1]) / 16), ((D[0] - A[0]) / 16, (D[1] - A[1]) / 16), 1.0)
# Left (front) face: D -> C (u, texture x) and down (v, texture y)
draw_face(front, D, ((C[0] - D[0]) / 16, (C[1] - D[1]) / 16), (0.0, V / 16), 1.0)
# Right face: B -> C (u, texture x) and down (v)
draw_face(side, B, ((C[0] - B[0]) / 16, (C[1] - B[1]) / 16), (0.0, V / 16), 0.80)

# 1px dark outline, so the mostly grey block still reads on a dark mod list background
alpha = canvas[..., 3] > 0
outline = np.zeros_like(alpha)
for dy in (-1, 0, 1):
    for dx in (-1, 0, 1):
        if dx == 0 and dy == 0:
            continue
        shifted = np.roll(np.roll(alpha, dy, axis=0), dx, axis=1)
        outline |= shifted
outline &= ~alpha
canvas[outline, :3] = np.array([10, 10, 12], dtype=np.float32)
canvas[outline, 3] = 255.0

Image.fromarray(canvas.astype(np.uint8), 'RGBA').save(OUT)
print('wrote', OUT)
