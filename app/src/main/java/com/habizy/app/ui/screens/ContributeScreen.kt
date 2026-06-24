package com.habizy.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habizy.app.ui.theme.DarkText
import com.habizy.app.ui.theme.DmSansFamily
import com.habizy.app.ui.theme.ScreenBackground
import com.habizy.app.ui.theme.SubtitleText

@Composable
fun ContributeScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBackground)
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.Cancel,
            contentDescription = null,
            tint = SubtitleText,
            modifier = Modifier.padding(bottom = 16.dp),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Fonctionnalité supprimée",
            style = TextStyle(
                fontFamily = DmSansFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                color = DarkText,
            ),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Le système de cotisations a été retiré de l'application.",
            style = TextStyle(
                fontFamily = DmSansFamily,
                fontSize = 14.sp,
                color = SubtitleText,
            ),
            textAlign = TextAlign.Center,
        )
    }
}
