package com.gujaratpost.app.utils

object Constants {
    // Public live tunnel endpoint (works anywhere on 4G/5G, Wi-Fi, across any network)
    const val TUNNEL_LIVE_URL = "https://gujaratpost-news-api.loca.lt/"
    const val LOCAL_WIFI_URL = "http://192.168.1.16:5000/"
    const val LOCAL_FRONTEND_URL = "http://192.168.1.16:3000/"
    const val LOCAL_EMULATOR_URL = "http://10.0.2.2:5000/"
    const val PREV_WIFI_URL = "http://10.110.59.96:5000/"
    const val CLOUD_PRODUCTION_URL = "https://gujaratpost.vercel.app/"

    // Primary default URL: Live tunnel for guaranteed connectivity everywhere
    const val DEFAULT_BASE_URL = TUNNEL_LIVE_URL
    const val WEB_BASE_URL = CLOUD_PRODUCTION_URL

    // Ordered list of candidate backend URLs for automatic failover discovery
    val CANDIDATE_BASE_URLS = listOf(
        TUNNEL_LIVE_URL,
        LOCAL_WIFI_URL,
        LOCAL_FRONTEND_URL,
        CLOUD_PRODUCTION_URL
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

