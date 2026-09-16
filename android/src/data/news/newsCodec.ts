/**
 * Ported from core/data/{Firestore,NewsCodec}.kt — exact Firestore field keys
 * for the News module's three collections. `to*()` produces the map written
 * to Firestore; `*From()` decodes a snapshot back into our domain types,
 * degrading to a safe default rather than throwing on a missing/malformed
 * field (mirrors the Kotlin `Map.str/bool/int/long/strList/enum` helpers).
 */
import { DocumentData, DocumentSnapshot, QueryDocumentSnapshot, FieldValue } from '@react-native-firebase/firestore';
import {
  Category,
  NewsArticle,
  NewsStatus,
  NotificationItem,
  NotificationType,
  UserRole,
} from '../../types/news';
import { LocalizedText, toLocalizedText, toLocalizedTextList } from '../../core/i18n/LocalizedText';

export const COLLECTIONS = {
  ARTICLES: 'articles',
  CATEGORIES: 'categories',
  NEWS_NOTIFICATIONS: 'newsNotifications',
  ARTICLE_REPORTS: 'articleReports',
} as const;

type DocSnap = DocumentSnapshot<DocumentData> | QueryDocumentSnapshot<DocumentData>;

function str(data: DocumentData, key: string, fallback = ''): string {
  const v = data[key];
  return typeof v === 'string' ? v : fallback;
}
function strOrNull(data: DocumentData, key: string): string | null {
  const v = data[key];
  return typeof v === 'string' ? v : null;
}
function bool(data: DocumentData, key: string, fallback = false): boolean {
  const v = data[key];
  return typeof v === 'boolean' ? v : fallback;
}
function num(data: DocumentData, key: string, fallback = 0): number {
  const v = data[key];
  return typeof v === 'number' ? v : fallback;
}
function numOrNull(data: DocumentData, key: string): number | null {
  const v = data[key];
  return typeof v === 'number' ? v : null;
}
function strList(data: DocumentData, key: string): string[] {
  const v = data[key];
  return Array.isArray(v) ? v.filter((x): x is string => typeof x === 'string') : [];
}
function localizedOrNull(data: DocumentData, key: string): LocalizedText | null {
  const v = data[key];
  return v == null ? null : toLocalizedText(v);
}

// ---------- articles ----------

export function articleToMap(a: NewsArticle): DocumentData {
  return {
    headline: a.headline,
    shortDescription: a.shortDescription,
    content: a.content,
    categoryId: a.categoryId,
    location: a.location,
    imageUrl: a.imageUrl,
    photoUrls: a.photoUrls,
    videoUrls: a.videoUrls,
    tags: a.tags,
    isBreaking: a.isBreaking,
    isFeatured: a.isFeatured,
    isTrending: a.isTrending,
    status: a.status,
    reporterId: a.reporterId,
    reporterName: a.reporterName,
    reporterAvatarUrl: a.reporterAvatarUrl,
    createdAt: a.createdAt,
    updatedAt: a.updatedAt,
    publishedAt: a.publishedAt,
    rejectionReason: a.rejectionReason,
    editorNote: a.editorNote,
    views: a.views,
    reportCount: a.reportCount,
    // Denormalized, write-only: used for reader ordering without a composite index.
    sortAt: a.publishedAt ?? a.createdAt ?? Date.now(),
  };
}

export function articleFrom(doc: DocSnap): NewsArticle | null {
  const data = doc.data();
  if (!data) return null;
  return {
    id: doc.id,
    headline: toLocalizedText(data.headline),
    shortDescription: toLocalizedText(data.shortDescription),
    content: toLocalizedText(data.content),
    categoryId: str(data, 'categoryId'),
    location: str(data, 'location'),
    imageUrl: str(data, 'imageUrl'),
    photoUrls: strList(data, 'photoUrls'),
    videoUrls: strList(data, 'videoUrls'),
    tags: toLocalizedTextList(data.tags),
    isBreaking: bool(data, 'isBreaking'),
    isFeatured: bool(data, 'isFeatured'),
    isTrending: bool(data, 'isTrending'),
    status: (str(data, 'status', 'DRAFT') as NewsStatus),
    reporterId: str(data, 'reporterId'),
    reporterName: str(data, 'reporterName'),
    reporterAvatarUrl: str(data, 'reporterAvatarUrl'),
    createdAt: num(data, 'createdAt', Date.now()),
    updatedAt: num(data, 'updatedAt', Date.now()),
    publishedAt: numOrNull(data, 'publishedAt'),
    rejectionReason: localizedOrNull(data, 'rejectionReason'),
    editorNote: localizedOrNull(data, 'editorNote'),
    views: num(data, 'views'),
    reportCount: num(data, 'reportCount'),
  };
}

// ---------- categories ----------

export function categoryToMap(c: Category): DocumentData {
  return { name: c.name, emoji: c.emoji, isEnabled: c.isEnabled };
}

export function categoryFrom(doc: DocSnap): Category | null {
  const data = doc.data();
  if (!data) return null;
  return {
    id: doc.id,
    name: toLocalizedText(data.name),
    emoji: str(data, 'emoji', '📰'),
    isEnabled: data.isEnabled == null ? true : bool(data, 'isEnabled', true),
  };
}

// ---------- newsNotifications ----------

export function notificationToMap(n: NotificationItem): DocumentData {
  return {
    title: n.title,
    message: n.message,
    timeMillis: n.timeMillis,
    type: n.type,
    isRead: n.isRead,
    articleId: n.articleId,
    targetRole: n.targetRole,
  };
}

export function notificationFrom(doc: DocSnap): NotificationItem | null {
  const data = doc.data();
  if (!data) return null;
  return {
    id: doc.id,
    title: toLocalizedText(data.title),
    message: toLocalizedText(data.message),
    timeMillis: num(data, 'timeMillis', Date.now()),
    type: (str(data, 'type', 'GENERAL') as NotificationType),
    isRead: bool(data, 'isRead'),
    articleId: strOrNull(data, 'articleId'),
    targetRole: (strOrNull(data, 'targetRole') as UserRole | null),
  };
}

export function increment(by = 1) {
  return FieldValue.increment(by);
}
