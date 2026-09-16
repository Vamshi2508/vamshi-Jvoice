/**
 * Ported from core/data/StaffDirectory.kt — the desk's people, read from the
 * Realtime Database `users/` tree (not Firestore, to avoid two disagreeing
 * copies of who has which role).
 *
 * KNOWN LIMITATION (inherited from the Kotlin app, not introduced by this
 * rewrite — see docs/audit-report.html follow-up): `web/firebase/
 * database.rules.json` sets `users/.read: false` at the parent node and only
 * grants `.read` at the per-child path `users/$uid`. Firebase RTDB rules do
 * not retroactively grant a read at a shallow path just because a deeper
 * child path allows it — so this listener's `onValue(ref(rtdb, 'users'))`
 * will get a permission-denied and yield an empty list for EVERYONE,
 * including super_admin, until the rules add an explicit `.read` at the
 * `users` node itself for admin roles. The Kotlin class's own doc comment
 * claims this works for admins; it does not, under the rules as written.
 * Ported faithfully (fails closed to an empty list) rather than silently
 * "fixed" here, since changing shared production security rules is a
 * separate, explicit decision — flag it to the user before touching
 * database.rules.json.
 */
import { create } from 'zustand';
import { ref, onValue, update, set as dbSet } from '@react-native-firebase/database';
import { rtdb } from '../firebase/firebase';
import { jvRoleFromCode, newsRoleForCode } from './JvRole';
import { Reporter, User } from '../../types/news';

type Unsub = () => void;

interface StaffState {
  staff: User[];
  reporters: Reporter[];
  permissionDenied: boolean;
  _unsub: Unsub | null;

  start: () => void;
  stop: () => void;

  setActive: (uid: string, active: boolean) => Promise<void>;
  setRole: (uid: string, roleCode: string) => Promise<void>;
  forceLogout: (uid: string) => Promise<void>;
  updateProfile: (uid: string, name: string, email: string, location: string) => Promise<void>;
}

function toUser(uid: string, data: Record<string, unknown>): User | null {
  const roleCode = jvRoleFromCode(typeof data.role === 'string' ? data.role : null);
  const role = newsRoleForCode(roleCode);
  if (!role) return null; // real account, just not one the News module surfaces
  const isLogin = data.isLogin;
  return {
    id: uid,
    name: (data.name as string) || (data.loginId as string) || uid,
    email: (data.email as string) || '',
    phone: (data.phone as string) || '',
    role,
    location: (data.location as string) || '',
    avatarUrl: (data.avatarUrl as string) || '',
    isActive: isLogin == null ? true : isLogin !== false && isLogin !== 'false',
    joinedOn: (data.joinedOn as string) || '',
  };
}

export const useStaffStore = create<StaffState>((set, get) => ({
  staff: [],
  reporters: [],
  permissionDenied: false,
  _unsub: null,

  start: () => {
    if (get()._unsub) return;
    const unsub = onValue(
      ref(rtdb, 'users'),
      snapshot => {
        const value = (snapshot.val() ?? {}) as Record<string, Record<string, unknown>>;
        const people = Object.entries(value)
          .map(([uid, data]) => toUser(uid, data))
          .filter((u): u is User => u !== null);
        set({
          staff: people,
          reporters: people
            .filter(u => u.role === 'REPORTER')
            .map(u => ({ userId: u.id, name: u.name, assignedLocation: u.location, beat: 'General', isActive: u.isActive, avatarUrl: u.avatarUrl })),
          permissionDenied: false,
        });
      },
      () => set({ staff: [], reporters: [], permissionDenied: true }),
    );
    set({ _unsub: unsub });
  },

  stop: () => {
    get()._unsub?.();
    set({ _unsub: null });
  },

  setActive: async (uid, active) => {
    await update(ref(rtdb, `users/${uid}`), { isLogin: active });
  },
  setRole: async (uid, roleCode) => {
    await update(ref(rtdb, `users/${uid}`), { role: roleCode });
  },
  forceLogout: async uid => {
    await dbSet(ref(rtdb, `users/${uid}/forceLogoutAt`), Date.now());
  },
  updateProfile: async (uid, name, email, location) => {
    await update(ref(rtdb, `users/${uid}`), { name, email, location });
  },
}));
