# J Voice — Actions

Every action a user can perform, grouped by role, with the function behind it. Read
this next to [ROLES.md](ROLES.md), which says *who* may do each of these.

All actions mutate in-memory state only. No network call, no database write, no push
notification. Paths are relative to
`J Voice android app/app/src/main/java/com/jvoice/`.

**Web console:** the same management actions exist in the browser console — including
writing study material and questions from scratch —
(`J Voice web`), where one **Admin** login covers both modules and a separate **Editor**
login covers the news review queue. There the actions are reducer cases in
`src/store/store.jsx` (`article/approve`, `track/save`, `exam/toggleActive`, …) rather
than ViewModel functions; `J Voice web/README.md` lists them screen by screen.

**Status vocabulary**

| Domain | Statuses |
|---|---|
| News article (`NewsStatus`) | `DRAFT` → `SUBMITTED` → `UNDER_REVIEW` → `APPROVED` / `REJECTED` / `SENT_BACK` → `PUBLISHED` |
| Study content (`ContentStatus`) | `DRAFT` → `PENDING_REVIEW` → `PUBLISHED` |
| Exam type (`ExamTrack.isEnabled`) | hidden ⇄ visible in the student exam picker |
| AI short (`AIShortStatus`) | `DRAFT` → `SCRIPT_GENERATING` → `SCRIPT_READY` → `WAITING_FOR_REVIEW` → `PREPARING_MEDIA` → `VOICE_GENERATING` → `RENDER_QUEUED` → `RENDERING` → `READY` → `APPROVED` → `PUBLISHED` (or `FAILED`) |

---

# Module 1 — News

## Reader — `ui/reader/ReaderViewModel.kt`

| Action | Call | Effect |
|---|---|---|
| Refresh the feed | `refresh()` | Re-reads the repository, shows the loading state |
| Retry after an error | `retry()` | Clears the error and reloads |
| Open an article | `articleById(id)` | Loads the detail screen |
| Count a read | `registerView(id)` | Increments the article's view count |
| Save / unsave | `toggleSave(id)` | Adds to or removes from Saved; returns the new state |
| Clear all saved | `clearSaved()` | Empties the Saved list |
| Browse a category | `articlesInCategory(id)` | Published articles in that category |
| Read related | `related(article)`, `moreFromCategory(article)` | Suggestion rows on the detail screen |
| Report an article | `reportArticle(id)` | Flags it for moderation |
| Change location | `setLocation(location)` | Updates the profile city |
| Mark one notification read | `markNotificationRead(id)` | Clears its badge |
| Mark all read | `markAllRead()` | Clears the reader badge |

### Engagement — `data/repository/EngagementRepository.kt`

| Action | Call |
|---|---|
| Like / unlike an article | `toggleLike(articleId)` |
| Dislike / undo | `toggleDislike(articleId)` |
| Post a comment | `addComment(articleId, authorName, text)` |
| Like a comment | `likeComment(commentId)` |
| Delete own comment | `deleteComment(commentId)` |
| Submit a report with a reason | `submitReport(...)` |

### Clips — `data/repository/ClipsRepository.kt`

| Action | Call |
|---|---|
| Load the clip feed | `feed(categoryId)` |
| Like / unlike a clip | `toggleLike(clipId)` |
| Save / unsave a clip | `toggleSave(clipId)` |
| Count a clip view | `registerView(clipId)` |

## Reporter — `ui/reporter/ReporterViewModel.kt`

| Action | Call | Effect |
|---|---|---|
| Start a new article | `startNewArticle()` | Opens an empty form |
| Open one for editing | `loadForEdit(articleId)` | Fills the form; allowed only when `canEdit` |
| Edit a field | `updateForm { ... }` | Updates the form state, validation included |
| Save as draft | `save(submit = false)` | Status stays `DRAFT` |
| Submit for review | `save(submit = true)` | Status becomes `SUBMITTED`, enters the editor queue |
| Delete a draft | `deleteDraft(articleId)` | Removes it |
| Check editability | `canEdit(article)` | True for `DRAFT`, `REJECTED`, `SENT_BACK` |
| Filter by status | `articlesWithStatus(status)` | Drives the My News tabs |

Resubmitting after a rejection or send-back is the same `save(submit = true)` call —
the reason or note stays on the article as history.

## Editor — `ui/editor/EditorViewModel.kt`

