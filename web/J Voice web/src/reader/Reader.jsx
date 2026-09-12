import { createContext, useContext, useEffect, useMemo, useState } from 'react'
import { Link, Navigate, Outlet, useNavigate, useParams } from 'react-router-dom'
import { JVoiceMark } from '../components/Logo.jsx'
import { DemoPersonas, StaffSignInForm } from '../components/StaffSignIn.jsx'
import { L, ltList } from '../i18n/localized.js'
import { useAuth } from '../store/store.jsx'
import { relativeTime } from '../components/ui.jsx'
import { byNewest, usePublicFeed } from './publicFeed.js'
import './reader.css'

/* ------------------------------------------------------------------ language */

const LANG_KEY = 'jvoice.reader.lang'

/**
 * The reading language, shared down the reader tree.
 *
 * Every content field in Firestore is a `{en, te}` pair, so switching language is
 * a render concern only — nothing is refetched. `L()` falls back to the other
 * language when one side is untranslated, which is why a Telugu-only story still
 * appears in the English feed instead of showing as a blank card.
 */
const LangContext = createContext({ lang: 'en', setLang: () => {} })
const useLang = () => useContext(LangContext)

function readStoredLang() {
  try {
    const saved = window.localStorage.getItem(LANG_KEY)
    return saved === 'te' || saved === 'en' ? saved : 'en'
  } catch {
    // Private windows and blocked site data both throw here; English is fine.
    return 'en'
  }
}

/* --------------------------------------------------------------------- shell */

const FeedContext = createContext(null)
export const useFeed = () => useContext(FeedContext)

/**
 * The public reader: header, the routed page, and the footer.
 *
 * One feed subscription lives here rather than in each page, so moving between
 * the landing page and a story re-renders from cache instead of refetching.
 */
export function ReaderShell() {
  const feed = usePublicFeed()
  const [lang, setLangState] = useState(readStoredLang)
  const { session } = useAuth()

  const setLang = (next) => {
    setLangState(next)
    try {
      window.localStorage.setItem(LANG_KEY, next)
    } catch {
      // Remembering the choice is a convenience; failing to is not an error.
    }
  }

  const langValue = useMemo(() => ({ lang, setLang }), [lang])

  return (
    <LangContext.Provider value={langValue}>
      <FeedContext.Provider value={feed}>
        <div className="reader">
          <header className="rd-top">
            <Link to="/" className="rd-brand">
              <JVoiceMark size={36} />
              <span>
                <span className="rd-brand-name">J Voice</span>
                <span className="rd-brand-sub">Telugu news &amp; exam prep</span>
              </span>
            </Link>

            <span className="rd-spacer" />

            <div className="rd-langs" role="group" aria-label="Reading language">
              <button
                className={lang === 'en' ? 'rd-lang on' : 'rd-lang'}
                onClick={() => setLang('en')}
                aria-pressed={lang === 'en'}
              >
                English
              </button>
              <button
                className={lang === 'te' ? 'rd-lang on' : 'rd-lang'}
                onClick={() => setLang('te')}
                aria-pressed={lang === 'te'}
              >
                తెలుగు
              </button>
            </div>

            {session ? (
              <Link className="rd-signin" to="/console">
                Open console
              </Link>
            ) : (
              <a className="rd-signin" href="#staff-signin">
                Staff sign in
              </a>
            )}
          </header>

          <Outlet />

          <footer className="rd-foot">
            <div className="rd-foot-in">
              <div>
                <strong>J Voice</strong> — Telugu news and competitive-exam preparation.
              </div>
              <span className="rd-spacer" />
              <Link to="/login">Staff &amp; admin console</Link>
            </div>
          </footer>
        </div>
      </FeedContext.Provider>
    </LangContext.Provider>
  )
}

/* --------------------------------------------------------------------- cards */

function Thumb({ article, tall }) {
  const { lang } = useLang()
  if (!article.imageUrl) {
    return (
      <div className={tall ? 'rd-thumb rd-thumb-tall rd-thumb-blank' : 'rd-thumb rd-thumb-blank'}>
        <JVoiceMark size={40} />
      </div>
    )
  }
  return (
    <img
      className={tall ? 'rd-thumb rd-thumb-tall' : 'rd-thumb'}
      src={article.imageUrl}
      alt={L(article.headline, lang)}
      loading="lazy"
    />
  )
}

/**
 * When a story went out.
 *
 * Returns null rather than a date for a story with no timestamp: `relativeTime(0)`
 * renders "01 Jan 1970", and a wrong date on a byline is worse than none.
 */
function whenLabel(article) {
  const at = article.publishedAt || article.createdAt
  return at ? relativeTime(at) : null
}

function Flags({ article }) {
  return (
    <>
      {article.isBreaking ? <span className="rd-flag breaking">Breaking</span> : null}
      {article.isTrending ? <span className="rd-flag trending">Trending</span> : null}
    </>
  )
}

