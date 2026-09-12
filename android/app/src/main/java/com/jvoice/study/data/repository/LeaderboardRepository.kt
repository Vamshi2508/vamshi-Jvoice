package com.jvoice.study.data.repository

import com.jvoice.study.data.mock.MockDataSource
import com.jvoice.study.data.model.LeaderboardEntry
import com.jvoice.study.data.model.LeaderboardPeriod

/**
 * Leaderboard data for the four periods. Entries are generated deterministically
 * from the mock data source, with the demo student pinned at a period-specific
 * rank so the "Your rank" row always has something to show.
 */
object LeaderboardRepository {

    private val cache = mutableMapOf<String, List<LeaderboardEntry>>()

    /**
     * Who is sitting the current exam. Captured on the entry screen purely so a
     * score has a name and a number beside it on the rank list — nothing else
     * reads these.
     */
    var candidateName: String = ""
        private set
    var candidateMobile: String = ""
        private set

    fun setCandidate(name: String, mobile: String) {
        candidateName = name
        candidateMobile = mobile
    }

    /**
     * Every exam has its own board. The selected track is read here rather than
     * threaded through every caller, so a rank always belongs to one exam.
     */
    fun entries(period: LeaderboardPeriod): List<LeaderboardEntry> {
        val trackId = ExamTrackRepository.selectedId.value
        return cache.getOrPut(period.name + "|" + trackId) {
            MockDataSource.leaderboard(period, trackId)
        }
    }

    fun topEntries(period: LeaderboardPeriod, count: Int = 20): List<LeaderboardEntry> =
        entries(period).take(count)

    fun currentUserEntry(period: LeaderboardPeriod): LeaderboardEntry? =
        entries(period).firstOrNull { it.isCurrentUser }

    /** The student's row plus two neighbours on each side, as real apps show it. */
    fun neighbourhood(period: LeaderboardPeriod, radius: Int = 2): List<LeaderboardEntry> {
        val all = entries(period)
        val index = all.indexOfFirst { it.isCurrentUser }
        if (index == -1) return emptyList()
        val from = (index - radius).coerceAtLeast(0)
        val to = (index + radius).coerceAtMost(all.lastIndex)
        return all.subList(from, to + 1)
    }

    fun participants(period: LeaderboardPeriod): Int = entries(period).size

    fun rank(period: LeaderboardPeriod): Int = currentUserEntry(period)?.rank ?: 0

    fun points(period: LeaderboardPeriod): Int = currentUserEntry(period)?.points ?: 0

    val defaultParticipants: Int = MockDataSource.PARTICIPANTS
}
