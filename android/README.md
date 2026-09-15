# J Voice — React Native (News module, phase 1)

React Native rewrite of the Android app, replacing the previous Kotlin/Jetpack
Compose project. See `../docs/audit-report.html` and the plan this was built
from for full background. **Phase 1 scope: News module only** — reader,
staff sign-in, and the reader home/detail screens. Reporter, Editor, News
Admin, Super Admin screens, and the Study module are not built yet.

## One-time setup still needed from you

1. **Copy `google-services.json` into place.** I could not write this file
   myself (it contains an API key and was blocked by a safety check). Copy it
   from the old location in git history:
   ```
   git show HEAD~1:android/app/google-services.json > android/android/app/google-services.json
   ```
   (run from the `Jvoice` repo root, on a commit before this rewrite — or ask
   me to walk you through finding the right commit if `HEAD~1` isn't it).

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
- `src/core/i18n/` — bilingual `LocalizedText` + language preference store.
- `src/core/auth/` — session store, staff sign-in, force-logout watcher, app
  update gate (ported from `core/auth/*.kt`).
- `src/core/firebase/` — Firebase app/auth/firestore/database init +
  emulator wiring.
- `src/data/news/` — Firestore codec + the `useNewsStore` Zustand store
  (ported from `NewsRepository.kt`).
- `src/screens/reader/`, `src/navigation/` — the first vertical slice:
  Landing → Reader tabs (Home built out; Categories/Saved/Notifications/
  Profile are stubs) and Staff sign-in → same tabs. `ReaderHomeScreen.tsx` is
  the template the remaining screens (and later Reporter/Editor/Admin/
  SuperAdmin phases) follow.

## Next up

Reporter flow (Dashboard, My News, Create/Edit form), then Editor, News
Admin, and Super Admin, per the approved plan — each phase wires more of
`useNewsStore`'s already-complete mutation set into real screens.
