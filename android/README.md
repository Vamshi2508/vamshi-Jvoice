# J Voice — Module 1 (News) + Module 2 (Study & Exams) Prototype

Kotlin + Jetpack Compose + Material 3 Android app covering **Module 1: News** and
**Module 2: Study & Exam Preparation**.

Everything runs on **local dummy/mock data**. There is no Firebase, no backend, no API,
no real authentication, no push notifications, no AI and no database. All content is
fabricated demo material and is labelled as such inside the app.

## Build & run

```bash
./gradlew assembleDebug          # APK -> app/build/outputs/apk/debug/app-debug.apk
./gradlew installDebug           # install on a connected device/emulator
```

Requirements: JDK 17, Android SDK 34, minSdk 24. `local.properties` must point at your SDK.

The only network use is Coil fetching placeholder demo images from `picsum.photos`.
Offline, the cards fall back to a gradient placeholder and everything still works.

## Landing page

Launch shows a **demo landing page** with a segmented switch between
**Module 1 · News** and **Module 2 · Study**. Each side shows live counts of the mock
data it holds, the workflow it demonstrates, and a row per role. Tapping a role signs
straight into it; "Switch role" anywhere returns here.

---

# Module 1 — News

## Roles

| Role | Signs in as | Entry screen |
|---|---|---|
| Reader | Sai Charan | Home feed + bottom navigation |
| Reporter | Kiran Kumar | Reporter dashboard |
| Editor | Sridevi Naidu | Editor dashboard (drawer) |
| News Admin | Anitha Reddy | Admin dashboard (drawer) |
| Super Admin | Ravi Teja Sharma | System dashboard (drawer) |

## The news workflow

`NewsRepository` is a single in-memory singleton shared by every role, so the whole
pipeline is observable live inside one run of the app:

1. **Reporter** → Create News → *Submit for Review* (status `SUBMITTED`)
2. **Editor** → Review Queue → opens it (status flips to `UNDER_REVIEW`) → edits the
   headline/description/body/category/tags → **Approve & publish**
3. The article's status becomes `PUBLISHED` and it appears **immediately** in the
   Reader's Latest feed, its category section, and search.
4. **News Admin** can then mark it breaking, pin it, unpublish it or remove it.
5. Reject / Send back write a reason onto the article and notify the reporter, who can
   edit and resubmit it (drafts, rejected and sent-back articles are editable).

Approve-only (without publishing) leaves it in `APPROVED`, which the News Admin
dashboard surfaces as "not live yet" with a Publish now action.

## Module 1 structure

```
com.jvoice.news
├── data
│   ├── model/Models.kt              User, NewsArticle, Category, Reporter,
│   │                                Notification, NewsStatus, UserRole, SystemSettings
│   ├── mock/MockData.kt             25 articles, 12 categories, 5 reporters,
│   │                                3 editors, 2 admins, 1 super admin, notifications
│   └── repository/NewsRepository.kt single shared StateFlow-backed mock repository
├── ui
│   ├── auth        SessionViewModel, LandingScreen, RoleSelectorScreen
│   ├── reader      ReaderViewModel, Home, Categories, Saved, Notifications, Profile,
│   │               Search, NewsDetail, CategoryNews
│   ├── reporter    ReporterViewModel, Dashboard, MyNews, Create/Edit News
│   ├── editor      EditorViewModel, Dashboard, ReviewQueue, ArticleReview
│   ├── admin       AdminViewModel, Dashboard, NewsManagement, CategoryManagement,
│   │               ReporterManagement
│   └── superadmin  SuperAdminViewModel, Dashboard, UserManagement, RoleManagement,
│                   SystemSettings
├── navigation      Routes, JVoiceNavGraph, NavigationShells (bottom bar + drawer)
├── components      NewsCard / FeaturedNewsCard / CompactNewsCard / WorkflowNewsRow,
│                   StatCard, StatusChip, SectionHeader, Loading/Empty/Error states,
│                   ConfirmDialog, PullToRefreshBox
├── theme           Color, Type, Theme (light + dark) — shared by both modules
└── utils           relative time, status colours, share intent
```

## Module 1 content

- Telugu + English dummy headlines, e.g. *"తెలంగాణలో కొత్త విద్యా విధానంపై కీలక నిర్ణయం"*,
  *"హైదరాబాద్‌లో భారీ వర్షాలు.."*, *"India records major growth in technology sector"*.
- 12 categories: Telangana, Andhra Pradesh, India, Politics, Crime, Business, Education,
  Technology, Sports, Entertainment, Health, Jobs.
- Seeded workflow states so every screen has data on first launch: submitted, under
  review, approved-not-published, rejected (with reason), sent back (with editor note),
  a draft, and two reader-reported articles for the moderation views.

