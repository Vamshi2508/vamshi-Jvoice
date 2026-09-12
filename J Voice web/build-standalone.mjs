/**
 * Bundles the console into one self-contained HTML fragment — CSS and JS
 * inlined, no external requests — for publishing as a hosted page.
 */
import { readFileSync, writeFileSync, readdirSync } from 'node:fs'
import { join } from 'node:path'

const dist = 'dist'
const assets = join(dist, 'assets')
const files = readdirSync(assets)
const css = files.filter((f) => f.endsWith('.css')).map((f) => readFileSync(join(assets, f), 'utf8')).join('\n')
const js = files.filter((f) => f.endsWith('.js')).map((f) => readFileSync(join(assets, f), 'utf8')).join('\n')

// a literal </script> inside the bundle would close the tag early
const safeJs = js.replaceAll('</script>', '<\/script>')

// The favicon is inlined as a data URI rather than linked. This file's whole
// promise is "no external requests", and a linked /favicon-32.png would be one -
// it would 404 wherever the fragment is embedded.
const favicon = readFileSync(join('public', 'favicon-32.png'))
const faviconUri = 'data:image/png;base64,' + favicon.toString('base64')

const out = `<title>J Voice Console</title>
<link rel="icon" type="image/png" href="${faviconUri}">
<meta name="theme-color" content="#17318f">
<style>
${css}
</style>
<div id="root"></div>
<script type="module">
${safeJs}
</script>
`
writeFileSync(process.argv[2] || 'jvoice-console.html', out)
console.log('wrote', (out.length / 1024).toFixed(0) + 'kB')
