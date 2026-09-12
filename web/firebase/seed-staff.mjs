/**
 * Creates the J Voice desk accounts in Firebase Auth and their `users/{uid}`
 * records in the Realtime Database.
 *
 * Why a script and not the console: the two halves have to agree. An Auth user
 * with no `users/{uid}` record can authenticate but has no role, so the app signs
 * it straight back out; a record with no Auth user is dead weight. Doing it by
 * hand in two consoles is how those drift apart.
 *
 * ## Running it
 *
 *   cd firebase
 *   npm install            # once, installs firebase-admin
 *   node seed-staff.mjs                # create/update, keep existing passwords
 *   node seed-staff.mjs --reset-passwords   # also reset passwords to the printed ones
 *   node seed-staff.mjs --dry-run      # print what it would do, touch nothing
 *
 * ## Credentials
 *
 * Uses Application Default Credentials, so it runs off the gcloud login already
 * on this machine:
 *
 *   gcloud auth application-default login
 *
 * Or set GOOGLE_APPLICATION_CREDENTIALS to a service-account JSON path.
 *
 * ## Passwords
 *
 * Generated per account and PRINTED ONCE at the end - except where an account
 * names a `passwordEnv`, in which case that environment variable supplies it, so
 * a chosen password never has to be written into this file.
 *
 * Either way the password is not stored anywhere afterwards
 * — not in this file, not in the database, not in a log. The database rules reject
 * a password field outright (see database.rules.json), and the app never writes one.
 * Copy them out of the terminal, hand them over, and rotate them after first use.
 */

import { initializeApp, applicationDefault } from 'firebase-admin/app'
import { getAuth } from 'firebase-admin/auth'
import { randomBytes } from 'node:crypto'
import { execFileSync } from 'node:child_process'
import { writeFileSync, unlinkSync } from 'node:fs'
import { tmpdir } from 'node:os'
import { join } from 'node:path'

const PROJECT_ID = 'jvoice-b4b2e'
const DATABASE_URL = 'https://jvoice-b4b2e-default-rtdb.asia-southeast1.firebasedatabase.app'

/** Staff sign in with a bare username; this is what it expands to. */
const LOGIN_DOMAIN = 'jvoicenews.com'

const DRY_RUN = process.argv.includes('--dry-run')
const RESET_PASSWORDS = process.argv.includes('--reset-passwords')

/**
 * The desk. Roles must match JvRole.code in the Android app and the regex in
 * database.rules.json — all three are the same list, and the rules will reject a
 * value the app would not have understood anyway.
 *
 * Readers and students are absent on purpose: they use the app without an
 * account.
 */
const STAFF = [
  {
    // The owner's account. Signs in with the full address, not a bare username -
    // toEmail() passes anything containing "@" through untouched, which is why an
    // external address works alongside the @jvoicenews.com ones.
    loginId: 'someshsheela14@gmail.com',
    name: 'Somesh Sheela',
    role: 'super_admin',
    phone: '7680096640',
    // Read from the environment, never committed. See the passwords note above.
    passwordEnv: 'JVOICE_SUPERADMIN_PASSWORD'
  },
  { loginId: 'kiran',   name: 'Kiran Kumar',   role: 'reporter', phone: '9000000005' },
  { loginId: 'sridevi', name: 'Sridevi Naidu', role: 'editor',   phone: '9000000003' }
]

/**
 * A readable but unguessable password: two short words plus digits.
 *
 * Not a raw base64 blob, because these get read down a phone to a district
 * reporter, and a password that cannot be dictated gets replaced with "jvoice123"
 * by whoever has to dictate it. Entropy comes from the digits and the word pair,
 * and every one of these is meant to be rotated after first sign-in.
 */
const WORDS = [
  'delta', 'harbour', 'monsoon', 'lantern', 'copper', 'marble', 'signal',
  'orchid', 'quartz', 'sparrow', 'cinder', 'meadow', 'anchor', 'velvet'
]

function generatePassword() {
  const bytes = randomBytes(4)
  const a = WORDS[bytes[0] % WORDS.length]
  const b = WORDS[bytes[1] % WORDS.length]
  const n = 100 + (((bytes[2] << 8) | bytes[3]) % 900)
  return `${a}-${b}-${n}`
}

