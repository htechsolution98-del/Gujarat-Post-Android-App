package com.gujaratpost.app.data.models

import com.google.gson.annotations.SerializedName

data class Category(
    @SerializedName("id")
    val id: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("nameGu")
    val nameGu: String? = null,

    @SerializedName("nameHi")
    val nameHi: String? = null,

    @SerializedName("slug")
    val slug: String,

    @SerializedName("color")
    val color: String? = null,

    @SerializedName("displayOrder")
    val displayOrder: Int = 0,

    @SerializedName("isActive")
    val isActive: Boolean = true
) {
    /**
     * Display name prioritizing Gujarati translation
     */
    val displayName: String
        get() = nameGu?.takeIf { it.isNotBlank() } ?: name
}

data class CategoriesResponseData(
    @SerializedName("categories")
    val categories: List<Category> = emptyList()
)
