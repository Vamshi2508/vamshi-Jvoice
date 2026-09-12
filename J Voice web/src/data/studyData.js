/**
 * Module 2 (Study & Exams) seed data — mirrors the Android app, including the
 * admin-managed exam types that drive the student's exam picker.
 */

const HOUR = 60 * 60 * 1000
const DAY = 24 * HOUR
const now = Date.now()

export const CONTENT_STATUS = {
  DRAFT: 'Draft',
  PENDING_REVIEW: 'Pending Review',
  PUBLISHED: 'Published'
}

export const DIFFICULTY = ['Easy', 'Medium', 'Hard']

export const EXAM_GROUPS = [
  'Police & Uniform',
  'TSPSC / APPSC Groups',
  'SSC & Railways',
  'Banking & Insurance',
  'Teaching'
]

export const subjects = [
  { id: 'sub_math', nameEn: 'Mathematics', nameTe: 'గణితం', emoji: '🔢', isEnabled: true },
  { id: 'sub_science', nameEn: 'General Science', nameTe: 'సాధారణ విజ్ఞానం', emoji: '🔬', isEnabled: true },
  { id: 'sub_history', nameEn: 'History', nameTe: 'చరిత్ర', emoji: '🏺', isEnabled: true },
  { id: 'sub_geography', nameEn: 'Geography', nameTe: 'భూగోళశాస్త్రం', emoji: '🗺️', isEnabled: true },
  { id: 'sub_polity', nameEn: 'Indian Polity', nameTe: 'భారత రాజ్యాంగం', emoji: '⚖️', isEnabled: true },
  { id: 'sub_economy', nameEn: 'Economy', nameTe: 'ఆర్థిక వ్యవస్థ', emoji: '📈', isEnabled: true },
  { id: 'sub_ca', nameEn: 'Current Affairs', nameTe: 'వర్తమాన అంశాలు', emoji: '📰', isEnabled: true },
  { id: 'sub_gk', nameEn: 'General Knowledge', nameTe: 'సామాన్య జ్ఞానం', emoji: '🌍', isEnabled: true },
  { id: 'sub_english', nameEn: 'English', nameTe: 'ఇంగ్లీష్', emoji: '🔤', isEnabled: true },
  { id: 'sub_reasoning', nameEn: 'Reasoning', nameTe: 'రీజనింగ్', emoji: '🧩', isEnabled: true }
]

const t = (id, subjectId, nameEn, nameTe, order, difficulty) => ({
  id, subjectId, nameEn, nameTe, order, difficulty, isEnabled: true
})

export const topics = [
  t('t_num_system', 'sub_math', 'Number System', 'సంఖ్యా వ్యవస్థ', 1, 'Easy'),
  t('t_percentage', 'sub_math', 'Percentages', 'శాతాలు', 2, 'Medium'),
  t('t_profit_loss', 'sub_math', 'Profit & Loss', 'లాభం - నష్టం', 3, 'Medium'),
  t('t_time_work', 'sub_math', 'Time & Work', 'కాలం - పని', 4, 'Hard'),
  t('t_average', 'sub_math', 'Average', 'సగటు', 5, 'Easy'),
  t('t_physics', 'sub_science', 'Physics Basics', 'భౌతిక శాస్త్రం', 1, 'Medium'),
  t('t_chemistry', 'sub_science', 'Chemistry Basics', 'రసాయన శాస్త్రం', 2, 'Medium'),
  t('t_human_body', 'sub_science', 'Human Body', 'మానవ శరీరం', 3, 'Easy'),
  t('t_environment', 'sub_science', 'Environment & Ecology', 'పర్యావరణం', 4, 'Medium'),
  t('t_ancient', 'sub_history', 'Ancient India', 'ప్రాచీన భారతదేశం', 1, 'Medium'),
  t('t_medieval', 'sub_history', 'Medieval India', 'మధ్యయుగ భారతదేశం', 2, 'Medium'),
  t('t_freedom', 'sub_history', 'Freedom Movement', 'స్వాతంత్ర్య ఉద్యమం', 3, 'Medium'),
  t('t_rivers', 'sub_geography', 'Indian Rivers', 'భారత నదులు', 1, 'Easy'),
  t('t_climate', 'sub_geography', 'Climate & Monsoon', 'వాతావరణం', 2, 'Medium'),
  t('t_constitution', 'sub_polity', 'Constitution Basics', 'రాజ్యాంగ ప్రాథమికాలు', 1, 'Medium'),
  t('t_articles', 'sub_polity', 'Articles of the Constitution', 'రాజ్యాంగ అధికరణలు', 2, 'Hard'),
  t('t_frights', 'sub_polity', 'Fundamental Rights', 'ప్రాథమిక హక్కులు', 3, 'Medium'),
  t('t_president', 'sub_polity', 'President', 'రాష్ట్రపతి', 4, 'Medium'),
  t('t_pm_council', 'sub_polity', 'Prime Minister & Council of Ministers', 'ప్రధాని - మంత్రి మండలి', 5, 'Medium'),
  t('t_parliament', 'sub_polity', 'Parliament', 'పార్లమెంటు', 6, 'Hard'),
  t('t_judiciary', 'sub_polity', 'Supreme Court & Judiciary', 'సుప్రీంకోర్టు - న్యాయవ్యవస్థ', 7, 'Hard'),
  t('t_amendments', 'sub_polity', 'Amendments', 'సవరణలు', 8, 'Hard'),
  t('t_panchayat', 'sub_polity', 'Panchayati Raj', 'పంచాయతీ రాజ్', 9, 'Easy'),
  t('t_banking', 'sub_economy', 'Banking in India', 'బ్యాంకింగ్', 1, 'Medium'),
  t('t_budget', 'sub_economy', 'Budget & Taxation', 'బడ్జెట్ - పన్నులు', 2, 'Hard'),
  t('t_national_af', 'sub_ca', 'National Affairs', 'జాతీయ అంశాలు', 1, 'Medium'),
  t('t_sports_awards', 'sub_ca', 'Sports & Awards', 'క్రీడలు - అవార్డులు', 2, 'Easy'),
  t('t_culture', 'sub_gk', 'Indian Culture', 'భారతీయ సంస్కృతి', 1, 'Easy'),
  t('t_days', 'sub_gk', 'Important Days', 'ముఖ్యమైన దినాలు', 2, 'Easy'),
  t('t_grammar', 'sub_english', 'Grammar Basics', 'వ్యాకరణం', 1, 'Medium'),
  t('t_synonyms', 'sub_english', 'Synonyms & Antonyms', 'పర్యాయ పదాలు', 2, 'Easy'),
  t('t_series', 'sub_reasoning', 'Number & Letter Series', 'శ్రేణులు', 1, 'Easy'),
  t('t_coding', 'sub_reasoning', 'Coding & Decoding', 'కోడింగ్ - డీకోడింగ్', 2, 'Medium'),
  t('t_blood', 'sub_reasoning', 'Blood Relations', 'బంధుత్వాలు', 3, 'Medium')
]

