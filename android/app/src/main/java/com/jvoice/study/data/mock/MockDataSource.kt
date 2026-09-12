package com.jvoice.study.data.mock

import com.jvoice.study.data.model.AnsweredQuestion
import com.jvoice.study.data.model.Difficulty
import com.jvoice.study.data.model.Exam
import com.jvoice.study.data.model.ExamResult
import com.jvoice.study.data.model.ExamKeyDate
import com.jvoice.study.data.model.ExamSection
import com.jvoice.study.data.model.ExamTrack
import com.jvoice.study.data.model.ExamTrackGroup
import com.jvoice.study.data.model.ExamType
import com.jvoice.study.data.model.LeaderboardEntry
import com.jvoice.study.data.model.LeaderboardPeriod
import com.jvoice.study.data.model.Question
import com.jvoice.study.data.model.Quiz
import com.jvoice.study.data.model.RolePermissionRow
import com.jvoice.study.data.model.Student
import com.jvoice.study.data.model.StudyArticle
import com.jvoice.study.data.model.StudyNotification
import com.jvoice.study.data.model.StudyNotificationType
import com.jvoice.study.data.model.StudyRole
import com.jvoice.study.data.model.StudyUser
import com.jvoice.study.data.model.Subject
import com.jvoice.study.data.model.SubjectScore
import com.jvoice.study.data.model.Topic
import com.jvoice.study.data.model.TopicScore
import com.jvoice.core.i18n.LocalizedText
import com.jvoice.core.i18n.lt
import com.jvoice.core.i18n.AppLanguage

/**
 * ---------------------------------------------------------------------------
 *  MODULE 2 MOCK DATA SOURCE - DUMMY / DEMO CONTENT ONLY
 *  The single source of seed data for the Study & Exam module. Swapping this
 *  for a network or database source later requires no UI changes, because the
 *  repositories are the only things that read it.
 * ---------------------------------------------------------------------------
 */
object MockDataSource {

    const val DEMO_DISCLAIMER = "Demo content • డెమో కంటెంట్ — local mock data only"

    private val NOW = System.currentTimeMillis()
    private const val MINUTE = 60_000L
    private const val HOUR = 60 * MINUTE
    private const val DAY = 24 * HOUR

    private fun img(seed: String) = "https://picsum.photos/seed/" + seed + "/300/300"

    // ================================================================ subjects

    val subjects: List<Subject> = listOf(
        Subject("sub_math", lt("Mathematics", "గణితం"), "🔢"),
        Subject("sub_science", lt("General Science", "సాధారణ విజ్ఞానం"), "🔬"),
        Subject("sub_history", lt("History", "చరిత్ర"), "🏺"),
        Subject("sub_geography", lt("Geography", "భూగోళశాస్త్రం"), "🗺️"),
        Subject("sub_polity", lt("Indian Polity", "భారత రాజ్యాంగం"), "⚖️"),
        Subject("sub_economy", lt("Economy", "ఆర్థిక వ్యవస్థ"), "📈"),
        Subject("sub_ca", lt("Current Affairs", "వర్తమాన అంశాలు"), "📰"),
        Subject("sub_gk", lt("General Knowledge", "సామాన్య జ్ఞానం"), "🌍"),
        Subject("sub_english", lt("English", "ఇంగ్లీష్"), "🔤"),
        Subject("sub_reasoning", lt("Reasoning", "రీజనింగ్"), "🧩")
    )

    // ================================================================== topics

    val topics: List<Topic> = listOf(
        // Mathematics
        Topic("t_num_system", "sub_math", lt("Number System", "సంఖ్యా వ్యవస్థ"), 1, Difficulty.EASY),
        Topic("t_percentage", "sub_math", lt("Percentages", "శాతాలు"), 2, Difficulty.MEDIUM),
        Topic("t_profit_loss", "sub_math", lt("Profit & Loss", "లాభం - నష్టం"), 3, Difficulty.MEDIUM),
        Topic("t_time_work", "sub_math", lt("Time & Work", "కాలం - పని"), 4, Difficulty.HARD),
        Topic("t_ratio", "sub_math", lt("Ratio & Proportion", "నిష్పత్తి"), 5, Difficulty.MEDIUM),
        Topic("t_average", "sub_math", lt("Average", "సగటు"), 6, Difficulty.EASY),
        // General Science
        Topic("t_physics", "sub_science", lt("Physics Basics", "భౌతిక శాస్త్రం"), 1, Difficulty.MEDIUM),
        Topic("t_chemistry", "sub_science", lt("Chemistry Basics", "రసాయన శాస్త్రం"), 2, Difficulty.MEDIUM),
        Topic("t_human_body", "sub_science", lt("Human Body", "మానవ శరీరం"), 3, Difficulty.EASY),
        Topic("t_plants_animals", "sub_science", lt("Plants & Animals", "మొక్కలు - జంతువులు"), 4, Difficulty.EASY),
        Topic("t_environment", "sub_science", lt("Environment & Ecology", "పర్యావరణం"), 5, Difficulty.MEDIUM),
        // History
        Topic("t_ancient", "sub_history", lt("Ancient India", "ప్రాచీన భారతదేశం"), 1, Difficulty.MEDIUM),
        Topic("t_medieval", "sub_history", lt("Medieval India", "మధ్యయుగ భారతదేశం"), 2, Difficulty.MEDIUM),
        Topic("t_modern", "sub_history", lt("Modern India", "ఆధునిక భారతదేశం"), 3, Difficulty.HARD),
        Topic("t_freedom", "sub_history", lt("Indian Freedom Movement", "స్వాతంత్ర్య ఉద్యమం"), 4, Difficulty.MEDIUM),
        // Geography
        Topic("t_rivers", "sub_geography", lt("Indian Rivers", "భారత నదులు"), 1, Difficulty.EASY),
        Topic("t_climate", "sub_geography", lt("Indian Climate & Monsoon", "వాతావరణం"), 2, Difficulty.MEDIUM),
        Topic("t_world_geo", "sub_geography", lt("World Geography", "ప్రపంచ భూగోళం"), 3, Difficulty.MEDIUM),
        Topic("t_physical", "sub_geography", lt("Physical Features of India", "భౌతిక స్వరూపం"), 4, Difficulty.MEDIUM),
        // Polity
        Topic("t_constitution", "sub_polity", lt("Constitution Basics", "రాజ్యాంగ ప్రాథమికాలు"), 1, Difficulty.MEDIUM),
        Topic("t_frights", "sub_polity", lt("Fundamental Rights", "ప్రాథమిక హక్కులు"), 2, Difficulty.MEDIUM),
        Topic("t_parliament", "sub_polity", lt("Parliament", "పార్లమెంటు"), 3, Difficulty.HARD),
        Topic("t_judiciary", "sub_polity", lt("Judiciary", "న్యాయవ్యవస్థ"), 4, Difficulty.MEDIUM),
        Topic("t_panchayat", "sub_polity", lt("Panchayati Raj", "పంచాయతీ రాజ్"), 5, Difficulty.EASY),
        // Economy
        Topic("t_eco_basics", "sub_economy", lt("Basic Economic Concepts", "ఆర్థిక ప్రాథమికాలు"), 1, Difficulty.MEDIUM),
        Topic("t_banking", "sub_economy", lt("Banking in India", "బ్యాంకింగ్"), 2, Difficulty.MEDIUM),
        Topic("t_budget", "sub_economy", lt("Budget & Taxation", "బడ్జెట్ - పన్నులు"), 3, Difficulty.HARD),
        Topic("t_agriculture", "sub_economy", lt("Indian Agriculture", "వ్యవసాయం"), 4, Difficulty.EASY),
        // Current Affairs
        Topic("t_national_af", "sub_ca", lt("National Affairs", "జాతీయ అంశాలు"), 1, Difficulty.MEDIUM),
        Topic("t_international_af", "sub_ca", lt("International Affairs", "అంతర్జాతీయ అంశాలు"), 2, Difficulty.MEDIUM),
        Topic("t_sports_awards", "sub_ca", lt("Sports & Awards", "క్రీడలు - అవార్డులు"), 3, Difficulty.EASY),
        // General Knowledge
        Topic("t_culture", "sub_gk", lt("Indian Culture & Heritage", "భారతీయ సంస్కృతి"), 1, Difficulty.EASY),
        Topic("t_books", "sub_gk", lt("Books & Authors", "పుస్తకాలు - రచయితలు"), 2, Difficulty.MEDIUM),
        Topic("t_days", "sub_gk", lt("Important Days", "ముఖ్యమైన దినాలు"), 3, Difficulty.EASY),
        // English
        Topic("t_grammar", "sub_english", lt("Grammar Basics", "వ్యాకరణం"), 1, Difficulty.MEDIUM),
        Topic("t_vocabulary", "sub_english", lt("Vocabulary Building", "పదజాలం"), 2, Difficulty.MEDIUM),
        Topic("t_synonyms", "sub_english", lt("Synonyms & Antonyms", "పర్యాయ - వ్యతిరేక పదాలు"), 3, Difficulty.EASY),
        Topic("t_sentence", "sub_english", lt("Sentence Correction", "వాక్య సవరణ"), 4, Difficulty.HARD),
        // Reasoning
        Topic("t_series", "sub_reasoning", lt("Number & Letter Series", "శ్రేణులు"), 1, Difficulty.EASY),
        Topic("t_coding", "sub_reasoning", lt("Coding & Decoding", "కోడింగ్ - డీకోడింగ్"), 2, Difficulty.MEDIUM),
        Topic("t_blood", "sub_reasoning", lt("Blood Relations", "బంధుత్వాలు"), 3, Difficulty.MEDIUM),
        Topic("t_direction", "sub_reasoning", lt("Direction Sense", "దిక్కుల జ్ఞానం"), 4, Difficulty.EASY)
    )

