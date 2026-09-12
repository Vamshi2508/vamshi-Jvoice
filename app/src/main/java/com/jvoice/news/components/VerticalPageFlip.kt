package com.jvoice.news.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.abs
import kotlinx.coroutines.launch
import com.jvoice.core.i18n.current

/**
 * Vertical page turn where **only the lower panel flips**.
 *
 * The image on top stays flat and simply crossfades to the next article, while the
 * story below is a hinged sheet: it is pinned along its top edge (the seam) and
 * rotates up through 180 degrees. Past halfway the sheet is showing its reverse, so
 * it swaps to the next story and corrects the rotation by 180 degrees - the next
 * article arrives on the back of the page you are turning, with a fold shadow that
 * peaks when the sheet is edge-on.
 *
 * Drag up to turn forward, drag down to come back. The turn follows the finger and
 * settles to the nearer side on release.
 */
@Composable
fun VerticalTwoPanelFlip(
    count: Int,
    modifier: Modifier = Modifier,
    topFraction: Float = 0.46f,
    /** Changing this rewinds to the first page - e.g. a new deck. */
    resetKey: Any? = null,
    onPageSettled: (Int) -> Unit = {},
    topPanel: @Composable (index: Int) -> Unit,
    bottomPanel: @Composable (index: Int) -> Unit
) {
    if (count <= 0) return

    var current by rememberSaveable { mutableIntStateOf(0) }
    if (current > count - 1) current = count - 1

    // -1f .. 1f. Positive turns the current page away, negative brings the previous
    // page back down.
    val progress = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(resetKey) {
        current = 0
        progress.snapTo(0f)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(count, current) {
                detectVerticalDragGestures(
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        val delta = -dragAmount / size.height
                        val lower = if (current > 0) -1f else 0f
                        val upper = if (current < count - 1) 1f else 0f
                        scope.launch {
                            progress.snapTo((progress.value + delta).coerceIn(lower, upper))
                        }
                    },
                    onDragEnd = {
                        scope.launch {
                            val spec = tween<Float>(durationMillis = 440, easing = FastOutSlowInEasing)
                            when {
                                progress.value > 0.3f && current < count - 1 -> {
                                    progress.animateTo(1f, spec)
                                    current += 1
                                    progress.snapTo(0f)
                                    onPageSettled(current)
                                }
                                progress.value < -0.3f && current > 0 -> {
                                    progress.animateTo(-1f, spec)
                                    current -= 1
                                    progress.snapTo(0f)
                                    onPageSettled(current)
                                }
                                else -> progress.animateTo(0f, spec)
                            }
                        }
                    },
                    onDragCancel = {
                        scope.launch { progress.animateTo(0f, tween(260)) }
                    }
                )
            }
    ) {
        val p = progress.value
        val forward = p >= 0f
        val travel = abs(p)

        // Where the turn is heading. Null when there is nowhere to go.
        val targetIndex = when {
            forward && current < count - 1 -> current + 1
            !forward && current > 0 -> current - 1
            else -> null
        }

        // The sheet's own rotation: 0 flat, 1 fully turned.
        val leafTurn = if (forward) p else 1f - travel
        val leafFront = if (forward) current else (current - 1).coerceAtLeast(0)
        val leafBack = if (forward) targetIndex else current
        // What sits under the turning sheet.
        val storyBeneath = if (forward) (targetIndex ?: current) else current

        Column(Modifier.fillMaxSize()) {

            // ---------------------------------------------------- image: no flip
            // Stays flat and crossfades towards the article being turned to.
            Box(
                Modifier
                    .fillMaxWidth()
                    .weight(topFraction)
            ) {
                if (targetIndex != null) {
                    topPanel(targetIndex)
                }
                Box(Modifier.fillMaxSize().alpha(1f - travel)) {
                    topPanel(current)
                }
            }

            // ------------------------------------------------- story: the flip
            Box(
                Modifier
                    .fillMaxWidth()
                    .weight(1f - topFraction)
            ) {
                bottomPanel(storyBeneath)
                TurningLeaf(
                    turn = leafTurn,
                    frontIndex = leafFront,
                    backIndex = leafBack,
                    content = bottomPanel
                )
            }
        }
    }
}

/** The hinged sheet: shows its reverse past the halfway point. */
@Composable
private fun TurningLeaf(
    turn: Float,
    frontIndex: Int,
    backIndex: Int?,
    content: @Composable (index: Int) -> Unit
) {
    if (frontIndex < 0) return

    val angle = -180f * turn.coerceIn(0f, 1f)
    val showingBack = angle <= -90f
    val visibleIndex = if (showingBack) backIndex else frontIndex
    if (visibleIndex == null || visibleIndex < 0) return

    // 0 flat, 1 edge-on - drives the fold shadow.
    val fold = 1f - abs(abs(angle) - 90f) / 90f

    Box(
        Modifier
            .fillMaxSize()
            .graphicsLayer {
                cameraDistance = 26f * density
                transformOrigin = TransformOrigin(0.5f, 0f)
                rotationX = if (showingBack) angle + 180f else angle
            }
    ) {
        content(visibleIndex)

        if (fold > 0.01f) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0f to Color.Black.copy(alpha = 0.50f * fold),
                            0.6f to Color.Black.copy(alpha = 0.20f * fold),
                            1f to Color.Black.copy(alpha = 0.04f * fold)
                        )
                    )
            )
        }
    }
}