const section = (subjectId, questions, marks) => ({ subjectId, questions, marks })

const track = (o) => ({
  isEnabled: true,
  stages: [],
  ...o,
  totalQuestions: o.sections.reduce((s, x) => s + x.questions, 0),
  totalMarks: o.sections.reduce((s, x) => s + x.marks, 0)
})

/** The list a student picks from. Admin-owned: this is the exam picker. */
export const examTracks = [
  track({
    id: 'track_constable',
    nameEn: 'Police Constable',
    nameTe: 'పోలీస్ కానిస్టేబుల్',
    shortName: 'Constable',
    emoji: '👮',
    group: 'Police & Uniform',
    tagline: 'Civil & AR Constable — preliminary and final written test',
    qualification: 'Intermediate / 10+2',
    ageLimit: '18 - 22 years',
    vacancyLabel: '≈ 15,600 posts',
    examDateLabel: 'Notification expected soon',
    durationMinutes: 180,
    negativeMarking: 'No negative marking',
    stages: ['Preliminary Written Test', 'PET / PMT', 'Final Written Test'],
    sections: [
      section('sub_gk', 40, 40), section('sub_ca', 30, 30), section('sub_science', 30, 30),
      section('sub_math', 20, 20), section('sub_reasoning', 20, 20), section('sub_history', 20, 20),
      section('sub_geography', 20, 20), section('sub_polity', 20, 20)
    ]
  }),
  track({
    id: 'track_si',
    nameEn: 'Police Sub Inspector',
    nameTe: 'పోలీస్ ఎస్‌ఐ',
    shortName: 'SI',
    emoji: '🚔',
    group: 'Police & Uniform',
    tagline: 'SI of Police — preliminary, physical and mains',
    qualification: 'Any degree',
    ageLimit: '21 - 25 years',
    vacancyLabel: '≈ 1,200 posts',
    examDateLabel: 'Prelims in 3 months',
    durationMinutes: 180,
    negativeMarking: 'No negative marking',
    stages: ['Preliminary Written Test', 'PET / PMT', 'Mains', 'Interview'],
    sections: [
      section('sub_gk', 30, 30), section('sub_ca', 25, 25), section('sub_science', 25, 25),
      section('sub_history', 20, 20), section('sub_geography', 20, 20), section('sub_polity', 20, 20),
      section('sub_math', 20, 20), section('sub_reasoning', 20, 20), section('sub_english', 20, 20)
    ]
  }),
  track({
    id: 'track_group1',
    nameEn: 'Group-1 Services',
    nameTe: 'గ్రూప్-1',
    shortName: 'Group-1',
    emoji: '🏛️',
    group: 'TSPSC / APPSC Groups',
    tagline: 'Deputy Collector, DSP and other Group-1 posts',
    qualification: 'Any degree',
    ageLimit: '18 - 46 years',
    vacancyLabel: '≈ 560 posts',
    examDateLabel: 'Prelims announced',
    durationMinutes: 150,
    negativeMarking: 'No negative marking',
    stages: ['Prelims (screening)', 'Mains - 6 papers', 'Interview'],
    sections: [
      section('sub_history', 25, 25), section('sub_ca', 25, 25), section('sub_polity', 20, 20),
      section('sub_economy', 20, 20), section('sub_science', 20, 20), section('sub_geography', 15, 15),
      section('sub_reasoning', 15, 15), section('sub_gk', 10, 10)
    ]
  }),
  track({
    id: 'track_group2',
    nameEn: 'Group-2 Services',
    nameTe: 'గ్రూప్-2',
    shortName: 'Group-2',
    emoji: '📋',
    group: 'TSPSC / APPSC Groups',
    tagline: 'Executive and non-executive Group-2 posts',
    qualification: 'Any degree',
    ageLimit: '18 - 44 years',
    vacancyLabel: '≈ 780 posts',
    examDateLabel: 'Mains in 5 months',
    durationMinutes: 150,
    negativeMarking: 'No negative marking',
    stages: ['Paper 1 - General Studies', 'Paper 2 - History & Polity', 'Paper 3 - Economy'],
    sections: [
      section('sub_history', 30, 30), section('sub_polity', 30, 30), section('sub_economy', 30, 30),
      section('sub_geography', 20, 20), section('sub_ca', 20, 20), section('sub_gk', 20, 20)
    ]
  }),
  track({
    id: 'track_group4',
    nameEn: 'Group-4 Junior Assistant',
    nameTe: 'గ్రూప్-4 జూనియర్ అసిస్టెంట్',
    shortName: 'Group-4',
    emoji: '🗂️',
    group: 'TSPSC / APPSC Groups',
    tagline: 'Junior Assistant, Junior Accountant and Typist posts',
    qualification: 'Any degree',
    ageLimit: '18 - 44 years',
    vacancyLabel: '≈ 9,100 posts',
    examDateLabel: 'Notification released',
    durationMinutes: 150,
    negativeMarking: 'No negative marking',
    stages: ['Paper 1 - General Studies', 'Paper 2 - Secretarial Abilities'],
    sections: [
      section('sub_gk', 30, 30), section('sub_ca', 25, 25), section('sub_history', 20, 20),
      section('sub_geography', 20, 20), section('sub_polity', 20, 20), section('sub_economy', 15, 15),
      section('sub_science', 10, 10), section('sub_math', 5, 5), section('sub_reasoning', 5, 5)
    ]
  }),
  track({
    id: 'track_vro',
    nameEn: 'VRO / Panchayat Secretary',
    nameTe: 'వీఆర్‌ఓ / పంచాయతీ కార్యదర్శి',
    shortName: 'VRO',
    emoji: '🏡',
    group: 'TSPSC / APPSC Groups',
    tagline: 'Village Revenue Officer and Panchayat Secretary',
    qualification: 'Intermediate / Any degree',
    ageLimit: '18 - 44 years',
    vacancyLabel: '≈ 2,400 posts',
    examDateLabel: 'Expected next quarter',
    durationMinutes: 150,
    negativeMarking: 'No negative marking',
    stages: ['Written Test', 'Certificate Verification'],
    sections: [
      section('sub_gk', 40, 40), section('sub_ca', 20, 20), section('sub_science', 20, 20),
      section('sub_polity', 20, 20), section('sub_math', 20, 20), section('sub_reasoning', 15, 15),
      section('sub_geography', 15, 15)
    ]
  }),
  track({
    id: 'track_ssc',
    nameEn: 'SSC CGL / CHSL',
    nameTe: 'ఎస్‌ఎస్‌సీ సీజీఎల్',
    shortName: 'SSC',
    emoji: '🇮🇳',
    group: 'SSC & Railways',
    tagline: 'Central government graduate and 10+2 level posts',
    qualification: 'Any degree (CGL) / 10+2 (CHSL)',
    ageLimit: '18 - 32 years',
    vacancyLabel: '≈ 17,700 posts',
    examDateLabel: 'Tier-1 in 2 months',
    durationMinutes: 60,
    negativeMarking: '0.50 marks per wrong answer',
    stages: ['Tier-1 CBT', 'Tier-2 CBT', 'Skill / Typing Test'],
    sections: [
      section('sub_math', 25, 50), section('sub_reasoning', 25, 50),
      section('sub_english', 25, 50), section('sub_gk', 25, 50)
    ]
  }),
  track({
    id: 'track_rrb',
    nameEn: 'RRB NTPC / Group-D',
    nameTe: 'ఆర్‌ఆర్‌బీ ఎన్‌టీపీసీ',
    shortName: 'RRB',
    emoji: '🚆',
    group: 'SSC & Railways',
    tagline: 'Railway non-technical and level-1 posts',
    qualification: '10+2 / Any degree',
    ageLimit: '18 - 33 years',
    vacancyLabel: '≈ 35,000 posts',
    examDateLabel: 'CBT-1 in 4 months',
    durationMinutes: 90,
    negativeMarking: '1/3 mark per wrong answer',
    stages: ['CBT-1', 'CBT-2', 'Typing / PET', 'Document Verification'],
    sections: [
      section('sub_math', 30, 30), section('sub_reasoning', 30, 30),
      section('sub_gk', 25, 25), section('sub_ca', 15, 15)
    ]
  }),
  track({
    id: 'track_bank',
    nameEn: 'IBPS PO / Clerk',
    nameTe: 'బ్యాంక్ పీఓ / క్లర్క్',
    shortName: 'Bank',
    emoji: '🏦',
    group: 'Banking & Insurance',
    tagline: 'Probationary Officer and Clerk prelims',
    qualification: 'Any degree',
    ageLimit: '20 - 30 years',
    vacancyLabel: '≈ 5,200 posts',
    examDateLabel: 'Prelims in 6 weeks',
    durationMinutes: 60,
    negativeMarking: '0.25 marks per wrong answer',
    stages: ['Prelims', 'Mains', 'Interview'],
    sections: [
      section('sub_math', 35, 35), section('sub_reasoning', 35, 35), section('sub_english', 30, 30)
    ]
  }),
  track({
    id: 'track_dsc',
    nameEn: 'DSC / TET',
    nameTe: 'డీఎస్‌సీ / టెట్',
    shortName: 'DSC',
    emoji: '🎓',
    group: 'Teaching',
    tagline: 'Teacher recruitment and eligibility test',
    qualification: 'D.Ed / B.Ed',
    ageLimit: '18 - 46 years',
    vacancyLabel: '≈ 11,000 posts',
    examDateLabel: 'TET in 3 months',
    durationMinutes: 150,
    negativeMarking: 'No negative marking',
    stages: ['TET Paper', 'DSC Written Test', 'Merit List'],
    sections: [
      section('sub_gk', 30, 30), section('sub_english', 30, 30), section('sub_math', 30, 30),
      section('sub_science', 30, 30), section('sub_history', 15, 15), section('sub_geography', 15, 15)
    ]
  })
]

