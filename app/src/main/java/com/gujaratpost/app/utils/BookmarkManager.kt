package com.gujaratpost.app.utils

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.gujaratpost.app.data.models.Article

object BookmarkManager {

    private const val PREFS_NAME = "gujarat_post_bookmarks"
    private const val KEY_BOOKMARKS = "saved_articles_json"
    private val gson = Gson()

    fun getSavedArticles(context: Context): List<Article> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_BOOKMARKS, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<Article>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun isBookmarked(context: Context, articleId: String): Boolean {
        val saved = getSavedArticles(context)
        return saved.any { it.id == articleId }
    }

    fun toggleBookmark(context: Context, article: Article): Boolean {
        val list = getSavedArticles(context).toMutableList()
        val index = list.indexOfFirst { it.id == article.id }
        val isNowBookmarked: Boolean

        if (index >= 0) {
            list.removeAt(index)
            isNowBookmarked = false
        } else {
            list.add(0, article)
            isNowBookmarked = true
        }

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_BOOKMARKS, gson.toJson(list)).apply()
        return isNowBookmarked
    }

    fun removeArticle(context: Context, articleId: String) {
        val list = getSavedArticles(context).toMutableList()
        list.removeAll { it.id == articleId }
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_BOOKMARKS, gson.toJson(list)).apply()
    }
}
