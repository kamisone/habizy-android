package com.habizy.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habizy.app.R
import com.habizy.app.ui.theme.Blue
import com.habizy.app.ui.theme.BorderColor
import com.habizy.app.ui.theme.DarkText
import com.habizy.app.ui.theme.DmSansFamily
import com.habizy.app.ui.theme.FredokaFamily
import com.habizy.app.ui.theme.GreenPrimary
import com.habizy.app.ui.theme.Purple
import com.habizy.app.ui.theme.ScreenBackground
import com.habizy.app.ui.theme.SubtitleText

@Composable
fun WelcomeScreen(
    onLogin: () -> Unit,
    onCreateAccount: () -> Unit,
    onJoinColocation: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBackground)
            .padding(horizontal = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.weight(1f))

        androidx.compose.foundation.Image(
            painter = painterResource(id = R.drawable.logo_habizy),
            contentDescription = "Logo Habizy",
            modifier = Modifier.size(100.dp),
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = "Habizy",
            style = TextStyle(
                fontFamily = FredokaFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 42.sp,
                brush = Brush.linearGradient(
                    colors = listOf(GreenPrimary, Blue, Purple, Color(0xFFEC4899)),
                ),
            ),
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = "Gérez votre colocation\nen toute sérénité.",
            fontFamily = DmSansFamily,
            fontSize = 16.sp,
            color = SubtitleText,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp,
        )

        Spacer(Modifier.weight(1f))

        Button(
            onClick = onCreateAccount,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(18.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp, pressedElevation = 2.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = GreenPrimary,
                contentColor = Color.White,
            ),
        ) {
            Text(
                text = "Créer un compte",
                fontFamily = FredokaFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 17.sp,
            )
        }

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = onJoinColocation,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(18.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp, pressedElevation = 1.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = DarkText,
                contentColor = Color.White,
            ),
        ) {
            Text(
                text = "Rejoindre une colocation",
                fontFamily = FredokaFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 17.sp,
            )
        }

        Spacer(Modifier.height(12.dp))

        OutlinedButton(
            onClick = onLogin,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(18.dp),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, BorderColor),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = DarkText),
        ) {
            Text(
                text = "Se connecter",
                fontFamily = FredokaFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 17.sp,
            )
        }

        Spacer(Modifier.height(48.dp))
    }
}