const sa = (id, subjectId, topicId, title, status, authorName, minutes, createdAt) => ({
  id, subjectId, topicId, title, status, authorName,
  readingMinutes: minutes,
  createdAt,
  description: title + ' — concept notes, important points and solved examples.',
  content: 'Demo study content for ' + title + '. In the real build this is the full article body written by a Content Creator.',
  importantPoints: ['Key definition and formula', 'Common exam trap', 'Previous-year pattern'],
  examples: ['Worked example 1', 'Worked example 2']
})

export const studyArticles = [
  sa('a1', 'sub_math', 't_percentage', 'Percentages with shortcuts', CONTENT_STATUS.PUBLISHED, 'Sunitha Rao', 6, now - 5 * DAY),
  sa('a2', 'sub_math', 't_profit_loss', 'Profit & Loss basics', CONTENT_STATUS.PUBLISHED, 'Sunitha Rao', 7, now - 5 * DAY),
  sa('a3', 'sub_math', 't_time_work', 'Time & Work — pipes and cisterns', CONTENT_STATUS.PENDING_REVIEW, 'Divya Sree', 8, now - 6 * HOUR),
  sa('a4', 'sub_science', 't_physics', 'Physics — laws of motion', CONTENT_STATUS.PUBLISHED, 'Divya Sree', 9, now - 8 * DAY),
  sa('a5', 'sub_science', 't_human_body', 'Human body — systems overview', CONTENT_STATUS.PUBLISHED, 'Sunitha Rao', 6, now - 9 * DAY),
  sa('a20', 'sub_polity', 't_articles', 'Articles 12–35 — the rights cluster', CONTENT_STATUS.PUBLISHED, 'Sunitha Rao', 12, now - 4 * DAY),
  sa('a21', 'sub_polity', 't_articles', 'Articles 36–51A — DPSP and duties', CONTENT_STATUS.PUBLISHED, 'Divya Sree', 9, now - 3 * DAY),
  sa('a22', 'sub_polity', 't_president', 'President — election, powers, impeachment', CONTENT_STATUS.PUBLISHED, 'Divya Sree', 11, now - 2 * DAY),
  sa('a23', 'sub_polity', 't_president', 'Ordinance power under Article 123', CONTENT_STATUS.PENDING_REVIEW, 'Sunitha Rao', 6, now - 5 * HOUR),
  sa('a24', 'sub_polity', 't_pm_council', 'PM and the Council of Ministers', CONTENT_STATUS.PUBLISHED, 'Divya Sree', 8, now - 6 * DAY),
  sa('a25', 'sub_polity', 't_judiciary', 'Supreme Court — jurisdiction and writs', CONTENT_STATUS.DRAFT, 'Sunitha Rao', 10, now - 3 * HOUR),
  sa('a26', 'sub_polity', 't_amendments', 'Key amendments — 42nd, 44th, 73rd, 74th', CONTENT_STATUS.PUBLISHED, 'Divya Sree', 13, now - 8 * DAY),
  sa('a6', 'sub_history', 't_freedom', 'Freedom movement timeline', CONTENT_STATUS.PUBLISHED, 'Kiran Kumar', 11, now - 3 * DAY),
  sa('a7', 'sub_history', 't_ancient', 'Ancient India — Indus valley', CONTENT_STATUS.DRAFT, 'Kiran Kumar', 10, now - 12 * HOUR),
  sa('a8', 'sub_polity', 't_constitution', 'Constitution — preamble & features', CONTENT_STATUS.PUBLISHED, 'Sunitha Rao', 8, now - 11 * DAY),
  sa('a9', 'sub_polity', 't_frights', 'Fundamental Rights explained', CONTENT_STATUS.PENDING_REVIEW, 'Divya Sree', 7, now - 20 * HOUR),
  sa('a10', 'sub_geography', 't_rivers', 'Indian rivers and tributaries', CONTENT_STATUS.PUBLISHED, 'Kiran Kumar', 6, now - 14 * DAY),
  sa('a11', 'sub_ca', 't_national_af', 'Government schemes revision', CONTENT_STATUS.PENDING_REVIEW, 'Sunitha Rao', 5, now - 3 * HOUR),
  sa('a12', 'sub_reasoning', 't_series', 'Number & letter series patterns', CONTENT_STATUS.PUBLISHED, 'Divya Sree', 5, now - 6 * DAY)
]