    // ============================================================ content bank

    val articles: List<StudyArticle> = StudyContentData.articles
    val questions: List<Question> = StudyContentData.questions
    val quizzes: List<Quiz> = StudyContentData.quizzes

    // =================================================================== users

    val student = Student(
        userId = "u_st1",
        name = "Sai Charan",
        targetExam = "Group-2 / SSC",
        className = "Degree final year",
        studyStreakDays = 12,
        avatarUrl = img("student1")
    )

    val users: List<StudyUser> = listOf(
        StudyUser("u_st1", "Sai Charan", "sai.charan@demo.in", StudyRole.STUDENT, joinedOn = "12 Feb 2025", avatarUrl = img("student1")),
        StudyUser("u_st2", "Pooja Rani", "pooja.rani@demo.in", StudyRole.STUDENT, joinedOn = "03 Mar 2025", avatarUrl = img("student2")),
        StudyUser("u_st3", "Vamsi Krishna", "vamsi.k@demo.in", StudyRole.STUDENT, joinedOn = "19 Mar 2025", avatarUrl = img("student3")),
        StudyUser("u_st4", "Zoya Khan", "zoya.khan@demo.in", StudyRole.STUDENT, isActive = false, joinedOn = "21 Apr 2025", avatarUrl = img("student4")),
        StudyUser("u_st5", "Harish Goud", "harish.goud@demo.in", StudyRole.STUDENT, joinedOn = "02 May 2025", avatarUrl = img("student5")),
        StudyUser("u_cc1", "Sunitha Rao", "sunitha.rao@jvoice.demo", StudyRole.CONTENT_CREATOR, joinedOn = "10 Jan 2025", avatarUrl = img("cc1")),
        StudyUser("u_cc2", "Divya Sree", "divya.sree@jvoice.demo", StudyRole.CONTENT_CREATOR, joinedOn = "18 Jan 2025", avatarUrl = img("cc2")),
        StudyUser("u_cc3", "Kiran Kumar", "kiran.kumar@jvoice.demo", StudyRole.CONTENT_CREATOR, joinedOn = "05 Feb 2025", avatarUrl = img("cc3")),
        StudyUser("u_ea1", "Mahesh Chandra", "mahesh.c@jvoice.demo", StudyRole.EXAM_ADMIN, joinedOn = "08 Jan 2025", avatarUrl = img("ea1")),
        StudyUser("u_ea2", "Farhana Begum", "farhana.b@jvoice.demo", StudyRole.EXAM_ADMIN, joinedOn = "22 Feb 2025", avatarUrl = img("ea2")),
        StudyUser("u_sa1", "Sridevi Naidu", "sridevi.n@jvoice.demo", StudyRole.STUDY_ADMIN, joinedOn = "04 Jan 2025", avatarUrl = img("sa1")),
        StudyUser("u_sa2", "Naveen Kumar", "naveen.k@jvoice.demo", StudyRole.STUDY_ADMIN, joinedOn = "27 Mar 2025", avatarUrl = img("sa2")),
        StudyUser("u_su1", "Ravi Teja Sharma", "superadmin@jvoice.demo", StudyRole.SUPER_ADMIN, joinedOn = "02 Jan 2024", avatarUrl = img("su1"))
    )

    val demoAccounts: Map<StudyRole, StudyUser> = mapOf(
        StudyRole.STUDENT to users.first { it.id == "u_st1" },
        StudyRole.CONTENT_CREATOR to users.first { it.id == "u_cc1" },
        StudyRole.EXAM_ADMIN to users.first { it.id == "u_ea1" },
        StudyRole.STUDY_ADMIN to users.first { it.id == "u_sa1" },
        StudyRole.SUPER_ADMIN to users.first { it.id == "u_su1" }
    )

    // =================================================================== exams

    private fun pick(subjectId: String, count: Int, skip: Int = 0): List<String> =
        questions.filter { it.subjectId == subjectId }.drop(skip).take(count).map { it.id }

    /** Builds a mixed paper by rotating through the given subjects. */
    private fun mixedPaper(perSubject: Map<String, Int>, skip: Int = 0): List<String> =
        perSubject.flatMap { (subjectId, count) -> pick(subjectId, count, skip) }.distinct()

    private val dailyMix = mapOf(
        "sub_math" to 5,
        "sub_history" to 5,
        "sub_science" to 5,
        "sub_geography" to 3,
        "sub_polity" to 2
    )

    val dailyExams: List<Exam> = listOf(
        Exam(
            id = "exam_daily_today",
            title = lt("Daily Exam - Today", "రోజువారీ పరీక్ష - ఈ రోజు"),
            type = ExamType.DAILY,
            dateLabel = "Today",
            durationMinutes = 20,
            questionIds = mixedPaper(dailyMix),
            subjectIds = dailyMix.keys.toList(),
            difficulty = Difficulty.MEDIUM,
            instructions = lt("20 questions, 20 minutes. Each question carries one mark. There is no negative marking in this demo.", "20 ప్రశ్నలు, 20 నిమిషాలు. ప్రతి ప్రశ్నకు ఒక మార్కు. ఈ డెమోలో నెగెటివ్ మార్కింగ్ లేదు.")
        ),
        Exam(
            id = "exam_daily_y1",
            title = lt("Daily Exam - Yesterday", "రోజువారీ పరీక్ష - నిన్న"),
            type = ExamType.DAILY,
            dateLabel = "Yesterday",
            durationMinutes = 20,
            questionIds = mixedPaper(
                mapOf("sub_economy" to 5, "sub_english" to 5, "sub_reasoning" to 5, "sub_gk" to 5)
            ),
            subjectIds = listOf("sub_economy", "sub_english", "sub_reasoning", "sub_gk"),
            difficulty = Difficulty.MEDIUM,
            instructions = lt("20 questions, 20 minutes.", "20 ప్రశ్నలు, 20 నిమిషాలు.")
        ),
        Exam(
            id = "exam_daily_y2",
            title = lt("Daily Exam - Polity Special", "రోజువారీ పరీక్ష - రాజ్యాంగం ప్రత్యేకం"),
            type = ExamType.DAILY,
            dateLabel = "2 days ago",
            durationMinutes = 15,
            questionIds = pick("sub_polity", 9),
            subjectIds = listOf("sub_polity"),
            difficulty = Difficulty.HARD,
            instructions = lt("Single-subject drill on Indian Polity.", "భారత రాజ్యాంగంపై ఒకే సబ్జెక్ట్ డ్రిల్.")
        ),
        Exam(
            id = "exam_daily_tomorrow",
            title = lt("Daily Exam - Tomorrow", "రోజువారీ పరీక్ష - రేపు"),
            type = ExamType.DAILY,
            dateLabel = "Tomorrow",
            durationMinutes = 20,
            questionIds = mixedPaper(dailyMix, skip = 2),
            subjectIds = dailyMix.keys.toList(),
            difficulty = Difficulty.MEDIUM,
            instructions = lt("Scheduled for tomorrow. Currently inactive.", "రేపు షెడ్యూల్ చేయబడింది. ప్రస్తుతం యాక్టివ్‌గా లేదు."),
            isActive = false
        )
    )

