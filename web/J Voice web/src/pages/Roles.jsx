import { useState } from 'react'
import { useStore, useToast } from '../store/store.jsx'
import { Confirm, DemoNote, Field, Modal, Pill, SectionHead, Stat } from '../components/ui.jsx'
import { MODULES, PERMS } from '../data/newsData.js'

const TOTAL_PERMS = MODULES.length * PERMS.length

/** Admin is locked at full access — it is the login that owns the console. */
const isLocked = (role) => role.isSystem && role.name === 'Admin'

/* ------------------------------------------------------------ permissions */

/** One module's create / read / update / delete ticks for a single role. */
function PermRow({ role, module }) {
  const { dispatch } = useStore()
  const { notify } = useToast()
  const grid = role.perms[module] || {}
  const onCount = PERMS.filter((k) => grid[k]).length
  const locked = isLocked(role)

  return (
    <tr>
      <td className="cell-title">{module}</td>
      {PERMS.map((perm) => (
        <td key={perm} style={{ textAlign: 'center' }}>
          <input
            type="checkbox"
            checked={!!grid[perm]}
            disabled={locked}
            onChange={() =>
              dispatch({ type: 'role/togglePerm', payload: { id: role.id, module, perm, name: role.name } })
            }
          />
        </td>
      ))}
      <td className="actions">
        <button
          className="small ghost"
          disabled={locked}
          onClick={() => {
            const on = onCount < PERMS.length
            dispatch({ type: 'role/setModule', payload: { id: role.id, module, on, name: role.name } })
            notify(role.name + ': ' + module + (on ? ' full access' : ' no access'))
          }}
        >
          {onCount === PERMS.length ? 'Clear all' : 'Grant all'}
        </button>
      </td>
    </tr>
  )
}

function RoleCard({ role, people, onEdit, onDelete }) {
  const granted = MODULES.reduce((sum, m) => sum + PERMS.filter((k) => role.perms[m]?.[k]).length, 0)
  const full = granted === TOTAL_PERMS

  return (
    <div className="card card-pad">
      <div className="btn-row" style={{ marginBottom: 6, alignItems: 'center' }}>
        <h3 style={{ fontSize: 16, margin: 0 }}>{role.name}</h3>
        <Pill tone={full ? 'ok' : 'info'}>{role.scope}</Pill>
        {role.isSystem ? <Pill tone="muted">Built-in</Pill> : null}
        <span style={{ flex: 1 }} />
        <span className="cell-sub">
          {granted}/{TOTAL_PERMS} permissions · {people} {people === 1 ? 'person' : 'people'}
        </span>
      </div>
      <div className="cell-sub" style={{ marginBottom: 10 }}>{role.description}</div>

      <div className="table-wrap">
        <table>
          <thead>
            <tr>
              <th>Module</th>
              {PERMS.map((perm) => (
                <th key={perm} style={{ textAlign: 'center', textTransform: 'capitalize' }}>
                  {perm}
                </th>
              ))}
              <th />
            </tr>
          </thead>
          <tbody>
            {MODULES.map((m) => (
              <PermRow key={m} role={role} module={m} />
            ))}
          </tbody>
        </table>
      </div>

      <div className="btn-row" style={{ marginTop: 12, alignItems: 'center' }}>
        <button className="small ghost" onClick={onEdit}>Edit role</button>
        <button className="small danger" disabled={role.isSystem} onClick={onDelete}>Delete</button>
        {isLocked(role) ? (
          <span className="cell-sub">Locked — the Admin login always keeps full access.</span>
        ) : role.isSystem ? (
          <span className="cell-sub">Built-in: re-permission it freely, but it cannot be deleted.</span>
        ) : null}
      </div>
    </div>
  )
}

/* ------------------------------------------------------------------- page */

