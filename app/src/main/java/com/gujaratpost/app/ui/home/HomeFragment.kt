package com.gujaratpost.app.ui.home

import android.content.Intent
import android.net.Uri
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
import com.gujaratpost.app.data.models.Reel
import com.gujaratpost.app.data.models.Video
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

    // Section Adapters matching website portal structure
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var breakingHeaderAdapter: BreakingNewsHeaderAdapter
    private lateinit var heroCarouselAdapter: HeroCarouselAdapter
    private lateinit var topStoriesAdapter: TopStoriesHeaderAdapter
    private lateinit var reelsAdapter: ReelsHeaderAdapter
    private lateinit var mostReadAdapter: MostReadHeaderAdapter
    private lateinit var videosAdapter: VideosHeaderAdapter
    private lateinit var sectionHeaderAdapter: SectionHeaderAdapter
    private lateinit var articleAdapter: ArticleAdapter
    private lateinit var footerLoadingAdapter: FooterLoadingAdapter

    // State
    private var selectedCategorySlug: String? = null
    private var heroArticles: List<Article> = emptyList()
    private var topStoriesList: List<Article> = emptyList()
    private var mostReadList: List<Article> = emptyList()
    private var currentFeedArticles: List<Article> = emptyList()

    // Pagination & Search state
    private var currentPage: Int = 1
    private var isLoadingMore: Boolean = false
    private var hasMorePages: Boolean = true
    private var currentSearchQuery: String? = null

    // Rotator jobs
    private var breakingArticles: List<Article> = emptyList()
    private var currentBreakingIndex: Int = 0
    private var breakingRotatorJob: Job? = null
    private var heroRotatorJob: Job? = null

    // Fallback data for Reels & Videos to guarantee rich website look even offline
    private val fallbackReels = listOf(
        Reel(
            id = "demo-1",
            heading = "Gujarat Post Daily News Highlights",
            headingGu = "ગુજરાત પોસ્ટ દૈનિક સમાચાર હાઇલાઇટ્સ",
            instaUrl = "https://www.instagram.com/gujaratpost.in/",
            thumbnail = "https://images.unsplash.com/photo-1585829365295-ab7cd400c167?w=600&auto=format&fit=crop&q=80",
            views = 45200L
        ),
        Reel(
            id = "demo-2",
            heading = "Breaking Politics & City News",
            headingGu = "રાજકારણ અને શહેરના તાજા સમાચાર",
            instaUrl = "https://www.instagram.com/gujaratpost.in/",
            thumbnail = "https://images.unsplash.com/photo-1504711434969-e33886168f5c?w=600&auto=format&fit=crop&q=80",
            views = 82100L
        ),
        Reel(
            id = "demo-3",
            heading = "Live Weather & Special Ground Report",
            headingGu = "હવામાન અને ખાસ ગ્રાઉન્ડ રિપોર્ટ",
            instaUrl = "https://www.instagram.com/gujaratpost.in/",
            thumbnail = "https://images.unsplash.com/photo-1495020689067-958852a7765e?w=600&auto=format&fit=crop&q=80",
            views = 63400L
        ),
        Reel(
            id = "demo-4",
            heading = "Gujarat Business & Market Updates",
            headingGu = "ગુજરાત વ્યાપાર અને બજાર સમાચાર",
            instaUrl = "https://www.instagram.com/gujaratpost.in/",
            thumbnail = "https://images.unsplash.com/photo-1526304640581-d334cdbbf45e?w=600&auto=format&fit=crop&q=80",
            views = 38900L
        )
    )

    private val fallbackVideos = listOf(
        Video(
            id = "v-1",
            titleGu = "ગુજરાત પોસ્ટ વિશેષ: રાજ્યના મુખ્ય ઘટનાક્રમ અને વિશ્લેષણ",
            youtubeId = "A_5vL-ngK4M",
            duration = "03:45"
        ),
        Video(
            id = "v-2",
            titleGu = "અમદાવાદ મેગા ડેવલપમેન્ટ પ્રોજેક્ટ ગ્રાઉન્ડ રિપોર્ટ",
            youtubeId = "dQw4w9WgXcQ",
            duration = "02:15"
        ),
        Video(
            id = "v-3",
            titleGu = "સુરત ડાયમંડ બુર્સ અને ગુજરાત વેપાર સમાચાર",
            youtubeId = "3JZ_D3ELwOQ",
            duration = "04:10"
        )
    )

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
        // 1. Live Breaking News ticker
        breakingHeaderAdapter = BreakingNewsHeaderAdapter { article ->
            openArticleDetail(article)
        }

        // 2. Featured Hero Carousel (Top 5 stories)
        heroCarouselAdapter = HeroCarouselAdapter { article ->
            openArticleDetail(article)
        }

        // 3. "ટોપ સમાચાર" (Top Stories) Horizontal strip
        topStoriesAdapter = TopStoriesHeaderAdapter(
            onArticleClick = { article -> openArticleDetail(article) },
            onViewAllClick = { filterByCategory("gujarat", "ગુજરાત") }
        )

        // 4. "ઇન્સ્ટાગ્રામ રીલ્સ" (Instagram Reels) Horizontal strip
        reelsAdapter = ReelsHeaderAdapter(
            onReelClick = { reel -> openReel(reel) },
            onViewAllClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com/gujaratpost.in/"))
                startActivity(intent)
            }
        )

        // 5. "સૌથી વધુ વંચાયેલા" (Most Read / Trending #1..#4)
        mostReadAdapter = MostReadHeaderAdapter(
            onArticleClick = { article -> openArticleDetail(article) },
            onViewAllClick = { filterByCategory("trending", "ટ્રેન્ડિંગ") }
        )

        // 6. "વીડિયો ડેસ્ક" (Videos) Horizontal strip
        videosAdapter = VideosHeaderAdapter(
            onVideoClick = { video -> openVideo(video) },
            onViewAllClick = {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/@Gujaratpostnews"))
                startActivity(intent)
            }
        )

        // 7. Section Header for the main infinite feed
        sectionHeaderAdapter = SectionHeaderAdapter()

        // 8. Main infinite scrolling feed adapter
        articleAdapter = ArticleAdapter { article ->
            openArticleDetail(article)
        }

        // 9. Footer loading and "all caught up" indicator
        footerLoadingAdapter = FooterLoadingAdapter()

        // ConcatAdapter joins all components in exact website order
        val concatAdapter = ConcatAdapter(
            breakingHeaderAdapter,
            heroCarouselAdapter,
            topStoriesAdapter,
            reelsAdapter,
            mostReadAdapter,
            videosAdapter,
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
                distributeHomeArticles(cached)
                reelsAdapter.submitReels(fallbackReels)
                videosAdapter.submitVideos(fallbackVideos)
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

        // In search mode, collapse promotional portal strips and show results feed
        heroCarouselAdapter.submitHeroArticles(emptyList())
        topStoriesAdapter.submitArticles(emptyList())
        reelsAdapter.submitReels(emptyList())
        mostReadAdapter.submitArticles(emptyList())
        videosAdapter.submitVideos(emptyList())
        footerLoadingAdapter.setState(loading = false, showNoMore = false)

        sectionHeaderAdapter.setTitle("શોધ પરિણામ: \"$trimmed\"", "પરિણામ")
        binding.progressLoading.visibility = View.VISIBLE
        binding.layoutError.visibility = View.GONE

        lifecycleScope.launch {
            fetchArticles(null, page = 1, isAppend = false, query = trimmed)
            binding.progressLoading.visibility = View.GONE
            binding.swipeRefresh.isRefreshing = false
        }
    }

    private fun loadData(isPullToRefresh: Boolean) {
        if (!isPullToRefresh && articleAdapter.itemCount == 0 && heroArticles.isEmpty()) {
            binding.progressLoading.visibility = View.VISIBLE
        }

        currentPage = 1
        hasMorePages = true
        footerLoadingAdapter.setState(loading = false, showNoMore = false)

        // 1. Fetch main articles
        lifecycleScope.launch {
            fetchArticles(selectedCategorySlug, page = 1, isAppend = false, query = currentSearchQuery)
            binding.progressLoading.visibility = View.GONE
            binding.swipeRefresh.isRefreshing = false
        }

        // 2. Fetch categories
        lifecycleScope.launch {
            fetchCategories()
        }

        // 3. Fetch breaking ticker
        lifecycleScope.launch {
            fetchBreakingNews()
        }

        // 4. Fetch videos & reels for the portal sections
        if (selectedCategorySlug == null || selectedCategorySlug == "all") {
            lifecycleScope.launch {
                fetchVideos()
            }
            lifecycleScope.launch {
                fetchReels()
            }
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

    private suspend fun fetchVideos() {
        try {
            val response = RetrofitClient.apiService.getVideos(limit = 12)
            if (response.isSuccessful && response.body()?.success == true) {
                val list = response.body()?.data?.videos.orEmpty()
                if (list.isNotEmpty()) {
                    videosAdapter.submitVideos(list)
                    return
                }
            }
            videosAdapter.submitVideos(fallbackVideos)
        } catch (e: Exception) {
            videosAdapter.submitVideos(fallbackVideos)
        }
    }

    private suspend fun fetchReels() {
        try {
            val response = RetrofitClient.apiService.getReels()
            if (response.isSuccessful && response.body()?.success == true) {
                val list = response.body()?.data.orEmpty()
                if (list.isNotEmpty()) {
                    reelsAdapter.submitReels(list)
                    return
                }
            }
            reelsAdapter.submitReels(fallbackReels)
        } catch (e: Exception) {
            reelsAdapter.submitReels(fallbackReels)
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

    private fun startHeroRotator() {
        heroRotatorJob?.cancel()
        if (heroArticles.size <= 1 || _binding == null) return

        heroRotatorJob = lifecycleScope.launch {
            while (isActive && heroArticles.size > 1) {
                delay(4500)
                heroCarouselAdapter.rotateNext()
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
                limit = 25,
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
                            distributeHomeArticles(newArticles)
                        } else if (!query.isNullOrBlank()) {
                            // Search results
                            heroCarouselAdapter.submitHeroArticles(emptyList())
                            topStoriesAdapter.submitArticles(emptyList())
                            reelsAdapter.submitReels(emptyList())
                            mostReadAdapter.submitArticles(emptyList())
                            videosAdapter.submitVideos(emptyList())
                            currentFeedArticles = newArticles
                            articleAdapter.submitList(newArticles)
                            sectionHeaderAdapter.setTitle("શોધ પરિણામો: \"$query\" (${newArticles.size} સમાચાર)", "પરિણામ")
                        } else {
                            // Specific category selected
                            topStoriesAdapter.submitArticles(emptyList())
                            reelsAdapter.submitReels(emptyList())
                            mostReadAdapter.submitArticles(emptyList())
                            videosAdapter.submitVideos(emptyList())

                            val leadHero = newArticles.take(1)
                            heroArticles = leadHero
                            heroCarouselAdapter.submitHeroArticles(leadHero)

                            val rest = if (newArticles.size > 1) newArticles.drop(1) else emptyList()
                            currentFeedArticles = rest
                            articleAdapter.submitList(rest)

                            val catName = categoryAdapter.findCategoryNameBySlug(categorySlug) ?: "સમાચાર"
                            sectionHeaderAdapter.setTitle("$catName સમાચાર", "કૅટેગરી")
                        }
                        updateRepositoryArticles()
                        footerLoadingAdapter.setState(loading = false, showNoMore = false)
                    } else {
                        clearAllSections()
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

    /**
     * Distributes the articles into the website-style multi-section portal hierarchy:
     * - Articles 0..4: Hero Carousel (5 items)
     * - Articles 5..12: Top Stories (8 items)
     * - Articles 13..16: Most Read (4 items)
     * - Articles 17..end: Latest News infinite feed
     */
    private fun distributeHomeArticles(articles: List<Article>) {
        if (articles.isEmpty()) return

        // 1. Hero Carousel (Top 5)
        val hero = articles.take(5)
        heroArticles = hero
        heroCarouselAdapter.submitHeroArticles(hero)
        startHeroRotator()

        // 2. Top Stories (Next 8)
        val topStories = if (articles.size > 5) articles.subList(5, minOf(13, articles.size)) else emptyList()
        topStoriesList = topStories
        topStoriesAdapter.submitArticles(topStories)

        // 3. Most Read (Next 4)
        val mostRead = if (articles.size > 13) articles.subList(13, minOf(17, articles.size)) else emptyList()
        mostReadList = mostRead
        mostReadAdapter.submitArticles(mostRead)

        // 4. Latest News Feed (Remaining articles)
        val feed = if (articles.size > 17) {
            articles.subList(17, articles.size)
        } else if (articles.size > 5) {
            articles.subList(5, articles.size)
        } else {
            articles
        }
        currentFeedArticles = feed
        articleAdapter.submitList(feed)

        sectionHeaderAdapter.setTitle("તાજા સમાચાર ફિડ", "નવીનતમ")
    }

    private fun clearAllSections() {
        heroRotatorJob?.cancel()
        heroArticles = emptyList()
        topStoriesList = emptyList()
        mostReadList = emptyList()
        currentFeedArticles = emptyList()

        heroCarouselAdapter.submitHeroArticles(emptyList())
        topStoriesAdapter.submitArticles(emptyList())
        reelsAdapter.submitReels(emptyList())
        mostReadAdapter.submitArticles(emptyList())
        videosAdapter.submitVideos(emptyList())
        articleAdapter.submitList(emptyList())
        sectionHeaderAdapter.setTitle(null)
    }

    private fun updateRepositoryArticles() {
        val allArticles = mutableListOf<Article>()
        allArticles.addAll(heroArticles)
        allArticles.addAll(topStoriesList)
        allArticles.addAll(mostReadList)
        allArticles.addAll(currentFeedArticles)
        ArticleRepository.currentArticles = allArticles.distinctBy { it.id }
    }

    private fun handleInitialLoadError(categorySlug: String?) {
        if (categorySlug == null || categorySlug == "all") {
            val fallback = context?.let { NewsCacheManager.getCachedArticles(it) }.orEmpty()
            if (fallback.isNotEmpty()) {
                binding.layoutError.visibility = View.GONE
                distributeHomeArticles(fallback)
                reelsAdapter.submitReels(fallbackReels)
                videosAdapter.submitVideos(fallbackVideos)
                updateRepositoryArticles()
            } else {
                binding.layoutError.visibility = View.VISIBLE
                binding.tvErrorMessage.text = "સમાચાર લોડ કરી શકાયા નથી. કૃપા કરીને ફરી પ્રયાસ કરો."
            }
        } else {
            clearAllSections()
            binding.layoutError.visibility = View.VISIBLE
            binding.tvErrorMessage.text = "આ કૅટેગરીના સમાચાર મેળવી શકાયા નથી."
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

    private fun openReel(reel: Reel) {
        try {
            val uri = Uri.parse(reel.targetUrl)
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        } catch (e: Exception) {
            // Safe fallback
        }
    }

    private fun openVideo(video: Video) {
        try {
            val yId = video.youtubeId?.trim().orEmpty()
            if (yId.isNotBlank()) {
                val appIntent = Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:$yId"))
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=$yId"))
                try {
                    startActivity(appIntent)
                } catch (ex: Exception) {
                    startActivity(webIntent)
                }
            }
        } catch (e: Exception) {
            // Safe fallback
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        breakingRotatorJob?.cancel()
        heroRotatorJob?.cancel()
        _binding = null
    }
}