    /** The Grand Test paper: every subject, up to 100 questions from the bank. */
    private fun grandTestPaper(skip: Int = 0): List<String> {
        val perSubject = 12
        val ids = subjects.flatMap { pick(it.id, perSubject, skip) }.distinct()
        if (ids.size >= 100) return ids.take(100)
        // Top up from the rest of the bank so the paper always reaches 100 items.
        val remaining = questions.map { it.id }.filterNot { ids.contains(it) }
        return (ids + remaining).distinct().take(100)
    }

    val grandTests: List<Exam> = listOf(
        Exam(
            id = "exam_gt_week",
            title = lt("Weekly Grand Test - Week 34", "వారపు గ్రాండ్ టెస్ట్ - 34వ వారం"),
            type = ExamType.GRAND_TEST,
            dateLabel = "This Sunday",
            durationMinutes = 90,
            questionIds = grandTestPaper(),
            subjectIds = subjects.map { it.id },
            difficulty = Difficulty.HARD,
            instructions = lt("100 questions in 90 minutes covering all subjects. Use Mark for Review to flag questions and the palette to jump between them.", "90 నిమిషాల్లో అన్ని సబ్జెక్టులను కవర్ చేసే 100 ప్రశ్నలు. ప్రశ్నలను గుర్తు పెట్టడానికి Mark for Review, వాటి మధ్య మారడానికి పాలెట్ ఉపయోగించండి.")
        ),
        Exam(
            id = "exam_gt_prev",
            title = lt("Weekly Grand Test - Week 33", "వారపు గ్రాండ్ టెస్ట్ - 33వ వారం"),
            type = ExamType.GRAND_TEST,
            dateLabel = "Last Sunday",
            durationMinutes = 90,
            questionIds = grandTestPaper(skip = 1),
            subjectIds = subjects.map { it.id },
            difficulty = Difficulty.HARD,
            instructions = lt("Completed. Review your paper from the results screen.", "పూర్తయింది. ఫలితాల స్క్రీన్ నుంచి మీ పేపర్ సమీక్షించండి.")
        )
    )

    // ============================================================ exam tracks

    /**
     * The target exams a student can prepare for. Chosen first, before any study
     * material is shown, so every screen after it is scoped to one exam.
     *
     * Pattern figures follow the real notifications closely enough to be
     * recognisable; the papers generated below are drawn from the demo question
     * bank, so a generated test is smaller than the printed pattern.
     */
    private fun dates(vararg rows: Pair<LocalizedText, String>): List<ExamKeyDate> =
        rows.map { (label, date) -> ExamKeyDate(label, date) }

