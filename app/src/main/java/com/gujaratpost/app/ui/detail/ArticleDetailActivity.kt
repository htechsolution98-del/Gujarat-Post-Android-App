package com.gujaratpost.app.ui.detail

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.gujaratpost.app.R
import com.gujaratpost.app.data.ArticleRepository
import com.gujaratpost.app.data.api.RetrofitClient
import com.gujaratpost.app.data.models.Article
import com.gujaratpost.app.databinding.ActivityArticleDetailBinding
import com.gujaratpost.app.utils.BookmarkManager
import com.gujaratpost.app.utils.Constants
import kotlinx.coroutines.launch

class ArticleDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityArticleDetailBinding
    private lateinit var pagerAdapter: ArticlePagerAdapter
    private var articlesList: List<Article> = emptyList()
    private var currentIndex: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityArticleDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val initialPosition = intent.getIntExtra("EXTRA_ARTICLE_POSITION", ArticleRepository.currentPosition)
        articlesList = ArticleRepository.currentArticles

        // Fallback: If opened without repository list, create a single article from intent extras
        if (articlesList.isEmpty()) {
            val articleId = intent.getStringExtra(Constants.EXTRA_ARTICLE_ID).orEmpty()
            val articleSlug = intent.getStringExtra(Constants.EXTRA_ARTICLE_SLUG).orEmpty()
            val articleTitle = intent.getStringExtra(Constants.EXTRA_ARTICLE_TITLE).orEmpty()
            val single = Article(
                id = articleId,
                slug = articleSlug,
                title = articleTitle,
                titleGu = articleTitle
            )
            articlesList = listOf(single)
        }

        setupToolbar()
        setupViewPager(initialPosition.coerceIn(0, (articlesList.size - 1).coerceAtLeast(0)))
        setupActions()
    }

    private fun setupToolbar() {
        binding.btnBackDetail.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupViewPager(startPos: Int) {
        pagerAdapter = ArticlePagerAdapter(articlesList)
        binding.viewPagerArticles.adapter = pagerAdapter
        binding.viewPagerArticles.offscreenPageLimit = 1
        binding.viewPagerArticles.clipChildren = false
        binding.viewPagerArticles.clipToPadding = false
        (binding.viewPagerArticles.getChildAt(0) as? androidx.recyclerview.widget.RecyclerView)?.apply {
            clipChildren = false
            clipToPadding = false
        }
        binding.viewPagerArticles.setPageTransformer(NewspaperPageTransformer())
        binding.viewPagerArticles.setCurrentItem(startPos, false)
        currentIndex = startPos

        updatePageIndicator(startPos)
        updateBookmarkIcon(startPos)

        binding.viewPagerArticles.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                currentIndex = position
                updatePageIndicator(position)
                updateBookmarkIcon(position)
                prefetchArticleDetail(position)
            }
        })

        // Fetch detail for first item
        prefetchArticleDetail(startPos)
    }

    private fun updatePageIndicator(position: Int) {
        binding.tvPageIndicator.text = "${position + 1} / ${articlesList.size}"
    }

    private fun updateBookmarkIcon(position: Int) {
        val article = articlesList.getOrNull(position) ?: return
        val isBookmarked = BookmarkManager.isBookmarked(this, article.id)
        val iconRes = if (isBookmarked) R.drawable.ic_bookmark_filled else R.drawable.ic_bookmark
        binding.btnBookmark.setImageResource(iconRes)
    }

    private fun setupActions() {
        // Bookmark Toggle
        binding.btnBookmark.setOnClickListener {
            val article = articlesList.getOrNull(currentIndex) ?: return@setOnClickListener
            val isNowSaved = BookmarkManager.toggleBookmark(this, article)
            val iconRes = if (isNowSaved) R.drawable.ic_bookmark_filled else R.drawable.ic_bookmark
            binding.btnBookmark.setImageResource(iconRes)

            val toastMsg = if (isNowSaved) {
                getString(R.string.article_saved_toast)
            } else {
                getString(R.string.article_unsaved_toast)
            }
            Toast.makeText(this, toastMsg, Toast.LENGTH_SHORT).show()
        }

        // WhatsApp / Native Share
        binding.btnShare.setOnClickListener {
            val article = articlesList.getOrNull(currentIndex) ?: return@setOnClickListener
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

    private fun prefetchArticleDetail(position: Int) {
        val article = articlesList.getOrNull(position) ?: return
        val slugOrId = article.slug.ifBlank { article.id }
        if (slugOrId.isBlank()) return

        lifecycleScope.launch {
            try {
                // Call API with the ArticleDetailResponseData wrapper
                val response = RetrofitClient.apiService.getArticleDetail(slugOrId)
                if (response.isSuccessful && response.body()?.success == true) {
                    val fullArticle = response.body()?.data?.article
                    if (fullArticle != null) {
                        val updatedList = articlesList.toMutableList()
                        if (position in 0 until updatedList.size) {
                            updatedList[position] = fullArticle
                            articlesList = updatedList
                            ArticleRepository.currentArticles = updatedList
                        }
                        if (::pagerAdapter.isInitialized) {
                            pagerAdapter.updateArticleAt(position, fullArticle)
                        }
                    }
                }
            } catch (e: Exception) {
                // Fallback silently to already loaded article data
            }
        }
    }
}
