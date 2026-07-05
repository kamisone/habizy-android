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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
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
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val canSubmit = !isJoinLoading && inviteCode.isNotBlank() && name.isNotBlank() && email.isNotBlank()

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
            text = "Entre le code d'invitation et tes informations",
            fontFamily = DmSansFamily,
            fontSize = 14.sp,
            color = SubtitleText,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(24.dp))

        // Invite code
        OutlinedTextField(
            value = inviteCode,
            onValueChange = {
                inviteCode = it
                viewModel.clearJoinError()
            },
            label = { Text("Code d'invitation", fontFamily = DmSansFamily, color = SubtitleText, fontSize = 13.sp) },
            textStyle = TextStyle(fontFamily = DmSansFamily, fontSize = 16.sp, color = DarkText),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = fieldColors(),
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(12.dp))

        // Name
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Prénom et nom", fontFamily = DmSansFamily, color = SubtitleText, fontSize = 13.sp) },
            textStyle = TextStyle(fontFamily = DmSansFamily, fontSize = 16.sp, color = DarkText),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = fieldColors(),
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(12.dp))

        // Email
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email", fontFamily = DmSansFamily, color = SubtitleText, fontSize = 13.sp) },
            textStyle = TextStyle(fontFamily = DmSansFamily, fontSize = 16.sp, color = DarkText),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = fieldColors(),
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(12.dp))

        // Password (optional)
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Mot de passe (optionnel)", fontFamily = DmSansFamily, color = SubtitleText, fontSize = 13.sp) },
            placeholder = { Text("Généré automatiquement si vide", fontFamily = DmSansFamily, color = SubtitleText.copy(alpha = 0.6f), fontSize = 13.sp) },
            textStyle = TextStyle(fontFamily = DmSansFamily, fontSize = 16.sp, color = DarkText),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (passwordVisible) "Masquer" else "Afficher",
                        tint = SubtitleText,
                    )
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = fieldColors(),
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
            onClick = {
                viewModel.joinColocation(
                    inviteCode = inviteCode.trim(),
                    name = name.trim(),
                    email = email.trim(),
                    password = password.ifBlank { null },
                )
            },
            enabled = canSubmit,
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

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = CardBackground,
    unfocusedContainerColor = CardBackground,
    focusedBorderColor = GreenPrimary,
    unfocusedBorderColor = BorderColor,
    cursorColor = GreenPrimary,
    focusedLabelColor = GreenPrimary,
    unfocusedLabelColor = SubtitleText,
)
