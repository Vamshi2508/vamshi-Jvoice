/**
 * J Voice brand marks — the supplied logo artwork, not a rebuild of it.
 *
 * Both crops are generated from the one source file by the `brand/` asset
 * pipeline and served from `public/`, so the web console and the Android app
 * render the same artwork rather than two interpretations of it.
 *
 *  - `JVoiceMark`  the emblem alone (the J and the globe), for the sidebar
 *  - `JVoiceLogo`  the full lockup with wordmark and tagline, for the login hero
 *
 * The PNGs are transparent. The source sits on solid black, which would have
 * shown as a black box on every light surface, so the background was keyed out
 * before import.
 *
 * Sized by WIDTH, not height: the lockup is wider than it is tall and its aspect
 * ratio is fixed by the artwork, so width is the dimension that actually
 * constrains it. Sizing by height means the caller cannot predict how much
 * horizontal room the mark takes and it silently overflows narrow containers.
 */

/**
 * The emblem — the J and the globe.
 *
 * Deliberately the tighter crop rather than the whole emblem. The microphone,
 * the signal waves and the small mic-flag are all in the full artwork, and below
 * roughly 48px those five elements stop being distinguishable and read as
 * coloured noise. Served at 96px so a 34px render stays crisp on 2x displays.
 */
export function JVoiceMark({ size = 34, title = 'J Voice' }) {
  return (
    <img
      src="/jvoice-emblem.png"
      width={size}
      height={size}
      alt={title}
      style={{ display: 'block', flex: '0 0 auto', objectFit: 'contain' }}
    />
  )
}

/**
 * The full lockup: emblem, "J VOICE" wordmark and the NEWS · PEOPLE · TRUTH
 * strip.
 *
 * Best on a dark surface — the logo is designed on near-black, and its wordmark
 * carries a scarlet block that loses contrast against a red or light ground.
 *
 * `height: auto` is explicit so the intrinsic ratio drives the height even
 * though a width attribute is set; without it a stylesheet rule on `img` could
 * stretch the artwork.
 */
export function JVoiceLogo({ width = 300, title = 'J Voice' }) {
  return (
    <img
      className="jv-logo"
      src="/jvoice-logo.png"
      width={width}
      alt={title}
      style={{ display: 'block', width, height: 'auto', margin: '0 auto' }}
    />
  )
}