| Action | Call | Effect |
|---|---|---|
| Open a queued article | `loadDraft(articleId)` | Status flips to `UNDER_REVIEW` |
| Edit the copy | `updateDraft { ... }` | Headline, description, body, category |
| Add / remove a tag | `addTag(tag)`, `removeTag(tag)` | Edits the tag list |
| Save edits only | `saveEdits()` | Keeps it in review |
| Approve & publish | `approveAndPublish()` | Status `PUBLISHED` — live in the Reader feed at once |
| Approve without publishing | `approveOnly()` | Status `APPROVED`; the News Admin sees "not live yet" |
| Reject | `reject(reason)` | Status `REJECTED`, reason written on the article, reporter notified |
| Send back | `sendBack(note)` | Status `SENT_BACK`, note written on the article, reporter notified |

## News Admin — `ui/admin/AdminViewModel.kt`

**News management**

| Action | Call |
|---|---|
| Search | `setQuery(value)` |
| Filter by category / status | `setCategoryFilter(id)`, `setStatusFilter(status)` |
| Publish an approved article | `publish(id)` |
| Unpublish a live article | `unpublish(id)` |
| Mark / unmark breaking | `toggleBreaking(id)` |
| Feature / unfeature (pin) | `toggleFeatured(id)` |
| Remove an article | `removeArticle(id)` |

**Categories**

| Action | Call |
|---|---|
| Add | `addCategory(nameEn, nameTe, emoji)` |
| Rename or re-emoji | `updateCategory(id, nameEn, nameTe, emoji)` |
| Enable / disable | `toggleCategory(id)` |
| Delete | `deleteCategory(id)` |

**Reporters**

| Action | Call |
|---|---|
| Activate / deactivate | `toggleReporterActive(userId)` |
| Change location | `updateReporterLocation(userId, location)` |

## Super Admin — `ui/superadmin/SuperAdminViewModel.kt`

| Action | Call |
|---|---|
| Filter users by role | `setRoleFilter(role)` |
| Search users | `setQuery(value)` |
| Activate / deactivate a user | `setActive(userId, active)` |
| Change a user's role | `changeRole(userId, role)` |
| Edit a user | `updateUser(userId, name, email, location)` |
| Change system settings | `updateSettings { ... }` |

## AI Shorts — `aishorts/ui/AIShortViewModel.kt`

Reached by Editor, News Admin and Super Admin. Turns a news article into a short
video, entirely with mocked generation steps.

| Stage | Action | Call |
|---|---|---|
| Start | Create a draft short from an article | `createDraftFor(newsId, createdBy)` |
| Script | Generate the script | `generateScript(shortId)` |
| Script | Edit scene text / duration | `updateSceneText(...)`, `updateSceneDuration(...)` |
| Script | Add or delete a scene | `addScene(shortId)`, `deleteScene(shortId, sceneId)` |
| Style | Pick a template | `selectTemplate(shortId, templateId)` |
| Style | See the recommendation and why | `recommendedTemplateFor(newsId)`, `recommendationReasonFor(newsId)` |
| Voice | Pick language and voice | `selectLanguage(...)`, `selectVoice(shortId, voiceId, style)` |
| Voice | Preview a voice | `previewVoice(voiceId, style)` |
| Media | Assign, upload or re-suggest media | `assignMedia(...)`, `uploadMedia(...)`, `resuggestMedia(shortId)` |
| Media | Trim a clip | `setClipWindow(shortId, sceneId, startMs, endMs)` |
| Length | Set total duration | `setDuration(shortId, seconds)` |
| Render | Start generation | `startGeneration(shortId)` |
| Review | Approve | `approve(shortId)` |
| Review | Publish / unpublish | `publish(shortId)`, `unpublish(shortId)` |
| Review | Regenerate one part | `regenerate(shortId, target)` |
| Manage | Delete a short | `deleteShort(shortId)` |
| Templates | Enable / disable, set default | `setTemplateActive(id, active)`, `setDefaultTemplate(id)` |
| List | Filter and search | `setFilter(value)`, `setQuery(value)` |

Languages: Telugu and English are live; Hindi, Tamil and Kannada exist in the model
and are shown as unavailable.

---

# Module 2 — Study & Exam Preparation

## Student — `study/ui/student/StudentViewModels.kt`

**Choosing the exam** (`ExamTrackViewModel`) — the first thing that happens in the Study
tab. Nothing below is available until an exam is chosen.

