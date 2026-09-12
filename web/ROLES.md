# J Voice — Roles

Every role in the app, the demo account it signs in as, where it lands, what it can
reach and what it deliberately cannot. Roles are chosen from the **landing page** —
there is no password and no real authentication. "Switch role" anywhere returns to
the landing page.

Source of truth: `news/data/model/Models.kt` (`UserRole`),
`study/data/model/StudyModels.kt` (`StudyRole`), and the drawer filters in
`news/navigation/NavigationShells.kt` / `study/navigation/StudyShells.kt`.

Companion docs: [ACTIONS.md](ACTIONS.md) · `J Voice android app/README.md` ·
`J Voice android app/SCREENS.md` · `J Voice web/README.md`.

---

## Web admin console — two logins

The browser console in `J Voice web` does not mirror the app's ten roles. It has two
logins, and the Admin one covers **both** modules:

| Console login | Scope | Replaces these app roles |
|---|---|---|
| **Admin** — Ravi Teja Sharma | News + Study | News Admin, Study Admin, Exam Admin, Super Admin |
| **Editor** — Sridevi Naidu | News only | Editor |
| **Reporter** — Kiran Kumar | News only | Reporter |
| **Content Creator** — Sunitha Rao | Study only | Content Creator |

The Admin console does everything below that is not a reader or student action: review
and publish news, categories, reporters, AI shorts, exam types, syllabus, content,
question bank, exams, results, users, roles and settings. The Editor login is limited to
the review queue — edit, approve, publish, reject, send back. The Reporter login is the
filing end only, and mirrors the mobile Reporter role described below: file, track,
resubmit, never approve or publish. Pages a persona cannot use redirect it back to the
dashboard.

The rest of this document describes the **mobile app** roles.

---

## At a glance

| Module | Role | Telugu | Demo user | Entry screen | Navigation |
|---|---|---|---|---|---|
| News | Reader | పాఠకుడు | Sai Charan | News feed (flip cards) | Floating pill bar |
| News | Reporter | రిపోర్టర్ | Kiran Kumar | Reporter dashboard | Top bar + tabs |
| News | Editor | ఎడిటర్ | Sridevi Naidu | Editor dashboard | Drawer |
| News | News Admin | న్యూస్ అడ్మిన్ | Anitha Reddy | Admin dashboard | Drawer |
| News | Super Admin | సూపర్ అడ్మిన్ | Ravi Teja Sharma | System dashboard | Drawer |
| Study | Student | విద్యార్థి | Sai Charan | Exam picker → exam hub | Bottom bar |
| Study | Content Creator | కంటెంట్ క్రియేటర్ | Sunitha Rao | Content dashboard | Drawer |
| Study | Exam Admin | ఎగ్జామ్ అడ్మిన్ | Mahesh Chandra | Exam dashboard | Drawer |
| Study | Study Admin | స్టడీ అడ్మిన్ | Sridevi Naidu | Syllabus dashboard | Drawer |
| Study | Super Admin | సూపర్ అడ్మిన్ | Ravi Teja Sharma | System dashboard | Drawer |

The two Super Admin rows are the same person across both modules, and Super Admin is
the only role whose drawer offers a direct hop to the other module.

---

# Module 1 — News

## Reader — `UserRole.READER`

Consumes published news only. Nothing in any other state is ever visible —
`NewsStatus.isVisibleToReader` is true for `PUBLISHED` alone.

**Can** — browse the flip-card feed, watch short video Clips, open an article,
search, browse by category, save and unsave, like or dislike, comment, like or
delete their own comment, report an article, mark notifications read, set their
location, toggle dark mode, and hop into the Study module from the Study tab.

**Cannot** — see drafts, submissions, review notes or rejected articles; create or
edit anything; reach any dashboard.

## Reporter — `UserRole.REPORTER`

Owns the creation end of the pipeline, scoped to their own articles.

