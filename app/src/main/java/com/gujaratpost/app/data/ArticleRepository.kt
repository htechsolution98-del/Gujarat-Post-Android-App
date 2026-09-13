package com.gujaratpost.app.data

import com.gujaratpost.app.data.models.Article

object ArticleRepository {
    var currentArticles: List<Article> = emptyList()
    var currentPosition: Int = 0
}
