package com.gujaratpost.app.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.gujaratpost.app.R
import com.gujaratpost.app.data.ArticleRepository
import com.gujaratpost.app.data.api.RetrofitClient
import com.gujaratpost.app.data.models.Article
import com.gujaratpost.app.data.models.Category
import com.gujaratpost.app.databinding.FragmentHomeBinding
import com.gujaratpost.app.ui.category.CategoryAdapter
import com.gujaratpost.app.ui.detail.ArticleDetailActivity
import com.gujaratpost.app.utils.Constants
import com.gujaratpost.app.utils.DateFormatter
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var articleAdapter: ArticleAdapter
    private var selectedCategorySlug: String? = null
    private var currentHeroArticle: Article? = null
    private var currentFeedArticles: List<Article> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSwipeRefresh()
        loadData(isPullToRefresh = false)
    }

    private fun setupRecyclerView() {
        articleAdapter = ArticleAdapter { article ->
            openArticleDetail(article)
        }
        binding.rvArticles.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = articleAdapter
            isNestedScrollingEnabled = false
        }

        binding.btnRetry.setOnClickListener {
            loadData(isPullToRefresh = false)
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setColorSchemeResources(R.color.brand_primary)
        binding.swipeRefresh.setOnRefreshListener {
            loadData(isPullToRefresh = true)
        }
    }

    fun filterByCategory(categorySlug: String?, categoryName: String? = null) {
        selectedCategorySlug = if (categorySlug == "all") null else categorySlug
        if (_binding != null) {
            binding.tvSectionHeader.text = if (!categoryName.isNullOrBlank()) {
                "$categoryName સમાચાર"
            } else {
                getString(R.string.latest_news_title)
            }
            loadData(isPullToRefresh = true)
        }
    }

    private fun loadData(isPullToRefresh: Boolean) {
        if (!isPullToRefresh) {
            binding.progressLoading.visibility = View.VISIBLE
        }
        binding.layoutError.visibility = View.GONE

        lifecycleScope.launch {
            try {
                val catDeferred = async { fetchCategories() }
                val breakingDeferred = async { fetchBreakingNews() }
                val articlesDeferred = async { fetchArticles(selectedCategorySlug) }

                catDeferred.await()
                breakingDeferred.await()
                articlesDeferred.await()
            } catch (e: Exception) {
                // Individual handlers manage errors
            } finally {
                binding.progressLoading.visibility = View.GONE
                binding.swipeRefresh.isRefreshing = false
            }
        }
    }

    private suspend fun fetchCategories() {
        try {
            val response = RetrofitClient.apiService.getCategories()
            if (response.isSuccessful && response.body()?.success == true) {
                val list = response.body()?.data?.categories.orEmpty()
                val allCategory = Category(
                    id = "all",
                    name = "All",
                    nameGu = getString(R.string.cat_all),
                    nameHi = "सभी",
                    slug = "all"
                )
                val fullList = mutableListOf(allCategory).apply { addAll(list) }

                val catAdapter = CategoryAdapter(fullList) { category ->
                    selectedCategorySlug = category?.slug
                    binding.tvSectionHeader.text = if (category != null && category.slug != "all") {
                        "${category.displayName} સમાચાર"
                    } else {
                        getString(R.string.latest_news_title)
                    }
                    lifecycleScope.launch {
                        binding.swipeRefresh.isRefreshing = true
                        fetchArticles(selectedCategorySlug)
                        binding.swipeRefresh.isRefreshing = false
                    }
                }
                binding.rvCategories.apply {
                    layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                    adapter = catAdapter
                }
            }
        } catch (e: Exception) {
            // Keep default layout on category error
        }
    }

    private suspend fun fetchBreakingNews() {
        try {
            val response = RetrofitClient.apiService.getBreakingArticles(isBreaking = true, limit = 5)
            if (response.isSuccessful && response.body()?.success == true) {
                val breaking = response.body()?.data?.articles.orEmpty()
                if (breaking.isNotEmpty()) {
                    val first = breaking[0]
                    binding.cardBreakingNews.visibility = View.VISIBLE
                    binding.tvBreakingNewsTitle.text = first.displayTitle
                    binding.tvBreakingNewsTitle.isSelected = true // Start marquee
                    binding.cardBreakingNews.setOnClickListener {
                        openArticleDetail(first)
                    }
                } else {
                    binding.cardBreakingNews.visibility = View.GONE
                }
            }
        } catch (e: Exception) {
            binding.cardBreakingNews.visibility = View.GONE
        }
    }

    private suspend fun fetchArticles(categorySlug: String?) {
        try {
            val response = RetrofitClient.apiService.getArticles(
                page = 1,
                limit = 30,
                categorySlug = if (categorySlug == "all") null else categorySlug,
                sort = "latest"
            )

            if (response.isSuccessful && response.body()?.success == true) {
                val articles = response.body()?.data?.articles.orEmpty()

                if (articles.isNotEmpty()) {
                    binding.layoutError.visibility = View.GONE

                    // First article becomes the Hero Featured card
                    val heroArticle = articles[0]
                    currentHeroArticle = heroArticle
                    setupHeroFeaturedCard(heroArticle)

                    // Remaining articles go into the feed
                    val feedArticles: List<Article> = if (articles.size > 1) articles.subList(1, articles.size) else emptyList()
                    currentFeedArticles = feedArticles
                    articleAdapter.submitList(feedArticles)
                } else {
                    currentHeroArticle = null
                    currentFeedArticles = emptyList()
                    binding.cardHeroFeatured.visibility = View.GONE
                    articleAdapter.submitList(emptyList<Article>())
                    binding.layoutError.visibility = View.VISIBLE
                    binding.tvErrorMessage.text = getString(R.string.no_articles_found)
                }
            } else {
                if (articleAdapter.itemCount == 0) {
                    binding.layoutError.visibility = View.VISIBLE
                }
            }
        } catch (e: Exception) {
            if (articleAdapter.itemCount == 0) {
                binding.layoutError.visibility = View.VISIBLE
                binding.tvErrorMessage.text = "સમાચાર લોડ કરી શકાયા નથી: ${e.localizedMessage ?: "ઇન્ટરનેટ કનેક્શન તપાસો"}"
            }
        }
    }

    private fun setupHeroFeaturedCard(article: Article) {
        binding.cardHeroFeatured.visibility = View.VISIBLE
        binding.tvHeroTitle.text = article.displayTitle
        binding.tvHeroExcerpt.text = article.displayExcerpt
        binding.tvHeroCategory.text = article.categoryName
        binding.tvHeroDate.text = DateFormatter.formatIsoDate(article.publishedAt ?: article.createdAt)

        val imageUrl = article.resolvedImageUrl
        if (!imageUrl.isNullOrBlank()) {
            binding.ivHeroImage.visibility = View.VISIBLE
            Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.rounded_card_bg)
                .error(R.drawable.rounded_card_bg)
                .into(binding.ivHeroImage)
        } else {
            binding.ivHeroImage.visibility = View.GONE
        }

        binding.cardHeroFeatured.setOnClickListener {
            openArticleDetail(article)
        }
    }

    private fun openArticleDetail(article: Article) {
        // Collect full active articles list for horizontal swiping
        val allArticles = mutableListOf<Article>()
        currentHeroArticle?.let { allArticles.add(it) }
        allArticles.addAll(currentFeedArticles)

        if (allArticles.isEmpty()) {
            allArticles.add(article)
        }

        ArticleRepository.currentArticles = allArticles
        ArticleRepository.currentPosition = allArticles.indexOfFirst { it.id == article.id }.coerceAtLeast(0)

        val intent = Intent(requireContext(), ArticleDetailActivity::class.java).apply {
            putExtra("EXTRA_ARTICLE_POSITION", ArticleRepository.currentPosition)
            putExtra(Constants.EXTRA_ARTICLE_ID, article.id)
            putExtra(Constants.EXTRA_ARTICLE_SLUG, article.slug)
            putExtra(Constants.EXTRA_ARTICLE_TITLE, article.displayTitle)
        }
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
