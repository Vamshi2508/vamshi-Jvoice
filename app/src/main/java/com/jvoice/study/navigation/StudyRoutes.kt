package com.jvoice.study.navigation

object StudyRoutes {

    // ---- student tabs
    /** Exam picker or the selected exam's hub, depending on what is chosen. */
    const val HOME = "study/home"
    const val TRACKS = "study/tracks"
    const val DASHBOARD = "study/dashboard"
    const val BROWSE = "study/browse"
    const val EXAMS = "study/exams"
    const val LEADERBOARD = "study/leaderboard"
    const val PROFILE = "study/profile"

    // ---- student flows
    const val SUBJECT_TOPICS = "study/subject/{subjectId}"
    const val ARTICLE = "study/article/{topicId}"
    const val QUIZ_RUNNER = "study/quiz/{topicId}"
    const val EXAM_RUNNER = "study/exam/{examId}"
    const val RESULT = "study/result/{resultId}"
    const val REVIEW = "study/review/{resultId}"
    const val ANALYSIS = "study/analysis"
    const val SUBJECT_ANALYSIS = "study/analysis/{subjectId}"
    const val WEAK_AREAS = "study/weak-areas"
    const val WEAK_TOPIC = "study/weak-topic/{topicId}"
    const val PRACTICE_RUNNER = "study/practice/{topicId}"
    const val PERFORMANCE = "study/performance"
    const val NOTIFICATIONS = "study/notifications"
    const val KEY_DATES = "study/key-dates"
    const val PAST_PAPERS = "study/past-papers"

    fun subjectTopics(subjectId: String) = "study/subject/$subjectId"
    fun article(topicId: String) = "study/article/$topicId"
    fun quizRunner(topicId: String) = "study/quiz/$topicId"
    fun examRunner(examId: String) = "study/exam/$examId"
    fun result(resultId: String) = "study/result/$resultId"
    fun review(resultId: String) = "study/review/$resultId"
    fun subjectAnalysis(subjectId: String) = "study/analysis/$subjectId"
    fun weakTopic(topicId: String) = "study/weak-topic/$topicId"
    fun practiceRunner(topicId: String) = "study/practice/$topicId"

    // ---- content creator
    const val CREATOR_DASHBOARD = "creator/dashboard"
    const val CREATOR_ARTICLES = "creator/articles"
    const val CREATOR_QUESTIONS = "creator/questions"
    const val CREATOR_ARTICLE_EDITOR = "creator/article-editor?articleId={articleId}"
    const val CREATOR_QUESTION_EDITOR = "creator/question-editor?questionId={questionId}"

    fun creatorArticleNew() = "creator/article-editor?articleId="
    fun creatorArticleEdit(id: String) = "creator/article-editor?articleId=$id"
    fun creatorQuestionNew() = "creator/question-editor?questionId="
    fun creatorQuestionEdit(id: String) = "creator/question-editor?questionId=$id"

    // ---- exam admin
    const val EXAM_ADMIN_DASHBOARD = "examadmin/dashboard"
    const val EXAM_ADMIN_DAILY = "examadmin/daily"
    const val EXAM_ADMIN_GRAND = "examadmin/grand"
    const val EXAM_ADMIN_BANK = "examadmin/bank"
    const val EXAM_ADMIN_RESULTS = "examadmin/results"
    const val EXAM_ADMIN_EDITOR = "examadmin/editor?examId={examId}&type={type}"

    fun examEditorNew(type: String) = "examadmin/editor?examId=&type=$type"
    fun examEditorEdit(examId: String, type: String) = "examadmin/editor?examId=$examId&type=$type"

    // ---- study admin
    const val STUDY_ADMIN_DASHBOARD = "studyadmin/dashboard"
    const val STUDY_ADMIN_EXAM_TYPES = "studyadmin/exam-types"
    const val STUDY_ADMIN_TRACK_EDITOR = "studyadmin/exam-type-editor?trackId={trackId}"
    const val STUDY_ADMIN_SUBJECTS = "studyadmin/subjects"
    const val STUDY_ADMIN_TOPICS = "studyadmin/topics"
    const val STUDY_ADMIN_CONTENT = "studyadmin/content"
    const val STUDY_ADMIN_BANK = "studyadmin/bank"

    // ---- super admin
    const val SUPER_DASHBOARD = "studysuper/dashboard"
    const val SUPER_USERS = "studysuper/users"
    const val SUPER_ROLES = "studysuper/roles"

    fun trackEditorNew() = "studyadmin/exam-type-editor?trackId="
    fun trackEditorEdit(id: String) = "studyadmin/exam-type-editor?trackId=$id"

    // ---- args
    const val ARG_SUBJECT_ID = "subjectId"
    const val ARG_TOPIC_ID = "topicId"
    const val ARG_EXAM_ID = "examId"
    const val ARG_RESULT_ID = "resultId"
    const val ARG_ARTICLE_ID = "articleId"
    const val ARG_QUESTION_ID = "questionId"
    const val ARG_TYPE = "type"
    const val ARG_TRACK_ID = "trackId"
}
