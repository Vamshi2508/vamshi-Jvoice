/**
 * Ported from android core/i18n/LanguagePreference.kt — persisted app-language
 * choice plus the first-run "have you chosen a language yet" / "asked for
 * notifications yet" flags. AsyncStorage stands in for SharedPreferences.
 */
import AsyncStorage from '@react-native-async-storage/async-storage';
import { create } from 'zustand';
import { AppLanguage, DEFAULT_LANGUAGE } from './LocalizedText';

const KEY_LANGUAGE = 'jvoice_language.app_language';
const KEY_CHOSEN = 'jvoice_language.language_chosen';
const KEY_NOTIFICATIONS_ASKED = 'jvoice_language.notifications_asked';

interface LanguageState {
  language: AppLanguage;
  hasChosen: boolean;
  notificationsAsked: boolean;
  hydrated: boolean;
  hydrate: () => Promise<void>;
  /** Applies a language choice without marking it "chosen" — used while the
   * first-run dialog is still open so it can react live. */
  apply: (language: AppLanguage) => void;
  /** Applies + marks chosen, closing the first-run dialog for good. */
  set: (language: AppLanguage) => Promise<void>;
  markChosen: () => Promise<void>;
  markNotificationsAsked: () => Promise<void>;
  toggle: () => Promise<void>;
}

export const useLanguageStore = create<LanguageState>((set, get) => ({
  language: DEFAULT_LANGUAGE,
  hasChosen: false,
  notificationsAsked: false,
  hydrated: false,

  hydrate: async () => {
    const [lang, chosen, asked] = await Promise.all([
      AsyncStorage.getItem(KEY_LANGUAGE),
      AsyncStorage.getItem(KEY_CHOSEN),
      AsyncStorage.getItem(KEY_NOTIFICATIONS_ASKED),
    ]);
    set({
      language: lang === 'en' || lang === 'te' ? lang : DEFAULT_LANGUAGE,
      hasChosen: chosen === 'true',
      notificationsAsked: asked === 'true',
      hydrated: true,
    });
  },

  apply: language => {
    set({ language });
    AsyncStorage.setItem(KEY_LANGUAGE, language).catch(() => {});
  },

  set: async language => {
    set({ language, hasChosen: true });
    await Promise.all([
      AsyncStorage.setItem(KEY_LANGUAGE, language),
      AsyncStorage.setItem(KEY_CHOSEN, 'true'),
    ]);
  },

  markChosen: async () => {
    set({ hasChosen: true });
    await AsyncStorage.setItem(KEY_CHOSEN, 'true');
  },

  markNotificationsAsked: async () => {
    set({ notificationsAsked: true });
    await AsyncStorage.setItem(KEY_NOTIFICATIONS_ASKED, 'true');
  },

  toggle: async () => {
    const next: AppLanguage = get().language === 'en' ? 'te' : 'en';
    await get().set(next);
  },
}));
