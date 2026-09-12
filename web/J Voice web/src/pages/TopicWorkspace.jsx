import { useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { useSelectors, useStore, useToast } from '../store/store.jsx'
import {
  Confirm, Empty, Modal, Pill, SectionHead, Stat, StatusPill, relativeTime
} from '../components/ui.jsx'
import { CONTENT_STATUS, QUESTION_SOURCE } from '../data/studyData.js'
import { StudyArticleEditor } from './Study.jsx'
import { QuestionEditor } from './Exams.jsx'

/**
 * Answer-as-you-go practice. Picking an option locks that question in: the
 * choice turns green when right and red when wrong, and the correct answer is
 * revealed either way.
 */
function QuizPlayer({ set, onClose }) {
  const [picked, setPicked] = useState({})
  const answered = Object.keys(picked).length
  const score = set.questions.filter((q) => picked[q.id] === q.correctIndex).length
  const done = answered === set.questions.length

  return (
    <Modal
      wide
      title={set.title}
      sub={answered + ' of ' + set.questions.length + ' answered'}
      onClose={onClose}
      footer={
        <>
          {answered ? (
            <span className={'score ' + (score * 2 >= answered ? 'good' : 'bad')}>
              {score} / {answered} correct
            </span>
          ) : null}
          <span style={{ flex: 1 }} />
          <button onClick={() => setPicked({})} disabled={!answered}>Retry set</button>
          <button className="primary" onClick={onClose}>Close</button>
        </>
      }
    >
      {done ? (
        <div className={'quiz-summary ' + (score * 2 >= set.questions.length ? 'good' : 'bad')}>
          Set complete — {score} of {set.questions.length} correct.
        </div>
      ) : null}

      {set.questions.map((q, i) => {
        const chosen = picked[q.id]
        const settled = chosen !== undefined
        return (
          <div className="quiz-q" key={q.id}>
            {q.source === QUESTION_SOURCE.PREVIOUS && q.paperName ? (
              <Pill tone="info">{[q.paperName, q.year].filter(Boolean).join(' · ')}</Pill>
            ) : null}
            <div className="cell-title" style={{ margin: '6px 0 10px' }}>
              {i + 1}. {q.text}
            </div>

            {q.options.map((option, index) => {
              const isCorrect = index === q.correctIndex
              const isChosen = chosen === index
              const state = !settled ? '' : isCorrect ? ' correct' : isChosen ? ' wrong' : ''
              return (
                <button
                  type="button"
                  key={index}
                  className={'quiz-option' + state}
                  disabled={settled}
                  onClick={() => setPicked((p) => ({ ...p, [q.id]: index }))}
                >
                  <span className="opt-letter">{String.fromCharCode(65 + index)}</span>
                  <span className="opt-text">{option}</span>
                  {settled && isCorrect ? <span className="opt-mark">✓</span> : null}
                  {settled && isChosen && !isCorrect ? <span className="opt-mark">✗</span> : null}
                </button>
              )
            })}

            {settled ? (
              <div className={'quiz-verdict ' + (chosen === q.correctIndex ? 'good' : 'bad')}>
                {chosen === q.correctIndex ? 'Correct' : 'Answer: ' + q.options[q.correctIndex]}
                {q.explanation ? <div className="cell-sub">{q.explanation}</div> : null}
              </div>
            ) : null}
          </div>
        )
      })}
    </Modal>
  )
}

/** One question list — used for both the sample bank and the past-paper bank. */
function QuestionTable({ rows, empty, showPaper, onAdd, onEdit, onRemove }) {
  const s = useSelectors()
  if (rows.length === 0) {
    return (
      <Empty
        title={empty.title}
        description={empty.description}
        actionLabel={empty.action}
        onAction={onAdd}
      />
    )
  }
  return (
    <div className="card table-wrap">
      <table>
        <thead>
          <tr>
            <th>Question</th>
            {showPaper ? <th>Asked in</th> : null}
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
                {q.explanation ? <div className="cell-sub">{q.explanation}</div> : null}
              </td>
              {showPaper ? (
                <td>
                  <div className="cell-title">{q.paperName || '—'}</div>
                  <div className="cell-sub">{q.year}</div>
                </td>
              ) : null}
              <td className="cell-sub">{q.options[q.correctIndex]}</td>
              <td>
                <Pill
                  tone={q.difficulty === 'Easy' ? 'ok' : q.difficulty === 'Hard' ? 'danger' : 'warn'}
                >
                  {q.difficulty}
                </Pill>
              </td>
              <td className="actions">
                <button className="small ghost" onClick={() => onEdit(q)}>Edit</button>
                <button className="small danger" onClick={() => onRemove(q)}>Delete</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}

/**
 * Everything that belongs to one topic, in one place: the study material a
 * student reads and the questions they are tested on. Both editors open with
 * the subject and topic already filled in, so nothing lands unfiled.
 */
export default function TopicWorkspace() {
  const { topicId } = useParams()
  const { state, dispatch } = useStore()
  const s = useSelectors()
  const { notify } = useToast()
  const nav = useNavigate()

  const [editArticle, setEditArticle] = useState(null)
  const [editQuestion, setEditQuestion] = useState(null)
  const [remove, setRemove] = useState(null)
  const [playing, setPlaying] = useState(null)

  const topic = state.topics.find((t) => t.id === topicId)
  if (!topic) {
    return (
      <Empty
        title="Topic not found"
        description="It may have been deleted."
        actionLabel="Back to topics"
        onAction={() => nav('/study/topics')}
      />
    )
  }

  const articles = state.studyArticles.filter((a) => a.topicId === topic.id)
  const questions = state.questions.filter((q) => q.topicId === topic.id)
  const quizSets = s.quizSetsOfTopic(topic.id)
  const sample = questions.filter((q) => q.source !== QUESTION_SOURCE.PREVIOUS)
  const previous = questions.filter((q) => q.source === QUESTION_SOURCE.PREVIOUS)
  const published = articles.filter((a) => a.status === CONTENT_STATUS.PUBLISHED)
  const liveQuestions = questions.filter((q) => q.status === CONTENT_STATUS.PUBLISHED)

  // papers that can pull from this topic — either pinned to it or open to the subject
  const usedBy = state.exams.filter((e) =>
    (e.blueprint || []).some(
      (r) => r.subjectId === topic.subjectId && (!r.topicIds?.length || r.topicIds.includes(topic.id))
    )
  )

  const blank = { subjectId: topic.subjectId, topicId: topic.id }

  return (
    <>
      <div className="crumb">
        <button className="link" onClick={() => nav('/study/subjects')}>
          {s.subjectEmoji(topic.subjectId)} {s.subjectName(topic.subjectId)}
        </button>
        <span>›</span>
        <button className="link" onClick={() => nav('/study/topics')}>Topics</button>
        <span>›</span>
        <strong>{topic.nameEn}</strong>
      </div>

      <div className="card card-pad greeting">
        <div>
          <h2>{topic.nameEn}</h2>
          <div className="cell-sub">
            {topic.nameTe} · chapter {topic.order} of{' '}
            {s.topicsOf(topic.subjectId).length} · {topic.difficulty}
          </div>
        </div>
        <div className="tag-row">
          {topic.isEnabled ? (
            <Pill tone="ok">Visible to students</Pill>
          ) : (
            <Pill tone="muted">Hidden</Pill>
          )}
        </div>
      </div>

      <div className="grid grid-4">
        <Stat label="Study material" value={articles.length} caption={published.length + ' published'} />
        <Stat
          label="Sample questions"
          value={sample.length}
          caption="Written for practice"
          accent="var(--brand)"
        />
        <Stat
          label="Previously asked"
          value={previous.length}
          caption={previous.length ? 'From real papers' : 'None recorded yet'}
          accent={previous.length ? 'var(--info)' : 'var(--muted)'}
        />
        <Stat
          label="Papers that can use it"
          value={usedBy.length}
          caption={usedBy.length ? 'Drawn from this topic' : 'No paper covers it yet'}
          accent={usedBy.length ? 'var(--ok)' : 'var(--danger)'}
          onClick={() => nav('/study/exams')}
        />
      </div>

      {/* ------------------------------------------------------- content */}
      <SectionHead title="Study material" sub="What a student reads for this topic">
        <button className="primary" onClick={() => setEditArticle(blank)}>New content</button>
      </SectionHead>

      {articles.length === 0 ? (
        <Empty
          title="No material yet"
          description="Students opening this topic would find nothing to read."
          actionLabel="Write the first piece"
          onAction={() => setEditArticle(blank)}
        />
      ) : (
        <div className="card table-wrap">
          <table>
            <thead>
              <tr>
                <th>Title</th>
                <th>Author</th>
                <th className="num">Read</th>
                <th>Status</th>
                <th />
              </tr>
            </thead>
            <tbody>
              {articles.map((a) => (
                <tr key={a.id}>
                  <td>
                    <div className="cell-title">{a.title}</div>
                    <div className="cell-sub">{relativeTime(a.createdAt)}</div>
                  </td>
                  <td className="cell-sub">{a.authorName}</td>
                  <td className="num">{a.readingMinutes} min</td>
                  <td><StatusPill status={a.status} /></td>
                  <td className="actions">
                    <button className="small ghost" onClick={() => setEditArticle(a)}>Edit</button>
                    <button
                      className="small danger"
                      onClick={() => setRemove({ kind: 'article', item: a })}
                    >
                      Delete
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* ------------------------------------------------------ quiz sets */}
      <SectionHead
        title="Quiz sets"
        sub="How this topic's questions are split for practice — click a set to try it"
      />

      {quizSets.length === 0 ? (
        <Empty
          title="No quiz sets yet"
          description="Sets build themselves once the topic has questions."
        />
      ) : (
        <div className="set-grid">
          {quizSets.map((set) => (
            <button
              type="button"
              key={set.id}
              className={'set-card' + (set.isPast ? ' past' : '')}
              onClick={() => setPlaying(set)}
            >
              <span className="set-count">{set.questions.length}</span>
              <span className="set-kind">{set.isPast ? 'past paper' : 'questions'}</span>
              <span className="set-title">{set.title}</span>
              <span className="cell-sub">about {set.questions.length <= 4 ? 5 : 10} min</span>
            </button>
          ))}
        </div>
      )}

      {/* ----------------------------------------------- sample questions */}
      <SectionHead title="Sample questions" sub="Practice questions written for this topic">
        <button
          className="primary"
          onClick={() => setEditQuestion({ ...blank, source: QUESTION_SOURCE.SAMPLE })}
        >
          New sample question
        </button>
      </SectionHead>

      <QuestionTable
        rows={sample}
        empty={{
          title: 'No sample questions yet',
          description: 'No paper can test this topic until the bank has questions for it.',
          action: 'Add the first question'
        }}
        onAdd={() => setEditQuestion({ ...blank, source: QUESTION_SOURCE.SAMPLE })}
        onEdit={setEditQuestion}
        onRemove={(q) => setRemove({ kind: 'question', item: q })}
      />

      {/* --------------------------------------------- previously asked */}
      <SectionHead
        title="Previously asked questions"
        sub="What real papers have asked on this topic"
      >
        <button
          className="primary"
          onClick={() => setEditQuestion({ ...blank, source: QUESTION_SOURCE.PREVIOUS })}
        >
          Add a past question
        </button>
      </SectionHead>

      <QuestionTable
        rows={previous}
        showPaper
        empty={{
          title: 'Nothing recorded yet',
          description:
            'Add questions this topic has been asked in past papers — students weight their revision by these.',
          action: 'Add a past question'
        }}
        onAdd={() => setEditQuestion({ ...blank, source: QUESTION_SOURCE.PREVIOUS })}
        onEdit={setEditQuestion}
        onRemove={(q) => setRemove({ kind: 'question', item: q })}
      />

      {editArticle ? (
        <StudyArticleEditor
          article={editArticle}
          onClose={() => setEditArticle(null)}
          onSave={(fields) => {
            dispatch({ type: 'studyArticle/save', payload: { id: editArticle.id, fields } })
            setEditArticle(null)
            notify(editArticle.id ? 'Content updated' : 'Content added to ' + topic.nameEn)
          }}
        />
      ) : null}

      {editQuestion ? (
        <QuestionEditor
          question={editQuestion}
          onClose={() => setEditQuestion(null)}
          onSave={(fields) => {
            dispatch({ type: 'question/save', payload: { id: editQuestion.id, fields } })
            setEditQuestion(null)
            notify(editQuestion.id ? 'Question updated' : 'Question added to ' + topic.nameEn)
          }}
        />
      ) : null}

      {playing ? <QuizPlayer set={playing} onClose={() => setPlaying(null)} /> : null}

      {remove ? (
        <Confirm
          title={'Delete this ' + remove.kind + '?'}
          message={
            remove.kind === 'article'
              ? '"' + remove.item.title + '" will be removed from this topic.'
              : 'This question will be removed from the bank. Papers drawing on ' +
                topic.nameEn +
                ' may come up short.'
          }
          confirmLabel="Delete"
          danger
          onClose={() => setRemove(null)}
          onConfirm={() => {
            if (remove.kind === 'article') {
              dispatch({
                type: 'studyArticle/delete',
                payload: { id: remove.item.id, title: remove.item.title }
              })
            } else {
              dispatch({ type: 'question/delete', payload: { id: remove.item.id } })
            }
            setRemove(null)
            notify(remove.kind === 'article' ? 'Content deleted' : 'Question deleted')
          }}
        />
      ) : null}
    </>
  )
}
