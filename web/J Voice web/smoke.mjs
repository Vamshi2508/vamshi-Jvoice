// Drives the running dev server with headless Chromium and captures every
// screen. Start `npm run dev` first, then `npm run smoke`.
import { chromium } from 'playwright'
import { mkdirSync } from 'node:fs'

const OUT = process.argv[2] || 'screenshots'
const BASE = 'http://localhost:5173'
mkdirSync(OUT, { recursive: true })

const errors = []
const browser = await chromium.launch({ args: ['--no-sandbox'] })
const page = await (await browser.newContext({ viewport: { width: 1500, height: 1000 } })).newPage()
page.on('console', (m) => m.type() === 'error' && errors.push(m.text()))
page.on('pageerror', (e) => errors.push('pageerror: ' + e.message))

const shot = async (name, full = false) => {
  await page.waitForTimeout(350)
  await page.screenshot({ path: `${OUT}/${name}.png`, fullPage: full })
  console.log('  shot →', name + '.png')
}

// Navigate through the sidebar, never page.goto — a reload would drop the
// in-memory session and bounce us back to the login screen.
const go = async (navLabel, waitText) => {
  await page.getByRole('link', { name: navLabel }).click()
  await page.getByText(waitText, { exact: false }).first().waitFor({ timeout: 15000 })
  console.log('→ ' + navLabel)
}

// ---------------------------------------------------------------- login page
await page.goto(BASE)
// The wordmark now carries the brand name, so the heading is just the scope.
await page.getByRole('heading', { name: 'Admin Console' }).waitFor({ timeout: 20000 })
console.log('login page')
await shot('01-login')

// ------------------------------------------------------------ sign in as Admin
await page.getByRole('button', { name: 'Continue as Admin' }).click()
await page.getByRole('heading', { name: 'Dashboard' }).waitFor({ timeout: 15000 })
console.log('signed in as Admin')
await shot('02-dashboard-admin', true)

// ------------------------------------------------------------------ news flow
await page.getByRole('link', { name: /Review queue/ }).click()
await page.getByRole('heading', { name: 'Review queue' }).waitFor()
console.log('review queue')
await shot('03-review-queue')

// open the first story for review -> Under Review + editor pane
await page.getByRole('button', { name: 'Review' }).first().click()
await page.getByRole('heading', { name: 'Review story' }).waitFor()
console.log('opened a story for review (status flips to Under Review)')
await shot('04-article-review')

// approve & publish it
await page.getByRole('button', { name: 'Approve & publish' }).click()
await page.getByText('Published — live in the reader feed').waitFor({ timeout: 5000 })
console.log('approved & published — toast shown')
await shot('05-after-publish')

await go(/All news/, 'Filters')
await shot('06-all-news', true)

// ------------------------------------------------ news data entry (write a story)
const countBefore = await page.getByRole('heading', { name: /articles$/ }).innerText()
await page.getByRole('button', { name: 'New article' }).click()
await page.getByRole('heading', { name: 'New article' }).waitFor()
await page.getByLabel('Headline *').fill('కానిస్టేబుల్ నోటిఫికేషన్‌కు దరఖాస్తుల గడువు పొడిగింపు')
await page.getByLabel('Short description shown on cards').fill('దరఖాస్తు గడువును పది రోజులు పొడిగించారు.')
await page.getByLabel('Story body *').fill(
  'పోలీస్ కానిస్టేబుల్ నోటిఫికేషన్‌కు దరఖాస్తు చేసుకునే గడువును పది రోజులు పొడిగించినట్టు అధికారులు ప్రకటించారు.'
)
await page.getByLabel('Category').selectOption({ label: '🧑‍💼 Jobs' })
await page.getByLabel('Tags (comma separated)').fill('constable, jobs, notification')
await page.getByLabel('Mark as breaking').check()
console.log('wrote a new story in the console')
await shot('24-news-entry')

await page.getByRole('contentinfo').getByRole('button', { name: 'Publish now' }).click()
await page.getByText('Published — live in the reader feed').waitFor({ timeout: 5000 })
const countAfter = await page.getByRole('heading', { name: /articles$/ }).innerText()
console.log('published straight to the feed: ' + countBefore + ' → ' + countAfter)
await shot('25-news-published', true)

await go(/Categories/, 'categories')
await shot('07-categories')

await go(/AI Shorts/, 'Shorts')
await shot('08-ai-shorts', true)

// ----------------------------------------------------------------- study flow
await go(/Exam types/, 'exam types')
await shot('09-exam-types', true)

