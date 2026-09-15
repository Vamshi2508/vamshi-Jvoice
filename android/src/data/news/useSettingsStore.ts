/**
 * Ported from NewsRepository.kt's `settings` StateFlow and static
 * `rolePermissions` list — in-memory only on Android today (never
 * persisted), ported as-is.
 */
import { create } from 'zustand';
import { DEFAULT_SYSTEM_SETTINGS, RolePermission, SystemSettings } from '../../types/news';

export const ROLE_PERMISSIONS: RolePermission[] = [
  { role: 'READER', permissions: ['Read published news', 'Save articles', 'Comment', 'Report content'] },
  { role: 'REPORTER', permissions: ['File stories', 'Edit own drafts', 'Submit for review', 'View own history'] },
  { role: 'EDITOR', permissions: ['Review submissions', 'Approve / reject / send back', 'Publish news', 'Create AI Shorts'] },
  { role: 'NEWS_ADMIN', permissions: ['Manage all news', 'Manage categories', 'Manage reporters', 'Toggle breaking/featured'] },
  { role: 'SUPER_ADMIN', permissions: ['Everything News Admin can, plus:', 'Manage users & roles', 'System settings', 'Cross-module switching'] },
];

interface SettingsState {
  settings: SystemSettings;
  updateSettings: (transform: (prev: SystemSettings) => SystemSettings) => void;
}

export const useSettingsStore = create<SettingsState>(set => ({
  settings: DEFAULT_SYSTEM_SETTINGS,
  updateSettings: transform => set(state => ({ settings: transform(state.settings) })),
}));
