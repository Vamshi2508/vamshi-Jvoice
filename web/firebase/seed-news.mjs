/**
 * Seeds the News module's starting content into Firestore: the categories, and
 * two real articles filed by the seeded reporter.
 *
 * ## Why the Admin SDK and not the app
 *
 * The security rules require a signed-in desk role to write articles, and pin
 * `reporterId` to the caller's uid. That is correct for the app, but it means
 * content cannot be planted from outside without an account. The Admin SDK
 * bypasses rules, which is exactly what a seeding tool should do — and it lets
 * the articles carry the *real* uid of the seeded reporter, so the workflow
 * screens ("my stories", reporter stats) treat them as genuinely his.
 *
 * ## Shape
 *
 * Every field mirrors `NewsCodec.kt` exactly — LocalizedText as `{en, te}`, enums
 * by name, timestamps as epoch millis. If the two ever disagree the app will
 * silently read defaults, so they are kept in step deliberately.
 *
 *   node seed-news.mjs
 *   node seed-news.mjs --dry-run
 */

import { initializeApp, applicationDefault } from 'firebase-admin/app'
import { getFirestore } from 'firebase-admin/firestore'

const PROJECT_ID = 'jvoice-b4b2e'
const DRY_RUN = process.argv.includes('--dry-run')

/** The seeded reporter and editor, from seed-staff.mjs. */
const REPORTER = { uid: '37JhY2LczaeEpBRxkMgzo5IgX5F3', name: 'Kiran Kumar' }

/** `{en, te}` — the wire shape of LocalizedText. */
const lt = (en, te) => ({ en, te })

const NOW = Date.now()
const HOUR = 60 * 60 * 1000

/**
 * Categories.
 *
 * Seeded because an article without a category renders as "General" everywhere
 * and the desk's category filter would be empty — these are the sections the
 * newsroom actually files into, not demo content.
 */
const CATEGORIES = [
  { id: 'cat_telangana',   name: lt('Telangana', 'తెలంగాణ'),        emoji: '🏛️' },
  { id: 'cat_ap',          name: lt('Andhra Pradesh', 'ఆంధ్రప్రదేశ్'), emoji: '🌾' },
  { id: 'cat_india',       name: lt('India', 'భారత్'),               emoji: '🇮🇳' },
  { id: 'cat_politics',    name: lt('Politics', 'రాజకీయాలు'),        emoji: '🗳️' },
  { id: 'cat_crime',       name: lt('Crime', 'క్రైమ్'),               emoji: '🚨' },
  { id: 'cat_business',    name: lt('Business', 'బిజినెస్'),          emoji: '📈' },
  { id: 'cat_education',   name: lt('Education', 'విద్య'),           emoji: '🎓' },
  { id: 'cat_technology',  name: lt('Technology', 'టెక్నాలజీ'),       emoji: '💻' },
  { id: 'cat_sports',      name: lt('Sports', 'క్రీడలు'),             emoji: '🏏' },
  { id: 'cat_entertainment', name: lt('Entertainment', 'సినిమా'),    emoji: '🎬' },
  { id: 'cat_health',      name: lt('Health', 'ఆరోగ్యం'),            emoji: '🏥' },
  { id: 'cat_jobs',        name: lt('Jobs', 'ఉద్యోగాలు'),            emoji: '💼' }
].map((c) => ({ ...c, isEnabled: true }))

/**
 * Two articles.
 *
 * Both PUBLISHED so they appear in the reader feed immediately — the point is to
 * see the app working end to end. They carry the reporter's real uid, so they
 * also show up under his "My news" and in his reporter statistics.
 */
