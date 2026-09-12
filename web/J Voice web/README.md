# J Voice — Admin Console (web)

React + Vite web console for **both** modules: News (Module 1) and Study &amp; Exams
(Module 2). Every management action from the Android app is here, driven by the same
kind of local mock data — no backend, no API, no authentication.

## Run it

```bash
cd "J Voice web"
npm install
npm run dev        # http://localhost:5173
npm run build      # production bundle in dist/
npm run preview    # serve the built bundle
```

Node 18+ required.

## Four personas

Either sign in with a real staff account, or pick a demo persona on the landing
screen — those need no password.

| Persona | Scope | What it can do |
|---|---|---|
| **Admin** (Ravi Teja Sharma) | News + Study | Everything: news pipeline, categories, reporters, AI shorts, exam types, syllabus, content, question bank, exams, results, users, roles, settings |
| **Editor** (Sridevi Naidu) | News only | Review queue: edit copy, approve, publish, approve-only, reject with a reason, send back with a note |
| **Reporter** (Kiran Kumar) | News only | File a story as a draft or into the review queue, track their own filings, read a rejection reason or send-back note |
| **Content Creator** (Sunitha Rao) | Study only | Write, edit and publish study material and questions for their subjects |

The Android app's separate **News Admin, Study Admin, Exam Admin and Super Admin** roles
are collapsed into the single console **Admin** — one account owns both modules. Routes a
persona cannot use redirect back to the dashboard, and the sidebar hides them too.

A **Reporter** files into the desk and can never judge it: approve, publish, breaking and
pin are absent from their compose form, and `/news/review` refuses them by URL as well as
hiding itself from their sidebar. That guard is explicit rather than implied by the
sidebar — see `App.jsx`.

## Screens

**Overview**
- **Dashboard** — news pipeline counters (queue, published, approved-not-live, reader
  reports, drafts, rejections), study counters (exam types, subjects, questions, active
  tests, content awaiting review, attempts), AI shorts counters, the next stories in the
  queue and a session activity log. Editors see the news half only.

**News**
- **Review queue** — submitted and under-review stories, oldest first. Opening one flips it
  to `Under Review` exactly as the app does, then: edit headline / description / body /
  category / tags → **Approve & publish**, **Approve only**, **Reject** (reason), **Send
  back** (note). Previous rejection reasons and editor notes are shown inline.
- **All news** — every article in any status, filtered by status, category and text.
  Publish an approved story, unpublish a live one, mark breaking, pin, clear reader
  reports, remove, and open the full body. **New article** is the data-entry screen:
  headline, description, body, category, reporter credit, tags, breaking and pin
  switches, then Save draft · Send to review queue · Publish now.
- **Categories** — add, edit (English / Telugu / icon), show or hide, delete, with the
  article count per category.
- **Reporters** — accounts with stories written and published, activate or deactivate,
  edit name / email / location.
- **AI Shorts** — the news-to-video pipeline: draft a short from a published story,
  generate the script, edit scenes (text, seconds, re-suggest media, add or delete), pick
  template / language / voice, render, approve, publish, unpublish, retry a failed render,
  delete. Plus the video template list with enable / disable and a default.

**Study**
- **Exam types** — the list a student picks from before any syllabus is shown. Grouped as
  the app's picker groups them. Create or edit: names in both languages, short name, icon,
  category, description, qualification, age limit, vacancies, exam-date label, selection
  stages, duration, negative marking and the **subject-wise pattern** (questions and marks
  per subject, totals live). Show or hide, delete (blocked while papers are tagged to it).
- **Subjects** — topics / articles / questions per subject, which exam types use it,
  student accuracy with its band, enable / disable, delete (blocked while topics exist).
- **Topics** — filter by subject; difficulty, article and question counts, enable /
  disable, edit (rename, reassign subject, change difficulty), delete (blocked while
  content references it).
- **Study material** — the data-entry screen for what students read. **New study article**
  (or Edit on any row) opens the writing screen: title, subject, topic, reading time, short
  description, full body, and the study aids — important points, formulas and solved
  examples, one per line. Save as a draft, send it for review, or publish straight to
  students. The list then filters by status with publish, send back to draft, delete and a
  read-back view showing the article as the app renders it.
- **Question bank** — filter by subject, difficulty and text. Add or edit a question with
  options, the correct answer, difficulty and an explanation; delete.
- **Exams & tests** — daily exams and grand tests, filtered by exam type or general
  practice. Create or edit a paper (title, date label, duration, question count, subjects,
  difficulty, instructions), **tag it with the exam types that sit it**, fill it from an
  exam type's pattern in one click, activate / deactivate, delete.
- **Results & ranks** — every attempt with score, accuracy band, rank and time taken,
  filtered by exam type, plus subject accuracy across all students.

**System**
- **Users** — all accounts across both modules: search, filter by role, change role
  inline, activate / deactivate, add or edit.
- **Roles** — what each console login can do, and what each mobile app role does.
- **Settings** — app name, default language, feature switches (breaking alerts,
  auto-publish on approval, comments, clips, AI shorts, maintenance mode) and the session
  activity log.

## Structure

```
J Voice web/
├── index.html
├── vite.config.js
└── src/
    ├── main.jsx            React root + router + providers
    ├── App.jsx             routes, auth gate, AdminOnly guard
    ├── styles.css          light/dark theme, layout, tables, forms, modals
    ├── data/
    │   ├── newsData.js     categories, articles (all statuses), users, settings
    │   ├── studyData.js    exam types with patterns, subjects, topics, articles,
    │   │                   questions, exams, results, seeded accuracy
    │   └── shortsData.js   AI shorts, templates, voices, languages
    ├── store/store.jsx     one reducer holding all state + every action, auth,
    │                       toasts, selectors, activity log
    ├── components/
    │   ├── Layout.jsx      role-filtered sidebar, top bar, toast host
    │   └── ui.jsx          Stat, StatusPill, Pill, Modal, Confirm, ReasonDialog,
    │                       Field, Switch, Chips, Bar, Empty, SectionHead, helpers
    └── pages/
        ├── Login.jsx  Dashboard.jsx
        ├── News.jsx        review queue, article review, all news, categories, reporters
        ├── Shorts.jsx      AI shorts + templates
        ├── Study.jsx       exam types + editor, subjects, topics,
        │                   study material + article editor
        ├── Exams.jsx       question bank, exams + editor, results
        └── System.jsx      users, roles, settings
```

## Verify it end to end

With the dev server running:

```bash
npm run dev      # terminal 1
npm run smoke    # terminal 2 — drives headless Chromium, writes screenshots/
```

`smoke.mjs` signs in as Admin, walks every screen, reviews and publishes a story, opens
the exam-type pattern editor, **writes a new study article and publishes it**, edits an
existing one, then signs in as Editor to check the news-only sidebar and that an
admin-only route redirects. Along the way it also **writes a news story and publishes
it**. It prints any console error it sees and leaves 25 screenshots in `screenshots/`.

## Notes

- All state is in memory. Reloading the page restores the seed data — nothing is persisted.
- Actions are wired end to end: publishing a story moves it out of the queue and into the
  published list, tagging a paper with an exam type changes who would see it, hiding an
  exam type removes it from what students could pick.
- Every action writes a line into the session activity log (dashboard and Settings), so a
  demo can show what was changed.
- Guard rails match the app: an exam type with papers, a subject with topics and a topic
  with content all refuse deletion.
