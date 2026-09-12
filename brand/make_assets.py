# -*- coding: utf-8 -*-
"""
Turns the supplied J Voice logo into the asset set both clients need.

The source is a 1254x1254 JPEG on solid black with a glow around the artwork.
Two things have to happen before it is usable in an app:

1. The black has to become transparent, or the logo is a black box on every
   light surface.

2. That cannot be done with a global "make dark pixels transparent" key. The
   microphone is dark grey and its stem crosses black background in the middle
   of the image - a global key would turn the stem into a ghost. So the
   background is found by FLOOD FILLING inward from the borders: only black that
   is connected to the edge is removed, and the dark mic in the interior is left
   alone because nothing connects it to the border.

3. Inside that background region the alpha is ramped rather than set flat to 0.
   The artwork has a glow that fades into the black, and a hard cut would leave
   a dark fringe around the whole mark.

4. The test for "is this black" is MAX CHANNEL, not luminance. The logo's own
   navy (#17318F) has a luminance of 52 - under any threshold loose enough to
   catch the glow - so a luminance key quietly ate the "VOICE" plate and it came
   out translucent on light backgrounds. Its max channel is 143, nowhere near
   black, so max channel separates them cleanly.

Outputs, from one source:
  * the full lockup, trimmed              -> hero / login use
  * the emblem alone (J + globe + mic)    -> launcher icon, favicon
  * Android adaptive foreground + legacy mipmaps at every density
  * web logo + favicons
"""
import io
import os
import sys
from collections import deque

from PIL import Image

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.dirname(HERE)

SRC = os.path.join(HERE, 'jvoice-logo-source.jpg')
ANDROID = os.path.join(ROOT, 'J Voice android app', 'app', 'src', 'main', 'res')
WEB = os.path.join(ROOT, 'J Voice web', 'public')
BRAND = HERE

# Max-channel below LO is certainly background; above HI is certainly artwork.
# Between them the alpha ramps, which is what keeps the glow soft.
LO, HI = 6, 48


def key_out_black(img):
    """RGBA copy of `img` with the border-connected black made transparent."""
    rgb = img.convert('RGB')
    w, h = rgb.size
    px = rgb.load()

    # Max channel per pixel - the "how far from black" measure. See note 4.
    lum = [0] * (w * h)
    for y in range(h):
        row = y * w
        for x in range(w):
            r, g, b = px[x, y]
            lum[row + x] = r if (r >= g and r >= b) else (g if g >= b else b)

    # Flood fill inward from every border pixel that is dark enough. Scanline
    # fill rather than per-pixel BFS - the background is one big region and this
    # keeps it to a couple of seconds in pure Python.
    is_bg = bytearray(w * h)
    stack = deque()

    def maybe_seed(x, y):
        i = y * w + x
        if not is_bg[i] and lum[i] <= HI:
            stack.append((x, y))

    for x in range(w):
        maybe_seed(x, 0)
        maybe_seed(x, h - 1)
    for y in range(h):
        maybe_seed(0, y)
        maybe_seed(w - 1, y)

    while stack:
        x, y = stack.pop()
        i = y * w + x
        if is_bg[i] or lum[i] > HI:
            continue
        # walk left
        xl = x
        while xl > 0 and not is_bg[y * w + xl - 1] and lum[y * w + xl - 1] <= HI:
            xl -= 1
        # walk right
        xr = x
        while xr < w - 1 and not is_bg[y * w + xr + 1] and lum[y * w + xr + 1] <= HI:
            xr += 1
        for xx in range(xl, xr + 1):
            is_bg[y * w + xx] = 1
        for ny in (y - 1, y + 1):
            if 0 <= ny < h:
                base = ny * w
                for xx in range(xl, xr + 1):
                    j = base + xx
                    if not is_bg[j] and lum[j] <= HI:
                        stack.append((xx, ny))

    out = Image.new('RGBA', (w, h))
    op = out.load()
    span = float(HI - LO)
    for y in range(h):
        row = y * w
        for x in range(w):
            i = row + x
            r, g, b = px[x, y]
            if is_bg[i]:
                l = lum[i]
                if l <= LO:
                    a = 0
                else:
                    a = int(min(255, max(0, (l - LO) * 255.0 / span)))
            else:
                a = 255
            op[x, y] = (r, g, b, a)
    return out


def trimmed(img, pad=0):
    """Crops to the visible content, with optional transparent padding."""
    bbox = img.split()[3].getbbox()
    if not bbox:
        return img
    if pad:
        l, t, r, b = bbox
        bbox = (max(0, l - pad), max(0, t - pad),
                min(img.width, r + pad), min(img.height, b + pad))
    return img.crop(bbox)


