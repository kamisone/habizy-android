package com.habizy.app.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.habizy.app.data.local.TokenManager
import com.habizy.app.data.model.CatalogArticle
import com.habizy.app.data.model.ColocationMemberResponse
import com.habizy.app.data.model.ShoppingItemResponse
import com.habizy.app.data.remote.ApiClient
import com.habizy.app.data.repository.AuthRepository
import com.habizy.app.data.repository.CatalogRepository
import com.habizy.app.data.repository.ColocationRepository
import com.habizy.app.data.repository.ShoppingRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ShoppingViewModel(application: Application) : AndroidViewModel(application) {

    private val tokenManager = TokenManager(application)
    private val api = ApiClient.apiService
    private val authRepository = AuthRepository(api, tokenManager)
    private val colocationRepository = ColocationRepository(api, tokenManager)
    private val shoppingRepository = ShoppingRepository(api)
    private val catalogRepository = CatalogRepository(api)

    private val _items = MutableStateFlow<List<ShoppingItemResponse>>(emptyList())
    val items: StateFlow<List<ShoppingItemResponse>> = _items.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<String?>(null)
    val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

    private val _members = MutableStateFlow<List<ColocationMemberResponse>>(emptyList())
    val members: StateFlow<List<ColocationMemberResponse>> = _members.asStateFlow()

    private val _catalogArticles = MutableStateFlow<List<CatalogArticle>>(emptyList())
    val catalogArticles: StateFlow<List<CatalogArticle>> = _catalogArticles.asStateFlow()

    private val _categories = MutableStateFlow<List<String>>(emptyList())
    val categories: StateFlow<List<String>> = _categories.asStateFlow()

    private val _isAdmin = MutableStateFlow(false)
    val isAdmin: StateFlow<Boolean> = _isAdmin.asStateFlow()

    val uncheckedItems: List<ShoppingItemResponse>
        get() = _items.value.filter { !it.isChecked }

    val checkedItems: List<ShoppingItemResponse>
        get() = _items.value.filter { it.isChecked }

    init {
        load()
    }

    fun load(isRefresh: Boolean = false, silent: Boolean = false) {
        if (silent && (_isLoading.value || _isRefreshing.value)) return
        viewModelScope.launch {
            if (!silent) {
                if (isRefresh) _isRefreshing.value = true else _isLoading.value = true
                _errorMessage.value = null
            }

            val colocationId = tokenManager.getColocationId()
            if (colocationId == null) {
                if (!silent) {
                    _errorMessage.value = "Aucune colocation"
                    if (isRefresh) _isRefreshing.value = false else _isLoading.value = false
                }
                return@launch
            }

            val meDeferred = async { authRepository.getMe() }
            val itemsDeferred = async { shoppingRepository.getList(colocationId) }
            val colocationDeferred = async { colocationRepository.getMyColocation() }
            val catalogDeferred = async { catalogRepository.getArticles(colocationId) }
            val categoriesDeferred = async { catalogRepository.getCategories(colocationId) }

            val me = meDeferred.await().getOrNull()
            if (me != null) _isAdmin.value = me.isAdmin

            itemsDeferred.await()
                .onSuccess { _items.value = it }
                .onFailure { e -> if (!silent) _errorMessage.value = e.message ?: "Erreur chargement liste" }

            colocationDeferred.await().onSuccess { _members.value = it.members }
            catalogDeferred.await().onSuccess { _catalogArticles.value = it }
            categoriesDeferred.await().onSuccess { _categories.value = it }

            if (!silent) {
                if (isRefresh) _isRefreshing.value = false else _isLoading.value = false
            }
        }
    }

    fun refresh() = load(isRefresh = true)
    fun silentRefresh() = load(silent = true)

    fun addItem(name: String, quantity: Int, assigneeId: String? = null) {
        viewModelScope.launch {
            _errorMessage.value = null

            val colocationId = tokenManager.getColocationId()
            if (colocationId == null) {
                _errorMessage.value = "Aucune colocation"
                return@launch
            }

            val result = shoppingRepository.addItem(name, quantity, assigneeId, colocationId)
            result.onSuccess { newItem ->
                _items.value = _items.value + newItem
                _successMessage.value = "Article ajoute"
            }.onFailure { e ->
                _errorMessage.value = e.message ?: "Erreur ajout article"
            }
        }
    }

    fun toggle(itemId: String) {
        viewModelScope.launch {
            _errorMessage.value = null

            val result = shoppingRepository.toggle(itemId)
            result.onSuccess { updated ->
                _items.value = _items.value.map { item ->
                    if (item.id == itemId) updated else item
                }
            }.onFailure { e ->
                _errorMessage.value = e.message ?: "Erreur mise à jour article"
            }
        }
    }

    fun delete(itemId: String) {
        viewModelScope.launch {
            _errorMessage.value = null

            val result = shoppingRepository.delete(itemId)
            result.onSuccess {
                _items.value = _items.value.filter { it.id != itemId }
                _successMessage.value = "Article supprime"
            }.onFailure { e ->
                _errorMessage.value = e.message ?: "Erreur suppression article"
            }
        }
    }

    fun addCatalogArticle(name: String, category: String) {
        viewModelScope.launch {
            _errorMessage.value = null

            val colocationId = tokenManager.getColocationId()
            if (colocationId == null) {
                _errorMessage.value = "Aucune colocation"
                return@launch
            }

            val result = catalogRepository.createArticle(name, category, colocationId)
            result.onSuccess { newArticle ->
                _catalogArticles.value = _catalogArticles.value + newArticle
                _successMessage.value = "Article catalogue ajoute"
            }.onFailure { e ->
                _errorMessage.value = e.message ?: "Erreur ajout article catalogue"
            }
        }
    }

    fun deleteCatalogArticle(articleId: String) {
        viewModelScope.launch {
            _errorMessage.value = null

            val result = catalogRepository.deleteArticle(articleId)
            result.onSuccess {
                _catalogArticles.value = _catalogArticles.value.filter { it.id != articleId }
                _successMessage.value = "Article catalogue supprime"
            }.onFailure { e ->
                _errorMessage.value = e.message ?: "Erreur suppression article catalogue"
            }
        }
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    fun clearSuccessMessage() {
        _successMessage.value = null
    }
}