---

# Module 2 — Study & Exam Preparation

A competitive-exam preparation module for students, with four management roles behind
it. Same rules as Module 1: local mock data only.

## Exam types come first

Tapping **Study** opens the **exam picker**, not a syllabus. The student chooses what they
are preparing for — Police Constable, SI, Group-1/2/4, VRO, SSC, RRB, Bank, DSC — and from
then on the module is that exam: its subjects in its own paper order, its topic quizzes,
its daily tests and grand tests, its results, its rank. "Change" in the top bar or
Profile → Target exam returns to the picker.

The list is **not hardcoded into the UI**. `ExamTrackRepository` owns it and a
**Study Admin / Super Admin** manages it from *Exam Types* in the admin drawer: name in
both languages, icon, category, eligibility, vacancies, exam date, duration, negative
marking, selection stages, the subject-wise pattern (questions and marks per subject) and
the important dates shown on the student's exam home.
Hiding an exam type removes it from the picker; adding one notifies students. An Exam
Admin tags each Daily Exam or Grand Test with the exam types that sit it — untagged papers
stay visible to everyone as general practice.

```
Study tab → Exam picker (Constable · Group-4 · SSC …)
          → Exam hub: pattern → subject-wise study material → topic quizzes
                      → daily tests → grand test → results → rank → weak areas
```

## Module 2 roles

| Role | Signs in as | Entry screen |
|---|---|---|
| Student | Sai Charan | Exam picker, then that exam's hub + bottom navigation |
| Content Creator | Sunitha Rao | Content dashboard (drawer) |
| Exam Admin | Mahesh Chandra | Exam dashboard (drawer) |
| Study Admin | Sridevi Naidu | Syllabus dashboard (drawer, owns Exam Types) |
| Super Admin | Ravi Teja Sharma | System dashboard (drawer) |

Drawer items are filtered by role — a Content Creator never sees exam or user
administration; the Super Admin sees everything.

## Student experience

- **Exam picker** — exams grouped by category (Police, Groups, SSC & Railways, Banking,
  Teaching) with search; each card shows the pattern, eligibility, vacancies, selection
  stages and live content counts for that exam.
- **Exam hub** — the chosen exam's home, kept short on purpose: exam name with a Change
  action, the latest **notifications**, the exam's **important dates** (notification, last
  date to apply, hall ticket, exam day — entered by an admin), a **quick-links** grid with
  live counts (Daily Exam · Grand Test · Study material · Topic quizzes · My results · My
  rank · Weak areas), and **progress at the bottom**: syllabus ring, streak, per-subject
  bars, Continue and Full dashboard.
- **Dashboard** (`study/dashboard`) — greeting, today's progress ring, today's exam with Start, Continue
  Studying, Weekly Grand Test, weak areas, recommended topics, strong subjects,
  recent results, rank and leaderboard preview, study streak.
- **Study** — 10 subjects with progress bars, topic lists, search across articles.
- **Article** — title, subject, reading time, full content, important points, key
  formulas, examples, previous/next topic, Mark done, Take Quiz.
- **Topic Quiz** — question counter, progress bar, options, next/previous, submit,
  result screen.
- **Daily Exam / Grand Test** — one exam engine drives quizzes, 20-question daily
  exams, 100-question grand tests and practice sets: a countdown timer that
  auto-submits at zero, a question palette with five visual states, mark for review,
  clear answer, previous/next and a submit confirmation showing what is unanswered.
- **Result** — score, accuracy ring, correct/wrong/skipped, time taken, subject-wise
  and topic-wise performance, rank and participants for grand tests, improvement
  versus the previous grand test, Review Answers, Study Weak Topics, Back to Home.
- **Review Answers** — every question with your choice, the correct option and the
  explanation.
- **Analysis** — subjects split into Strong 🟢 / Needs Practice 🟡 / Weak 🔴; open a
  subject for topic-level accuracy.
- **Explore Your Weak Areas** — weak subjects with their lowest topics, and a
  three-step plan per topic (read article → topic quiz → practice 20 questions).
- **Leaderboard** — Daily / Weekly / Monthly / Grand Test tabs, 200 entries per
  period, your rank pinned prominently, neighbours shown when you are outside the
  visible top.
- **My Performance** — exams, quizzes, average and best score, rank, streak, topics
  done, a 7-exam accuracy bar chart, subject accuracy, full attempt history.
- **Notifications** — daily exam, grand test, new article, result published, rank
  updated, weak-topic recommendation.

## Performance logic

`PerformanceRepository` keeps a live topic-level tally (correct/total). Subject figures
are **summed from the topic tally**, so the subject and topic views can never disagree.
Classification uses the specified rule:

