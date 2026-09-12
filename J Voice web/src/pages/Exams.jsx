import { useState } from 'react'
import { useSelectors, useStore, useToast } from '../store/store.jsx'
import {
  Bar, Chips, Confirm, DemoNote, Empty, Field, Modal, Pill, SearchInput,
  SectionHead, Stat, StatusPill, Switch, clock, relativeTime
} from '../components/ui.jsx'
import { CONTENT_STATUS, DIFFICULTY, QUESTION_SOURCE } from '../data/studyData.js'

/* =========================================================== question bank */

export function QuestionBank() {
  const { state, dispatch } = useStore()
  const s = useSelectors()
  const { notify } = useToast()
  const [subject, setSubject] = useState('All')
  const [topic, setTopic] = useState('All')
  const [difficulty, setDifficulty] = useState('All')
  const [query, setQuery] = useState('')
  const [edit, setEdit] = useState(null)
  const [remove, setRemove] = useState(null)

  const rows = state.questions.filter((q) => {
    const text = query.trim().toLowerCase()
    return (
      (subject === 'All' || q.subjectId === subject) &&
      (topic === 'All' || q.topicId === topic) &&
      (difficulty === 'All' || q.difficulty === difficulty) &&
      (!text || q.text.toLowerCase().includes(text))
    )
  })

  // topics only make sense once a subject is chosen
  const topicOptions = subject === 'All' ? [] : s.topicsOf(subject)

  return (
    <>
      <div className="grid grid-4">
        <Stat label="Questions" value={state.questions.length} />
        <Stat
          label="Easy"
          value={state.questions.filter((q) => q.difficulty === 'Easy').length}
          accent="var(--ok)"
        />
        <Stat
          label="Medium"
          value={state.questions.filter((q) => q.difficulty === 'Medium').length}
          accent="var(--warn)"
        />
        <Stat
          label="Hard"
          value={state.questions.filter((q) => q.difficulty === 'Hard').length}
          accent="var(--danger)"
        />
      </div>

      <SectionHead
        title="Bank coverage"
        sub="Questions available per subject — thin subjects cap what a paper can ask for"
      />
      <div className="card card-pad" style={{ marginBottom: 14 }}>
        {state.subjects.filter((sub) => sub.isEnabled).map((sub) => {
          const n = state.questions.filter(
            (q) => q.subjectId === sub.id && q.status === CONTENT_STATUS.PUBLISHED
          ).length
          const biggestAsk = Math.max(
            0,
            ...state.exams.flatMap((e) =>
              (e.blueprint || []).filter((r) => r.subjectId === sub.id).map((r) => Number(r.questions || 0))
            )
          )
          const ok = n >= biggestAsk
          return (
            <div
              key={sub.id}
              className="btn-row"
              style={{ padding: '7px 0', borderBottom: '1px solid var(--border)', alignItems: 'center' }}
            >
              <div style={{ flex: 1 }}>
                <div className="cell-title">{sub.emoji} {sub.nameEn}</div>
                <div className="cell-sub">
                  {s.topicsOf(sub.id).length} topics · biggest paper asks for {biggestAsk}
                </div>
              </div>
              <span className="cell-sub" style={{ marginRight: 10 }}>{n} published</span>
              {ok ? <Pill tone="ok">Covered</Pill> : <Pill tone="danger">{biggestAsk - n} short</Pill>}
            </div>
          )
        })}
      </div>

      <SectionHead title="Filters">
        <SearchInput value={query} onChange={setQuery} placeholder="Search question text…" />
        <button className="primary" onClick={() => setEdit({})}>New question</button>
      </SectionHead>
      <div className="card card-pad" style={{ marginBottom: 14 }}>
        <div style={{ marginBottom: 10 }}>
          <Chips
            options={[
              { id: 'All', label: 'All subjects' },
              ...state.subjects.map((sub) => ({ id: sub.id, label: sub.emoji + ' ' + sub.nameEn }))
            ]}
            value={subject}
            onToggle={(v) => {
              setSubject(v)
              setTopic('All')
            }}
            multi={false}
          />
        </div>
        {topicOptions.length ? (
          <div style={{ marginBottom: 10 }}>
            <Chips
              options={[
                { id: 'All', label: 'All topics' },
                ...topicOptions.map((t) => ({
                  id: t.id,
                  label: t.nameEn + ' · ' + s.questionsOfTopic(t.id).length + 'Q'
                }))
              ]}
              value={topic}
              onToggle={setTopic}
              multi={false}
            />
          </div>
        ) : null}
        <Chips options={['All', ...DIFFICULTY]} value={difficulty} onToggle={setDifficulty} multi={false} />
      </div>

      <SectionHead title={rows.length + ' questions'} sub="Exams draw their papers from here" />
      {rows.length === 0 ? (
        <Empty title="No questions match" description="Loosen the filters or add a question." />
      ) : (
        <div className="card table-wrap">
          <table>
            <thead>
              <tr>
                <th>Question</th>
                <th>Subject / topic</th>
                <th>Answer</th>
                <th>Difficulty</th>
                <th />
              </tr>
            </thead>
            <tbody>
              {rows.map((q) => (
                <tr key={q.id}>
                  <td style={{ maxWidth: 380 }}>
                    <div className="cell-title">{q.text}</div>
                    <div className="cell-sub">{q.options.join(' · ')}</div>
                  </td>
                  <td>
                    <div className="cell-title">{s.subjectName(q.subjectId)}</div>
                    <div className="cell-sub">{s.topicName(q.topicId)}</div>
                  </td>
                  <td>
                    <Pill tone="ok">{q.options[q.correctIndex]}</Pill>
                  </td>
                  <td>
                    <Pill tone={q.difficulty === 'Hard' ? 'danger' : q.difficulty === 'Medium' ? 'warn' : 'ok'}>
                      {q.difficulty}
                    </Pill>
                  </td>
                  <td className="actions">
                    <button className="small ghost" onClick={() => setEdit(q)}>Edit</button>
                    <button className="small danger" onClick={() => setRemove(q)}>Delete</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {edit ? (
        <QuestionEditor
          question={edit}
          onClose={() => setEdit(null)}
          onSave={(fields) => {
            dispatch({ type: 'question/save', payload: { id: edit.id, fields } })
            setEdit(null)
            notify(edit.id ? 'Question updated' : 'Question added to the bank')
          }}
        />
      ) : null}

      {remove ? (
        <Confirm
          title="Delete this question?"
          message="Exams built from the bank will no longer include it."
          confirmLabel="Delete"
          danger
          onClose={() => setRemove(null)}
          onConfirm={() => {
            dispatch({ type: 'question/delete', payload: { id: remove.id } })
            setRemove(null)
            notify('Question deleted')
          }}
        />
      ) : null}
    </>
  )
}

export function QuestionEditor({ question, onClose, onSave }) {
  const { state } = useStore()
  const s = useSelectors()
  const [form, setForm] = useState({
    subjectId: question.subjectId || state.subjects[0]?.id,
    topicId: question.topicId || '',
    text: question.text || '',
    options: question.options || ['', '', '', ''],
    correctIndex: question.correctIndex ?? 0,
    difficulty: question.difficulty || 'Medium',
    explanation: question.explanation || '',
    source: question.source || QUESTION_SOURCE.SAMPLE,
    paperName: question.paperName || '',
    year: question.year || '',
    status: question.status || CONTENT_STATUS.PUBLISHED
  })
  const [showErrors, setShowErrors] = useState(false)

  const topics = s.topicsOf(form.subjectId)
  const textError = form.text.trim() ? null : 'Question text is required'
  const optionError = form.options.filter((o) => o.trim()).length < 2 ? 'At least two options are needed' : null
  const topicError = form.topicId ? null : 'Pick a topic'

  const setOption = (i, value) =>
    setForm((f) => ({ ...f, options: f.options.map((o, idx) => (idx === i ? value : o)) }))

  return (
    <Modal
      wide
      title={question.id ? 'Edit question' : 'New question'}
      onClose={onClose}
      footer={
        <>
          <button onClick={onClose}>Cancel</button>
          <button
            className="primary"
            onClick={() => {
              setShowErrors(true)
              if (textError || optionError || topicError) return
              onSave({ ...form, options: form.options.filter((o) => o.trim()) })
            }}
          >
            Save question
          </button>
        </>
      }
    >
      <div className="form-row">
        <Field label="Where it comes from">
          <select value={form.source} onChange={(e) => setForm({ ...form, source: e.target.value })}>
            <option value={QUESTION_SOURCE.SAMPLE}>Sample question — written for practice</option>
            <option value={QUESTION_SOURCE.PREVIOUS}>Previously asked — from a real paper</option>
          </select>
        </Field>
        {form.source === QUESTION_SOURCE.PREVIOUS ? (
          <>
            <Field label="Paper">
              <input
                type="text"
                value={form.paperName}
                placeholder="TSPSC Group-2 General Studies"
                onChange={(e) => setForm({ ...form, paperName: e.target.value })}
              />
            </Field>
            <Field label="Year">
              <input
                type="text"
                value={form.year}
                placeholder="2023"
                onChange={(e) => setForm({ ...form, year: e.target.value })}
              />
            </Field>
          </>
        ) : null}
      </div>

      <Field label="Question text *" error={showErrors ? textError : null}>
        <textarea style={{ minHeight: 70 }} value={form.text} onChange={(e) => setForm({ ...form, text: e.target.value })} />
      </Field>
      <div className="form-row">
        <Field label="Subject">
          <select
            value={form.subjectId}
            onChange={(e) => setForm({ ...form, subjectId: e.target.value, topicId: '' })}
          >
            {state.subjects.map((sub) => (
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
        <Field label="Difficulty">
          <select value={form.difficulty} onChange={(e) => setForm({ ...form, difficulty: e.target.value })}>
            {DIFFICULTY.map((d) => <option key={d} value={d}>{d}</option>)}
          </select>
        </Field>
      </div>

      <SectionHead title="Options" sub="Tick the correct answer" />
      {showErrors && optionError ? <div className="field-error">{optionError}</div> : null}
      {form.options.map((o, i) => (
        <div className="btn-row" key={i} style={{ marginBottom: 8 }}>
          <button
            type="button"
            className={'chip' + (form.correctIndex === i ? ' on' : '')}
            onClick={() => setForm({ ...form, correctIndex: i })}
          >
            {String.fromCharCode(65 + i)}
          </button>
          <input
            type="text"
            value={o}
            placeholder={'Option ' + String.fromCharCode(65 + i)}
            onChange={(e) => setOption(i, e.target.value)}
            style={{ flex: 1 }}
          />
        </div>
      ))}

      <Field label="Explanation shown after submission">
        <textarea
          style={{ minHeight: 60 }}
          value={form.explanation}
          onChange={(e) => setForm({ ...form, explanation: e.target.value })}
        />
      </Field>
    </Modal>
  )
}

/* ================================================================== exams */

export function Exams() {
  const { state, dispatch } = useStore()
  const s = useSelectors()
  const { notify } = useToast()
  const [type, setType] = useState('Daily Exam')
  const [track, setTrack] = useState('All')
  const [edit, setEdit] = useState(null)
  const [remove, setRemove] = useState(null)

  const rows = state.exams.filter(
    (e) =>
      e.type === type &&
      (track === 'All' || e.trackIds.includes(track) || (track === 'General' && e.trackIds.length === 0))
  )

  /** Can the question bank actually fill this paper right now? */
  const readiness = (e) => {
    const cover = s.paperCoverage(e)
    if (!e.blueprint?.length) return <Pill tone="muted">No blueprint</Pill>
    return cover.ready ? (
      <Pill tone="ok">Ready</Pill>
    ) : (
      <Pill tone="danger">{cover.shortTotal} short</Pill>
    )
  }

  const notReady = state.exams.filter((e) => e.isActive && !s.paperCoverage(e).ready)

  return (
    <>
      <DemoNote>
        A paper tagged with an exam type is only offered to those students. Untagged papers are general
        practice and appear under every exam type.
      </DemoNote>

      <div className="grid grid-4">
        <Stat
          label="Daily exams"
          value={state.exams.filter((e) => e.type === 'Daily Exam').length}
          caption={state.exams.filter((e) => e.type === 'Daily Exam' && e.isActive).length + ' active'}
        />
        <Stat
          label="Grand tests"
          value={state.exams.filter((e) => e.type === 'Grand Test').length}
          caption={state.exams.filter((e) => e.type === 'Grand Test' && e.isActive).length + ' active'}
          accent="var(--info)"
        />
        <Stat
          label="Tagged to an exam type"
          value={state.exams.filter((e) => e.trackIds.length > 0).length}
          accent="var(--brand)"
        />
        <Stat
          label="Live but short"
          value={notReady.length}
          caption="Active papers the bank cannot fill"
          accent={notReady.length ? 'var(--danger)' : 'var(--muted)'}
        />
      </div>

      <SectionHead title="Papers" sub="Create, tag, activate">
        <button className="primary" onClick={() => setEdit({ type })}>New {type.toLowerCase()}</button>
      </SectionHead>
      <div className="card card-pad" style={{ marginBottom: 14 }}>
        <div style={{ marginBottom: 10 }}>
          <Chips options={['Daily Exam', 'Grand Test']} value={type} onToggle={setType} multi={false} />
        </div>
        <Chips
          options={[
            { id: 'All', label: 'All exam types' },
            { id: 'General', label: 'General practice' },
            ...state.tracks.map((t) => ({ id: t.id, label: t.emoji + ' ' + t.shortName }))
          ]}
          value={track}
          onToggle={setTrack}
          multi={false}
        />
      </div>

      {rows.length === 0 ? (
        <Empty title="No papers here" description="Create one, or change the filter." />
      ) : (
        <div className="card table-wrap">
          <table>
            <thead>
              <tr>
                <th>Paper</th>
                <th>For</th>
                <th>Shape</th>
                <th>Blueprint</th>
                <th>Bank</th>
                <th>Live</th>
                <th />
              </tr>
            </thead>
            <tbody>
              {rows.map((e) => (
                <tr key={e.id}>
                  <td style={{ maxWidth: 280 }}>
                    <div className="cell-title">{e.title}</div>
                    <div className="cell-sub">{e.dateLabel} · {e.difficulty}</div>
                  </td>
                  <td>
                    {e.trackIds.length === 0 ? (
                      <Pill tone="muted">Every exam type</Pill>
                    ) : (
                      <div className="btn-row">
                        {e.trackIds.map((id) => (
                          <Pill key={id}>{s.trackName(id)}</Pill>
                        ))}
                      </div>
                    )}
                  </td>
                  <td className="num">
                    {e.questionCount} Q · {e.totalMarks} marks
                    <div className="cell-sub">{e.durationMinutes} min</div>
                  </td>
                  <td>
                    <div className="cell-sub">
                      {e.subjectIds.slice(0, 3).map((id) => s.subjectName(id)).join(', ')}
                      {e.subjectIds.length > 3 ? ' +' + (e.subjectIds.length - 3) : ''}
                    </div>
                  </td>
                  <td>{readiness(e)}</td>
                  <td>
                    <Switch
                      checked={e.isActive}
                      onChange={() => {
                        dispatch({ type: 'exam/toggleActive', payload: { id: e.id, title: e.title } })
                        notify(e.title + (e.isActive ? ' deactivated' : ' is live — students notified'))
                      }}
                    />
                  </td>
                  <td className="actions">
                    <button className="small ghost" onClick={() => setEdit(e)}>Edit</button>
                    <button className="small danger" onClick={() => setRemove(e)}>Delete</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {edit ? (
        <ExamEditor
          exam={edit}
          onClose={() => setEdit(null)}
          onSave={(fields) => {
            dispatch({ type: 'exam/save', payload: { id: edit.id, fields } })
            setEdit(null)
            notify(edit.id ? 'Exam updated' : 'Exam created')
          }}
        />
      ) : null}

      {remove ? (
        <Confirm
          title="Delete this paper?"
          message={'"' + remove.title + '" disappears from the student app.'}
          confirmLabel="Delete"
          danger
          onClose={() => setRemove(null)}
          onConfirm={() => {
            dispatch({ type: 'exam/delete', payload: { id: remove.id, title: remove.title } })
            setRemove(null)
            notify('Paper deleted')
          }}
        />
      ) : null}
    </>
  )
}

/* ---------------------------------------------------------------- blueprint */

/**
 * One subject row of the paper. The bank column is live: it counts the
 * published questions that actually match this row, so a paper can never
 * quietly ask for more than exists.
 */
function BlueprintRow({ row, onChange, onRemove }) {
  const { state } = useStore()
  const s = useSelectors()
  const [openTopics, setOpenTopics] = useState(false)

  const topics = s.topicsOf(row.subjectId)
  const have = s.availableFor(row).length
  const want = Number(row.questions || 0)
  const short = Math.max(0, want - have)

  return (
    <>
      <tr>
        <td>
          <div className="cell-title">
            {s.subjectEmoji(row.subjectId)} {s.subjectName(row.subjectId)}
          </div>
          <div className="cell-sub">
            {row.topicIds.length
              ? row.topicIds.length + ' of ' + topics.length + ' topics'
              : 'All ' + topics.length + ' topics'}
          </div>
        </td>
        <td className="num">
          <input
            type="number"
            min="0"
            className="mini"
            value={row.questions}
            onChange={(e) => onChange({ ...row, questions: e.target.value })}
          />
        </td>
        <td className="num">
          <input
            type="number"
            min="0"
            className="mini"
            value={row.marks}
            onChange={(e) => onChange({ ...row, marks: e.target.value })}
          />
        </td>
        <td className="num">
          {short ? (
            <Pill tone="danger">{have} — short {short}</Pill>
          ) : (
            <Pill tone="ok">{have} ready</Pill>
          )}
        </td>
        <td className="actions">
          <button type="button" className="small ghost" onClick={() => setOpenTopics((v) => !v)}>
            {openTopics ? 'Hide topics' : 'Topics'}
          </button>
          <button type="button" className="small danger" onClick={onRemove}>Remove</button>
        </td>
      </tr>

      {openTopics ? (
        <tr>
          <td colSpan={5} style={{ background: 'var(--panel-2)' }}>
            <div className="cell-sub" style={{ marginBottom: 8 }}>
              Leave every topic off to draw from the whole subject. Pick topics to pin the paper to
              specific chapters — the bank count above follows your choice.
            </div>
            {topics.length === 0 ? (
              <div className="cell-sub">No topics under this subject yet.</div>
            ) : (
              <div className="picklist">
                {topics.map((t) => {
                  const on = row.topicIds.includes(t.id)
                  const n = s.questionsOfTopic(t.id).length
                  return (
                    <button
                      type="button"
                      key={t.id}
                      className={'chip' + (on ? ' on' : '')}
                      onClick={() =>
                        onChange({
                          ...row,
                          topicIds: on
                            ? row.topicIds.filter((x) => x !== t.id)
                            : [...row.topicIds, t.id]
                        })
                      }
                    >
                      {t.nameEn} · {n}Q
                    </button>
                  )
                })}
              </div>
            )}
          </td>
        </tr>
      ) : null}
    </>
  )
}

/* ------------------------------------------------------------- exam editor */

function ExamEditor({ exam, onClose, onSave }) {
  const { state } = useStore()
  const s = useSelectors()
  const isGrand = (exam.type || 'Daily Exam') === 'Grand Test'
  const [form, setForm] = useState({
    title: exam.title || (isGrand ? 'Weekly Grand Test' : 'Daily Exam'),
    type: exam.type || 'Daily Exam',
    dateLabel: exam.dateLabel || (isGrand ? 'This Sunday' : 'Today'),
    durationMinutes: exam.durationMinutes || (isGrand ? 90 : 20),
    blueprint: (exam.blueprint || []).map((r) => ({ ...r, topicIds: r.topicIds || [] })),
    trackIds: exam.trackIds || [],
    difficulty: exam.difficulty || (isGrand ? 'Hard' : 'Medium'),
    instructions: exam.instructions || '',
    isActive: exam.isActive !== false
  })
  const [showErrors, setShowErrors] = useState(false)
  const [preview, setPreview] = useState(false)

  const totalQuestions = form.blueprint.reduce((n, r) => n + Number(r.questions || 0), 0)
  const totalMarks = form.blueprint.reduce((n, r) => n + Number(r.marks || 0), 0)
  const coverage = s.paperCoverage(form)

  const titleError = form.title.trim() ? null : 'Title is required'
  const blueprintError = form.blueprint.length ? null : 'Add at least one subject to the blueprint'
  const countError = totalQuestions > 0 ? null : 'The blueprint adds up to zero questions'
  const firstError = titleError || blueprintError || countError

  const setRow = (i, row) =>
    setForm((f) => ({ ...f, blueprint: f.blueprint.map((r, n) => (n === i ? row : r)) }))
  const removeRow = (i) =>
    setForm((f) => ({ ...f, blueprint: f.blueprint.filter((_, n) => n !== i) }))
  const addSubject = (subjectId) =>
    setForm((f) => ({
      ...f,
      blueprint: [...f.blueprint, { subjectId, questions: 5, marks: 5, topicIds: [] }]
    }))

  const unusedSubjects = state.subjects.filter(
    (sub) => sub.isEnabled && !form.blueprint.some((r) => r.subjectId === sub.id)
  )

  /** Fill the blueprint straight from an exam type's official pattern. */
  const applyPattern = (trackId) => {
    const track = state.tracks.find((t) => t.id === trackId)
    if (!track) return
    const grand = form.type === 'Grand Test'
    // a daily exam is a scaled-down slice of the full pattern
    const scale = grand ? 1 : 20 / (track.totalQuestions || 1)
    const rows = track.sections
      .map((sec) => ({
        subjectId: sec.subjectId,
        questions: grand ? sec.questions : Math.max(1, Math.round(sec.questions * scale)),
        marks: grand ? sec.marks : Math.max(1, Math.round(sec.marks * scale)),
        topicIds: []
      }))
      .filter((r) => r.questions > 0)
    setForm((f) => ({
      ...f,
      trackIds: [trackId],
      title: track.shortName + ' ' + (grand ? 'Grand Test' : 'Daily Exam'),
      durationMinutes: grand ? track.durationMinutes : 20,
      blueprint: rows,
      instructions:
        rows.reduce((n, r) => n + r.questions, 0) + ' Q · ' +
        rows.reduce((n, r) => n + r.marks, 0) + ' marks · ' + track.negativeMarking + '.'
    }))
  }

  /** Spread a target total across the subjects already in the blueprint. */
  const balanceTo = (target) => {
    if (!form.blueprint.length) return
    const each = Math.floor(target / form.blueprint.length)
    let left = target - each * form.blueprint.length
    setForm((f) => ({
      ...f,
      blueprint: f.blueprint.map((r) => {
        const extra = left-- > 0 ? 1 : 0
        const n = each + extra
        return { ...r, questions: n, marks: n }
      })
    }))
  }

  return (
    <Modal
      wide
      title={exam.id ? 'Edit paper' : 'New paper'}
      sub="Blueprint first — the header totals are worked out from it."
      onClose={onClose}
      footer={
        <>
          {showErrors && firstError ? <span className="field-error">{firstError}</span> : null}
          <span className="cell-sub" style={{ marginRight: 'auto' }}>
            {totalQuestions} questions · {totalMarks} marks
            {coverage.shortTotal ? ' · ' + coverage.shortTotal + ' short in the bank' : ''}
          </span>
          <button onClick={onClose}>Cancel</button>
          <button
            className="primary"
            onClick={() => {
              setShowErrors(true)
              if (firstError) return
              onSave({
                ...form,
                durationMinutes: Number(form.durationMinutes),
                blueprint: form.blueprint.map((r) => ({
                  ...r,
                  questions: Number(r.questions || 0),
                  marks: Number(r.marks || 0)
                }))
              })
            }}
          >
            Save paper
          </button>
        </>
      }
    >
      {/* ------------------------------------------------------ 1. paper */}
      <div className="form-section">
        <div className="form-section-head">
          <span className="step">1</span>
          <div>
            <div className="cell-title">The paper</div>
            <div className="cell-sub">What students see before they start.</div>
          </div>
        </div>

        <Field label="Title *" error={showErrors ? titleError : null}>
          <input
            type="text"
            value={form.title}
            onChange={(e) => setForm({ ...form, title: e.target.value })}
          />
        </Field>
        <div className="form-row">
          <Field label="Type">
            <select value={form.type} onChange={(e) => setForm({ ...form, type: e.target.value })}>
              <option value="Daily Exam">Daily Exam</option>
              <option value="Grand Test">Grand Test</option>
            </select>
          </Field>
          <Field label="Date label">
            <input
              type="text"
              value={form.dateLabel}
              onChange={(e) => setForm({ ...form, dateLabel: e.target.value })}
            />
          </Field>
          <Field label="Duration (min)">
            <input
              type="number"
              value={form.durationMinutes}
              onChange={(e) => setForm({ ...form, durationMinutes: e.target.value })}
            />
          </Field>
          <Field label="Difficulty">
            <select
              value={form.difficulty}
              onChange={(e) => setForm({ ...form, difficulty: e.target.value })}
            >
              {DIFFICULTY.map((d) => <option key={d} value={d}>{d}</option>)}
            </select>
          </Field>
        </div>
      </div>

      {/* -------------------------------------------------- 2. exam types */}
      <div className="form-section">
        <div className="form-section-head">
          <span className="step">2</span>
          <div>
            <div className="cell-title">Who sits it</div>
            <div className="cell-sub">
              Tag an exam type and only those students are offered it. Leave empty for general practice,
              which appears under every exam type.
            </div>
          </div>
        </div>

        <div className="picklist">
          {state.tracks.map((t) => (
            <button
              type="button"
              key={t.id}
              className={'chip' + (form.trackIds.includes(t.id) ? ' on' : '')}
              onClick={() =>
                setForm((f) => ({
                  ...f,
                  trackIds: f.trackIds.includes(t.id)
                    ? f.trackIds.filter((x) => x !== t.id)
                    : [...f.trackIds, t.id]
                }))
              }
            >
              {t.emoji} {t.shortName}
            </button>
          ))}
        </div>

        {form.trackIds.length === 1 ? (
          <button
            type="button"
            className="small"
            style={{ marginTop: 10 }}
            onClick={() => applyPattern(form.trackIds[0])}
          >
            Build blueprint from the{' '}
            {state.tracks.find((t) => t.id === form.trackIds[0])?.shortName} pattern
          </button>
        ) : null}
      </div>

      {/* --------------------------------------------------- 3. blueprint */}
      <div className="form-section">
        <div className="form-section-head">
          <span className="step">3</span>
          <div>
            <div className="cell-title">Blueprint</div>
            <div className="cell-sub">
              How many questions come from each subject, and optionally from which topics. The bank
              column counts what is actually published and available.
            </div>
          </div>
        </div>

        {showErrors && blueprintError ? <div className="field-error">{blueprintError}</div> : null}

        {form.blueprint.length ? (
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>Subject</th>
                  <th className="num">Questions</th>
                  <th className="num">Marks</th>
                  <th className="num">In the bank</th>
                  <th />
                </tr>
              </thead>
              <tbody>
                {form.blueprint.map((row, i) => (
                  <BlueprintRow
                    key={row.subjectId}
                    row={row}
                    onChange={(r) => setRow(i, r)}
                    onRemove={() => removeRow(i)}
                  />
                ))}
                <tr>
                  <td className="cell-title">Total</td>
                  <td className="num cell-title">{totalQuestions}</td>
                  <td className="num cell-title">{totalMarks}</td>
                  <td className="num">
                    {coverage.ready ? (
                      <Pill tone="ok">Ready to run</Pill>
                    ) : (
                      <Pill tone="danger">{coverage.shortTotal} short</Pill>
                    )}
                  </td>
                  <td />
                </tr>
              </tbody>
            </table>
          </div>
        ) : (
          <div className="cell-sub">
            Nothing in the blueprint yet — add a subject below, or build it from an exam type pattern.
          </div>
        )}

        {unusedSubjects.length ? (
          <Field label="Add a subject">
            <div className="picklist">
              {unusedSubjects.map((sub) => (
                <button type="button" key={sub.id} className="chip" onClick={() => addSubject(sub.id)}>
                  + {sub.emoji} {sub.nameEn}
                </button>
              ))}
            </div>
          </Field>
        ) : null}

        {form.blueprint.length ? (
          <div className="btn-row" style={{ marginTop: 10, alignItems: 'center' }}>
            <span className="cell-sub">Even split across subjects:</span>
            {[20, 50, 100].map((n) => (
              <button type="button" key={n} className="small ghost" onClick={() => balanceTo(n)}>
                {n} Q
              </button>
            ))}
            <span style={{ flex: 1 }} />
            <button
              type="button"
              className="small ghost"
              disabled={!totalQuestions}
              onClick={() => setPreview(true)}
            >
              Preview paper
            </button>
          </div>
        ) : null}

        {coverage.shortRows.length ? (
          <div className="warn-note" style={{ background: 'var(--danger-soft)', color: 'var(--danger)' }}>
            ⚠ The bank cannot fill this paper yet —{' '}
            {coverage.shortRows
              .map((r) => s.subjectName(r.subjectId) + ' needs ' + r.short + ' more')
              .join(', ')}
            . You can still save it; add the questions before activating.
          </div>
        ) : null}
      </div>

      {/* ----------------------------------------------- 4. instructions */}
      <div className="form-section">
        <div className="form-section-head">
          <span className="step">4</span>
          <div>
            <div className="cell-title">Before they start</div>
            <div className="cell-sub">Instructions shown on the cover screen.</div>
          </div>
        </div>

        <Field label="Instructions">
          <textarea
            style={{ minHeight: 70 }}
            value={form.instructions}
            onChange={(e) => setForm({ ...form, instructions: e.target.value })}
          />
        </Field>

        <Switch
          checked={form.isActive}
          onChange={(v) => setForm({ ...form, isActive: v })}
          label="Activate immediately (students get a notification)"
        />
        {form.isActive && !coverage.ready ? (
          <div className="warn-note">
            ⚠ Activating a paper the bank cannot fill means students meet a short paper. Add the missing
            questions first, or leave it inactive for now.
          </div>
        ) : null}
      </div>

      {preview ? <PaperPreview paper={form} onClose={() => setPreview(false)} /> : null}
    </Modal>
  )
}

/* ----------------------------------------------------------------- preview */

/** The actual questions this blueprint draws, grouped the way students see them. */
function PaperPreview({ paper, onClose }) {
  const s = useSelectors()
  const drawn = s.drawPaper(paper)

  return (
    <Modal
      wide
      title="Paper preview"
      sub={
        drawn.length +
        ' of ' +
        paper.blueprint.reduce((n, r) => n + Number(r.questions || 0), 0) +
        ' questions drawn from the bank'
      }
      onClose={onClose}
      footer={<button className="primary" onClick={onClose}>Close</button>}
    >
      {drawn.length === 0 ? (
        <Empty
          title="Nothing to draw"
          description="The bank has no published questions matching this blueprint."
        />
      ) : (
        paper.blueprint.map((row) => {
          const rowQuestions = s.availableFor(row).slice(0, Number(row.questions || 0))
          const missing = Number(row.questions || 0) - rowQuestions.length
          return (
            <div key={row.subjectId} className="role-block">
              <div className="role-block-head">
                {s.subjectEmoji(row.subjectId)} {s.subjectName(row.subjectId)} · {row.questions} Q ·{' '}
                {row.marks} marks
                {missing > 0 ? <span className="field-error"> — {missing} missing</span> : null}
              </div>
              <ol className="preview-list">
                {rowQuestions.map((q) => (
                  <li key={q.id}>
                    <div className="cell-title">{q.text}</div>
                    <div className="cell-sub">
                      {s.topicName(q.topicId)} · {q.difficulty} · answer:{' '}
                      {q.options[q.correctIndex]}
                    </div>
                  </li>
                ))}
              </ol>
            </div>
          )
        })
      )}
    </Modal>
  )
}

/* ================================================================ results */

export function Results() {
  const { state } = useStore()
  const s = useSelectors()
  const [track, setTrack] = useState('All')
  const [view, setView] = useState(null)

  const rows = state.results
    .filter((r) => track === 'All' || r.trackId === track || (track === 'General' && !r.trackId))
    .sort((a, b) => b.takenAt - a.takenAt)

  const accuracy = (r) => Math.round((r.correct * 100) / r.totalQuestions)
  const average = rows.length
    ? Math.round(rows.reduce((n, r) => n + accuracy(r), 0) / rows.length)
    : 0

  return (
    <>
      <div className="grid grid-4">
        <Stat label="Attempts" value={rows.length} />
        <Stat label="Average accuracy" value={average + '%'} accent={average >= 50 ? 'var(--ok)' : 'var(--danger)'} />
        <Stat
          label="Grand test attempts"
          value={rows.filter((r) => r.type === 'Grand Test').length}
          accent="var(--info)"
        />
        <Stat
          label="Exam types with activity"
          value={new Set(rows.map((r) => r.trackId).filter(Boolean)).size}
          accent="var(--brand)"
        />
      </div>

      <SectionHead title="Attempts" sub="Every submitted paper, newest first" />
      <div className="card card-pad" style={{ marginBottom: 14 }}>
        <Chips
          options={[
            { id: 'All', label: 'All exam types' },
            { id: 'General', label: 'General papers' },
            ...state.tracks.map((t) => ({ id: t.id, label: t.emoji + ' ' + t.shortName }))
          ]}
          value={track}
          onToggle={setTrack}
          multi={false}
        />
      </div>

      {rows.length === 0 ? (
        <Empty title="No attempts" description="Nothing has been submitted for this exam type." />
      ) : (
        <div className="card table-wrap">
          <table>
            <thead>
              <tr>
                <th>Student</th>
                <th>Paper</th>
                <th>Exam type</th>
                <th>Score</th>
                <th>Accuracy</th>
                <th>Rank</th>
                <th>Taken</th>
                <th />
              </tr>
            </thead>
            <tbody>
              {rows.map((r) => (
                <tr key={r.id}>
                  <td className="cell-title">{r.studentName}</td>
                  <td>
                    <div className="cell-title">{r.examTitle}</div>
                    <div className="cell-sub">{r.type}</div>
                  </td>
                  <td>{r.trackId ? <Pill>{s.trackName(r.trackId)}</Pill> : <Pill tone="muted">General</Pill>}</td>
                  <td className="num">{r.correct}/{r.totalQuestions}</td>
                  <td style={{ minWidth: 120 }}>
                    <Bar percent={accuracy(r)} />
                    <div className="cell-sub">{accuracy(r)}% · {s.band(accuracy(r))}</div>
                  </td>
                  <td className="num">{r.rank ? '#' + r.rank + ' / ' + r.participants : '—'}</td>
                  <td className="num">{relativeTime(r.takenAt)}</td>
                  <td className="actions">
                    <button className="small ghost" onClick={() => setView(r)}>Open</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <SectionHead title="Subject accuracy across students" sub="Summed from every recorded attempt" />
      <div className="card card-pad">
        {state.subjects.map((sub) => {
          const acc = s.subjectAccuracy(sub.id)
          if (acc === null) return null
          return (
            <div key={sub.id} style={{ padding: '7px 0' }}>
              <div className="btn-row">
                <span style={{ flex: 1 }}>{sub.emoji} {sub.nameEn}</span>
                <span className="cell-sub">{acc}% · {s.band(acc)}</span>
              </div>
              <Bar percent={acc} />
            </div>
          )
        })}
      </div>

      {view ? (
        <Modal
          title={view.examTitle}
          sub={view.studentName + ' · ' + relativeTime(view.takenAt)}
          onClose={() => setView(null)}
          footer={<button onClick={() => setView(null)}>Close</button>}
        >
          <div className="grid grid-4">
            <Stat label="Correct" value={view.correct} accent="var(--ok)" />
            <Stat label="Wrong" value={view.wrong} accent="var(--danger)" />
            <Stat label="Skipped" value={view.skipped} accent="var(--muted)" />
            <Stat label="Accuracy" value={accuracy(view) + '%'} />
          </div>
          <div className="kv" style={{ marginTop: 14 }}>
            <span className="k">Time taken</span>
            <span className="v num">{clock(view.timeTakenSeconds)}</span>
          </div>
          <div className="kv">
            <span className="k">Exam type</span>
            <span className="v">{view.trackId ? s.trackName(view.trackId) : 'General practice'}</span>
          </div>
          {view.rank ? (
            <div className="kv">
              <span className="k">Rank</span>
              <span className="v num">#{view.rank} of {view.participants}</span>
            </div>
          ) : null}
        </Modal>
      ) : null}
    </>
  )
}
