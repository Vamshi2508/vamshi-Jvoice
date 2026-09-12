import { Link, useNavigate } from 'react-router-dom'
import { JVoiceLogo } from '../components/Logo.jsx'
import { DemoPersonas, StaffSignInForm } from '../components/StaffSignIn.jsx'

/**
 * The console's own sign-in screen, reached from the reader's landing page or
 * directly at /login.
 *
 * The form and the persona cards live in StaffSignIn.jsx, because the landing
 * page shows the same two things underneath the news feed.
 */
export default function Login() {
  const navigate = useNavigate()
  const toConsole = () => navigate('/console', { replace: true })

  return (
    <div className="login-page">
      <div className="login-box">
        <div className="login-head">
          <JVoiceLogo width={320} />
          <h1>Admin Console</h1>
          <p>
            News (Module 1) and Study &amp; Exams (Module 2) in one place. Sign in with a staff account,
            or explore with a demo persona — each login lands on its own dashboard.
          </p>
          <p style={{ marginTop: 10 }}>
            <Link to="/">← Back to J Voice news</Link>
          </p>
        </div>

        <StaffSignInForm onSignedIn={toConsole} />

        <div className="demo-divider">
          <span>or explore with a demo persona — no account needed</span>
        </div>

        <DemoPersonas onSignedIn={toConsole} />

        <div className="demo-note" style={{ marginTop: 18 }}>
          Four dashboards, one console. <strong>Admin</strong> owns both modules end to end.
          <strong> Editor</strong> runs the news review desk, scoped to the reporters and sections assigned
          to them. <strong>Reporter</strong> files stories into that desk and tracks them, and cannot
          approve or publish anything, including their own copy. <strong>Content Creator</strong> is the
          single study role — they write and publish all study material and questions. A real account can
          hold several roles at once — see Roles for the permission matrix.
        </div>
      </div>
    </div>
  )
}
