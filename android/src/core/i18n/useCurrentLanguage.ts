/**
 * Ported from core/i18n/I18nCompose.kt's `LocalAppLanguage` + `tr()` idiom.
 * React has no CompositionLocal equivalent, but since language is already a
 * Zustand store, components can just select it directly — same effect
 * (any component re-renders when the language changes), less machinery.
 */
import { useLanguageStore } from './useLanguageStore';
import { AppLanguage, LocalizedText, ltGet as _ltGet } from './LocalizedText';

export function useCurrentLanguage(): AppLanguage {
  return useLanguageStore(s => s.language);
}

/** Convenience re-export so screens can do `ltGet(text, language)` without a
 * second import from LocalizedText.ts. */
export function ltGet(value: LocalizedText | null | undefined, language: AppLanguage): string {
  return _ltGet(value, language);
}
