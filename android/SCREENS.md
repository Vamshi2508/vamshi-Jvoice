# J Voice — Screen Catalogue

Every screen in the app, with its route, source file, the role that reaches it and
what it contains. All data is local mock data — no Firebase, no backend, no API.

**Totals:** 63 screen composables — 26 in Module 1 (News), 37 in Module 2 (Study).
Four of them are reused across contexts (noted inline), so the app presents 60+
distinct states.

---

## Legend

| Symbol | Meaning |
|---|---|
| 🏠 | Reader pill-bar tab |
| 🗄️ | Reached from a navigation drawer |
| ↪️ | Pushed on top of another screen |
| 🔁 | One composable reused in more than one context |

---

# Shell

| # | Screen | File | Reached from |
|---|---|---|---|
| 0.1 | **Landing Page** | `news/ui/auth/LandingScreen.kt` | App launch |
| 0.2 | **Role Selector** | `news/ui/auth/RoleSelectorScreen.kt` | Landing → "Open the full role selector" |

### 0.1 Landing Page
The entry screen and module switcher.
- Hero header: J Voice logo, "మీ వార్తలు • మీ చదువు", demo-build note.
- Segmented switch: **📰 Module 1 · News** / **📚 Module 2 · Study**.
- Live mock-data counts for the selected module (News: articles, published,
  categories, users — Study: subjects, topics, articles, questions).
- A 4-step workflow card for the selected module.
- One row per role: name in English + Telugu, what the role does, which demo user it
  signs in as. Tapping a row enters that role directly.
- Footer disclaimer: demo content, no Firebase, no backend.

### 0.2 Role Selector
The original Module 1 selector, kept intact. Five role cards with icons,
descriptions and demo account names. System back returns to the landing page.

---

# MODULE 1 — NEWS 📰

## Reader

The reader runs on a **floating pill bar** with four tabs — **News · Clips · Study ·
Profile** — modelled on Netflix's bar. Search lives in the News top bar; Categories,
Saved and Notifications moved into Profile to make room.

| # | Screen | Nav | Route | File |
|---|---|---|---|---|
| 1.1 | News (flip cards) | 🏠 | `reader/home` | `ui/reader/NewsFlipScreen.kt` |
| 1.2 | Clips (short video) | 🏠 | `reader/clips` | `ui/reader/VideoClipsScreen.kt` |
| 1.3 | Study | 🏠 🔁 | `study/home` | `study/ui/student/StudentHomeScreen.kt` |
| 1.4 | Profile | 🏠 | `reader/profile` | `ui/reader/ReaderTabScreens.kt` |
| 1.5 | Classic Feed | ↪️ | `reader/feed` | `ui/reader/ReaderHomeScreen.kt` |
| 1.6 | Search | ↪️ | `reader/search` | `ui/reader/SearchScreen.kt` |
| 1.7 | News Detail | ↪️ | `reader/detail/{articleId}` | `ui/reader/NewsDetailScreen.kt` |
| 1.8 | Categories | ↪️ | `reader/categories` | `ui/reader/ReaderTabScreens.kt` |
| 1.9 | Category Feed | ↪️ | `reader/category/{categoryId}` | `ui/reader/ReaderTabScreens.kt` |
| 1.10 | Saved News | ↪️ | `reader/saved` | `ui/reader/ReaderTabScreens.kt` |
| 1.11 | Notifications | ↪️ | `reader/notifications` | `ui/reader/ReaderTabScreens.kt` |

**1.1 News — flip cards.** The default reader experience: one published article per
full-screen card, swiped vertically.
- Top bar: J Voice wordmark, location dropdown (11 locations), classic-feed toggle,
  search, notification bell with unread badge.
- **Filter chip row** beneath the top bar: **All** plus every category that actually
  has published news, each with its emoji. Selecting one filters the deck and the
  counter re-bases to the filtered set.
- Card: image (top 46%), breaking badge, category pill, relative time, headline,
  short description, an opening paragraph pulled from the article body, then
  byline · location · view count.
- Actions: **Read full story**, bookmark, share (OS share sheet).
- Card counter top-right; "Swipe up for the next story" hint on the first card.
- Empty state per filter with a "Clear filter" action; loading state on first open.

**1.2 Clips — short video.** Vertical short-form video feed.
- Full-bleed portrait thumbnail with legibility scrims; tap anywhere to pause or
  resume, play glyph shown while paused.