function bail(message, error) {
  console.error(`\n✗ ${message}`)
  if (error) console.error(`  ${error.message ?? error}`)
  process.exit(1)
}

// ---------------------------------------------------------------------------

console.log(`J Voice staff seeding — project ${PROJECT_ID}`)
console.log(`login domain: @${LOGIN_DOMAIN}`)
if (DRY_RUN) console.log('MODE: dry run, nothing will be written')
if (RESET_PASSWORDS) console.log('MODE: passwords will be reset for existing accounts')
console.log()

let auth
try {
  // No databaseURL: this app instance is used for Auth only. See the header note
  // - user ADC cannot write to the Realtime Database, so that half goes through
  // the Firebase CLI at the end instead.
  initializeApp({
    credential: applicationDefault(),
    projectId: PROJECT_ID
  })
  auth = getAuth()
} catch (e) {
  bail(
    'Could not initialise the Admin SDK. Run `gcloud auth application-default login`, ' +
      'or set GOOGLE_APPLICATION_CREDENTIALS to a service-account JSON.',
    e
  )
}

/**
 * Is Firebase Authentication actually switched on for this project?
 *
 * Creating a project does NOT create its Auth configuration - that happens the
 * first time somebody opens Authentication in the console and enables a provider.
 * Until then every Admin SDK auth call fails with
 * "There is no configuration corresponding to the provided identifier", which
 * says nothing about what to do. This turns it into the one instruction that
 * matters, once, instead of eleven times.
 */
async function preflightAuth() {
  try {
    await auth.listUsers(1)
    return true
  } catch (e) {
    const message = e.message ?? ''
    const notConfigured =
      e.code === 'auth/configuration-not-found' ||
      message.includes('no configuration corresponding')
    if (notConfigured) {
      console.error('✗ Firebase Authentication is not enabled on this project yet.')
      console.error('')
      console.error('  Enable it once, in the console:')
      console.error(`    https://console.firebase.google.com/project/${PROJECT_ID}/authentication`)
      console.error('    Authentication -> Get started -> Email/Password -> Enable -> Save')
      console.error('')
      console.error('  It is free-tier; billing is NOT required. Then re-run this script.')
      return false
    }
    if (e.code === 'auth/insufficient-permission' || message.includes('PERMISSION_DENIED')) {
      console.error('✗ The credentials in use cannot manage Auth for this project.')
      console.error('')
      console.error('  Run: gcloud auth application-default login')
      console.error('  and make sure that account is an Owner/Editor on ' + PROJECT_ID)
      return false
    }
    console.error('✗ Unexpected Auth error during preflight:')
    console.error('  ' + message)
    return false
  }
}

if (!(await preflightAuth())) process.exit(1)

const created = []
const updated = []
const failed = []

/** uid -> users/{uid} record, written to the Realtime Database in one go below. */
const rtdbRecords = {}

