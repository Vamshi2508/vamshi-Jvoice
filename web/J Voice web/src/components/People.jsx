import { useState } from 'react'
import { useStore, useToast } from '../store/store.jsx'
import { Confirm, Empty, Modal, Pill, SearchInput, SectionHead, Stat, Switch } from './ui.jsx'
import AccountEditor, { makeTempPassword } from './AccountEditor.jsx'

/**
 * One people manager reused by Reporters, Editors and Content Creators.
 * The Admin gets the full set here — create a login, edit it, reset the
 * password, suspend, delete — plus whatever assignment that role needs.
 */
export default function PeopleManager({
  role,
  title,
  sub,
  noun,
  columns = [],
  extraStats = () => [],
  rowActions,
  note
}) {
  const { state, dispatch } = useStore()
  const { notify } = useToast()
  const [query, setQuery] = useState('')
  const [edit, setEdit] = useState(null)
  const [remove, setRemove] = useState(null)
  const [reset, setReset] = useState(null)
  const [created, setCreated] = useState(null)

  const people = state.users.filter((u) => u.roles.includes(role))
  const q = query.trim().toLowerCase()
  const rows = people.filter(
    (u) =>
      !q ||
      u.name.toLowerCase().includes(q) ||
      u.email.toLowerCase().includes(q) ||
      u.username.toLowerCase().includes(q)
  )

  return (
    <>
      {note ? <div className="demo-note" style={{ marginBottom: 14 }}>{note}</div> : null}

      <div className="grid grid-4">
        <Stat label={noun + 's'} value={people.length} />
        <Stat label="Active" value={people.filter((u) => u.isActive).length} accent="var(--ok)" />
        <Stat label="Suspended" value={people.filter((u) => !u.isActive).length} accent="var(--danger)" />
        {extraStats(people).map((s) => (
          <Stat key={s.label} label={s.label} value={s.value} accent={s.accent || 'var(--brand)'} />
        ))}
      </div>

      <SectionHead title={title} sub={sub}>
        <SearchInput value={query} onChange={setQuery} placeholder={'Search name, username or email…'} />
        <button className="primary" onClick={() => setEdit({})}>New {noun.toLowerCase()}</button>
      </SectionHead>

      {rows.length === 0 ? (
        <Empty
          title={'No ' + noun.toLowerCase() + 's' + (q ? ' match' : ' yet')}
          description={q ? 'Try a different search.' : 'Create the first login to get started.'}
          actionLabel={'New ' + noun.toLowerCase()}
          onAction={() => setEdit({})}
        />
      ) : (
        <div className="card table-wrap">
          <table>
            <thead>
              <tr>
                <th>{noun}</th>
                <th>Login</th>
                {columns.map((c) => (
                  <th key={c.label} className={c.numeric ? 'num' : undefined}>{c.label}</th>
                ))}
                <th>Active</th>
                <th />
              </tr>
            </thead>
            <tbody>
              {rows.map((u) => (
                <tr key={u.id}>
                  <td>
                    <div className="cell-title">{u.name}</div>
                    <div className="cell-sub">
                      {u.employeeId} · {u.location} · joined {u.joinedOn}
                    </div>
                    {u.roles.length > 1 ? (
                      <div className="tag-row" style={{ marginTop: 4 }}>
                        {u.roles
                          .filter((r) => r !== role)
                          .map((r) => (
                            <span className="tag alt" key={r}>also {r}</span>
                          ))}
                      </div>
                    ) : null}
                  </td>
                  <td>
                    <div className="cell-title mono">{u.username}</div>
                    <div className="cell-sub">{u.email}</div>
                    <div className="cell-sub">{u.phone}</div>
                    {u.mustChangePassword ? (
                      <Pill tone="warn">Password change pending</Pill>
                    ) : null}
                  </td>
                  {columns.map((c) => (
                    <td key={c.label} className={c.numeric ? 'num' : undefined}>{c.render(u)}</td>
                  ))}
                  <td>
                    <Switch
                      checked={u.isActive}
                      onChange={() => {
                        dispatch({ type: 'user/toggleActive', payload: { id: u.id, name: u.name } })
                        notify(u.name + (u.isActive ? ' suspended' : ' activated'))
                      }}
                    />
                  </td>
                  <td className="actions">
                    {rowActions ? rowActions(u) : null}
                    <button className="small ghost" onClick={() => setEdit(u)}>Edit</button>
                    <button className="small ghost" onClick={() => setReset(u)}>Reset password</button>
                    <button className="small danger" onClick={() => setRemove(u)}>Delete</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {edit ? (
        <AccountEditor
          person={edit}
          role={role}
          noun={noun}
          onClose={() => setEdit(null)}
          onSave={({ tempPassword, ...fields }) => {
            dispatch({ type: 'user/save', payload: { id: edit.id, fields } })
            const isNew = !edit.id
            setEdit(null)
            if (isNew) setCreated({ ...fields, tempPassword })
            notify(isNew ? noun + ' login created' : noun + ' updated')
          }}
        />
      ) : null}

      {created ? (
        <CredentialsHandover person={created} noun={noun} onClose={() => setCreated(null)} />
      ) : null}

      {reset ? (
        <ResetPassword
          person={reset}
          onClose={() => setReset(null)}
          onConfirm={() => {
            dispatch({ type: 'user/resetPassword', payload: { id: reset.id, name: reset.name } })
            notify('Temporary password issued for ' + reset.name)
          }}
        />
      ) : null}

      {remove ? (
        <Confirm
          title={'Delete ' + noun.toLowerCase() + '?'}
          message={
            remove.roles.length > 1
              ? remove.name +
                ' also holds ' +
                remove.roles.filter((r) => r !== role).join(' and ') +
                '. Deleting removes the whole login, not just the ' +
                noun.toLowerCase() +
                ' role — drop the role on their Edit form instead if that is what you meant.'
              : remove.name +
                ' (' + remove.username + ') loses access immediately. Work they already filed stays in ' +
                'place; only the login goes.'
          }
          confirmLabel="Delete"
          danger
          onClose={() => setRemove(null)}
          onConfirm={() => {
            dispatch({ type: 'user/delete', payload: { id: remove.id, name: remove.name } })
            setRemove(null)
            notify(noun + ' deleted')
          }}
        />
      ) : null}
    </>
  )
}

/* ------------------------------------------------------------- credentials */

/** Shown once after a login is created — the only time the password is visible. */
function CredentialsHandover({ person, noun, onClose }) {
  const [copied, setCopied] = useState(false)
  const block =
    'J Voice — ' + noun + ' login\n' +
    'Username: ' + person.username + '\n' +
    'Email: ' + person.email + '\n' +
    'Temporary password: ' + person.tempPassword + '\n' +
    'Change it on first sign-in.'

  return (
    <Modal
      title={noun + ' login created'}
      sub="Hand these over now — the password is not shown again."
      onClose={onClose}
      footer={
        <>
          <button
            onClick={() => {
              navigator.clipboard?.writeText(block)
              setCopied(true)
            }}
          >
            {copied ? 'Copied ✓' : 'Copy all'}
          </button>
          <button className="primary" onClick={onClose}>Done</button>
        </>
      }
    >
      <div className="kv"><span className="k">Name</span><span className="v">{person.name}</span></div>
      <div className="kv"><span className="k">Username</span><span className="v mono">{person.username}</span></div>
      <div className="kv"><span className="k">Email</span><span className="v">{person.email}</span></div>
      <div className="kv"><span className="k">Mobile</span><span className="v">{person.phone}</span></div>
      <div className="kv">
        <span className="k">Temporary password</span>
        <span className="v"><code className="password">{person.tempPassword}</code></span>
      </div>
      <div className="demo-note" style={{ marginTop: 12 }}>
        They must change this on first sign-in. If it is lost, use <strong>Reset password</strong> on their
        row to issue a new one.
      </div>
    </Modal>
  )
}

function ResetPassword({ person, onClose, onConfirm }) {
  const [password] = useState(() => makeTempPassword())
  const [done, setDone] = useState(false)
  const [copied, setCopied] = useState(false)

  return (
    <Modal
      title="Reset password"
      sub={person.name + ' · ' + person.username}
      onClose={onClose}
      footer={
        done ? (
          <button className="primary" onClick={onClose}>Done</button>
        ) : (
          <>
            <button onClick={onClose}>Cancel</button>
            <button className="primary" onClick={() => { onConfirm(); setDone(true) }}>
              Issue new password
            </button>
          </>
        )
      }
    >
      {done ? (
        <>
          <div className="kv">
            <span className="k">New temporary password</span>
            <span className="v"><code className="password">{password}</code></span>
          </div>
          <button
            className="small ghost"
            style={{ marginTop: 8 }}
            onClick={() => { navigator.clipboard?.writeText(password); setCopied(true) }}
          >
            {copied ? 'Copied ✓' : 'Copy password'}
          </button>
          <div className="demo-note" style={{ marginTop: 12 }}>
            Their old password stopped working. They must set a new one on next sign-in.
          </div>
        </>
      ) : (
        <p style={{ margin: 0, fontSize: 13.5 }}>
          This revokes {person.name}'s current password straight away and issues a one-time replacement
          you hand over. They will be forced to set a new one on their next sign-in.
        </p>
      )}
    </Modal>
  )
}

/* -------------------------------------------------------------- assignment */

/**
 * Assigning people to the person who reviews them, from the reviewer's side —
 * reporters to a news editor, content creators to a study editor. It is the
 * same `editorIds` link the account form sets from the other direction, so
 * ticking here writes straight through to each member.
 */
export function AssignDialog({
  owner,
  onClose,
  memberRole = 'Reporter',
  memberNoun = 'reporter',
  ownerNoun = 'editor',
  tagsOf = () => '—',
  tagsLabel = 'Beats'
}) {
  const { state, dispatch } = useStore()
  const { notify } = useToast()

  // never offer someone to themselves — a study editor who also writes material
  const members = state.users.filter((u) => u.roles.includes(memberRole) && u.id !== owner.id)
  const [picked, setPicked] = useState(() =>
    members.filter((m) => m.editorIds.includes(owner.id)).map((m) => m.id)
  )
  const toggle = (id) => setPicked((p) => (p.includes(id) ? p.filter((x) => x !== id) : [...p, id]))

  return (
    <Modal
      title={'Assign ' + memberNoun + 's to ' + owner.name}
      sub={
        'Their submissions land in this ' + ownerNoun + "'s queue. A " + memberNoun +
        ' may sit under up to two ' + ownerNoun + 's.'
      }
      wide
      onClose={onClose}
      footer={
        <>
          <span className="cell-sub" style={{ marginRight: 'auto' }}>
            {picked.length} of {members.length} selected
          </span>
          <button onClick={onClose}>Cancel</button>
          <button
            className="primary"
            onClick={() => {
              dispatch({
                type: 'user/assignReporters',
                payload: {
                  editorId: owner.id,
                  reporterIds: picked,
                  editorName: owner.name,
                  memberRole
                }
              })
              onClose()
              notify(owner.name + ' now owns ' + picked.length + ' ' + memberNoun + '(s)')
            }}
          >
            Save assignment
          </button>
        </>
      }
    >
      {members.length === 0 ? (
        <p style={{ margin: 0, fontSize: 13.5 }}>
          No {memberNoun}s to assign yet. Create one first.
        </p>
      ) : (
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th style={{ width: 40 }} />
                <th>{memberNoun[0].toUpperCase() + memberNoun.slice(1)}</th>
                <th>{tagsLabel}</th>
                <th>Other {ownerNoun}s</th>
              </tr>
            </thead>
            <tbody>
              {members.map((m) => {
                const others = m.editorIds.filter((id) => id !== owner.id)
                const atLimit = !picked.includes(m.id) && others.length >= 2
                return (
                  <tr key={m.id}>
                    <td style={{ textAlign: 'center' }}>
                      <input
                        type="checkbox"
                        checked={picked.includes(m.id)}
                        disabled={atLimit}
                        onChange={() => toggle(m.id)}
                      />
                    </td>
                    <td>
                      <div className="cell-title">{m.name}</div>
                      <div className="cell-sub">
                        {m.location}
                        {m.isActive ? '' : ' · suspended'}
                        {m.roles.length > 1 ? ' · also ' + m.roles.filter((r) => r !== memberRole).join(', ') : ''}
                      </div>
                    </td>
                    <td className="cell-sub">{tagsOf(m) || '—'}</td>
                    <td className="cell-sub">
                      {others.length
                        ? others.map((id) => state.users.find((u) => u.id === id)?.name).join(', ')
                        : '—'}
                      {atLimit ? ' (at limit)' : ''}
                    </td>
                  </tr>
                )
              })}
            </tbody>
          </table>
        </div>
      )}
    </Modal>
  )
}
