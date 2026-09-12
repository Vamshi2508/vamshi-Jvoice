# J Voice

Telugu news + competitive-exam preparation platform: an Android app for readers and
students, and a web admin console for editors and admins. Prototype — local mock data
only, no backend, no Firebase, no real authentication.

## Documentation

| Document | What is inside |
|---|---|
| [J Voice web/README.md](J%20Voice%20web/README.md) | The web admin console: how to run it, its two logins and every screen |
| [ROLES.md](ROLES.md) | All 10 roles, demo accounts, entry screens, drawer menus, and the permission matrix |
| [ACTIONS.md](ACTIONS.md) | Every action per role with the function behind it, statuses, and end-to-end demo flows |
| [J Voice android app/README.md](J%20Voice%20android%20app/README.md) | Build & run, module overview, architecture, mock-data counts |
| [J Voice android app/SCREENS.md](J%20Voice%20android%20app/SCREENS.md) | Screen catalogue — 63 screens with route, file and contents |

## Two apps

| App | Folder | Who it is for |
|---|---|---|
| **Android app** | `J Voice android app` | Readers and students, plus the in-app reporter, editor and admin roles |
| **Admin console (web)** | `J Voice web` | Editors and admins in the browser. One **Admin** login owns both modules; a separate **Editor** login is limited to reviewing and publishing news |

```bash
cd "J Voice web" && npm install && npm run dev    # http://localhost:5173
```

## Modules

- **Module 1 · News** — reader feed, short video clips, and the reporter → editor →
  admin publishing pipeline, plus an AI Shorts video generator (mocked).
- **Module 2 · Study & Exams** — subjects, topics, articles, quizzes, timed daily
  exams and grand tests, performance analysis, weak-area plans and leaderboards.

## Deploy the site

The web console and the public reader are one bundle on Firebase Hosting
(project `jvoice-b4b2e`). `firebase.json` lives at the repo root, not in
`firebase/`, because a deploy spans both folders and Hosting refuses a `public`
path outside the config's own directory.

```bash
cd firebase && npm run deploy:web      # builds 'J Voice web' then deploys hosting
```

| | |
|---|---|
| Firebase URL | <https://jvoice-b4b2e.web.app> |
| Custom domain | `jvoicetelugu.com` (GoDaddy) — see [firebase/README.md](firebase/README.md#custom-domain) |

Always build before deploying. `deploy:web` does both; a bare
`firebase deploy --only hosting` ships whatever is already in `dist/`, stale or not.

## Build the Android app

```bash
cd "J Voice android app"
./gradlew assembleDebug     # APK -> app/build/outputs/apk/debug/app-debug.apk
./gradlew installDebug
```

JDK 17 · Android SDK 34 · minSdk 24.
