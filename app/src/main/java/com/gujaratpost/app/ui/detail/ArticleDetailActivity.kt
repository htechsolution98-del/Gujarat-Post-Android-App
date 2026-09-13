package com.gujaratpost.app.ui.detail

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.gujaratpost.app.R
import com.gujaratpost.app.data.api.RetrofitClient
import com.gujaratpost.app.data.models.Article
import com.gujaratpost.app.databinding.ActivityArticleDetailBinding
import com.gujaratpost.app.utils.Constants
import com.gujaratpost.app.utils.DateFormatter
import kotlinx.coroutines.launch

class ArticleDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityArticleDetailBinding
    private var currentArticle: Article? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityArticleDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val articleId = intent.getStringExtra(Constants.EXTRA_ARTICLE_ID).orEmpty()
        val articleSlug = intent.getStringExtra(Constants.EXTRA_ARTICLE_SLUG).orEmpty()
        val articleTitle = intent.getStringExtra(Constants.EXTRA_ARTICLE_TITLE).orEmpty()

        setupToolbar(articleTitle)
        setupShareAction()

        val queryParam = articleSlug.ifBlank { articleId }
        if (queryParam.isNotBlank()) {
            loadArticleDetail(queryParam)
        }
    }

    private fun setupToolbar(title: String) {
        binding.toolbarDetail.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        if (title.isNotBlank()) {
            binding.tvDetailTitle.text = title
        }
    }

    private fun setupShareAction() {
        binding.ivShareBtn.setOnClickListener {
            val article = currentArticle ?: return@setOnClickListener
            val shareUrl = "${Constants.WEB_BASE_URL}/news/${article.slug}"
            val shareText = "${article.displayTitle}\n\nવધુ વાંચો ગુજરાત પોસ્ટ પર:\n$shareUrl"

            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, shareText)
                type = "text/plain"
            }
            val shareIntent = Intent.createChooser(sendIntent, getString(R.string.share_via))
            startActivity(shareIntent)
        }
    }

    private fun loadArticleDetail(slugOrId: String) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getArticleDetail(slugOrId)
                if (response.isSuccessful && response.body()?.success == true) {
                    val article = response.body()?.data
                    if (article != null) {
                        currentArticle = article
                        displayArticle(article)
                    }
                }
            } catch (e: Exception) {
                // Fallback: keep existing title
            }
        }
    }

    private fun displayArticle(article: Article) {
        binding.tvDetailTitle.text = article.displayTitle
        binding.tvDetailCategory.text = article.categoryName
        
        val dateStr = DateFormatter.formatIsoDate(article.publishedAt ?: article.createdAt)
        val authorName = article.author?.displayName ?: "ગુજરાત પોસ્ટ બ્યુરો"
        binding.tvDetailDate.text = "$authorName • $dateStr"

        // Parse HTML content or excerpt
        val rawContent = article.displayContent
        val formattedContent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(rawContent, Html.FROM_HTML_MODE_COMPACT)
        } else {
            @Suppress("DEPRECATION")
            Html.fromHtml(rawContent)
        }
        binding.tvDetailContent.text = formattedContent

        // Featured image
        val imageUrl = article.resolvedImageUrl
        if (!imageUrl.isNullOrBlank()) {
            binding.ivDetailImage.visibility = View.VISIBLE
            Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.rounded_card_bg)
                .error(R.drawable.rounded_card_bg)
                .into(binding.ivDetailImage)
        } else {
            binding.ivDetailImage.visibility = View.GONE
        }
    }
}
