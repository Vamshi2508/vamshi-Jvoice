package com.jvoice.news.data.repository

import com.jvoice.news.data.model.ArticleComment
import com.jvoice.news.data.model.ArticleEngagement
import com.jvoice.news.data.model.ArticleReport
import com.jvoice.news.data.model.Reaction
import com.jvoice.news.data.model.ReportReason
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Likes, dislikes and comments on news articles.
 *
 * A separate singleton so [NewsRepository] is untouched. Same in-memory pattern:
 * everything resets with the process, and a real backend can replace the body of
 * these methods without the UI noticing.
 */
object EngagementRepository {

    // Starts empty: likes and view counts accumulate from real reading. The type
    // is spelled out because an empty literal gives the compiler nothing to infer
    // the map's parameters from.
    private val _engagement = MutableStateFlow<Map<String, ArticleEngagement>>(emptyMap())
    val engagement: StateFlow<Map<String, ArticleEngagement>> = _engagement.asStateFlow()

    private val _comments = MutableStateFlow<List<ArticleComment>>(emptyList())
    val comments: StateFlow<List<ArticleComment>> = _comments.asStateFlow()

    private var idCounter = 1_000

    fun engagementFor(articleId: String): ArticleEngagement =
        _engagement.value[articleId] ?: ArticleEngagement(articleId)

    fun commentsFor(articleId: String): List<ArticleComment> =
        _comments.value.filter { it.articleId == articleId }.sortedByDescending { it.timeMillis }

    fun commentCount(articleId: String): Int = _comments.value.count { it.articleId == articleId }

    /**
     * Like is exclusive with dislike, and tapping the active one clears it -
     * the counts move accordingly.
     */
    fun toggleLike(articleId: String) = react(articleId, Reaction.LIKE)

    fun toggleDislike(articleId: String) = react(articleId, Reaction.DISLIKE)

    private fun react(articleId: String, wanted: Reaction) {
        _engagement.update { map ->
            val current = map[articleId] ?: ArticleEngagement(articleId)
            val next = if (current.myReaction == wanted) Reaction.NONE else wanted

            var likes = current.likes
            var dislikes = current.dislikes

            // remove the old vote
            when (current.myReaction) {
                Reaction.LIKE -> likes -= 1
                Reaction.DISLIKE -> dislikes -= 1
                Reaction.NONE -> Unit
            }
            // apply the new one
            when (next) {
                Reaction.LIKE -> likes += 1
                Reaction.DISLIKE -> dislikes += 1
                Reaction.NONE -> Unit
            }

            map + (articleId to current.copy(
                likes = likes.coerceAtLeast(0),
                dislikes = dislikes.coerceAtLeast(0),
                myReaction = next
            ))
        }
    }

    fun addComment(articleId: String, authorName: String, text: String): Boolean {
        val body = text.trim()
        if (body.isBlank()) return false
        idCounter += 1
        val comment = ArticleComment(
            id = "cm_new_$idCounter",
            articleId = articleId,
            authorName = authorName,
            text = body,
            timeMillis = System.currentTimeMillis(),
            likes = 0,
            isOwn = true
        )
        _comments.update { listOf(comment) + it }
        return true
    }

    // ------------------------------------------------------------------ reports
    private val _reports = MutableStateFlow(emptyList<ArticleReport>())
    val reports: StateFlow<List<ArticleReport>> = _reports.asStateFlow()

    /**
     * Files a reader report. The article's own report counter is bumped through
     * [NewsRepository] too, so the News Admin moderation view picks it up.
     */
    fun submitReport(
        articleId: String,
        reason: ReportReason,
        suggestion: String,
        reportedBy: String
    ) {
        idCounter += 1
        val report = ArticleReport(
            id = "rp_$idCounter",
            articleId = articleId,
            reason = reason,
            suggestion = suggestion.trim(),
            reportedBy = reportedBy,
            timeMillis = System.currentTimeMillis()
        )
        _reports.update { listOf(report) + it }
        NewsRepository.reportArticle(articleId)
    }

    fun reportsFor(articleId: String): List<ArticleReport> =
        _reports.value.filter { it.articleId == articleId }

    fun likeComment(commentId: String) {
        _comments.update { list ->
            list.map { if (it.id == commentId) it.copy(likes = it.likes + 1) else it }
        }
    }

    fun deleteComment(commentId: String) {
        _comments.update { list -> list.filterNot { it.id == commentId } }
    }
}
