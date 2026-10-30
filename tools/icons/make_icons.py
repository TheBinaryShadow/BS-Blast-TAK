#!/usr/bin/env python3
"""Generate Blast's original icons (concentric standoff rings).

    python3 tools/icons/make_icons.py

Writes app/src/main/res/drawable/ic_launcher.png (128 px toolbar icon),
app/src/main/res/drawable-nodpi/ic_blast_radial.png (32 px radial button)
and docs/user_manual/plugin_icon.png.
"""
import os
from PIL import Image, ImageDraw

ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

GREEN = (0, 200, 83, 255)
AMBER = (255, 171, 0, 255)
RED = (213, 0, 0, 255)
WHITE = (255, 255, 255, 255)
DARK = (20, 20, 20, 255)


def draw(size):
    ss = 8  # supersample for smooth edges
    s = size * ss
    im = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    d = ImageDraw.Draw(im)
    c = s / 2

    def disc(r, fill, outline=None, width=0):
        d.ellipse((c - r, c - r, c + r, c + r), fill=fill, outline=outline,
                  width=width)

    # Outer preferred line (green ring), amber shelter band, red mandatory.
    disc(0.48 * s, WHITE)
    disc(0.44 * s, GREEN)
    disc(0.375 * s, WHITE)
    disc(0.35 * s, AMBER)
    disc(0.20 * s, WHITE)
    disc(0.175 * s, RED)
    # Hazard diamond at the centre.
    r = 0.09 * s
    d.polygon([(c, c - r), (c + r, c), (c, c + r), (c - r, c)], fill=WHITE)
    r2 = 0.055 * s
    d.polygon([(c, c - r2), (c + r2, c), (c, c + r2), (c - r2, c)], fill=DARK)
    return im.resize((size, size), Image.LANCZOS)


def save(img, rel):
    path = os.path.join(ROOT, rel)
    os.makedirs(os.path.dirname(path), exist_ok=True)
    img.save(path, optimize=True)
    print("wrote", rel)


if __name__ == "__main__":
    big = draw(128)
    save(big, "app/src/main/res/drawable/ic_launcher.png")
    save(big, "docs/user_manual/plugin_icon.png")
    save(draw(32), "app/src/main/res/drawable-nodpi/ic_blast_radial.png")