// open the Constable exam type editor with its subject-wise pattern
await page.getByRole('button', { name: 'Edit' }).first().click()
await page.getByRole('heading', { name: 'Edit exam type' }).waitFor()
console.log('exam type editor (pattern)')
await shot('10-exam-type-editor')
await page.getByRole('button', { name: 'Cancel' }).click()

await go(/Subjects/, 'subjects')
await shot('11-subjects', true)

await go(/Question bank/, 'questions')
await shot('12-question-bank')

await go(/Exams & tests/, 'Papers')
await shot('13-exams')

await go(/Results & ranks/, 'Attempts')
await shot('14-results', true)

await go(/Users/, 'accounts')
await shot('15-users')

// ------------------------------------------- study material data entry (write)
await go(/Study material/, 'Study material')
await shot('19-study-content', true)

await page.getByRole('button', { name: 'New study article' }).click()
await page.getByRole('heading', { name: 'New study article' }).waitFor()
await page.getByLabel('Title *').fill('Ratio & Proportion — shortcuts for Constable')
await page.getByLabel('Subject').selectOption({ label: '🔢 Mathematics' })
await page.getByLabel('Topic *').selectOption({ label: 'Number System' })
await page.getByLabel('Reading time (min)').fill('7')
await page.getByLabel('Short description shown in lists').fill(
  'Ratios, proportions and the three shortcuts that clear most Constable questions in under 20 seconds.'
)
await page.getByLabel('Article body *').fill(
  [
    'A ratio compares two quantities of the same kind. Written a : b, it means a/b.',
    '',
    'Work in parts, not in rupees: if the ratio is 3 : 5 and the total is 6400, one part is 800.'
  ].join('\n')
)
await page.getByLabel('Important points').fill(
  ['Ratio has no units — always simplify first', 'Product of means = product of extremes'].join('\n')
)
await page.getByLabel('Formulas').fill(
  ['Fourth proportional = (b × c) / a', 'One part = Total / (sum of ratio terms)'].join('\n')
)
await page.getByLabel('Solved examples').fill('Divide 6400 in 3 : 5 → 2400 and 4000')
await page.getByLabel('Author').fill('Sunitha Rao')
console.log('wrote a new study article')
await shot('20-study-article-editor')

await page.getByRole('contentinfo').getByRole('button', { name: 'Publish' }).click()
await page.getByText('Published to students').waitFor({ timeout: 5000 })
console.log('published to students')
await shot('21-article-saved', true)

const row = page.getByRole('row', { name: /Ratio & Proportion/ })
await row.waitFor()
await row.getByRole('button', { name: 'Open' }).click()
await page.getByRole('heading', { name: /Ratio & Proportion/ }).waitFor()
console.log('reads back with body, points, formulas and examples')
await shot('22-article-readback', true)
await page.getByRole('button', { name: 'Close' }).click()

await page.getByRole('row', { name: /Government schemes revision/ }).getByRole('button', { name: 'Edit' }).click()
await page.getByRole('heading', { name: 'Edit study article' }).waitFor()
console.log('existing article loads into the editor: ' + (await page.getByLabel('Title *').inputValue()))
await shot('23-edit-existing')
await page.getByRole('contentinfo').getByRole('button', { name: 'Save draft' }).click()
await page.getByText('Study article saved').waitFor({ timeout: 5000 })
console.log('saved back as a draft')

// ------------------------------------------------------- editor login (scoped)
await page.getByText('Sign out').click()
await page.getByRole('button', { name: 'Continue as Editor' }).waitFor()
await page.getByRole('button', { name: 'Continue as Editor' }).click()
await page.getByRole('heading', { name: 'Dashboard' }).waitFor()
console.log('signed in as Editor — sidebar is news-only')
await shot('16-editor-dashboard', true)

await page.getByRole('link', { name: /Review queue/ }).click()
await page.getByRole('heading', { name: 'Review queue' }).waitFor()
console.log('editor review queue')
await shot('17-editor-review-queue')

// an admin-only route must bounce the Editor back to the dashboard. Push the URL
// in-place (no reload) so the session survives and the router re-resolves.
await page.evaluate(() => {
  window.history.pushState({}, '', '/study/exam-types')
  window.dispatchEvent(new PopStateEvent('popstate'))
})
await page.getByRole('heading', { name: 'Dashboard' }).waitFor({ timeout: 8000 })
console.log('editor pushed to /study/exam-types → redirected to Dashboard, url is now ' + new URL(page.url()).pathname)
await shot('18-editor-blocked')

console.log('\nconsole errors:', errors.length ? errors : 'none')
await browser.close()
