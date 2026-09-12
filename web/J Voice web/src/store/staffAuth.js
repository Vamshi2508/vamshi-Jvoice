/**
 * Staff sign-in for the console, against Firebase Auth + the Realtime Database.
 *
 * Mirrors the Android implementation deliberately — same `@jvoicenews.com`
 * expansion, same `users/{uid}` lookup, same `isLogin` kill-switch, same
 * `forceLogoutAt` live guard — so the two clients cannot drift into disagreeing
 * about what a session is.
 *
 * ## Which staff can use the console
 *
 * All desk roles, reporters included. A reporter used to authenticate and then
 * be turned away here on the grounds that they file from the Android app, but a
 * reporter at a desk with a browser and no phone to hand had nowhere to go. They
 * now get their own persona with its own narrow surface - file a story, track
 * what happened to it - and emphatically not the review queue. Mapping desk
 * roles onto console personas is [ROLE_TO_PERSONA].
 */

import {
  signInWithEmailAndPassword,
  signOut as fbSignOut,
  onAuthStateChanged
} from 'firebase/auth'
import { get, ref, onValue, serverTimestamp, update } from 'firebase/database'
import { getFirebaseAuth, getFirebaseDb, isFirebaseReady, toEmail } from '../firebase.js'

/**
 * Desk role -> console persona.
 *
 * Four personas (Admin / Editor / Content Creator / Reporter) against seven
 * database roles, so this is still a narrowing on the admin side. `reporter` is
 * mapped to its own persona rather than folded into Editor: the two want
 * opposite things from the news pipeline - a reporter files into the queue, an
 * editor judges what is in it - and collapsing them would hand every reporter
 * approve and publish.
 *
 * An unknown role maps to `undefined`, which is refused at sign-in. Nothing maps
 * to null any more, but a null here would be read as "authenticates, no console"
 * if a role ever needs that again.
 */
export const ROLE_TO_PERSONA = {
  super_admin: 'Admin',
  news_admin: 'Admin',
  study_admin: 'Admin',
  exam_admin: 'Admin',
  editor: 'Editor',
  content_creator: 'Content Creator',
  reporter: 'Reporter'
}

/** Interprets a raw `isLogin` value. Missing means enabled — see AuthGate.kt. */
export function isAccountEnabled(value) {
  if (value === null || value === undefined) return true
  if (typeof value === 'boolean') return value
  if (typeof value === 'number') return value !== 0
  if (typeof value === 'string') {
    const v = value.toLowerCase()
    return !(v === 'false' || v === 'no' || v === '0')
  }
  return true
}

/** A failure the UI can show, with a stable `code` for the caller to branch on. */
class SignInError extends Error {
  constructor(code, message) {
    super(message)
    this.code = code
  }
}

/**
 * Signs in and resolves the console session.
 *
 * Throws [SignInError] with a message already written for a human — the caller
 * shows `error.message` directly.
 */
export async function signInStaff(loginId, password) {
  if (!isFirebaseReady()) {
    throw new SignInError('not-configured', 'Firebase is not configured in this build.')
  }
  const id = String(loginId ?? '').trim()
  if (!id || !password) {
    throw new SignInError('empty', 'Enter your login ID and password.')
  }

  const auth = getFirebaseAuth()
  const db = getFirebaseDb()

  let uid
  try {
    const credential = await signInWithEmailAndPassword(auth, toEmail(id), password)
    uid = credential.user.uid
  } catch (e) {
    throw new SignInError(e.code ?? 'unknown', authErrorMessage(e))
  }

  // Authenticated. Everything below can still refuse the session, and each
  // refusal signs the credential back out so the app is never left half-in.
  const snapshot = await get(ref(db, `users/${uid}`))
  const record = snapshot.val()

  if (!record) {
    await fbSignOut(auth)
    throw new SignInError(
      'no-profile',
      'Your profile is missing from the system. Contact the administrator.'
    )
  }

  if (!isAccountEnabled(record.isLogin)) {
    await fbSignOut(auth)
    throw new SignInError('disabled', 'This account has been disabled. Contact the administrator.')
  }

  const persona = ROLE_TO_PERSONA[record.role]
  if (persona === undefined) {
    await fbSignOut(auth)
    throw new SignInError(
      'no-role',
      'This account has no desk role assigned. Contact the administrator.'
    )
  }
  if (persona === null) {
    await fbSignOut(auth)
    throw new SignInError(
      'no-console-access',
      'This role has no console access. Use the J Voice mobile app.'
    )
  }

  // Re-enable the launch flag and stamp the sign-in. Written while still
  // authenticated, which is the only time the rules permit it.
  await update(ref(db, `users/${uid}`), {
    isLogin: true,
    lastLoginAt: serverTimestamp()
  }).catch((e) => console.warn('[auth] could not stamp sign-in:', e.message))

  return {
    uid,
    role: persona,
    deskRole: record.role,
    loginId: record.loginId ?? id,
    name: record.name ?? record.loginId ?? id,
    email: record.email ?? toEmail(id),
    phone: record.phone ?? ''
  }
}

