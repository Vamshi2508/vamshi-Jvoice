import { NavLink, Outlet, useLocation } from 'react-router-dom'
import { useAuth, useSelectors, useStore, useToast } from '../store/store.jsx'
import { JVoiceMark } from './Logo.jsx'

/**
 * The sidebar, grouped by module.
 *
 * Every item names the roles that may see it, and the sidebar filters on the
 * signed-in role rather than hiding routes behind a redirect alone — a link to a
 * page that bounces you back to the dashboard is worse than no link.
 */
const NAV = [
  {
    label: 'Overview',
    items: [
      { to: '/console', label: 'Dashboard', icon: '▦', roles: ['Admin', 'Editor', 'Content Creator', 'Reporter'] }
    ]
  },
  {
    label: 'News',
    items: [
      { to: '/news/mine', label: 'My stories', icon: '🖋️', roles: ['Reporter'] },
      { to: '/news/review', label: 'Review queue', icon: '📝', roles: ['Admin', 'Editor'], badge: 'queue' },
      { to: '/news/articles', label: 'All news', icon: '📰', roles: ['Admin'] },
      { to: '/news/categories', label: 'Categories', icon: '🗂️', roles: ['Admin'] },
      { to: '/news/reporters', label: 'Reporters', icon: '🧑‍💼', roles: ['Admin'] },
      { to: '/news/editors', label: 'Editors', icon: '🖊️', roles: ['Admin'] },
      { to: '/news/shorts', label: 'AI Shorts', icon: '🎬', roles: ['Admin'] }
    ]
  },
  {
    label: 'Study',
    items: [
      { to: '/study/exam-types', label: 'Exam types', icon: '🎯', roles: ['Admin'] },
      { to: '/study/subjects', label: 'Subjects', icon: '📘', roles: ['Admin'] },
      { to: '/study/topics', label: 'Topics', icon: '🔖', roles: ['Admin'] },
      { to: '/study/content', label: 'Study material', icon: '📄', roles: ['Admin', 'Content Creator'], badge: 'pending' },
      { to: '/study/questions', label: 'Question bank', icon: '❓', roles: ['Admin', 'Content Creator'] },
      { to: '/study/exams', label: 'Exams & tests', icon: '🗓️', roles: ['Admin'] },
      { to: '/study/results', label: 'Results & ranks', icon: '🏆', roles: ['Admin'] },
      { to: '/study/creators', label: 'Content creators', icon: '✍️', roles: ['Admin'] }
    ]
  },
  {
    label: 'System',
    items: [
      { to: '/system/users', label: 'Users', icon: '👥', roles: ['Admin'] },
      { to: '/system/roles', label: 'Roles', icon: '🛡️', roles: ['Admin'] },
      { to: '/system/settings', label: 'Settings', icon: '⚙️', roles: ['Admin'] },
      { to: '/system/flags', label: 'App flags', icon: '🚩', roles: ['Admin'] }
    ]
  }
]

/** What each role's console actually covers, shown under their name. */
const ROLE_SCOPE = {
  Admin: 'News + Study',
  Editor: 'News desk',
  'Content Creator': 'Study authoring',
  Reporter: 'Field reporting'
}

/** Page heading and subtitle by route, so each screen does not repeat itself. */
const TITLES = {
  '/console': ['Dashboard', 'Your work at a glance'],
  '/news/mine': ['My stories', 'What you have filed, and what the desk did with it'],
  '/news/review': ['Review queue', 'Submitted stories waiting on an editor'],
  '/news/articles': ['All news', 'Everything in the pipeline, any status'],
  '/news/categories': ['Categories', 'Sections readers browse'],
  '/news/reporters': ['Reporters', 'Create, edit, suspend or delete field accounts'],
  '/news/editors': ['Editors', 'Create, edit, suspend or delete review-queue accounts'],
  '/news/shorts': ['AI Shorts', 'News turned into short video'],
  '/study/exam-types': ['Exam types', 'What students can prepare for — open one to walk its syllabus'],
  '/study/subjects': ['Subjects', 'Syllabus top level'],
  '/study/topics': ['Topics', 'Chapters inside each subject — content and questions live under these'],
  '/study/content': ['Study material', 'Write, review and publish what students read'],
  '/study/questions': ['Question bank', 'Every question available to exams'],
  '/study/exams': ['Exams & tests', 'Daily exams and grand tests per exam type'],
  '/study/results': ['Results & ranks', 'Student attempts and leaderboards'],
  '/study/creators': ['Content creators', 'The study team — they write and publish all material'],
  '/system/users': ['Users', 'Every account across both modules — full create, edit and delete'],
  '/system/roles': ['Roles', 'Full permission matrix — tick what each role can do'],
  '/system/settings': ['Settings', 'Platform configuration'],
  '/system/flags': ['App flags', 'Live switches for what the Android app shows']
}

export default function Layout() {
  const { session, signOut } = useAuth()
  const { state } = useStore()
  const { reviewQueue } = useSelectors()
  const { toast } = useToast()
  const location = useLocation()

  const pendingStudy = state.studyArticles.filter((a) => a.status === 'Pending Review').length
  const badges = { queue: reviewQueue.length, pending: pendingStudy }
  const [title, sub] = TITLES[location.pathname] || ['J Voice', 'Admin console']

  return (
    <div className="shell">
      <aside className="sidebar">
        <div className="brand">
          <JVoiceMark size={34} />
          <div>
            <div className="brand-name">J Voice</div>
            <div className="brand-sub">Admin console</div>
          </div>
        </div>

        {NAV.map((group) => {
          const items = group.items.filter((item) => item.roles.includes(session.role))
          if (!items.length) return null
          return (
            <div className="nav-group" key={group.label}>
              <div className="nav-label">{group.label}</div>
              {items.map((item) => (
                <NavLink
                  key={item.to}
                  to={item.to}
                  end={item.to === '/console'}
                  className={({ isActive }) => 'nav-item' + (isActive ? ' active' : '')}
                >
                  <span>{item.icon}</span>
                  <span>{item.label}</span>
                  {item.badge && badges[item.badge] > 0 ? (
                    <span className="badge">{badges[item.badge]}</span>
                  ) : null}
                </NavLink>
              ))}
            </div>
          )
        })}

        <div className="nav-group">
          <div className="nav-label">Session</div>
          {/* The reader is a separate app on the same origin; a plain link keeps
              the console out of its layout rather than nesting the two. */}
          <NavLink to="/" className="nav-item">
            <span>🌐</span>
            <span>View public site</span>
          </NavLink>
          <div className="nav-item" onClick={signOut}>
            <span>⎋</span>
            <span>Sign out</span>
          </div>
        </div>
      </aside>

      <div className="main">
        <div className="topbar">
          <div>
            <h1>{title}</h1>
            <div className="sub">{sub}</div>
          </div>
          <div className="spacer" />
          <div className="who">
            <div className="name">{session.name}</div>
            <div className="role">{session.role} · {ROLE_SCOPE[session.role] || 'Console'}</div>
          </div>
        </div>
        <div className="content">
          <Outlet />
        </div>
      </div>

      {toast ? <div className="toast">{toast.message}</div> : null}
    </div>
  )
}
