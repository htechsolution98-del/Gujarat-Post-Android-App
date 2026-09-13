package com.gujaratpost.app.data.models

import com.google.gson.annotations.SerializedName

data class Category(
    @SerializedName("id")
    val id: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("nameGu")
    val nameGu: String?,

    @SerializedName("nameHi")
    val nameHi: String?,

    @SerializedName("slug")
    val slug: String,

    @SerializedName("color")
    val color: String?,

    @SerializedName("order")
    val order: Int = 0,

    @SerializedName("isActive")
    val isActive: Boolean = true
) {
    /**
     * Display name prioritizing Gujarati translation
     */
    val displayName: String
        get() = nameGu?.takeIf { it.isNotBlank() } ?: name
}