    val examTracks: List<ExamTrack> = listOf(
        ExamTrack(
            id = "track_constable",
            name = lt("Police Constable", "పోలీస్ కానిస్టేబుల్"),
            shortName = "Constable",
            emoji = "👮",
            group = ExamTrackGroup.POLICE,
            tagline = lt("Civil & AR Constable - preliminary and final written test", "సివిల్ - ఏఆర్ కానిస్టేబుల్ - ప్రిలిమినరీ, ఫైనల్ రాత పరీక్ష"),
            qualification = lt("Intermediate / 10+2", "ఇంటర్మీడియట్ / 10+2"),
            ageLimit = lt("18 - 22 years", "18 - 22 సంవత్సరాలు"),
            vacancyLabel = lt("≈ 15,600 posts", "≈ 15,600 పోస్టులు"),
            examDateLabel = lt("Notification expected soon", "నోటిఫికేషన్ త్వరలో వచ్చే అవకాశం"),
            totalQuestions = 200,
            totalMarks = 200,
            durationMinutes = 180,
            negativeMarking = lt("No negative marking", "నెగెటివ్ మార్కింగ్ లేదు"),
            sections = listOf(
                ExamSection("sub_gk", 40, 40),
                ExamSection("sub_ca", 30, 30),
                ExamSection("sub_science", 30, 30),
                ExamSection("sub_math", 20, 20),
                ExamSection("sub_reasoning", 20, 20),
                ExamSection("sub_history", 20, 20),
                ExamSection("sub_geography", 20, 20),
                ExamSection("sub_polity", 20, 20)
            ),
            stages = listOf(
                lt("Preliminary Written Test", "ప్రిలిమినరీ రాత పరీక్ష"),
                lt("PET / PMT", "పీఈటీ / పీఎంటీ"),
                lt("Final Written Test", "ఫైనల్ రాత పరీక్ష")
            ),
            keyDates = dates(
                lt("Notification", "నోటిఫికేషన్") to "12 Sep 2026",
                lt("Last date to apply", "దరఖాస్తుకు చివరి తేదీ") to "05 Oct 2026",
                lt("Hall ticket", "హాల్ టికెట్") to "18 Nov 2026",
                lt("Preliminary exam", "ప్రిలిమినరీ పరీక్ష") to "29 Nov 2026"
            )
        ),
        ExamTrack(
            id = "track_si",
            name = lt("Police Sub Inspector", "పోలీస్ ఎస్‌ఐ"),
            shortName = "SI",
            emoji = "🚔",
            group = ExamTrackGroup.POLICE,
            tagline = lt("SI of Police - preliminary, physical and mains", "పోలీస్ ఎస్‌ఐ - ప్రిలిమినరీ, ఫిజికల్, మెయిన్స్"),
            qualification = lt("Any degree", "ఏదైనా డిగ్రీ"),
            ageLimit = lt("21 - 25 years", "21 - 25 సంవత్సరాలు"),
            vacancyLabel = lt("≈ 1,200 posts", "≈ 1,200 పోస్టులు"),
            examDateLabel = lt("Prelims in 3 months", "3 నెలల్లో ప్రిలిమ్స్"),
            totalQuestions = 200,
            totalMarks = 200,
            durationMinutes = 180,
            negativeMarking = lt("No negative marking", "నెగెటివ్ మార్కింగ్ లేదు"),
            sections = listOf(
                ExamSection("sub_gk", 30, 30),
                ExamSection("sub_ca", 25, 25),
                ExamSection("sub_science", 25, 25),
                ExamSection("sub_history", 20, 20),
                ExamSection("sub_geography", 20, 20),
                ExamSection("sub_polity", 20, 20),
                ExamSection("sub_math", 20, 20),
                ExamSection("sub_reasoning", 20, 20),
                ExamSection("sub_english", 20, 20)
            ),
            stages = listOf(
                lt("Preliminary Written Test", "ప్రిలిమినరీ రాత పరీక్ష"),
                lt("PET / PMT", "పీఈటీ / పీఎంటీ"),
                lt("Mains", "మెయిన్స్"),
                lt("Interview", "ఇంటర్వ్యూ")
            ),
            keyDates = dates(
                lt("Notification", "నోటిఫికేషన్") to "20 Sep 2026",
                lt("Last date to apply", "దరఖాస్తుకు చివరి తేదీ") to "14 Oct 2026",
                lt("Prelims exam", "ప్రిలిమ్స్ పరీక్ష") to "06 Dec 2026",
                lt("PET / PMT", "పీఈటీ / పీఎంటీ") to "Jan 2027"
            )
        ),
        ExamTrack(
            id = "track_group1",
            name = lt("Group-1 Services", "గ్రూప్-1"),
            shortName = "Group-1",
            emoji = "🏛️",
            group = ExamTrackGroup.GROUPS,
            tagline = lt("Deputy Collector, DSP and other Group-1 posts", "డిప్యూటీ కలెక్టర్, డీఎస్‌పీ తదితర గ్రూప్-1 పోస్టులు"),
            qualification = lt("Any degree", "ఏదైనా డిగ్రీ"),
            ageLimit = lt("18 - 46 years", "18 - 46 సంవత్సరాలు"),
            vacancyLabel = lt("≈ 560 posts", "≈ 560 పోస్టులు"),
            examDateLabel = lt("Prelims announced", "ప్రిలిమ్స్ ప్రకటించారు"),
            totalQuestions = 150,
            totalMarks = 150,
            durationMinutes = 150,
            negativeMarking = lt("No negative marking", "నెగెటివ్ మార్కింగ్ లేదు"),
            sections = listOf(
                ExamSection("sub_history", 25, 25),
                ExamSection("sub_ca", 25, 25),
                ExamSection("sub_polity", 20, 20),
                ExamSection("sub_economy", 20, 20),
                ExamSection("sub_science", 20, 20),
                ExamSection("sub_geography", 15, 15),
                ExamSection("sub_reasoning", 15, 15),
                ExamSection("sub_gk", 10, 10)
            ),
            stages = listOf(
                lt("Prelims (screening)", "ప్రిలిమ్స్ (స్క్రీనింగ్)"),
                lt("Mains - 6 papers", "మెయిన్స్ - 6 పేపర్లు"),
                lt("Interview", "ఇంటర్వ్యూ")
            ),
            keyDates = dates(
                lt("Notification", "నోటిఫికేషన్") to "Released",
                lt("Last date to apply", "దరఖాస్తుకు చివరి తేదీ") to "30 Sep 2026",
                lt("Prelims exam", "ప్రిలిమ్స్ పరీక్ష") to "15 Nov 2026",
                lt("Mains", "మెయిన్స్") to "Feb 2027"
            )
        ),
        ExamTrack(
            id = "track_group2",
            name = lt("Group-2 Services", "గ్రూప్-2"),
            shortName = "Group-2",
            emoji = "📋",
            group = ExamTrackGroup.GROUPS,
            tagline = lt("Executive and non-executive Group-2 posts", "ఎగ్జిక్యూటివ్, నాన్-ఎగ్జిక్యూటివ్ గ్రూప్-2 పోస్టులు"),
            qualification = lt("Any degree", "ఏదైనా డిగ్రీ"),
            ageLimit = lt("18 - 44 years", "18 - 44 సంవత్సరాలు"),
            vacancyLabel = lt("≈ 780 posts", "≈ 780 పోస్టులు"),
            examDateLabel = lt("Mains in 5 months", "5 నెలల్లో మెయిన్స్"),
            totalQuestions = 150,
            totalMarks = 150,
            durationMinutes = 150,
            negativeMarking = lt("No negative marking", "నెగెటివ్ మార్కింగ్ లేదు"),
            sections = listOf(
                ExamSection("sub_history", 30, 30),
                ExamSection("sub_polity", 30, 30),
                ExamSection("sub_economy", 30, 30),
                ExamSection("sub_geography", 20, 20),
                ExamSection("sub_ca", 20, 20),
                ExamSection("sub_gk", 20, 20)
            ),
            stages = listOf(
                lt("Paper 1 - General Studies", "పేపర్ 1 - జనరల్ స్టడీస్"),
                lt("Paper 2 - History & Polity", "పేపర్ 2 - చరిత్ర, రాజ్యాంగం"),
                lt("Paper 3 - Economy", "పేపర్ 3 - ఆర్థిక వ్యవస్థ"),
                lt("Paper 4 - Telangana Movement", "పేపర్ 4 - తెలంగాణ ఉద్యమం")
            ),
            keyDates = dates(
                lt("Notification", "నోటిఫికేషన్") to "Released",
                lt("Last date to apply", "దరఖాస్తుకు చివరి తేదీ") to "22 Sep 2026",
                lt("Mains exam", "మెయిన్స్ పరీక్ష") to "17 Jan 2027",
                lt("Results", "ఫలితాలు") to "Apr 2027"
            )
        ),
        ExamTrack(
            id = "track_group4",
            name = lt("Group-4 Junior Assistant", "గ్రూప్-4 జూనియర్ అసిస్టెంట్"),
            shortName = "Group-4",
            emoji = "🗂️",
            group = ExamTrackGroup.GROUPS,
            tagline = lt("Junior Assistant, Junior Accountant and Typist posts", "జూనియర్ అసిస్టెంట్, జూనియర్ అకౌంటెంట్, టైపిస్ట్ పోస్టులు"),
            qualification = lt("Any degree", "ఏదైనా డిగ్రీ"),
            ageLimit = lt("18 - 44 years", "18 - 44 సంవత్సరాలు"),
            vacancyLabel = lt("≈ 9,100 posts", "≈ 9,100 పోస్టులు"),
            examDateLabel = lt("Notification released", "నోటిఫికేషన్ విడుదలైంది"),
            totalQuestions = 150,
            totalMarks = 150,
            durationMinutes = 150,
            negativeMarking = lt("No negative marking", "నెగెటివ్ మార్కింగ్ లేదు"),
            sections = listOf(
                ExamSection("sub_gk", 30, 30),
                ExamSection("sub_ca", 25, 25),
                ExamSection("sub_history", 20, 20),
                ExamSection("sub_geography", 20, 20),
                ExamSection("sub_polity", 20, 20),
                ExamSection("sub_economy", 15, 15),
                ExamSection("sub_science", 10, 10),
                ExamSection("sub_math", 5, 5),
                ExamSection("sub_reasoning", 5, 5)
            ),
            stages = listOf(
                lt("Paper 1 - General Studies", "పేపర్ 1 - జనరల్ స్టడీస్"),
                lt("Paper 2 - Secretarial Abilities", "పేపర్ 2 - సెక్రటేరియల్ ఎబిలిటీస్")
            ),
            keyDates = dates(
                lt("Notification", "నోటిఫికేషన్") to "Released",
                lt("Last date to apply", "దరఖాస్తుకు చివరి తేదీ") to "28 Sep 2026",
                lt("Hall ticket", "హాల్ టికెట్") to "20 Dec 2026",
                lt("Exam day", "పరీక్ష రోజు") to "03 Jan 2027"
            )
        ),
        ExamTrack(
            id = "track_vro",
            name = lt("VRO / Panchayat Secretary", "వీఆర్‌ఓ / పంచాయతీ కార్యదర్శి"),
            shortName = "VRO",
            emoji = "🏡",
            group = ExamTrackGroup.GROUPS,
            tagline = lt("Village Revenue Officer and Panchayat Secretary", "విలేజ్ రెవెన్యూ ఆఫీసర్, పంచాయతీ సెక్రటరీ"),
            qualification = lt("Intermediate / Any degree", "ఇంటర్మీడియట్ / ఏదైనా డిగ్రీ"),
            ageLimit = lt("18 - 44 years", "18 - 44 సంవత్సరాలు"),
            vacancyLabel = lt("≈ 2,400 posts", "≈ 2,400 పోస్టులు"),
            examDateLabel = lt("Expected next quarter", "వచ్చే త్రైమాసికంలో అంచనా"),
            totalQuestions = 150,
            totalMarks = 150,
            durationMinutes = 150,
            negativeMarking = lt("No negative marking", "నెగెటివ్ మార్కింగ్ లేదు"),
            sections = listOf(
                ExamSection("sub_gk", 40, 40),
                ExamSection("sub_ca", 20, 20),
                ExamSection("sub_science", 20, 20),
                ExamSection("sub_polity", 20, 20),
                ExamSection("sub_math", 20, 20),
                ExamSection("sub_reasoning", 15, 15),
                ExamSection("sub_geography", 15, 15)
            ),
            stages = listOf(
                lt("Written Test", "రాత పరీక్ష"),
                lt("Certificate Verification", "సర్టిఫికెట్ వెరిఫికేషన్")
            ),
            keyDates = dates(
                lt("Notification", "నోటిఫికేషన్") to "Expected Oct 2026",
                lt("Exam window", "పరీక్ష సమయం") to "Jan 2027"
            )
        ),
        ExamTrack(
            id = "track_ssc",
            name = lt("SSC CGL / CHSL", "ఎస్‌ఎస్‌సీ సీజీఎల్"),
            shortName = "SSC",
            emoji = "🇮🇳",
            group = ExamTrackGroup.SSC_RAILWAY,
            tagline = lt("Central government graduate and 10+2 level posts", "కేంద్ర ప్రభుత్వ డిగ్రీ, 10+2 స్థాయి పోస్టులు"),
            qualification = lt("Any degree (CGL) / 10+2 (CHSL)", "ఏదైనా డిగ్రీ (CGL) / 10+2 (CHSL)"),
            ageLimit = lt("18 - 32 years", "18 - 32 సంవత్సరాలు"),
            vacancyLabel = lt("≈ 17,700 posts", "≈ 17,700 పోస్టులు"),
            examDateLabel = lt("Tier-1 in 2 months", "2 నెలల్లో టైర్-1"),
            totalQuestions = 100,
            totalMarks = 200,
            durationMinutes = 60,
            negativeMarking = lt("0.50 marks per wrong answer", "తప్పు సమాధానానికి 0.50 మార్కులు కోత"),
            sections = listOf(
                ExamSection("sub_math", 25, 50),
                ExamSection("sub_reasoning", 25, 50),
                ExamSection("sub_english", 25, 50),
                ExamSection("sub_gk", 25, 50)
            ),
            stages = listOf(
                lt("Tier-1 CBT", "టైర్-1 సీబీటీ"),
                lt("Tier-2 CBT", "టైర్-2 సీబీటీ"),
                lt("Skill / Typing Test", "స్కిల్ / టైపింగ్ పరీక్ష")
            ),
            keyDates = dates(
                lt("Last date to apply", "దరఖాస్తుకు చివరి తేదీ") to "18 Sep 2026",
                lt("Tier-1 CBT", "టైర్-1 సీబీటీ") to "24 Oct 2026",
                lt("Tier-2 CBT", "టైర్-2 సీబీటీ") to "Feb 2027"
            )
        ),
        ExamTrack(
            id = "track_rrb",
            name = lt("RRB NTPC / Group-D", "ఆర్‌ఆర్‌బీ ఎన్‌టీపీసీ"),
            shortName = "RRB",
            emoji = "🚆",
            group = ExamTrackGroup.SSC_RAILWAY,
            tagline = lt("Railway non-technical and level-1 posts", "రైల్వే నాన్-టెక్నికల్, లెవెల్-1 పోస్టులు"),
            qualification = lt("10+2 / Any degree", "10+2 / ఏదైనా డిగ్రీ"),
            ageLimit = lt("18 - 33 years", "18 - 33 సంవత్సరాలు"),
            vacancyLabel = lt("≈ 35,000 posts", "≈ 35,000 పోస్టులు"),
            examDateLabel = lt("CBT-1 in 4 months", "4 నెలల్లో సీబీటీ-1"),
            totalQuestions = 100,
            totalMarks = 100,
            durationMinutes = 90,
            negativeMarking = lt("1/3 mark per wrong answer", "తప్పు సమాధానానికి 1/3 మార్కు కోత"),
            sections = listOf(
                ExamSection("sub_math", 30, 30),
                ExamSection("sub_reasoning", 30, 30),
                ExamSection("sub_gk", 25, 25),
                ExamSection("sub_ca", 15, 15)
            ),
            stages = listOf(
                lt("CBT-1", "సీబీటీ-1"),
                lt("CBT-2", "సీబీటీ-2"),
                lt("Typing / PET", "టైపింగ్ / పీఈటీ"),
                lt("Document Verification", "డాక్యుమెంట్ వెరిఫికేషన్")
            ),
            keyDates = dates(
                lt("Notification", "నోటిఫికేషన్") to "Released",
                lt("Last date to apply", "దరఖాస్తుకు చివరి తేదీ") to "10 Oct 2026",
                lt("CBT-1", "సీబీటీ-1") to "Dec 2026"
            )
        ),
        ExamTrack(
            id = "track_bank",
            name = lt("IBPS PO / Clerk", "బ్యాంక్ పీఓ / క్లర్క్"),
            shortName = "Bank",
            emoji = "🏦",
            group = ExamTrackGroup.BANKING,
            tagline = lt("Probationary Officer and Clerk prelims", "ప్రొబేషనరీ ఆఫీసర్, క్లర్క్ ప్రిలిమ్స్"),
            qualification = lt("Any degree", "ఏదైనా డిగ్రీ"),
            ageLimit = lt("20 - 30 years", "20 - 30 సంవత్సరాలు"),
            vacancyLabel = lt("≈ 5,200 posts", "≈ 5,200 పోస్టులు"),
            examDateLabel = lt("Prelims in 6 weeks", "6 వారాల్లో ప్రిలిమ్స్"),
            totalQuestions = 100,
            totalMarks = 100,
            durationMinutes = 60,
            negativeMarking = lt("0.25 marks per wrong answer", "తప్పు సమాధానానికి 0.25 మార్కులు కోత"),
            sections = listOf(
                ExamSection("sub_math", 35, 35),
                ExamSection("sub_reasoning", 35, 35),
                ExamSection("sub_english", 30, 30)
            ),
            stages = listOf(
                lt("Prelims", "ప్రిలిమ్స్"),
                lt("Mains", "మెయిన్స్"),
                lt("Interview", "ఇంటర్వ్యూ")
            ),
            keyDates = dates(
                lt("Last date to apply", "దరఖాస్తుకు చివరి తేదీ") to "08 Sep 2026",
                lt("Prelims", "ప్రిలిమ్స్") to "04 Oct 2026",
                lt("Mains", "మెయిన్స్") to "22 Nov 2026"
            )
        ),
        ExamTrack(
            id = "track_dsc",
            name = lt("DSC / TET", "డీఎస్‌సీ / టెట్"),
            shortName = "DSC",
            emoji = "🎓",
            group = ExamTrackGroup.TEACHING,
            tagline = lt("Teacher recruitment and eligibility test", "ఉపాధ్యాయ నియామకం, అర్హత పరీక్ష"),
            qualification = lt("D.Ed / B.Ed", "డీఎడ్ / బీఎడ్"),
            ageLimit = lt("18 - 46 years", "18 - 46 సంవత్సరాలు"),
            vacancyLabel = lt("≈ 11,000 posts", "≈ 11,000 పోస్టులు"),
            examDateLabel = lt("TET in 3 months", "3 నెలల్లో టెట్"),
            totalQuestions = 150,
            totalMarks = 150,
            durationMinutes = 150,
            negativeMarking = lt("No negative marking", "నెగెటివ్ మార్కింగ్ లేదు"),
            sections = listOf(
                ExamSection("sub_gk", 30, 30),
                ExamSection("sub_english", 30, 30),
                ExamSection("sub_math", 30, 30),
                ExamSection("sub_science", 30, 30),
                ExamSection("sub_history", 15, 15),
                ExamSection("sub_geography", 15, 15)
            ),
            stages = listOf(
                lt("TET Paper", "టెట్ పేపర్"),
                lt("DSC Written Test", "డీఎస్‌సీ రాత పరీక్ష"),
                lt("Merit List", "మెరిట్ జాబితా")
            ),
            keyDates = dates(
                lt("TET notification", "టెట్ నోటిఫికేషన్") to "Released",
                lt("TET exam", "టెట్ పరీక్ష") to "23 Nov 2026",
                lt("DSC written test", "డీఎస్‌సీ రాత పరీక్ష") to "Feb 2027"
            )
        )
    )

