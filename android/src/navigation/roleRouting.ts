import { UserRole } from '../types/news';
import { RootStackParamList } from './types';

/** Mirrors JVoiceNavGraph.kt's startDestination switch on user.role. */
export function routeForRole(role: UserRole): keyof RootStackParamList {
  switch (role) {
    case 'REPORTER':
      return 'ReporterStack';
    case 'EDITOR':
      return 'EditorDrawer';
    case 'NEWS_ADMIN':
      return 'NewsAdminDrawer';
    case 'SUPER_ADMIN':
      return 'SuperAdminDrawer';
    case 'READER':
    default:
      return 'ReaderTabs';
  }
}
