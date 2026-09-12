import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useSelectors, useStore, useToast } from '../store/store.jsx'
import {
  Bar, Chips, Confirm, DemoNote, Empty, Field, Modal, Pill, SearchInput,
  SectionHead, Stat, StatusPill, Switch, relativeTime
} from '../components/ui.jsx'
import { CONTENT_STATUS, DIFFICULTY, EXAM_GROUPS } from '../data/studyData.js'
import PeopleManager from '../components/People.jsx'

/* ============================================================== exam types */

/** This list is exactly what a student sees in the app's exam picker. */
export function ExamTypes() {
  const { state, dispatch } = useStore()
  const s = useSelectors()
  const { notify } = useToast()
  const nav = useNavigate()
  const [edit, setEdit] = useState(null)
  const [remove, setRemove] = useState(null)
  const [query, setQuery] = useState('')

  const rows = state.tracks.filter((t) => {
    const q = query.trim().toLowerCase()
    return !q || t.nameEn.toLowerCase().includes(q) || t.shortName.toLowerCase().includes(q) || t.group.toLowerCase().includes(q)
  })

  const topicCount = (track) =>
    track.sections.reduce((n, sec) => n + s.topicsOf(sec.subjectId).length, 0)

  return (
    <>
      <DemoNote>
        A student picks one of these before any syllabus is shown. Hiding an exam type removes it from the
        picker; the pattern below drives their subject list, tests and ranks.
      </DemoNote>

      <div className="grid grid-4">
        <Stat label="Exam types" value={state.tracks.length} />
        <Stat
          label="Visible to students"
          value={state.tracks.filter((t) => t.isEnabled).length}
          accent="var(--ok)"
        />
        <Stat label="Hidden" value={state.tracks.filter((t) => !t.isEnabled).length} accent="var(--muted)" />
        <Stat
          label="Papers tagged"
          value={state.exams.filter((e) => e.trackIds.length > 0).length}
          caption={state.exams.filter((e) => e.trackIds.length === 0).length + ' general papers'}
          accent="var(--info)"
        />
      </div>

      <SectionHead title={rows.length + ' exam types'} sub="Grouped as the student picker groups them">
        <SearchInput value={query} onChange={setQuery} placeholder="Search exams…" />
        <button className="primary" onClick={() => setEdit({ sections: [] })}>New exam type</button>
      </SectionHead>

      {rows.length === 0 ? (
        <Empty
          title="No exam types"
          description="Students cannot start studying until at least one exists."
          actionLabel="New exam type"
          onAction={() => setEdit({ sections: [] })}
        />
      ) : (
        EXAM_GROUPS.map((group) => {
          const list = rows.filter((t) => t.group === group)
          if (!list.length) return null
          return (
            <div key={group} style={{ marginBottom: 16 }}>
              <SectionHead title={group} sub={list.length + ' exams'} />
              <div className="card table-wrap">
                <table>
                  <thead>
                    <tr>
                      <th>Exam</th>
                      <th>Pattern</th>
                      <th>Subjects</th>
                      <th className="num">Papers</th>
                      <th>Visible</th>
                      <th />
                    </tr>
                  </thead>
                  <tbody>
                    {list.map((t) => (
                      <tr key={t.id}>
                        <td style={{ maxWidth: 280 }}>
                          <div className="cell-title">{t.emoji} {t.nameEn}</div>
                          <div className="cell-sub">{t.nameTe} · {t.tagline}</div>
                        </td>
                        <td>
                          <div className="cell-title num">
                            {t.totalQuestions} Q · {t.totalMarks} marks · {t.durationMinutes} min
                          </div>
                          <div className="cell-sub">{t.negativeMarking}</div>
                        </td>
                        <td>
                          <div className="cell-title num">{t.sections.length} subjects</div>
                          <div className="cell-sub">{topicCount(t)} topics in scope</div>
                        </td>
                        <td className="num">{s.papersTaggedTo(t.id).length}</td>
                        <td>
                          <Switch
                            checked={t.isEnabled}
                            onChange={() => {
                              dispatch({ type: 'track/toggle', payload: { id: t.id, name: t.nameEn } })
                              notify(t.nameEn + (t.isEnabled ? ' hidden from students' : ' visible to students'))
                            }}
                          />
                        </td>
                        <td className="actions">
                          <button
                            className="small primary"
                            onClick={() => nav('/study/exam-type/' + t.id)}
                          >
                            Syllabus
                          </button>
                          <button className="small ghost" onClick={() => setEdit(t)}>Edit</button>
                          <button className="small danger" onClick={() => setRemove(t)}>Delete</button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          )
        })
      )}

      {edit ? (
        <ExamTypeEditor
          track={edit}
          onClose={() => setEdit(null)}
          onSave={(fields) => {
            dispatch({ type: 'track/save', payload: { id: edit.id, fields } })
            setEdit(null)
            notify(edit.id ? 'Exam type updated' : 'Exam type added — students see it now')
          }}
        />
      ) : null}

      {remove ? (
        <Confirm
          title={'Delete ' + remove.nameEn + '?'}
          message={
            s.papersTaggedTo(remove.id).length > 0
              ? s.papersTaggedTo(remove.id).length +
                ' papers are still tagged with this exam type. Untag or delete them first.'
              : 'Students preparing for it are returned to the exam picker.'
          }
          confirmLabel="Delete"
          danger
          onClose={() => setRemove(null)}
          onConfirm={() => {
            if (s.papersTaggedTo(remove.id).length > 0) {
              notify('Delete the papers tagged with this exam first')
              setRemove(null)
              return
            }
            dispatch({ type: 'track/delete', payload: { id: remove.id, name: remove.nameEn } })
            setRemove(null)
            notify('Exam type deleted')
          }}
        />
      ) : null}
    </>
  )
}

function ExamTypeEditor({ track, onClose, onSave }) {
  const { state } = useStore()
  const [form, setForm] = useState({
    nameEn: track.nameEn || '',
    nameTe: track.nameTe || '',
    shortName: track.shortName || '',
    emoji: track.emoji || '🎯',
    group: track.group || EXAM_GROUPS[0],
    tagline: track.tagline || '',
    qualification: track.qualification || '',
    ageLimit: track.ageLimit || '',
    vacancyLabel: track.vacancyLabel || '',
    examDateLabel: track.examDateLabel || '',
    durationMinutes: track.durationMinutes || 60,
    negativeMarking: track.negativeMarking || 'No negative marking',
    stagesText: (track.stages || []).join(', '),
    sections: track.sections || [],
    isEnabled: track.isEnabled !== false
  })
  const [showErrors, setShowErrors] = useState(false)

  const totalQ = form.sections.reduce((n, x) => n + Number(x.questions || 0), 0)
  const totalM = form.sections.reduce((n, x) => n + Number(x.marks || 0), 0)
  const nameError = form.nameEn.trim() ? null : 'Exam name is required'
  const durationError = Number(form.durationMinutes) > 0 ? null : 'Duration must be a positive number'
  const sectionError = form.sections.length === 0
    ? 'Add at least one subject to the pattern'
    : totalQ <= 0 ? 'The pattern needs at least one question' : null

  const toggleSubject = (subjectId) => {
    setForm((f) => ({
      ...f,
      sections: f.sections.some((x) => x.subjectId === subjectId)
        ? f.sections.filter((x) => x.subjectId !== subjectId)
        : [...f.sections, { subjectId, questions: 10, marks: 10 }]
    }))
  }

  const setSection = (subjectId, key, value) => {
    setForm((f) => ({
      ...f,
      sections: f.sections.map((x) => (x.subjectId === subjectId ? { ...x, [key]: Number(value) || 0 } : x))
    }))
  }

  return (
    <Modal
      wide
      title={track.id ? 'Edit exam type' : 'New exam type'}
      sub="Everything here is what the student sees in the exam picker and hub"
      onClose={onClose}
      footer={
        <>
          <button onClick={onClose}>Cancel</button>
          <button
            className="primary"
            onClick={() => {
              setShowErrors(true)
              if (nameError || durationError || sectionError) return
              onSave({
                ...form,
                durationMinutes: Number(form.durationMinutes),
                nameTe: form.nameTe || form.nameEn,
                shortName: form.shortName || form.nameEn.slice(0, 12),
                stages: form.stagesText.split(',').map((x) => x.trim()).filter(Boolean)
              })
            }}
          >
            Save exam type
          </button>
        </>
      }
    >
      <SectionHead title="Identity" />
      <Field label="Exam name in English *" error={showErrors ? nameError : null}>
        <input type="text" value={form.nameEn} onChange={(e) => setForm({ ...form, nameEn: e.target.value })} />
      </Field>
      <div className="form-row">
        <Field label="Name in Telugu">
          <input type="text" value={form.nameTe} onChange={(e) => setForm({ ...form, nameTe: e.target.value })} />
        </Field>
        <Field label="Short name">
          <input type="text" value={form.shortName} onChange={(e) => setForm({ ...form, shortName: e.target.value })} />
        </Field>
        <Field label="Icon">
          <input type="text" value={form.emoji} onChange={(e) => setForm({ ...form, emoji: e.target.value })} />
        </Field>
      </div>
      <Field label="One-line description">
        <input type="text" value={form.tagline} onChange={(e) => setForm({ ...form, tagline: e.target.value })} />
      </Field>
      <Field label="Category in the picker">
        <Chips options={EXAM_GROUPS} value={form.group} onToggle={(g) => setForm({ ...form, group: g })} multi={false} />
      </Field>

      <SectionHead title="Notification details" />
      <div className="form-row">
        <Field label="Qualification">
          <input type="text" value={form.qualification} onChange={(e) => setForm({ ...form, qualification: e.target.value })} />
        </Field>
        <Field label="Age limit">
          <input type="text" value={form.ageLimit} onChange={(e) => setForm({ ...form, ageLimit: e.target.value })} />
        </Field>
        <Field label="Vacancies">
          <input type="text" value={form.vacancyLabel} onChange={(e) => setForm({ ...form, vacancyLabel: e.target.value })} />
        </Field>
      </div>
      <div className="form-row">
        <Field label="Exam date label">
          <input type="text" value={form.examDateLabel} onChange={(e) => setForm({ ...form, examDateLabel: e.target.value })} />
        </Field>
        <Field label="Selection stages (comma separated)">
          <input type="text" value={form.stagesText} onChange={(e) => setForm({ ...form, stagesText: e.target.value })} />
        </Field>
      </div>

      <SectionHead title="Paper rules" />
      <div className="form-row">
        <Field label="Duration in minutes *" error={showErrors ? durationError : null}>
          <input
            type="number"
            value={form.durationMinutes}
            onChange={(e) => setForm({ ...form, durationMinutes: e.target.value })}
          />
        </Field>
        <Field label="Negative marking">
          <input
            type="text"
            value={form.negativeMarking}
            onChange={(e) => setForm({ ...form, negativeMarking: e.target.value })}
          />
        </Field>
      </div>

      <SectionHead
        title="Exam pattern — subject wise"
        sub={totalQ + ' questions · ' + totalM + ' marks · ' + form.sections.length + ' subjects'}
      />
      {showErrors && sectionError ? <div className="field-error" style={{ marginBottom: 8 }}>{sectionError}</div> : null}
      <div className="table-wrap card">
        <table>
          <thead>
            <tr>
              <th>Subject</th>
              <th>On this paper</th>
              <th>Questions</th>
              <th>Marks</th>
            </tr>
          </thead>
          <tbody>
            {state.subjects.filter((sub) => sub.isEnabled).map((sub) => {
              const section = form.sections.find((x) => x.subjectId === sub.id)
              return (
                <tr key={sub.id}>
                  <td className="cell-title">{sub.emoji} {sub.nameEn}</td>
                  <td>
                    <Switch checked={!!section} onChange={() => toggleSubject(sub.id)} />
                  </td>
                  <td>
                    <input
                      type="number"
                      style={{ width: 90 }}
                      disabled={!section}
                      value={section ? section.questions : ''}
                      onChange={(e) => setSection(sub.id, 'questions', e.target.value)}
                    />
                  </td>
                  <td>
                    <input
                      type="number"
                      style={{ width: 90 }}
                      disabled={!section}
                      value={section ? section.marks : ''}
                      onChange={(e) => setSection(sub.id, 'marks', e.target.value)}
                    />
                  </td>
                </tr>
              )
            })}
          </tbody>
        </table>
      </div>

      <div style={{ marginTop: 16 }}>
        <Switch
          checked={form.isEnabled}
          onChange={(v) => setForm({ ...form, isEnabled: v })}
          label="Visible to students in the exam picker"
        />
      </div>
    </Modal>
  )
}

/* ================================================================ subjects */

export function Subjects() {
  const { state, dispatch } = useStore()
  const s = useSelectors()
  const { notify } = useToast()
  const [edit, setEdit] = useState(null)
  const [remove, setRemove] = useState(null)

  return (
    <>
      <SectionHead
        title={state.subjects.length + ' subjects'}
        sub={state.topics.length + ' topics across them'}
      >
        <button className="primary" onClick={() => setEdit({})}>New subject</button>
      </SectionHead>

      <div className="card table-wrap">
        <table>
          <thead>
            <tr>
              <th>Subject</th>
              <th className="num">Topics</th>
              <th className="num">Articles</th>
              <th className="num">Questions</th>
              <th>Used by exams</th>
              <th>Student accuracy</th>
              <th>Enabled</th>
              <th />
            </tr>
          </thead>
          <tbody>
            {state.subjects.map((sub) => {
              const accuracy = s.subjectAccuracy(sub.id)
              const usedBy = state.tracks.filter((t) => t.sections.some((x) => x.subjectId === sub.id))
              return (
                <tr key={sub.id}>
                  <td>
                    <div className="cell-title">{sub.emoji} {sub.nameEn}</div>
                    <div className="cell-sub">{sub.nameTe}</div>
                  </td>
                  <td className="num">{s.topicsOf(sub.id).length}</td>
                  <td className="num">{s.articlesOfSubject(sub.id).length}</td>
                  <td className="num">{s.questionsOfSubject(sub.id).length}</td>
                  <td>
                    <div className="cell-sub">
                      {usedBy.length ? usedBy.slice(0, 3).map((t) => t.shortName).join(', ') : '—'}
                      {usedBy.length > 3 ? ' +' + (usedBy.length - 3) : ''}
                    </div>
                  </td>
                  <td>
                    {accuracy === null ? (
                      <span className="cell-sub">Not attempted</span>
                    ) : (
                      <>
                        <Bar percent={accuracy} />
                        <div className="cell-sub">{accuracy}% · {s.band(accuracy)}</div>
                      </>
                    )}
                  </td>
                  <td>
                    <Switch
                      checked={sub.isEnabled}
                      onChange={() => {
                        dispatch({ type: 'subject/toggle', payload: { id: sub.id, name: sub.nameEn } })
                        notify(sub.nameEn + (sub.isEnabled ? ' disabled' : ' enabled'))
                      }}
                    />
                  </td>
                  <td className="actions">
                    <button className="small ghost" onClick={() => setEdit(sub)}>Edit</button>
                    <button className="small danger" onClick={() => setRemove(sub)}>Delete</button>
                  </td>
                </tr>
              )
            })}
          </tbody>
        </table>
      </div>

      {edit ? (
        <SubjectEditor
          subject={edit}
          onClose={() => setEdit(null)}
          onSave={(fields) => {
            dispatch({ type: 'subject/save', payload: { id: edit.id, fields } })
            setEdit(null)
            notify(edit.id ? 'Subject updated' : 'Subject added')
          }}
        />
      ) : null}

      {remove ? (
        <Confirm
          title={'Delete ' + remove.nameEn + '?'}
          message={
            s.topicsOf(remove.id).length > 0
              ? s.topicsOf(remove.id).length + ' topics still belong to this subject. Delete them first.'
              : 'No topics belong to this subject.'
          }
          confirmLabel="Delete"
          danger
          onClose={() => setRemove(null)}
          onConfirm={() => {
            if (s.topicsOf(remove.id).length > 0) {
              notify('Delete the topics under this subject first')
              setRemove(null)
              return
            }
            dispatch({ type: 'subject/delete', payload: { id: remove.id, name: remove.nameEn } })
            setRemove(null)
            notify('Subject deleted')
          }}
        />
      ) : null}
    </>
  )
}

function SubjectEditor({ subject, onClose, onSave }) {
  const [form, setForm] = useState({
    nameEn: subject.nameEn || '',
    nameTe: subject.nameTe || '',
    emoji: subject.emoji || '📘'
  })
  const [showErrors, setShowErrors] = useState(false)
  const error = form.nameEn.trim() ? null : 'English name is required'

  return (
    <Modal
      title={subject.id ? 'Edit subject' : 'New subject'}
      onClose={onClose}
      footer={
        <>
          <button onClick={onClose}>Cancel</button>
          <button
            className="primary"
            onClick={() => {
              setShowErrors(true)
              if (!error) {
                onSave({ ...form, nameTe: form.nameTe || form.nameEn, emoji: form.emoji || '📘' })
              }
            }}
          >
            Save
          </button>
        </>
      }
    >
      <Field label="Name in English *" error={showErrors ? error : null}>
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

/* ================================================================== topics */

export function Topics() {
  const { state, dispatch } = useStore()
  const s = useSelectors()
  const { notify } = useToast()
  const nav = useNavigate()
  const [subjectFilter, setSubjectFilter] = useState('All')
  const [edit, setEdit] = useState(null)
  const [remove, setRemove] = useState(null)

  const rows = state.topics
    .filter((t) => subjectFilter === 'All' || t.subjectId === subjectFilter)
    .sort((a, b) => a.subjectId.localeCompare(b.subjectId) || a.order - b.order)

  const usage = (topicId) => ({
    articles: state.studyArticles.filter((a) => a.topicId === topicId).length,
    questions: state.questions.filter((q) => q.topicId === topicId).length
  })

  return (
    <>
      <SectionHead title={rows.length + ' topics'} sub="Chapters students study inside each subject">
        <button className="primary" onClick={() => setEdit({ subjectId: state.subjects[0]?.id })}>
          New topic
        </button>
      </SectionHead>

      <div className="card card-pad" style={{ marginBottom: 14 }}>
        <Chips
          options={[
            { id: 'All', label: 'All subjects' },
            ...state.subjects.map((sub) => ({ id: sub.id, label: sub.emoji + ' ' + sub.nameEn }))
          ]}
          value={subjectFilter}
          onToggle={setSubjectFilter}
          multi={false}
        />
      </div>

      <div className="card table-wrap">
        <table>
          <thead>
            <tr>
              <th>Topic</th>
              <th>Subject</th>
              <th>Difficulty</th>
              <th className="num">Articles</th>
              <th className="num">Questions</th>
              <th>Enabled</th>
              <th />
            </tr>
          </thead>
          <tbody>
            {rows.map((t) => {
              const u = usage(t.id)
              return (
                <tr key={t.id}>
                  <td>
                    <div className="cell-title">{t.nameEn}</div>
                    <div className="cell-sub">{t.nameTe}</div>
                  </td>
                  <td>{s.subjectEmoji(t.subjectId)} {s.subjectName(t.subjectId)}</td>
                  <td>
                    <Pill tone={t.difficulty === 'Hard' ? 'danger' : t.difficulty === 'Medium' ? 'warn' : 'ok'}>
                      {t.difficulty}
                    </Pill>
                  </td>
                  <td className="num">
                    {u.articles ? u.articles : <Pill tone="danger">none</Pill>}
                  </td>
                  <td className="num">
                    {u.questions ? u.questions : <Pill tone="danger">none</Pill>}
                  </td>
                  <td>
                    <Switch
                      checked={t.isEnabled}
                      onChange={() => {
                        dispatch({ type: 'topic/toggle', payload: { id: t.id, name: t.nameEn } })
                        notify(t.nameEn + (t.isEnabled ? ' disabled' : ' enabled'))
                      }}
                    />
                  </td>
                  <td className="actions">
                    <button className="small primary" onClick={() => nav('/study/topic/' + t.id)}>
                      Open
                    </button>
                    <button className="small ghost" onClick={() => setEdit(t)}>Edit</button>
                    <button className="small danger" onClick={() => setRemove(t)}>Delete</button>
                  </td>
                </tr>
              )
            })}
          </tbody>
        </table>
      </div>

      {edit ? (
        <TopicEditor
          topic={edit}
          onClose={() => setEdit(null)}
          onSave={(fields) => {
            dispatch({ type: 'topic/save', payload: { id: edit.id, fields } })
            setEdit(null)
            notify(edit.id ? 'Topic updated' : 'Topic added')
          }}
        />
      ) : null}

      {remove ? (
        <Confirm
          title={'Delete ' + remove.nameEn + '?'}
          message={
            usage(remove.id).articles + usage(remove.id).questions > 0
              ? 'Articles or questions still reference this topic. Remove them first.'
              : 'Nothing references this topic.'
          }
          confirmLabel="Delete"
          danger
          onClose={() => setRemove(null)}
          onConfirm={() => {
            const u = usage(remove.id)
            if (u.articles + u.questions > 0) {
              notify('Content still references this topic')
              setRemove(null)
              return
            }
            dispatch({ type: 'topic/delete', payload: { id: remove.id, name: remove.nameEn } })
            setRemove(null)
            notify('Topic deleted')
          }}
        />
      ) : null}
    </>
  )
}

export function TopicEditor({ topic, onClose, onSave }) {
  const { state } = useStore()
  const [form, setForm] = useState({
    nameEn: topic.nameEn || '',
    nameTe: topic.nameTe || '',
    subjectId: topic.subjectId || state.subjects[0]?.id,
    difficulty: topic.difficulty || 'Medium'
  })
  const [showErrors, setShowErrors] = useState(false)
  const error = form.nameEn.trim() ? null : 'Topic name is required'

  return (
    <Modal
      title={topic.id ? 'Edit topic' : 'New topic'}
      onClose={onClose}
      footer={
        <>
          <button onClick={onClose}>Cancel</button>
          <button
            className="primary"
            onClick={() => {
              setShowErrors(true)
              if (!error) onSave({ ...form, nameTe: form.nameTe || form.nameEn })
            }}
          >
            Save
          </button>
        </>
      }
    >
      <Field label="Topic name in English *" error={showErrors ? error : null}>
        <input type="text" value={form.nameEn} onChange={(e) => setForm({ ...form, nameEn: e.target.value })} />
      </Field>
      <Field label="Name in Telugu">
        <input type="text" value={form.nameTe} onChange={(e) => setForm({ ...form, nameTe: e.target.value })} />
      </Field>
      <div className="form-row">
        <Field label="Subject">
          <select value={form.subjectId} onChange={(e) => setForm({ ...form, subjectId: e.target.value })}>
            {state.subjects.map((sub) => (
              <option key={sub.id} value={sub.id}>{sub.emoji} {sub.nameEn}</option>
            ))}
          </select>
        </Field>
        <Field label="Difficulty">
          <select value={form.difficulty} onChange={(e) => setForm({ ...form, difficulty: e.target.value })}>
            {DIFFICULTY.map((d) => <option key={d} value={d}>{d}</option>)}
          </select>
        </Field>
      </div>
    </Modal>
  )
}

/* ================================================================= content */

export function StudyContent() {
  const { state, dispatch } = useStore()
  const s = useSelectors()
  const { notify } = useToast()
  const [status, setStatus] = useState('All')
  const [view, setView] = useState(null)
  const [edit, setEdit] = useState(null)
  const [remove, setRemove] = useState(null)

  const rows = state.studyArticles
    .filter((a) => status === 'All' || a.status === status)
    .sort((a, b) => b.createdAt - a.createdAt)

  const act = (article, next, message) => {
    dispatch({ type: 'studyArticle/setStatus', payload: { id: article.id, status: next, title: article.title } })
    notify(message)
  }

  return (
    <>
      <div className="grid grid-4">
        <Stat label="Study articles" value={state.studyArticles.length} />
        <Stat
          label="Published"
          value={state.studyArticles.filter((a) => a.status === CONTENT_STATUS.PUBLISHED).length}
          accent="var(--ok)"
        />
        <Stat
          label="Awaiting review"
          value={state.studyArticles.filter((a) => a.status === CONTENT_STATUS.PENDING_REVIEW).length}
          accent="var(--warn)"
        />
        <Stat
          label="Drafts"
          value={state.studyArticles.filter((a) => a.status === CONTENT_STATUS.DRAFT).length}
          accent="var(--muted)"
        />
      </div>

      <SectionHead title="Study material" sub="Write it here, or review what creators submitted">
        <button className="primary" onClick={() => setEdit({})}>New study article</button>
      </SectionHead>
      <div className="card card-pad" style={{ marginBottom: 14 }}>
        <Chips options={['All', ...Object.values(CONTENT_STATUS)]} value={status} onToggle={setStatus} multi={false} />
      </div>

      {rows.length === 0 ? (
        <Empty
          title="Nothing here"
          description="No article has that status. Write one to fill the syllabus."
          actionLabel="New study article"
          onAction={() => setEdit({})}
        />
      ) : (
        <div className="card table-wrap">
          <table>
            <thead>
              <tr>
                <th>Article</th>
                <th>Subject / topic</th>
                <th>Author</th>
                <th>Status</th>
                <th>Created</th>
                <th />
              </tr>
            </thead>
            <tbody>
              {rows.map((a) => (
                <tr key={a.id}>
                  <td style={{ maxWidth: 320 }}>
                    <div className="cell-title">{a.title}</div>
                    <div className="cell-sub">{a.readingMinutes} min read</div>
                  </td>
                  <td>
                    <div className="cell-title">{s.subjectName(a.subjectId)}</div>
                    <div className="cell-sub">{s.topicName(a.topicId)}</div>
                  </td>
                  <td>{a.authorName}</td>
                  <td><StatusPill status={a.status} /></td>
                  <td className="num">{relativeTime(a.createdAt)}</td>
                  <td className="actions">
                    <div className="btn-row" style={{ justifyContent: 'flex-end' }}>
                      <button className="small ghost" onClick={() => setView(a)}>Open</button>
                      <button className="small ghost" onClick={() => setEdit(a)}>Edit</button>
                      {a.status !== CONTENT_STATUS.PUBLISHED ? (
                        <button
                          className="small primary"
                          onClick={() => act(a, CONTENT_STATUS.PUBLISHED, 'Published to students')}
                        >
                          Publish
                        </button>
                      ) : null}
                      {a.status !== CONTENT_STATUS.DRAFT ? (
                        <button className="small" onClick={() => act(a, CONTENT_STATUS.DRAFT, 'Sent back to draft')}>
                          Send back
                        </button>
                      ) : null}
                      <button className="small danger" onClick={() => setRemove(a)}>Delete</button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {edit ? (
        <StudyArticleEditor
          article={edit}
          onClose={() => setEdit(null)}
          onSave={(fields) => {
            dispatch({ type: 'studyArticle/save', payload: { id: edit.id, fields } })
            setEdit(null)
            notify(
              edit.id
                ? 'Study article saved'
                : fields.status === CONTENT_STATUS.PUBLISHED
                  ? 'Published to students'
                  : 'Saved as ' + fields.status.toLowerCase()
            )
          }}
        />
      ) : null}

      {view ? (
        <Modal
          wide
          title={view.title}
          sub={s.subjectName(view.subjectId) + ' · ' + s.topicName(view.topicId) + ' · ' + view.authorName}
          onClose={() => setView(null)}
          footer={<button onClick={() => setView(null)}>Close</button>}
        >
          <p style={{ color: 'var(--muted)', marginTop: 0 }}>{view.description}</p>
          <p className="article-body">{view.content}</p>
          {view.importantPoints?.length ? (
            <>
              <SectionHead title="Important points" />
              <ul>{view.importantPoints.map((p, i) => <li key={i}>{p}</li>)}</ul>
            </>
          ) : null}
          {view.formulas?.length ? (
            <>
              <SectionHead title="Formulas" />
              <ul>{view.formulas.map((p, i) => <li key={i}>{p}</li>)}</ul>
            </>
          ) : null}
          {view.examples?.length ? (
            <>
              <SectionHead title="Examples" />
              <ul>{view.examples.map((p, i) => <li key={i}>{p}</li>)}</ul>
            </>
          ) : null}
        </Modal>
      ) : null}

      {remove ? (
        <Confirm
          title="Delete this article?"
          message={'"' + remove.title + '" is removed from the student app.'}
          confirmLabel="Delete"
          danger
          onClose={() => setRemove(null)}
          onConfirm={() => {
            dispatch({ type: 'studyArticle/delete', payload: { id: remove.id, title: remove.title } })
            setRemove(null)
            notify('Study article deleted')
          }}
        />
      ) : null}
    </>
  )
}

/* ------------------------------------------------- study material data entry */

/**
 * Writing screen for study material: the whole article a student reads, with
 * its important points, formulas and examples. Save as a draft, send it for
 * review, or publish straight to students.
 */
export function StudyArticleEditor({ article, onClose, onSave }) {
  const { state } = useStore()
  const s = useSelectors()
  const [form, setForm] = useState({
    title: article.title || '',
    subjectId: article.subjectId || state.subjects[0]?.id,
    topicId: article.topicId || '',
    description: article.description || '',
    content: article.content || '',
    pointsText: (article.importantPoints || []).join('\n'),
    formulasText: (article.formulas || []).join('\n'),
    examplesText: (article.examples || []).join('\n'),
    readingMinutes: article.readingMinutes || 5,
    authorName: article.authorName || 'Ravi Teja Sharma',
    status: article.status || CONTENT_STATUS.DRAFT
  })
  const [showErrors, setShowErrors] = useState(false)

  const topics = s.topicsOf(form.subjectId)
  const titleError = form.title.trim() ? null : 'Title is required'
  const topicError = form.topicId ? null : 'Pick the topic this belongs to'
  const bodyError = form.content.trim() ? null : 'The article body cannot be empty'

  const lines = (text) => text.split('\n').map((x) => x.trim()).filter(Boolean)

  const save = (status) => {
    setShowErrors(true)
    if (titleError || topicError || bodyError) return
    onSave({
      title: form.title.trim(),
      subjectId: form.subjectId,
      topicId: form.topicId,
      description: form.description.trim(),
      content: form.content.trim(),
      importantPoints: lines(form.pointsText),
      formulas: lines(form.formulasText),
      examples: lines(form.examplesText),
      readingMinutes: Number(form.readingMinutes) || 5,
      authorName: form.authorName.trim() || 'J Voice Desk',
      status
    })
  }

  return (
    <Modal
      wide
      title={article.id ? 'Edit study article' : 'New study article'}
      sub="What a student reads inside the topic"
      onClose={onClose}
      footer={
        <>
          <button onClick={onClose}>Cancel</button>
          <button onClick={() => save(CONTENT_STATUS.DRAFT)}>Save draft</button>
          <button onClick={() => save(CONTENT_STATUS.PENDING_REVIEW)}>Send for review</button>
          <button className="primary" onClick={() => save(CONTENT_STATUS.PUBLISHED)}>Publish</button>
        </>
      }
    >
      <Field label="Title *" error={showErrors ? titleError : null}>
        <input type="text" value={form.title} onChange={(e) => setForm({ ...form, title: e.target.value })} />
      </Field>

      <div className="form-row">
        <Field label="Subject">
          <select
            value={form.subjectId}
            onChange={(e) => setForm({ ...form, subjectId: e.target.value, topicId: '' })}
          >
            {state.subjects.filter((sub) => sub.isEnabled).map((sub) => (
              <option key={sub.id} value={sub.id}>{sub.emoji} {sub.nameEn}</option>
            ))}
          </select>
        </Field>
        <Field label="Topic *" error={showErrors ? topicError : null}>
          <select value={form.topicId} onChange={(e) => setForm({ ...form, topicId: e.target.value })}>
            <option value="">Select a topic</option>
            {topics.map((t) => (
              <option key={t.id} value={t.id}>{t.nameEn}</option>
            ))}
          </select>
        </Field>
        <Field label="Reading time (min)">
          <input
            type="number"
            value={form.readingMinutes}
            onChange={(e) => setForm({ ...form, readingMinutes: e.target.value })}
          />
        </Field>
      </div>

      {topics.length === 0 ? (
        <div className="demo-note">
          That subject has no topics yet. Add one under <strong>Topics</strong> first — every article
          belongs to a topic.
        </div>
      ) : null}

      <Field label="Short description shown in lists">
        <textarea
          style={{ minHeight: 60 }}
          value={form.description}
          onChange={(e) => setForm({ ...form, description: e.target.value })}
        />
      </Field>

      <Field label="Article body *" error={showErrors ? bodyError : null}>
        <textarea
          style={{ minHeight: 220 }}
          value={form.content}
          onChange={(e) => setForm({ ...form, content: e.target.value })}
        />
      </Field>

      <SectionHead title="Study aids" sub="One per line — each becomes a bullet in the app" />
      <Field label="Important points">
        <textarea
          style={{ minHeight: 90 }}
          value={form.pointsText}
          placeholder={'Key definition and formula\nCommon exam trap\nPrevious-year pattern'}
          onChange={(e) => setForm({ ...form, pointsText: e.target.value })}
        />
      </Field>
      <div className="form-row">
        <Field label="Formulas">
          <textarea
            style={{ minHeight: 80 }}
            value={form.formulasText}
            placeholder={'Profit % = (Profit / CP) × 100'}
            onChange={(e) => setForm({ ...form, formulasText: e.target.value })}
          />
        </Field>
        <Field label="Solved examples">
          <textarea
            style={{ minHeight: 80 }}
            value={form.examplesText}
            placeholder={'CP 500, SP 575 → profit 15%'}
            onChange={(e) => setForm({ ...form, examplesText: e.target.value })}
          />
        </Field>
      </div>

      <Field label="Author">
        <input
          type="text"
          value={form.authorName}
          onChange={(e) => setForm({ ...form, authorName: e.target.value })}
        />
      </Field>

      <div className="demo-note">
        Current status: <strong>{form.status}</strong>. Only <strong>Published</strong> articles are
        visible to students.
      </div>
    </Modal>
  )
}

/* ======================================================== content creators */

/**
 * The study-side authors. Same login-creation form the news roles get, with
 * subjects and exam types as the assignment step instead of beats and editors.
 */
export function ContentCreators() {
  const { state } = useStore()
  const s = useSelectors()
  const mine = (name) => state.studyArticles.filter((a) => a.authorName === name)

  return (
    <PeopleManager
      role="Content Creator"
      noun="Content creator"
      title="Study content creators"
      sub="The one study role — create logins and assign the subjects they own"
      note="Content creators own the study side. They write material and questions for the subjects assigned here and publish them — there is no separate study review role. Unassigned creators have nothing to work on."
      extraStats={(people) => [
        {
          label: 'Unassigned',
          value: people.filter((u) => u.subjectIds.length === 0).length,
          accent: 'var(--danger)'
        }
      ]}
      columns={[
        {
          label: 'Subjects',
          render: (u) =>
            u.subjectIds.length ? (
              <div className="tag-row">
                {u.subjectIds.map((id) => (
                  <span className="tag" key={id}>
                    {s.subjectEmoji(id)} {s.subjectName(id)}
                  </span>
                ))}
              </div>
            ) : (
              <Pill tone="danger">Unassigned</Pill>
            )
        },
        {
          label: 'Exam types',
          render: (u) =>
            u.trackIds.length ? (
              <div className="tag-row">
                {u.trackIds.map((id) => (
                  <span className="tag" key={id}>{s.trackName(id)}</span>
                ))}
              </div>
            ) : (
              <span className="cell-sub">All exam types</span>
            )
        },
        { label: 'Articles', numeric: true, render: (u) => mine(u.name).length },
        {
          label: 'Published',
          numeric: true,
          render: (u) => mine(u.name).filter((a) => a.status === CONTENT_STATUS.PUBLISHED).length
        }
      ]}
    />
  )
}
