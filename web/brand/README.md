# J Voice brand assets

Everything in both clients is generated from one file — `jvoice-logo-source.jpg`,
the supplied logo. Run `python make_assets.py` from this folder and it rewrites
every derived asset in both projects. Nothing is hand-edited downstream, so
replacing the source and re-running is all it takes to change the brand.

Requires Pillow (`pip install Pillow`). Nothing else.

## What the script has to do first

The source is a 1254×1254 JPEG of the logo on **solid black**. Two things stand
between that and a usable app asset:

**The black has to go.** A logo on an opaque black square shows as a black box on
every light surface.

**It cannot be keyed globally.** Two traps, both hit during this work:

- *The microphone.* It is dark grey and its stem crosses black background in the
  middle of the image. A "make dark pixels transparent" pass turns the stem into
  a ghost. So the background is found by **flood filling inward from the
  borders** — only black connected to the edge is removed, and the mic survives
  because nothing connects it to the border.

- *The wordmark's navy plate.* `#17318F` has a luminance of 51. Any luminance
  threshold loose enough to catch the logo's outer glow also catches the navy,
  and the "VOICE" block comes out translucent — it looked grey instead of blue on
  light backgrounds. The test is therefore **max channel**, not luminance: black
  is ~0, navy is 143, and they separate cleanly.

Alpha inside the background region is ramped from the pixel's own value rather
than set flat to 0, because the artwork has a glow that fades into the black and
a hard cut leaves a dark fringe around the whole mark.

## The three crops

| File | What it is | Used for |
|---|---|---|
| `jvoice-logo-full.png` | the whole lockup — emblem, wordmark, tagline | Android landing hero, web login hero |
| `jvoice-emblem.png` | the illustration above the wordmark | reference; not shipped directly |
| `jvoice-icon-art.png` | globe + J only | launcher icons, favicons, sidebar mark |

`jvoice-icon-art.png` exists for legibility, not taste. The full emblem is five
competing elements — globe, J, microphone, signal waves, mic-flag — and at 48px
they stop being distinguishable and read as coloured noise. The crop bounds are
**measured**, not guessed: a per-column scan for strong-red and strong-blue
content puts the globe at 17–54% of the emblem width and the J at 49–72%, with
the mic starting around 66%. An earlier crop ended at 62%, sliced the J in half,
and looked perfectly fine at 432px while being unrecognisable at 48. The crop
now runs 12–75%, which keeps the whole J; that necessarily includes a sliver of
the microphone, because the mic overlaps the J's right edge in the original.

## What gets written

**Android** — `J Voice android app/app/src/main/res/`

- `drawable-nodpi/jvoice_logo.png` — hero art, capped at 1024px
- `drawable-nodpi/jvoice_emblem.png` — compact mark
- `drawable-nodpi/ic_launcher_logo.png` — adaptive foreground, transparent, art
  inside the safe zone (only the middle ~61% of an adaptive icon survives the
  circle mask)
- `mipmap-{m,h,xh,xxh,xxxh}dpi/ic_launcher{,_round}.png` — legacy icons with the
  plate baked in

The legacy mipmaps are not optional. `minSdk` is 24 and the project previously
shipped only `mipmap-anydpi-v26`, so API 24–25 had no launcher icon to resolve
at all.

`drawable/ic_launcher_monochrome.xml` stays a hand-drawn **vector** and is not
generated here. Android tints that layer flat to match the wallpaper, and a
photographic crop tinted to a single colour is a blob — the themed icon needs a
silhouette.

**Web** — `J Voice web/public/`

- `jvoice-logo.png` — login hero
- `jvoice-emblem.png` — sidebar mark, 96px so a 34px render stays crisp at 2x
- `favicon-{16,32,48}.png` — resampled per size rather than one scaled image,
  which is what keeps 16px legible
- `apple-touch-icon.png` — opaque; iOS renders a transparent touch icon on black
- `jvoice-icon-512.png` — PWA / large tile

## Palette

Taken from the logo and mirrored in `theme/Color.kt` and `styles.css`:

| | hex |
|---|---|
| scarlet | `#DD0B1E` |
| scarlet bright | `#FF3B4A` |
| royal blue | `#17318F` |
| royal blue bright | `#2A57CC` |
| navy plate | `#0D1F63` |
| silver | `#E9ECF1` |

One deliberate divergence: the **web console's UI accent is the blue, not the
scarlet.** The console already uses red for `--danger`, and an admin tool where
"current page" and "delete" are the same colour is a worse tool. Scarlet is kept
for the brand marks. Dark mode lifts the accent to `#7E9CFF` — `#17318F` reads as
near-black on a dark panel and the nav loses its selected state.
