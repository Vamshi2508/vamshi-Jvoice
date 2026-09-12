/**
 * Uploads the Study module's content — subjects, topics, exam tracks, study
 * articles, the question bank, quizzes and exams — into Firestore.
 *
 * ## Where the input comes from
 *
 * `jvoice-seed.json`, produced by `SeedExportTest` in the Android project:
 *
 *   cd "J Voice android app"
 *   ./gradlew :app:testDebugUnitTest --tests '*SeedExportTest'
 *
 * The content is authored as typed Kotlin with bilingual
 * `LocalizedText` pairs, so it is serialised by the same `toMap()` codecs the app
 * writes with rather than retyped here. Hand-copying ~5,600 Telugu codepoints
 * into a JavaScript literal is exactly how translations get corrupted.
 *
 * ## Why the Admin SDK
 *
 * Same reason as `seed-news.mjs`: the security rules require a signed-in desk
 * role to write content, and there is no interactive session here. The Admin SDK
 * bypasses rules, which is what a seeding tool should do.
 *
 * ## Document ids are preserved, deliberately
 *
 * The ids are meaningful and cross-referencing: topics carry `subjectId`,
 * quizzes and exams carry `questionIds`, exams also carry `subjectIds`. Letting
 * Firestore generate ids would break every one of those references, so the
 * exported ids are used verbatim and the references are verified before anything
 * is written.
 *
 *   node seed-content.mjs --dry-run
 *   node seed-content.mjs
 *   node seed-content.mjs --verify    # compare Firestore against the export
 */

import { readFileSync } from 'node:fs'
import { fileURLToPath } from 'node:url'
import { dirname, join } from 'node:path'
import { initializeApp, applicationDefault } from 'firebase-admin/app'
import { getFirestore } from 'firebase-admin/firestore'

const PROJECT_ID = 'jvoice-b4b2e'
const DRY_RUN = process.argv.includes('--dry-run')
const VERIFY = process.argv.includes('--verify')
const HERE = dirname(fileURLToPath(import.meta.url))
const SEED_FILE = join(HERE, 'jvoice-seed.json')

/**
 * Collection order matters on upload: a document is written only after whatever
 * it points at. A reader who opens the app mid-upload then sees a topic whose
 * subject already exists, rather than one pointing into a gap.
 */
const ORDER = [
  'subjects',
  'topics',
  'examTracks',
  'studyArticles',
  'questions',
  'quizzes',
  'exams'
]

/**
 * Which fields reference which collection. Checked before the first write, so a
 * broken export fails with a list of dangling ids instead of half-uploading.
 *
 *   [field, target collection, isList]
 */
const REFERENCES = {
  topics: [['subjectId', 'subjects', false]],
  studyArticles: [
    ['subjectId', 'subjects', false],
    ['topicId', 'topics', false]
  ],
  questions: [
    ['subjectId', 'subjects', false],
    ['topicId', 'topics', false]
  ],
  quizzes: [
    ['subjectId', 'subjects', false],
    ['topicId', 'topics', false],
    ['questionIds', 'questions', true]
  ],
  exams: [
    ['questionIds', 'questions', true],
    ['subjectIds', 'subjects', true],
    ['trackId', 'examTracks', false]
  ]
}

/** Firestore caps a batch at 500 writes. */
const BATCH_LIMIT = 500

// ---------------------------------------------------------------------------

console.log(`J Voice study-content seeding — project ${PROJECT_ID}`)
if (DRY_RUN) console.log('MODE: dry run, nothing will be written')
console.log()

let seed
try {
  seed = JSON.parse(readFileSync(SEED_FILE, 'utf8'))
} catch (e) {
  console.error(`✗ Cannot read ${SEED_FILE}`)
  console.error(`  ${e.message}`)
  console.error()
  console.error('  Produce it first, from the Android project:')
  console.error("    ./gradlew :app:testDebugUnitTest --tests '*SeedExportTest'")
  process.exit(1)
}

/* ------------------------------------------------------------------ validate */

const missing = ORDER.filter((c) => !seed[c])
if (missing.length) {
  console.error(`✗ Export is missing collections: ${missing.join(', ')}`)
  process.exit(1)
}

const extra = Object.keys(seed).filter((c) => !ORDER.includes(c))
if (extra.length) {
  // Not fatal, but silently dropping a collection the exporter has started
  // writing is worse than saying so.
  console.log(`  ! export carries collections this script does not upload: ${extra.join(', ')}`)
}

const ids = Object.fromEntries(ORDER.map((c) => [c, new Set(Object.keys(seed[c]))]))
const dangling = []

