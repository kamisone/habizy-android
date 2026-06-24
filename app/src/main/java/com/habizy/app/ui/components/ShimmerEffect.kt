package com.habizy.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.habizy.app.ui.theme.BorderColor

/**
 * Modifier that overlays a shimmer animation, matching the iOS ShimmerModifier.
 */
fun Modifier.shimmerEffect(): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateX by transition.animateFloat(
        initialValue = -300f,
        targetValue = 600f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmerTranslateX",
    )

    val shimmerBrush = Brush.linearGradient(
        colors = listOf(
            Color.Transparent,
            Color.White.copy(alpha = 0.4f),
            Color.Transparent,
        ),
        start = Offset(translateX, 0f),
        end = Offset(translateX + 300f, 0f),
    )

    this.background(shimmerBrush)
}

/**
 * A rectangular shimmer placeholder box, mirroring iOS ShimmerBox.
 */
@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    width: Dp? = null,
    height: Dp = 16.dp,
    cornerRadius: Dp = 8.dp,
) {
    val baseModifier = modifier
        .then(if (width != null) Modifier.width(width) else Modifier.fillMaxWidth())
        .height(height)
        .clip(RoundedCornerShape(cornerRadius))
        .background(BorderColor.copy(alpha = 0.3f))
        .shimmerEffect()

    Box(modifier = baseModifier)
}

// -- Screen-level shimmer loading composables matching iOS --

@Composable
fun ShimmerHomeLoading(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .padding(horizontal = 18.dp)
            .padding(vertical = 8.dp),
    ) {
        // Title area
        Column {
            ShimmerBox(width = 80.dp, height = 14.dp)
            Spacer(Modifier.height(6.dp))
            ShimmerBox(width = 140.dp, height = 22.dp, cornerRadius = 10.dp)
        }
        Spacer(Modifier.height(14.dp))
        ShimmerBox(height = 140.dp, cornerRadius = 28.dp)
        Spacer(Modifier.height(14.dp))
        Row {
            ShimmerBox(modifier = Modifier.weight(1f), height = 80.dp, cornerRadius = 20.dp)
            Spacer(Modifier.width(12.dp))
            ShimmerBox(modifier = Modifier.weight(1f), height = 80.dp, cornerRadius = 20.dp)
        }
        Spacer(Modifier.height(14.dp))
        ShimmerBox(height = 90.dp, cornerRadius = 26.dp)
        Spacer(Modifier.height(14.dp))
        Row {
            ShimmerBox(modifier = Modifier.weight(1f), height = 64.dp, cornerRadius = 18.dp)
            Spacer(Modifier.width(12.dp))
            ShimmerBox(modifier = Modifier.weight(1f), height = 64.dp, cornerRadius = 18.dp)
        }
    }
}

@Composable
fun ShimmerReportsLoading(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(horizontal = 18.dp),
    ) {
        ShimmerBox(height = 48.dp, cornerRadius = 16.dp)
        Spacer(Modifier.height(14.dp))
        Row {
            repeat(4) {
                ShimmerBox(width = 60.dp, height = 30.dp, cornerRadius = 20.dp)
                Spacer(Modifier.width(8.dp))
            }
        }
        Spacer(Modifier.height(14.dp))
        repeat(4) {
            Row(modifier = Modifier.padding(vertical = 12.dp)) {
                ShimmerBox(width = 52.dp, height = 52.dp, cornerRadius = 12.dp)
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    ShimmerBox(width = 100.dp, height = 12.dp)
                    Spacer(Modifier.height(6.dp))
                    ShimmerBox(height = 14.dp, cornerRadius = 6.dp)
                    Spacer(Modifier.height(6.dp))
                    ShimmerBox(width = 80.dp, height = 11.dp)
                }
            }
        }
    }
}

@Composable
fun ShimmerExpensesLoading(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .padding(horizontal = 18.dp)
            .padding(vertical = 8.dp),
    ) {
        ShimmerBox(width = 120.dp, height = 22.dp, cornerRadius = 10.dp)
        Spacer(Modifier.height(14.dp))
        ShimmerBox(height = 100.dp, cornerRadius = 22.dp)
        Spacer(Modifier.height(14.dp))
        ShimmerBox(height = 160.dp, cornerRadius = 22.dp)
        Spacer(Modifier.height(14.dp))
        repeat(5) {
            Row(modifier = Modifier.padding(vertical = 10.dp)) {
                ShimmerBox(width = 40.dp, height = 40.dp, cornerRadius = 13.dp)
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    ShimmerBox(width = 100.dp, height = 14.dp)
                    Spacer(Modifier.height(4.dp))
                    ShimmerBox(width = 60.dp, height = 11.dp)
                }
                Spacer(Modifier.width(12.dp))
                ShimmerBox(width = 50.dp, height = 14.dp)
            }
        }
    }
}

@Composable
fun ShimmerProfileLoading(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .padding(horizontal = 18.dp)
            .padding(vertical = 8.dp),
    ) {
        Row(modifier = Modifier.padding(top = 12.dp)) {
            ShimmerBox(width = 80.dp, height = 80.dp, cornerRadius = 26.dp)
            Spacer(Modifier.width(14.dp))
            Column {
                ShimmerBox(width = 120.dp, height = 20.dp, cornerRadius = 10.dp)
                Spacer(Modifier.height(6.dp))
                ShimmerBox(width = 80.dp, height = 14.dp)
            }
        }
        Spacer(Modifier.height(14.dp))
        Row {
            ShimmerBox(modifier = Modifier.weight(1f), height = 80.dp, cornerRadius = 20.dp)
            Spacer(Modifier.width(12.dp))
            ShimmerBox(modifier = Modifier.weight(1f), height = 80.dp, cornerRadius = 20.dp)
        }
        Spacer(Modifier.height(14.dp))
        ShimmerBox(height = 180.dp, cornerRadius = 22.dp)
        Spacer(Modifier.height(14.dp))
        ShimmerBox(height = 120.dp, cornerRadius = 22.dp)
    }
}
