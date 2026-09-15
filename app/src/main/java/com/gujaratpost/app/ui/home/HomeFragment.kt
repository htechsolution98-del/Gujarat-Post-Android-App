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
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
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
import com.gujaratpost.app.utils.NewsCacheManager
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var categoryAdapter: CategoryAdapter
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

        // 0. Restore saved server preferences if any
        RetrofitClient.initFromPreferences(requireContext())

        // 1. Initialize category bar immediately with defaults so it never flashes empty
        setupCategoriesBar()

        // 2. Initialize article feed
        setupRecyclerView()
        setupSwipeRefresh()

        // 3. Instant offline cache load (zero-wait display)
        loadCachedData()

        // 4. Fresh live network fetch
        loadData(isPullToRefresh = false)
    }

    fun reloadCurrentFeed() {
        if (_binding != null) {
            loadData(isPullToRefresh = true)
        }
    }

    private fun getDefaultCategories(): List<Category> {
        return listOf(
            Category(id = "all", name = "All", nameGu = getString(R.string.cat_all), slug = "all"),
            Category(id = "cat-breaking", name = "Breaking News", nameGu = getString(R.string.breaking_news_badge), slug = "breaking-news"),
            Category(id = "cat-gujarat", name = "Gujarat", nameGu = getString(R.string.cat_gujarat), slug = "gujarat"),
            Category(id = "cat-sports", name = "Sports", nameGu = getString(R.string.cat_sports), slug = "sports"),
            Category(id = "cat-business", name = "Business", nameGu = getString(R.string.cat_business), slug = "business"),
            Category(id = "cat-national", name = "National", nameGu = getString(R.string.cat_national), slug = "national"),
            Category(id = "cat-world", name = "World", nameGu = getString(R.string.cat_world), slug = "world"),
            Category(id = "cat-entertainment", name = "Entertainment", nameGu = getString(R.string.cat_entertainment), slug = "entertainment")
        )
    }

    private fun setupCategoriesBar() {
        val initialList = getDefaultCategories()
        categoryAdapter = CategoryAdapter(initialList) { category ->
            val slug = category?.slug ?: "all"
            val name = category?.displayName ?: "બધા"
            filterByCategory(slug, name)
        }
        binding.rvCategories.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = categoryAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupRecyclerView() {
        articleAdapter = ArticleAdapter { article ->
            openArticleDetail(article)
        }
        binding.rvArticles.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = articleAdapter
            isNestedScrollingEnabled = false
            setItemViewCacheSize(20)
        }

        binding.btnRetry.setOnClickListener {
            binding.layoutError.visibility = View.GONE
            loadData(isPullToRefresh = false)
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setColorSchemeResources(R.color.brand_primary)
        binding.swipeRefresh.setOnRefreshListener {
            loadData(isPullToRefresh = true)
        }
    }

    private fun loadCachedData() {
        try {
            val cached = NewsCacheManager.getCachedArticles(requireContext())
            if (cached.isNotEmpty()) {
                currentHeroArticle = cached[0]
                setupHeroFeaturedCard(cached[0])

                val feed: List<Article> = if (cached.size > 1) cached.subList(1, cached.size) else emptyList()
                currentFeedArticles = feed
                articleAdapter.submitList(feed)

                binding.tvSectionHeader.visibility = View.VISIBLE
                binding.progressLoading.visibility = View.GONE
                binding.layoutError.visibility = View.GONE
            }
        } catch (e: Exception) {
            // Ignore cache read failures
        }
    }

    fun filterByCategory(categorySlug: String?, categoryName: String? = null) {
        val targetSlug = if (categorySlug.isNullOrBlank() || categorySlug == "all") null else categorySlug
        selectedCategorySlug = targetSlug
        if (_binding != null) {
            val lookupSlug = categorySlug ?: "all"

            // 1. Programmatically highlight chip in horizontal category bar and scroll to it
            val pos = categoryAdapter.selectCategoryBySlug(lookupSlug)
            if (pos >= 0) {
                binding.rvCategories.smoothScrollToPosition(pos)
            }

            // 2. Keep MainActivity drawer highlight in sync
            (activity as? com.gujaratpost.app.ui.MainActivity)?.highlightDrawerCategoryBySlug(lookupSlug)

            // 3. Update section title
            binding.tvSectionHeader.text = if (!categoryName.isNullOrBlank() && lookupSlug != "all") {
                "$categoryName સમાચાર"
            } else {
                getString(R.string.latest_news_title)
            }

            // 4. Fetch filtered articles
            binding.progressLoading.visibility = View.VISIBLE
            binding.layoutError.visibility = View.GONE
            lifecycleScope.launch {
                fetchArticles(selectedCategorySlug)
                binding.progressLoading.visibility = View.GONE
                binding.swipeRefresh.isRefreshing = false
            }
        }
    }

    private fun loadData(isPullToRefresh: Boolean) {
        // Only show spinner if there is no content already on screen
        if (!isPullToRefresh && articleAdapter.itemCount == 0 && currentHeroArticle == null) {
            binding.progressLoading.visibility = View.VISIBLE
        }

        // Independent non-blocking coroutines: articles are prioritized!
        lifecycleScope.launch {
            fetchArticles(selectedCategorySlug)
            binding.progressLoading.visibility = View.GONE
            binding.swipeRefresh.isRefreshing = false
        }

        lifecycleScope.launch {
            fetchCategories()
        }

        lifecycleScope.launch {
            fetchBreakingNews()
        }
    }

    private suspend fun fetchCategories() {
        try {
            val response = RetrofitClient.apiService.getCategories()
            if (response.isSuccessful && response.body()?.success == true) {
                val list = response.body()?.data?.categories.orEmpty()
                if (list.isNotEmpty()) {
                    val allCategory = Category(
                        id = "all",
                        name = "All",
                        nameGu = getString(R.string.cat_all),
                        nameHi = "सभी",
                        slug = "all"
                    )
                    val fullList = mutableListOf(allCategory).apply { addAll(list) }
                    categoryAdapter.updateCategories(fullList, activeSlug = selectedCategorySlug ?: "all")
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
                    binding.tvSectionHeader.visibility = View.VISIBLE

                    // Save to local cache for instant offline loading on next app start
                    if (categorySlug == null || categorySlug == "all") {
                        context?.let { ctx ->
                            NewsCacheManager.saveCachedArticles(ctx, articles)
                        }
                    }

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
                    binding.cardHeroFeatured.visibility = View.GONE
                    currentFeedArticles = emptyList()
                    articleAdapter.submitList(emptyList())
                    binding.tvSectionHeader.visibility = View.GONE
                    binding.layoutError.visibility = View.VISIBLE
                    binding.tvErrorMessage.text = if (categorySlug != null && categorySlug != "all") {
                        "આ કૅટેગરીમાં હાલ કોઈ સમાચાર નથી."
                    } else {
                        getString(R.string.no_articles_found)
                    }
                }
            } else {
                if (categorySlug == null || categorySlug == "all") {
                    val fallback = context?.let { NewsCacheManager.getCachedArticles(it) }.orEmpty()
                    if (fallback.isNotEmpty()) {
                        setupFallbackFeed(fallback)
                    } else {
                        binding.layoutError.visibility = View.VISIBLE
                        binding.tvErrorMessage.text = "સમાચાર લોડ કરી શકાયા નથી. કૃપા કરીને ફરી પ્રયાસ કરો."
                    }
                } else {
                    currentHeroArticle = null
                    binding.cardHeroFeatured.visibility = View.GONE
                    currentFeedArticles = emptyList()
                    articleAdapter.submitList(emptyList())
                    binding.tvSectionHeader.visibility = View.GONE
                    binding.layoutError.visibility = View.VISIBLE
                    binding.tvErrorMessage.text = "આ કૅટેગરીના સમાચાર મેળવી શકાયા નથી."
                }
            }
        } catch (e: Exception) {
            if (categorySlug == null || categorySlug == "all") {
                val fallback = context?.let { NewsCacheManager.getCachedArticles(it) }.orEmpty()
                if (fallback.isNotEmpty()) {
                    setupFallbackFeed(fallback)
                } else {
                    binding.layoutError.visibility = View.VISIBLE
                    binding.tvErrorMessage.text = "ઇન્ટરનેટ કનેક્શન ધીમું છે. કૃપા કરીને ફરી પ્રયાસ કરો."
                }
            } else {
                currentHeroArticle = null
                binding.cardHeroFeatured.visibility = View.GONE
                currentFeedArticles = emptyList()
                articleAdapter.submitList(emptyList())
                binding.tvSectionHeader.visibility = View.GONE
                binding.layoutError.visibility = View.VISIBLE
                binding.tvErrorMessage.text = "ઇન્ટરનેટ કનેક્શન ધીમું છે. કૃપા કરીને ફરી પ્રયાસ કરો."
            }
        }
    }

    private fun setupFallbackFeed(fallback: List<Article>) {
        if (fallback.isNotEmpty()) {
            binding.layoutError.visibility = View.GONE
            binding.tvSectionHeader.visibility = View.VISIBLE
            val hero = fallback[0]
            currentHeroArticle = hero
            setupHeroFeaturedCard(hero)

            val feed = if (fallback.size > 1) fallback.subList(1, fallback.size) else emptyList()
            currentFeedArticles = feed
            articleAdapter.submitList(feed)
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
                .transition(DrawableTransitionOptions.withCrossFade(250))
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
