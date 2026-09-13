package com.gujaratpost.app.utils

object Constants {
    // Default base URL for Android Emulator pointing to local Node.js backend (Port 5000)
    // For a physical device on local Wi-Fi, replace with your PC IP (e.g., "http://192.168.1.X:5000/")
    // For production backend, use your deployed domain (e.g., "https://api.gujaratpost.com/")
    const val DEFAULT_BASE_URL = "http://10.0.2.2:5000/"

    // Intent extras
    const val EXTRA_ARTICLE_ID = "extra_article_id"
    const val EXTRA_ARTICLE_SLUG = "extra_article_slug"
    const val EXTRA_ARTICLE_TITLE = "extra_article_title"
    const val EXTRA_CATEGORY_SLUG = "extra_category_slug"
    const val EXTRA_CATEGORY_NAME = "extra_category_name"

    // Preferences keys
    const val PREFS_NAME = "gujarat_post_prefs"
    const val KEY_SAVED_ARTICLES = "saved_articles_json"
}
