/**
 * Ported 1:1 from android news/data/model/{Models,ClipModels,EngagementModels}.kt
 * and core/data/NewsCodec.kt. This is the canonical schema — it matches
 * `web/firebase/firestore.rules` and the live production Firestore data.
 * Do NOT reshape these to match the web console's field names; the web
 * console itself has schema bugs (see docs/web.html #9) that this app must
 * not inherit.
 */
import { LocalizedText } from '../core/i18n/LocalizedText';

export type UserRole = 'READER' | 'REPORTER' | 'EDITOR' | 'NEWS_ADMIN' | 'SUPER_ADMIN';

export const USER_ROLE_LABELS: Record<UserRole, { label: string; teluguLabel: string }> = {
  READER: { label: 'Reader', teluguLabel: 'పాఠకుడు' },
  REPORTER: { label: 'Reporter', teluguLabel: 'రిపోర్టర్' },
  EDITOR: { label: 'Editor', teluguLabel: 'ఎడిటర్' },
  NEWS_ADMIN: { label: 'News Admin', teluguLabel: 'న్యూస్ అడ్మిన్' },
  SUPER_ADMIN: { label: 'Super Admin', teluguLabel: 'సూపర్ అడ్మిన్' },
};

export type NewsStatus =
  | 'DRAFT'
  | 'SUBMITTED'
  | 'UNDER_REVIEW'
  | 'APPROVED'
  | 'REJECTED'
  | 'SENT_BACK'
  | 'PUBLISHED';

export const NEWS_STATUS_LABELS: Record<NewsStatus, { label: string; teluguLabel: string }> = {
  DRAFT: { label: 'Draft', teluguLabel: 'డ్రాఫ్ట్' },
  SUBMITTED: { label: 'Submitted', teluguLabel: 'సమర్పించారు' },
  UNDER_REVIEW: { label: 'Under Review', teluguLabel: 'సమీక్షలో ఉంది' },
  APPROVED: { label: 'Approved', teluguLabel: 'ఆమోదించారు' },
  REJECTED: { label: 'Rejected', teluguLabel: 'తిరస్కరించారు' },
  SENT_BACK: { label: 'Sent Back', teluguLabel: 'తిరిగి పంపారు' },
  PUBLISHED: { label: 'Published', teluguLabel: 'ప్రచురించారు' },
};

export function isVisibleToReader(status: NewsStatus): boolean {
  return status === 'PUBLISHED';
}

export function isEditableByReporter(status: NewsStatus): boolean {
  return status === 'DRAFT' || status === 'REJECTED' || status === 'SENT_BACK';
}

export interface User {
  id: string;
  name: string;
  email: string;
  phone: string;
  role: UserRole;
  location: string;
  avatarUrl: string;
  isActive: boolean;
  joinedOn: string;
}

export interface Reporter {
  userId: string;
  name: string;
  assignedLocation: string;
  beat: string;
  isActive: boolean;
  avatarUrl: string;
}

export interface ReporterStats {
  reporter: Reporter;
  total: number;
  approved: number;
  rejected: number;
  pending: number;
  published: number;
}

/** Firestore collection `categories`. */
export interface Category {
  id: string;
  name: LocalizedText;
  emoji: string;
  isEnabled: boolean;
}

/** Firestore collection `articles`. Field names here are the exact wire schema. */
export interface NewsArticle {
  id: string;
  headline: LocalizedText;
  shortDescription: LocalizedText;
  content: LocalizedText;
  categoryId: string;
  location: string;
  imageUrl: string;
  photoUrls: string[];
  videoUrls: string[];
  tags: LocalizedText[];
  isBreaking: boolean;
  isFeatured: boolean;
  isTrending: boolean;
  status: NewsStatus;
  reporterId: string;
  reporterName: string;
  reporterAvatarUrl: string;
  createdAt: number;
  updatedAt: number;
  publishedAt: number | null;
  rejectionReason: LocalizedText | null;
  editorNote: LocalizedText | null;
  views: number;
  reportCount: number;
}