    /**
     * One live Daily Exam and one Weekly Grand Test per track, built from that
     * track's own section mix so a Constable test looks nothing like a Bank test.
     * Counts are capped by what the demo question bank actually holds.
     */
    private fun trackPaper(track: ExamTrack, cap: Int, skip: Int = 0): List<String> {
        val total = track.sections.sumOf { it.questions }.coerceAtLeast(1)
        return track.sections.flatMap { section ->
            val share = (section.questions * cap) / total
            pick(section.subjectId, share.coerceAtLeast(1), skip)
        }.distinct().take(cap)
    }

    val trackExams: List<Exam> = examTracks.flatMap { track ->
        val dailyIds = trackPaper(track, 20)
        val grandIds = trackPaper(track, 60, skip = 1)
        listOf(
            Exam(
                id = "exam_daily_" + track.id,
                title = lt(
                    track.shortName + " Daily Exam - Today",
                    track.shortName + " రోజువారీ పరీక్ష - ఈ రోజు"
                ),
                type = ExamType.DAILY,
                dateLabel = "Today",
                durationMinutes = 20,
                questionIds = dailyIds,
                subjectIds = track.subjectIds,
                difficulty = Difficulty.MEDIUM,
                instructions = dailyInstructions(track, dailyIds.size),
                trackIds = listOf(track.id)
            ),
            Exam(
                id = "exam_gt_" + track.id,
                title = lt(
                    track.shortName + " Grand Test",
                    track.shortName + " గ్రాండ్ టెస్ట్"
                ),
                type = ExamType.GRAND_TEST,
                dateLabel = "This Sunday",
                durationMinutes = 60,
                questionIds = grandIds,
                subjectIds = track.subjectIds,
                difficulty = Difficulty.HARD,
                instructions = grandInstructions(track, grandIds.size),
                trackIds = listOf(track.id)
            )
        )
    }

