/**
 * Module 1 (News) seed data — demo content only, mirrors the Android app's
 * MockData.kt. Nothing here talks to a server.
 */

const HOUR = 60 * 60 * 1000
const DAY = 24 * HOUR
const now = Date.now()

/** DRAFT → SUBMITTED → UNDER_REVIEW → APPROVED / REJECTED / SENT_BACK → PUBLISHED */
export const NEWS_STATUS = {
  DRAFT: 'Draft',
  SUBMITTED: 'Submitted',
  UNDER_REVIEW: 'Under Review',
  APPROVED: 'Approved',
  REJECTED: 'Rejected',
  SENT_BACK: 'Sent Back',
  PUBLISHED: 'Published'
}

export const categories = [
  { id: 'cat_ts', nameEn: 'Telangana', nameTe: 'తెలంగాణ', emoji: '🏛️', isEnabled: true },
  { id: 'cat_ap', nameEn: 'Andhra Pradesh', nameTe: 'ఆంధ్రప్రదేశ్', emoji: '🌾', isEnabled: true },
  { id: 'cat_india', nameEn: 'India', nameTe: 'భారత్', emoji: '🇮🇳', isEnabled: true },
  { id: 'cat_pol', nameEn: 'Politics', nameTe: 'రాజకీయాలు', emoji: '🗳️', isEnabled: true },
  { id: 'cat_crime', nameEn: 'Crime', nameTe: 'నేరాలు', emoji: '🚨', isEnabled: true },
  { id: 'cat_biz', nameEn: 'Business', nameTe: 'వాణిజ్యం', emoji: '💼', isEnabled: true },
  { id: 'cat_edu', nameEn: 'Education', nameTe: 'విద్య', emoji: '🎓', isEnabled: true },
  { id: 'cat_tech', nameEn: 'Technology', nameTe: 'సాంకేతికం', emoji: '💻', isEnabled: true },
  { id: 'cat_sports', nameEn: 'Sports', nameTe: 'క్రీడలు', emoji: '🏏', isEnabled: true },
  { id: 'cat_ent', nameEn: 'Entertainment', nameTe: 'వినోదం', emoji: '🎬', isEnabled: true },
  { id: 'cat_health', nameEn: 'Health', nameTe: 'ఆరోగ్యం', emoji: '🩺', isEnabled: true },
  { id: 'cat_jobs', nameEn: 'Jobs', nameTe: 'ఉద్యోగాలు', emoji: '🧑‍💼', isEnabled: false }
]

const article = (o) => ({
  tags: [],
  isBreaking: false,
  isFeatured: false,
  views: 0,
  reports: 0,
  rejectionReason: '',
  editorNote: '',
  imageUrl: '',
  ...o
})