- Right-hand action rail: like (with count), save, share, mute.
- Caption block: breaking badge, category pill, duration, title, description,
  reporter · location · views · time, and **Read the full story** linking through to
  the related article.
- Transport progress bar pinned to the bottom edge.
- Its own translucent filter chips floating over the video (All + categories in use).
- 12 dummy clips, Telugu and English titles, 38–95 second durations.
- **Playback is simulated.** There is no media stream or bundled video in this build;
  the transport advances in real time over each clip's stated duration. The card
  states this on screen. `NewsClip.videoUrl` already exists, so a real player can be
  dropped in behind the thumbnail without touching the chrome.

**1.3 Study** 🔁 — Module 2's Student home, mounted *inside* the News shell so the
pill bar stays visible and "Study" behaves as a tab rather than a module takeover.
Every Student destination is reachable from here (see Module 2 §6). Detail in §6.1.

**1.4 Profile** — avatar, name, email, role pill; stat row (saved count, location,
member since); a **My news** section linking to Saved news, Categories and
Notifications; preference toggles for dark mode, push alerts and Telugu-first; about
row; "Switch role" with confirmation.

**1.5 Classic Feed** — the original sectioned home, preserved and reachable from the
News top bar: demo-data bar, Breaking News hero rail, Trending Now ranked rail,
Latest News list, and up to six per-category rails each with "See all".
Pull-to-refresh, loading / empty / error states.

**1.6 Search** — local search over title, description, category (English *and*
Telugu), location, tags and reporter name; category filter chips; location filter
chips; result count; empty-search and no-results states with "clear filters".

**1.7 News Detail** — category pill, breaking badge, large headline, standfirst,
16:9 image, reporter byline with avatar initial, location, full timestamp, view
count, article body, tag chips, Related News rail (scored by category + location +
tag overlap) and "More in <category>" rail. Actions: report, OS share sheet, bookmark.

**1.8 Categories** — 2-column grid of the 12 enabled categories with emoji, English
and Telugu names, and a live article count.

**1.9 Category Feed** — every published article in one category, bookmark toggles.

**1.10 Saved News** — locally bookmarked articles, "clear all" with confirmation.

**1.11 Notifications** — role-filtered list with type-coloured icons, relative
timestamps, NEW pills, mark-all-read, tap to open the linked article.

## Reporter

| # | Screen | Nav | Route | File |
|---|---|---|---|---|
| 2.1 | Reporter Dashboard | entry | `reporter/dashboard` | `ui/reporter/ReporterScreens.kt` |
| 2.2 | My News | ↪️ | `reporter/my_news` | `ui/reporter/ReporterScreens.kt` |
| 2.3 | Create / Edit News | ↪️ | `reporter/editor?articleId=` | `ui/reporter/CreateNewsScreen.kt` |

**2.1 Reporter Dashboard** — 4 stat cards (total submitted, pending review, approved,
rejected) plus a 3-up row (drafts, published live, sent back); recent articles with
status chips; desk updates feed; "Create News" FAB; pull-to-refresh.

**2.2 My News** — filter chips for all seven workflow statuses with live counts;
workflow rows showing status, breaking/pinned badges, byline, category, location,
last-updated, rejection reason and editor note; delete-draft with confirmation; only
drafts, rejected and sent-back articles open for editing (snackbar otherwise).

**2.3 Create / Edit News** — headline, short description, full article (with character
counter), category chips, location chips, tags, image URL with live preview, Breaking
News toggle. Buttons: **Save Draft** and **Submit** (with a confirmation dialog).
Per-field validation appears after the first submit attempt.

## Editor

| # | Screen | Nav | Route | File |
|---|---|---|---|---|
| 3.1 | Editor Dashboard | 🗄️ | `editor/dashboard` | `ui/editor/EditorScreens.kt` |
| 3.2 | Review Queue | 🗄️ | `editor/queue` | `ui/editor/EditorScreens.kt` |
| 3.3 | Article Review | ↪️ 🔁 | `editor/review/{articleId}` | `ui/editor/ArticleReviewScreen.kt` |

**3.1 Editor Dashboard** — today's counters (awaiting review, approved today,
rejected today, published today); the queue's first four items; recently handled list.

**3.2 Review Queue** — everything in `SUBMITTED` or `UNDER_REVIEW`, oldest first, with
submitted-time and location.

