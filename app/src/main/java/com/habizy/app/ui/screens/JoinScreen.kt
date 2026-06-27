package com.habizy.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.habizy.app.R
import com.habizy.app.ui.theme.Blue
import com.habizy.app.ui.theme.BorderColor
import com.habizy.app.ui.theme.CardBackground
import com.habizy.app.ui.theme.CoralRed
import com.habizy.app.ui.theme.DarkText
import com.habizy.app.ui.theme.DmSansFamily
import com.habizy.app.ui.theme.FredokaFamily
import com.habizy.app.ui.theme.GreenPrimary
import com.habizy.app.ui.theme.Purple
import com.habizy.app.ui.theme.ScreenBackground
import com.habizy.app.ui.theme.SubtitleText
import com.habizy.app.ui.viewmodel.AuthViewModel

@Composable
fun JoinScreen(viewModel: AuthViewModel = viewModel()) {
    val isJoinLoading by viewModel.isJoinLoading.collectAsStateWithLifecycle()
    val joinError by viewModel.joinError.collectAsStateWithLifecycle()

    var inviteCode by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBackground)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(top = 60.dp, bottom = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        androidx.compose.foundation.Image(
            painter = painterResource(id = R.drawable.logo_habizy),
            contentDescription = "Logo Habizy",
            modifier = Modifier.size(80.dp),
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = "Habizy",
            style = TextStyle(
                fontFamily = FredokaFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp,
                brush = Brush.linearGradient(
                    colors = listOf(GreenPrimary, Blue, Purple, Color(0xFFEC4899)),
                ),
            ),
        )

        Spacer(Modifier.height(48.dp))

        Text(
            text = "Rejoindre une colocation",
            fontFamily = FredokaFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 22.sp,
            color = DarkText,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Entre le code d'invitation que tu as reçu",
            fontFamily = DmSansFamily,
            fontSize = 14.sp,
            color = SubtitleText,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(20.dp))

        OutlinedTextField(
            value = inviteCode,
            onValueChange = {
                inviteCode = it
                viewModel.clearJoinError()
            },
            placeholder = {
                Text(text = "Code d'invitation", fontFamily = DmSansFamily, color = SubtitleText)
            },
            textStyle = TextStyle(fontFamily = DmSansFamily, fontSize = 16.sp, color = DarkText),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Done),
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = CardBackground,
                unfocusedContainerColor = CardBackground,
                focusedBorderColor = GreenPrimary,
                unfocusedBorderColor = BorderColor,
                cursorColor = GreenPrimary,
            ),
            modifier = Modifier.fillMaxWidth(),
        )

        if (joinError != null) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = joinError ?: "",
                fontFamily = DmSansFamily,
                fontSize = 13.sp,
                color = CoralRed,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = { viewModel.joinColocation(inviteCode.trim()) },
            enabled = !isJoinLoading && inviteCode.isNotBlank(),
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(18.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp, pressedElevation = 2.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = DarkText,
                contentColor = Color.White,
                disabledContainerColor = DarkText.copy(alpha = 0.5f),
                disabledContentColor = Color.White.copy(alpha = 0.7f),
            ),
        ) {
            if (isJoinLoading) {
                CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
            } else {
                Text(text = "Rejoindre", fontFamily = FredokaFamily, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            }
        }
    }
}