for (const person of STAFF) {
  // Same expansion rule as the apps: a bare username gets the domain appended, an
  // address with "@" is used as-is.
  const email = person.loginId.includes('@')
    ? person.loginId
    : `${person.loginId}@${LOGIN_DOMAIN}`

  // An account may pin its password through the environment - used for the
  // owner's account, so the credential never enters the repository. Everything
  // else gets a generated one.
  const pinned = person.passwordEnv ? process.env[person.passwordEnv] : null
  if (person.passwordEnv && !pinned) {
    console.log(
      `  ! ${email} expects ${person.passwordEnv} in the environment; using a generated password instead`
    )
  }
  const password = pinned || generatePassword()

  try {
    // Is there already an account? Deciding this by lookup rather than by
    // catching a create conflict keeps the "existing" path free of side effects,
    // so re-running the script is safe.
    let user = null
    try {
      user = await auth.getUserByEmail(email)
    } catch (e) {
      if (e.code !== 'auth/user-not-found') throw e
    }

    if (DRY_RUN) {
      console.log(`  would ${user ? 'update' : 'create'}  ${email.padEnd(30)} ${person.role}`)
      continue
    }

    if (!user) {
      user = await auth.createUser({
        email,
        password,
        displayName: person.name,
        emailVerified: true // Internal desk accounts; there is no verification flow.
      })
      created.push({ ...person, email, password, uid: user.uid })
    } else {
      if (RESET_PASSWORDS) {
        await auth.updateUser(user.uid, { password, displayName: person.name })
        updated.push({ ...person, email, password, uid: user.uid })
      } else {
        await auth.updateUser(user.uid, { displayName: person.name })
        updated.push({ ...person, email, password: null, uid: user.uid })
      }
    }

    // The role also goes on the ID token as a custom claim, because the Firestore
    // rules read `request.auth.token.role`. Firestore rules cannot reach the
    // Realtime Database, and a claim costs nothing to check - whereas the
    // alternative (a get() on a mirrored document) is a billed read on every rule
    // evaluation, on every document of every list query.
    //
    // Set on every run, not only on create, so fixing a role in the STAFF list
    // above actually reaches the token. The client picks it up on its next token
    // refresh (about an hour, or immediately on sign-out/in).
    await auth.setCustomUserClaims(user.uid, { role: person.role })

    // Collected now, written in one CLI call at the end.
    //
    // `isLogin` and `createdAt` are set only for accounts created on this run.
    // Including them on a re-run would silently re-enable an account an admin had
    // deliberately disabled - which is the whole point of the kill-switch.
    const isNew = created.some((c) => c.uid === user.uid)
    rtdbRecords[user.uid] = {
      uid: user.uid,
      loginId: person.loginId,
      name: person.name,
      email,
      phone: person.phone,
      role: person.role,
      ...(isNew ? { isLogin: true, createdAt: Date.now() } : {})
    }

    console.log(`  ✓ ${email.padEnd(30)} ${person.role.padEnd(16)} ${user.uid}`)
  } catch (e) {
    failed.push({ ...person, email, error: e.message })
    console.log(`  ✗ ${email.padEnd(30)} ${e.message}`)
  }
}

console.log()
if (DRY_RUN) {
  console.log('Dry run complete. Nothing was written.')
  process.exit(0)
}

/**
 * Writes the `users/` records through the Firebase CLI.
 *
 * A PATCH at `/users` rather than a PUT: a PUT would replace the whole tree and
 * delete any account not in STAFF. Per-uid keys mean this merges.
 */
if (Object.keys(rtdbRecords).length) {
  const payload = join(tmpdir(), `jvoice-users-${Date.now()}.json`)
  writeFileSync(payload, JSON.stringify(rtdbRecords, null, 2))
  try {
    // shell:true on Windows so PATH resolution finds the firebase shim. Without
    // it execFileSync looks for a literal executable and fails even though the
    // same command works from a terminal.
    execFileSync(
      'firebase',
      ['database:update', '/users', payload, '--project', PROJECT_ID, '--force'],
      { stdio: 'inherit', shell: process.platform === 'win32' }
    )
    console.log(`\u2713 wrote ${Object.keys(rtdbRecords).length} users/{uid} record(s) to the Realtime Database`)
  } catch (e) {
    console.error('\u2717 Could not write the users/ records via the Firebase CLI.')
    console.error('  The Auth accounts exist, but the app cannot resolve their roles without these.')
    console.error(`  Payload kept for a manual retry: ${payload}`)
    console.error(`  firebase database:update /users "${payload}" --project ${PROJECT_ID}`)
    process.exit(1)
  }
  unlinkSync(payload)
}

console.log(`created: ${created.length}   updated: ${updated.length}   failed: ${failed.length}`)

const withPasswords = [...created, ...updated.filter((u) => u.password)]
if (withPasswords.length) {
  console.log('\n' + '─'.repeat(66))
  console.log('PASSWORDS — shown once, not stored anywhere. Copy them now.')
  console.log('─'.repeat(66))
  for (const u of withPasswords) {
    console.log(`  ${u.loginId.padEnd(10)} ${u.password.padEnd(24)} ${u.role}`)
  }
  console.log('─'.repeat(66))
  console.log('Staff sign in with the bare login id, not the full address.')
  console.log('Rotate these after first sign-in.')
}

if (updated.some((u) => !u.password)) {
  console.log(
    `\n${updated.filter((u) => !u.password).length} existing account(s) kept their current password. ` +
      'Re-run with --reset-passwords to change them.'
  )
}

if (failed.length) {
  console.log('\nFailures:')
  for (const f of failed) console.log(`  ${f.email}: ${f.error}`)
  process.exit(1)
}

process.exit(0)
