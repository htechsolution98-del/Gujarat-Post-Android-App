package com.gujaratpost.app.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gujaratpost.app.R
import com.gujaratpost.app.data.ArticleRepository
import com.gujaratpost.app.data.api.RetrofitClient
import com.gujaratpost.app.data.models.Article
import com.gujaratpost.app.data.models.Category
import com.gujaratpost.app.databinding.FragmentHomeBinding
import com.gujaratpost.app.ui.MainActivity
import com.gujaratpost.app.ui.category.CategoryAdapter
import com.gujaratpost.app.ui.detail.ArticleDetailActivity
import com.gujaratpost.app.utils.Constants
import com.gujaratpost.app.utils.NewsCacheManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // Adapters
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var articleAdapter: ArticleAdapter
    private lateinit var breakingHeaderAdapter: BreakingNewsHeaderAdapter
    private lateinit var heroHeaderAdapter: HeroArticleAdapter
    private lateinit var sectionHeaderAdapter: SectionHeaderAdapter
    private lateinit var footerLoadingAdapter: FooterLoadingAdapter

    // State
    private var selectedCategorySlug: String? = null
    private var currentHeroArticle: Article? = null
    private var currentFeedArticles: List<Article> = emptyList()

    // Pagination & Search state
    private var currentPage: Int = 1
    private var isLoadingMore: Boolean = false
    private var hasMorePages: Boolean = true
    private var currentSearchQuery: String? = null

    // Breaking news rotator state
    private var breakingArticles: List<Article> = emptyList()
    private var currentBreakingIndex: Int = 0
    private var breakingRotatorJob: Job? = null

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

        // 1. Initialize pinned category ticker bar immediately with defaults
        setupCategoriesBar()

        // 2. Initialize unified virtualized RecyclerView feed with ConcatAdapter
        setupRecyclerView()
        setupSwipeRefresh()
        setupInfiniteScroll()

        // 3. Instant offline cache load
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
        breakingHeaderAdapter = BreakingNewsHeaderAdapter { article ->
            openArticleDetail(article)
        }
        heroHeaderAdapter = HeroArticleAdapter { article ->
            openArticleDetail(article)
        }
        sectionHeaderAdapter = SectionHeaderAdapter()
        footerLoadingAdapter = FooterLoadingAdapter()

        // ConcatAdapter joins all components into a single virtualized scrolling container
        val concatAdapter = ConcatAdapter(
            breakingHeaderAdapter,
            heroHeaderAdapter,
            sectionHeaderAdapter,
            articleAdapter,
            footerLoadingAdapter
        )

        binding.rvArticles.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = concatAdapter
            setHasFixedSize(true)
            setItemViewCacheSize(10)
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

    private fun setupInfiniteScroll() {
        binding.rvArticles.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy <= 0) return // Only check on downward scroll

                val layoutManager = recyclerView.layoutManager as? LinearLayoutManager ?: return
                val totalItemCount = layoutManager.itemCount
                val lastVisibleItem = layoutManager.findLastVisibleItemPosition()

                // Guard against duplicate / concurrent loads
                if (!isLoadingMore && hasMorePages && totalItemCount > 0 && lastVisibleItem >= totalItemCount - 4) {
                    loadNextPage()
                }
            }
        })
    }

    private fun loadCachedData() {
        try {
            val cached = NewsCacheManager.getCachedArticles(requireContext())
            if (cached.isNotEmpty()) {
                currentHeroArticle = cached[0]
                heroHeaderAdapter.submitHeroArticle(cached[0])

                val feed: List<Article> = if (cached.size > 1) cached.subList(1, cached.size) else emptyList()
                currentFeedArticles = feed
                articleAdapter.submitList(feed)

                sectionHeaderAdapter.setTitle(getString(R.string.latest_news_title))
                binding.progressLoading.visibility = View.GONE
                binding.layoutError.visibility = View.GONE
                updateRepositoryArticles()
            }
        } catch (e: Exception) {
            // Ignore cache read failures
        }
    }

    fun filterByCategory(categorySlug: String?, categoryName: String? = null) {
        currentSearchQuery = null
        val targetSlug = if (categorySlug.isNullOrBlank() || categorySlug == "all") null else categorySlug
        selectedCategorySlug = targetSlug
        currentPage = 1
        hasMorePages = true
        footerLoadingAdapter.setState(loading = false, showNoMore = false)

        if (_binding != null) {
            val lookupSlug = categorySlug ?: "all"

            val pos = categoryAdapter.selectCategoryBySlug(lookupSlug)
            if (pos >= 0) {
                binding.rvCategories.smoothScrollToPosition(pos)
            }

            (activity as? MainActivity)?.highlightDrawerCategoryBySlug(lookupSlug)

            val headerText = if (!categoryName.isNullOrBlank() && lookupSlug != "all") {
                "$categoryName સમાચાર"
            } else {
                getString(R.string.latest_news_title)
            }
            sectionHeaderAdapter.setTitle(headerText)

            binding.progressLoading.visibility = View.VISIBLE
            binding.layoutError.visibility = View.GONE

            lifecycleScope.launch {
                fetchArticles(selectedCategorySlug, page = 1, isAppend = false)
                binding.progressLoading.visibility = View.GONE
                binding.swipeRefresh.isRefreshing = false
            }
        }
    }

    fun searchArticles(query: String) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) {
            filterByCategory("all", "બધા")
            return
        }

        currentSearchQuery = trimmed
        selectedCategorySlug = null
        currentPage = 1
        hasMorePages = true
        currentHeroArticle = null
        heroHeaderAdapter.submitHeroArticle(null)
        footerLoadingAdapter.setState(loading = false, showNoMore = false)

        sectionHeaderAdapter.setTitle("શોધ પરિણામ: \"$trimmed\"")
        binding.progressLoading.visibility = View.VISIBLE
        binding.layoutError.visibility = View.GONE

        lifecycleScope.launch {
            fetchArticles(null, page = 1, isAppend = false, query = trimmed)
            binding.progressLoading.visibility = View.GONE
            binding.swipeRefresh.isRefreshing = false
        }
    }

    private fun loadData(isPullToRefresh: Boolean) {
        if (!isPullToRefresh && articleAdapter.itemCount == 0 && currentHeroArticle == null) {
            binding.progressLoading.visibility = View.VISIBLE
        }

        currentPage = 1
        hasMorePages = true
        footerLoadingAdapter.setState(loading = false, showNoMore = false)

        lifecycleScope.launch {
            fetchArticles(selectedCategorySlug, page = 1, isAppend = false, query = currentSearchQuery)
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

    private fun loadNextPage() {
        if (isLoadingMore || !hasMorePages || binding.progressLoading.visibility == View.VISIBLE) {
            return
        }

        isLoadingMore = true
        footerLoadingAdapter.setState(loading = true, showNoMore = false)

        val nextPage = currentPage + 1
        lifecycleScope.launch {
            fetchArticles(selectedCategorySlug, page = nextPage, isAppend = true, query = currentSearchQuery)
            isLoadingMore = false
            footerLoadingAdapter.setState(loading = false, showNoMore = !hasMorePages && currentFeedArticles.isNotEmpty())
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
            val response = RetrofitClient.apiService.getBreakingArticles(isBreaking = true, limit = 6)
            if (response.isSuccessful && response.body()?.success == true) {
                val breaking = response.body()?.data?.articles.orEmpty()
                if (breaking.isNotEmpty()) {
                    breakingArticles = breaking
                    startBreakingRotator()
                } else {
                    breakingHeaderAdapter.submitBreakingArticles(emptyList())
                }
            }
        } catch (e: Exception) {
            breakingHeaderAdapter.submitBreakingArticles(emptyList())
        }
    }

    private fun startBreakingRotator() {
        breakingRotatorJob?.cancel()
        if (breakingArticles.isEmpty() || _binding == null) return

        currentBreakingIndex = 0
        breakingHeaderAdapter.submitBreakingArticles(breakingArticles)

        breakingRotatorJob = lifecycleScope.launch {
            while (isActive && breakingArticles.isNotEmpty()) {
                breakingHeaderAdapter.rotateIndex(currentBreakingIndex)
                delay(4500)
                currentBreakingIndex = (currentBreakingIndex + 1) % breakingArticles.size
            }
        }
    }

    private suspend fun fetchArticles(
        categorySlug: String?,
        page: Int,
        isAppend: Boolean,
        query: String? = null
    ) {
        try {
            val response = RetrofitClient.apiService.getArticles(
                page = page,
                limit = 20,
                categorySlug = if (categorySlug == "all") null else categorySlug,
                query = query,
                sort = "latest"
            )

            if (response.isSuccessful && response.body()?.success == true) {
                val newArticles = response.body()?.data?.articles.orEmpty()

                if (isAppend) {
                    if (newArticles.isNotEmpty()) {
                        currentPage = page
                        val updatedFeed = currentFeedArticles.toMutableList().apply {
                            addAll(newArticles)
                        }
                        currentFeedArticles = updatedFeed
                        articleAdapter.submitList(updatedFeed)
                        updateRepositoryArticles()
                        footerLoadingAdapter.setState(loading = false, showNoMore = false)
                    } else {
                        hasMorePages = false
                        footerLoadingAdapter.setState(loading = false, showNoMore = true)
                    }
                } else {
                    // Fresh page 1
                    currentPage = 1
                    if (newArticles.isNotEmpty()) {
                        binding.layoutError.visibility = View.GONE

                        if (query.isNullOrBlank() && (categorySlug == null || categorySlug == "all")) {
                            context?.let { ctx ->
                                NewsCacheManager.saveCachedArticles(ctx, newArticles)
                            }
                        }

                        if (query.isNullOrBlank()) {
                            val heroArticle = newArticles[0]
                            currentHeroArticle = heroArticle
                            heroHeaderAdapter.submitHeroArticle(heroArticle)
                            val feedArticles = if (newArticles.size > 1) newArticles.subList(1, newArticles.size) else emptyList()
                            currentFeedArticles = feedArticles
                            articleAdapter.submitList(feedArticles)
                            sectionHeaderAdapter.setTitle(getString(R.string.latest_news_title))
                        } else {
                            currentHeroArticle = null
                            heroHeaderAdapter.submitHeroArticle(null)
                            currentFeedArticles = newArticles
                            articleAdapter.submitList(newArticles)
                            sectionHeaderAdapter.setTitle("શોધ પરિણામો: \"$query\" (${newArticles.size} સમાચાર)")
                        }
                        updateRepositoryArticles()
                        footerLoadingAdapter.setState(loading = false, showNoMore = false)
                    } else {
                        currentHeroArticle = null
                        heroHeaderAdapter.submitHeroArticle(null)
                        currentFeedArticles = emptyList()
                        articleAdapter.submitList(emptyList())
                        binding.layoutError.visibility = View.VISIBLE
                        binding.tvErrorMessage.text = if (!query.isNullOrBlank()) {
                            "\"$query\" માટે કોઈ સમાચાર મળ્યા નથી."
                        } else if (categorySlug != null && categorySlug != "all") {
                            "આ કૅટેગરીમાં હાલ કોઈ સમાચાર નથી."
                        } else {
                            getString(R.string.no_articles_found)
                        }
                    }
                }
            } else {
                if (!isAppend) {
                    handleInitialLoadError(categorySlug)
                }
            }
        } catch (e: Exception) {
            if (!isAppend) {
                handleInitialLoadError(categorySlug)
            }
        }
    }

    private fun updateRepositoryArticles() {
        val allArticles = mutableListOf<Article>()
        currentHeroArticle?.let { allArticles.add(it) }
        allArticles.addAll(currentFeedArticles)
        ArticleRepository.currentArticles = allArticles
    }

    private fun handleInitialLoadError(categorySlug: String?) {
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
            heroHeaderAdapter.submitHeroArticle(null)
            currentFeedArticles = emptyList()
            articleAdapter.submitList(emptyList())
            sectionHeaderAdapter.setTitle(null)
            binding.layoutError.visibility = View.VISIBLE
            binding.tvErrorMessage.text = "આ કૅટેગરીના સમાચાર મેળવી શકાયા નથી."
        }
    }

    private fun setupFallbackFeed(fallback: List<Article>) {
        if (fallback.isNotEmpty()) {
            binding.layoutError.visibility = View.GONE
            val hero = fallback[0]
            currentHeroArticle = hero
            heroHeaderAdapter.submitHeroArticle(hero)

            val feed = if (fallback.size > 1) fallback.subList(1, fallback.size) else emptyList()
            currentFeedArticles = feed
            articleAdapter.submitList(feed)
            sectionHeaderAdapter.setTitle(getString(R.string.latest_news_title))
            updateRepositoryArticles()
        }
    }

    private fun openArticleDetail(article: Article) {
        updateRepositoryArticles()
        val allArticles = ArticleRepository.currentArticles
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
        breakingRotatorJob?.cancel()
        _binding = null
    }
}
