/**
 * Ported from android core/i18n/LocalizedText.kt (Kotlin) — bilingual English/Telugu
 * value type shared by every content-bearing News field (headline, shortDescription,
 * content, tags, category name, notification title/message, rejection/editor notes).
 */

export type AppLanguage = 'te' | 'en';

export const DEFAULT_LANGUAGE: AppLanguage = 'te';

export const APP_LANGUAGES: Array<{
  code: AppLanguage;
  labelEn: string;
  labelNative: string;
  shortLabel: string;
}> = [
  { code: 'te', labelEn: 'Telugu', labelNative: 'తెలుగు', shortLabel: 'తె' },
  { code: 'en', labelEn: 'English', labelNative: 'English', shortLabel: 'EN' },
];

export interface LocalizedText {
  en: string;
  te: string;
}

export const EMPTY_LOCALIZED: LocalizedText = { en: '', te: '' };

export function lt(en: string, te: string): LocalizedText {
  return { en, te };
}

export function ltEn(value: string): LocalizedText {
  return { en: value, te: '' };
}

export function ltTe(value: string): LocalizedText {
  return { en: '', te: value };
}

/** Reader-facing: falls back to the other language if the requested one is blank. */
export function ltGet(value: LocalizedText | null | undefined, language: AppLanguage): string {
  if (!value) return '';
  const primary = value[language];
  if (primary && primary.trim().length > 0) return primary;
  const other = language === 'en' ? value.te : value.en;
  return other ?? '';
}

/** Authoring-facing: the literal stored value for `language`, no fallback. */
export function ltRawFor(value: LocalizedText | null | undefined, language: AppLanguage): string {
  if (!value) return '';
  return value[language] ?? '';
}

export function ltWith(value: LocalizedText, language: AppLanguage, next: string): LocalizedText {
  return { ...value, [language]: next };
}

export function ltIsComplete(value: LocalizedText | null | undefined): boolean {
  if (!value) return false;
  return value.en.trim().length > 0 && value.te.trim().length > 0;
}

export function ltIsBlank(value: LocalizedText | null | undefined): boolean {
  if (!value) return true;
  return value.en.trim().length === 0 && value.te.trim().length === 0;
}

export function ltMissingLanguages(value: LocalizedText | null | undefined): AppLanguage[] {
  if (!value) return ['en', 'te'];
  const missing: AppLanguage[] = [];
  if (value.en.trim().length === 0) missing.push('en');
  if (value.te.trim().length === 0) missing.push('te');
  return missing;
}

export function ltMatches(value: LocalizedText | null | undefined, query: string): boolean {
  if (!value || !query) return false;
  const q = query.trim().toLowerCase();
  if (!q) return true;
  return value.en.toLowerCase().includes(q) || value.te.toLowerCase().includes(q);
}

export function ltTrimmed(value: LocalizedText): LocalizedText {
  return { en: value.en.trim(), te: value.te.trim() };
}

/** One-line "en / te" display, used in compact desk UI. */
export function ltInline(value: LocalizedText): string {
  return [value.en, value.te].filter(Boolean).join(' / ');
}

export function ltListGet(values: LocalizedText[], language: AppLanguage): string[] {
  return values.map(v => ltGet(v, language)).filter(s => s.length > 0);
}

export function ltListJoin(values: LocalizedText[], language: AppLanguage, sep = ', '): string {
  return ltListGet(values, language).join(sep);
}

export function ltListAnyMatches(values: LocalizedText[], query: string): boolean {
  return values.some(v => ltMatches(v, query));
}

/**
 * Coerces a raw Firestore value into a LocalizedText: accepts an existing
 * {en, te} map, a bare string (treated as English-only — for hand-typed
 * console/Firestore-console values), or null/undefined -> EMPTY_LOCALIZED.
 * Mirrors NewsCodec.kt's `localizedFrom`.
 */
export function toLocalizedText(raw: unknown): LocalizedText {
  if (raw == null) return { ...EMPTY_LOCALIZED };
  if (typeof raw === 'string') return { en: raw, te: '' };
  if (typeof raw === 'object') {
    const obj = raw as Record<string, unknown>;
    return {
      en: typeof obj.en === 'string' ? obj.en : '',
      te: typeof obj.te === 'string' ? obj.te : '',
    };
  }
  return { ...EMPTY_LOCALIZED };
}

export function toLocalizedTextList(raw: unknown): LocalizedText[] {
  if (!Array.isArray(raw)) return [];
  return raw.map(toLocalizedText);
}
