import { useMemo, useState } from 'react'
import { effectivePerms, useStore } from '../store/store.jsx'
import { Field, Modal, Pill, Switch } from './ui.jsx'
import { MODULES, PERMS } from '../data/newsData.js'

/* ------------------------------------------------------------------ helpers */

const PASSWORD_ALPHABET = 'abcdefghijkmnpqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ23456789!@#$%'

/** A readable one-time password the admin hands over — never stored as typed. */
export function makeTempPassword() {
  let out = ''
  for (let i = 0; i < 10; i++) {
    out += PASSWORD_ALPHABET[Math.floor(Math.random() * PASSWORD_ALPHABET.length)]
  }
  return out
}

/** kiran.kumar from "Kiran Kumar" — the admin can still overwrite it. */
const suggestUsername = (name) =>
  name.trim().toLowerCase().replace(/[^a-z\s]/g, '').split(/\s+/).filter(Boolean).slice(0, 2).join('.')

const BUILT_IN_ROLES = ['Admin', 'Editor', 'Reporter', 'Content Creator', 'Student']

const PREFIX = {
  Reporter: 'JV-R', Editor: 'JV-E', Admin: 'JV-A',
  'Content Creator': 'JV-C'
}

/** Numbered off the first role held — a Reporter + Editor still gets JV-R-nnn. */
const suggestEmployeeId = (roles, users) => {
  const prefix = PREFIX[roles[0]]
  if (!prefix) return '—'
  const n = users.filter((u) => u.roles[0] === roles[0]).length + 1
  return prefix + '-' + String(n).padStart(3, '0')
}

/** Multi-select pill list — used for beats, sections, subjects and exam types. */
function PickList({ options, selected, onToggle, empty, max }) {
  if (!options.length) return <div className="cell-sub">{empty}</div>
  const atMax = max && selected.length >= max
  return (
    <div className="picklist">
      {options.map((o) => {
        const on = selected.includes(o.id)
        return (
          <button
            type="button"
            key={o.id}
            className={'chip' + (on ? ' on' : '')}
            disabled={!on && atMax}
            title={!on && atMax ? 'Limit of ' + max + ' reached' : undefined}
            onClick={() => onToggle(o.id)}
          >
            {o.emoji ? o.emoji + ' ' : ''}
            {o.label}
          </button>
        )
      })}
    </div>
  )
}

/* ------------------------------------------------------------------- editor */

/**
 * The one form behind every "New <role>" button. Three blocks, always in the
 * same order — login details, account details, then whatever that role needs
 * assigning. Reporters pick beats and their owning editors here; editors pick
 * the sections they run; creators pick subjects and exam types.
 */
