package com.habizy.app.data.repository

import com.habizy.app.data.model.*
import com.habizy.app.data.remote.ApiService

class CatalogRepository(private val api: ApiService) {

    suspend fun getArticles(colocationId: String): Result<List<CatalogArticle>> = runCatching {
        api.getCatalogArticles(colocationId)
    }

    suspend fun getCategories(colocationId: String): Result<List<String>> = runCatching {
        api.getCatalogCategories(colocationId)
    }

    suspend fun createArticle(
        name: String,
        category: String,
        colocationId: String
    ): Result<CatalogArticle> = runCatching {
        api.createCatalogArticle(CreateCatalogArticleRequest(name, category, colocationId))
    }

    suspend fun deleteArticle(id: String): Result<Unit> = runCatching {
        api.deleteCatalogArticle(id)
    }
}
