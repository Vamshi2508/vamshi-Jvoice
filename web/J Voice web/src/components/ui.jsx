import { useEffect, useState } from 'react'

/* --------------------------------------------------------------- primitives */

export function Pill({ children, tone = 'brand' }) {
  const cls = tone === 'brand' ? 'pill' : 'pill ' + tone
  return <span className={cls}>{children}</span>
}

/** Status colour mapping shared by news, study content and AI shorts. */
export function StatusPill({ status }) {
  const tone =
    {
      Published: 'ok',
      Approved: 'info',
      'Under Review': 'warn',
      Submitted: 'warn',
      'Pending Review': 'warn',
      Rejected: 'danger',
      Failed: 'danger',
      'Sent Back': 'danger',
      Draft: 'muted',
      'Script ready': 'info',
      Rendering: 'warn',
      Ready: 'info'
    }[status] || 'muted'
  return <Pill tone={tone}>{status}</Pill>
}

export function Stat({ label, value, caption, accent = 'var(--brand)', onClick }) {
  return (
    <div className={'card stat' + (onClick ? ' clickable' : '')} onClick={onClick}>
      <div className="accent" style={{ background: accent }} />
      <div className="value" style={{ color: accent }}>{value}</div>
      <div className="label">{label}</div>
      {caption ? <div className="caption">{caption}</div> : null}
    </div>
  )
}

export function SectionHead({ title, sub, children }) {
  return (
    <div className="section-head">
      <div>
        <h2>{title}</h2>
        {sub ? <div className="sub">{sub}</div> : null}
      </div>
      <div className="spacer" />
      {children}
    </div>
  )
}

export function Empty({ title, description, actionLabel, onAction }) {
  return (
    <div className="card empty">
      <h3>{title}</h3>
      {description ? <p>{description}</p> : null}
      {actionLabel ? (
        <button className="primary" onClick={onAction} style={{ marginTop: 10 }}>
          {actionLabel}
        </button>
      ) : null}
    </div>
  )
}

export function DemoNote({ children }) {
  return <div className="demo-note">{children}</div>
}

export function Field({ label, error, children }) {
  return (
    <label className="field">
      <span>{label}</span>
      {children}
      {error ? <div className="field-error">{error}</div> : null}
    </label>
  )
}

export function Switch({ checked, onChange, label }) {
  return (
    <label className="switch">
      <input type="checkbox" checked={!!checked} onChange={(e) => onChange(e.target.checked)} />
      {label ? <span>{label}</span> : null}
    </label>
  )
}

export function Chips({ options, value, onToggle, multi = true }) {
  const selected = multi ? value || [] : [value]
  return (
    <div className="chip-row">
      {options.map((o) => {
        const id = o.id ?? o
        const label = o.label ?? o
        const on = selected.includes(id)
        return (
          <button type="button" key={id} className={'chip' + (on ? ' on' : '')} onClick={() => onToggle(id)}>
            {label}
          </button>
        )
      })}
    </div>
  )
}

export function Bar({ percent, tone }) {
  const t = tone || (percent >= 80 ? 'ok' : percent >= 50 ? 'warn' : 'danger')
  return (
    <div className={'bar ' + t}>
      <i style={{ width: Math.max(0, Math.min(100, percent)) + '%' }} />
    </div>
  )
}

export function SearchInput({ value, onChange, placeholder }) {
  return (
    <input
      type="text"
      value={value}
      placeholder={placeholder || 'Search…'}
      onChange={(e) => onChange(e.target.value)}
      style={{ maxWidth: 280 }}
    />
  )
}

/* ------------------------------------------------------------------- modals */

export function Modal({ title, sub, onClose, children, footer, wide }) {
  useEffect(() => {
    const onKey = (e) => e.key === 'Escape' && onClose()
    window.addEventListener('keydown', onKey)
    return () => window.removeEventListener('keydown', onKey)
  }, [onClose])

  return (
    <div className="overlay" onClick={onClose}>
      <div className="modal" style={wide ? { width: 'min(960px, 100%)' } : undefined} onClick={(e) => e.stopPropagation()}>
        <header>
          <h3>{title}</h3>
          {sub ? <div className="sub">{sub}</div> : null}
        </header>
        <div className="body">{children}</div>
        {footer ? <footer>{footer}</footer> : null}
      </div>
    </div>
  )
}

export function Confirm({ title, message, confirmLabel = 'Confirm', danger, onConfirm, onClose }) {
  return (
    <Modal
      title={title}
      onClose={onClose}
      footer={
        <>
          <button onClick={onClose}>Cancel</button>
          <button className={danger ? 'danger' : 'primary'} onClick={onConfirm}>
            {confirmLabel}
          </button>
        </>
      }
    >
      <p style={{ margin: 0, color: 'var(--muted)' }}>{message}</p>
    </Modal>
  )
}

/** Small prompt used for rejection reasons and editor notes. */
export function ReasonDialog({ title, label, placeholder, confirmLabel, danger, onSubmit, onClose }) {
  const [text, setText] = useState('')
  return (
    <Modal
      title={title}
      onClose={onClose}
      footer={
        <>
          <button onClick={onClose}>Cancel</button>
          <button className={danger ? 'danger' : 'primary'} disabled={!text.trim()} onClick={() => onSubmit(text.trim())}>
            {confirmLabel}
          </button>
        </>
      }
    >
      <Field label={label}>
        <textarea value={text} placeholder={placeholder} onChange={(e) => setText(e.target.value)} />
      </Field>
    </Modal>
  )
}

/* -------------------------------------------------------------------- utils */

export function relativeTime(ms) {
  const diff = Date.now() - ms
  const min = Math.floor(diff / 60000)
  const hr = Math.floor(min / 60)
  const day = Math.floor(hr / 24)
  if (min < 1) return 'Just now'
  if (min < 60) return min + ' min ago'
  if (hr < 24) return hr === 1 ? '1 hour ago' : hr + ' hours ago'
  if (day < 7) return day === 1 ? 'Yesterday' : day + ' days ago'
  return new Date(ms).toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' })
}

export const clock = (seconds) =>
  String(Math.floor(seconds / 60)).padStart(2, '0') + ':' + String(seconds % 60).padStart(2, '0')