/**
 * Signs out.
 *
 * `isLogin = false` is written BEFORE `signOut()`, because the rules only permit
 * that write while authenticated. Reversing the order makes it fail silently and
 * leaves the account flagged as still signed in.
 */
export async function signOutStaff(uid) {
  if (!isFirebaseReady()) return
  const auth = getFirebaseAuth()
  const db = getFirebaseDb()
  if (uid) {
    try {
      await update(ref(db, `users/${uid}`), { isLogin: false })
    } catch (e) {
      console.warn('[auth] could not clear isLogin on sign-out:', e.message)
    }
  }
  try {
    await fbSignOut(auth)
  } catch (e) {
    console.warn('[auth] signOut failed:', e.message)
  }
}

/**
 * Watches `users/{uid}/forceLogoutAt` and calls [onForceLogout] when the desk
 * revokes this session.
 *
 * **Skips the first snapshot.** `forceLogoutAt` is never cleared, so a user
 * force-logged-out last week still has a non-zero value; acting on the value
 * present at attach time would sign them out the instant they signed back in.
 * Only a later bump counts. Returns an unsubscribe function.
 */
export function watchForceLogout(uid, onForceLogout) {
  if (!isFirebaseReady() || !uid) return () => {}
  const db = getFirebaseDb()
  let seenInitial = false
  return onValue(
    ref(db, `users/${uid}/forceLogoutAt`),
    (snapshot) => {
      if (!seenInitial) {
        seenInitial = true
        return
      }
      const stamp = Number(snapshot.val() ?? 0)
      if (stamp > 0) onForceLogout()
    },
    (e) => console.warn('[auth] force-logout listener cancelled:', e.message)
  )
}

/** Restores a session on reload, for a browser Firebase still holds a credential for. */
export function observeSession(onSession) {
  if (!isFirebaseReady()) {
    onSession(null)
    return () => {}
  }
  const auth = getFirebaseAuth()
  const db = getFirebaseDb()
  return onAuthStateChanged(auth, async (user) => {
    if (!user) {
      onSession(null)
      return
    }
    try {
      const snapshot = await get(ref(db, `users/${user.uid}`))
      const record = snapshot.val()
      const persona = record ? ROLE_TO_PERSONA[record.role] : undefined
      // A restored session gets the same refusals as a fresh sign-in: a role
      // that was changed or revoked while the tab was closed must not come back.
      if (!record || !persona || !isAccountEnabled(record.isLogin)) {
        await signOutStaff(user.uid)
        onSession(null)
        return
      }
      onSession({
        uid: user.uid,
        role: persona,
        deskRole: record.role,
        loginId: record.loginId ?? '',
        name: record.name ?? record.loginId ?? '',
        email: record.email ?? user.email ?? '',
        phone: record.phone ?? ''
      })
    } catch (e) {
      console.warn('[auth] session restore failed:', e.message)
      onSession(null)
    }
  })
}

/** Firebase error codes -> messages a human can act on. */
function authErrorMessage(e) {
  const code = e.code ?? ''
  switch (code) {
    case 'auth/invalid-credential':
    case 'auth/wrong-password':
    case 'auth/user-not-found':
      // One message for both cases: saying which was wrong confirms whether an
      // account exists.
      return 'Incorrect login ID or password.'
    case 'auth/too-many-requests':
      return 'Too many attempts. Wait a few minutes and try again.'
    case 'auth/network-request-failed':
      return 'Network problem. Check your connection and try again.'
    case 'auth/user-disabled':
      return 'This account has been disabled. Contact the administrator.'
    case 'auth/operation-not-allowed':
      // The exact failure when Email/Password has not been switched on in the
      // console. Worth its own message — it looks like a bad password otherwise.
      return 'Email sign-in is not enabled on the Firebase project yet.'
    case 'auth/configuration-not-found':
      return 'Firebase Authentication has not been set up on the project yet.'
    default:
      return e.message ?? 'Sign-in failed. Please try again.'
  }
}