**3.3 Article Review** — opening it flips the article to `UNDER_REVIEW`. Editable
headline, description and body; category chips; tag add/remove chips. Four decisions:
**Send back** and **Reject** (each requires a reason in a dialog), **Approve only**,
**Approve & publish**. 🔁 The News Admin also opens this screen to edit an article.

## News Admin

| # | Screen | Nav | Route | File |
|---|---|---|---|---|
| 4.1 | Admin Dashboard | 🗄️ | `admin/dashboard` | `ui/admin/AdminScreens.kt` |
| 4.2 | News Management | 🗄️ | `admin/news` | `ui/admin/AdminScreens.kt` |
| 4.3 | Category Management | 🗄️ | `admin/categories` | `ui/admin/AdminScreens.kt` |
| 4.4 | Reporter Management | 🗄️ | `admin/reporters` | `ui/admin/AdminScreens.kt` |

**4.1 Admin Dashboard** — people counters (users, reporters, editors) and content
counters (total, published, pending, reported); an alert card when approved articles
are not live yet; quick-action rows.

**4.2 News Management** — search by headline / reporter / location, category filter,
status filter. Per-article ⋮ menu: open in review, toggle breaking, pin/unpin,
publish now (approved only), unpublish (published only), remove with confirmation.

**4.3 Category Management** — add, edit (English/Telugu/emoji), enable-disable switch,
delete. Deletion is blocked while articles still use the category, with a snackbar
explaining why.

**4.4 Reporter Management** — per-reporter card: name, beat, assigned location, active
pill, four mini-stats (articles, approved, rejected, pending), change-location menu,
account enable/disable switch.

## Super Admin

| # | Screen | Nav | Route | File |
|---|---|---|---|---|
| 5.1 | System Dashboard | 🗄️ | `super/dashboard` | `ui/superadmin/SuperAdminScreens.kt` |
| 5.2 | User Management | 🗄️ | `super/users` | `ui/superadmin/SuperAdminScreens.kt` |
| 5.3 | Role Management | 🗄️ | `super/roles` | `ui/superadmin/SuperAdminScreens.kt` |
| 5.4 | System Settings | 🗄️ | `super/settings` | `ui/superadmin/SuperAdminScreens.kt` |

**5.1 System Dashboard** — people counters (users, reporters, editors, admins) and
content counters (total, published, pending, rejected); system navigation rows.

**5.2 User Management** — search by name / email / location, role filter chips.
Per-user ⋮ menu: edit details (dialog), activate/deactivate (confirmation for
deactivate), change role (menu with a tick on the current role).

**5.3 Role Management** — read-only permission matrix for all five news roles, each
with its Telugu label and permission list.

**5.4 System Settings** — six groups: app settings, news categories, breaking news
(banner toggle + auto-expiry stepper), notifications, user roles, content moderation
(auto-moderation, profanity filter, comments, auto-hide report threshold).

> **Super Admin only:** the drawer also carries a **📚 Study module** item that jumps
> straight into Module 2 at the same rank.

---

# MODULE 2 — STUDY & EXAM PREPARATION 📚

## Student

Reachable two ways: as the **Study tab** inside the News pill bar, or by entering the
Study module directly from the landing page — in which case the student gets its own
five-tab bar (Home · Study · Exams · Ranks · Profile). The destinations are the same
either way; `studentDestinations(...)` takes the host's bar as a parameter.

**The Study tab opens on the exam picker.** Until an exam type is chosen — Police
Constable, Group-4, SSC and so on — no syllabus is shown at all. Once chosen, `study/home`
becomes that exam's hub and every screen below it is scoped to that exam: its subjects in
its own pattern order, its quizzes, its daily tests and grand tests, its results, its
leaderboard. "Change" in the top bar (or Profile → Target exam) returns to the picker.

