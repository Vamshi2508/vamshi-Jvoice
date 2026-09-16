/**
 * Ported from news/data/repository/NewsRepository.kt. A single Zustand store
 * standing in for the Kotlin `object` repository + its StateFlows. Firestore
 * IO lives here only — screens read state via the hook and call these
 * functions, never touching Firestore directly (mirrors the Kotlin
 * repository-owns-IO / ViewModel-is-thin pattern).
 */
import { create } from 'zustand';
import { collection, doc, setDoc, updateDoc, deleteDoc, onSnapshot, query, where, writeBatch, Query } from '@react-native-firebase/firestore';
import { firestoreDb } from '../../core/firebase/firebase';
import {
  Category,
  LOCATIONS,
  NewsArticle,
  NewsStatus,
  NotificationItem,
  UserRole,
} from '../../types/news';
import { LocalizedText } from '../../core/i18n/LocalizedText';
import { COLLECTIONS, articleFrom, articleToMap, categoryFrom, categoryToMap, increment, notificationFrom, notificationToMap } from './newsCodec';

type Unsub = () => void;

interface NewArticleInput {
  headline: LocalizedText;
  shortDescription: LocalizedText;
  content: LocalizedText;
  categoryId: string;
  location: string;
  imageUrl?: string;
  photoUrls?: string[];
  videoUrls?: string[];
  tags?: LocalizedText[];
  isBreaking?: boolean;
  isFeatured?: boolean;
  reporterId: string;
  reporterName: string;
  reporterAvatarUrl?: string;
  status: NewsStatus;
}

interface NewsState {
  articles: NewsArticle[];
  categories: Category[];
  notifications: NotificationItem[];
  savedArticleIds: Set<string>;
  selectedLocation: string;
  isLoading: boolean;
  errorMessage: string | null;

  _articlesUnsub: Unsub | null;
  _categoriesUnsub: Unsub | null;
  _notificationsUnsub: Unsub | null;
  _signedIn: boolean;

  /** Attaches Firestore listeners. Call once at app start, and again via
   * onAuthChanged whenever sign-in state flips (the articles query differs). */
  start: (signedIn: boolean) => void;
  onAuthChanged: (signedIn: boolean) => void;
  stop: () => void;

  setLocation: (location: string) => void;

  // ---- reads / derived helpers ----
  categoryName: (categoryId: string) => string;
  articleById: (id: string) => NewsArticle | undefined;
  articlesInCategory: (categoryId: string) => NewsArticle[];
  reviewQueue: () => NewsArticle[];
  search: (query: string, categoryFilter: string | null, locationFilter: string | null) => NewsArticle[];
  isSaved: (id: string) => boolean;

  // ---- article mutations ----
  registerView: (articleId: string) => Promise<void>;
  reportArticle: (articleId: string) => Promise<void>;
  createOrUpdateArticle: (input: NewArticleInput, existingId?: string) => Promise<string>;
  deleteDraft: (articleId: string) => Promise<void>;
  markUnderReview: (articleId: string) => Promise<void>;
  applyEditorEdits: (
    articleId: string,
    fields: Partial<Pick<NewsArticle, 'headline' | 'shortDescription' | 'content' | 'categoryId' | 'tags'>>,
  ) => Promise<void>;
  approveArticle: (articleId: string, publishNow: boolean) => Promise<void>;
  rejectArticle: (articleId: string, reason: LocalizedText) => Promise<void>;
  sendBackForCorrection: (articleId: string, note: LocalizedText) => Promise<void>;
  publishApproved: (articleId: string) => Promise<void>;
  unpublish: (articleId: string) => Promise<void>;
  toggleBreaking: (articleId: string) => Promise<void>;
  toggleFeatured: (articleId: string) => Promise<void>;
  removeArticle: (articleId: string) => Promise<void>;

  // ---- category mutations ----
  addCategory: (name: LocalizedText, emoji: string) => Promise<void>;
  updateCategory: (id: string, name: LocalizedText, emoji: string) => Promise<void>;
  toggleCategoryEnabled: (id: string) => Promise<void>;
  deleteCategory: (id: string) => Promise<boolean>;

