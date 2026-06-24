package com.habizy.app.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.habizy.app.data.model.ShoppingItemResponse
import com.habizy.app.ui.components.TopBarWithBack
import com.habizy.app.ui.theme.CardBackground
import com.habizy.app.ui.theme.DarkText
import com.habizy.app.ui.theme.DmSansFamily
import com.habizy.app.ui.theme.FredokaFamily
import com.habizy.app.ui.theme.GreenDark
import com.habizy.app.ui.theme.GreenPrimary
import com.habizy.app.ui.theme.LightText
import com.habizy.app.ui.theme.ScreenBackground
import com.habizy.app.ui.theme.SubtitleText
import com.habizy.app.ui.viewmodel.ShoppingViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun DecideForMeScreen(
    onBack: () -> Unit,
    viewModel: ShoppingViewModel = viewModel(),
) {
    val items by viewModel.items.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    val uncheckedItems = items.filter { !it.isChecked }

    var isSpinning by remember { mutableStateOf(false) }
    var decidedItem by remember { mutableStateOf<ShoppingItemResponse?>(null) }
    val scope = rememberCoroutineScope()

    // Rotation animation for spinning
    val rotationAnim = remember { Animatable(0f) }

    LaunchedEffect(isSpinning) {
        if (isSpinning) {
            rotationAnim.animateTo(
                targetValue = rotationAnim.value + 360f * 100f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 600, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart,
                ),
            )
        } else {
            rotationAnim.stop()
        }
    }

    // Scale animation for result reveal
    val resultScale by animateFloatAsState(
        targetValue = if (decidedItem != null && !isSpinning) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow,
        ),
        label = "resultScale",
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBackground),
    ) {
        TopBarWithBack(title = "Decide pour moi", onBack = onBack)

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // ── Big circle ──────────────────────────────────────
            item {
                Spacer(Modifier.height(24.dp))

                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(GreenPrimary, GreenDark),
                            )
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    if (isSpinning) {
                        // Spinning dice
                        Icon(
                            imageVector = Icons.Default.Casino,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier
                                .size(52.dp)
                                .rotate(rotationAnim.value),
                        )
                    } else if (decidedItem != null) {
                        // Result
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .scale(resultScale)
                                .padding(16.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingBag,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(36.dp),
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = decidedItem!!.name,
                                fontFamily = FredokaFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                            )
                        }
                    } else {
                        // Default dice icon
                        Icon(
                            imageVector = Icons.Default.Casino,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(52.dp),
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))
            }

            // ── Status text ─────────────────────────────────────
            item {
                if (decidedItem != null && !isSpinning) {
                    Text(
                        text = "Tu dois acheter...",
                        fontFamily = DmSansFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                        color = SubtitleText,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = decidedItem!!.name,
                        fontFamily = FredokaFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 26.sp,
                        color = DarkText,
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "x${decidedItem!!.quantity}",
                        fontFamily = DmSansFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        color = GreenPrimary,
                    )
                } else if (isLoading) {
                    CircularProgressIndicator(color = GreenPrimary)
                } else if (uncheckedItems.isEmpty()) {
                    Text(
                        text = "La liste de courses est vide !",
                        fontFamily = DmSansFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp,
                        color = LightText,
                        textAlign = TextAlign.Center,
                    )
                } else {
                    Text(
                        text = "Laisse le hasard choisir !",
                        fontFamily = FredokaFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        color = DarkText,
                        textAlign = TextAlign.Center,
                    )
                }

                Spacer(Modifier.height(24.dp))
            }

            // ── Button ──────────────────────────────────────────
            item {
                Button(
                    onClick = {
                        if (uncheckedItems.isEmpty() || isSpinning) return@Button
                        isSpinning = true
                        decidedItem = null
                        scope.launch {
                            delay(1500)
                            val picked = uncheckedItems.random()
                            isSpinning = false
                            decidedItem = picked
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uncheckedItems.isNotEmpty() && !isSpinning,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GreenPrimary,
                    ),
                ) {
                    Text(
                        text = if (decidedItem != null) "Relancer" else "Decide pour moi !",
                        fontFamily = DmSansFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                    )
                }

                Spacer(Modifier.height(28.dp))
            }

            // ── Item list ───────────────────────────────────────
            if (uncheckedItems.isNotEmpty()) {
                item {
                    Text(
                        text = "Articles disponibles (${uncheckedItems.size})",
                        fontFamily = FredokaFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = DarkText,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                    )
                }

                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(8.dp, RoundedCornerShape(22.dp))
                            .clip(RoundedCornerShape(22.dp))
                            .background(CardBackground),
                    ) {
                        Column {
                            uncheckedItems.forEachIndexed { index, item ->
                                val isDecided = decidedItem?.id == item.id

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .then(
                                            if (isDecided) {
                                                Modifier.background(GreenPrimary.copy(alpha = 0.10f))
                                            } else {
                                                Modifier
                                            }
                                        )
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isDecided) GreenPrimary else SubtitleText.copy(
                                                    alpha = 0.4f
                                                )
                                            ),
                                    )

                                    Spacer(Modifier.width(12.dp))

                                    Text(
                                        text = item.name,
                                        fontFamily = DmSansFamily,
                                        fontWeight = if (isDecided) FontWeight.SemiBold else FontWeight.Normal,
                                        fontSize = 14.sp,
                                        color = if (isDecided) GreenPrimary else DarkText,
                                        modifier = Modifier.weight(1f),
                                    )

                                    Text(
                                        text = "x${item.quantity}",
                                        fontFamily = DmSansFamily,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 13.sp,
                                        color = if (isDecided) GreenPrimary else SubtitleText,
                                    )
                                }
                            }
                        }
                    }
                }

                item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }
}