```
80-100% = Strong 🟢      50-79% = Needs Practice 🟡      0-49% = Weak 🔴
```

Every submitted attempt is folded into the tally, so getting all five Mathematics
questions wrong in an exam immediately drags Mathematics towards Weak and surfaces it
in Weak Areas with a recommendation. The seed data is tuned so the demo opens with a
realistic spread: Science 92%, Geography 86% and Reasoning 83% strong; English, GK,
Polity, Economy and Current Affairs needing practice; Mathematics 40% and History 37%
weak.

## Module 2 structure

```
com.jvoice.study
├── data
│   ├── model/StudyModels.kt          ExamTrack, ExamSection, ExamTrackGroup,
│   │                                 Subject, Topic, StudyArticle, Question, Quiz,
│   │                                 Exam, ExamAttempt, ExamResult, SubjectScore,
│   │                                 TopicScore, SubjectPerformance, TopicPerformance,
│   │                                 LeaderboardEntry, StudyNotification, StudyUser,
│   │                                 Student, PerformanceBand, OverallStats
│   ├── mock/MockDataSource.kt        subjects, topics, users, exams, leaderboard,
│   │                                 seeded performance, past results, notifications
│   ├── mock/StudyContentData.kt      44 articles + 137 questions + topic quizzes
│   └── repository/                   ExamTrackRepository (the selected exam and the
│                                     scope filter), StudyRepository, ExamRepository,
│                                     PerformanceRepository, LeaderboardRepository
├── engine/ExamEngine.kt              attempt creation, navigation, grading and the
│                                     subject/topic breakdown (pure Kotlin, testable)
├── ui
│   ├── student      Exam picker, Exam hub, Dashboard, Study browse, Subject topics,
│   │                Article, Exam runner,
│   │                Result, Review, Analysis, Subject analysis, Weak areas,
│   │                Weak topic plan, Leaderboard, Performance, Notifications, Profile
│   ├── creator      Dashboard, Articles, Article editor, Questions, Question editor
│   ├── examadmin    Dashboard, Daily exams, Grand tests, Exam editor, Question bank,
│   │                Exam results
│   ├── studyadmin   Dashboard, Exam types + editor, Subjects, Topics, Content,
│   │                Question bank
│   └── superadmin   Dashboard, User management, Role permissions
├── navigation       StudyRoutes, StudyNavGraph, StudyShells (bottom bar + drawer)
├── components       ProgressRing, AccuracyBar, TrendBarChart, ScoreCard, MetricCard,
│                    ExamTimer, PaletteCell + legend, OptionRow, BandPill
└── utils            relative time, clock formatting
```

## Module 2 mock data

10 exam types · 10 subjects · 42 topics · 44 study articles · 137 questions ·
42 topic quizzes · 4 general daily exams · 2 general grand tests · one daily paper and
one grand test generated per exam type from its own section mix · 200 leaderboard entries per period ·
9 past results (7 daily exams for the trend chart plus 2 grand tests for the
improvement comparison) · 42 seeded topic performance records.

Questions are split between hand-written items across all ten subjects (each with an
explanation) and deterministically generated arithmetic and reasoning sets
(percentages, profit & loss, averages, time & work, ratio, HCF/LCM, series) whose
answers are computed rather than invented.

## Module 2 workflow to demo

```
Student → Article → Topic Quiz → Daily Exam → Result → Subject Analysis
       → Weak Topic Detection → Recommended Study → Practice Set
       → Weekly Grand Test → Leaderboard → Performance
```

Admin side:

```
Study Admin     → add an exam type (pattern, subjects, marks) → visible to students
Content Creator → create article / question (draft → submit → publish)
Study Admin     → manage subjects, topics, content, question bank
Exam Admin      → create a Daily Exam or Grand Test, tag it with an exam type, activate
Student         → the new exam appears in that exam type's Tests tab with a notification
```

Creating and activating an exam as Exam Admin, then switching to Student, shows it
live in the Exams tab — the repositories are shared singletons, so the whole chain
works inside one run of the app.

---

## UI states covered (both modules)

Loading, empty, error with retry, confirmation dialogs, snackbars, pull-to-refresh,
badges, progress rings and bars, a bar chart, dark mode (system default, toggle in
Profile) and form validation on every editor screen.

## Not implemented (by design)

Firebase (Auth / Firestore / Storage / FCM), backend APIs, real authentication,
payments, real notifications, AI or LLM calls, search APIs, production database.
State lives in memory and resets when the process is killed. The repositories are the
only things that read the mock data sources, so a real backend can be introduced
behind them without touching the UI or the business logic.
