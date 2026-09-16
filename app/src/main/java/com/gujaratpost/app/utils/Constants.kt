package com.gujaratpost.app.utils

import com.gujaratpost.app.BuildConfig

object Constants {
    // Production Cloud HTTPS endpoint
    const val CLOUD_PRODUCTION_URL = "https://gujaratpost.vercel.app/"
    const val WEB_BASE_URL = CLOUD_PRODUCTION_URL

    // Local & debug development endpoints (isolated to debug builds)
    const val LOCAL_EMULATOR_URL = "http://10.0.2.2:5000/"
    const val LOCAL_WIFI_URL = "http://192.168.1.16:5000/"
    const val LOCAL_FRONTEND_URL = "http://192.168.1.16:3000/"
    const val TUNNEL_LIVE_URL = "https://gujaratpost-news-api.loca.lt/"

    // In Release: Strict HTTPS cloud production URL.
    // In Debug: Local development or tunnel endpoint can be used.
    val DEFAULT_BASE_URL: String
        get() = if (BuildConfig.DEBUG) {
            TUNNEL_LIVE_URL
        } else {
            CLOUD_PRODUCTION_URL
        }

    // Failover candidate list:
    // In Release: Only secure HTTPS endpoints (no cleartext, no localhost, no localtunnel).
    // In Debug: Includes local dev endpoints for testing.
    val CANDIDATE_BASE_URLS: List<String>
        get() = if (BuildConfig.DEBUG) {
            listOf(
                TUNNEL_LIVE_URL,
                LOCAL_EMULATOR_URL,
                LOCAL_WIFI_URL,
                CLOUD_PRODUCTION_URL
            )
        } else {
            listOf(
                CLOUD_PRODUCTION_URL
            )
        }

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

