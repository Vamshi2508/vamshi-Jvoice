import { Fragment, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { useSelectors, useStore, useToast } from '../store/store.jsx'
import { Empty, Pill, SectionHead, Stat, StatusPill } from '../components/ui.jsx'
import { CONTENT_STATUS, QUESTION_SOURCE } from '../data/studyData.js'
import { StudyArticleEditor, TopicEditor } from './Study.jsx'
import { QuestionEditor } from './Exams.jsx'

/**
 * The student's path, walked from the admin side: one exam type, the
 * categories inside it, and the topics inside each category. Every topic
 * links to its workspace, where the material and both question banks live.
 */
export default function ExamTypeSyllabus() {
  const { trackId } = useParams()
  const { state, dispatch } = useStore()
  const s = useSelectors()
  const { notify } = useToast()
  const nav = useNavigate()
  const [open, setOpen] = useState(null)
  // topics expanded to show their articles and questions
  const [openTopics, setOpenTopics] = useState([])
  // one of: {kind:'topic'|'article'|'question', ...seed}
  const [creating, setCreating] = useState(null)
  const [editing, setEditing] = useState(null)

  const toggleTopic = (id) =>
    setOpenTopics((list) => (list.includes(id) ? list.filter((x) => x !== id) : [...list, id]))

  const track = state.tracks.find((t) => t.id === trackId)
  if (!track) {
    return (
      <Empty
        title="Exam type not found"
        description="It may have been deleted."
        actionLabel="Back to exam types"
        onAction={() => nav('/study/exam-types')}
      />
    )
  }

  // the categories this exam actually tests, in the order the pattern lists them
  const sections = track.sections.map((sec) => {
    const topics = s.topicsOf(sec.subjectId)
    const questions = state.questions.filter((q) => q.subjectId === sec.subjectId)
    return {
      ...sec,
      topics,
      articles: state.studyArticles.filter((a) => a.subjectId === sec.subjectId).length,
      sample: questions.filter((q) => q.source !== QUESTION_SOURCE.PREVIOUS).length,
      previous: questions.filter((q) => q.source === QUESTION_SOURCE.PREVIOUS).length
    }
  })

  const totals = sections.reduce(
    (acc, x) => ({
      topics: acc.topics + x.topics.length,
      articles: acc.articles + x.articles,
      sample: acc.sample + x.sample,
      previous: acc.previous + x.previous
    }),
    { topics: 0, articles: 0, sample: 0, previous: 0 }
  )

  const itemsOf = (topicId) => ({
    articles: state.studyArticles.filter((a) => a.topicId === topicId),
    sample: state.questions.filter(
      (q) => q.topicId === topicId && q.source !== QUESTION_SOURCE.PREVIOUS
    ),
    previous: state.questions.filter(
      (q) => q.topicId === topicId && q.source === QUESTION_SOURCE.PREVIOUS
    )
  })

  const topicCounts = (topicId) => ({
    articles: state.studyArticles.filter((a) => a.topicId === topicId).length,
    sample: state.questions.filter(
      (q) => q.topicId === topicId && q.source !== QUESTION_SOURCE.PREVIOUS
    ).length,
    previous: state.questions.filter(
      (q) => q.topicId === topicId && q.source === QUESTION_SOURCE.PREVIOUS
    ).length
  })

  return (
    <>
      <div className="crumb">
        <button className="link" onClick={() => nav('/study/exam-types')}>Exam types</button>
        <span>›</span>
        <strong>{track.shortName}</strong>
      </div>

      <div className="card card-pad greeting">
        <div>
          <h2>{track.emoji} {track.nameEn}</h2>
          <div className="cell-sub">
            {track.tagline} · {track.totalQuestions} Q · {track.totalMarks} marks ·{' '}
            {track.durationMinutes} min
          </div>
        </div>
        <div className="tag-row">
          {track.isEnabled ? (
            <Pill tone="ok">Visible to students</Pill>
          ) : (
            <Pill tone="muted">Hidden</Pill>
          )}
        </div>
      </div>

      <div className="grid grid-4">
        <Stat label="Categories" value={sections.length} caption="Subjects in the pattern" />
        <Stat label="Topics" value={totals.topics} caption="Across all categories" />
        <Stat
          label="Study material"
          value={totals.articles}
          accent="var(--ok)"
          onClick={() => nav('/study/content')}
        />
        <Stat
          label="Questions"
          value={totals.sample + totals.previous}
          caption={totals.previous + ' from past papers'}
          accent="var(--brand)"
        />
      </div>

      <SectionHead
        title="Categories in this exam"
        sub="Open one to see its topics — add a topic, then its material and questions"
      />

      {sections.length === 0 ? (
        <Empty
          title="No pattern set"
          description="Add sections to this exam type first."
          actionLabel="Edit exam type"
          onAction={() => nav('/study/exam-types')}
        />
      ) : (
        <div className="card table-wrap">
          <table>
            <thead>
              <tr>
                <th>Category</th>
                <th className="num">In the paper</th>
                <th className="num">Topics</th>
                <th className="num">Material</th>
                <th className="num">Sample Q</th>
                <th className="num">Past Q</th>
                <th />
              </tr>
            </thead>
            <tbody>
              {sections.map((sec) => (
                <SectionRows
                  key={sec.subjectId}
                  sec={sec}
                  open={open === sec.subjectId}
                  onToggle={() => setOpen(open === sec.subjectId ? null : sec.subjectId)}
                  onOpenTopic={(id) => nav('/study/topic/' + id)}
                  onAddTopic={() =>
                    setCreating({ kind: 'topic', subjectId: sec.subjectId })
                  }
                  onAddArticle={(topicId) =>
                    setCreating({ kind: 'article', subjectId: sec.subjectId, topicId })
                  }
                  onAddQuestion={(topicId, source) =>
                    setCreating({ kind: 'question', subjectId: sec.subjectId, topicId, source })
                  }
                  counts={topicCounts}
                  items={itemsOf}
                  openTopics={openTopics}
                  onToggleTopic={toggleTopic}
                  onEditArticle={(a) => setEditing({ kind: 'article', item: a })}
                  onEditQuestion={(q) => setEditing({ kind: 'question', item: q })}
                  s={s}
                />
              ))}
            </tbody>
          </table>
        </div>
      )}

      {creating?.kind === 'topic' ? (
        <TopicEditor
          topic={{ subjectId: creating.subjectId }}
          onClose={() => setCreating(null)}
          onSave={(fields) => {
            dispatch({ type: 'topic/save', payload: { fields } })
            setOpen(creating.subjectId)
            setCreating(null)
            notify('Topic added to ' + s.subjectName(creating.subjectId))
          }}
        />
      ) : null}

      {creating?.kind === 'article' ? (
        <StudyArticleEditor
          article={{ subjectId: creating.subjectId, topicId: creating.topicId }}
          onClose={() => setCreating(null)}
          onSave={(fields) => {
            dispatch({ type: 'studyArticle/save', payload: { fields } })
            setCreating(null)
            notify('Study material added')
          }}
        />
      ) : null}

      {creating?.kind === 'question' ? (
        <QuestionEditor
          question={{
            subjectId: creating.subjectId,
            topicId: creating.topicId,
            source: creating.source
          }}
          onClose={() => setCreating(null)}
          onSave={(fields) => {
            dispatch({ type: 'question/save', payload: { fields } })
            setCreating(null)
            notify(
              creating.source === QUESTION_SOURCE.PREVIOUS
                ? 'Past question added'
                : 'Sample question added'
            )
          }}
        />
      ) : null}

      {editing?.kind === 'article' ? (
        <StudyArticleEditor
          article={editing.item}
          onClose={() => setEditing(null)}
          onSave={(fields) => {
            dispatch({ type: 'studyArticle/save', payload: { id: editing.item.id, fields } })
            setEditing(null)
            notify('Study material updated')
          }}
        />
      ) : null}

      {editing?.kind === 'question' ? (
        <QuestionEditor
          question={editing.item}
          onClose={() => setEditing(null)}
          onSave={(fields) => {
            dispatch({ type: 'question/save', payload: { id: editing.item.id, fields } })
            setEditing(null)
            notify('Question updated')
          }}
        />
      ) : null}
    </>
  )
}

function SectionRows({
  sec, open, onToggle, onOpenTopic, onAddTopic, onAddArticle, onAddQuestion,
  counts, items, openTopics, onToggleTopic, onEditArticle, onEditQuestion, s
}) {
  return (
    <>
      <tr style={{ cursor: 'pointer' }} onClick={onToggle}>
        <td>
          <div className="cell-title">
            {open ? '▾' : '▸'} {s.subjectEmoji(sec.subjectId)} {s.subjectName(sec.subjectId)}
          </div>
        </td>
        <td className="num">{sec.questions} Q · {sec.marks} marks</td>
        <td className="num">{sec.topics.length}</td>
        <td className="num">{sec.articles}</td>
        <td className="num">{sec.sample}</td>
        <td className="num">
          {sec.previous ? sec.previous : <span className="cell-sub">—</span>}
        </td>
        <td className="actions">
          <button
            className="small primary"
            onClick={(e) => { e.stopPropagation(); onAddTopic() }}
          >
            + Topic
          </button>
          <button className="small ghost" onClick={(e) => { e.stopPropagation(); onToggle() }}>
            {open ? 'Hide topics' : 'Topics'}
          </button>
        </td>
      </tr>

      {open
        ? sec.topics.map((t) => {
            const c = counts(t.id)
            const bare = c.articles === 0 && c.sample === 0
            const expanded = openTopics.includes(t.id)
            const it = expanded ? items(t.id) : null
            return (
              <Fragment key={t.id}>
                <tr className="sub-row" style={{ cursor: 'pointer' }} onClick={() => onToggleTopic(t.id)}>
                  <td style={{ paddingLeft: 30 }}>
                    <div className="cell-title">
                      {expanded ? '▾' : '▸'} {t.nameEn}
                    </div>
                    <div className="cell-sub">{t.nameTe} · {t.difficulty}</div>
                  </td>
                  <td className="num cell-sub">chapter {t.order}</td>
                  <td className="num" />
                  <td className="num">
                    {c.articles ? c.articles : <Pill tone="danger">none</Pill>}
                  </td>
                  <td className="num">
                    {c.sample ? c.sample : <Pill tone="danger">none</Pill>}
                  </td>
                  <td className="num">
                    {c.previous ? c.previous : <span className="cell-sub">—</span>}
                  </td>
                  <td className="actions">
                    <button
                      className="small ghost"
                      onClick={(e) => { e.stopPropagation(); onAddArticle(t.id) }}
                    >
                      + Material
                    </button>
                    <button
                      className="small ghost"
                      onClick={(e) => {
                        e.stopPropagation()
                        onAddQuestion(t.id, QUESTION_SOURCE.SAMPLE)
                      }}
                    >
                      + Question
                    </button>
                    <button
                      className={'small ' + (bare ? 'primary' : 'ghost')}
                      onClick={(e) => { e.stopPropagation(); onOpenTopic(t.id) }}
                    >
                      {bare ? 'Start it' : 'Open'}
                    </button>
                  </td>
                </tr>

                {expanded ? (
                  <tr className="item-row">
                    <td colSpan={7} style={{ paddingLeft: 46 }}>
                      <ItemList
                        topic={t}
                        items={it}
                        onEditArticle={onEditArticle}
                        onEditQuestion={onEditQuestion}
                        onAddArticle={() => onAddArticle(t.id)}
                        onAddQuestion={(source) => onAddQuestion(t.id, source)}
                      />
                    </td>
                  </tr>
                ) : null}
              </Fragment>
            )
          })
        : null}

      {open && sec.topics.length === 0 ? (
        <tr className="sub-row">
          <td colSpan={7} style={{ paddingLeft: 30 }}>
            <span className="cell-sub">No topics under this category yet.</span>{' '}
            <button className="small primary" onClick={onAddTopic}>Create the first topic</button>
          </td>
        </tr>
      ) : null}
    </>
  )
}

/**
 * A topic opened in place: its study articles, its sample questions and the
 * ones real papers asked. Click any line to edit it without leaving the page.
 */
function ItemList({ topic, items, onEditArticle, onEditQuestion, onAddArticle, onAddQuestion }) {
  if (!items) return null
  const { articles, sample, previous } = items
  const empty = !articles.length && !sample.length && !previous.length

  if (empty) {
    return (
      <div className="item-empty">
        <span className="cell-sub">
          Nothing under <strong>{topic.nameEn}</strong> yet.
        </span>
        <button className="small primary" onClick={onAddArticle}>Add study material</button>
        <button className="small ghost" onClick={() => onAddQuestion(QUESTION_SOURCE.SAMPLE)}>
          Add a question
        </button>
      </div>
    )
  }

  return (
    <div className="item-list">
      <div className="item-group">
        <div className="item-head">
          <span>Study material ({articles.length})</span>
          <button className="small ghost" onClick={onAddArticle}>+ Add</button>
        </div>
        {articles.length === 0 ? (
          <div className="cell-sub item-none">No material yet</div>
        ) : (
          articles.map((a) => (
            <button type="button" className="item" key={a.id} onClick={() => onEditArticle(a)}>
              <span className="item-icon">📄</span>
              <span className="item-body">
                <span className="cell-title">{a.title}</span>
                <span className="cell-sub">
                  {a.authorName} · {a.readingMinutes} min read
                </span>
              </span>
              <StatusPill status={a.status} />
            </button>
          ))
        )}
      </div>

      <div className="item-group">
        <div className="item-head">
          <span>Sample questions ({sample.length})</span>
          <button className="small ghost" onClick={() => onAddQuestion(QUESTION_SOURCE.SAMPLE)}>
            + Add
          </button>
        </div>
        {sample.length === 0 ? (
          <div className="cell-sub item-none">No sample questions yet</div>
        ) : (
          sample.map((q) => (
            <button type="button" className="item" key={q.id} onClick={() => onEditQuestion(q)}>
              <span className="item-icon">❓</span>
              <span className="item-body">
                <span className="cell-title">{q.text}</span>
                <span className="cell-sub">Answer: {q.options[q.correctIndex]}</span>
              </span>
              <Pill tone={q.difficulty === 'Easy' ? 'ok' : q.difficulty === 'Hard' ? 'danger' : 'warn'}>
                {q.difficulty}
              </Pill>
            </button>
          ))
        )}
      </div>

      <div className="item-group">
        <div className="item-head">
          <span>Previously asked ({previous.length})</span>
          <button className="small ghost" onClick={() => onAddQuestion(QUESTION_SOURCE.PREVIOUS)}>
            + Add
          </button>
        </div>
        {previous.length === 0 ? (
          <div className="cell-sub item-none">Nothing recorded from past papers</div>
        ) : (
          previous.map((q) => (
            <button type="button" className="item" key={q.id} onClick={() => onEditQuestion(q)}>
              <span className="item-icon">🏛️</span>
              <span className="item-body">
                <span className="cell-title">{q.text}</span>
                <span className="cell-sub">
                  {[q.paperName, q.year].filter(Boolean).join(' · ') || 'Paper not recorded'} · answer:{' '}
                  {q.options[q.correctIndex]}
                </span>
              </span>
              <Pill tone="info">Past</Pill>
            </button>
          ))
        )}
      </div>
    </div>
  )
}
