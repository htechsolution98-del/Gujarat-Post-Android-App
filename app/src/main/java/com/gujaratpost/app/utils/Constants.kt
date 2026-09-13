package com.gujaratpost.app.utils

object Constants {
    // Production live API endpoint proxying to Render backend
    const val DEFAULT_BASE_URL = "https://gujaratpost.vercel.app/"
    const val WEB_BASE_URL = "https://gujaratpost.vercel.app"

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
