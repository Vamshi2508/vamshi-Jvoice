/**
 * AI Shorts seed data — the mocked news-to-video pipeline from the Android app.
 */

const HOUR = 60 * 60 * 1000
const now = Date.now()

/** DRAFT → Script ready → Rendering → Ready → Approved → Published (or Failed) */
export const SHORT_STATUS = {
  DRAFT: 'Draft',
  SCRIPT_READY: 'Script ready',
  RENDERING: 'Rendering',
  READY: 'Ready',
  APPROVED: 'Approved',
  PUBLISHED: 'Published',
  FAILED: 'Failed'
}

export const SHORT_STATUS_ORDER = [
  SHORT_STATUS.DRAFT,
  SHORT_STATUS.SCRIPT_READY,
  SHORT_STATUS.RENDERING,
  SHORT_STATUS.READY,
  SHORT_STATUS.APPROVED,
  SHORT_STATUS.PUBLISHED
]

export const templates = [
  { id: 'tpl_breaking', name: 'Breaking Red', category: 'Breaking', isActive: true, isDefault: true, description: 'Bold red banner, fast cuts, ticker at the bottom.' },
  { id: 'tpl_explainer', name: 'Explainer Clean', category: 'Explainer', isActive: true, isDefault: false, description: 'Calm palette, large captions, slow pans.' },
  { id: 'tpl_sports', name: 'Sports Energy', category: 'Sports', isActive: true, isDefault: false, description: 'High contrast, score strip, punchy transitions.' },
  { id: 'tpl_govt', name: 'Government Notice', category: 'Jobs', isActive: true, isDefault: false, description: 'Formal layout for notifications and results.' },
  { id: 'tpl_retro', name: 'Retro Print', category: 'Feature', isActive: false, isDefault: false, description: 'Newspaper texture. Disabled for now.' }
]

export const voices = [
  { id: 'v_te_f', name: 'Sirisha', language: 'Telugu', gender: 'Female' },
  { id: 'v_te_m', name: 'Raghu', language: 'Telugu', gender: 'Male' },
  { id: 'v_en_f', name: 'Maya', language: 'English', gender: 'Female' },
  { id: 'v_en_m', name: 'Arjun', language: 'English', gender: 'Male' }
]

export const LANGUAGES = [
  { code: 'te', label: 'Telugu', available: true },
  { code: 'en', label: 'English', available: true },
  { code: 'hi', label: 'Hindi', available: false },
  { code: 'ta', label: 'Tamil', available: false },
  { code: 'kn', label: 'Kannada', available: false }
]

const scene = (id, text, seconds) => ({ id, text, seconds, mediaLabel: 'Stock clip ' + id })

export const shorts = [
  {
    id: 'sh1',
    newsId: 'n1',
    title: 'తెలంగాణలో కొత్త విద్యా విధానం',
    status: SHORT_STATUS.PUBLISHED,
    templateId: 'tpl_explainer',
    voiceId: 'v_te_f',
    language: 'Telugu',
    durationSeconds: 45,
    createdBy: 'Anitha Reddy',
    createdAt: now - 5 * HOUR,
    views: 42100,
    scenes: [
      scene('s1', 'రాష్ట్ర మంత్రివర్గం కొత్త విద్యా విధానానికి ఆమోదం తెలిపింది.', 12),
      scene('s2', 'వచ్చే విద్యా సంవత్సరం నుంచి అమలు.', 10),
      scene('s3', 'ఉపాధ్యాయ నియామకాలు, డిజిటల్ తరగతులు ప్రధాన అంశాలు.', 13),
      scene('s4', 'పూర్తి వివరాలు J Voice యాప్‌లో.', 10)
    ]
  },
  {
    id: 'sh2',
    newsId: 'n2',
    title: 'హైదరాబాద్‌లో భారీ వర్షాలు',
    status: SHORT_STATUS.READY,
    templateId: 'tpl_breaking',
    voiceId: 'v_te_m',
    language: 'Telugu',
    durationSeconds: 30,
    createdBy: 'Sridevi Naidu',
    createdAt: now - 2 * HOUR,
    views: 0,
    scenes: [
      scene('s1', 'నగరంలో ఎడతెరిపి లేని వర్షం.', 10),
      scene('s2', 'లోతట్టు ప్రాంతాల్లో నీటి నిల్వ.', 10),
      scene('s3', 'జీహెచ్ఎంసీ సిబ్బంది రంగంలోకి.', 10)
    ]
  },
  {
    id: 'sh3',
    newsId: 'n3',
    title: 'Technology sector growth',
    status: SHORT_STATUS.SCRIPT_READY,
    templateId: 'tpl_explainer',
    voiceId: 'v_en_f',
    language: 'English',
    durationSeconds: 40,
    createdBy: 'Anitha Reddy',
    createdAt: now - 40 * 60 * 1000,
    views: 0,
    scenes: [
      scene('s1', 'The IT sector posted double-digit export growth this quarter.', 14),
      scene('s2', 'Hiring is expected to pick up in the second half.', 13),
      scene('s3', 'Read the full story on J Voice.', 13)
    ]
  },
  {
    id: 'sh4',
    newsId: 'n12',
    title: 'Metro rail extension cleared',
    status: SHORT_STATUS.APPROVED,
    templateId: 'tpl_govt',
    voiceId: 'v_en_m',
    language: 'English',
    durationSeconds: 35,
    createdBy: 'Ravi Teja Sharma',
    createdAt: now - 26 * HOUR,
    views: 0,
    scenes: [
      scene('s1', 'The metro extension received central clearance.', 12),
      scene('s2', 'A 26 km stretch will reach the airport corridor.', 12),
      scene('s3', 'Work starts after the monsoon.', 11)
    ]
  },
  {
    id: 'sh5',
    newsId: 'n11',
    title: 'Vehicle theft case — two arrested',
    status: SHORT_STATUS.FAILED,
    templateId: 'tpl_breaking',
    voiceId: 'v_en_m',
    language: 'English',
    durationSeconds: 25,
    createdBy: 'Anitha Reddy',
    createdAt: now - 3 * HOUR,
    views: 0,
    failureReason: 'Voice generation timed out in the demo pipeline. Regenerate the voice track.',
    scenes: [scene('s1', 'Police arrested two persons and recovered four two-wheelers.', 25)]
  }
]