    /**
     * Instruction text for a generated paper, composed in both languages.
     *
     * Composed rather than table-looked-up because the figures come from the
     * track. Writing the two sentence shapes separately - instead of translating
     * fragments and gluing them - keeps each one grammatical in its own language;
     * Telugu does not take an English sentence's word order.
     */
    private fun dailyInstructions(track: ExamTrack, count: Int) = LocalizedText(
        en = "$count questions in 20 minutes, mixed exactly like the " +
            track.name.en + " paper. " + track.negativeMarking.en + ".",
        te = track.name.te + " పేపర్ లాగే మిక్స్ చేసిన $count ప్రశ్నలు, 20 నిమిషాలు. " +
            track.negativeMarking.te + "."
    )

    private fun grandInstructions(track: ExamTrack, count: Int) = LocalizedText(
        en = "Full-pattern mock for " + track.name.en + " (" +
            track.patternLabel(AppLanguage.ENGLISH) + " in the real exam). " +
            "This demo paper carries $count questions in 60 minutes.",
        te = track.name.te + " కోసం పూర్తి నమూనా మాక్ (అసలు పరీక్షలో " +
            track.patternLabel(AppLanguage.TELUGU) + "). " +
            "ఈ డెమో పేపర్‌లో 60 నిమిషాల్లో $count ప్రశ్నలు ఉంటాయి."
    )

    val exams: List<Exam> = trackExams + dailyExams + grandTests

    // ============================================ seeded performance aggregates

    /**
     * Topic-level aggregates from the student's earlier practice. Subject figures
     * are summed from these, so the subject and topic views can never disagree.
     *
     * Chosen so the demo shows a realistic spread: Science / Geography / Reasoning
     * strong, Polity and Economy needing practice, Mathematics and History weak.
     */
    val seedTopicScores: List<TopicScore> = listOf(
        // Mathematics -> 48/120 = 40% (weak)
        TopicScore("t_num_system", "sub_math", 12, 20),
        TopicScore("t_percentage", "sub_math", 6, 20),
        TopicScore("t_profit_loss", "sub_math", 5, 20),
        TopicScore("t_time_work", "sub_math", 8, 20),
        TopicScore("t_ratio", "sub_math", 7, 20),
        TopicScore("t_average", "sub_math", 10, 20),
        // General Science -> 92/100 = 92% (strong)
        TopicScore("t_physics", "sub_science", 18, 20),
        TopicScore("t_chemistry", "sub_science", 19, 20),
        TopicScore("t_human_body", "sub_science", 18, 20),
        TopicScore("t_plants_animals", "sub_science", 19, 20),
        TopicScore("t_environment", "sub_science", 18, 20),
        // History -> 29/78 = 37% (weak)
        TopicScore("t_ancient", "sub_history", 8, 10),
        TopicScore("t_medieval", "sub_history", 5, 10),
        TopicScore("t_modern", "sub_history", 9, 30),
        TopicScore("t_freedom", "sub_history", 7, 28),
        // Geography -> 69/80 = 86% (strong)
        TopicScore("t_rivers", "sub_geography", 17, 20),
        TopicScore("t_climate", "sub_geography", 18, 20),
        TopicScore("t_world_geo", "sub_geography", 16, 20),
        TopicScore("t_physical", "sub_geography", 18, 20),
        // Polity -> 68/100 = 68% (needs practice)
        TopicScore("t_constitution", "sub_polity", 15, 20),
        TopicScore("t_frights", "sub_polity", 9, 20),
        TopicScore("t_parliament", "sub_polity", 14, 20),
        TopicScore("t_judiciary", "sub_polity", 13, 20),
        TopicScore("t_panchayat", "sub_polity", 17, 20),
        // Economy -> 52/80 = 65% (needs practice)
        TopicScore("t_eco_basics", "sub_economy", 14, 20),
        TopicScore("t_banking", "sub_economy", 13, 20),
        TopicScore("t_budget", "sub_economy", 12, 20),
        TopicScore("t_agriculture", "sub_economy", 13, 20),
        // Current Affairs -> 35/60 = 58% (needs practice)
        TopicScore("t_national_af", "sub_ca", 12, 20),
        TopicScore("t_international_af", "sub_ca", 10, 20),
        TopicScore("t_sports_awards", "sub_ca", 13, 20),
        // General Knowledge -> 43/60 = 71% (needs practice)
        TopicScore("t_culture", "sub_gk", 15, 20),
        TopicScore("t_books", "sub_gk", 13, 20),
        TopicScore("t_days", "sub_gk", 15, 20),
        // English -> 62/80 = 77% (needs practice)
        TopicScore("t_grammar", "sub_english", 16, 20),
        TopicScore("t_vocabulary", "sub_english", 15, 20),
        TopicScore("t_synonyms", "sub_english", 16, 20),
        TopicScore("t_sentence", "sub_english", 15, 20),
        // Reasoning -> 67/80 = 83% (strong)
        TopicScore("t_series", "sub_reasoning", 17, 20),
        TopicScore("t_coding", "sub_reasoning", 17, 20),
        TopicScore("t_blood", "sub_reasoning", 16, 20),
        TopicScore("t_direction", "sub_reasoning", 17, 20)
    )

    // =========================================================== past results

    private fun subjectScoresFrom(topicScores: List<TopicScore>): List<SubjectScore> =
        topicScores.groupBy { it.subjectId }.map { (subjectId, list) ->
            SubjectScore(subjectId, list.sumOf { it.correct }, list.sumOf { it.total })
        }

    /**
     * "Daily Exam - 3 days ago" / "రోజువారీ పరీక్ష - 3 రోజుల క్రితం".
     *
     * "Yesterday" is special-cased in both languages because neither one says
     * "1 day ago" naturally.
     */
    private fun pastDailyTitle(daysAgo: Int): LocalizedText = when (daysAgo) {
        1 -> lt("Daily Exam - Yesterday", "రోజువారీ పరీక్ష - నిన్న")
        else -> lt(
            "Daily Exam - $daysAgo days ago",
            "రోజువారీ పరీక్ష - $daysAgo రోజుల క్రితం"
        )
    }

