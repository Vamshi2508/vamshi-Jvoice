import { useState } from 'react'
import { LOGIN_DOMAIN } from '../firebase.js'
import { LOGINS, useAuth } from '../store/store.jsx'
import { Pill } from './ui.jsx'

/**
 * The staff sign-in form.
 *
 * Lifted out of the Login page so the reader's landing page can carry the same
 * form at its foot without a second copy drifting from this one — two sign-in
 * forms that validate differently is exactly the bug this prevents.
 */
export function StaffSignInForm({ onSignedIn }) {
  const { signInStaff, firebaseReady } = useAuth()

  const [loginId, setLoginId] = useState('')
  const [password, setPassword] = useState('')
  const [showPassword, setShowPassword] = useState(false)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState(null)

  async function submit(event) {
    event.preventDefault()
    if (busy) return
    setBusy(true)
    setError(null)
    try {
      await signInStaff(loginId, password)
      // On success the provider swaps the session; the caller decides where to go.
      onSignedIn?.()
    } catch (e) {
      setError(e.message)
    } finally {
      setBusy(false)
    }
  }

  return (
    <form className="card staff-signin" onSubmit={submit}>
      <h3>Staff sign in</h3>
      <p className="cell-sub">Reporters, editors and the desk. Sign in with your J Voice login ID.</p>

      <label className="field">
        <span>Login ID</span>
        <span className="field-with-suffix">
          <input
            type="text"
            value={loginId}
            autoComplete="username"
            placeholder="your username"
            disabled={busy || !firebaseReady}
            onChange={(e) => setLoginId(e.target.value.trim())}
          />
          <span className="field-suffix">@{LOGIN_DOMAIN}</span>
        </span>
      </label>

      <label className="field">
        <span>Password</span>
        <span className="field-with-suffix">
          <input
            type={showPassword ? 'text' : 'password'}
            value={password}
            autoComplete="current-password"
            disabled={busy || !firebaseReady}
            onChange={(e) => setPassword(e.target.value)}
          />
          <button
            type="button"
            className="ghost field-suffix-btn"
            onClick={() => setShowPassword((v) => !v)}
            tabIndex={-1}
          >
            {showPassword ? 'Hide' : 'Show'}
          </button>
        </span>
      </label>

      {error ? <div className="signin-error">{error}</div> : null}

      <button type="submit" className="primary block" disabled={busy || !firebaseReady || !loginId || !password}>
        {busy ? 'Signing in…' : 'Sign in'}
      </button>

      {!firebaseReady ? <div className="signin-error">Firebase is not configured in this build.</div> : null}
    </form>
  )
}

const BADGE = {
  Admin: { icon: '🛡️', scope: 'Full access', tone: 'ok' },
  Editor: { icon: '🖊️', scope: 'News desk', tone: 'info' },
  'Content Creator': { icon: '✍️', scope: 'Study authoring', tone: 'muted' },
  Reporter: { icon: '🎙️', scope: 'Field reporting', tone: 'muted' }
}

/** A persona with no badge would crash the card on a destructure, so unknown
 *  roles fall back rather than take the login page down with them. */
const FALLBACK_BADGE = { icon: '👤', scope: 'Console', tone: 'muted' }

/** The demo persona cards. `compact` drops the access list, for the landing page. */
export function DemoPersonas({ compact = false, onSignedIn }) {
  const { signIn } = useAuth()
  return (
    <div className={compact ? 'grid grid-3' : 'grid grid-2'}>
      {LOGINS.map((login) => {
        const { icon, scope, tone } = BADGE[login.role] || FALLBACK_BADGE
        return (
          <div className="card login-card" key={login.role}>
            <div className="btn-row" style={{ alignItems: 'center', marginBottom: 4 }}>
              <h3 style={{ margin: 0 }}>
                {icon} {login.role}
              </h3>
              <span style={{ flex: 1 }} />
              <Pill tone={tone}>{scope}</Pill>
            </div>

            <p>{login.blurb}</p>

            {compact ? null : (
              <>
                <div className="cell-sub" style={{ margin: '10px 0 4px' }}>
                  {login.role === 'Admin' ? 'Can do everything' : 'What they can do'}
                </div>
                <ul className="access-list">
                  {login.access.map((line) => (
                    <li key={line}>{line}</li>
                  ))}
                </ul>

                <div className="kv" style={{ marginTop: 10 }}>
                  <span className="k">Signs in as</span>
                  <span className="v">{login.name}</span>
                </div>
                <div className="kv">
                  <span className="k">Email</span>
                  <span className="v">{login.email}</span>
                </div>
              </>
            )}

            <button
              className="primary"
              style={{ marginTop: 14, width: '100%' }}
              onClick={() => {
                signIn(login.role)
                onSignedIn?.()
              }}
            >
              Continue as {login.role}
            </button>
          </div>
        )
      })}
    </div>
  )
}
