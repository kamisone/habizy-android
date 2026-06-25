package com.habizy.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
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
fun LoginScreen() {
    val viewModel: AuthViewModel = viewModel()
    val isLoginLoading by viewModel.isLoginLoading.collectAsStateWithLifecycle()
    val isJoinLoading by viewModel.isJoinLoading.collectAsStateWithLifecycle()
    val loginError by viewModel.loginError.collectAsStateWithLifecycle()
    val joinError by viewModel.joinError.collectAsStateWithLifecycle()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var inviteCode by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBackground)
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp)
            .padding(top = 60.dp, bottom = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // -- Logo section --
        androidx.compose.foundation.Image(
            painter = painterResource(id = R.drawable.logo_habizy),
            contentDescription = "Logo Habizy",
            modifier = Modifier.size(90.dp),
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Habizy",
            style = TextStyle(
                fontFamily = FredokaFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 36.sp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        GreenPrimary,
                        Blue,
                        Purple,
                        Color(0xFFEC4899),
                    ),
                ),
            ),
        )

        Spacer(modifier = Modifier.height(40.dp))

        // -- Login section --
        Text(
            text = "Connexion",
            fontFamily = FredokaFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 22.sp,
            color = DarkText,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Email field
        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                viewModel.clearLoginError()
            },
            placeholder = {
                Text(
                    text = "Email",
                    fontFamily = DmSansFamily,
                    color = SubtitleText,
                )
            },
            textStyle = TextStyle(
                fontFamily = DmSansFamily,
                fontSize = 16.sp,
                color = DarkText,
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
            ),
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = CardBackground,
                unfocusedContainerColor = CardBackground,
                focusedBorderColor = GreenPrimary,
                unfocusedBorderColor = BorderColor,
                cursorColor = GreenPrimary,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 0.dp),
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Password field
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                viewModel.clearLoginError()
            },
            placeholder = {
                Text(
                    text = "Mot de passe",
                    fontFamily = DmSansFamily,
                    color = SubtitleText,
                )
            },
            textStyle = TextStyle(
                fontFamily = DmSansFamily,
                fontSize = 16.sp,
                color = DarkText,
            ),
            visualTransformation = if (passwordVisible) VisualTransformation.None
            else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
            ),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff
                        else Icons.Default.Visibility,
                        contentDescription = if (passwordVisible) "Masquer" else "Afficher",
                        tint = SubtitleText,
                    )
                }
            },
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

        // Login error
        if (loginError != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = loginError ?: "",
                fontFamily = DmSansFamily,
                fontSize = 13.sp,
                color = CoralRed,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Login button
        Button(
            onClick = { viewModel.login(email.trim(), password) },
            enabled = !isLoginLoading && email.isNotBlank() && password.isNotBlank(),
            shape = RoundedCornerShape(18.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp, pressedElevation = 2.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = GreenPrimary,
                contentColor = Color.White,
                disabledContainerColor = GreenPrimary.copy(alpha = 0.5f),
                disabledContentColor = Color.White.copy(alpha = 0.7f),
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
        ) {
            if (isLoginLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = Color.White,
                    strokeWidth = 2.dp,
                )
            } else {
                Text(
                    text = "Se connecter",
                    fontFamily = FredokaFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // -- Divider with "ou" --
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = BorderColor,
            )
            Text(
                text = "ou",
                fontFamily = DmSansFamily,
                fontSize = 14.sp,
                color = SubtitleText,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = BorderColor,
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // -- Join section --
        Text(
            text = "Rejoindre une colocation",
            fontFamily = FredokaFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 22.sp,
            color = DarkText,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Entre le code d'invitation que tu as reçu",
            fontFamily = DmSansFamily,
            fontSize = 14.sp,
            color = SubtitleText,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Invite code field
        OutlinedTextField(
            value = inviteCode,
            onValueChange = {
                inviteCode = it
                viewModel.clearJoinError()
            },
            placeholder = {
                Text(
                    text = "Code d'invitation",
                    fontFamily = DmSansFamily,
                    color = SubtitleText,
                )
            },
            textStyle = TextStyle(
                fontFamily = DmSansFamily,
                fontSize = 16.sp,
                color = DarkText,
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Done,
            ),
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

        // Join error
        if (joinError != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = joinError ?: "",
                fontFamily = DmSansFamily,
                fontSize = 13.sp,
                color = CoralRed,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Join button
        Button(
            onClick = { viewModel.joinColocation(inviteCode.trim()) },
            enabled = !isJoinLoading && inviteCode.isNotBlank(),
            shape = RoundedCornerShape(18.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp, pressedElevation = 2.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = DarkText,
                contentColor = Color.White,
                disabledContainerColor = DarkText.copy(alpha = 0.5f),
                disabledContentColor = Color.White.copy(alpha = 0.7f),
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
        ) {
            if (isJoinLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = Color.White,
                    strokeWidth = 2.dp,
                )
            } else {
                Text(
                    text = "Rejoindre",
                    fontFamily = FredokaFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                )
            }
        }
    }
}
