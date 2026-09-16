/**
 * Ported from core/auth/{SessionStore,AuthGate,LoginViewModel,ForceLogoutGuard,
 * AppUpdateGate}.kt, collapsed into one Zustand store. Covers both entry
 * paths from LandingScreen.kt: anonymous reader (signInAsReader, no
 * credentials, nothing persisted) and staff sign-in (signIn, real Firebase
 * Auth + RTDB-backed session, persisted via AsyncStorage).
 */
import { create } from 'zustand';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { signInWithEmailAndPassword, signOut as firebaseSignOut } from '@react-native-firebase/auth';
import { ref, get as dbGet, update, onValue, serverTimestamp, increment } from '@react-native-firebase/database';
import { firebaseAuth, rtdb } from '../firebase/firebase';
import { jvRoleFromCode, JvRoleCode, newsRoleForCode } from './JvRole';
import { User } from '../../types/news';

const SESSION_KEY = 'jvoice_desk_session';
const LOGIN_DOMAIN = 'jvoicenews.com';

export interface DeskSession {
  uid: string;
  loginId: string;
  name: string;
  email: string;
  phone: string;
  role: JvRoleCode;
}

export type LoginUiState =
  | { status: 'idle' }
  | { status: 'loading' }
  | { status: 'success'; session: DeskSession }
  | { status: 'error'; message: string };

interface AuthState {
  hydrated: boolean;
  session: DeskSession | null;
  readerUser: User | null;
  loginState: LoginUiState;

  hydrate: () => Promise<void>;
  currentUser: () => User | null;
  signInAsReader: () => void;

  signIn: (loginId: string, password: string) => Promise<void>;
  signOut: () => Promise<void>;
  requestPasswordReset: (phone: string) => Promise<{ ok: boolean; message: string }>;

  /** Live-watches users/{uid}/forceLogoutAt; deliberately skips the first
   * snapshot (a monotonic marker that's never cleared — acting on the
   * initial value would loop-logout a returning user). Call once after
   * sign-in and keep the returned unsubscribe for the session's lifetime. */
  watchForceLogout: (onForceLogout: () => void) => () => void;

  /** Fails open (returns Ok) on any missing node or read error. */
  checkAppUpdate: (installedVersionCode: number) => Promise<
    { kind: 'ok' } | { kind: 'update_required'; requiredVersion: number; blocking: boolean }
  >;
}

function toEmail(loginId: string): string {
  return loginId.includes('@') ? loginId : `${loginId}@${LOGIN_DOMAIN}`;
}

function isAccountEnabled(value: unknown): boolean {
  if (value == null) return true;
  if (value === false || value === 'false' || value === 'no' || value === '0' || value === 0) return false;
  return true;
}

function deviceId(): string {
  // A stable-enough per-install id for rate-limiting failed attempts; a real
  // device-id library can replace this later without changing the RTDB shape.
  return 'app-install';
}

