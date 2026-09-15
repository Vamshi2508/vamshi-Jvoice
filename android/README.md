# J Voice — React Native (News module)

React Native rewrite of the Android app, replacing the previous Kotlin/Jetpack
Compose project. See `../docs/audit-report.html` and the plan this was built
from for full background. **Scope: the News module (Module 1) only** — the
Study module and AI Shorts are deliberately out of scope (see below).

All five roles are now built: Reader, Reporter, Editor, News Admin, Super
Admin.

## One-time setup still needed from you

1. **Copy `google-services.json` into place.** I could not write this file
   myself (it contains an API key and was blocked by a safety check). Copy it
   from the old location in git history:
   ```
   git show 0c0799f:android/app/google-services.json > android/android/app/google-services.json
   ```
   (run from the `Jvoice` repo root — `0c0799f` is the last commit before the
   Kotlin app was removed; ask me if that commit ever gets rebased away).

2. **JDK 17.** This machine currently has Java 8 on `PATH` (`java -version`).
   The Android Gradle Plugin used here needs JDK 17 — point `JAVA_HOME` at a
   17 install (Android Studio ships one; e.g.
   `C:\Program Files\Android\Android Studio\jbr`) before running
   `npm run android`.

3. **iOS Firebase config.** There is no iOS app registered in the
   `jvoice-b4b2e` Firebase project yet (this product was Android-only before
   now). Add one in the Firebase console and hand me the resulting
   `GoogleService-Info.plist` before iOS Firebase calls will work. The JS/TS
   code itself is already cross-platform.

**None of this has been run yet** — the code typechecks and lints clean, but
hasn't been launched on a device/emulator, because of the two blockers above.

## Known limitation carried over from the Kotlin app: staff/reporter lists

`useStaffStore.ts` (Reporter Management, User Management) reads the RTDB
`users/` tree, same as the old `StaffDirectory.kt`. But
`web/firebase/database.rules.json` denies `.read` at the `users` parent node
and only grants it per-child (`users/$uid`) — Firebase doesn't retroactively
grant a shallow read from a deeper child rule, so **this listener gets
permission-denied for everyone, including super_admin**, and those screens
show an explanatory empty state instead of data. This is a pre-existing gap
in the shared security rules, not something introduced by this rewrite —
fixing it means adding an explicit `.read` at the `users` node for admin
roles in `database.rules.json` and redeploying it (touches the live project
and the web app too, so flagged here rather than changed unilaterally).

## Development backend: Firebase Local Emulator Suite

This app is wired to talk to the **Firebase emulators**, not the live
production project, whenever `__DEV__` is true (see
`src/core/firebase/firebase.ts`). Start them from the `web/firebase` package
(reuses the existing rules files) before running the app:

```
cd ../../web/firebase
firebase emulators:start
```

Android emulator reaches the host machine at `10.0.2.2`; iOS simulator uses
`localhost` — this is already handled in `firebase.ts`.

## Running

```
npm install
npm run android   # or: npm run ios (requires a Mac)
```

## What's here

- `src/types/news.ts` — canonical schema, ported 1:1 from the Kotlin
  `NewsCodec.kt` / `Models.kt` (**not** the web console's schema — see the
  audit report for why).
- `src/core/i18n/` — bilingual `LocalizedText`, language preference store,
  `LocalizedFormField` component for bilingual authoring.
- `src/core/auth/` — session store, staff sign-in, force-logout watcher, app
  update gate, staff directory (ported from `core/auth/*.kt` and
  `core/data/StaffDirectory.kt`).
- `src/core/firebase/` — Firebase app/auth/firestore/database init +
  emulator wiring.
- `src/data/news/` — Firestore codec + `useNewsStore` (article/category/
  notification CRUD, ported from `NewsRepository.kt`), `useEngagementStore`
  (comments/likes/reports, in-memory — matches Android), `useSettingsStore`
  (in-memory system settings + static role-permission list).
- `src/screens/reader/` — Home, Categories, Category detail, Saved,
  Notifications, Profile, Search, Article detail (with like/dislike/comment/
  report), Comments.
- `src/screens/reporter/` — Dashboard, My News, Create/Edit form
  (`ArticleEditor`, bilingual).
- `src/screens/editor/` — Dashboard, Review Queue, Article Review
  (approve/publish/reject/send-back).
- `src/screens/admin/` — Dashboard, News Management, Category Management,
  Reporter Management.
- `src/screens/superadmin/` — Dashboard, User Management, Role Management
  (read-only, matches Android), System Settings.
- `src/navigation/` — role-based routing (`roleRouting.ts`, mirrors
  `JVoiceNavGraph.kt`'s `startDestination` switch): Reader gets a bottom tab
  bar, Reporter a plain stack, Editor/News Admin/Super Admin get a drawer
  (`DeskDrawerContent.tsx`, mirrors `AdminScaffold`'s drawer + "Switch role").

## Deliberately not built

- **Study module** and **AI Shorts** — out of scope per the approved plan;
  Android's versions are mock-data-only / fully simulated respectively.
- **Clips tab** — Android's own `ClipsRepository` was never wired to a
  backend (empty on every launch there too); not worth porting a
  non-functional feature. See `docs/android.html`.
- **The swipeable flip-card News feed** (`NewsFlipScreen.kt`) — simplified to
  the classic list feed (`ReaderHomeScreen`) only, to avoid building a
  complex custom gesture deck for a phase-1 rewrite. Can be added later.
- **Video Templates / cross-module switching** — tied to AI Shorts / Study,
  out of scope.
