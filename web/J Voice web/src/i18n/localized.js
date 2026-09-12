/**
 * Bilingual text for the console — the JavaScript counterpart of the Android
 * app's `LocalizedText`.
 *
 * Both clients read and write the same Firestore documents, so the wire shape has
 * to match exactly: `{ en, te }`. Anything else and one client silently destroys
 * the other's translations.
 *
 * ## The failure this exists to prevent
 *
 * The console used to store a headline as a single string. Pointed at Firestore
 * unchanged, an editor opening an article would load `{en, te}`, render one of
 * them, and save back a bare string — wiping the Telugu with no error. Every
 * content field in the console is therefore a `{en, te}` pair from here on, and
 * every form edits both sides.
 */

/** Builds a bilingual value. */
export const lt = (en = '', te = '') => ({ en, te })

export const EMPTY_LT = Object.freeze({ en: '', te: '' })

/**
 * Coerces whatever came out of Firestore into a bilingual value.
 *
 * A bare string becomes English-only rather than being dropped — that is what a
 * human typing into the Firestore console produces, and losing their text would
 * be worse than mislabelling its language.
 */
export function toLt(value) {
  if (value == null) return { ...EMPTY_LT }
  if (typeof value === 'string') return { en: value, te: '' }
  if (typeof value === 'object') {
    return { en: value.en ?? '', te: value.te ?? '' }
  }
  return { ...EMPTY_LT }
}

/**
 * The string to display, in `lang`, falling back to the other language.
 *
 * Falling back rather than showing blank is deliberate: a story filed in Telugu
 * and not yet translated should still be readable in the console, not appear to
 * be an empty row.
 */
export function L(value, lang = 'en') {
  const v = toLt(value)
  const primary = lang === 'te' ? v.te : v.en
  const secondary = lang === 'te' ? v.en : v.te
  return primary || secondary || ''
}

/** What is actually stored for `lang` — blank stays blank. Forms bind to this. */
export function raw(value, lang = 'en') {
  const v = toLt(value)
  return lang === 'te' ? v.te : v.en
}

/** Returns a copy with one language replaced. */
export function withLang(value, lang, next) {
  const v = toLt(value)
  return lang === 'te' ? { ...v, te: next } : { ...v, en: next }
}

/** Nothing written in either language. */
export function isBlankLt(value) {
  const v = toLt(value)
  return !v.en.trim() && !v.te.trim()
}

/** Both languages written. Drives the "needs translation" badges. */
export function isCompleteLt(value) {
  const v = toLt(value)
  return Boolean(v.en.trim()) && Boolean(v.te.trim())
}

/** The languages still missing, for the badges. */
export function missingLangs(value) {
  const v = toLt(value)
  const out = []
  if (!v.en.trim()) out.push('en')
  if (!v.te.trim()) out.push('te')
  return out
}

/** Searches both languages at once, whichever the console is displaying. */
export function ltMatches(value, query) {
  const q = String(query ?? '').trim().toLowerCase()
  if (!q) return true
  const v = toLt(value)
  return v.en.toLowerCase().includes(q) || v.te.toLowerCase().includes(q)
}

/** Trims both sides. */
export function trimLt(value) {
  const v = toLt(value)
  return { en: v.en.trim(), te: v.te.trim() }
}

/** A list of bilingual values, in one language, blanks dropped. */
export function ltList(values, lang = 'en') {
  return (values ?? []).map((v) => L(v, lang)).filter(Boolean)
}

/** Coerces a list from Firestore. */
export function toLtList(values) {
  return (values ?? []).map(toLt)
}

/** The two languages, for tab rows. */
export const LANGS = [
  { code: 'en', label: 'English' },
  { code: 'te', label: 'తెలుగు' }
]