**Can** — create an article (headline, description, body, category, tags, image),
save it as a draft, submit it for review, edit anything still editable
(`DRAFT`, `REJECTED`, `SENT_BACK`), delete their own draft, read the rejection reason
or send-back note, resubmit, and follow their articles through every status.

**Cannot** — approve or publish, touch another reporter's article, or manage
categories and users.

## Editor — `UserRole.EDITOR`

Owns the gate between submitted and published.

**Drawer** — Dashboard · Review Queue · AI Shorts.

**Can** — open a queued article (which flips it to `UNDER_REVIEW`), edit its
headline, description, body, category and tags, save edits, **Approve & publish**,
**Approve only** (leaves it `APPROVED`, not yet live), **Reject** with a reason, or
**Send back** with a note — the last two notify the reporter and reopen it for
editing.

**Cannot** — manage categories, reporters, users or system settings.

## News Admin — `UserRole.NEWS_ADMIN`

Owns everything published, plus the news-side catalogue.

**Drawer** — Dashboard · News Management · Categories · Reporters · AI Shorts ·
Video Templates.

**Can** — publish an approved article, unpublish a live one, mark breaking, feature
or pin, remove an article, filter news by query, category or status; add, rename,
enable/disable or delete a category; activate or deactivate a reporter and change
their location; run the AI Shorts pipeline and administer video templates.

**Cannot** — change anyone's role, edit users, or alter system settings.

## Super Admin — `UserRole.SUPER_ADMIN`

Owns people and platform configuration.

**Drawer** — Dashboard · User Management · Role Management · System Settings ·
AI Shorts · Video Templates · **Switch module**.

**Can** — everything above, plus: search and filter users by role, activate or
deactivate a user, change a user's role, edit name / email / location, review the
role matrix, and change system settings.

---

# Module 2 — Study & Exam Preparation

## Student — `StudyRole.STUDENT`

The learner. No drawer — a bottom bar only.

The Study tab opens on the **exam picker**: nothing is studied until the student picks
what they are preparing for (Police Constable, Group-4, SSC…). Afterwards the whole
module is scoped to that exam — its subjects in its own paper order, its quizzes, its
tests, its results and its leaderboard — and "Change" returns to the picker. The exam
types on offer are whatever a Study Admin has enabled.

**Can** — choose and change their target exam, read published study articles, mark a
topic done, take a topic quiz, sit the exam's Daily Test or Grand Test (timed,
auto-submitting, with a question palette, mark-for-review and clear-answer), run a
20-question practice set, see the
result with its subject- and topic-wise breakdown, review every answer with its
explanation, read the Strong / Needs Practice / Weak analysis, work a weak-topic
three-step plan, check the leaderboard across four periods, track their own
performance and streak, and read notifications.

**Cannot** — see draft or pending content, create or edit anything, or reach any
dashboard.

## Content Creator — `StudyRole.CONTENT_CREATOR`

Writes the material.

**Drawer** — Dashboard · Articles · Questions & Quizzes.

**Can** — create and edit a study article (subject, topic, body, important points,
formulas, examples), save it as `DRAFT`, submit it as `PENDING_REVIEW`, publish it,
delete it; create and edit questions with options, correct answer, difficulty and an
explanation; generate a topic quiz from a topic's question pool; filter their own
work by status.

**Cannot** — create or activate exams, change subjects or topics, or manage users.

## Exam Admin — `StudyRole.EXAM_ADMIN`

Owns exams.

**Drawer** — Dashboard · Daily Exams · Grand Tests · Question Bank · Exam Results.

**Can** — create a Daily Exam or a Grand Test (title, duration, question count,
subject mix, date), **tag it with the exam types that sit it** (or leave it untagged as
general practice for every exam type) and fill the paper from an exam type's pattern in
one tap, edit it, activate or deactivate it, delete it, browse the question bank filtered
by subject, difficulty and text, and read exam results. Activating an exam makes it appear
live in that exam type's Tests tab with a notification.

