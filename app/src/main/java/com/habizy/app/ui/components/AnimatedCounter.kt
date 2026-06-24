package com.habizy.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.habizy.app.ui.theme.FredokaFamily
import java.util.Locale

/**
 * Animated number display with euro formatting, mirroring the iOS CountingText.
 *
 * The value animates from 0 (or previous) to [targetValue] with an ease-out curve.
 */
@Composable
fun AnimatedCounter(
    targetValue: Double,
    modifier: Modifier = Modifier,
    durationMillis: Int = 800,
    style: TextStyle = TextStyle(
        fontFamily = FredokaFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
    ),
    color: Color = Color.White,
) {
    val animatable = remember { Animatable(0f) }

    LaunchedEffect(targetValue) {
        animatable.animateTo(
            targetValue = targetValue.toFloat(),
            animationSpec = tween(
                durationMillis = durationMillis,
                easing = FastOutSlowInEasing,
            ),
        )
    }

    Text(
        text = animatable.value.toDouble().euroFormatted(),
        style = style,
        color = color,
        modifier = modifier,
    )
}

/**
 * Format a Double as a euro amount: "12,50 €"
 */
fun Double.euroFormatted(): String {
    val formatted = String.format(Locale.FRANCE, "%.2f", this).replace('.', ',')
    return "$formatted €"
}