  // ---- notifications ----
  pushNotification: (input: Omit<NotificationItem, 'id' | 'isRead'>) => Promise<void>;
  markNotificationRead: (id: string) => Promise<void>;
  markAllNotificationsRead: (role: UserRole) => Promise<void>;

  // ---- local-only bookmarks (never synced — anonymous readers have nowhere
  // server-side to store them, matches Android behavior) ----
  toggleSaved: (id: string) => void;
  clearSaved: () => void;
}

function sortByRecency(articles: NewsArticle[]): NewsArticle[] {
  return [...articles].sort((a, b) => (b.publishedAt ?? b.createdAt) - (a.publishedAt ?? a.createdAt));
}

function articlesCol() {
  return collection(firestoreDb, COLLECTIONS.ARTICLES);
}
function categoriesCol() {
  return collection(firestoreDb, COLLECTIONS.CATEGORIES);
}
function notificationsCol() {
  return collection(firestoreDb, COLLECTIONS.NEWS_NOTIFICATIONS);
}

export const useNewsStore = create<NewsState>((set, get) => ({
  articles: [],
  categories: [],
  notifications: [],
  savedArticleIds: new Set(),
  selectedLocation: LOCATIONS[0],
  isLoading: true,
  errorMessage: null,

  _articlesUnsub: null,
  _categoriesUnsub: null,
  _notificationsUnsub: null,
  _signedIn: false,

  start: signedIn => {
    set({ _signedIn: signedIn, isLoading: true });
    attachArticles(set, signedIn);
    attachCategories(set);
    attachNotifications(set);
  },

  onAuthChanged: signedIn => {
    if (get()._signedIn === signedIn) return;
    get()._articlesUnsub?.();
    set({ _signedIn: signedIn });
    attachArticles(set, signedIn);
  },

  stop: () => {
    get()._articlesUnsub?.();
    get()._categoriesUnsub?.();
    get()._notificationsUnsub?.();
    set({ _articlesUnsub: null, _categoriesUnsub: null, _notificationsUnsub: null });
  },

  setLocation: location => set({ selectedLocation: location }),

  categoryName: categoryId => get().categories.find(c => c.id === categoryId)?.name.en ?? '',
  articleById: id => get().articles.find(a => a.id === id),
  articlesInCategory: categoryId => sortByRecency(get().articles.filter(a => a.categoryId === categoryId && a.status === 'PUBLISHED')),
  reviewQueue: () =>
    get()
      .articles.filter(a => a.status === 'SUBMITTED' || a.status === 'UNDER_REVIEW')
      .sort((a, b) => a.createdAt - b.createdAt),
  search: (q, categoryFilter, locationFilter) => {
    const query_ = q.trim().toLowerCase();
    return get().articles.filter(a => {
      if (a.status !== 'PUBLISHED') return false;
      if (categoryFilter && a.categoryId !== categoryFilter) return false;
      if (locationFilter && a.location !== locationFilter) return false;
      if (!query_) return true;
      return (
        a.headline.en.toLowerCase().includes(query_) ||
        a.headline.te.toLowerCase().includes(query_) ||
        a.shortDescription.en.toLowerCase().includes(query_) ||
        a.shortDescription.te.toLowerCase().includes(query_)
      );
    });
  },
  isSaved: id => get().savedArticleIds.has(id),

  registerView: async articleId => {
    await updateDoc(doc(articlesCol(), articleId), { views: increment(1) });
  },
  reportArticle: async articleId => {
    await updateDoc(doc(articlesCol(), articleId), { reportCount: increment(1) });
  },

  createOrUpdateArticle: async (input, existingId) => {
    const id = existingId ?? doc(articlesCol()).id;
    const now = Date.now();
    const existing = get().articleById(id);
    const article: NewsArticle = {
      id,
      headline: input.headline,
      shortDescription: input.shortDescription,
      content: input.content,
      categoryId: input.categoryId,
      location: input.location,
      imageUrl: input.imageUrl ?? '',
      photoUrls: input.photoUrls ?? [],
      videoUrls: input.videoUrls ?? [],
      tags: input.tags ?? [],
      isBreaking: input.isBreaking ?? false,
      isFeatured: input.isFeatured ?? false,
      isTrending: existing?.isTrending ?? false,
      status: input.status,
      reporterId: input.reporterId,
      reporterName: input.reporterName,
      reporterAvatarUrl: input.reporterAvatarUrl ?? '',
      createdAt: existing?.createdAt ?? now,
      updatedAt: now,
      publishedAt: input.status === 'PUBLISHED' ? existing?.publishedAt ?? now : existing?.publishedAt ?? null,
      rejectionReason: existing?.rejectionReason ?? null,
      editorNote: existing?.editorNote ?? null,
      views: existing?.views ?? 0,
      reportCount: existing?.reportCount ?? 0,
    };
    await setDoc(doc(articlesCol(), id), articleToMap(article));
    if (input.status === 'SUBMITTED') {
      await get().pushNotification({
        title: { en: 'New story submitted', te: 'కొత్త కథనం సమర్పించారు' },
        message: article.headline,
        timeMillis: Date.now(),
        type: 'SYSTEM',
        articleId: id,
        targetRole: 'EDITOR',
      });
    }
    return id;
  },

  deleteDraft: async articleId => {
    await deleteDoc(doc(articlesCol(), articleId));
  },

  markUnderReview: async articleId => {
    const article = get().articleById(articleId);
    if (article?.status !== 'SUBMITTED') return;
    await updateDoc(doc(articlesCol(), articleId), {
      status: 'UNDER_REVIEW',
      updatedAt: Date.now(),
    });
  },

  applyEditorEdits: async (articleId, fields) => {
    await updateDoc(doc(articlesCol(), articleId), { ...fields, updatedAt: Date.now() });
  },

  approveArticle: async (articleId, publishNow) => {
    const now = Date.now();
    await updateDoc(doc(articlesCol(), articleId), {
      status: publishNow ? 'PUBLISHED' : 'APPROVED',
      publishedAt: publishNow ? now : null,
      ...(publishNow ? { sortAt: now } : {}),
      rejectionReason: null,
      editorNote: null,
      updatedAt: now,
    });
    const article = get().articleById(articleId);
    if (article) {
      await get().pushNotification({
        title: { en: publishNow ? 'Your story was published' : 'Your story was approved', te: '' },
        message: article.headline,
        timeMillis: now,
        type: 'APPROVAL',
        articleId,
        targetRole: 'REPORTER',
      });
      if (publishNow && article.isBreaking) {
        await get().pushNotification({
          title: { en: 'Breaking news', te: 'బ్రేకింగ్ న్యూస్' },
          message: article.headline,
          timeMillis: now,
          type: 'BREAKING',
          articleId,
          targetRole: null,
        });
      }
    }
  },

  rejectArticle: async (articleId, reason) => {
    await updateDoc(doc(articlesCol(), articleId), {
      status: 'REJECTED',
      rejectionReason: reason,
      updatedAt: Date.now(),
    });
    const article = get().articleById(articleId);
    if (article) {
      await get().pushNotification({
        title: { en: 'Your story was rejected', te: '' },
        message: reason,
        timeMillis: Date.now(),
        type: 'REJECTION',
        articleId,
        targetRole: 'REPORTER',
      });
    }
  },

  sendBackForCorrection: async (articleId, note) => {
    await updateDoc(doc(articlesCol(), articleId), {
      status: 'SENT_BACK',
      editorNote: note,
      updatedAt: Date.now(),
    });
    const article = get().articleById(articleId);
    if (article) {
      await get().pushNotification({
        title: { en: 'Your story needs changes', te: '' },
        message: note,
        timeMillis: Date.now(),
        type: 'REJECTION',
        articleId,
        targetRole: 'REPORTER',
      });
    }
  },

  publishApproved: async articleId => {
    const now = Date.now();
    await updateDoc(doc(articlesCol(), articleId), {
      status: 'PUBLISHED',
      publishedAt: now,
      sortAt: now,
      updatedAt: now,
    });
  },

  unpublish: async articleId => {
    await updateDoc(doc(articlesCol(), articleId), { status: 'APPROVED', updatedAt: Date.now() });
  },

  toggleBreaking: async articleId => {
    const article = get().articleById(articleId);
    if (!article) return;
    await updateDoc(doc(articlesCol(), articleId), { isBreaking: !article.isBreaking, updatedAt: Date.now() });
  },

  toggleFeatured: async articleId => {
    const article = get().articleById(articleId);
    if (!article) return;
    await updateDoc(doc(articlesCol(), articleId), { isFeatured: !article.isFeatured, updatedAt: Date.now() });
  },

  removeArticle: async articleId => {
    await deleteDoc(doc(articlesCol(), articleId));
  },

  addCategory: async (name, emoji) => {
    const ref = doc(categoriesCol());
    await setDoc(ref, categoryToMap({ id: ref.id, name, emoji, isEnabled: true }));
  },

  updateCategory: async (id, name, emoji) => {
    await updateDoc(doc(categoriesCol(), id), { name, emoji });
  },

  toggleCategoryEnabled: async id => {
    const category = get().categories.find(c => c.id === id);
    if (!category) return;
    await updateDoc(doc(categoriesCol(), id), { isEnabled: !category.isEnabled });
  },

  deleteCategory: async id => {
    const inUse = get().articles.some(a => a.categoryId === id);
    if (inUse) return false;
    await deleteDoc(doc(categoriesCol(), id));
    return true;
  },

  pushNotification: async input => {
    const ref = doc(notificationsCol());
    const item: NotificationItem = { id: ref.id, isRead: false, ...input };
    await setDoc(ref, notificationToMap(item));
  },

  markNotificationRead: async id => {
    await updateDoc(doc(notificationsCol(), id), { isRead: true });
  },

  markAllNotificationsRead: async role => {
    const batch = writeBatch(firestoreDb);
    get()
      .notifications.filter(n => !n.isRead && (n.targetRole === null || n.targetRole === role))
      .forEach(n => batch.update(doc(notificationsCol(), n.id), { isRead: true }));
    await batch.commit();
  },

  toggleSaved: id =>
    set(state => {
      const next = new Set(state.savedArticleIds);
      if (next.has(id)) next.delete(id);
      else next.add(id);
      return { savedArticleIds: next };
    }),

  clearSaved: () => set({ savedArticleIds: new Set() }),
}));

