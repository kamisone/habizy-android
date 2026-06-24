package com.habizy.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habizy.app.ui.theme.Blue
import com.habizy.app.ui.theme.CardBackground
import com.habizy.app.ui.theme.CoralRed
import com.habizy.app.ui.theme.DarkText
import com.habizy.app.ui.theme.DmSansFamily
import com.habizy.app.ui.theme.GreenPrimary

/**
 * Provides a [SnackbarHostState] through composition to allow any child to show snackbars.
 */
val LocalSnackbarHost = compositionLocalOf<SnackbarHostState> {
    error("No SnackbarHostState provided")
}

/**
 * Snackbar visual type: ERROR, SUCCESS, or INFO.
 */
enum class SnackbarType {
    ERROR,
    SUCCESS,
    INFO;

    val color: Color
        get() = when (this) {
            ERROR -> CoralRed
            SUCCESS -> GreenPrimary
            INFO -> Blue
        }

    val icon: ImageVector
        get() = when (this) {
            ERROR -> Icons.Default.Error
            SUCCESS -> Icons.Default.CheckCircle
            INFO -> Icons.Default.Info
        }
}

/**
 * Custom visuals carrying the [SnackbarType] alongside the message.
 */
data class AppSnackbarVisuals(
    override val message: String,
    val type: SnackbarType = SnackbarType.ERROR,
    override val actionLabel: String? = null,
    override val withDismissAction: Boolean = false,
    override val duration: SnackbarDuration = SnackbarDuration.Short,
) : SnackbarVisuals

/**
 * Extension to show a typed snackbar easily.
 */
suspend fun SnackbarHostState.showTyped(
    message: String,
    type: SnackbarType = SnackbarType.ERROR,
) {
    showSnackbar(AppSnackbarVisuals(message = message, type = type))
}

/**
 * Custom snackbar host positioned at the top of the screen with styled rendering.
 */
@Composable
fun AppSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        SnackbarHost(
            hostState = hostState,
            modifier = Modifier.padding(top = 48.dp),
        ) { data ->
            val visuals = data.visuals as? AppSnackbarVisuals
            val type = visuals?.type ?: SnackbarType.ERROR

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(16.dp),
                color = CardBackground,
                shadowElevation = 12.dp,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = type.icon,
                        contentDescription = null,
                        tint = type.color,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = data.visuals.message,
                        fontFamily = DmSansFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = DarkText,
                    )
                }
            }
        }
    }
}