for (const [collection, refs] of Object.entries(REFERENCES)) {
  for (const [docId, doc] of Object.entries(seed[collection])) {
    for (const [field, target, isList] of refs) {
      const value = doc[field]
      // Absent and empty are both legitimate: a study article can sit on a
      // subject with no topic, and an exam need not belong to a track.
      if (value == null || value === '') continue
      const candidates = isList ? value : [value]
      for (const ref of candidates) {
        if (!ids[target].has(ref)) {
          dangling.push(`${collection}/${docId}.${field} -> ${target}/${ref}`)
        }
      }
    }
  }
}

if (dangling.length) {
  console.error(`✗ ${dangling.length} dangling reference(s) in the export — nothing written:`)
  dangling.slice(0, 20).forEach((d) => console.error(`    ${d}`))
  if (dangling.length > 20) console.error(`    … and ${dangling.length - 20} more`)
  console.error()
  console.error('  The export and the app models have drifted. Re-run SeedExportTest.')
  process.exit(1)
}

const total = ORDER.reduce((n, c) => n + ids[c].size, 0)
console.log(`  ✓ ${total} documents, all references resolve`)
for (const c of ORDER) console.log(`      ${String(ids[c].size).padStart(4)}  ${c}`)
console.log()

/** How much of the content is still English-only — reported, not blocked. */
const untranslated = []
for (const c of ORDER) {
  let n = 0
  for (const doc of Object.values(seed[c])) {
    if (hasEmptyTelugu(doc)) n++
  }
  if (n) untranslated.push(`${n}/${ids[c].size} ${c}`)
}
if (untranslated.length) {
  console.log(`  ! documents with an empty Telugu field: ${untranslated.join(', ')}`)
  console.log('    Uploaded as-is; the app falls back to English per i18n/localized.')
  console.log()
}

/** True if any `{en, te}` pair anywhere in the document has a blank `te`. */
function hasEmptyTelugu(value) {
  if (Array.isArray(value)) return value.some(hasEmptyTelugu)
  if (value && typeof value === 'object') {
    const keys = Object.keys(value)
    const isLt = keys.length === 2 && keys.includes('en') && keys.includes('te')
    if (isLt) return value.en !== '' && value.te === ''
    return Object.values(value).some(hasEmptyTelugu)
  }
  return false
}

if (DRY_RUN) {
  for (const c of ORDER) {
    const list = Object.keys(seed[c])
    console.log(`  would write ${list.length} docs to ${c}`)
    list.slice(0, 3).forEach((id) => console.log(`      ${c}/${id}`))
    if (list.length > 3) console.log(`      … and ${list.length - 3} more`)
  }
  console.log()
  console.log('Dry run complete. Nothing was written.')
  process.exit(0)
}

initializeApp({ credential: applicationDefault(), projectId: PROJECT_ID })
const db = getFirestore()

/* -------------------------------------------------------------------- verify */

if (VERIFY) {
  let bad = 0
  for (const collection of ORDER) {
    const snapshot = await db.collection(collection).get()
    const live = new Set(snapshot.docs.map((d) => d.id))
    const expected = ids[collection]
    const absent = [...expected].filter((id) => !live.has(id))
    // Extra documents are not a failure: the desk may have authored content in
    // the console since the upload, and this script must not imply that is wrong.
    const extras = [...live].filter((id) => !expected.has(id))

    const mark = absent.length ? '✗' : '✓'
    if (absent.length) bad++
    console.log(
      `  ${mark} ${String(live.size).padStart(4)}/${String(expected.size).padEnd(4)} ${collection}` +
        (extras.length ? `   (+${extras.length} not from the export)` : '')
    )
    absent.slice(0, 5).forEach((id) => console.log(`        missing ${collection}/${id}`))
    if (absent.length > 5) console.log(`        … and ${absent.length - 5} more missing`)
  }
  console.log()
  console.log(bad ? `✗ ${bad} collection(s) incomplete — re-run the upload.` : 'Verified.')
  process.exit(bad ? 1 : 0)
}

/* -------------------------------------------------------------------- upload */

let wrote = 0
for (const collection of ORDER) {
  const entries = Object.entries(seed[collection])
  for (let i = 0; i < entries.length; i += BATCH_LIMIT) {
    const chunk = entries.slice(i, i + BATCH_LIMIT)
    const batch = db.batch()
    // merge:true so re-running keeps any edit the desk has made through the
    // console to a field the export does not carry.
    for (const [id, data] of chunk) {
      batch.set(db.collection(collection).doc(id), data, { merge: true })
    }
    await batch.commit()
    wrote += chunk.length
  }
  console.log(`  ✓ ${String(entries.length).padStart(4)}  ${collection}`)
}

console.log()
console.log(`Done — ${wrote} documents written.`)
process.exit(0)