/** Where a question came from — practice written for the topic, or a real paper. */
export const QUESTION_SOURCE = {
  SAMPLE: 'Sample',
  PREVIOUS: 'Previous Paper'
}

const q = (id, subjectId, topicId, text, options, correctIndex, difficulty, explanation) => ({
  id, subjectId, topicId, text, options, correctIndex, difficulty, explanation,
  type: 'Multiple choice',
  source: QUESTION_SOURCE.SAMPLE,
  paperName: '',
  year: '',
  status: CONTENT_STATUS.PUBLISHED
})

/** The same question, but asked in a real exam — carries the paper and year. */
const pyq = (id, subjectId, topicId, text, options, correctIndex, difficulty, explanation, paperName, year) => ({
  ...q(id, subjectId, topicId, text, options, correctIndex, difficulty, explanation),
  source: QUESTION_SOURCE.PREVIOUS,
  paperName,
  year
})

export const questions = [
  pyq('qy1', 'sub_polity', 't_articles', 'The Right to Equality is guaranteed by which Articles?',
    ['Articles 12-13', 'Articles 14-18', 'Articles 19-22', 'Articles 23-24'], 1, 'Medium',
    'Articles 14 to 18 together form the Right to Equality.',
    'TSPSC Group-2 General Studies', '2023'),
  pyq('qy2', 'sub_polity', 't_articles', 'Which Article deals with the Right to Life and Personal Liberty?',
    ['Article 19', 'Article 20', 'Article 21', 'Article 22'], 2, 'Easy',
    'Article 21 — expanded by the courts to cover dignity, privacy and livelihood.',
    'TS Police Constable Preliminary', '2022'),
  pyq('qy3', 'sub_polity', 't_president', 'The President holds office for a term of',
    ['4 years', '5 years', '6 years', 'Till 65 years of age'], 1, 'Easy',
    'Five years from the date of entering office, and is eligible for re-election.',
    'SSC CGL Tier-1', '2023'),
  pyq('qy4', 'sub_polity', 't_president', 'Who acts as President when both the President and Vice President posts are vacant?',
    ['Prime Minister', 'Speaker of Lok Sabha', 'Chief Justice of India', 'Home Minister'], 2, 'Hard',
    'The Chief Justice of India acts as President — as happened in 1969.',
    'TSPSC Group-1 Prelims', '2022'),
  pyq('qy5', 'sub_polity', 't_amendments', 'The 73rd Constitutional Amendment relates to',
    ['Municipalities', 'Panchayati Raj', 'Anti-defection', 'Education'], 1, 'Medium',
    'The 73rd Amendment, 1992 gave Panchayati Raj constitutional status.',
    'TS Police SI Preliminary', '2023'),
  pyq('qy6', 'sub_polity', 't_parliament', 'The maximum strength of the Lok Sabha is',
    ['500', '543', '552', '560'], 2, 'Medium',
    'The Constitution fixes the maximum at 552 members.',
    'RRB NTPC CBT-1', '2022'),
  q('qp1', 'sub_polity', 't_articles', 'Which Article abolishes untouchability?',
    ['Article 14', 'Article 15', 'Article 17', 'Article 21'], 2, 'Easy',
    'Article 17 abolishes untouchability and forbids its practice in any form.'),
  q('qp2', 'sub_polity', 't_articles', 'The Right to Constitutional Remedies is guaranteed by',
    ['Article 30', 'Article 32', 'Article 36', 'Article 44'], 1, 'Medium',
    'Article 32 — Dr Ambedkar called it the heart and soul of the Constitution.'),
  q('qp3', 'sub_polity', 't_articles', 'Article 21A deals with',
    ['Right to Work', 'Right to Education', 'Right to Property', 'Right to Information'], 1, 'Medium',
    'Inserted by the 86th Amendment — free and compulsory education for ages 6 to 14.'),
  q('qp4', 'sub_polity', 't_president', 'Who administers the oath of office to the President?',
    ['Prime Minister', 'Vice President', 'Chief Justice of India', 'Speaker'], 2, 'Easy',
    'The Chief Justice of India, or in their absence the senior-most Supreme Court judge.'),
  q('qp5', 'sub_polity', 't_president', 'The President can be removed by',
    ['A no-confidence motion', 'Impeachment under Article 61', 'A Supreme Court order', 'A referendum'],
    1, 'Medium', 'Impeachment for violation of the Constitution, under Article 61.'),
  q('qp6', 'sub_polity', 't_president', 'The minimum age to become President is',
    ['25 years', '30 years', '35 years', '40 years'], 2, 'Easy', 'Article 58 sets it at 35 years.'),
  q('qp7', 'sub_polity', 't_pm_council', 'The Prime Minister is appointed by',
    ['The Parliament', 'The President', 'The Lok Sabha Speaker', 'The Chief Justice'], 1, 'Easy',
    'Article 75 — the President appoints the PM, conventionally the majority leader.'),
  q('qp8', 'sub_polity', 't_judiciary', 'The writ of Habeas Corpus literally means',
    ['We command', 'To have the body', 'By what authority', 'To be certified'], 1, 'Medium',
    'Latin for "to have the body" — used against unlawful detention.'),
  q('qp9', 'sub_polity', 't_amendments', 'Which amendment added the words Socialist and Secular?',
    ['24th', '42nd', '44th', '52nd'], 1, 'Medium',
    'The 42nd Amendment, 1976 — often called the Mini Constitution.'),
  q('qp10', 'sub_polity', 't_amendments', 'Panchayati Raj got constitutional status through the',
    ['71st Amendment', '72nd Amendment', '73rd Amendment', '74th Amendment'], 2, 'Medium',
    'The 73rd Amendment, 1992, added Part IX and the Eleventh Schedule.'),
  q('q1', 'sub_math', 't_percentage', 'What is 15% of 480?', ['62', '72', '68', '75'], 1, 'Easy', '480 × 15/100 = 72.'),
  q('q2', 'sub_math', 't_percentage', 'A number increased by 20% becomes 96. The number is', ['76', '80', '84', '90'], 1, 'Medium', 'x × 1.2 = 96 → x = 80.'),
  q('q3', 'sub_math', 't_profit_loss', 'Cost price ₹500, selling price ₹575. Profit percent is', ['12%', '15%', '18%', '20%'], 1, 'Easy', 'Profit 75 on 500 = 15%.'),
  q('q4', 'sub_math', 't_time_work', 'A does a job in 12 days, B in 24 days. Together they take', ['6 days', '8 days', '9 days', '10 days'], 1, 'Medium', '1/12 + 1/24 = 1/8 → 8 days.'),
  q('q5', 'sub_math', 't_average', 'Average of 12, 18, 24, 30 is', ['20', '21', '22', '24'], 1, 'Easy', 'Sum 84 / 4 = 21.'),
  q('q6', 'sub_science', 't_physics', 'SI unit of force is', ['Joule', 'Newton', 'Watt', 'Pascal'], 1, 'Easy', 'Force is measured in newtons.'),
  q('q7', 'sub_science', 't_chemistry', 'Chemical formula of common salt is', ['NaCl', 'KCl', 'CaCO3', 'NaOH'], 0, 'Easy', 'Sodium chloride, NaCl.'),
  q('q8', 'sub_science', 't_human_body', 'The largest organ of the human body is', ['Liver', 'Skin', 'Lungs', 'Brain'], 1, 'Easy', 'Skin is the largest organ.'),
  q('q9', 'sub_history', 't_freedom', 'The Quit India Movement began in', ['1930', '1935', '1942', '1945'], 2, 'Medium', 'August 1942.'),
  q('q10', 'sub_history', 't_ancient', 'Mohenjo-daro is located on the bank of', ['Ganga', 'Indus', 'Ravi', 'Yamuna'], 1, 'Medium', 'On the Indus river.'),
  q('q11', 'sub_polity', 't_constitution', 'How many schedules does the Constitution have today?', ['10', '12', '9', '14'], 1, 'Medium', 'Twelve schedules.'),
  q('q12', 'sub_polity', 't_frights', 'Right to Equality is covered under Articles', ['12-13', '14-18', '19-22', '23-24'], 1, 'Medium', 'Articles 14 to 18.'),
  q('q13', 'sub_polity', 't_parliament', 'Maximum strength of the Lok Sabha is', ['543', '550', '552', '560'], 2, 'Hard', '552 including nominated members.'),
  q('q14', 'sub_geography', 't_rivers', 'The longest river in India is', ['Godavari', 'Ganga', 'Krishna', 'Narmada'], 1, 'Easy', 'The Ganga.'),
  q('q15', 'sub_geography', 't_climate', 'South-west monsoon reaches Kerala around', ['1 May', '1 June', '1 July', '15 July'], 1, 'Medium', 'Around 1 June.'),
  q('q16', 'sub_economy', 't_banking', 'RBI was established in', ['1925', '1935', '1947', '1955'], 1, 'Medium', 'RBI began operations in 1935.'),
  q('q17', 'sub_economy', 't_budget', 'GST was introduced in India in', ['2014', '2016', '2017', '2019'], 2, 'Medium', 'From 1 July 2017.'),
  q('q18', 'sub_ca', 't_sports_awards', 'The highest sporting honour in India is', ['Arjuna Award', 'Khel Ratna', 'Dronacharya', 'Padma Shri'], 1, 'Easy', 'Major Dhyan Chand Khel Ratna.'),
  q('q19', 'sub_gk', 't_days', 'World Environment Day is observed on', ['5 June', '22 April', '8 March', '1 May'], 0, 'Easy', '5 June every year.'),
  q('q20', 'sub_gk', 't_culture', 'Kuchipudi is a classical dance of', ['Tamil Nadu', 'Andhra Pradesh', 'Kerala', 'Odisha'], 1, 'Easy', 'Andhra Pradesh.'),
  q('q21', 'sub_english', 't_synonyms', 'Synonym of "abundant" is', ['Scarce', 'Plentiful', 'Weak', 'Rare'], 1, 'Easy', 'Abundant = plentiful.'),
  q('q22', 'sub_english', 't_grammar', 'She ____ to school every day.', ['go', 'goes', 'going', 'gone'], 1, 'Easy', 'Third person singular takes "goes".'),
  q('q23', 'sub_reasoning', 't_series', 'Find the next term: 2, 6, 12, 20, ?', ['28', '30', '32', '36'], 1, 'Medium', 'Differences 4, 6, 8, 10 → 30.'),
  q('q24', 'sub_reasoning', 't_coding', 'If CAT = 3120, then DOG =', ['4157', '4158', '4157', '41574'], 1, 'Medium', 'Positional coding of each letter.'),
  q('q25', 'sub_reasoning', 't_blood', "A's mother's brother is B. B is A's", ['Uncle', 'Father', 'Cousin', 'Nephew'], 0, 'Easy', 'Mother\'s brother is the maternal uncle.')
]