| # | Screen | Nav | Route | File |
|---|---|---|---|---|
| 6.0a | **Exam Picker** | 🏠 | `study/home` (nothing chosen) · `study/tracks` | `ui/student/ExamTrackScreens.kt` |
| 6.0b | **Exam Hub** | 🏠 | `study/home` (exam chosen) | `ui/student/ExamTrackScreens.kt` |
| 6.1 | Student Dashboard | ↪️ | `study/dashboard` | `ui/student/StudentHomeScreen.kt` |
| 6.2 | Study (subjects) | 🏠 | `study/browse` | `ui/student/StudyBrowseScreens.kt` |
| 6.3 | Exams Hub | 🏠 | `study/exams` | `ui/student/ExamScreens.kt` |
| 6.4 | Leaderboard | 🏠 | `study/leaderboard` | `ui/student/StudentMiscScreens.kt` |
| 6.5 | Student Profile | 🏠 | `study/profile` | `ui/student/StudentMiscScreens.kt` |
| 6.6 | Subject Topics | ↪️ | `study/subject/{subjectId}` | `ui/student/StudyBrowseScreens.kt` |
| 6.7 | Study Article | ↪️ | `study/article/{topicId}` | `ui/student/StudyBrowseScreens.kt` |
| 6.8 | Exam Runner | ↪️ 🔁 | `study/exam/{examId}` · `study/quiz/{topicId}` · `study/practice/{topicId}` | `ui/student/ExamScreens.kt` |
| 6.9 | Result | ↪️ | `study/result/{resultId}` | `ui/student/ResultScreens.kt` |
| 6.10 | Review Answers | ↪️ | `study/review/{resultId}` | `ui/student/ResultScreens.kt` |
| 6.11 | Strong & Weak Analysis | ↪️ | `study/analysis` | `ui/student/AnalysisScreens.kt` |
| 6.12 | Subject Analysis | ↪️ | `study/analysis/{subjectId}` | `ui/student/AnalysisScreens.kt` |
| 6.13 | Explore Weak Areas | ↪️ | `study/weak-areas` | `ui/student/AnalysisScreens.kt` |
| 6.14 | Weak Topic Plan | ↪️ | `study/weak-topic/{topicId}` | `ui/student/AnalysisScreens.kt` |
| 6.15 | My Performance | ↪️ | `study/performance` | `ui/student/StudentMiscScreens.kt` |
| 6.17 | Choose-exam prompt | 🏠 🔁 | shown on Study / Exams / Ranks with no exam chosen | `ui/student/ExamTrackScreens.kt` |
| 6.16 | Notifications | ↪️ | `study/notifications` | `ui/student/StudentMiscScreens.kt` |

**6.1 Student Home** — top bar with time-aware greeting ("Good Evening, Sai 👋"),
study-streak flame counter and a notification bell with unread badge. Body:
today's-progress ring card with streak and rank pills; **Today's Exam** card
(questions, minutes, mixed-subjects, Start button); **Continue Studying** card (next
unfinished topic); **Weekly Grand Test** card (100 questions, 90 min, date, View);
**Weak Areas 🔴** accuracy bars; **Recommended Topics** rail with band pills;
**Strong Subjects 🟢** bars; **Recent Results** cards; **Leaderboard** preview with
medals and your pinned rank row; shortcuts to Performance and Analysis.
Pull-to-refresh and a loading state.

**6.2 Study** — search field over articles and topics; per-subject cards with emoji,
English/Telugu names, progress bar, percent complete, and counts of topics, articles
and questions. Search results switch the body to a result list.

**6.3 Exams Hub** — two tabs, **Daily Exams** and **Grand Tests**. Each exam card
shows question count, duration, difficulty, date label, subject list, instructions and
an Active/Inactive pill; Start is disabled for inactive exams. "My results" link.