export function Roles() {
  const { state, dispatch } = useStore()
  const { notify } = useToast()
  const [edit, setEdit] = useState(null)
  const [remove, setRemove] = useState(null)

  const inUse = (name) => state.users.filter((u) => u.roles.includes(name)).length

  return (
    <>
      <DemoNote>
        The <strong>Admin</strong> login holds create, read, update and delete on every module — News,
        Study, Exams and System — and that card stays locked on. Every other role is yours to change: tick
        a single permission, grant a whole module at once, or build a new role from scratch.
      </DemoNote>

      <div className="grid grid-4">
        <Stat label="Roles" value={state.roles.length} />
        <Stat label="Built-in" value={state.roles.filter((r) => r.isSystem).length} />
        <Stat label="Custom" value={state.roles.filter((r) => !r.isSystem).length} accent="var(--brand)" />
        <Stat label="People assigned" value={state.users.length} accent="var(--ok)" />
      </div>

      <SectionHead
        title="Roles & permissions"
        sub="Tick what each role may create, read, update and delete"
      >
        <button className="primary" onClick={() => setEdit({})}>New role</button>
      </SectionHead>

      <div className="grid grid-2">
        {state.roles.map((role) => (
          <RoleCard
            key={role.id}
            role={role}
            people={inUse(role.name)}
            onEdit={() => setEdit(role)}
            onDelete={() => setRemove(role)}
          />
        ))}
      </div>

      <SectionHead title="Who holds what" sub="Headcount per role, straight off the Users page" />
      <div className="card table-wrap">
        <table>
          <thead>
            <tr>
              <th>Role</th>
              <th>Scope</th>
              <th>What they do</th>
              <th className="num">People</th>
            </tr>
          </thead>
          <tbody>
            {state.roles.map((r) => (
              <tr key={r.id}>
                <td className="cell-title">{r.name}</td>
                <td><Pill tone="info">{r.scope}</Pill></td>
                <td>{r.description}</td>
                <td className="num">{inUse(r.name)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {edit ? (
        <RoleEditor
          role={edit}
          onClose={() => setEdit(null)}
          onSave={(fields) => {
            dispatch({ type: 'role/save', payload: { id: edit.id, fields } })
            setEdit(null)
            notify(edit.id ? 'Role updated' : 'Role created')
          }}
        />
      ) : null}

      {remove ? (
        <Confirm
          title="Delete role?"
          message={
            inUse(remove.name) > 0
              ? inUse(remove.name) +
                ' people still hold ' +
                remove.name +
                '. Move them to another role first, or they are left with no permissions.'
              : remove.name + ' will be removed. Nobody currently holds it.'
          }
          confirmLabel="Delete"
          danger
          onClose={() => setRemove(null)}
          onConfirm={() => {
            dispatch({ type: 'role/delete', payload: { id: remove.id, name: remove.name } })
            setRemove(null)
            notify('Role deleted')
          }}
        />
      ) : null}
    </>
  )
}

/* ----------------------------------------------------------------- editor */

const BLANK_PERMS = MODULES.reduce(
  (acc, m) => ({ ...acc, [m]: PERMS.reduce((p, k) => ({ ...p, [k]: false }), {}) }),
  {}
)

function RoleEditor({ role, onClose, onSave }) {
  const [form, setForm] = useState({
    name: role.name || '',
    scope: role.scope || 'News only',
    description: role.description || '',
    perms: role.perms || BLANK_PERMS
  })
  const [showErrors, setShowErrors] = useState(false)
  const nameError = form.name.trim() ? null : 'Role name is required'

  const toggle = (module, perm) =>
    setForm({
      ...form,
      perms: { ...form.perms, [module]: { ...form.perms[module], [perm]: !form.perms[module]?.[perm] } }
    })

  return (
    <Modal
      title={role.id ? 'Edit role' : 'New role'}
      sub="Name it, say where it applies, then tick the permissions."
      wide
      onClose={onClose}
      footer={
        <>
          <button onClick={onClose}>Cancel</button>
          <button
            className="primary"
            onClick={() => {
              setShowErrors(true)
              if (!nameError) onSave(form)
            }}
          >
            Save
          </button>
        </>
      }
    >
      <div className="form-row">
        <Field label="Role name *" error={showErrors ? nameError : null}>
          <input
            type="text"
            value={form.name}
            disabled={role.isSystem}
            onChange={(e) => setForm({ ...form, name: e.target.value })}
          />
        </Field>
        <Field label="Scope">
          <select value={form.scope} onChange={(e) => setForm({ ...form, scope: e.target.value })}>
            <option value="News + Study">News + Study</option>
            <option value="News only">News only</option>
            <option value="Study only">Study only</option>
          </select>
        </Field>
      </div>

      <Field label="What they do">
        <input
          type="text"
          value={form.description}
          placeholder="One line — shown on the roles table"
          onChange={(e) => setForm({ ...form, description: e.target.value })}
        />
      </Field>

      <Field label="Permissions">
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Module</th>
                {PERMS.map((perm) => (
                  <th key={perm} style={{ textAlign: 'center', textTransform: 'capitalize' }}>
                    {perm}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody>
              {MODULES.map((m) => (
                <tr key={m}>
                  <td className="cell-title">{m}</td>
                  {PERMS.map((perm) => (
                    <td key={perm} style={{ textAlign: 'center' }}>
                      <input
                        type="checkbox"
                        checked={!!form.perms[m]?.[perm]}
                        onChange={() => toggle(m, perm)}
                      />
                    </td>
                  ))}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </Field>
    </Modal>
  )
}