| Action | Call |
|---|---|
| Browse exam types by category | `grouped` |
| Search exam types | `setQuery(value)` |
| Pick an exam | `select(trackId)` |
| Change exam | `changeExam()` |
| Read one exam's counts | `summary(track)` |
| Open the exam hub | `hub` (notifications, important dates, quick links, progress) |

Everything after this point is scoped to that exam: `ExamTrackRepository.isInScope(...)`
filters the subjects, topics, articles, analysis and weak areas, `ExamRepository` filters
the papers, and `LeaderboardRepository` keys the board by exam.

**Studying**

| Action | Call |
|---|---|
| Browse subjects and topics | `enabledSubjects()`, `topicsOf(subjectId)` |
| Search study content | `setQuery(value)` |
| Open a topic's article | `articleForTopic(topicId)` |
| Mark a topic done / undone | `toggleCompleted(topicId)` |
| Move to the previous / next topic | `neighbours(topicId)` |
| See progress on a subject | `subjectProgress(subjectId)` |

**Taking an exam** (`ExamEngine` + `ExamRepository`)

| Action | Call |
|---|---|
| Start a topic quiz | `startTopicQuiz(topicId)` |
| Start a daily exam or grand test | `startExam(examId)` |
| Start a 20-question practice set | `startPractice(topicId)` |
| Answer a question | `select(index)` |
| Clear the answer | `clearSelection()` |
| Mark for review | `toggleMark()` |
| Jump via the palette | `goTo(index)` |
| Next / previous | `next()`, `previous()` |
| Submit | `submit()` |
| Leave without submitting | `abandon()` |

The timer auto-submits at zero; `stopTimer()` halts it when the screen closes.

**After the exam**

| Action | Call |
|---|---|
| Open a result | `resultById(id)` |
| Review each answer with its explanation | `questionById(id)` per attempt row |
| See strong / needs-practice / weak subjects | `strong()`, `needsPractice()`, `weak()` |
| Drill into a subject | `subjectPerformanceById(id)`, `topicsFor(subjectId)` |
| Topic accuracy | `topicPerformance(topicId)` |
| Get the three-step plan for a weak topic | `studyPlan(topicId)` |
| Compare grand tests | `grandTestImprovement()` |
| Switch leaderboard period | `setPeriod(value)` |
| Read the leaderboard | `participants()` |
| Notifications | `markRead(id)`, `markAllRead()` |

Banding rule: `80–100% Strong 🟢 · 50–79% Needs Practice 🟡 · 0–49% Weak 🔴`.
Subject figures are summed from the topic tally, so a submitted attempt moves both at
once.

## Content Creator — `study/ui/creator/CreatorViewModel.kt`

| Action | Call | Effect |
|---|---|---|
| New article | `newArticle()` | Empty editor |
| Open for editing | `loadArticle(articleId)` | Fills the editor |
| Edit fields | `updateArticleForm { ... }` | Title, subject, topic, body, points, formulas, examples |
| Save as draft | `saveArticle(ContentStatus.DRAFT, author)` | Stays private |
| Submit for review | `saveArticle(ContentStatus.PENDING_REVIEW, author)` | Goes to the Study Admin |
| Publish | `publishArticle(id)` | Visible to students |
| Delete | `deleteArticle(id)` | Removes it |
| New question | `newQuestion()` | Empty question editor |
| Open a question | `loadQuestion(questionId)` | Fills the editor |
| Edit a question | `updateQuestionForm { ... }` | Options, correct answer, difficulty, explanation |
| Save a question | `saveQuestion(status)` | Draft or pending review |
| Delete a question | `deleteQuestion(id)` | Removes it |
| Build a topic quiz | `createQuizForTopic(topicId, author)` | Assembles a quiz from that topic's questions |
| Filter by status | `setStatusFilter(status)` | Draft / pending / published |

## Exam Admin — `study/ui/examadmin/ExamAdminViewModel.kt`

| Action | Call | Effect |
|---|---|---|
| New exam | `newExam(type)` | Daily exam or grand test |
| Open for editing | `loadExam(examId)` | Fills the form |
| Edit fields | `updateForm { ... }` | Title, duration, question count, date |
| Choose the subject mix | `toggleSubject(subjectId)` | Adds/removes a subject |
| Tag the paper to an exam type | `toggleTrack(trackId)` | Untagged = general practice, visible under every exam type |
| Fill the paper from a pattern | `applyTrackPattern(trackId)` | Copies that exam's subjects, duration and question count |
| Save | `save()` | Validates and persists |
| Activate / deactivate | `setActive(examId, active)` | Activating puts it live in the Student's Exams tab with a notification |
| Delete | `deleteExam(examId)` | Removes it |
| Browse the question bank | `setBankSubject(id)`, `setBankDifficulty(value)`, `setBankQuery(value)` | Filters |
| Read results | `dailyExams()`, `grandTests()` + the results screen | Attempt data |