function attachArticles(set: (partial: Partial<NewsState>) => void, signedIn: boolean) {
  let q: Query = articlesCol();
  if (!signedIn) {
    q = query(articlesCol(), where('status', '==', 'PUBLISHED'));
  }
  const unsub = onSnapshot(
    q,
    snapshot => {
      const articles = snapshot.docs.map(articleFrom).filter((a): a is NewsArticle => a !== null);
      set({ articles: sortByRecency(articles), isLoading: false, errorMessage: null });
    },
    error => set({ errorMessage: error.message, isLoading: false }),
  );
  set({ _articlesUnsub: unsub });
}

function attachCategories(set: (partial: Partial<NewsState>) => void) {
  const unsub = onSnapshot(categoriesCol(), snapshot => {
    const categories = snapshot.docs
      .map(categoryFrom)
      .filter((c): c is Category => c !== null)
      .sort((a, b) => a.name.en.localeCompare(b.name.en));
    set({ categories });
  });
  set({ _categoriesUnsub: unsub });
}

function attachNotifications(set: (partial: Partial<NewsState>) => void) {
  const unsub = onSnapshot(notificationsCol(), snapshot => {
    const notifications = snapshot.docs
      .map(notificationFrom)
      .filter((n): n is NotificationItem => n !== null)
      .sort((a, b) => b.timeMillis - a.timeMillis);
    set({ notifications });
  });
  set({ _notificationsUnsub: unsub });
}
