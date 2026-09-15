package com.gujaratpost.app.ui.saved

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.gujaratpost.app.data.ArticleRepository
import com.gujaratpost.app.data.models.Article
import com.gujaratpost.app.databinding.FragmentSavedArticlesBinding
import com.gujaratpost.app.ui.detail.ArticleDetailActivity
import com.gujaratpost.app.ui.home.ArticleAdapter
import com.gujaratpost.app.utils.BookmarkManager

class SavedArticlesFragment : Fragment() {

    private var _binding: FragmentSavedArticlesBinding? = null
    private val binding get() = _binding!!

    private lateinit var articleAdapter: ArticleAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSavedArticlesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        articleAdapter = ArticleAdapter { article ->
            val savedList = BookmarkManager.getSavedArticles(requireContext())
            ArticleRepository.currentArticles = savedList
            ArticleRepository.currentPosition = savedList.indexOfFirst { it.id == article.id }.coerceAtLeast(0)

            val intent = Intent(requireContext(), ArticleDetailActivity::class.java).apply {
                putExtra("EXTRA_ARTICLE_POSITION", ArticleRepository.currentPosition)
                putExtra(Constants.EXTRA_ARTICLE_ID, article.safeId)
                putExtra(Constants.EXTRA_ARTICLE_SLUG, article.safeSlug)
                putExtra(Constants.EXTRA_ARTICLE_TITLE, article.displayTitle)
            }
            startActivity(intent)
        }

        binding.rvSavedArticles.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = articleAdapter
        }

        loadSavedArticles()
    }

    override fun onResume() {
        super.onResume()
        loadSavedArticles()
    }

    private fun loadSavedArticles() {
        val saved = BookmarkManager.getSavedArticles(requireContext())
        if (saved.isEmpty()) {
            binding.rvSavedArticles.visibility = View.GONE
            binding.layoutEmptySaved.visibility = View.VISIBLE
        } else {
            binding.rvSavedArticles.visibility = View.VISIBLE
            binding.layoutEmptySaved.visibility = View.GONE
            articleAdapter.submitList(saved)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
