import { useMemo, useState } from 'react'
import { useAuth, useSelectors, useStore, useToast } from '../store/store.jsx'
import {
  Chips, Confirm, DemoNote, Empty, Field, Modal, Pill, ReasonDialog, SearchInput,
  SectionHead, Stat, StatusPill, Switch, relativeTime
} from '../components/ui.jsx'
import { NEWS_STATUS } from '../data/newsData.js'
import PeopleManager, { AssignDialog } from '../components/People.jsx'

/* ============================================================ review queue */

export function ReviewQueue() {
  const { state, dispatch } = useStore()
  const s = useSelectors()
  const { notify } = useToast()
  const [openId, setOpenId] = useState(null)

  const queue = useMemo(
    () => [...s.reviewQueue].sort((a, b) => a.createdAt - b.createdAt),
    [s.reviewQueue]
  )

  const open = (article) => {
    dispatch({ type: 'article/startReview', payload: { id: article.id, headline: article.headline } })
    setOpenId(article.id)
  }

  return (
    <>
      <DemoNote>
        Opening a story moves it to <strong>Under Review</strong> — exactly as in the app. Approve &amp;
        publish makes it live in the reader feed immediately.
      </DemoNote>

      <div className="grid grid-4">
        <Stat
          label="Submitted"
          value={queue.filter((a) => a.status === NEWS_STATUS.SUBMITTED).length}
          accent="var(--warn)"
        />
        <Stat
          label="Under review"
          value={queue.filter((a) => a.status === NEWS_STATUS.UNDER_REVIEW).length}
          accent="var(--info)"
        />
        <Stat label="Approved, not live" value={s.approvedNotLive.length} accent="var(--info)" />
        <Stat label="Published today" value={s.published.length} accent="var(--ok)" />
      </div>

      <SectionHead title={queue.length + ' stories waiting'} sub="Oldest first" />
      {queue.length === 0 ? (
        <Empty title="Queue is clear" description="No reporter submissions are pending." />
      ) : (
        <div className="card table-wrap">
          <table>
            <thead>
              <tr>
                <th>Story</th>
                <th>Category</th>
                <th>Reporter</th>
                <th>Submitted</th>
                <th>Status</th>
                <th />
              </tr>
            </thead>
            <tbody>
              {queue.map((a) => (
                <tr key={a.id}>
                  <td style={{ maxWidth: 420 }}>
                    <div className="cell-title">{a.headline}</div>
                    <div className="cell-sub">{a.description}</div>
                  </td>
                  <td>{s.categoryName(a.categoryId)}</td>
                  <td>{s.userName(a.reporterId)}</td>
                  <td className="num">{relativeTime(a.createdAt)}</td>
                  <td><StatusPill status={a.status} /></td>
                  <td className="actions">
                    <button className="primary small" onClick={() => open(a)}>Review</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {openId ? (
        <ArticleReview
          article={state.articles.find((a) => a.id === openId)}
          onClose={() => setOpenId(null)}
          onDone={(msg) => {
            setOpenId(null)
            notify(msg)
          }}
        />
      ) : null}
    </>
  )
}

/* ------------------------------------------------------- editor review pane */

function ArticleReview({ article, onClose, onDone }) {
  const { state, dispatch } = useStore()
  const s = useSelectors()
  const [form, setForm] = useState({
    headline: article.headline,
    description: article.description,
    body: article.body,
    categoryId: article.categoryId,
    tagsText: (article.tags || []).join(', ')
  })
  const [ask, setAsk] = useState(null) // 'reject' | 'sendBack'

  const fields = () => ({
    headline: form.headline.trim(),
    description: form.description.trim(),
    body: form.body.trim(),
    categoryId: form.categoryId,
    tags: form.tagsText.split(',').map((t) => t.trim()).filter(Boolean)
  })

  const save = () => {
    dispatch({ type: 'article/saveEdits', payload: { id: article.id, fields: fields() } })
    onDone('Editor changes saved')
  }

  const approve = (publish) => {
    dispatch({ type: 'article/saveEdits', payload: { id: article.id, fields: fields() } })
    dispatch({ type: 'article/approve', payload: { id: article.id, publish, headline: form.headline } })
    onDone(publish ? 'Published — live in the reader feed' : 'Approved, waiting to be published')
  }

  return (
    <>
      <Modal
        wide
        title="Review story"
        sub={
          s.userName(article.reporterId) + ' · ' + relativeTime(article.createdAt) + ' · ' + article.status
        }
        onClose={onClose}
        footer={
          <>
            <button className="danger" onClick={() => setAsk('reject')}>Reject</button>
            <button onClick={() => setAsk('sendBack')}>Send back</button>
            <button onClick={save}>Save edits</button>
            <button onClick={() => approve(false)}>Approve only</button>
            <button className="primary" onClick={() => approve(true)}>Approve &amp; publish</button>
          </>
        }
      >
        {article.rejectionReason ? (
          <div className="demo-note">Previous rejection: {article.rejectionReason}</div>
        ) : null}
        {article.editorNote ? (
          <div className="demo-note">Previous editor note: {article.editorNote}</div>
        ) : null}

        <Field label="Headline">
          <input
            type="text"
            value={form.headline}
            onChange={(e) => setForm({ ...form, headline: e.target.value })}
          />
        </Field>
        <Field label="Short description">
          <textarea
            style={{ minHeight: 60 }}
            value={form.description}
            onChange={(e) => setForm({ ...form, description: e.target.value })}
          />
        </Field>
        <Field label="Body">
          <textarea
            style={{ minHeight: 200 }}
            value={form.body}
            onChange={(e) => setForm({ ...form, body: e.target.value })}
          />
        </Field>
        <div className="form-row">
          <Field label="Category">
            <select
              value={form.categoryId}
              onChange={(e) => setForm({ ...form, categoryId: e.target.value })}
            >
              {state.categories.map((c) => (
                <option key={c.id} value={c.id}>
                  {c.emoji} {c.nameEn}
                </option>
              ))}
            </select>
          </Field>
          <Field label="Tags (comma separated)">
            <input
              type="text"
              value={form.tagsText}
              onChange={(e) => setForm({ ...form, tagsText: e.target.value })}
            />
          </Field>
        </div>
      </Modal>

      {ask === 'reject' ? (
        <ReasonDialog
          title="Reject this story"
          label="Reason sent to the reporter"
          placeholder="What has to change before this can run?"
          confirmLabel="Reject"
          danger
          onClose={() => setAsk(null)}
          onSubmit={(reason) => {
            dispatch({ type: 'article/reject', payload: { id: article.id, reason, headline: form.headline } })
            setAsk(null)
            onDone('Rejected — reporter notified')
          }}
        />
      ) : null}

      {ask === 'sendBack' ? (
        <ReasonDialog
          title="Send back for changes"
          label="Note for the reporter"
          placeholder="Add a quote from the superintendent and the opening date."
          confirmLabel="Send back"
          onClose={() => setAsk(null)}
          onSubmit={(note) => {
            dispatch({ type: 'article/sendBack', payload: { id: article.id, note, headline: form.headline } })
            setAsk(null)
            onDone('Sent back — reporter can edit and resubmit')
          }}
        />
      ) : null}
    </>
  )
}

/* =========================================================== all news (admin) */

export function NewsManagement() {
  const { state, dispatch } = useStore()
  const s = useSelectors()
  const { notify } = useToast()
  const [query, setQuery] = useState('')
  const [status, setStatus] = useState('All')
  const [category, setCategory] = useState('All')
  const [remove, setRemove] = useState(null)
  const [view, setView] = useState(null)
  const [compose, setCompose] = useState(false)

  const statuses = ['All', ...Object.values(NEWS_STATUS)]

  const rows = state.articles.filter((a) => {
    const q = query.trim().toLowerCase()
    return (
      (status === 'All' || a.status === status) &&
      (category === 'All' || a.categoryId === category) &&
      (!q || a.headline.toLowerCase().includes(q) || a.description.toLowerCase().includes(q))
    )
  })

  const act = (type, article, message) => {
    dispatch({ type, payload: { id: article.id, headline: article.headline } })
    notify(message)
  }

  return (
    <>
      <div className="grid grid-4">
        <Stat label="Total articles" value={state.articles.length} />
        <Stat label="Published" value={s.published.length} accent="var(--ok)" />
        <Stat label="Breaking" value={state.articles.filter((a) => a.isBreaking).length} accent="var(--danger)" />
        <Stat label="Pinned" value={state.articles.filter((a) => a.isFeatured).length} accent="var(--info)" />
      </div>

      <SectionHead title="Filters">
        <SearchInput value={query} onChange={setQuery} placeholder="Search headlines…" />
        <button className="primary" onClick={() => setCompose(true)}>New article</button>
      </SectionHead>
      <div className="card card-pad">
        <div style={{ marginBottom: 10 }}>
          <Chips options={statuses} value={status} onToggle={setStatus} multi={false} />
        </div>
        <Chips
          options={[{ id: 'All', label: 'All categories' }, ...state.categories.map((c) => ({ id: c.id, label: c.emoji + ' ' + c.nameEn }))]}
          value={category}
          onToggle={setCategory}
          multi={false}
        />
      </div>

      <SectionHead title={rows.length + ' articles'} sub="Publish, unpublish, flag or remove" />
      {rows.length === 0 ? (
        <Empty title="Nothing matches" description="Change the filters or the search text." />
      ) : (
        <div className="card table-wrap">
          <table>
            <thead>
              <tr>
                <th>Headline</th>
                <th>Category</th>
                <th>Status</th>
                <th>Flags</th>
                <th className="num">Views</th>
                <th>Updated</th>
                <th />
              </tr>
            </thead>
            <tbody>
              {rows.map((a) => (
                <tr key={a.id}>
                  <td style={{ maxWidth: 360 }}>
                    <div className="cell-title">{a.headline}</div>
                    <div className="cell-sub">{s.userName(a.reporterId)}</div>
                  </td>
                  <td>{s.categoryName(a.categoryId)}</td>
                  <td><StatusPill status={a.status} /></td>
                  <td>
                    <div className="btn-row">
                      {a.isBreaking ? <Pill tone="danger">Breaking</Pill> : null}
                      {a.isFeatured ? <Pill tone="info">Pinned</Pill> : null}
                      {a.reports > 0 ? <Pill tone="warn">{a.reports} reports</Pill> : null}
                    </div>
                  </td>
                  <td className="num">{a.views.toLocaleString('en-IN')}</td>
                  <td className="num">{relativeTime(a.publishedAt || a.createdAt)}</td>
                  <td className="actions">
                    <div className="btn-row" style={{ justifyContent: 'flex-end' }}>
                      <button className="small ghost" onClick={() => setView(a)}>Open</button>
                      {a.status === NEWS_STATUS.APPROVED ? (
                        <button className="small primary" onClick={() => act('article/publish', a, 'Published')}>
                          Publish
                        </button>
                      ) : null}
                      {a.status === NEWS_STATUS.PUBLISHED ? (
                        <>
                          <button className="small" onClick={() => act('article/toggleBreaking', a, a.isBreaking ? 'Breaking removed' : 'Marked breaking')}>
                            {a.isBreaking ? 'Unbreak' : 'Breaking'}
                          </button>
                          <button className="small" onClick={() => act('article/toggleFeatured', a, a.isFeatured ? 'Unpinned' : 'Pinned')}>
                            {a.isFeatured ? 'Unpin' : 'Pin'}
                          </button>
                          <button className="small" onClick={() => act('article/unpublish', a, 'Unpublished')}>
                            Unpublish
                          </button>
                        </>
                      ) : null}
                      {a.reports > 0 ? (
                        <button className="small" onClick={() => act('article/clearReports', a, 'Reports cleared')}>
                          Clear reports
                        </button>
                      ) : null}
                      <button className="small danger" onClick={() => setRemove(a)}>Remove</button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {compose ? (
        <NewsArticleEditor
          onClose={() => setCompose(false)}
          onSave={(fields) => {
            dispatch({ type: 'article/create', payload: { fields } })
            setCompose(false)
            notify(
              fields.status === NEWS_STATUS.PUBLISHED
                ? 'Published — live in the reader feed'
                : 'Article created as ' + fields.status.toLowerCase()
            )
          }}
        />
      ) : null}

      {view ? (
        <Modal
          wide
          title={view.headline}
          sub={s.categoryName(view.categoryId) + ' · ' + s.userName(view.reporterId) + ' · ' + view.status}
          onClose={() => setView(null)}
          footer={<button onClick={() => setView(null)}>Close</button>}
        >
          <div className="kv"><span className="k">Description</span><span className="v">{view.description}</span></div>
          <div className="kv"><span className="k">Tags</span><span className="v">{(view.tags || []).join(', ') || '—'}</span></div>
          <div className="kv"><span className="k">Views</span><span className="v">{view.views.toLocaleString('en-IN')}</span></div>
          {view.rejectionReason ? (
            <div className="kv"><span className="k">Rejection reason</span><span className="v">{view.rejectionReason}</span></div>
          ) : null}
          {view.editorNote ? (
            <div className="kv"><span className="k">Editor note</span><span className="v">{view.editorNote}</span></div>
          ) : null}
          <p className="article-body" style={{ marginTop: 14 }}>{view.body}</p>
        </Modal>
      ) : null}

      {remove ? (
        <Confirm
          title="Remove this article?"
          message={'"' + remove.headline + '" disappears from the app immediately.'}
          confirmLabel="Remove"
          danger
          onClose={() => setRemove(null)}
          onConfirm={() => {
            act('article/remove', remove, 'Article removed')
            setRemove(null)
          }}
        />
      ) : null}
    </>
  )
}

/* ============================================================== categories */

export function Categories() {
  const { state, dispatch } = useStore()
  const s = useSelectors()
  const { notify } = useToast()
  const [edit, setEdit] = useState(null)
  const [remove, setRemove] = useState(null)

  const count = (id) => state.articles.filter((a) => a.categoryId === id).length

  return (
    <>
      <SectionHead
        title={state.categories.length + ' categories'}
        sub={state.categories.filter((c) => c.isEnabled).length + ' visible to readers'}
      >
        <button className="primary" onClick={() => setEdit({ nameEn: '', nameTe: '', emoji: '' })}>
          New category
        </button>
      </SectionHead>

      <div className="card table-wrap">
        <table>
          <thead>
            <tr>
              <th>Category</th>
              <th>Telugu</th>
              <th className="num">Articles</th>
              <th>Visible</th>
              <th />
            </tr>
          </thead>
          <tbody>
            {state.categories.map((c) => (
              <tr key={c.id}>
                <td className="cell-title">{c.emoji} {c.nameEn}</td>
                <td>{c.nameTe}</td>
                <td className="num">{count(c.id)}</td>
                <td>
                  <Switch
                    checked={c.isEnabled}
                    onChange={() => {
                      dispatch({ type: 'category/toggle', payload: { id: c.id, name: c.nameEn } })
                      notify(c.nameEn + (c.isEnabled ? ' hidden' : ' visible'))
                    }}
                  />
                </td>
                <td className="actions">
                  <button className="small ghost" onClick={() => setEdit(c)}>Edit</button>
                  <button className="small danger" onClick={() => setRemove(c)}>Delete</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {edit ? (
        <CategoryEditor
          category={edit}
          onClose={() => setEdit(null)}
          onSave={(fields) => {
            dispatch({ type: 'category/save', payload: { id: edit.id, fields } })
            setEdit(null)
            notify(edit.id ? 'Category updated' : 'Category added')
          }}
        />
      ) : null}

      {remove ? (
        <Confirm
          title={'Delete ' + remove.nameEn + '?'}
          message={
            count(remove.id) > 0
              ? count(remove.id) + ' articles still use this category. Move them first, or delete anyway.'
              : 'No articles use this category.'
          }
          confirmLabel="Delete"
          danger
          onClose={() => setRemove(null)}
          onConfirm={() => {
            dispatch({ type: 'category/delete', payload: { id: remove.id, name: remove.nameEn } })
            setRemove(null)
            notify('Category deleted')
          }}
        />
      ) : null}
    </>
  )
}

function CategoryEditor({ category, onClose, onSave }) {
  const [form, setForm] = useState({
    nameEn: category.nameEn || '',
    nameTe: category.nameTe || '',
    emoji: category.emoji || ''
  })
  const [showError, setShowError] = useState(false)
  const error = form.nameEn.trim() ? null : 'English name is required'

  return (
    <Modal
      title={category.id ? 'Edit category' : 'New category'}
      onClose={onClose}
      footer={
        <>
          <button onClick={onClose}>Cancel</button>
          <button
            className="primary"
            onClick={() => {
              setShowError(true)
              if (!error) onSave({ ...form, nameTe: form.nameTe || form.nameEn, emoji: form.emoji || '🗂️' })
            }}
          >
            Save
          </button>
        </>
      }
    >
      <Field label="Name in English *" error={showError ? error : null}>
        <input type="text" value={form.nameEn} onChange={(e) => setForm({ ...form, nameEn: e.target.value })} />
      </Field>
      <div className="form-row">
        <Field label="Name in Telugu">
          <input type="text" value={form.nameTe} onChange={(e) => setForm({ ...form, nameTe: e.target.value })} />
        </Field>
        <Field label="Icon">
          <input type="text" value={form.emoji} onChange={(e) => setForm({ ...form, emoji: e.target.value })} />
        </Field>
      </div>
    </Modal>
  )
}

/* =============================================================== reporters */

export function Reporters() {
  const { state } = useStore()
  const s = useSelectors()
  const written = (id) => state.articles.filter((a) => a.reporterId === id)
  const editorName = (id) => state.users.find((u) => u.id === id)?.name

  return (
    <PeopleManager
      role="Reporter"
      noun="Reporter"
      title="Field reporters"
      sub="Create logins, set beats, and pick the editors who review their copy"
      note="Creating a reporter captures their login, profile and assignment in one form. A reporter can report to one or two editors — set it here, or from the Editors page."
      extraStats={(people) => [
        {
          label: 'Unassigned',
          value: people.filter((u) => u.editorIds.length === 0).length,
          accent: 'var(--danger)'
        }
      ]}
      columns={[
        {
          label: 'Beats',
          render: (u) =>
            u.beatIds.length ? (
              <div className="tag-row">
                {u.beatIds.map((id) => (
                  <span className="tag" key={id}>{s.categoryName(id)}</span>
                ))}
              </div>
            ) : (
              <span className="cell-sub">No beat set</span>
            )
        },
        {
          label: 'Reporting to',
          render: (u) =>
            u.editorIds.length ? (
              <div className="tag-row">
                {u.editorIds.map((id) => (
                  <span className="tag" key={id}>{editorName(id) || 'Removed'}</span>
                ))}
              </div>
            ) : (
              <Pill tone="danger">Unassigned</Pill>
            )
        },
        { label: 'Stories', numeric: true, render: (u) => written(u.id).length },
        {
          label: 'Published',
          numeric: true,
          render: (u) => written(u.id).filter((a) => a.status === NEWS_STATUS.PUBLISHED).length
        }
      ]}
    />
  )
}

/* ================================================================= editors */

/** News editors — the queue owners. Reporters are assigned to them from here. */
export function Editors() {
  const { state } = useStore()
  const s = useSelectors()
  const [assign, setAssign] = useState(null)

  const reportersOf = (editorId) =>
    state.users.filter((u) => u.roles.includes('Reporter') && u.editorIds.includes(editorId))

  return (
    <>
      <PeopleManager
        role="Editor"
        noun="Editor"
        title="News editors"
        sub="Create logins, set the sections they run, and assign reporters to them"
        note="An editor owns sections and a set of reporters. Use Assign reporters on any row to move people between editors — a reporter may sit under up to two."
        extraStats={() => [
          { label: 'Queue waiting', value: s.reviewQueue.length, accent: 'var(--warn)' }
        ]}
        rowActions={(u) => (
          <button className="small primary" onClick={() => setAssign(u)}>Assign reporters</button>
        )}
        columns={[
          {
            label: 'Sections owned',
            render: (u) =>
              u.sectionIds.length ? (
                <div className="tag-row">
                  {u.sectionIds.map((id) => (
                    <span className="tag" key={id}>{s.categoryName(id)}</span>
                  ))}
                </div>
              ) : (
                <span className="cell-sub">No section set</span>
              )
          },
          {
            label: 'Reporters',
            render: (u) => {
              const mine = reportersOf(u.id)
              if (!mine.length) return <Pill tone="warn">None assigned</Pill>
              return (
                <>
                  <div className="cell-title">{mine.length}</div>
                  <div className="cell-sub">{mine.map((r) => r.name).join(', ')}</div>
                </>
              )
            }
          }
        ]}
      />

      {assign ? (
        <AssignDialog
          owner={assign}
          memberRole="Reporter"
          memberNoun="reporter"
          ownerNoun="editor"
          tagsLabel="Beats"
          tagsOf={(m) => m.beatIds.map(s.categoryName).join(', ')}
          onClose={() => setAssign(null)}
        />
      ) : null}
    </>
  )
}

/* -------------------------------------------------------- news data entry */

/**
 * Writing a story directly in the console. A reporter would normally submit it
 * from the app; an admin can enter one here and choose where it lands — draft,
 * into the editor queue, or straight to the reader feed.
 */
/**
 * The compose form, shared by the desk and by a reporter filing their own copy.
 *
 * `filedBy` switches it into reporter mode: the byline is fixed to that account
 * instead of being a picker, the breaking and pin switches disappear, and the
 * footer offers only "Save draft" and "Send to my editor". Publishing is not
 * hidden from a reporter as a courtesy - it is the thing an editor exists to
 * decide, and a reporter who could publish their own copy would make the review
 * queue optional.
 */
export function NewsArticleEditor({ onClose, onSave, filedBy = null }) {
  const { state } = useStore()
  const reporters = state.users.filter((u) => u.role === 'Reporter')
  const [form, setForm] = useState({
    headline: '',
    description: '',
    body: '',
    categoryId: state.categories.find((c) => c.isEnabled)?.id || state.categories[0]?.id,
    reporterId: filedBy ? filedBy.id : reporters[0]?.id,
    tagsText: '',
    isBreaking: false,
    isFeatured: false
  })
  const [showErrors, setShowErrors] = useState(false)

  const headlineError = form.headline.trim() ? null : 'Headline is required'
  const bodyError = form.body.trim() ? null : 'The story body cannot be empty'

  const save = (status) => {
    setShowErrors(true)
    if (headlineError || bodyError) return
    onSave({
      headline: form.headline.trim(),
      description: form.description.trim(),
      body: form.body.trim(),
      categoryId: form.categoryId,
      reporterId: form.reporterId,
      tags: form.tagsText.split(',').map((t) => t.trim()).filter(Boolean),
      // A reporter cannot flag their own story as breaking or pin it to the
      // feed; both are desk calls made after review.
      isBreaking: filedBy ? false : form.isBreaking,
      isFeatured: filedBy ? false : form.isFeatured,
      status
    })
  }

  return (
    <Modal
      wide
      title={filedBy ? 'File a story' : 'New article'}
      sub={
        filedBy
          ? 'Filed as ' + filedBy.name + ' — your editor reviews it before it goes live'
          : 'Enter a story and choose where it lands'
      }
      onClose={onClose}
      footer={
        <>
          <button onClick={onClose}>Cancel</button>
          <button onClick={() => save(NEWS_STATUS.DRAFT)}>Save draft</button>
          {filedBy ? (
            <button className="primary" onClick={() => save(NEWS_STATUS.SUBMITTED)}>
              Send to my editor
            </button>
          ) : (
            <>
              <button onClick={() => save(NEWS_STATUS.SUBMITTED)}>Send to review queue</button>
              <button className="primary" onClick={() => save(NEWS_STATUS.PUBLISHED)}>Publish now</button>
            </>
          )}
        </>
      }
    >
      <Field label="Headline *" error={showErrors ? headlineError : null}>
        <input
          type="text"
          value={form.headline}
          onChange={(e) => setForm({ ...form, headline: e.target.value })}
        />
      </Field>
      <Field label="Short description shown on cards">
        <textarea
          style={{ minHeight: 60 }}
          value={form.description}
          onChange={(e) => setForm({ ...form, description: e.target.value })}
        />
      </Field>
      <Field label="Story body *" error={showErrors ? bodyError : null}>
        <textarea
          style={{ minHeight: 200 }}
          value={form.body}
          onChange={(e) => setForm({ ...form, body: e.target.value })}
        />
      </Field>
      <div className="form-row">
        <Field label="Category">
          <select
            value={form.categoryId}
            onChange={(e) => setForm({ ...form, categoryId: e.target.value })}
          >
            {state.categories.map((c) => (
              <option key={c.id} value={c.id}>{c.emoji} {c.nameEn}</option>
            ))}
          </select>
        </Field>
        {filedBy ? (
          <Field label="Filed by">
            <input type="text" value={filedBy.name} disabled />
          </Field>
        ) : (
          <Field label="Credit to reporter">
            <select
              value={form.reporterId}
              onChange={(e) => setForm({ ...form, reporterId: e.target.value })}
            >
              {reporters.map((r) => (
                <option key={r.id} value={r.id}>{r.name} · {r.location}</option>
              ))}
            </select>
          </Field>
        )}
        <Field label="Tags (comma separated)">
          <input
            type="text"
            value={form.tagsText}
            onChange={(e) => setForm({ ...form, tagsText: e.target.value })}
          />
        </Field>
      </div>
      {filedBy ? null : (
        <div className="btn-row" style={{ gap: 20 }}>
          <Switch
            checked={form.isBreaking}
            onChange={(v) => setForm({ ...form, isBreaking: v })}
            label="Mark as breaking"
          />
          <Switch
            checked={form.isFeatured}
            onChange={(v) => setForm({ ...form, isFeatured: v })}
            label="Pin to the top of the feed"
          />
        </div>
      )}
    </Modal>
  )
}