function ArticleCard({ article, categoryName, featured }) {
  const { lang } = useLang()
  return (
    <Link className={featured ? 'rd-card rd-card-lead' : 'rd-card'} to={`/read/${article.id}`}>
      <Thumb article={article} tall={featured} />
      <div className="rd-card-body">
        <div className="rd-card-meta">
          <span className="rd-cat">{categoryName(article.categoryId)}</span>
          <Flags article={article} />
        </div>
        <h3>{L(article.headline, lang)}</h3>
        <p>{L(article.shortDescription, lang)}</p>
        <div className="rd-byline">
          {article.reporterName ? <span>{article.reporterName}</span> : null}
          {article.location ? <span>· {article.location}</span> : null}
          {whenLabel(article) ? <span>· {whenLabel(article)}</span> : null}
        </div>
      </div>
    </Link>
  )
}

/* ------------------------------------------------------------------ landing */

export function ReaderHome() {
  const feed = useFeed()
  const { lang } = useLang()
  const { session } = useAuth()
  const navigate = useNavigate()
  const [categoryId, setCategoryId] = useState('all')

  // Signing in from the landing page means "take me to work", so go straight to
  // the console rather than leaving the reader open behind a changed banner.
  const toConsole = () => navigate('/console')

  const categories = useMemo(
    () => feed.categories.filter((c) => c.isEnabled !== false),
    [feed.categories]
  )
  const categoryName = (id) => L(categories.find((c) => c.id === id)?.name, lang) || 'General'

  const sorted = useMemo(() => [...feed.articles].sort(byNewest), [feed.articles])
  const shown = categoryId === 'all' ? sorted : sorted.filter((a) => a.categoryId === categoryId)

  const breaking = sorted.filter((a) => a.isBreaking).slice(0, 4)
  // The lead is the newest featured story, and the plain newest when nothing is
  // featured — a landing page with an empty hero would be worse than either.
  const lead = shown.find((a) => a.isFeatured) ?? shown[0] ?? null
  const rest = lead ? shown.filter((a) => a.id !== lead.id) : shown

  return (
    <main className="rd-main">
      {breaking.length ? (
        <div className="rd-breaking">
          <span className="rd-breaking-tag">Breaking</span>
          <div className="rd-breaking-list">
            {breaking.map((a) => (
              <Link key={a.id} to={`/read/${a.id}`}>
                {L(a.headline, lang)}
              </Link>
            ))}
          </div>
        </div>
      ) : null}

      <section className="rd-section">
        <div className="rd-chips">
          <button
            className={categoryId === 'all' ? 'rd-chip on' : 'rd-chip'}
            onClick={() => setCategoryId('all')}
          >
            All news
          </button>
          {categories.map((c) => (
            <button
              key={c.id}
              className={categoryId === c.id ? 'rd-chip on' : 'rd-chip'}
              onClick={() => setCategoryId(c.id)}
            >
              <span>{c.emoji}</span> {L(c.name, lang)}
            </button>
          ))}
        </div>

        {feed.status === 'loading' ? (
          <div className="rd-note">Loading the latest from J Voice…</div>
        ) : feed.status === 'off' ? (
          <div className="rd-note">
            This build has no Firebase configuration, so there is nothing to read.
          </div>
        ) : !shown.length ? (
          <div className="rd-note">
            No published stories in this section yet. Editors publish from the console.
          </div>
        ) : (
          <>
            {lead ? <ArticleCard article={lead} categoryName={categoryName} featured /> : null}
            <div className="rd-grid">
              {rest.map((a) => (
                <ArticleCard key={a.id} article={a} categoryName={categoryName} />
              ))}
            </div>
          </>
        )}
      </section>

      <StudyStrip />

      <section className="rd-signin-band" id="staff-signin">
        <div className="rd-signin-in">
          <div className="rd-signin-copy">
            <h2>Working at J Voice?</h2>
            <p>
              Reporters, editors, content creators and admins sign in here. The console is where
              stories are filed, reviewed and published, and where the study syllabus, question bank
              and exams are built — everything above is what readers see once it goes live.
            </p>
            {session ? (
              <Link className="rd-cta" to="/console">
                You are signed in as {session.name} — open the console
              </Link>
            ) : null}
          </div>

          <div className="rd-signin-form">
            {session ? null : <StaffSignInForm onSignedIn={toConsole} />}
          </div>
        </div>

        {session ? null : (
          <div className="rd-signin-demo">
            <div className="demo-divider">
              <span>or explore the console with a demo persona — no account needed</span>
            </div>
            <DemoPersonas compact onSignedIn={toConsole} />
          </div>
        )}
      </section>
    </main>
  )
}