def fit_into(img, canvas, box_fraction, background=None, radius_fraction=None):
    """
    Centres `img` on a square `canvas`px plate, scaled so its longest side is
    `box_fraction` of the canvas.

    `box_fraction` is how the Android safe zone is respected: an adaptive icon
    may be masked to a circle, and only the middle 66 of 108 units - about 61% -
    is guaranteed to survive.
    """
    target = int(canvas * box_fraction)
    scale = min(target / img.width, target / img.height)
    w, h = max(1, int(round(img.width * scale))), max(1, int(round(img.height * scale)))
    art = img.resize((w, h), Image.LANCZOS)

    plate = Image.new('RGBA', (canvas, canvas), (0, 0, 0, 0))
    if background:
        fill = Image.new('RGBA', (canvas, canvas), background)
        if radius_fraction:
            from PIL import ImageDraw
            mask = Image.new('L', (canvas, canvas), 0)
            ImageDraw.Draw(mask).rounded_rectangle(
                (0, 0, canvas - 1, canvas - 1),
                radius=int(canvas * radius_fraction), fill=255)
            plate.paste(fill, (0, 0), mask)
        else:
            plate = fill
    plate.alpha_composite(art, ((canvas - w) // 2, (canvas - h) // 2))
    return plate


def save(img, path, **kw):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    img.save(path, optimize=True, **kw)
    print('  %-72s %6.0f kB  %dx%d'
          % (os.path.relpath(path, ROOT),
             os.path.getsize(path) / 1024.0, img.width, img.height))


# ---------------------------------------------------------------------------
print('reading', SRC)
src = Image.open(SRC)
print('keying out the black background (flood fill from the borders)...')
keyed = key_out_black(src)

full = trimmed(keyed)
print('full lockup trimmed to %dx%d' % full.size)

# The emblem is the upper part of the lockup: the J, the globe, the mic and the
# waves, above the "J VOICE" wordmark plate. Cropped by proportion and then
# trimmed, so it tracks the artwork rather than hardcoded pixel rows.
emblem = trimmed(full.crop((0, 0, full.width, int(full.height * 0.60))))
print('emblem trimmed to %dx%d' % emblem.size)

# The icon crop: the globe and the J only, dropping the microphone, the signal
# waves and the little mic-flag.
#
# Not a stylistic preference - a legibility one. Rendered at 48px the whole
# emblem is five competing elements and reads as coloured noise; the globe with
# the J over it still reads.
#
# The bounds are measured, not guessed. A per-column scan of the emblem for
# strong-red and strong-blue content puts the globe at 17-54% of the width and
# the J at 49-72%; the mic starts around 66% and the waves around 81%. An earlier
# crop ended at 62% and sliced the J in half, which is exactly the kind of thing
# that looks fine at 432px and is unrecognisable at 48.
ICON_CROP = (0.12, 0.75)
icon_art = trimmed(emblem.crop((int(emblem.width * ICON_CROP[0]), 0,
                                int(emblem.width * ICON_CROP[1]), emblem.height)))
print('icon crop trimmed to %dx%d' % icon_art.size)

NAVY = (13, 31, 99, 255)

print('\nbrand master copies')
save(full, BRAND + '/jvoice-logo-full.png')
save(emblem, BRAND + '/jvoice-emblem.png')
save(icon_art, BRAND + '/jvoice-icon-art.png')

print('\nandroid')
# Hero art: capped at 1024 on the long side. Beyond that it is invisible detail
# carried as APK weight.
hero = full.copy()
hero.thumbnail((1024, 1024), Image.LANCZOS)
save(hero, ANDROID + '/drawable-nodpi/jvoice_logo.png')

# The emblem on its own, for compact in-app spots - a drawer header, a small
# brand row - where the full lockup's wordmark would be unreadable anyway.
emblem_hero = icon_art.copy()
emblem_hero.thumbnail((512, 512), Image.LANCZOS)
save(emblem_hero, ANDROID + '/drawable-nodpi/jvoice_emblem.png')

# Adaptive-icon foreground: transparent, emblem inside the safe zone. The plate
# colour comes from the adaptive-icon background, so it must NOT be baked in.
save(fit_into(icon_art, 432, 0.60), ANDROID + '/drawable-nodpi/ic_launcher_logo.png')

# Legacy launcher PNGs for API 24-25, which predate adaptive icons. The project
# only shipped mipmap-anydpi-v26, so those two API levels had no icon to resolve
# at all - the plate has to be baked in here.
for density, size in [('mdpi', 48), ('hdpi', 72), ('xhdpi', 96),
                      ('xxhdpi', 144), ('xxxhdpi', 192)]:
    icon = fit_into(icon_art, size, 0.78, background=NAVY, radius_fraction=0.18)
    save(icon, '%s/mipmap-%s/ic_launcher.png' % (ANDROID, density))
    round_icon = fit_into(icon_art, size, 0.72, background=NAVY, radius_fraction=0.5)
    save(round_icon, '%s/mipmap-%s/ic_launcher_round.png' % (ANDROID, density))

print('\nweb')
web_logo = full.copy()
web_logo.thumbnail((1024, 1024), Image.LANCZOS)
save(web_logo, WEB + '/jvoice-logo.png')

# The emblem for the sidebar mark, at 2x for crisp rendering at 34px.
save(fit_into(icon_art, 96, 1.0), WEB + '/jvoice-emblem.png')

for size in (16, 32, 48):
    save(fit_into(icon_art, size, 0.94), WEB + '/favicon-%d.png' % size)
# Apple wants an opaque icon - a transparent one renders black on iOS.
save(fit_into(icon_art, 180, 0.80, background=NAVY), WEB + '/apple-touch-icon.png')
save(fit_into(icon_art, 512, 0.80, background=NAVY), WEB + '/jvoice-icon-512.png')

print('\ndone')