    /**
     * Title is derived from [daysAgo] rather than passed in - see
     * [pastDailyTitle]. One less thing to keep in sync, in two languages.
     */
    private fun pastDaily(
        id: String,
        daysAgo: Int,
        correct: Int,
        total: Int = 20,
        seconds: Int,
        breakdown: List<TopicScore>
    ): ExamResult {
        val wrong = total - correct
        return ExamResult(
            id = id,
            examId = "exam_daily_history",
            examTitle = pastDailyTitle(daysAgo),
            type = ExamType.DAILY,
            totalQuestions = total,
            correct = correct,
            wrong = wrong,
            skipped = 0,
            timeTakenSeconds = seconds,
            takenAt = NOW - daysAgo * DAY,
            subjectScores = subjectScoresFrom(breakdown),
            topicScores = breakdown
        )
    }

    /** Seven past daily exams: 58, 62, 65, 61, 70, 72, 78 percent - the trend chart. */
    val pastResults: List<ExamResult> = listOf(
        pastDaily(
            "res_d1", 7, 12, 20, 1_005,
            listOf(
                TopicScore("t_percentage", "sub_math", 1, 5),
                TopicScore("t_modern", "sub_history", 2, 5),
                TopicScore("t_physics", "sub_science", 5, 5),
                TopicScore("t_rivers", "sub_geography", 3, 3),
                TopicScore("t_frights", "sub_polity", 1, 2)
            )
        ),
        pastDaily(
            "res_d2", 6, 13, 20, 962,
            listOf(
                TopicScore("t_profit_loss", "sub_math", 2, 5),
                TopicScore("t_freedom", "sub_history", 1, 5),
                TopicScore("t_chemistry", "sub_science", 5, 5),
                TopicScore("t_climate", "sub_geography", 3, 3),
                TopicScore("t_parliament", "sub_polity", 2, 2)
            )
        ),
        pastDaily(
            "res_d3", 5, 14, 20, 918,
            listOf(
                TopicScore("t_average", "sub_math", 3, 5),
                TopicScore("t_medieval", "sub_history", 2, 5),
                TopicScore("t_human_body", "sub_science", 5, 5),
                TopicScore("t_physical", "sub_geography", 2, 3),
                TopicScore("t_judiciary", "sub_polity", 2, 2)
            )
        ),
        pastDaily(
            "res_d4", 4, 13, 20, 1_040,
            listOf(
                TopicScore("t_time_work", "sub_math", 2, 5),
                TopicScore("t_modern", "sub_history", 1, 5),
                TopicScore("t_environment", "sub_science", 5, 5),
                TopicScore("t_world_geo", "sub_geography", 3, 3),
                TopicScore("t_panchayat", "sub_polity", 2, 2)
            )
        ),
        pastDaily(
            "res_d5", 3, 15, 20, 880,
            listOf(
                TopicScore("t_ratio", "sub_math", 3, 5),
                TopicScore("t_ancient", "sub_history", 3, 5),
                TopicScore("t_plants_animals", "sub_science", 5, 5),
                TopicScore("t_rivers", "sub_geography", 2, 3),
                TopicScore("t_constitution", "sub_polity", 2, 2)
            )
        ),
        pastDaily(
            "res_d6", 2, 16, 20, 872,
            listOf(
                TopicScore("t_num_system", "sub_math", 4, 5),
                TopicScore("t_freedom", "sub_history", 2, 5),
                TopicScore("t_physics", "sub_science", 5, 5),
                TopicScore("t_climate", "sub_geography", 3, 3),
                TopicScore("t_frights", "sub_polity", 2, 2)
            )
        ),
        pastDaily(
            "res_d7", 1, 16, 20, 872,
            listOf(
                TopicScore("t_percentage", "sub_math", 4, 5),
                TopicScore("t_medieval", "sub_history", 2, 5),
                TopicScore("t_chemistry", "sub_science", 5, 5),
                TopicScore("t_physical", "sub_geography", 3, 3),
                TopicScore("t_parliament", "sub_polity", 2, 2)
            )
        ),
        // ------------------------------------------------- grand test results
        ExamResult(
            id = "res_gt_prev",
            examId = "exam_gt_prev",
            examTitle = lt(
                "Weekly Grand Test - Week 33",
                "వారపు గ్రాండ్ టెస్ట్ - 33వ వారం"
            ),
            type = ExamType.GRAND_TEST,
            totalQuestions = 100,
            correct = 65,
            wrong = 30,
            skipped = 5,
            timeTakenSeconds = 5_120,
            takenAt = NOW - 8 * DAY,
            subjectScores = listOf(
                SubjectScore("sub_math", 5, 12),
                SubjectScore("sub_science", 9, 12),
                SubjectScore("sub_history", 4, 12),
                SubjectScore("sub_geography", 8, 11),
                SubjectScore("sub_polity", 7, 11),
                SubjectScore("sub_economy", 6, 10),
                SubjectScore("sub_ca", 6, 10),
                SubjectScore("sub_gk", 7, 8),
                SubjectScore("sub_english", 7, 8),
                SubjectScore("sub_reasoning", 6, 6)
            ),
            rank = 186,
            participants = 200
        ),
        ExamResult(
            id = "res_gt_last",
            examId = "exam_gt_prev",
            examTitle = lt(
                "Weekly Grand Test - Week 33 (retake)",
                "వారపు గ్రాండ్ టెస్ట్ - 33వ వారం (రీటేక్)"
            ),
            type = ExamType.GRAND_TEST,
            totalQuestions = 100,
            correct = 72,
            wrong = 26,
            skipped = 2,
            timeTakenSeconds = 4_980,
            takenAt = NOW - 1 * DAY - 3 * HOUR,
            subjectScores = listOf(
                SubjectScore("sub_math", 6, 12),
                SubjectScore("sub_science", 10, 12),
                SubjectScore("sub_history", 5, 12),
                SubjectScore("sub_geography", 9, 11),
                SubjectScore("sub_polity", 8, 11),
                SubjectScore("sub_economy", 7, 10),
                SubjectScore("sub_ca", 7, 10),
                SubjectScore("sub_gk", 6, 8),
                SubjectScore("sub_english", 7, 8),
                SubjectScore("sub_reasoning", 7, 6)
            ),
            rank = 128,
            participants = 200
        )
    )

    // ============================================================= leaderboard

    private val firstNames = listOf(
        "Rahul", "Priya", "Arjun", "Sneha", "Karthik", "Divya", "Vikram", "Anusha", "Rohit", "Meghana",
        "Suresh", "Lakshmi", "Naveen", "Swathi", "Ajay", "Bhavana", "Chaitanya", "Deepika", "Eshwar", "Farah",
        "Ganesh", "Harika", "Imran", "Jyothi", "Kalyan", "Latha", "Manoj", "Nandini", "Omkar", "Padma",
        "Praveen", "Ramya", "Sandeep", "Tejaswi", "Uday", "Vaishnavi", "Yashwanth", "Zainab", "Abhinav", "Bindu",
        "Charan", "Dhanush", "Esha", "Gopal", "Hemanth", "Indu", "Jagan", "Keerthi", "Lokesh", "Madhavi",
        "Nikhil", "Ojas", "Pallavi", "Raghav", "Sirisha", "Tarun", "Usha", "Varun", "Yamini", "Zoya"
    )
    private val lastNames = listOf(
        "Reddy", "Rao", "Sharma", "Naidu", "Goud", "Kumar", "Chowdary", "Verma", "Prasad", "Iyer"
    )

    private const val CURRENT_STUDENT_ID = "u_st1"

