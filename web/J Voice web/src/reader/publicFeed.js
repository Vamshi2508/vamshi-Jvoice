/**
 * The reader's own Firestore feed.
 *
 * Deliberately separate from `store/firestoreData.js`, which is the *console's*
 * store: that one subscribes to every collection, including the desk-only
 * question bank, and it writes back. The reader is anonymous and read-only, so it
 * subscribes to exactly what `firestore.rules` lets an unauthenticated caller
 * see:
 *
 *   articles       where status == PUBLISHED   (a draft must never leak)
 *   studyArticles  where status == PUBLISHED
 *   categories / subjects / topics / examTracks / exams — public by rule
 *
 * ## Why the ordering is done here and not in the query
 *
 * `where('status','==',…)` combined with `orderBy('sortAt')` is an equality plus
 * a range on a different field, which Firestore can only serve from a COMPOSITE
 * index — one that has to be created in the console before the query works at
 * all. Sorting the (bounded) result in JavaScript instead means the reader runs
 * against a fresh project with no index setup. `FEED_LIMIT` keeps that honest.
 */

import { collection, limit as fsLimit, onSnapshot, query, where } from 'firebase/firestore'
import { useEffect, useMemo, useState } from 'react'
import { getFirebaseStore, isFirebaseReady } from '../firebase.js'
import { CODECS } from '../store/firestoreData.js'

/** Upper bound on documents pulled per collection — see the note above. */
const FEED_LIMIT = 300

/**
 * The console writes `Published`, the Android app and the seed write `PUBLISHED`.
 * Both mean the same thing to a reader, so compare case-insensitively and never
 * show a story on the strength of a status we do not recognise.
 */
export const isPublished = (status) => String(status ?? '').toUpperCase() === 'PUBLISHED'

/**
 * Each entry is one live subscription: the state key, the collection, and the
 * server-side filter (null = read the whole collection).
 */
const FEEDS = [
  { key: 'articles', codec: 'articles', name: 'articles', filter: where('status', '==', 'PUBLISHED') },
  { key: 'categories', codec: 'categories', name: 'categories', filter: null },
  { key: 'subjects', codec: 'subjects', name: 'subjects', filter: null },
  { key: 'topics', codec: 'topics', name: 'topics', filter: null },
  { key: 'tracks', codec: 'tracks', name: 'examTracks', filter: null },
  {
    key: 'studyArticles',
    codec: 'studyArticles',
    name: 'studyArticles',
    filter: where('status', '==', 'PUBLISHED')
  }
]

const EMPTY = Object.freeze({
  articles: [],
  categories: [],
  subjects: [],
  topics: [],
  tracks: [],
  studyArticles: []
})

/**
 * Live public content.
 *
 * `status` is `'loading'` until every subscription has delivered its first
 * snapshot, then `'ready'`; `'off'` when the build has no Firebase at all. A
 * per-collection denial does not fail the whole feed — that collection simply
 * stays empty, which is what a reader should see.
 */
export function usePublicFeed() {
  const [data, setData] = useState(EMPTY)
  const [status, setStatus] = useState(isFirebaseReady() ? 'loading' : 'off')
  const [error, setError] = useState(null)

  useEffect(() => {
    if (!isFirebaseReady()) {
      setStatus('off')
      return
    }
    const db = getFirebaseStore()
    const settled = new Set()

    const markSettled = (key) => {
      settled.add(key)
      if (settled.size === FEEDS.length) setStatus('ready')
    }

    const stops = FEEDS.map((feed) => {
      const ref = collection(db, feed.name)
      const q = feed.filter ? query(ref, feed.filter, fsLimit(FEED_LIMIT)) : query(ref, fsLimit(FEED_LIMIT))
      const decode = CODECS[feed.codec].from
      return onSnapshot(
        q,
        (snap) => {
          const rows = snap.docs
            .map((d) => {
              try {
                return decode(d.id, d.data())
              } catch (e) {
                console.warn(`[reader] skipping malformed ${feed.name}/${d.id}:`, e.message)
                return null
              }
            })
            .filter(Boolean)
          setData((prev) => ({ ...prev, [feed.key]: rows }))
          markSettled(feed.key)
        },
        (e) => {
          console.warn(`[reader] ${feed.name}: ${e.message}`)
          setError((prev) => prev ?? e)
          markSettled(feed.key)
        }
      )
    })

    return () => stops.forEach((stop) => stop())
  }, [])

  return useMemo(() => ({ ...data, status, error }), [data, status, error])
}

/** Newest first. `sortAt` is denormalised by the codec's writer; fall back for old rows. */
export const byNewest = (a, b) =>
  (b.publishedAt ?? b.createdAt ?? 0) - (a.publishedAt ?? a.createdAt ?? 0)