export function articleAllPhotos(article: NewsArticle): string[] {
  const photos = [...article.photoUrls];
  if (article.imageUrl && !photos.includes(article.imageUrl)) photos.unshift(article.imageUrl);
  return photos;
}

export type NotificationType = 'BREAKING' | 'APPROVAL' | 'REJECTION' | 'SYSTEM' | 'GENERAL';

/** Firestore collection `newsNotifications`. */
export interface NotificationItem {
  id: string;
  title: LocalizedText;
  message: LocalizedText;
  timeMillis: number;
  type: NotificationType;
  isRead: boolean;
  articleId: string | null;
  /** null means "everyone". */
  targetRole: UserRole | null;
}

export interface RolePermission {
  role: UserRole;
  permissions: string[];
}

/** In-memory only on Android today (never persisted) — ported as-is. */
export interface SystemSettings {
  appName: string;
  defaultLocation: string;
  breakingNewsEnabled: boolean;
  breakingNewsAutoExpiryHours: number;
  pushNotificationsEnabled: boolean;
  emailDigestEnabled: boolean;
  teluguFirstUi: boolean;
  commentsEnabled: boolean;
  autoModerationEnabled: boolean;
  profanityFilterEnabled: boolean;
  maxReportsBeforeAutoHide: number;
}

export const DEFAULT_SYSTEM_SETTINGS: SystemSettings = {
  appName: 'J Voice',
  defaultLocation: 'Hyderabad',
  breakingNewsEnabled: true,
  breakingNewsAutoExpiryHours: 12,
  pushNotificationsEnabled: true,
  emailDigestEnabled: false,
  teluguFirstUi: true,
  commentsEnabled: false,
  autoModerationEnabled: true,
  profanityFilterEnabled: true,
  maxReportsBeforeAutoHide: 5,
};

/** Reference data ported from NewsRepository.kt's static `locations` list. */
export const LOCATIONS: string[] = [
  'Hyderabad',
  'Warangal',
  'Karimnagar',
  'Nizamabad',
  'Khammam',
  'Vijayawada',
  'Visakhapatnam',
  'Guntur',
  'Tirupati',
  'Kurnool',
  'Delhi',
  'Bengaluru',
];

// ---- Engagement (in-memory only on Android today — ported as-is) ----

export type Reaction = 'NONE' | 'LIKE' | 'DISLIKE';

export interface ArticleComment {
  id: string;
  articleId: string;
  authorName: string;
  text: string;
  timeMillis: number;
  likes: number;
  isOwn: boolean;
}

export interface ArticleEngagement {
  articleId: string;
  likes: number;
  dislikes: number;
  myReaction: Reaction;
}

export type ReportReason =
  | 'INCORRECT_FACTS'
  | 'MISLEADING_HEADLINE'
  | 'OUTDATED'
  | 'OFFENSIVE'
  | 'SPAM'
  | 'DUPLICATE'
  | 'OTHER';

export const REPORT_REASON_LABELS: Record<ReportReason, { label: string; hint: string }> = {
  INCORRECT_FACTS: { label: 'Incorrect facts', hint: 'The article states something factually wrong' },
  MISLEADING_HEADLINE: { label: 'Misleading headline', hint: 'The headline does not match the content' },
  OUTDATED: { label: 'Outdated', hint: 'This information is no longer current' },
  OFFENSIVE: { label: 'Offensive', hint: 'Contains offensive or inappropriate content' },
  SPAM: { label: 'Spam', hint: 'Looks like spam or an advertisement' },
  DUPLICATE: { label: 'Duplicate', hint: 'This story was already published elsewhere' },
  OTHER: { label: 'Other', hint: 'Something else' },
};

export interface ArticleReport {
  id: string;
  articleId: string;
  reason: ReportReason;
  suggestion: string;
  reportedBy: string;
  timeMillis: number;
}
