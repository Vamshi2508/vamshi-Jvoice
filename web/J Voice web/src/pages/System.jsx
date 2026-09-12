import { useState } from 'react'
import { useStore, useToast } from '../store/store.jsx'
import {
  Chips, Confirm, DemoNote, Empty, Field, Modal, SearchInput, SectionHead, Stat, Switch
} from '../components/ui.jsx'
import AccountEditor from '../components/AccountEditor.jsx'

/* =================================================================== users */

export function Users() {
  const { state, dispatch } = useStore()
  const { notify } = useToast()
  const roleNames = state.roles.map((r) => r.name)
  const [role, setRole] = useState('All')
  const [query, setQuery] = useState('')
  const [edit, setEdit] = useState(null)
  const [remove, setRemove] = useState(null)

  const rows = state.users.filter((u) => {
    const q = query.trim().toLowerCase()
    return (
      (role === 'All' || u.roles.includes(role)) &&
      (!q || u.name.toLowerCase().includes(q) || u.email.toLowerCase().includes(q))
    )
  })

  return (
    <>
      <DemoNote>
        Every account across both modules. A login can hold more than one role — a reporter who also
        sub-edits, or a study editor who writes their own material — and its access is the union of them
        all. Use <strong>Edit</strong> to add or drop roles.
      </DemoNote>

      <div className="grid grid-4">
        <Stat label="Users" value={state.users.length} />
        <Stat label="Active" value={state.users.filter((u) => u.isActive).length} accent="var(--ok)" />
        <Stat
          label="Suspended"
          value={state.users.filter((u) => !u.isActive).length}
          accent="var(--danger)"
        />
        <Stat
          label="Multi-role"
          value={state.users.filter((u) => u.roles.length > 1).length}
          accent="var(--brand)"
          caption="Hold more than one role"
        />
      </div>

      <SectionHead title={rows.length + ' accounts'} sub="Filter by role, then edit or change access">
        <SearchInput value={query} onChange={setQuery} placeholder="Search name or email…" />
        <button className="primary" onClick={() => setEdit({})}>New user</button>
      </SectionHead>
      <div className="card card-pad" style={{ marginBottom: 14 }}>
        <Chips options={['All', ...roleNames]} value={role} onToggle={setRole} multi={false} />
      </div>

      {rows.length === 0 ? (
        <Empty title="No accounts match" description="Change the role filter or the search." />
      ) : (
        <div className="card table-wrap">
          <table>
            <thead>
              <tr>
                <th>User</th>
                <th>Login</th>
                <th>Role</th>
                <th>Joined</th>
                <th>Active</th>
                <th />
              </tr>
            </thead>
            <tbody>
              {rows.map((u) => (
                <tr key={u.id}>
                  <td>
                    <div className="cell-title">{u.name}</div>
                    <div className="cell-sub">{u.employeeId} · {u.location}</div>
                  </td>
                  <td>
                    <div className="cell-title mono">{u.username}</div>
                    <div className="cell-sub">{u.email}</div>
                    <div className="cell-sub">{u.phone}</div>
                  </td>
                  <td>
                    <div className="tag-row">
                      {u.roles.map((r) => (
                        <span className="tag" key={r}>{r}</span>
                      ))}
                    </div>
                    {u.roles.length > 1 ? (
                      <div className="cell-sub" style={{ marginTop: 4 }}>
                        {u.roles.length} roles — access is the union
                      </div>
                    ) : null}
                  </td>
                  <td className="num">{u.joinedOn}</td>
                  <td>
                    <Switch
                      checked={u.isActive}
                      onChange={() => {
                        dispatch({ type: 'user/toggleActive', payload: { id: u.id, name: u.name } })
                        notify(u.name + (u.isActive ? ' deactivated' : ' activated'))
                      }}
                    />
                  </td>
                  <td className="actions">
                    <button className="small ghost" onClick={() => setEdit(u)}>Edit</button>
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
          role={(edit.roles || [])[0] || 'Student'}
          noun="User"
          roleSelectable
          onClose={() => setEdit(null)}
          onSave={({ tempPassword, ...fields }) => {
            dispatch({ type: 'user/save', payload: { id: edit.id, fields } })
            setEdit(null)
            notify(edit.id ? 'User updated' : fields.roles.join(' + ') + ' login created')
          }}
        />
      ) : null}

      {remove ? (
        <Confirm
          title="Delete user?"
          message={
            remove.name + ' (' + remove.roles.join(' + ') + ') will be removed from the console and the app.'
          }
          confirmLabel="Delete"
          danger
          onClose={() => setRemove(null)}
          onConfirm={() => {
            dispatch({ type: 'user/delete', payload: { id: remove.id, name: remove.name } })
            setRemove(null)
            notify('User deleted')
          }}
        />
      ) : null}
    </>
  )
}

/* ================================================================ settings */

const TOGGLES = [
  ['breakingNewsAlerts', 'Breaking news alerts', 'Push a notification when an article is marked breaking'],
  ['autoPublishApproved', 'Auto-publish on approval', 'Approving a story makes it live without a separate publish'],
  ['commentsEnabled', 'Reader comments', 'Allow comments and replies under articles'],
  ['clipsEnabled', 'Clips tab', 'Show the short-video feed in the reader app'],
  ['aiShortsEnabled', 'AI Shorts pipeline', 'Allow news stories to be turned into video'],
  ['maintenanceMode', 'Maintenance mode', 'Readers and students see a maintenance screen']
]

export function Settings() {
  const { state, dispatch } = useStore()
  const { notify } = useToast()
  const s = state.settings

  const set = (patch, message) => {
    dispatch({ type: 'settings/update', payload: patch })
    notify(message)
  }

  return (
    <>
      <SectionHead title="Platform" sub="Applies to both modules" />
      <div className="card card-pad">
        <div className="form-row">
          <Field label="App name">
            <input
              type="text"
              value={s.appName}
              onChange={(e) => dispatch({ type: 'settings/update', payload: { appName: e.target.value } })}
            />
          </Field>
          <Field label="Default language">
            <select
              value={s.defaultLanguage}
              onChange={(e) => set({ defaultLanguage: e.target.value }, 'Default language updated')}
            >
              <option value="Telugu">Telugu</option>
              <option value="English">English</option>
            </select>
          </Field>
        </div>
      </div>

      <SectionHead title="Features" sub="Switch parts of the app on and off" />
      <div className="card card-pad">
        {TOGGLES.map(([key, label, desc]) => (
          <div
            key={key}
            className="btn-row"
            style={{ padding: '10px 0', borderBottom: '1px solid var(--border)' }}
          >
            <div style={{ flex: 1 }}>
              <div className="cell-title">{label}</div>
              <div className="cell-sub">{desc}</div>
            </div>
            <Switch
              checked={s[key]}
              onChange={(v) => set({ [key]: v }, label + (v ? ' enabled' : ' disabled'))}
            />
          </div>
        ))}
      </div>

      <SectionHead title="Session activity" sub="What has been changed in this console" />
      <div className="card card-pad">
        {state.activity.length === 0 ? (
          <p style={{ margin: 0, color: 'var(--muted)', fontSize: 13.5 }}>Nothing changed yet.</p>
        ) : (
          <ul className="plain activity">
            {state.activity.map((entry) => (
              <li key={entry.id}>{entry.message}</li>
            ))}
          </ul>
        )}
      </div>
    </>
  )
}