export const newsArticles = [
  article({
    id: 'n1',
    headline: 'తెలంగాణలో కొత్త విద్యా విధానంపై కీలక నిర్ణయం',
    description: 'రాష్ట్ర మంత్రివర్గం కొత్త విద్యా విధానానికి ఆమోదం తెలిపింది.',
    body: 'రాష్ట్ర మంత్రివర్గం సమావేశంలో కొత్త విద్యా విధానానికి ఆమోదం లభించింది. వచ్చే విద్యా సంవత్సరం నుంచి అమలులోకి రానుంది. ఉపాధ్యాయ నియామకాలు, డిజిటల్ తరగతులు, మధ్యాహ్న భోజన పథకంలో మార్పులు ప్రధాన అంశాలుగా ఉన్నాయి.',
    categoryId: 'cat_edu',
    reporterId: 'u_rep1',
    status: NEWS_STATUS.PUBLISHED,
    tags: ['education', 'telangana'],
    isBreaking: true,
    isFeatured: true,
    views: 12480,
    createdAt: now - 3 * HOUR,
    publishedAt: now - 2 * HOUR
  }),
  article({
    id: 'n2',
    headline: 'హైదరాబాద్‌లో భారీ వర్షాలు, పలు ప్రాంతాల్లో నీటి నిల్వ',
    description: 'నగరంలో రాత్రి నుంచి ఎడతెరిపి లేని వర్షం కురుస్తోంది.',
    body: 'హైదరాబాద్ నగరంలో గత రాత్రి నుంచి భారీ వర్షాలు కురుస్తున్నాయి. లోతట్టు ప్రాంతాల్లో నీరు నిలిచిపోయింది. జీహెచ్ఎంసీ సిబ్బంది రంగంలోకి దిగారు.',
    categoryId: 'cat_ts',
    reporterId: 'u_rep2',
    status: NEWS_STATUS.PUBLISHED,
    tags: ['rain', 'hyderabad'],
    isBreaking: true,
    views: 20310,
    reports: 2,
    createdAt: now - 6 * HOUR,
    publishedAt: now - 5 * HOUR
  }),
  article({
    id: 'n3',
    headline: 'India records major growth in technology sector',
    description: 'Exports from the IT sector grew by double digits this quarter.',
    body: 'The technology sector reported strong quarterly growth driven by exports and domestic demand. Analysts expect hiring to pick up in the second half of the year.',
    categoryId: 'cat_tech',
    reporterId: 'u_rep3',
    status: NEWS_STATUS.PUBLISHED,
    tags: ['it', 'growth'],
    isFeatured: true,
    views: 8840,
    createdAt: now - 26 * HOUR,
    publishedAt: now - 25 * HOUR
  }),
  article({
    id: 'n4',
    headline: 'గ్రూప్-4 నోటిఫికేషన్ విడుదల, 9,100 పోస్టులు',
    description: 'జూనియర్ అసిస్టెంట్ పోస్టులకు దరఖాస్తుల ప్రక్రియ ప్రారంభం.',
    body: 'గ్రూప్-4 నోటిఫికేషన్ విడుదలైంది. మొత్తం 9,100 పోస్టుల భర్తీకి ప్రకటన వెలువడింది. అర్హత, వయోపరిమితి, పరీక్షా విధానం వివరాలు ప్రకటనలో పేర్కొన్నారు.',
    categoryId: 'cat_jobs',
    reporterId: 'u_rep1',
    status: NEWS_STATUS.SUBMITTED,
    tags: ['jobs', 'group-4'],
    createdAt: now - 1 * HOUR
  }),
  article({
    id: 'n5',
    headline: 'State budget session to begin next week',
    description: 'The finance minister will table the annual budget on day two.',
    body: 'The budget session begins next week with the governor addressing both houses. The finance minister is expected to table the annual budget on the second day.',
    categoryId: 'cat_pol',
    reporterId: 'u_rep4',
    status: NEWS_STATUS.SUBMITTED,
    tags: ['budget'],
    createdAt: now - 2 * HOUR
  }),
  article({
    id: 'n6',
    headline: 'కృష్ణా నదికి పెరిగిన వరద ప్రవాహం',
    description: 'ఎగువ ప్రాంతాల్లో వర్షాల కారణంగా ప్రవాహం పెరిగింది.',
    body: 'ఎగువ ప్రాంతాల్లో కురిసిన భారీ వర్షాల కారణంగా కృష్ణా నదికి వరద ప్రవాహం పెరిగింది. అధికారులు లోతట్టు గ్రామాలను అప్రమత్తం చేశారు.',
    categoryId: 'cat_ap',
    reporterId: 'u_rep5',
    status: NEWS_STATUS.UNDER_REVIEW,
    tags: ['flood', 'krishna'],
    createdAt: now - 4 * HOUR
  }),
  article({
    id: 'n7',
    headline: 'New cricket stadium approved for the district',
    description: 'The sports authority cleared the proposal on Tuesday.',
    body: 'A new cricket stadium with a 25,000 seat capacity has been approved. Construction is expected to start after the monsoon.',
    categoryId: 'cat_sports',
    reporterId: 'u_rep2',
    status: NEWS_STATUS.APPROVED,
    tags: ['cricket', 'stadium'],
    createdAt: now - 8 * HOUR
  }),
  article({
    id: 'n8',
    headline: 'Local market prices see sharp correction',
    description: 'Vegetable prices dropped after fresh supply arrived.',
    body: 'Vegetable prices in the wholesale market corrected sharply after fresh supply arrived from neighbouring districts.',
    categoryId: 'cat_biz',
    reporterId: 'u_rep3',
    status: NEWS_STATUS.REJECTED,
    rejectionReason: 'Numbers are not sourced. Add the market committee figures and resubmit.',
    createdAt: now - 30 * HOUR
  }),
  article({
    id: 'n9',
    headline: 'ఆసుపత్రిలో కొత్త ఐసీయూ విభాగం ప్రారంభం',
    description: 'జిల్లా ఆసుపత్రిలో 20 పడకల ఐసీయూ అందుబాటులోకి వచ్చింది.',
    body: 'జిల్లా ఆసుపత్రిలో 20 పడకల ఐసీయూ విభాగాన్ని ప్రారంభించారు. వెంటిలేటర్లు, మానిటర్లు సమకూర్చారు.',
    categoryId: 'cat_health',
    reporterId: 'u_rep4',
    status: NEWS_STATUS.SENT_BACK,
    editorNote: 'Good story. Add a quote from the hospital superintendent and the opening date.',
    createdAt: now - 20 * HOUR
  }),
  article({
    id: 'n10',
    headline: 'Film industry announces new production hub',
    description: 'Work begins next month on the studio complex.',
    body: 'A new production hub with four studio floors and post-production facilities was announced. Work begins next month.',
    categoryId: 'cat_ent',
    reporterId: 'u_rep5',
    status: NEWS_STATUS.DRAFT,
    createdAt: now - 12 * HOUR
  }),
  article({
    id: 'n11',
    headline: 'Two arrested in vehicle theft case',
    description: 'Police recovered four two-wheelers from the accused.',
    body: 'Police arrested two persons in connection with a vehicle theft case and recovered four two-wheelers.',
    categoryId: 'cat_crime',
    reporterId: 'u_rep1',
    status: NEWS_STATUS.PUBLISHED,
    views: 5120,
    reports: 3,
    createdAt: now - 2 * DAY,
    publishedAt: now - 2 * DAY + HOUR
  }),
  article({
    id: 'n12',
    headline: 'Metro rail extension gets central clearance',
    description: 'The 26 km extension will connect the airport corridor.',
    body: 'The metro rail extension received central clearance. The 26 km stretch will connect the airport corridor with the existing network.',
    categoryId: 'cat_ts',
    reporterId: 'u_rep3',
    status: NEWS_STATUS.PUBLISHED,
    isFeatured: true,
    views: 15720,
    createdAt: now - 3 * DAY,
    publishedAt: now - 3 * DAY + 2 * HOUR
  }),
  article({
    id: 'n13',
    headline: 'ఇంటర్ ఫలితాలు ఈ నెలాఖరున విడుదల',
    description: 'ఫలితాల వెబ్‌సైట్ వివరాలు ప్రకటించారు.',
    body: 'ఇంటర్మీడియట్ ఫలితాలు ఈ నెలాఖరున విడుదల కానున్నాయి. ఫలితాలను అధికారిక వెబ్‌సైట్‌లో చూడవచ్చు.',
    categoryId: 'cat_edu',
    reporterId: 'u_rep2',
    status: NEWS_STATUS.SUBMITTED,
    createdAt: now - 40 * 60 * 1000
  }),
  article({
    id: 'n14',
    headline: 'Farmers get new procurement centres',
    description: 'Twelve additional centres opened across the district.',
    body: 'Twelve additional procurement centres were opened across the district ahead of the harvest season.',
    categoryId: 'cat_ap',
    reporterId: 'u_rep4',
    status: NEWS_STATUS.PUBLISHED,
    views: 4310,
    createdAt: now - 4 * DAY,
    publishedAt: now - 4 * DAY + HOUR
  })
]