/** Subjects and exam tracks, both public by rule — the study side, at a glance. */
function StudyStrip() {
  const feed = useFeed()
  const { lang } = useLang()
  if (!feed.subjects.length && !feed.tracks.length) return null

  const subjects = feed.subjects.filter((s) => s.isEnabled !== false)
  const tracks = feed.tracks.filter((t) => t.isEnabled !== false)
  const topicCount = (subjectId) => feed.topics.filter((t) => t.subjectId === subjectId).length
  const materialCount = (subjectId) =>
    feed.studyArticles.filter((a) => a.subjectId === subjectId).length

  return (
    <section className="rd-section rd-study">
      <h2>Study &amp; exams</h2>
      <p className="rd-study-sub">
        Free syllabus, notes and practice for Telangana and Andhra Pradesh competitive exams — read
        them in full in the J Voice app.
      </p>

      {tracks.length ? (
        <div className="rd-tracks">
          {tracks.map((t) => (
            <div className="rd-track" key={t.id}>
              <div className="rd-track-top">
                <span className="rd-track-emoji">{t.emoji}</span>
                <strong>{t.shortName || L(t.name, lang)}</strong>
              </div>
              <div className="rd-track-name">{L(t.name, lang)}</div>
              <div className="rd-track-meta">
                {t.totalQuestions ? <span>{t.totalQuestions} questions</span> : null}
                {t.durationMinutes ? <span>· {t.durationMinutes} min</span> : null}
              </div>
            </div>
          ))}
        </div>
      ) : null}

      {subjects.length ? (
        <div className="rd-subjects">
          {subjects.map((s) => (
            <div className="rd-subject" key={s.id}>
              <span className="rd-subject-emoji">{s.emoji}</span>
              <div>
                <div className="rd-subject-name">{L(s.name, lang)}</div>
                <div className="rd-subject-meta">
                  {topicCount(s.id)} topics · {materialCount(s.id)} notes
                </div>
              </div>
            </div>
          ))}
        </div>
      ) : null}
    </section>
  )
}

/* ------------------------------------------------------------------ article */

export function ReaderArticle() {
  const { id } = useParams()
  const feed = useFeed()
  const { lang } = useLang()

  const article = feed.articles.find((a) => a.id === id)

  // Scroll to the top on navigation — the router keeps the scroll position, which
  // otherwise drops the reader halfway down a story they have not started.
  useEffect(() => {
    window.scrollTo(0, 0)
  }, [id])

  if (feed.status === 'loading') {
    return (
      <main className="rd-main">
        <div className="rd-note">Loading…</div>
      </main>
    )
  }
  // Missing once the feed has settled means unpublished, deleted, or a bad link.
  if (!article) return <Navigate to="/" replace />

  const categoryName =
    L(feed.categories.find((c) => c.id === article.categoryId)?.name, lang) || 'General'
  const paragraphs = L(article.content, lang)
    .split(/\n\s*\n/)
    .map((p) => p.trim())
    .filter(Boolean)
  const tags = ltList(article.tags, lang)

  const related = feed.articles
    .filter((a) => a.id !== article.id && a.categoryId === article.categoryId)
    .sort(byNewest)
    .slice(0, 3)

  return (
    <main className="rd-main rd-read">
      <Link className="rd-back" to="/">
        ← All news
      </Link>

      <article className="rd-article">
        <div className="rd-card-meta">
          <span className="rd-cat">{categoryName}</span>
          <Flags article={article} />
        </div>

        <h1>{L(article.headline, lang)}</h1>
        <p className="rd-standfirst">{L(article.shortDescription, lang)}</p>

        <div className="rd-byline rd-byline-lg">
          {article.reporterAvatarUrl ? (
            <img className="rd-avatar" src={article.reporterAvatarUrl} alt="" />
          ) : null}
          {article.reporterName ? <span>{article.reporterName}</span> : null}
          {article.location ? <span>· {article.location}</span> : null}
          {whenLabel(article) ? <span>· {whenLabel(article)}</span> : null}
        </div>

        {article.imageUrl ? (
          <img className="rd-hero" src={article.imageUrl} alt={L(article.headline, lang)} />
        ) : null}

        <div className="rd-body">
          {paragraphs.length ? (
            paragraphs.map((p, i) => <p key={i}>{p}</p>)
          ) : (
            <p className="rd-note">This story has no body in the selected language yet.</p>
          )}
        </div>

        {article.photoUrls?.length ? (
          <div className="rd-photos">
            {article.photoUrls.map((url) => (
              <img key={url} src={url} alt="" loading="lazy" />
            ))}
          </div>
        ) : null}

        {tags.length ? (
          <div className="rd-tags">
            {tags.map((t) => (
              <span className="rd-tag" key={t}>
                #{t}
              </span>
            ))}
          </div>
        ) : null}
      </article>

      {related.length ? (
        <section className="rd-section">
          <h2>More in {categoryName}</h2>
          <div className="rd-grid">
            {related.map((a) => (
              <ArticleCard key={a.id} article={a} categoryName={() => categoryName} />
            ))}
          </div>
        </section>
      ) : null}
    </main>
  )
}