/** One line of a paper's blueprint: how many questions come from a subject. */
export const blueprintRow = (subjectId, questions, marks, topicIds = []) => ({
  subjectId, questions, marks, topicIds
})

/**
 * A paper. `questionCount` and `totalMarks` are always derived from the
 * blueprint, so the header can never disagree with the section breakdown.
 */
/** Even split of a target count across subjects — the default blueprint. */
const evenBlueprint = (subjectIds = [], total = 0) => {
  if (!subjectIds.length || !total) return []
  const each = Math.floor(total / subjectIds.length)
  let left = total - each * subjectIds.length
  return subjectIds.map((subjectId) => {
    const n = each + (left-- > 0 ? 1 : 0)
    return blueprintRow(subjectId, n, n)
  })
}

const exam = (o) => {
  // papers that predate the blueprint get an even split so totals still add up
  const blueprint = o.blueprint || evenBlueprint(o.subjectIds, o.questionCount)
  return {
    difficulty: 'Medium',
    isActive: true,
    trackIds: [],
    ...o,
    blueprint,
    questionCount: blueprint.length
      ? blueprint.reduce((n, r) => n + Number(r.questions || 0), 0)
      : o.questionCount || 0,
    totalMarks: blueprint.reduce((n, r) => n + Number(r.marks || 0), 0)
  }
}

