#!/usr/bin/env python3
"""Generate Blast's original icons.

    python3 tools/icons/make_icons.py

- app/src/main/res/drawable/ic_launcher.png      toolbar icon (explosion burst)
- app/src/main/res/drawable-nodpi/ic_blast_radial.png  radial menu button
- app/src/main/res/drawable-nodpi/ic_threat_<row>.png  threat buttons (white)
- docs/user_manual/plugin_icon.png
All shapes are drawn here from primitives; no external artwork.
"""
import math
import os
from PIL import Image, ImageDraw

ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

RED = (213, 0, 0, 255)
ORANGE = (255, 109, 0, 255)
YELLOW = (255, 214, 0, 255)
WHITE = (255, 255, 255, 255)
DARK = (25, 25, 25, 255)
CLEAR = (0, 0, 0, 0)

SS = 8  # supersampling


def burst_points(cx, cy, r_out, r_in, n, rot=0.0, jitter=None):
    pts = []
    for i in range(2 * n):
        r = r_out if i % 2 == 0 else r_in
        if jitter and i % 2 == 0:
            r *= jitter[(i // 2) % len(jitter)]
        a = rot + math.pi * i / n
        pts.append((cx + r * math.sin(a), cy - r * math.cos(a)))
    return pts


def burst(size, outline=True):
    s = size * SS
    im = Image.new("RGBA", (s, s), CLEAR)
    d = ImageDraw.Draw(im)
    c = s / 2
    j = [1.0, 0.86, 0.97, 0.82, 1.0, 0.9, 0.95, 0.84, 0.99, 0.88, 0.93, 0.85]
    if outline:
        d.polygon(burst_points(c, c, 0.50 * s, 0.30 * s, 12, 0.0, j), fill=DARK)
    d.polygon(burst_points(c, c, 0.46 * s, 0.27 * s, 12, 0.0, j), fill=RED)
    d.polygon(burst_points(c, c, 0.36 * s, 0.21 * s, 12, 0.13, j[3:] + j[:3]), fill=ORANGE)
    d.polygon(burst_points(c, c, 0.24 * s, 0.13 * s, 10, 0.3), fill=YELLOW)
    d.ellipse((c - 0.07 * s, c - 0.07 * s, c + 0.07 * s, c + 0.07 * s), fill=WHITE)
    return im.resize((size, size), Image.LANCZOS)


# ---------------------------------------------------------------------------
# Threat silhouettes, drawn on a 100 x 100 grid, white on transparent.

def canvas():
    s = 100 * SS
    im = Image.new("RGBA", (s, s), CLEAR)
    return im, ImageDraw.Draw(im)


def S(*v):
    return [x * SS for x in v]


def rr(d, box, r, fill=WHITE):
    d.rounded_rectangle(S(*box), radius=r * SS, fill=fill)


def wheel(d, cx, cy, r):
    d.ellipse(S(cx - r, cy - r, cx + r, cy + r), fill=WHITE)
    d.ellipse(S(cx - r * 0.45, cy - r * 0.45, cx + r * 0.45, cy + r * 0.45), fill=CLEAR)


def pipe_bomb():
    im, d = canvas()
    rr(d, (14, 40, 80, 62), 3)                       # pipe body
    rr(d, (8, 36, 20, 66), 3)                        # left cap
    rr(d, (74, 36, 86, 66), 3)                       # right cap
    d.line(S(86, 48, 92, 40, 90, 30), fill=WHITE, width=3 * SS, joint="curve")  # fuse
    d.polygon(S(90, 22, 94, 28, 99, 26, 95, 31, 98, 36, 92, 33, 88, 37, 89, 31, 84, 28, 89, 27), fill=WHITE)
    return im


def suicide_bomber():
    im, d = canvas()
    d.ellipse(S(39, 6, 61, 28), fill=WHITE)          # head
    rr(d, (28, 32, 72, 92), 10)                      # torso
    for x in (31, 44, 57):                           # vest charges (cut out)
        rr(d, (x, 46, x + 11, 64), 2, fill=CLEAR)
    d.line(S(28, 70, 72, 70), fill=CLEAR, width=3 * SS)  # wiring band
    return im


def briefcase():
    im, d = canvas()
    rr(d, (36, 18, 64, 34), 5)                       # handle
    rr(d, (42, 23, 58, 34), 2, fill=CLEAR)
    rr(d, (10, 32, 90, 84), 7)                       # case
    d.line(S(10, 52, 90, 52), fill=CLEAR, width=3 * SS)
    rr(d, (44, 47, 56, 57), 2, fill=CLEAR)           # clasp
    return im


def car():
    im, d = canvas()
    d.polygon(S(6, 66, 6, 54, 18, 50, 30, 36, 66, 36, 80, 50, 94, 54, 94, 66), fill=WHITE)
    d.polygon(S(34, 40, 48, 40, 48, 50, 25, 50), fill=CLEAR)   # windows
    d.polygon(S(52, 40, 64, 40, 74, 50, 52, 50), fill=CLEAR)
    wheel(d, 25, 68, 9)
    wheel(d, 75, 68, 9)
    return im


def suv_van():
    im, d = canvas()
    d.polygon(S(6, 68, 6, 30, 70, 30, 84, 46, 94, 50, 94, 68), fill=WHITE)
    rr(d, (12, 36, 30, 48), 2, fill=CLEAR)
    rr(d, (34, 36, 52, 48), 2, fill=CLEAR)
    d.polygon(S(56, 36, 68, 36, 78, 48, 56, 48), fill=CLEAR)
    wheel(d, 24, 70, 9)
    wheel(d, 76, 70, 9)
    return im


def small_truck():
    im, d = canvas()
    rr(d, (4, 22, 64, 70), 2)                        # box
    d.polygon(S(66, 34, 82, 34, 94, 50, 94, 70, 66, 70), fill=WHITE)  # cab
    d.polygon(S(70, 38, 80, 38, 89, 50, 70, 50), fill=CLEAR)
    wheel(d, 20, 72, 9)
    wheel(d, 80, 72, 9)
    return im


def container_truck():
    im, d = canvas()
    rr(d, (4, 30, 66, 60), 15)                       # tank
    rr(d, (8, 60, 66, 66), 1)                        # chassis
    d.polygon(S(68, 34, 82, 34, 94, 50, 94, 70, 68, 70), fill=WHITE)  # cab
    d.polygon(S(72, 38, 80, 38, 89, 50, 72, 50), fill=CLEAR)
    wheel(d, 18, 72, 8)
    wheel(d, 40, 72, 8)
    wheel(d, 82, 72, 8)
    return im


def semi_trailer():
    im, d = canvas()
    rr(d, (2, 30, 70, 64), 2)                        # trailer
    rr(d, (64, 62, 76, 66), 1)                       # hitch
    d.polygon(S(74, 40, 86, 40, 98, 54, 98, 70, 74, 70), fill=WHITE)  # tractor
    d.polygon(S(77, 44, 85, 44, 93, 54, 77, 54), fill=CLEAR)
    wheel(d, 12, 70, 7)
    wheel(d, 28, 70, 7)
    wheel(d, 86, 72, 7)
    return im


THREATS = {
    "pipe_bomb": pipe_bomb,
    "suicide_bomber": suicide_bomber,
    "briefcase": briefcase,
    "car": car,
    "suv_van": suv_van,
    "small_truck": small_truck,
    "container_truck": container_truck,
    "semi_trailer": semi_trailer,
}


def save(img, rel):
    path = os.path.join(ROOT, rel)
    os.makedirs(os.path.dirname(path), exist_ok=True)
    img.save(path, optimize=True)
    print("wrote", rel)


if __name__ == "__main__":
    save(burst(128), "app/src/main/res/drawable/ic_launcher.png")
    save(burst(256), "docs/user_manual/plugin_icon.png")
    save(burst(64), "app/src/main/res/drawable-nodpi/ic_blast_radial.png")
    for name, fn in THREATS.items():
        save(fn().resize((96, 96), Image.LANCZOS),
             "app/src/main/res/drawable-nodpi/ic_threat_" + name + ".png")
