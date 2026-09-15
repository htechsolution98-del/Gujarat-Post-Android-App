package com.gujaratpost.app.utils

object Constants {
    // Current primary development server IP (User's active Wi-Fi LAN IP)
    const val LOCAL_WIFI_URL = "http://10.110.59.96:5000/"
    const val LOCAL_EMULATOR_URL = "http://10.0.2.2:5000/"
    const val LOCALHOST_URL = "http://127.0.0.1:5000/"
    const val CLOUD_PRODUCTION_URL = "https://gujaratpost.vercel.app/"

    // Default primary endpoint: Try Wi-Fi backend first for immediate local testing
    const val DEFAULT_BASE_URL = LOCAL_WIFI_URL
    const val WEB_BASE_URL = CLOUD_PRODUCTION_URL

    // Ordered list of candidate backend URLs for automatic failover discovery
    val CANDIDATE_BASE_URLS = listOf(
        LOCAL_WIFI_URL,
        LOCAL_EMULATOR_URL,
        CLOUD_PRODUCTION_URL,
        LOCALHOST_URL
    )

    // Shared Preferences
    const val PREFS_NAME = "gujarat_post_prefs"
    const val KEY_CUSTOM_SERVER_URL = "key_custom_server_url"
    const val KEY_ACTIVE_SERVER_URL = "key_active_server_url"
    const val KEY_SAVED_ARTICLES = "saved_articles_json"

    // Intent extras
    const val EXTRA_ARTICLE_ID = "extra_article_id"
    const val EXTRA_ARTICLE_SLUG = "extra_article_slug"
    const val EXTRA_ARTICLE_TITLE = "extra_article_title"
    const val EXTRA_CATEGORY_SLUG = "extra_category_slug"
    const val EXTRA_CATEGORY_NAME = "extra_category_name"
}