const ARTICLES = [
  {
    id: 'jv_launch_note',
    headline: lt(
      'J Voice goes live with Telugu and English news',
      'తెలుగు, ఇంగ్లీష్ వార్తలతో J Voice ప్రారంభం'
    ),
    shortDescription: lt(
      'Every story on J Voice is written in both languages, and readers choose which one they read in.',
      'J Voice లోని ప్రతి కథనం రెండు భాషల్లోనూ ఉంటుంది. ఏ భాషలో చదవాలో పాఠకులే ఎంచుకోవచ్చు.'
    ),
    content: lt(
      'Hyderabad: J Voice has begun publishing in Telugu and English together. Every article, ' +
        'every study page and every exam question carries both languages, and a single switch in ' +
        'the profile tab changes the whole app.\n\n' +
        'The newsroom files once. A reporter writes in whichever language the story reaches them in, ' +
        'and the desk adds the second version before it goes live. Readers never see a gap: if one ' +
        'language is still being written, the story is shown in the other rather than hidden.',
      'హైదరాబాద్: J Voice తెలుగు, ఇంగ్లీష్ రెండు భాషల్లోనూ వార్తల ప్రచురణ ప్రారంభించింది. ప్రతి కథనం, ' +
        'ప్రతి స్టడీ పేజీ, ప్రతి పరీక్ష ప్రశ్న రెండు భాషల్లోనూ ఉంటాయి. ప్రొఫైల్ ట్యాబ్‌లోని ఒక్క స్విచ్‌తో ' +
        'యాప్ మొత్తం మారుతుంది.\n\n' +
        'న్యూస్‌రూమ్ ఒకసారే ఫైల్ చేస్తుంది. రిపోర్టర్ తనకు అందిన భాషలో రాస్తారు, ప్రచురణకు ముందు డెస్క్ ' +
        'రెండో వెర్షన్ చేర్చుతుంది. ఒక భాష ఇంకా సిద్ధం కాకపోతే కథనాన్ని దాచకుండా మరో భాషలో చూపుతాము.'
    ),
    categoryId: 'cat_telangana',
    location: 'Hyderabad',
    imageUrl: 'https://picsum.photos/seed/jvlaunch/900/600',
    tags: [lt('jvoice', 'జెవాయిస్'), lt('telugu', 'తెలుగు'), lt('launch', 'ప్రారంభం')],
    isBreaking: true,
    isFeatured: true,
    publishedAt: NOW - 2 * HOUR
  },
  {
    id: 'jv_exam_prep_note',
    headline: lt(
      'Exam preparation section opens for Group-2 and Constable aspirants',
      'గ్రూప్-2, కానిస్టేబుల్ అభ్యర్థుల కోసం పరీక్షల విభాగం'
    ),
    shortDescription: lt(
      'Syllabus, study material, topic quizzes and daily tests, scoped to the exam a student picks.',
      'విద్యార్థి ఎంచుకున్న పరీక్ష ప్రకారం సిలబస్, స్టడీ మెటీరియల్, టాపిక్ క్విజ్‌లు, రోజువారీ టెస్టులు.'
    ),
    content: lt(
      'Hyderabad: The Study section of J Voice is now open. A student chooses the exam they are ' +
        'preparing for — Police Constable, Group-2, SSC and others — and everything after that is ' +
        'scoped to it: the subjects, the topics, the question bank, the daily test and the rank list.\n\n' +
        'Study material is written in Telugu and English, question by question. Progress and results ' +
        'stay on the device, so no account is needed to start studying.',
      'హైదరాబాద్: J Voice స్టడీ విభాగం అందుబాటులోకి వచ్చింది. విద్యార్థి తాను ప్రిపేర్ అవుతున్న పరీక్షను ' +
        'ఎంచుకుంటే — పోలీస్ కానిస్టేబుల్, గ్రూప్-2, ఎస్‌ఎస్‌సీ తదితరాలు — ఆ తర్వాత సబ్జెక్టులు, టాపిక్‌లు, ' +
        'ప్రశ్న బ్యాంక్, రోజువారీ టెస్ట్, ర్యాంక్ జాబితా అన్నీ దానికే పరిమితమవుతాయి.\n\n' +
        'స్టడీ మెటీరియల్ ప్రతి ప్రశ్నా తెలుగు, ఇంగ్లీష్ రెండింటిలోనూ ఉంటుంది. ప్రగతి, ఫలితాలు ఫోన్‌లోనే ' +
        'ఉంటాయి కాబట్టి చదవడం మొదలుపెట్టడానికి ఖాతా అవసరం లేదు.'
    ),
    categoryId: 'cat_education',
    location: 'Hyderabad',
    imageUrl: 'https://picsum.photos/seed/jvstudy/900/600',
    tags: [lt('education', 'విద్య'), lt('exams', 'పరీక్షలు'), lt('group2', 'గ్రూప్-2')],
    isBreaking: false,
    isFeatured: true,
    publishedAt: NOW - 5 * HOUR
  }
]

/** Fills in the fields every article carries, so the codec never reads a default. */
function articleDoc(a) {
  const createdAt = a.publishedAt - HOUR
  return {
    headline: a.headline,
    shortDescription: a.shortDescription,
    content: a.content,
    categoryId: a.categoryId,
    location: a.location,
    imageUrl: a.imageUrl,
    photoUrls: [],
    videoUrls: [],
    tags: a.tags,
    isBreaking: a.isBreaking,
    isFeatured: a.isFeatured,
    isTrending: false,
    status: 'PUBLISHED',
    reporterId: REPORTER.uid,
    reporterName: REPORTER.name,
    createdAt,
    updatedAt: a.publishedAt,
    publishedAt: a.publishedAt,
    rejectionReason: null,
    editorNote: null,
    views: 0,
    reportCount: 0,
    // Denormalised sort key — see NewsCodec.articleToMap.
    sortAt: a.publishedAt
  }
}

// ---------------------------------------------------------------------------

console.log(`J Voice news seeding — project ${PROJECT_ID}`)
if (DRY_RUN) console.log('MODE: dry run, nothing will be written')
console.log()

initializeApp({ credential: applicationDefault(), projectId: PROJECT_ID })
const db = getFirestore()

let wrote = 0

for (const c of CATEGORIES) {
  const { id, ...data } = c
  if (DRY_RUN) {
    console.log(`  would write categories/${id}`)
    continue
  }
  await db.collection('categories').doc(id).set(data, { merge: true })
  wrote++
}
if (!DRY_RUN) console.log(`  ✓ ${CATEGORIES.length} categories`)

for (const a of ARTICLES) {
  if (DRY_RUN) {
    console.log(`  would write articles/${a.id}  "${a.headline.en}"`)
    continue
  }
  await db.collection('articles').doc(a.id).set(articleDoc(a), { merge: true })
  console.log(`  ✓ articles/${a.id}`)
  console.log(`      EN  ${a.headline.en}`)
  console.log(`      TE  ${a.headline.te}`)
  wrote++
}

console.log()
console.log(DRY_RUN ? 'Dry run complete.' : `Done — ${wrote} documents written.`)
process.exit(0)
