/**
 * Ported from core/auth/JvRole.kt — the single source of truth for the
 * RTDB-stored role string at `users/{uid}/role`, matching the validator in
 * web/firebase/database.rules.json exactly:
 *   /^(reporter|editor|news_admin|content_creator|exam_admin|study_admin|super_admin)$/
 *
 * Only the News-relevant codes matter for this phase; content_creator/
 * exam_admin/study_admin exist here because they're part of the same enum on
 * the Kotlin side, but map to no UserRole yet (Study module is out of scope).
 */
import { UserRole } from '../../types/news';

export type JvRoleCode =
  | 'reporter'
  | 'editor'
  | 'news_admin'
  | 'content_creator'
  | 'exam_admin'
  | 'study_admin'
  | 'super_admin';

const NEWS_ROLE_BY_CODE: Partial<Record<JvRoleCode, UserRole>> = {
  reporter: 'REPORTER',
  editor: 'EDITOR',
  news_admin: 'NEWS_ADMIN',
  super_admin: 'SUPER_ADMIN', // the only code granting both News and Study access
};

/** Normalizes case/hyphens/spaces to underscores, then matches against JvRoleCode. */
export function jvRoleFromCode(raw: string | null | undefined): JvRoleCode | null {
  if (!raw) return null;
  const normalized = raw.trim().toLowerCase().replace(/[-\s]+/g, '_');
  const known: JvRoleCode[] = [
    'reporter',
    'editor',
    'news_admin',
    'content_creator',
    'exam_admin',
    'study_admin',
    'super_admin',
  ];
  return (known as string[]).includes(normalized) ? (normalized as JvRoleCode) : null;
}

/** Readers never sign in — there is deliberately no code that maps to READER. */
export function newsRoleForCode(code: JvRoleCode | null): UserRole | null {
  if (!code) return null;
  return NEWS_ROLE_BY_CODE[code] ?? null;
}
