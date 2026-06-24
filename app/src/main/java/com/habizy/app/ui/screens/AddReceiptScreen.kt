package com.habizy.app.ui.screens

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habizy.app.data.local.TokenManager
import com.habizy.app.data.model.CatalogArticle
import com.habizy.app.data.model.CreateReceiptItemRequest
import com.habizy.app.data.remote.ApiClient
import com.habizy.app.data.repository.CatalogRepository
import com.habizy.app.data.repository.ReceiptRepository
import com.habizy.app.data.repository.RotationRepository
import com.habizy.app.ui.components.TopBarWithBack
import com.habizy.app.ui.theme.BorderColor
import com.habizy.app.ui.theme.CardBackground
import com.habizy.app.ui.theme.CoralRed
import com.habizy.app.ui.theme.DarkText
import com.habizy.app.ui.theme.DmSansFamily
import com.habizy.app.ui.theme.DividerColor
import com.habizy.app.ui.theme.FredokaFamily
import com.habizy.app.ui.theme.GreenPrimary
import com.habizy.app.ui.theme.LightCardBg
import com.habizy.app.ui.theme.LightText
import com.habizy.app.ui.theme.ScreenBackground
import com.habizy.app.ui.theme.SubtitleText
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.UUID

private data class ReceiptLineItem(
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val price: Double,
    val category: String,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddReceiptScreen(
    onBack: () -> Unit,
    onReceiptCreated: () -> Unit,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val tokenManager = remember { TokenManager(context) }
    val receiptRepository = remember { ReceiptRepository(ApiClient.apiService) }
    val rotationRepository = remember { RotationRepository(ApiClient.apiService) }
    val catalogRepository = remember { CatalogRepository(ApiClient.apiService) }

    // Turn check state
    var isCheckingTurn by remember { mutableStateOf(true) }
    var isMyTurn by remember { mutableStateOf(false) }
    var currentPurchaserName by remember { mutableStateOf("") }

    // Form state
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var store by remember { mutableStateOf("") }
    val calendar = remember { Calendar.getInstance() }
    var selectedDate by remember {
        mutableStateOf(
            String.format(
                "%04d-%02d-%02d",
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH),
            )
        )
    }
    var selectedTime by remember {
        mutableStateOf(
            String.format(
                "%02d:%02d",
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
            )
        )
    }
    val lineItems = remember { mutableStateListOf<ReceiptLineItem>() }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    // Catalog state
    var catalogArticles by remember { mutableStateOf<List<CatalogArticle>>(emptyList()) }
    var showCatalogSheet by remember { mutableStateOf(false) }
    var catalogSearch by remember { mutableStateOf("") }

    // Price dialog state
    var articleToAdd by remember { mutableStateOf<CatalogArticle?>(null) }
    var priceInput by remember { mutableStateOf("") }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview(),
    ) { bitmap ->
        if (bitmap != null) {
            capturedBitmap = bitmap
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        if (isGranted) cameraLauncher.launch(null)
    }

    fun launchCamera() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            cameraLauncher.launch(null)
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Check turn on appear
    LaunchedEffect(Unit) {
        try {
            val colocationId = tokenManager.getColocationId() ?: run {
                errorMessage = "Colocation introuvable"
                isCheckingTurn = false
                return@LaunchedEffect
            }
            val me = ApiClient.apiService.getMe()
            val rotation = rotationRepository.getRotation(colocationId).getOrNull() ?: emptyList()

            // Find current turn user (status == "current")
            val currentEntry = rotation.find { it.status == "current" }
            if (currentEntry != null && currentEntry.user.id == me.id) {
                isMyTurn = true
            } else if (currentEntry != null) {
                currentPurchaserName = currentEntry.user.name
                isMyTurn = false
            } else {
                // No rotation set or empty => allow
                isMyTurn = true
            }

            // Load catalog
            val articles = catalogRepository.getArticles(colocationId).getOrNull()
            if (articles != null) {
                catalogArticles = articles
            }
        } catch (e: Exception) {
            errorMessage = e.message ?: "Erreur lors de la verification"
            isMyTurn = true // fallback: allow
        }
        isCheckingTurn = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBackground),
    ) {
        TopBarWithBack(title = "Nouveau ticket", onBack = onBack)

        when {
            isCheckingTurn -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        color = GreenPrimary,
                        modifier = Modifier.size(36.dp),
                        strokeWidth = 3.dp,
                    )
                }
            }

            !isMyTurn -> {
                // Not my turn screen
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 32.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = SubtitleText,
                            modifier = Modifier.size(64.dp),
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Ce n'est pas ton tour",
                            fontFamily = FredokaFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 20.sp,
                            color = DarkText,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "C'est au tour de $currentPurchaserName",
                            fontFamily = DmSansFamily,
                            fontSize = 15.sp,
                            color = SubtitleText,
                        )
                    }
                }
            }

            else -> {
                // Receipt form
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 32.dp),
                ) {
                    Spacer(modifier = Modifier.height(8.dp))

                    // Photo capture
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (capturedBitmap != null) 200.dp else 120.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(LightCardBg)
                            .border(1.dp, BorderColor, RoundedCornerShape(16.dp))
                            .clickable { launchCamera() },
                        contentAlignment = Alignment.Center,
                    ) {
                        if (capturedBitmap != null) {
                            Image(
                                bitmap = capturedBitmap!!.asImageBitmap(),
                                contentDescription = "Photo du ticket",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(16.dp)),
                                contentScale = ContentScale.Crop,
                            )
                            // Retake overlay
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black.copy(alpha = 0.5f))
                                    .clickable { launchCamera() },
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Reprendre",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    tint = SubtitleText,
                                    modifier = Modifier.size(36.dp),
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Photographier le ticket",
                                    fontFamily = DmSansFamily,
                                    fontSize = 14.sp,
                                    color = SubtitleText,
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Store input
                    Text(
                        text = "Magasin",
                        fontFamily = FredokaFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = DarkText,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = store,
                        onValueChange = { store = it },
                        placeholder = {
                            Text(
                                text = "Nom du magasin",
                                fontFamily = DmSansFamily,
                                color = SubtitleText,
                            )
                        },
                        textStyle = TextStyle(
                            fontFamily = DmSansFamily,
                            fontSize = 16.sp,
                            color = DarkText,
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

                    Spacer(modifier = Modifier.height(16.dp))

                    // Date picker row
                    Text(
                        text = "Date",
                        fontFamily = FredokaFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = DarkText,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(CardBackground)
                            .border(1.dp, BorderColor, RoundedCornerShape(14.dp))
                            .clickable {
                                val parts = selectedDate.split("-")
                                val y = parts[0].toInt()
                                val m = parts[1].toInt() - 1
                                val d = parts[2].toInt()
                                DatePickerDialog(context, { _, year, month, day ->
                                    selectedDate = String.format("%04d-%02d-%02d", year, month + 1, day)
                                }, y, m, d).show()
                            }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = SubtitleText,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = formatDisplayDate(selectedDate),
                            fontFamily = DmSansFamily,
                            fontSize = 15.sp,
                            color = DarkText,
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Time picker row
                    Text(
                        text = "Heure",
                        fontFamily = FredokaFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = DarkText,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(CardBackground)
                            .border(1.dp, BorderColor, RoundedCornerShape(14.dp))
                            .clickable {
                                val parts = selectedTime.split(":")
                                val h = parts[0].toInt()
                                val m = parts[1].toInt()
                                TimePickerDialog(context, { _, hour, minute ->
                                    selectedTime = String.format("%02d:%02d", hour, minute)
                                }, h, m, true).show()
                            }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = SubtitleText,
                            modifier = Modifier.size(20.dp),
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = selectedTime,
                            fontFamily = DmSansFamily,
                            fontSize = 15.sp,
                            color = DarkText,
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Articles section
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = "Articles",
                            fontFamily = FredokaFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 18.sp,
                            color = DarkText,
                        )
                        IconButton(
                            onClick = {
                                catalogSearch = ""
                                showCatalogSheet = true
                            },
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(GreenPrimary),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Ajouter un article",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }
                    }

                    if (lineItems.isEmpty()) {
                        Text(
                            text = "Aucun article ajoute",
                            fontFamily = DmSansFamily,
                            fontSize = 14.sp,
                            color = LightText,
                            modifier = Modifier.padding(vertical = 8.dp),
                        )
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(CardBackground)
                                .padding(4.dp),
                        ) {
                            lineItems.forEachIndexed { index, item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.name,
                                            fontFamily = DmSansFamily,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 15.sp,
                                            color = DarkText,
                                        )
                                        Text(
                                            text = item.category,
                                            fontFamily = DmSansFamily,
                                            fontSize = 12.sp,
                                            color = SubtitleText,
                                        )
                                    }
                                    Text(
                                        text = String.format("%.2f €", item.price).replace(".", ","),
                                        fontFamily = DmSansFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 15.sp,
                                        color = DarkText,
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Supprimer",
                                        tint = CoralRed,
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clickable {
                                                lineItems.removeAt(index)
                                            },
                                    )
                                }
                                if (index < lineItems.lastIndex) {
                                    HorizontalDivider(color = DividerColor)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Total row
                    val total = lineItems.sumOf { it.price }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(CardBackground)
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Total",
                            fontFamily = FredokaFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 17.sp,
                            color = DarkText,
                        )
                        Text(
                            text = String.format("%.2f €", total).replace(".", ","),
                            fontFamily = FredokaFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = GreenPrimary,
                        )
                    }

                    // Error text
                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = errorMessage ?: "",
                            fontFamily = DmSansFamily,
                            fontSize = 13.sp,
                            color = CoralRed,
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Save button
                    val canSave = store.isNotBlank() && lineItems.isNotEmpty() && !isSaving
                    Button(
                        onClick = {
                            scope.launch {
                                isSaving = true
                                errorMessage = null
                                try {
                                    val colocationId = tokenManager.getColocationId()
                                        ?: throw Exception("Colocation introuvable")
                                    val items = lineItems.map { li ->
                                        CreateReceiptItemRequest(
                                            name = li.name,
                                            price = li.price,
                                            quantity = 1,
                                            category = li.category,
                                        )
                                    }
                                    val totalAmount = lineItems.sumOf { it.price }
                                    receiptRepository.createReceipt(
                                        colocationId = colocationId,
                                        store = store.trim(),
                                        date = selectedDate,
                                        time = selectedTime,
                                        totalAmount = totalAmount,
                                        photoUrl = null,
                                        items = items,
                                    ).getOrThrow()
                                    onReceiptCreated()
                                    onBack()
                                } catch (e: Exception) {
                                    errorMessage = e.message ?: "Erreur lors de l'enregistrement"
                                } finally {
                                    isSaving = false
                                }
                            }
                        },
                        enabled = canSave,
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GreenPrimary,
                            contentColor = Color.White,
                            disabledContainerColor = GreenPrimary.copy(alpha = 0.5f),
                            disabledContentColor = Color.White.copy(alpha = 0.7f),
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .shadow(6.dp, RoundedCornerShape(18.dp)),
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = Color.White,
                                strokeWidth = 2.dp,
                            )
                        } else {
                            Text(
                                text = "Enregistrer le ticket",
                                fontFamily = FredokaFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                            )
                        }
                    }
                }
            }
        }

        // Catalog bottom sheet
        if (showCatalogSheet) {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ModalBottomSheet(
                onDismissRequest = { showCatalogSheet = false },
                sheetState = sheetState,
                containerColor = CardBackground,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 32.dp),
                ) {
                    Text(
                        text = "Choisir un article",
                        fontFamily = FredokaFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        color = DarkText,
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Search field
                    OutlinedTextField(
                        value = catalogSearch,
                        onValueChange = { catalogSearch = it },
                        placeholder = {
                            Text(
                                text = "Rechercher...",
                                fontFamily = DmSansFamily,
                                color = SubtitleText,
                            )
                        },
                        textStyle = TextStyle(
                            fontFamily = DmSansFamily,
                            fontSize = 15.sp,
                            color = DarkText,
                        ),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = SubtitleText,
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = LightCardBg,
                            unfocusedContainerColor = LightCardBg,
                            focusedBorderColor = GreenPrimary,
                            unfocusedBorderColor = BorderColor,
                            cursorColor = GreenPrimary,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    val filteredArticles = catalogArticles.filter {
                        catalogSearch.isBlank() ||
                                it.name.contains(catalogSearch, ignoreCase = true) ||
                                it.category.contains(catalogSearch, ignoreCase = true)
                    }

                    if (filteredArticles.isEmpty()) {
                        Text(
                            text = "Aucun article trouve",
                            fontFamily = DmSansFamily,
                            fontSize = 14.sp,
                            color = SubtitleText,
                            modifier = Modifier.padding(vertical = 16.dp),
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.height(300.dp),
                        ) {
                            items(filteredArticles, key = { it.id }) { article ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            articleToAdd = article
                                            priceInput = ""
                                            showCatalogSheet = false
                                        }
                                        .padding(vertical = 12.dp, horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = article.name,
                                            fontFamily = DmSansFamily,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 15.sp,
                                            color = DarkText,
                                        )
                                        Text(
                                            text = article.category,
                                            fontFamily = DmSansFamily,
                                            fontSize = 12.sp,
                                            color = SubtitleText,
                                        )
                                    }
                                }
                                HorizontalDivider(color = DividerColor)
                            }
                        }
                    }
                }
            }
        }

        // Price input dialog
        if (articleToAdd != null) {
            AlertDialog(
                onDismissRequest = { articleToAdd = null },
                containerColor = CardBackground,
                shape = RoundedCornerShape(22.dp),
                title = {
                    Text(
                        text = articleToAdd!!.name,
                        fontFamily = FredokaFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        color = DarkText,
                    )
                },
                text = {
                    OutlinedTextField(
                        value = priceInput,
                        onValueChange = { priceInput = it },
                        placeholder = {
                            Text(
                                text = "Prix en €",
                                fontFamily = DmSansFamily,
                                color = SubtitleText,
                            )
                        },
                        textStyle = TextStyle(
                            fontFamily = DmSansFamily,
                            fontSize = 16.sp,
                            color = DarkText,
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
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
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val price = priceInput.replace(",", ".").toDoubleOrNull()
                            if (price != null && price > 0) {
                                lineItems.add(
                                    ReceiptLineItem(
                                        name = articleToAdd!!.name,
                                        price = price,
                                        category = articleToAdd!!.category,
                                    )
                                )
                                articleToAdd = null
                            }
                        },
                    ) {
                        Text(
                            text = "Ajouter",
                            fontFamily = DmSansFamily,
                            fontWeight = FontWeight.SemiBold,
                            color = GreenPrimary,
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { articleToAdd = null }) {
                        Text(
                            text = "Annuler",
                            fontFamily = DmSansFamily,
                            color = SubtitleText,
                        )
                    }
                },
            )
        }
    }
}

private fun formatDisplayDate(isoDate: String): String {
    return try {
        val parts = isoDate.split("-")
        "${parts[2]}/${parts[1]}/${parts[0]}"
    } catch (_: Exception) {
        isoDate
    }
}
