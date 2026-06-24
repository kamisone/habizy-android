package com.habizy.app.data.model

// -- Requests --

data class CreateCatalogArticleRequest(
    val name: String,
    val category: String,
    val colocationId: String
)

// -- Responses --

data class CatalogArticle(
    val id: String,
    val name: String,
    val category: String,
    val colocationId: String,
    val createdAt: String? = null
)
