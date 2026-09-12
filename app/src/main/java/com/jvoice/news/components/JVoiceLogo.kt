package com.jvoice.news.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jvoice.news.R

/**
 * The J Voice brand marks — the supplied logo artwork, not a rebuild of it.
 *
 * The artwork ships in two crops, both generated from the one source file by
 * `brand/`'s asset pipeline, so there is a single origin for the brand:
 *
 *  * [JVoiceLogo]   the full lockup: emblem, "J VOICE" wordmark and the
 *                   NEWS · PEOPLE · TRUTH strip. For hero surfaces.
 *  * [JVoiceMark]   the emblem alone — the J and the globe. For compact spots
 *                   where the wordmark would be too small to read anyway.
 *
 * Both are transparent PNGs. The source logo sits on solid black, which would
 * have shown as a black box on every light surface, so the background was keyed
 * out before import.
 *
 * ### Why the logo is sized by width
 *
 * The lockup is wider than it is tall and its aspect ratio is fixed by the
 * artwork. Sizing by height — which the earlier hand-drawn wordmark did — means
 * the caller cannot predict how much horizontal room the mark will take, and it
 * silently overflows narrow containers. Width is the dimension that actually
 * constrains it.
 */

/** Intrinsic aspect ratio of the full lockup, from the generated asset. */
private const val LOGO_ASPECT = 1024f / 874f

/** Intrinsic aspect ratio of the emblem crop. */
private const val EMBLEM_ASPECT = 512f / 473f

/**
 * The full lockup at a given [width]. Height follows from the artwork.
 *
 * Best on a dark surface: the logo is designed on near-black and its wordmark
 * carries a scarlet block that loses contrast against a red or light ground.
 */
@Composable
fun JVoiceLogo(
    modifier: Modifier = Modifier,
    width: Dp = 240.dp
) {
    Image(
        painter = painterResource(R.drawable.jvoice_logo),
        // The logo is the product name, so the name is the description. Repeating
        // the tagline here would make a screen reader read the brand twice.
        contentDescription = "J Voice",
        contentScale = ContentScale.Fit,
        modifier = modifier.width(width)
    )
}

/**
 * The emblem alone — the J and the globe — at a given [size].
 *
 * For drawer headers, list rows and avatars. Deliberately the tighter crop: the
 * microphone, the signal waves and the small mic-flag are all in the full
 * emblem, and below roughly 48dp those five elements stop being distinguishable
 * and read as coloured noise.
 */
@Composable
fun JVoiceMark(
    modifier: Modifier = Modifier,
    size: Dp = 52.dp
) {
    Box(modifier.size(size), contentAlignment = Alignment.Center) {
        Image(
            painter = painterResource(R.drawable.jvoice_emblem),
            contentDescription = "J Voice",
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(size)
        )
    }
}
