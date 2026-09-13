package com.gujaratpost.app.data.models

import com.google.gson.annotations.SerializedName
import com.gujaratpost.app.utils.Constants

import java.io.Serializable

data class Author(
    @SerializedName("id")
    val id: String? = null,

    @SerializedName("name")
    val name: String? = null,

    @SerializedName("nameGu")
    val nameGu: String? = null,

    @SerializedName("designation")
    val designation: String? = null,

    @SerializedName("designationGu")
    val designationGu: String? = null,

    @SerializedName("image")
    val image: String? = null
) : Serializable {
    val displayName: String
        get() = nameGu?.takeIf { it.isNotBlank() } ?: name ?: "ગુજરાત પોસ્ટ બ્યુરો"
}

data class Article(
    @SerializedName("id")
    val id: String,

    @SerializedName("slug")
    val slug: String,

    @SerializedName("articleNumber")
    val articleNumber: Long? = null,

    @SerializedName("title")
    val title: String? = null,

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

    @SerializedName("image")
    val image: String? = null,

    @SerializedName("featuredImage")
    val featuredImage: String? = null,

    // Backend returns string category name (e.g. "Videos", "World", etc.)
    @SerializedName("category")
    val category: String? = null,

    @SerializedName("categoryGu")
    val categoryGu: String? = null,

    @SerializedName("categoryHi")
    val categoryHi: String? = null,

    @SerializedName("location")
    val location: String? = null,

    @SerializedName("isTrending")
    val isTrending: Boolean = false,

    @SerializedName("isBreaking")
    val isBreaking: Boolean = false,

    @SerializedName("isFeatured")
    val isFeatured: Boolean = false,

    @SerializedName("views")
    val views: Long = 0,

    @SerializedName("viewCount")
    val viewCount: Long = 0,

    @SerializedName("readingTime")
    val readingTime: Int? = 3,

    @SerializedName("createdAt")
    val createdAt: String? = null,

    @SerializedName("publishedAt")
    val publishedAt: String? = null,

    @SerializedName("updatedAt")
    val updatedAt: String? = null,

    @SerializedName("author")
    val author: Author? = null
) : Serializable {
    /**
     * Resolves the article headline in Gujarati if available, else standard title.
     */
    val displayTitle: String
        get() = titleGu?.takeIf { it.isNotBlank() } ?: title.orEmpty()

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
        get() = categoryGu?.takeIf { it.isNotBlank() } ?: category ?: "સમાચાર"

    /**
     * Resolved full HTTP URL for the image
     */
    val resolvedImageUrl: String?
        get() {
            val raw = featuredImage?.takeIf { it.isNotBlank() }
                ?: image?.takeIf { it.isNotBlank() }
                ?: return null

            return when {
                raw.startsWith("http://") || raw.startsWith("https://") -> raw
                raw.startsWith("/") -> "${Constants.WEB_BASE_URL}$raw"
                else -> "${Constants.WEB_BASE_URL}/$raw"
            }
        }
}

data class ArticlesResponseData(
    @SerializedName("articles")
    val articles: List<Article> = emptyList(),

    @SerializedName("total")
    val total: Int = 0,

    @SerializedName("totalPages")
    val totalPages: Int = 1
)

data class ArticleDetailResponseData(
    @SerializedName("article")
    val article: Article? = null
)