export const useAuthStore = create<AuthState>((set, get) => ({
  hydrated: false,
  session: null,
  readerUser: null,
  loginState: { status: 'idle' },

  hydrate: async () => {
    const raw = await AsyncStorage.getItem(SESSION_KEY);
    if (raw) {
      try {
        const session: DeskSession = JSON.parse(raw);
        if (jvRoleFromCode(session.role)) {
          set({ session, hydrated: true });
          return;
        }
      } catch {
        // fall through to clear
      }
    }
    set({ hydrated: true });
  },

  currentUser: () => {
    const { session, readerUser } = get();
    if (session) {
      const role = newsRoleForCode(jvRoleFromCode(session.role));
      if (!role) return null;
      const user: User = {
        id: session.uid,
        name: session.name,
        email: session.email,
        phone: session.phone,
        role,
        location: 'Hyderabad',
        avatarUrl: '',
        isActive: true,
        joinedOn: '',
      };
      return user;
    }
    return readerUser;
  },

  signInAsReader: () => {
    set({
      readerUser: {
        id: 'local_reader',
        name: 'Reader',
        email: '',
        phone: '',
        role: 'READER',
        location: 'Hyderabad',
        avatarUrl: '',
        isActive: true,
        joinedOn: '',
      },
      session: null,
    });
  },

  signIn: async (loginId, password) => {
    if (!loginId.trim() || !password.trim()) {
      set({ loginState: { status: 'error', message: 'Enter your login ID and password' } });
      return;
    }
    set({ loginState: { status: 'loading' } });
    try {
      const credential = await signInWithEmailAndPassword(firebaseAuth, toEmail(loginId), password);
      const uid = credential.user.uid;
      const snapshot = await dbGet(ref(rtdb, `users/${uid}`));
      const record = snapshot.val();
      if (!record) {
        await firebaseSignOut(firebaseAuth);
        set({ loginState: { status: 'error', message: 'No profile found for this account' } });
        return;
      }
      if (!isAccountEnabled(record.isLogin)) {
        await firebaseSignOut(firebaseAuth);
        set({ loginState: { status: 'error', message: 'This account has been disabled' } });
        return;
      }
      const roleCode = jvRoleFromCode(record.role);
      if (!roleCode) {
        await firebaseSignOut(firebaseAuth);
        set({ loginState: { status: 'error', message: 'No desk role assigned to this account' } });
        return;
      }
      const session: DeskSession = {
        uid,
        loginId: record.loginId ?? loginId,
        name: record.name ?? record.loginId ?? loginId,
        email: record.email ?? toEmail(loginId),
        phone: record.phone ?? '',
        role: roleCode,
      };
      await AsyncStorage.setItem(SESSION_KEY, JSON.stringify(session));
      await update(ref(rtdb, `users/${uid}`), { isLogin: true, lastLoginAt: serverTimestamp() });
      set({ session, readerUser: null, loginState: { status: 'success', session } });
    } catch (err: any) {
      await logFailedAttempt(loginId);
      set({ loginState: { status: 'error', message: friendlyAuthError(err?.code) } });
    }
  },

  signOut: async () => {
    const { session } = get();
    if (session) {
      // Write isLogin=false BEFORE signing out of Firebase Auth — RTDB rules
      // require the caller to still be authenticated to write their own node.
      await update(ref(rtdb, `users/${session.uid}`), { isLogin: false }).catch(() => {});
      await firebaseSignOut(firebaseAuth).catch(() => {});
    }
    await AsyncStorage.removeItem(SESSION_KEY);
    set({ session: null, readerUser: null, loginState: { status: 'idle' } });
  },

  requestPasswordReset: async phone => {
    const digits = phone.replace(/\D/g, '');
    if (digits.length !== 10) {
      return { ok: false, message: 'Enter a valid 10-digit phone number' };
    }
    await update(ref(rtdb, `passwordResetRequests/${digits}`), {
      phone: digits,
      lastRequestedAt: serverTimestamp(),
      requests: increment(1),
    });
    return { ok: true, message: 'Request sent. Our team will contact you shortly.' };
  },

  watchForceLogout: onForceLogout => {
    const { session } = get();
    if (!session) return () => {};
    let seenInitial = false;
    const unsub = onValue(ref(rtdb, `users/${session.uid}/forceLogoutAt`), snapshot => {
      if (!seenInitial) {
        seenInitial = true;
        return;
      }
      const stamp = snapshot.val();
      if (typeof stamp === 'number' && stamp > 0) {
        onForceLogout();
      }
    });
    return unsub;
  },

  checkAppUpdate: async installedVersionCode => {
    const { session } = get();
    if (!session) return { kind: 'ok' };
    try {
      const snapshot = await dbGet(ref(rtdb, `users/${session.uid}/update`));
      const data = snapshot.val();
      if (!data || typeof data.version !== 'number') return { kind: 'ok' };
      if (data.version <= installedVersionCode) return { kind: 'ok' };
      return { kind: 'update_required', requiredVersion: data.version, blocking: Boolean(data.force) };
    } catch {
      return { kind: 'ok' };
    }
  },
}));

async function logFailedAttempt(loginId: string) {
  try {
    await update(ref(rtdb, `failedLoginAttempts/${deviceId()}`), {
      deviceId: deviceId(),
      loginId,
      lastAttemptAt: serverTimestamp(),
      attempts: increment(1),
    });
  } catch {
    // best-effort only
  }
}

function friendlyAuthError(code: string | undefined): string {
  switch (code) {
    case 'auth/invalid-credential':
    case 'auth/wrong-password':
    case 'auth/user-not-found':
      return 'Incorrect login ID or password';
    case 'auth/too-many-requests':
      return 'Too many attempts. Please try again later';
    case 'auth/network-request-failed':
      return 'Network error — check your connection';
    case 'auth/operation-not-allowed':
      return 'Sign-in is temporarily unavailable';
    default:
      return 'Something went wrong. Please try again';
  }
}