## Study Admin — `study/ui/studyadmin/StudyAdminViewModel.kt`

**Exam types** — this list is the student's exam picker.

| Action | Call | Effect |
|---|---|---|
| New exam type | `newTrack()` | Empty editor |
| Open one for editing | `loadTrack(trackId)` | Fills the editor |
| Edit fields | `updateTrackForm { ... }` | Name, icon, category, eligibility, vacancies, date, stages, duration, negative marking |
| Enter important dates | `updateTrackForm { it.copy(keyDatesText = ...) }` | One per line, `Label \| 12 Sep 2026` — listed on the student's exam home |
| Add / remove a subject from the pattern | `toggleSection(subjectId)` | Defaults to 10 questions, 10 marks |
| Set a subject's share | `setSectionQuestions(...)`, `setSectionMarks(...)` | Totals recompute live |
| Save | `saveTrack()` | New exam types notify students |
| Show / hide | `toggleTrack(id)` | Hidden types leave the picker; students on one are returned to it |
| Delete | `deleteTrack(id)` | Refused while papers are tagged with it |
| Papers tagged with it | `trackPaperCount(id)` | What blocks deletion |

**Syllabus**

| Action | Call |
|---|---|
| Add a subject | `addSubject(nameEn, nameTe, emoji)` |
| Edit a subject | `updateSubject(id, nameEn, nameTe, emoji)` |
| Enable / disable a subject | `toggleSubject(id)` |
| Delete a subject | `deleteSubject(id)` |
| Add a topic | `addTopic(subjectId, nameEn, nameTe, difficulty)` |
| Edit a topic | `updateTopic(id, subjectId, nameEn, nameTe, difficulty)` |
| Enable / disable a topic | `toggleTopic(id)` |
| Delete a topic | `deleteTopic(id)` |
| Publish a submitted article | `publishArticle(id)` |
| Send an article back to draft | `sendBackArticle(id)` |
| Delete an article | `deleteArticle(id)` |
| Filter topics / content | `setTopicSubjectFilter(id)`, `setContentStatusFilter(status)` |

## Super Admin (Study) — `study/ui/superadmin/StudySuperAdminViewModel.kt`

| Action | Call |
|---|---|
| Add a user | `addUser(name, email, role)` |
| Activate / deactivate | `setActive(userId, active)` |
| Change a role | `changeRole(userId, role)` |
| Edit a user | `updateUser(userId, name, email)` |
| Filter by role, search | `setRoleFilter(role)`, `setQuery(value)` |
| Count users per role | `countFor(role)` |

Plus every Study Admin and Exam Admin action above, reached from the same drawer.

---

## End-to-end flows worth demoing

**News publishing**

```
Reporter: create → submit
Editor:   queue → open (UNDER_REVIEW) → edit → Approve & publish
Reader:   the article is already in Latest, its category and search
Admin:    mark breaking / feature / unpublish / remove
```

**Send-back loop**

```
Editor: Send back with a note  →  Reporter: read the note → edit → resubmit
```

**AI short**

```
Article → create draft → generate script → template → voice → media → render
        → approve → publish → visible in the Reader's Clips tab
```

**Study loop**

```
Student: Study tab → exam picker → pick Constable
       → exam hub: pattern → subject → article → topic quiz
       → daily test → result → subject analysis
       → weak topic → three-step plan → practice set
       → grand test → leaderboard (Constable board) → performance
```

**Content and exam pipeline**

```
Study Admin:     Exam Types → add "Police Constable" with its subject-wise pattern
                 →  it appears in the student exam picker immediately
Content Creator: article / question  (draft → pending review → published)
Study Admin:     subjects, topics, publish or send back, question bank
Exam Admin:      create an exam → tag it "Constable" → activate
                 →  Constable students see it live + notification; others do not
```

Because every repository is a shared in-memory singleton, all of these are observable
inside a single run of the app by switching roles. State resets when the process is
killed.