/**
 * One account record. Everything the console captures when a login is created
 * lands here: credentials, the account/profile block, and the role-specific
 * assignments (which beats a reporter covers, which editors own them, which
 * subjects a creator writes).
 */
const account = ({
  id, name, username, email, phone, roles,
  location = 'Hyderabad', joinedOn, employeeId,
  isActive = true, mustChangePassword = false, passwordSetAt = 'On creation',
  beatIds = [], editorIds = [], sectionIds = [], subjectIds = [], trackIds = [],
  language = 'Telugu', notes = ''
}) => ({
  id, name, username, email, phone, roles,
  location, joinedOn, employeeId,
  isActive, mustChangePassword, passwordSetAt,
  beatIds, editorIds, sectionIds, subjectIds, trackIds,
  language, notes
})

export const newsUsers = [
  account({
    id: 'u_rep1', name: 'Kiran Kumar', username: 'kiran.kumar', email: 'kiran@jvoice.demo',
    phone: '+91 98480 11234', roles: ['Reporter'], location: 'Hyderabad', joinedOn: '12 Feb 2025',
    employeeId: 'JV-R-001', beatIds: ['cat_crime', 'cat_ts'], editorIds: ['u_ed1'],
    notes: 'Covers city crime desk, night shift.'
  }),
  account({
    id: 'u_rep2', name: 'Anil Varma', username: 'anil.varma', email: 'anil@jvoice.demo',
    phone: '+91 99590 22345', roles: ['Reporter'], location: 'Warangal', joinedOn: '02 Mar 2025',
    employeeId: 'JV-R-002', beatIds: ['cat_pol', 'cat_ts'], editorIds: ['u_ed1', 'u_ed2']
  }),
  // wears two hats — files his own copy and clears the desk when short-staffed
  account({
    id: 'u_rep6', name: 'Harika Devi', username: 'harika.devi', email: 'harika@jvoice.demo',
    phone: '+91 96760 66789', roles: ['Reporter', 'Editor'], location: 'Hyderabad',
    joinedOn: '14 Jan 2025', employeeId: 'JV-R-006',
    beatIds: ['cat_biz', 'cat_tech'], editorIds: ['u_ed1'], sectionIds: ['cat_biz', 'cat_tech'],
    notes: 'Business desk — reports and sub-edits.'
  }),
  account({
    id: 'u_rep3', name: 'Deepika Rao', username: 'deepika.rao', email: 'deepika@jvoice.demo',
    phone: '+91 90000 33456', roles: ['Reporter'], location: 'Vijayawada', joinedOn: '19 Mar 2025',
    employeeId: 'JV-R-003', beatIds: ['cat_ap', 'cat_edu'], editorIds: ['u_ed2']
  }),
  account({
    id: 'u_rep4', name: 'Suresh Babu', username: 'suresh.babu', email: 'suresh@jvoice.demo',
    phone: '+91 91210 44567', roles: ['Reporter'], location: 'Karimnagar', joinedOn: '21 Apr 2025',
    employeeId: 'JV-R-004', isActive: false, beatIds: ['cat_sports'], editorIds: [],
    notes: 'Suspended pending review.'
  }),
  account({
    id: 'u_rep5', name: 'Lakshmi Priya', username: 'lakshmi.priya', email: 'lakshmi@jvoice.demo',
    phone: '+91 93910 55678', roles: ['Reporter'], location: 'Guntur', joinedOn: '02 May 2025',
    employeeId: 'JV-R-005', beatIds: ['cat_health', 'cat_ap'], editorIds: ['u_ed2'],
    mustChangePassword: true, passwordSetAt: 'Not yet signed in'
  }),

  account({
    id: 'u_ed1', name: 'Sridevi Naidu', username: 'sridevi.naidu', email: 'sridevi@jvoice.demo',
    phone: '+91 98661 77001', roles: ['Editor'], location: 'Hyderabad', joinedOn: '04 Jan 2025',
    employeeId: 'JV-E-001', sectionIds: ['cat_ts', 'cat_crime', 'cat_pol'],
    notes: 'Desk head — Telangana and crime.'
  }),
  account({
    id: 'u_ed2', name: 'Naveen Kumar', username: 'naveen.kumar', email: 'naveen@jvoice.demo',
    phone: '+91 98661 77002', roles: ['Editor'], location: 'Hyderabad', joinedOn: '27 Mar 2025',
    employeeId: 'JV-E-002', sectionIds: ['cat_ap', 'cat_edu', 'cat_health', 'cat_sports']
  }),

  account({
    id: 'u_sed1', name: 'Padma Latha', username: 'padma.latha', email: 'padma@jvoice.demo',
    phone: '+91 97010 55001', roles: ['Content Creator'], location: 'Hyderabad',
    joinedOn: '15 Jan 2025', employeeId: 'JV-C-004',
    subjectIds: ['sub_math', 'sub_science', 'sub_reasoning'],
    trackIds: ['track_constable', 'track_si'],
    notes: 'Writes maths and science material.'
  }),
  // writes her own material and reviews other people's
  account({
    id: 'u_sed2', name: 'Rajesh Varma', username: 'rajesh.varma', email: 'rajesh@jvoice.demo',
    phone: '+91 97010 55002', roles: ['Content Creator'], location: 'Vijayawada',
    joinedOn: '20 Feb 2025', employeeId: 'JV-C-005',
    subjectIds: ['sub_history', 'sub_polity', 'sub_ca'], trackIds: ['track_group1', 'track_group2'],
    notes: 'Humanities writer.'
  }),

  account({
    id: 'u_adm1', name: 'Anitha Reddy', username: 'anitha.reddy', email: 'anitha@jvoice.demo',
    phone: '+91 90300 88001', roles: ['Admin'], location: 'Hyderabad', joinedOn: '08 Jan 2025',
    employeeId: 'JV-A-001'
  }),
  account({
    id: 'u_adm2', name: 'Ravi Teja Sharma', username: 'admin', email: 'admin@jvoice.demo',
    phone: '+91 90300 88002', roles: ['Admin'], location: 'Hyderabad', joinedOn: '02 Jan 2024',
    employeeId: 'JV-A-002'
  }),

  account({
    id: 'u_cc1', name: 'Sunitha Rao', username: 'sunitha.rao', email: 'sunitha@jvoice.demo',
    phone: '+91 94900 66001', roles: ['Content Creator'], location: 'Hyderabad', joinedOn: '10 Jan 2025',
    employeeId: 'JV-C-001', subjectIds: ['sub_math', 'sub_science'],
    trackIds: ['track_constable', 'track_si']
  }),
  account({
    id: 'u_cc2', name: 'Divya Sree', username: 'divya.sree', email: 'divya@jvoice.demo',
    phone: '+91 94900 66002', roles: ['Content Creator'], location: 'Vijayawada', joinedOn: '18 Feb 2025',
    employeeId: 'JV-C-002', subjectIds: ['sub_math', 'sub_science', 'sub_history'],
    trackIds: ['track_group2']
  }),
  account({
    id: 'u_cc3', name: 'Mahesh Babu', username: 'mahesh.babu', email: 'mahesh@jvoice.demo',
    phone: '+91 94900 66003', roles: ['Content Creator'], location: 'Warangal', joinedOn: '06 Apr 2025',
    employeeId: 'JV-C-003', isActive: false, subjectIds: ['sub_polity'], trackIds: []
  }),

  account({
    id: 'u_st1', name: 'Sai Charan', username: 'sai.charan', email: 'sai.charan@demo.in',
    phone: '+91 78930 12001', roles: ['Student'], location: 'Hyderabad', joinedOn: '12 Feb 2025',
    employeeId: '—', trackIds: ['track_constable']
  }),
  account({
    id: 'u_st2', name: 'Pooja Rani', username: 'pooja.rani', email: 'pooja.rani@demo.in',
    phone: '+91 78930 12002', roles: ['Student'], location: 'Nizamabad', joinedOn: '03 Mar 2025',
    employeeId: '—', trackIds: ['track_group2']
  }),
  account({
    id: 'u_st3', name: 'Zoya Khan', username: 'zoya.khan', email: 'zoya.khan@demo.in',
    phone: '+91 78930 12003', roles: ['Student'], location: 'Kurnool', joinedOn: '21 Apr 2025',
    employeeId: '—', isActive: false, trackIds: ['track_si']
  })
]

