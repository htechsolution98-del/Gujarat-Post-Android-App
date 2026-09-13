package com.gujaratpost.app.ui.category

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayout
import com.gujaratpost.app.R
import com.gujaratpost.app.data.api.RetrofitClient
import com.gujaratpost.app.data.models.Article
import com.gujaratpost.app.data.models.Category
import com.gujaratpost.app.databinding.FragmentCategoryBinding
import com.gujaratpost.app.ui.detail.ArticleDetailActivity
import com.gujaratpost.app.ui.home.ArticleAdapter
import com.gujaratpost.app.utils.Constants
import kotlinx.coroutines.launch

class CategoryFragment : Fragment() {

    private var _binding: FragmentCategoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var articleAdapter: ArticleAdapter
    private var categoriesList: List<Category> = emptyList()
    private var currentSelectedCategory: Category? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSwipeRefresh()
        loadCategoriesAndArticles()
    }

    private fun setupRecyclerView() {
        articleAdapter = ArticleAdapter { article ->
            val articles = articleAdapter.currentList
            ArticleRepository.currentArticles = articles
            ArticleRepository.currentPosition = articles.indexOfFirst { it.id == article.id }.coerceAtLeast(0)

            val intent = Intent(requireContext(), ArticleDetailActivity::class.java).apply {
                putExtra("EXTRA_ARTICLE_POSITION", ArticleRepository.currentPosition)
                putExtra(Constants.EXTRA_ARTICLE_ID, article.id)
                putExtra(Constants.EXTRA_ARTICLE_SLUG, article.slug)
                putExtra(Constants.EXTRA_ARTICLE_TITLE, article.displayTitle)
            }
            startActivity(intent)
        }
        binding.rvCatArticles.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = articleAdapter
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshCat.setColorSchemeResources(R.color.brand_primary)
        binding.swipeRefreshCat.setOnRefreshListener {
            loadArticlesForCurrentCategory()
        }
    }

    private fun loadCategoriesAndArticles() {
        binding.progressCatLoading.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getCategories()
                if (response.isSuccessful && response.body()?.success == true) {
                    categoriesList = response.body()?.data?.categories.orEmpty()
                    setupTabs()
                }
            } catch (e: Exception) {
                binding.progressCatLoading.visibility = View.GONE
            }
        }
    }

    private fun setupTabs() {
        binding.tabCategories.removeAllTabs()

        for (cat in categoriesList) {
            val tab = binding.tabCategories.newTab().setText(cat.displayName)
            binding.tabCategories.addTab(tab)
        }

        if (categoriesList.isNotEmpty()) {
            currentSelectedCategory = categoriesList[0]
            loadArticlesForCurrentCategory()
        }

        binding.tabCategories.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val index = tab?.position ?: 0
                if (index < categoriesList.size) {
                    currentSelectedCategory = categoriesList[index]
                    loadArticlesForCurrentCategory()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun loadArticlesForCurrentCategory() {
        val cat = currentSelectedCategory ?: return
        binding.progressCatLoading.visibility = View.VISIBLE

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getArticles(
                    page = 1,
                    limit = 30,
                    categorySlug = cat.slug,
                    sort = "latest"
                )

                if (response.isSuccessful && response.body()?.success == true) {
                    val articles = response.body()?.data?.articles.orEmpty()
                    articleAdapter.submitList(articles)
                }
            } catch (e: Exception) {
                // Ignore network error on reload
            } finally {
                binding.progressCatLoading.visibility = View.GONE
                binding.swipeRefreshCat.isRefreshing = false
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