export const exams = [
  exam({
    id: 'exam_daily_constable',
    title: 'Constable Daily Exam - Today',
    type: 'Daily Exam',
    dateLabel: 'Today',
    durationMinutes: 20,
    subjectIds: ['sub_gk', 'sub_ca', 'sub_science', 'sub_math', 'sub_reasoning'],
    trackIds: ['track_constable'],
    blueprint: [
      blueprintRow('sub_gk', 5, 5), blueprintRow('sub_ca', 5, 5),
      blueprintRow('sub_science', 4, 4), blueprintRow('sub_math', 3, 3),
      blueprintRow('sub_reasoning', 3, 3)
    ],
    instructions: '20 questions in 20 minutes, mixed like the Constable paper.'
  }),
  exam({
    id: 'exam_gt_constable',
    title: 'Constable Grand Test',
    type: 'Grand Test',
    dateLabel: 'This Sunday',
    durationMinutes: 60,
    subjectIds: ['sub_gk', 'sub_ca', 'sub_science', 'sub_math', 'sub_reasoning', 'sub_history', 'sub_geography', 'sub_polity'],
    trackIds: ['track_constable'],
    difficulty: 'Hard',
    blueprint: [
      blueprintRow('sub_gk', 12, 12), blueprintRow('sub_ca', 9, 9),
      blueprintRow('sub_science', 9, 9), blueprintRow('sub_math', 6, 6),
      blueprintRow('sub_reasoning', 6, 6), blueprintRow('sub_history', 6, 6),
      blueprintRow('sub_geography', 6, 6), blueprintRow('sub_polity', 6, 6)
    ],
    instructions: 'Full-pattern mock for Police Constable.'
  }),
  exam({
    id: 'exam_daily_group4',
    title: 'Group-4 Daily Exam - Today',
    type: 'Daily Exam',
    dateLabel: 'Today',
    durationMinutes: 20,
    questionCount: 20,
    subjectIds: ['sub_gk', 'sub_ca', 'sub_history', 'sub_polity'],
    trackIds: ['track_group4'],
    instructions: '20 questions in 20 minutes on the Group-4 mix.'
  }),
  exam({
    id: 'exam_gt_group4',
    title: 'Group-4 Grand Test',
    type: 'Grand Test',
    dateLabel: 'This Sunday',
    durationMinutes: 60,
    questionCount: 60,
    subjectIds: ['sub_gk', 'sub_ca', 'sub_history', 'sub_geography', 'sub_polity', 'sub_economy'],
    trackIds: ['track_group4'],
    difficulty: 'Hard',
    instructions: 'Full-pattern mock for Group-4 Junior Assistant.'
  }),
  exam({
    id: 'exam_daily_bank',
    title: 'Bank Daily Exam - Today',
    type: 'Daily Exam',
    dateLabel: 'Today',
    durationMinutes: 20,
    questionCount: 20,
    subjectIds: ['sub_math', 'sub_reasoning', 'sub_english'],
    trackIds: ['track_bank'],
    instructions: 'Quant, reasoning and English in the IBPS ratio.'
  }),
  exam({
    id: 'exam_daily_ssc',
    title: 'SSC Daily Exam - Today',
    type: 'Daily Exam',
    dateLabel: 'Today',
    durationMinutes: 20,
    questionCount: 20,
    subjectIds: ['sub_math', 'sub_reasoning', 'sub_english', 'sub_gk'],
    trackIds: ['track_ssc'],
    instructions: 'Tier-1 pattern practice.'
  }),
  exam({
    id: 'exam_daily_general',
    title: 'General Daily Exam - Mixed',
    type: 'Daily Exam',
    dateLabel: 'Today',
    durationMinutes: 20,
    questionCount: 20,
    subjectIds: ['sub_math', 'sub_history', 'sub_science', 'sub_geography', 'sub_polity'],
    instructions: 'General practice, visible under every exam type.'
  }),
  exam({
    id: 'exam_daily_tomorrow',
    title: 'General Daily Exam - Tomorrow',
    type: 'Daily Exam',
    dateLabel: 'Tomorrow',
    durationMinutes: 20,
    questionCount: 20,
    subjectIds: ['sub_economy', 'sub_english', 'sub_reasoning', 'sub_gk'],
    isActive: false,
    instructions: 'Scheduled for tomorrow. Not activated yet.'
  }),
  exam({
    id: 'exam_gt_general',
    title: 'Weekly Grand Test - All subjects',
    type: 'Grand Test',
    dateLabel: 'This Sunday',
    durationMinutes: 90,
    questionCount: 100,
    subjectIds: subjects.map((s) => s.id),
    difficulty: 'Hard',
    instructions: '100 questions in 90 minutes covering all subjects.'
  })
]