    /**
     * Deterministic leaderboard of 200 entries per period, with the demo student
     * inserted at a period-specific rank.
     */
    fun leaderboard(period: LeaderboardPeriod, trackId: String? = null): List<LeaderboardEntry> {
        val size = 200
        // Each exam has its own board, so the same student ranks differently in
        // the Constable board and the Bank board.
        val trackShift = trackId?.let { (kotlin.math.abs(it.hashCode()) % 60) - 30 } ?: 0
        val baseRank = when (period) {
            LeaderboardPeriod.DAILY -> 42
            LeaderboardPeriod.WEEKLY -> 128
            LeaderboardPeriod.MONTHLY -> 96
            LeaderboardPeriod.GRAND_TEST -> 128
        }
        val userRank = (baseRank + trackShift).coerceIn(2, size - 1)
        val topPoints = when (period) {
            LeaderboardPeriod.DAILY -> 200
            LeaderboardPeriod.WEEKLY -> 950
            LeaderboardPeriod.MONTHLY -> 3_820
            LeaderboardPeriod.GRAND_TEST -> 100
        }
        val step = when (period) {
            LeaderboardPeriod.DAILY -> 1
            LeaderboardPeriod.WEEKLY -> 2
            LeaderboardPeriod.MONTHLY -> 9
            LeaderboardPeriod.GRAND_TEST -> 1
        }
        val tests = when (period) {
            LeaderboardPeriod.DAILY -> 1
            LeaderboardPeriod.WEEKLY -> 7
            LeaderboardPeriod.MONTHLY -> 28
            LeaderboardPeriod.GRAND_TEST -> 1
        }

        return (1..size).map { rank ->
            val isUser = rank == userRank
            val nameIndex = (rank * 7) % firstNames.size
            val surnameIndex = (rank * 3) % lastNames.size
            val points = (topPoints - (rank - 1) * step).coerceAtLeast(10)
            LeaderboardEntry(
                rank = rank,
                studentId = if (isUser) CURRENT_STUDENT_ID else "u_lb_$rank",
                name = if (isUser) "You" else firstNames[nameIndex] + " " + lastNames[surnameIndex],
                points = points,
                testsCompleted = if (period == LeaderboardPeriod.MONTHLY) tests - (rank % 5) else tests,
                accuracy = (98 - rank / 4).coerceIn(35, 98),
                isCurrentUser = isUser
            )
        }
    }

    fun currentRank(period: LeaderboardPeriod, trackId: String? = null): Int =
        leaderboard(period, trackId).first { it.isCurrentUser }.rank

    const val PARTICIPANTS = 200

    // =========================================================== notifications

    val notifications: List<StudyNotification> = listOf(
        StudyNotification(
            "sn1",
            lt("Today's Daily Exam is live", "ఈ రోజు రోజువారీ పరీక్ష లైవ్‌లో ఉంది"),
            lt("20 questions, 20 minutes. Mixed subjects.", "20 ప్రశ్నలు, 20 నిమిషాలు. మిశ్రమ సబ్జెక్టులు."),
            NOW - 2 * HOUR, StudyNotificationType.DAILY_EXAM, false, StudyRole.STUDENT
        ),
        StudyNotification(
            "sn2",
            lt("Weekly Grand Test this Sunday", "ఈ ఆదివారం వారపు గ్రాండ్ టెస్ట్"),
            lt("100 questions in 90 minutes across all 10 subjects.", "10 సబ్జెక్టుల నుంచి 90 నిమిషాల్లో 100 ప్రశ్నలు."),
            NOW - 5 * HOUR, StudyNotificationType.GRAND_TEST, false, StudyRole.STUDENT
        ),
        StudyNotification(
            "sn3",
            lt("New study article", "కొత్త స్టడీ ఆర్టికల్"),
            lt("A new article on Percentages with shortcuts is available.", "శాతాలు - షార్ట్‌కట్‌లతో కొత్త ఆర్టికల్ అందుబాటులో ఉంది."),
            NOW - 8 * HOUR, StudyNotificationType.NEW_ARTICLE, false, StudyRole.STUDENT
        ),
        StudyNotification(
            "sn4",
            lt("Grand Test result published", "గ్రాండ్ టెస్ట్ ఫలితం విడుదలైంది"),
            lt("You scored 72/100. Rank #128 of 200.", "మీ స్కోరు 72/100. 200 మందిలో ర్యాంక్ #128."),
            NOW - 1 * DAY, StudyNotificationType.RESULT, true, StudyRole.STUDENT
        ),
        StudyNotification(
            "sn5",
            lt("Your rank improved", "మీ ర్యాంక్ మెరుగుపడింది"),
            lt("Weekly rank moved from #186 to #128. Keep going!", "వారపు ర్యాంక్ #186 నుంచి #128కి చేరింది. కొనసాగించండి!"),
            NOW - 1 * DAY + HOUR, StudyNotificationType.RANK, false, StudyRole.STUDENT
        ),
        StudyNotification(
            "sn6",
            lt("Weak area detected", "బలహీన ప్రాంతం గుర్తించబడింది"),
            lt("Mathematics accuracy is 40%. Start with Percentages and Profit & Loss.", "గణితంలో కచ్చితత్వం 40%. శాతాలు, లాభం-నష్టంతో మొదలుపెట్టండి."),
            NOW - 26 * HOUR, StudyNotificationType.RECOMMENDATION, false, StudyRole.STUDENT
        ),
        StudyNotification(
            "sn7",
            lt("Article awaiting review", "సమీక్ష కోసం ఎదురుచూస్తున్న ఆర్టికల్"),
            lt("Government Schemes Revision was submitted for review.", "ప్రభుత్వ పథకాల రివిజన్ సమీక్షకు సమర్పించబడింది."),
            NOW - 3 * HOUR, StudyNotificationType.SYSTEM, false, StudyRole.STUDY_ADMIN
        ),
        StudyNotification(
            "sn8",
            lt("Tomorrow's exam is inactive", "రేపటి పరీక్ష యాక్టివ్‌గా లేదు"),
            lt("Daily Exam - Tomorrow has not been activated yet.", "రోజువారీ పరీక్ష - రేపు ఇంకా యాక్టివేట్ చేయలేదు."),
            NOW - 4 * HOUR, StudyNotificationType.SYSTEM, false, StudyRole.EXAM_ADMIN
        ),
        StudyNotification(
            "sn9",
            lt("Demo build", "డెమో బిల్డ్"),
            lt("All Module 2 data is local mock data. No backend involved.", "మాడ్యూల్ 2 డేటా మొత్తం లోకల్ మాక్ డేటా. ఎలాంటి బ్యాకెండ్ లేదు."),
            NOW - 3 * DAY, StudyNotificationType.SYSTEM, true, null
        )
    )

    // ======================================================== role permissions

    val rolePermissions: List<RolePermissionRow> = listOf(
        RolePermissionRow(StudyRole.STUDENT, listOf("Study articles", "Topic quizzes", "Daily exams", "Grand tests", "Leaderboard", "Performance analysis")),
        RolePermissionRow(StudyRole.CONTENT_CREATOR, listOf("Create articles", "Create quizzes", "Add questions", "Save drafts", "Submit for review")),
        RolePermissionRow(StudyRole.EXAM_ADMIN, listOf("Create daily exams", "Create grand tests", "Activate / deactivate exams", "View exam results", "Use the question bank")),
        RolePermissionRow(StudyRole.STUDY_ADMIN, listOf("Manage subjects", "Manage topics", "Manage study content", "Manage question bank", "Publish content")),
        RolePermissionRow(StudyRole.SUPER_ADMIN, listOf("Full access", "Manage all users", "Change roles", "All content and exams", "System settings"))
    )

    // -------------------------------------------------- initially studied topics
    val completedTopicIds: Set<String> = setOf(
        "t_num_system", "t_average", "t_physics", "t_chemistry", "t_human_body",
        "t_rivers", "t_climate", "t_ancient", "t_panchayat", "t_series", "t_coding", "t_days"
    )

    /** Answers seeded for the most recent grand test so "Review Answers" has data. */
    val seededReviewAnswers: List<AnsweredQuestion> = questions.take(20).mapIndexed { index, question ->
        AnsweredQuestion(
            questionId = question.id,
            selectedIndex = when {
                index % 5 == 3 -> null
                index % 3 == 0 -> (question.correctIndex + 1) % question.options.size
                else -> question.correctIndex
            },
            correctIndex = question.correctIndex,
            markedForReview = index % 7 == 0
        )
    }
}