export default function AccountEditor({ person, role, noun, roleSelectable, onClose, onSave }) {
  const { state } = useStore()
  const isNew = !person.id
  /**
   * An account can hold more than one role — a reporter who also sub-edits, or a
   * study editor who writes their own material. Pages that manage one role keep
   * that role pinned and let the admin add others alongside it.
   */
  const [roles, setRoles] = useState(() => person.roles || [role])
  const has = (name) => roles.includes(name)

  const [form, setForm] = useState(() => ({
    name: person.name || '',
    username: person.username || '',
    email: person.email || '',
    phone: person.phone || '',
    location: person.location || 'Hyderabad',
    employeeId: person.employeeId || suggestEmployeeId(person.roles || [role], state.users),
    joinedOn: person.joinedOn || 'Today',
    language: person.language || 'Telugu',
    notes: person.notes || '',
    isActive: person.isActive !== false,
    mustChangePassword: isNew ? true : !!person.mustChangePassword,
    beatIds: person.beatIds || [],
    editorIds: person.editorIds || [],
    sectionIds: person.sectionIds || [],
    subjectIds: person.subjectIds || [],
    trackIds: person.trackIds || []
  }))
  const [password, setPassword] = useState(() => (isNew ? makeTempPassword() : null))
  const [copied, setCopied] = useState(false)
  const [showErrors, setShowErrors] = useState(false)

  const set = (patch) => setForm((f) => ({ ...f, ...patch }))
  const toggleIn = (key, id, max) =>
    setForm((f) => {
      const list = f[key]
      if (list.includes(id)) return { ...f, [key]: list.filter((x) => x !== id) }
      if (max && list.length >= max) return f
      return { ...f, [key]: [...list, id] }
    })

  /* ------------------------------------------------------------ validation */
  const taken = state.users.some(
    (u) => u.id !== person.id && u.username.toLowerCase() === form.username.trim().toLowerCase()
  )
  const emailTaken = state.users.some(
    (u) => u.id !== person.id && u.email.toLowerCase() === form.email.trim().toLowerCase()
  )
  const errors = {
    name: form.name.trim() ? null : 'Full name is required',
    username: !form.username.trim()
      ? 'Username is required'
      : taken
        ? 'That username is already taken'
        : null,
    email: !form.email.includes('@')
      ? 'A valid email is required'
      : emailTaken
        ? 'That email already has an account'
        : null,
    phone: form.phone.trim().length >= 10 ? null : 'A contact number is required',
    roles: roles.length ? null : 'Pick at least one role'
  }
  const firstError = Object.values(errors).find(Boolean)

  /* --------------------------------------------------------------- options */
  const categoryOptions = useMemo(
    () => state.categories.map((c) => ({ id: c.id, label: c.nameEn, emoji: c.emoji })),
    [state.categories]
  )
  const editorOptions = useMemo(
    () => state.users.filter((u) => u.roles.includes('Editor')).map((u) => ({ id: u.id, label: u.name })),
    [state.users]
  )
  const subjectOptions = useMemo(
    () => state.subjects.map((s) => ({ id: s.id, label: s.nameEn, emoji: s.emoji })),
    [state.subjects]
  )
  const trackOptions = useMemo(
    () => state.tracks.map((t) => ({ id: t.id, label: t.shortName })),
    [state.tracks]
  )
  const customRoles = roles.filter((r) => !BUILT_IN_ROLES.includes(r))
  // the union of every role held, so the admin sees the real access being granted
  const granted = useMemo(() => effectivePerms(roles, state.roles), [roles, state.roles])

  return (
    <Modal
      title={isNew ? 'New ' + noun.toLowerCase() : 'Edit ' + noun.toLowerCase()}
      sub={
        isNew
          ? 'Everything needed to create the login — credentials, profile, and what they are assigned to.'
          : person.email
      }
      wide
      onClose={onClose}
      footer={
        <>
          {showErrors && firstError ? <span className="field-error">{firstError}</span> : null}
          <button onClick={onClose}>Cancel</button>
          <button
            className="primary"
            onClick={() => {
              setShowErrors(true)
              if (!firstError) onSave({ ...form, roles, tempPassword: password })
            }}
          >
            {isNew ? 'Create login' : 'Save changes'}
          </button>
        </>
      }
    >
      {/* ------------------------------------------------------ 1. login */}
      <div className="form-section">
        <div className="form-section-head">
          <span className="step">1</span>
          <div>
            <div className="cell-title">Login details</div>
            <div className="cell-sub">How they sign in. Username and email must both be unique.</div>
          </div>
        </div>

        <div className="form-row">
          <Field label="Username *" error={showErrors ? errors.username : null}>
            <input
              type="text"
              value={form.username}
              placeholder="firstname.lastname"
              onChange={(e) => set({ username: e.target.value })}
            />
          </Field>
          <Field label="Email *" error={showErrors ? errors.email : null}>
            <input type="text" value={form.email} onChange={(e) => set({ email: e.target.value })} />
          </Field>
        </div>

        <div className="form-row">
          <Field label="Mobile number *" error={showErrors ? errors.phone : null}>
            <input
              type="text"
              value={form.phone}
              placeholder="+91 98480 00000"
              onChange={(e) => set({ phone: e.target.value })}
            />
          </Field>
          <Field label="Account status">
            <div className="inline-switch">
              <Switch checked={form.isActive} onChange={(v) => set({ isActive: v })} />
              <Pill tone={form.isActive ? 'ok' : 'danger'}>
                {form.isActive ? 'Active — can sign in' : 'Suspended — sign-in blocked'}
              </Pill>
            </div>
          </Field>
        </div>

        {isNew ? (
          <Field label="Temporary password">
            <div className="password-row">
              <code className="password">{password}</code>
              <button type="button" className="small ghost" onClick={() => { setPassword(makeTempPassword()); setCopied(false) }}>
                Regenerate
              </button>
              <button
                type="button"
                className="small ghost"
                onClick={() => {
                  navigator.clipboard?.writeText(password)
                  setCopied(true)
                }}
              >
                {copied ? 'Copied ✓' : 'Copy'}
              </button>
            </div>
            <div className="cell-sub" style={{ marginTop: 6 }}>
              Shown once — hand it over now. Only a masked placeholder is kept on the account.
            </div>
          </Field>
        ) : (
          <Field label="Password">
            <div className="password-row">
              <code className="password">••••••••••</code>
              <span className="cell-sub">Set: {person.passwordSetAt}</span>
            </div>
          </Field>
        )}

        <label className="check-line">
          <input
            type="checkbox"
            checked={form.mustChangePassword}
            onChange={(e) => set({ mustChangePassword: e.target.checked })}
          />
          <span>Must change password on first sign-in</span>
        </label>
      </div>

      {/* ---------------------------------------------------- 2. account */}
      <div className="form-section">
        <div className="form-section-head">
          <span className="step">2</span>
          <div>
            <div className="cell-title">Account details</div>
            <div className="cell-sub">Who they are — shown on bylines, tables and the app profile.</div>
          </div>
        </div>

        <div className="form-row">
          <Field label="Full name *" error={showErrors ? errors.name : null}>
            <input
              type="text"
              value={form.name}
              onChange={(e) => {
                const name = e.target.value
                // keep the username in step until the admin edits it themselves
                const auto = !form.username || form.username === suggestUsername(form.name)
                set(auto ? { name, username: suggestUsername(name) } : { name })
              }}
            />
          </Field>
          <Field label="Employee ID">
            <input
              type="text"
              value={form.employeeId}
              onChange={(e) => set({ employeeId: e.target.value })}
            />
          </Field>
        </div>

        <div className="form-row">
          <Field label="Location">
            <input type="text" value={form.location} onChange={(e) => set({ location: e.target.value })} />
          </Field>
          <Field label="Joined on">
            <input type="text" value={form.joinedOn} onChange={(e) => set({ joinedOn: e.target.value })} />
          </Field>
          <Field label="Language">
            <select value={form.language} onChange={(e) => set({ language: e.target.value })}>
              <option value="Telugu">Telugu</option>
              <option value="English">English</option>
            </select>
          </Field>
        </div>

        <Field label="Notes">
          <input
            type="text"
            value={form.notes}
            placeholder="Shift, desk, anything the team should know"
            onChange={(e) => set({ notes: e.target.value })}
          />
        </Field>
      </div>

      {/* --------------------------------------------- 3. roles + assignment */}
      <div className="form-section">
        <div className="form-section-head">
          <span className="step">3</span>
          <div>
            <div className="cell-title">Roles &amp; assignment</div>
            <div className="cell-sub">
              An account can hold more than one role — a reporter who also sub-edits, or a study editor who
              writes their own material. Each role adds its own block below.
            </div>
          </div>
        </div>

        <Field label={'Roles held (' + roles.length + ')'} error={showErrors ? errors.roles : null}>
          <div className="picklist">
            {state.roles.map((r) => {
              const on = has(r.name)
              const pinned = !roleSelectable && r.name === role
              return (
                <button
                  type="button"
                  key={r.id}
                  className={'chip' + (on ? ' on' : '')}
                  disabled={pinned}
                  title={pinned ? 'This page manages ' + role + 's — the role stays on.' : undefined}
                  onClick={() =>
                    setRoles((list) =>
                      list.includes(r.name) ? list.filter((x) => x !== r.name) : [...list, r.name]
                    )
                  }
                >
                  {r.name}
                  {pinned ? ' 🔒' : ''}
                </button>
              )
            })}
          </div>
        </Field>

        {has('Reporter') && has('Editor') ? (
          <div className="warn-note">
            ⚠ This account both files and reviews news. Their own stories are held back from their review
            queue — someone else has to clear those.
          </div>
        ) : null}

        {has('Reporter') ? (
          <div className="role-block">
            <div className="role-block-head">📝 Reporter</div>
            <Field label={'Beats covered (' + form.beatIds.length + ')'}>
              <PickList
                options={categoryOptions}
                selected={form.beatIds}
                onToggle={(id) => toggleIn('beatIds', id)}
                empty="No categories yet."
              />
            </Field>
            <Field label={'Reporting to (' + form.editorIds.length + ' of max 2)'}>
              <PickList
                options={editorOptions}
                selected={form.editorIds}
                onToggle={(id) => toggleIn('editorIds', id, 2)}
                max={2}
                empty="No editors yet — create one first."
              />
              <div className="cell-sub" style={{ marginTop: 6 }}>
                One or two editors. Leave empty and their stories land in the shared queue.
              </div>
            </Field>
          </div>
        ) : null}

        {has('Editor') ? (
          <div className="role-block">
            <div className="role-block-head">🖊️ Editor</div>
            <Field label={'Sections owned (' + form.sectionIds.length + ')'}>
              <PickList
                options={categoryOptions}
                selected={form.sectionIds}
                onToggle={(id) => toggleIn('sectionIds', id)}
                empty="No categories yet."
              />
            </Field>
            <div className="cell-sub">Reporters are assigned to this editor from the Editors page.</div>
          </div>
        ) : null}

        {has('Content Creator') ? (
          <div className="role-block">
            <div className="role-block-head">✍️ Content Creator</div>
            <Field label={'Subjects owned (' + form.subjectIds.length + ')'}>
              <PickList
                options={subjectOptions}
                selected={form.subjectIds}
                onToggle={(id) => toggleIn('subjectIds', id)}
                empty="No subjects yet."
              />
            </Field>
            <Field label={'Exam types (' + form.trackIds.length + ')'}>
              <PickList
                options={trackOptions}
                selected={form.trackIds}
                onToggle={(id) => toggleIn('trackIds', id)}
                empty="No exam types yet."
              />
            </Field>
            <div className="cell-sub">
              They write and publish everything under these subjects — material and question bank.
            </div>
          </div>
        ) : null}

        {has('Student') ? (
          <div className="role-block">
            <div className="role-block-head">🎓 Student</div>
            <Field label="Preparing for">
              <PickList
                options={trackOptions}
                selected={form.trackIds}
                onToggle={(id) => toggleIn('trackIds', id)}
                empty="No exam types yet."
              />
            </Field>
          </div>
        ) : null}

        {has('Admin') ? (
          <div className="role-block">
            <div className="role-block-head">🛡️ Admin</div>
            <div className="cell-sub">
              Full create, read, update and delete on News, Study, Exams and System. Nothing to assign.
            </div>
          </div>
        ) : null}

        {customRoles.length ? (
          <div className="role-block">
            <div className="role-block-head">🔧 {customRoles.join(', ')}</div>
            <div className="cell-sub">
              Custom {customRoles.length === 1 ? 'role' : 'roles'} — what they can do is set by the
              permission matrix on the Roles page.
            </div>
          </div>
        ) : null}

        <Field label="Effective access">
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>Module</th>
                  {PERMS.map((k) => (
                    <th key={k} style={{ textAlign: 'center', textTransform: 'capitalize' }}>
                      {k}
                    </th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {MODULES.map((m) => (
                  <tr key={m}>
                    <td className="cell-title">{m}</td>
                    {PERMS.map((k) => (
                      <td key={k} style={{ textAlign: 'center' }}>
                        {granted[m] && granted[m][k] ? (
                          <span className="tick yes">✓</span>
                        ) : (
                          <span className="tick no">–</span>
                        )}
                      </td>
                    ))}
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <div className="cell-sub" style={{ marginTop: 6 }}>
            Combined from {roles.join(' + ') || 'no roles'}. Change what a role may do on the Roles page.
          </div>
        </Field>
      </div>
    </Modal>
  )
}