**6.4 Leaderboard** — four scrollable tabs (Daily / Weekly / Monthly / Grand Test).
A prominent "Your rank" card (#rank, of N students, points, tests, accuracy), then the
top 50 rows with 🥇🥈🥉 for the podium, and an "Around you" block with your neighbours
when your rank falls outside the visible top.

**6.5 Student Profile** — avatar initial, name, email, Student and rank pills; average
accuracy ring plus streak and totals; links to My Performance and Analysis; a
**📰 News module** row that switches back to the news experience; dark-mode and
study-reminder toggles; about row; "Switch role" with confirmation.

**6.6 Subject Topics** — ordered topic list with completion ticks, Telugu name,
difficulty, question count, an accuracy pill where attempted, and a per-row quiz
shortcut.

**6.7 Study Article** — title, subject pill, reading time, Completed pill,
description, full body, **Important Points** card, **Key Formulas** block,
**Examples** list, previous/next topic navigation, author line. Bottom bar:
**Mark done** and **Take Quiz**. Falls back to an empty state (with the quiz still
offered) when a topic has no published article.

**6.8 Exam Runner** 🔁 — one screen drives all four attempt types: topic quiz,
20-question daily exam, 100-question grand test and generated practice sets.
Contains: countdown timer chip (amber under 5 min, red under 1 min, auto-submits at
zero), "Question N of M" counter, linear progress bar, subject and difficulty pills,
mark-for-review toggle, lettered A–D option rows, clear-answer, Previous / Next
(Submit on the last question), and an expandable **question palette** — a 5-column
grid with five visual states (current, answered, marked, answered+marked, not
answered, not visited) plus a legend and an answered/marked/left counter. Leaving
mid-attempt asks for confirmation; submitting summarises what is unanswered.

**6.9 Result** — "Quiz Completed 🎉" / "Exam Submitted 🎉" header; score card with
accuracy ring, score, time taken and correct / wrong / skipped tiles; rank and
participants for grand tests; improvement card comparing the previous grand test
(Previous 65 → Current 72, **+7**); subject-wise accuracy bars; a "Detected in this
paper" card listing subjects under 50% with band pills and a recommendation;
topic-wise bars. Buttons: **Review Answers**, **Study Weak Topics**, **Back to Home**.

**6.10 Review Answers** — correct / wrong / skipped legend chips, then every question
with its number badge colour-coded by outcome, subject pill, outcome pill, Marked pill,
all options with the correct one outlined green and a wrong choice outlined red, and
the explanation in a card.

**6.11 Strong & Weak Analysis** — overall accuracy ring plus the classification legend
(80–100 Strong 🟢 / 50–79 Needs Practice 🟡 / 0–49 Weak 🔴), then three grouped
sections of subject accuracy bars, each row opening the subject drill-down, and an
"Explore Your Weak Areas" button.

**6.12 Subject Analysis** — subject accuracy ring, band pill, correct-of-attempted
line, then topic bars sorted weakest-first with band emoji and Telugu names.

**6.13 Explore Weak Areas** — one card per non-strong subject: band emoji, name,
accuracy, and its three lowest topics with accuracy pills, plus a **Start Practice**
button that builds a practice set on the spot. Celebratory empty state when nothing is
weak.

**6.14 Weak Topic Plan** — your accuracy ring for that topic, band pill,
correct-of-total, then the three-step plan (1 read the study article → 2 take the topic
quiz → 3 practice N questions) with step 1 ticked when the topic is already marked
done, steps disabled when the content does not exist, a **Start Learning** button and
Topic Quiz / Practice 20 shortcuts.

**6.15 My Performance** — metric cards for exams, quizzes, streak, average score, best
score, current rank and topics completed; a **7-exam accuracy bar chart** (bars
coloured by band); subject accuracy bars; and the full attempt history with type,
score, time, rank and accuracy.

**6.16 Notifications** — type-coloured icons for daily exam, grand test, new article,
result, rank update and recommendation; relative times; NEW pills; mark-all-read.

## Content Creator

| # | Screen | Nav | Route | File |
|---|---|---|---|---|
| 7.1 | Creator Dashboard | 🗄️ | `creator/dashboard` | `ui/creator/CreatorScreens.kt` |
| 7.2 | Articles | 🗄️ | `creator/articles` | `ui/creator/CreatorScreens.kt` |
| 7.3 | Article Editor | ↪️ | `creator/article-editor?articleId=` | `ui/creator/CreatorScreens.kt` |
| 7.4 | Questions & Quizzes | 🗄️ | `creator/questions` | `ui/creator/CreatorScreens.kt` |
| 7.5 | Question Editor | ↪️ | `creator/question-editor?questionId=` | `ui/creator/CreatorScreens.kt` |

**7.1 Creator Dashboard** — library counters (articles, quizzes, questions) and
pipeline counters (Draft, Pending Review, Published) colour-coded by status; recent
articles with status pills; "New article" FAB.

**7.2 Articles** — status filter chips; rows with subject, topic, reading time and
status pill; per-row publish, edit and delete (delete confirms).

**7.3 Article Editor** — subject chips then topic chips; title, short description,
content (with character counter), important points (one per line), examples (one per
line), estimated reading time. Three actions: **Save Draft**, **Submit** (→ Pending
Review), **Publish**. Validation surfaces after the first attempt.

**7.4 Questions & Quizzes** — quiz count summary and the latest questions with
subject, topic, difficulty, type and correct-answer pills; edit and delete per row.

**7.5 Question Editor** — subject and topic chips; question text; four option fields
each paired with a tappable preview row that marks the correct answer; explanation;
difficulty chips; question-type chips (MCQ / True-False / Assertion). **Save Draft**
or **Publish**.

## Exam Admin

| # | Screen | Nav | Route | File |
|---|---|---|---|---|
| 8.1 | Exam Admin Dashboard | 🗄️ | `examadmin/dashboard` | `ui/examadmin/ExamAdminScreens.kt` |
| 8.2 | Daily Exams | 🗄️ 🔁 | `examadmin/daily` | `ui/examadmin/ExamAdminScreens.kt` |
| 8.3 | Grand Tests | 🗄️ 🔁 | `examadmin/grand` | `ui/examadmin/ExamAdminScreens.kt` |
| 8.4 | Exam Editor | ↪️ | `examadmin/editor?examId=&type=` | `ui/examadmin/ExamAdminScreens.kt` |
| 8.5 | Question Bank | 🗄️ 🔁 | `examadmin/bank` | `ui/examadmin/ExamAdminScreens.kt` |
| 8.6 | Exam Results | 🗄️ | `examadmin/results` | `ui/examadmin/ExamAdminScreens.kt` |

**8.1 Exam Admin Dashboard** — tappable counters for daily exams, grand tests,
question bank and results, with "N active" captions; upcoming-and-recent exam list
with Active/Inactive pills.

**8.2 / 8.3 Daily Exams · Grand Tests** 🔁 — the same `ExamListScreen` composable
parameterised by exam type. Each card: title, active pill, question count, duration,
difficulty, date, subject list, a live-for-students switch, edit and delete.
Activating an exam pushes a notification to students.

**8.4 Exam Editor** — title, date label, duration and question count; multi-select
subject chips (questions are drawn from the bank across them); difficulty chips;
instructions; "activate immediately" switch. Validates title, subjects, count and
duration.

**8.5 Question Bank** 🔁 — text search plus subject and difficulty filters; rows show
subject, topic, difficulty, type and status pills. Also mounted for the Study Admin
and Super Admin at `studyadmin/bank`.

**8.6 Exam Results** — every recorded attempt with type, score, time taken, relative
date, rank where applicable, and accuracy coloured by band.

## Study Admin

| # | Screen | Nav | Route | File |
|---|---|---|---|---|
| 9.1 | Study Admin Dashboard | 🗄️ | `studyadmin/dashboard` | `ui/studyadmin/StudyAdminScreens.kt` |
| 9.0a | **Exam Type Management** | 🗄️ | `studyadmin/exam-types` | `ui/studyadmin/ExamTypeScreens.kt` |
| 9.0b | **Exam Type Editor** | ↪️ | `studyadmin/exam-type-editor?trackId=` | `ui/studyadmin/ExamTypeScreens.kt` |
| 9.2 | Subject Management | 🗄️ | `studyadmin/subjects` | `ui/studyadmin/StudyAdminScreens.kt` |
| 9.3 | Topic Management | 🗄️ | `studyadmin/topics` | `ui/studyadmin/StudyAdminScreens.kt` |
| 9.4 | Content Management | 🗄️ | `studyadmin/content` | `ui/studyadmin/StudyAdminScreens.kt` |
| 9.5 | Question Bank | 🗄️ 🔁 | `studyadmin/bank` | `ui/examadmin/ExamAdminScreens.kt` |

**9.1 Study Admin Dashboard** — syllabus counters (subjects with "N enabled", topics)
and content counters (articles, published, pending review, questions), plus a topic
quizzes card.

**9.0a Exam Type Management** — every exam a student can prepare for: icon tile, category,
pattern line (questions · marks · minutes), subject count, topics, how many papers are
tagged to it, a visible/hidden switch, edit and delete. Deletion is refused while papers
are still tagged with that exam type. This list *is* the student exam picker.

**9.0b Exam Type Editor** — name (English + Telugu), short name, icon, category chips,
one-line description, qualification, age limit, vacancies, exam-date label, selection
stages, duration, negative-marking text, and the **subject-wise pattern**: a switch per
subject plus questions and marks for each, with the running totals in the section header.
A visibility switch controls whether students see it.

**9.2 Subject Management** — subject rows with emoji tile, Telugu name, topic and
question counts, enable/disable switch, edit dialog (English/Telugu/emoji), delete.
Deletion is refused while the subject still owns topics.

**9.3 Topic Management** — subject filter chips; rows with Telugu name, subject and
difficulty pills, enable/disable switch, edit dialog (reassign subject, rename, change
difficulty), delete. Deletion is refused while articles or questions reference the
topic.

**9.4 Content Management** — status filter chips; article rows with subject, topic and
author; publish, move-back-to-draft and delete actions.

**9.5 Question Bank** 🔁 — same screen as 8.5.

## Super Admin (Study)

| # | Screen | Nav | Route | File |
|---|---|---|---|---|
| 10.1 | Study System Dashboard | 🗄️ | `studysuper/dashboard` | `ui/superadmin/StudySuperAdminScreens.kt` |
| 10.2 | User Management | 🗄️ | `studysuper/users` | `ui/superadmin/StudySuperAdminScreens.kt` |
| 10.3 | Role Permissions | 🗄️ | `studysuper/roles` | `ui/superadmin/StudySuperAdminScreens.kt` |

**10.1 Study System Dashboard** — people counters (students, content creators, exam
admins, study admins) and content counters (total questions, articles, quizzes, daily
exams, grand tests, subjects), each tapping through to the relevant management screen;
management rows for users, roles and syllabus.

**10.2 User Management** — search, role filter chips, and per-user ⋮ menu:
view/edit dialog (name, email, role chips), activate/deactivate, change role. "Add
user" FAB.

**10.3 Role Permissions** — one card per study role with its Telugu label, live user
count and ticked permission list.

> The Super Admin drawer also carries every Study Admin and Exam Admin screen, plus a
> **📰 News module** item that jumps back into Module 1.

---

# Navigation summary

```
Landing  ──📰──►  News role      ──►  role NavHost (pill bar / drawer)
         └─📚──►  Study role     ──►  role NavHost (bottom bar / drawer)

Reader pill bar     :  News · Clips · Study · Profile        (Search in top bar)
Student bottom bar  :  Home · Study · Exams · Ranks · Profile
                       (only when Study is entered as its own module)

Drawers (role-filtered)
  Editor           :  Dashboard · Review Queue
  News Admin       :  Dashboard · News · Categories · Reporters
  News Super Admin :  Dashboard · Users · Roles · Settings          + 📚 Study module
  Content Creator  :  Dashboard · Articles · Questions & Quizzes
  Exam Admin       :  Dashboard · Daily · Grand · Bank · Results
  Study Admin      :  Dashboard · Exam Types · Subjects · Topics · Content · Bank
  Study Super Admin:  all Study Admin + Exam Admin screens          + 📰 News module
```

## Reader tab detail

```
News   ──► flip-card deck + category filter chips
           ├─ Read full story ──► News Detail
           └─ top bar ──► Classic Feed · Search · Notifications
Clips  ──► short-video feed + filter chips over the video
           └─ Read the full story ──► News Detail
Study  ──► exam picker → exam hub (pill bar stays visible)
Profile ─► Saved · Categories · Notifications · preferences · switch role
```

# Screens by role

| Role | Screens |
|---|---|
| Reader | 11 (incl. Study tab and Classic Feed) |
| Reporter | 3 |
| Editor | 3 |
| News Admin | 4 (+ Article Review) |
| News Super Admin | 4 |
| Student | 19 |
| Content Creator | 5 |
| Exam Admin | 6 |
| Study Admin | 7 |
| Study Super Admin | 3 (+ all Study/Exam Admin screens) |
| Shell | 2 |

# Cross-cutting UI states

Present throughout both modules: loading indicators, empty states with a call to
action, error state with retry, confirmation dialogs before anything destructive,
snackbar feedback on every mutation, pull-to-refresh on the classic feed and
dashboards, unread badges, filter chip rows, progress rings and bars, a bar chart,
form validation on every editor, and full light/dark theming (system default, with a
toggle in either Profile screen).

# Known limitations in this build

- **Clips playback is simulated** — no media stream, no bundled video. The transport
  advances over each clip's stated duration and the card says so on screen.
- **The reader pill bar is docked, not overlaid.** It is translucent, but content
  stops above it rather than scrolling underneath. Making it a true overlay needs the
  bar detached from the Scaffold bottom-bar slot plus bottom padding on each hosting
  screen.
- **All state is in memory.** Anything created in a session — an article, an exam
  attempt, a new subject, a like on a clip — resets when the process is killed.
