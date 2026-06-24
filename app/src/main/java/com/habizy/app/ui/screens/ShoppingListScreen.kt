package com.habizy.app.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
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
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.habizy.app.data.model.CatalogArticle
import com.habizy.app.data.model.ShoppingItemResponse
import com.habizy.app.ui.components.LocalSnackbarHost
import com.habizy.app.ui.components.SnackbarType
import com.habizy.app.ui.components.TopBarWithBack
import com.habizy.app.ui.components.showTyped
import com.habizy.app.ui.theme.BodyText
import com.habizy.app.ui.theme.BorderColor
import com.habizy.app.ui.theme.CardBackground
import com.habizy.app.ui.theme.CoralRed
import com.habizy.app.ui.theme.DarkText
import com.habizy.app.ui.theme.DividerColor
import com.habizy.app.ui.theme.DmSansFamily
import com.habizy.app.ui.theme.FredokaFamily
import com.habizy.app.ui.theme.GreenPrimary
import com.habizy.app.ui.theme.LightCardBg
import com.habizy.app.ui.theme.LightText
import com.habizy.app.ui.theme.ScreenBackground
import com.habizy.app.ui.theme.SubtitleText
import com.habizy.app.ui.viewmodel.ShoppingViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListScreen(
    onBack: () -> Unit,
    viewModel: ShoppingViewModel = viewModel(),
) {
    val items by viewModel.items.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val successMessage by viewModel.successMessage.collectAsStateWithLifecycle()
    val catalogArticles by viewModel.catalogArticles.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val isAdmin by viewModel.isAdmin.collectAsStateWithLifecycle()

    val snackbarHost = LocalSnackbarHost.current
    val scope = rememberCoroutineScope()

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.silentRefresh()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    var showCatalogSheet by remember { mutableStateOf(false) }
    var showQuantitySheet by remember { mutableStateOf(false) }
    var showAddCatalogSheet by remember { mutableStateOf(false) }
    var selectedCatalogArticle by remember { mutableStateOf<CatalogArticle?>(null) }

    val catalogSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val quantitySheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val addCatalogSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val uncheckedItems = items.filter { !it.isChecked }

    // Show error snackbar
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            scope.launch {
                snackbarHost.showTyped(it, SnackbarType.ERROR)
                viewModel.clearErrorMessage()
            }
        }
    }

    // Show success snackbar
    LaunchedEffect(successMessage) {
        successMessage?.let {
            scope.launch {
                snackbarHost.showTyped(it, SnackbarType.SUCCESS)
                viewModel.clearSuccessMessage()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBackground),
    ) {
        TopBarWithBack(title = "Articles manquants", onBack = onBack)

        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier.fillMaxSize(),
        ) {
            if (isLoading && items.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = GreenPrimary)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 18.dp)
                        .padding(bottom = 24.dp),
                ) {
                    // Header
                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = "${uncheckedItems.size} articles a acheter",
                        fontFamily = DmSansFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                        color = SubtitleText,
                    )

                    Spacer(Modifier.height(16.dp))

                    // Add button
                    Button(
                        onClick = { showCatalogSheet = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GreenPrimary,
                            contentColor = Color.White,
                        ),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Ajouter un article",
                            fontFamily = DmSansFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                        )
                    }

                    Spacer(Modifier.height(18.dp))

                    // Items list
                    if (uncheckedItems.isEmpty()) {
                        // Empty state
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 48.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = null,
                                tint = LightText,
                                modifier = Modifier.size(56.dp),
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                text = "Rien a acheter",
                                fontFamily = FredokaFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 18.sp,
                                color = DarkText,
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "Ajoutez des articles depuis le catalogue",
                                fontFamily = DmSansFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 14.sp,
                                color = SubtitleText,
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(8.dp, RoundedCornerShape(22.dp))
                                .clip(RoundedCornerShape(22.dp))
                                .background(CardBackground),
                        ) {
                            Column {
                                uncheckedItems.forEachIndexed { index, item ->
                                    ShoppingItemRow(
                                        item = item,
                                        onDelete = { viewModel.delete(item.id) },
                                    )
                                    if (index < uncheckedItems.lastIndex) {
                                        HorizontalDivider(
                                            color = DividerColor,
                                            modifier = Modifier.padding(horizontal = 16.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Admin catalog management
                    if (isAdmin) {
                        Spacer(Modifier.height(28.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "Gerer le catalogue",
                                fontFamily = FredokaFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                                color = DarkText,
                            )
                            IconButton(
                                onClick = { showAddCatalogSheet = true },
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
                                        contentDescription = "Ajouter au catalogue",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp),
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        if (catalogArticles.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(8.dp, RoundedCornerShape(22.dp))
                                    .clip(RoundedCornerShape(22.dp))
                                    .background(CardBackground),
                            ) {
                                Column {
                                    catalogArticles.forEachIndexed { index, article ->
                                        CatalogArticleRow(
                                            article = article,
                                            onDelete = {
                                                viewModel.deleteCatalogArticle(article.id)
                                            },
                                        )
                                        if (index < catalogArticles.lastIndex) {
                                            HorizontalDivider(
                                                color = DividerColor,
                                                modifier = Modifier.padding(horizontal = 16.dp),
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }

    // Catalog selection bottom sheet
    if (showCatalogSheet) {
        ModalBottomSheet(
            onDismissRequest = { showCatalogSheet = false },
            sheetState = catalogSheetState,
            containerColor = CardBackground,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        ) {
            CatalogSelectionSheet(
                catalogArticles = catalogArticles,
                onSelect = { article ->
                    selectedCatalogArticle = article
                    showCatalogSheet = false
                    showQuantitySheet = true
                },
            )
        }
    }

    // Quantity picker bottom sheet
    if (showQuantitySheet && selectedCatalogArticle != null) {
        ModalBottomSheet(
            onDismissRequest = {
                showQuantitySheet = false
                selectedCatalogArticle = null
            },
            sheetState = quantitySheetState,
            containerColor = CardBackground,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        ) {
            QuantityPickerSheet(
                articleName = selectedCatalogArticle!!.name,
                onAdd = { quantity ->
                    viewModel.addItem(selectedCatalogArticle!!.name, quantity)
                    showQuantitySheet = false
                    selectedCatalogArticle = null
                },
            )
        }
    }

    // Add catalog article bottom sheet
    if (showAddCatalogSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddCatalogSheet = false },
            sheetState = addCatalogSheetState,
            containerColor = CardBackground,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        ) {
            AddCatalogArticleSheet(
                existingCategories = categories,
                onAdd = { name, category ->
                    viewModel.addCatalogArticle(name, category)
                    showAddCatalogSheet = false
                },
            )
        }
    }
}

@Composable
private fun ShoppingItemRow(
    item: ShoppingItemResponse,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Green dot
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(GreenPrimary),
        )

        Spacer(Modifier.width(12.dp))

        // Item name
        Text(
            text = item.name,
            fontFamily = DmSansFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = DarkText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )

        // Quantity
        if (item.quantity > 1) {
            Text(
                text = "x${item.quantity}",
                fontFamily = DmSansFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 13.sp,
                color = SubtitleText,
                modifier = Modifier.padding(horizontal = 8.dp),
            )
        }

        // Delete button
        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(32.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Supprimer",
                tint = CoralRed,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
private fun CatalogArticleRow(
    article: CatalogArticle,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = article.name,
                fontFamily = DmSansFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = DarkText,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = article.category,
                fontFamily = DmSansFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                color = SubtitleText,
            )
        }

        IconButton(
            onClick = onDelete,
            modifier = Modifier.size(32.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Supprimer",
                tint = CoralRed,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
private fun CatalogSelectionSheet(
    catalogArticles: List<CatalogArticle>,
    onSelect: (CatalogArticle) -> Unit,
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredArticles = remember(catalogArticles, searchQuery) {
        if (searchQuery.isBlank()) catalogArticles
        else catalogArticles.filter {
            it.name.contains(searchQuery, ignoreCase = true)
        }
    }

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

        Spacer(Modifier.height(14.dp))

        // Search field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = {
                Text(
                    text = "Rechercher...",
                    fontFamily = DmSansFamily,
                    fontSize = 14.sp,
                    color = LightText,
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = LightText,
                    modifier = Modifier.size(20.dp),
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Effacer",
                            tint = LightText,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GreenPrimary,
                unfocusedBorderColor = BorderColor,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        )

        Spacer(Modifier.height(14.dp))

        if (filteredArticles.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Aucun article trouve",
                    fontFamily = DmSansFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    color = SubtitleText,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.height(400.dp),
            ) {
                itemsIndexed(filteredArticles) { index, article ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(article) }
                            .padding(horizontal = 4.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = article.name,
                                fontFamily = DmSansFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp,
                                color = DarkText,
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = article.category,
                                fontFamily = DmSansFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 12.sp,
                                color = SubtitleText,
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Ajouter",
                            tint = GreenPrimary,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                    if (index < filteredArticles.lastIndex) {
                        HorizontalDivider(color = DividerColor)
                    }
                }
            }
        }
    }
}

@Composable
private fun QuantityPickerSheet(
    articleName: String,
    onAdd: (Int) -> Unit,
) {
    var quantity by remember { mutableIntStateOf(1) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = articleName,
            fontFamily = FredokaFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            color = DarkText,
        )

        Spacer(Modifier.height(24.dp))

        // Quantity controls
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            IconButton(
                onClick = { if (quantity > 1) quantity-- },
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(LightCardBg),
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Diminuer",
                    tint = DarkText,
                    modifier = Modifier.size(20.dp),
                )
            }

            Spacer(Modifier.width(24.dp))

            Text(
                text = "$quantity",
                fontFamily = FredokaFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                color = DarkText,
            )

            Spacer(Modifier.width(24.dp))

            IconButton(
                onClick = { quantity++ },
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(LightCardBg),
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Augmenter",
                    tint = DarkText,
                    modifier = Modifier.size(20.dp),
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = { onAdd(quantity) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = GreenPrimary,
                contentColor = Color.White,
            ),
        ) {
            Text(
                text = "Ajouter",
                fontFamily = DmSansFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
            )
        }
    }
}

@Composable
private fun AddCatalogArticleSheet(
    existingCategories: List<String>,
    onAdd: (String, String) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 32.dp),
    ) {
        Text(
            text = "Nouvel article",
            fontFamily = FredokaFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            color = DarkText,
        )

        Spacer(Modifier.height(18.dp))

        // Name field
        Text(
            text = "Nom",
            fontFamily = DmSansFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp,
            color = BodyText,
        )
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            placeholder = {
                Text(
                    text = "Nom de l'article",
                    fontFamily = DmSansFamily,
                    fontSize = 14.sp,
                    color = LightText,
                )
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GreenPrimary,
                unfocusedBorderColor = BorderColor,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        )

        Spacer(Modifier.height(14.dp))

        // Category field
        Text(
            text = "Categorie",
            fontFamily = DmSansFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp,
            color = BodyText,
        )
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = category,
            onValueChange = { category = it },
            placeholder = {
                Text(
                    text = "Categorie",
                    fontFamily = DmSansFamily,
                    fontSize = 14.sp,
                    color = LightText,
                )
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = GreenPrimary,
                unfocusedBorderColor = BorderColor,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (name.isNotBlank() && category.isNotBlank()) {
                        onAdd(name.trim(), category.trim())
                    }
                },
            ),
        )

        // Category suggestions
        if (existingCategories.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                existingCategories.take(5).forEach { cat ->
                    Text(
                        text = cat,
                        fontFamily = DmSansFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        color = GreenPrimary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(GreenPrimary.copy(alpha = 0.1f))
                            .clickable { category = cat }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = {
                if (name.isNotBlank() && category.isNotBlank()) {
                    onAdd(name.trim(), category.trim())
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            enabled = name.isNotBlank() && category.isNotBlank(),
            colors = ButtonDefaults.buttonColors(
                containerColor = GreenPrimary,
                contentColor = Color.White,
                disabledContainerColor = GreenPrimary.copy(alpha = 0.4f),
                disabledContentColor = Color.White.copy(alpha = 0.7f),
            ),
        ) {
            Text(
                text = "Ajouter au catalogue",
                fontFamily = DmSansFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
            )
        }
    }
}