**Cannot** — create or edit exam types themselves, write articles, restructure the
syllabus, or manage users.

## Study Admin — `StudyRole.STUDY_ADMIN`

Owns the exam types and the syllabus.

**Drawer** — Dashboard · **Exam Types** · Subjects · Topics · Content · Question Bank.

**Can** — own the **exam types** students choose from: add or edit one (name in both
languages, icon, category, eligibility, age limit, vacancies, exam date, selection stages,
duration, negative marking, the subject-wise pattern with questions and marks per
subject, and the **important dates** shown on the student's exam home), hide or show it,
delete it when no paper is tagged with it. Also: add, rename,
enable/disable and delete a subject; add, edit, enable/disable and delete a topic (with
its difficulty and parent subject); publish or send back a creator's article; delete an
article; browse the question bank.

**Cannot** — create exams, or manage users and roles.

Nothing about the student's exam list is hardcoded: the picker shows exactly what is
enabled here, and adding an exam type notifies students.

## Super Admin (Study) — `StudyRole.SUPER_ADMIN`

**Drawer** — Dashboard · User Management · Role Permissions · **Exam Types** ·
Subjects · Topics · Content · Question Bank · Daily Exams · Grand Tests · Exam Results ·
**Switch module**.

**Can** — the union of Study Admin and Exam Admin, plus add a user, change a user's
role, activate or deactivate a user, edit name and email, and read the
role-permission matrix.

---

## Permission matrix

`✔` = allowed · `–` = not reachable for that role.

### News

| Action | Reader | Reporter | Editor | News Admin | Super Admin |
|---|:--:|:--:|:--:|:--:|:--:|
| Read published news | ✔ | ✔ | ✔ | ✔ | ✔ |
| Save / like / comment / report | ✔ | – | – | – | – |
| Create & submit an article | – | ✔ | – | – | – |
| Edit own draft / rejected / sent-back | – | ✔ | – | – | – |
| Review, edit & approve any article | – | – | ✔ | – | – |
| Reject / send back with a note | – | – | ✔ | – | – |
| Publish / unpublish | – | – | ✔ | ✔ | ✔ |
| Breaking / feature / remove | – | – | – | ✔ | ✔ |
| Manage categories | – | – | – | ✔ | ✔ |
| Manage reporters | – | – | – | ✔ | ✔ |
| AI Shorts & video templates | – | – | ✔ | ✔ | ✔ |
| Manage users & roles | – | – | – | – | ✔ |
| System settings | – | – | – | – | ✔ |

### Study

| Action | Student | Creator | Exam Admin | Study Admin | Super Admin |
|---|:--:|:--:|:--:|:--:|:--:|
| Choose / change target exam | ✔ | – | – | – | – |
| Create & edit exam types | – | – | – | ✔ | ✔ |
| Show / hide an exam type | – | – | – | ✔ | ✔ |
| Tag a paper to an exam type | – | – | ✔ | – | ✔ |
| Read published articles | ✔ | ✔ | ✔ | ✔ | ✔ |
| Take quizzes & exams | ✔ | – | – | – | – |
| Own performance & leaderboard | ✔ | – | – | – | – |
| Write / edit articles & questions | – | ✔ | – | – | – |
| Publish or send back content | – | ✔ | – | ✔ | ✔ |
| Manage subjects & topics | – | – | – | ✔ | ✔ |
| Create & activate exams | – | – | ✔ | – | ✔ |
| Read all exam results | – | – | ✔ | – | ✔ |
| Question bank | – | ✔ | ✔ | ✔ | ✔ |
| Manage users & role permissions | – | – | – | – | ✔ |

---

## Notes

- Roles are switched, not authenticated. There is no password, no session token and
  no server check — `SessionViewModel` and `StudySessionViewModel` simply hold the
  selected role.
- The repositories are shared in-memory singletons, so a change made in one role is
  visible immediately after switching to another **within the same run**. Killing the
  process resets everything to the seeded mock data.