const result = (id, examId, examTitle, type, trackId, total, correct, wrong, skipped, seconds, takenAt, studentName, rank, participants) => ({
  id, examId, examTitle, type, trackId, totalQuestions: total, correct, wrong, skipped,
  timeTakenSeconds: seconds, takenAt, studentName, rank, participants
})

export const examResults = [
  result('res1', 'exam_daily_constable', 'Constable Daily Exam - Today', 'Daily Exam', 'track_constable', 20, 15, 5, 0, 880, now - 3 * HOUR, 'Sai Charan', null, null),
  result('res2', 'exam_daily_constable', 'Constable Daily Exam - Today', 'Daily Exam', 'track_constable', 20, 12, 7, 1, 950, now - 4 * HOUR, 'Pooja Rani', null, null),
  result('res3', 'exam_gt_constable', 'Constable Grand Test', 'Grand Test', 'track_constable', 60, 41, 18, 1, 3200, now - 1 * DAY, 'Sai Charan', 128, 200),
  result('res4', 'exam_daily_group4', 'Group-4 Daily Exam - Today', 'Daily Exam', 'track_group4', 20, 17, 3, 0, 790, now - 5 * HOUR, 'Vamsi Krishna', null, null),
  result('res5', 'exam_daily_bank', 'Bank Daily Exam - Today', 'Daily Exam', 'track_bank', 20, 11, 8, 1, 1010, now - 26 * HOUR, 'Harish Goud', null, null),
  result('res6', 'exam_gt_general', 'Weekly Grand Test - All subjects', 'Grand Test', null, 100, 72, 26, 2, 4980, now - 2 * DAY, 'Sai Charan', 96, 200),
  result('res7', 'exam_daily_ssc', 'SSC Daily Exam - Today', 'Daily Exam', 'track_ssc', 20, 14, 6, 0, 905, now - 30 * HOUR, 'Pooja Rani', null, null),
  result('res8', 'exam_daily_general', 'General Daily Exam - Mixed', 'Daily Exam', null, 20, 13, 6, 1, 1005, now - 2 * DAY, 'Zoya Khan', null, null)
]

/** Topics a student has already studied — drives progress numbers on dashboards. */
export const completedTopicIds = [
  't_num_system', 't_average', 't_physics', 't_chemistry', 't_human_body',
  't_rivers', 't_climate', 't_ancient', 't_panchayat', 't_series', 't_coding', 't_days'
]

/** Seeded subject accuracy (correct / total) for the analysis widgets. */
export const subjectTally = {
  sub_math: [48, 120],
  sub_science: [92, 100],
  sub_history: [29, 78],
  sub_geography: [69, 80],
  sub_polity: [68, 100],
  sub_economy: [52, 80],
  sub_ca: [35, 60],
  sub_gk: [43, 60],
  sub_english: [62, 80],
  sub_reasoning: [67, 80]
}
