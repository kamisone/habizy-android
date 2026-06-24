package com.habizy.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habizy.app.ui.theme.DarkText
import com.habizy.app.ui.theme.FredokaFamily
import com.habizy.app.ui.theme.toComposeColor

/**
 * Top bar with a back arrow and a title, matching the iOS TopBarWithBack pattern.
 */
@Composable
fun TopBarWithBack(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Retour",
                tint = DarkText,
            )
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = title,
            fontFamily = FredokaFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 20.sp,
            color = DarkText,
        )
    }
}

/**
 * Colored rounded box with a single initial letter, mirroring the iOS RoommateAvatar.
 */
@Composable
fun RoommateAvatar(
    colorHex: String,
    initial: String,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    cornerRadius: Dp = 15.dp,
    fontSize: TextUnit = 17.sp,
) {
    val color = try {
        colorHex.toComposeColor()
    } catch (_: Exception) {
        Color(0xFF888888)
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(cornerRadius))
            .background(color.copy(alpha = 0.22f)),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initial,
            fontFamily = FredokaFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = fontSize,
            color = color,
        )
    }
}