export const USER_ROLES = ['Admin', 'Editor', 'Reporter', 'Content Creator', 'Student']

export const systemSettings = {
  appName: 'J Voice',
  breakingNewsAlerts: true,
  autoPublishApproved: false,
  commentsEnabled: true,
  clipsEnabled: true,
  aiShortsEnabled: true,
  maintenanceMode: false,
  defaultLanguage: 'Telugu'
}

/* ------------------------------------------------------------------ access */

/** The four areas a role can be granted access to. */
export const MODULES = ['News', 'Study', 'Exams', 'System']

/** Create / Read / Update / Delete — one tick each, per module. */
export const PERMS = ['create', 'read', 'update', 'delete']

const all = { create: true, read: true, update: true, delete: true }
const readOnly = { create: false, read: true, update: false, delete: false }
const none = { create: false, read: false, update: false, delete: false }

/**
 * Seed role matrix. Admin holds full CRUD on every module and is locked
 * (isSystem) so it cannot be deleted or downgraded out of the console.
 */
export const roles = [
  {
    id: 'role_admin',
    name: 'Admin',
    scope: 'News + Study',
    description: 'Full control of both modules — content, people, exams and settings.',
    isSystem: true,
    perms: { News: all, Study: all, Exams: all, System: all }
  },
  {
    id: 'role_editor',
    name: 'Editor',
    scope: 'News only',
    description: 'Reviews the news queue — approves, publishes, rejects, sends back.',
    isSystem: true,
    perms: {
      News: { create: true, read: true, update: true, delete: false },
      Study: none,
      Exams: none,
      System: none
    }
  },
  {
    id: 'role_reporter',
    name: 'Reporter',
    scope: 'News only',
    description: 'Writes and submits stories from the field, fixes and resubmits.',
    isSystem: true,
    perms: {
      News: { create: true, read: true, update: true, delete: false },
      Study: none,
      Exams: none,
      System: none
    }
  },
  {
    id: 'role_creator',
    name: 'Content Creator',
    scope: 'Study only',
    description: 'Owns study material and the question bank — writes, edits and publishes it.',
    isSystem: true,
    perms: {
      News: none,
      Study: { create: true, read: true, update: true, delete: true },
      Exams: { create: true, read: true, update: true, delete: true },
      System: none
    }
  },
  {
    id: 'role_student',
    name: 'Student',
    scope: 'Study only',
    description: 'Picks an exam type, studies it, takes tests and sees the rank.',
    isSystem: true,
    perms: { News: readOnly, Study: readOnly, Exams: readOnly, System: none }
  }
]
