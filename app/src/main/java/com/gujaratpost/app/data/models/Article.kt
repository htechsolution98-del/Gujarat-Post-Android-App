package com.gujaratpost.app.data.models

import com.google.gson.annotations.SerializedName

data class Article(
    @SerializedName("id")
    val id: String,

    @SerializedName("slug")
    val slug: String,

    @SerializedName("articleNumber")
    val articleNumber: Long? = null,

    @SerializedName("language")
    val language: String? = "gu",

    @SerializedName("title")
    val title: String,

    @SerializedName("titleGu")
    val titleGu: String? = null,

    @SerializedName("titleHi")
    val titleHi: String? = null,

    @SerializedName("excerpt")
    val excerpt: String? = null,

    @SerializedName("excerptGu")
    val excerptGu: String? = null,

    @SerializedName("excerptHi")
    val excerptHi: String? = null,

    @SerializedName("content")
    val content: String? = null,

    @SerializedName("contentGu")
    val contentGu: String? = null,

    @SerializedName("contentHi")
    val contentHi: String? = null,

    @SerializedName("featuredImage")
    val featuredImage: String? = null,

    @SerializedName("isTrending")
    val isTrending: Boolean = false,

    @SerializedName("isBreaking")
    val isBreaking: Boolean = false,

    @SerializedName("isFeatured")
    val isFeatured: Boolean = false,

    @SerializedName("viewCount")
    val viewCount: Long = 0,

    @SerializedName("createdAt")
    val createdAt: String? = null,

    @SerializedName("publishedAt")
    val publishedAt: String? = null,

    @SerializedName("category")
    val category: Category? = null
) {
    /**
     * Resolves the article headline in Gujarati if available, else standard title.
     */
    val displayTitle: String
        get() = titleGu?.takeIf { it.isNotBlank() } ?: title

    /**
     * Resolves the excerpt in Gujarati if available, else standard excerpt.
     */
    val displayExcerpt: String
        get() = excerptGu?.takeIf { it.isNotBlank() } ?: excerpt.orEmpty()

    /**
     * Resolves full article body in Gujarati if available, else standard content.
     */
    val displayContent: String
        get() = contentGu?.takeIf { it.isNotBlank() } ?: content ?: displayExcerpt

    /**
     * Display category badge text
     */
    val categoryName: String
        get() = category?.displayName ?: "સમાચાર"
}

data class ArticlesResponseData(
    @SerializedName("articles")
    val articles: List<Article>,

    @SerializedName("pagination")
    val pagination: Pagination? = null
)

data class Pagination(
    @SerializedName("total")
    val total: Int,

    @SerializedName("page")
    val page: Int,

    @SerializedName("limit")
    val limit: Int,

    @SerializedName("totalPages")
    val totalPages: Int
)
