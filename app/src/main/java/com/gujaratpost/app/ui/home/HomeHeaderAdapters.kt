package com.gujaratpost.app.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.gujaratpost.app.R
import com.gujaratpost.app.data.models.Article
import com.gujaratpost.app.databinding.ItemHomeBreakingBinding
import com.gujaratpost.app.databinding.ItemHomeFooterBinding
import com.gujaratpost.app.databinding.ItemHomeHeroBinding
import com.gujaratpost.app.databinding.ItemHomeSectionHeaderBinding
import com.gujaratpost.app.utils.DateFormatter

/**
 * Header adapter for live rotating Breaking News ticker in Home RecyclerView
 */
class BreakingNewsHeaderAdapter(
    private val onArticleClick: (Article) -> Unit
) : RecyclerView.Adapter<BreakingNewsHeaderAdapter.BreakingViewHolder>() {

    private var breakingArticles: List<Article> = emptyList()
    private var currentIndex: Int = 0

    fun submitBreakingArticles(articles: List<Article>) {
        val wasEmpty = breakingArticles.isEmpty()
        breakingArticles = articles
        currentIndex = 0
        if (wasEmpty && articles.isNotEmpty()) {
            notifyItemInserted(0)
        } else if (!wasEmpty && articles.isEmpty()) {
            notifyItemRemoved(0)
        } else {
            notifyItemChanged(0)
        }
    }

    fun rotateIndex(index: Int) {
        if (breakingArticles.isEmpty()) return
        currentIndex = index.coerceIn(0, breakingArticles.size - 1)
        notifyItemChanged(0, "TICKER_ROTATE")
    }

    override fun getItemCount(): Int = if (breakingArticles.isNotEmpty()) 1 else 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BreakingViewHolder {
        val binding = ItemHomeBreakingBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BreakingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BreakingViewHolder, position: Int) {
        holder.bind()
    }

    override fun onBindViewHolder(holder: BreakingViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isNotEmpty()) {
            holder.updateTickerOnly()
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    inner class BreakingViewHolder(
        val binding: ItemHomeBreakingBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind() {
            if (breakingArticles.isEmpty()) return
            updateTickerOnly()
        }

        fun updateTickerOnly() {
            if (breakingArticles.isEmpty()) return
            val current = breakingArticles[currentIndex.coerceIn(0, breakingArticles.size - 1)]
            binding.tvBreakingNewsTitle.text = current.displayTitle
            binding.tvBreakingNewsTitle.isSelected = true

            if (breakingArticles.size > 1) {
                binding.tvBreakingCounter.visibility = View.VISIBLE
                binding.tvBreakingCounter.text = "${currentIndex + 1}/${breakingArticles.size}"
            } else {
                binding.tvBreakingCounter.visibility = View.GONE
            }

            binding.cardBreakingNews.setOnClickListener {
                onArticleClick(current)
            }
        }
    }
}

/**
 * Header adapter for Featured Lead Story Hero Card in Home RecyclerView
 */
class HeroArticleAdapter(
    private val onArticleClick: (Article) -> Unit
) : RecyclerView.Adapter<HeroArticleAdapter.HeroViewHolder>() {

    private var heroArticle: Article? = null

    fun submitHeroArticle(article: Article?) {
        val hadArticle = heroArticle != null
        heroArticle = article
        val hasArticle = article != null

        if (!hadArticle && hasArticle) {
            notifyItemInserted(0)
        } else if (hadArticle && !hasArticle) {
            notifyItemRemoved(0)
        } else if (hasArticle) {
            notifyItemChanged(0)
        }
    }

    override fun getItemCount(): Int = if (heroArticle != null) 1 else 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HeroViewHolder {
        val binding = ItemHomeHeroBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HeroViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HeroViewHolder, position: Int) {
        heroArticle?.let { holder.bind(it) }
    }

    override fun onViewRecycled(holder: HeroViewHolder) {
        super.onViewRecycled(holder)
        try {
            Glide.with(holder.itemView.context).clear(holder.binding.ivHeroImage)
        } catch (e: Throwable) {
            // Safe fallback
        }
    }

    inner class HeroViewHolder(
        val binding: ItemHomeHeroBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(article: Article) {
            binding.tvHeroTitle.text = article.displayTitle
            binding.tvHeroExcerpt.text = article.displayExcerpt
            binding.tvHeroCategory.text = article.categoryName
            binding.tvHeroDate.text = DateFormatter.formatIsoDate(article.publishedAt ?: article.createdAt)

            val imageUrl = article.resolvedImageUrl
            if (!imageUrl.isNullOrBlank()) {
                binding.ivHeroImage.visibility = View.VISIBLE
                Glide.with(itemView.context)
                    .load(imageUrl)
                    .override(1080, 600)
                    .centerCrop()
                    .transition(DrawableTransitionOptions.withCrossFade(200))
                    .placeholder(R.drawable.rounded_card_bg)
                    .error(R.drawable.rounded_card_bg)
                    .into(binding.ivHeroImage)
            } else {
                binding.ivHeroImage.visibility = View.GONE
            }

            binding.cardHeroFeatured.setOnClickListener {
                onArticleClick(article)
            }
        }
    }
}

/**
 * Header adapter for section title (e.g. "તાજા સમાચાર" or category title)
 */
class SectionHeaderAdapter : RecyclerView.Adapter<SectionHeaderAdapter.HeaderViewHolder>() {

    private var title: String? = null

    fun setTitle(newTitle: String?) {
        val hadTitle = !title.isNullOrBlank()
        title = newTitle
        val hasTitle = !newTitle.isNullOrBlank()

        if (!hadTitle && hasTitle) {
            notifyItemInserted(0)
        } else if (hadTitle && !hasTitle) {
            notifyItemRemoved(0)
        } else if (hasTitle) {
            notifyItemChanged(0)
        }
    }

    override fun getItemCount(): Int = if (!title.isNullOrBlank()) 1 else 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HeaderViewHolder {
        val binding = ItemHomeSectionHeaderBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HeaderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HeaderViewHolder, position: Int) {
        holder.binding.tvSectionHeader.text = title.orEmpty()
    }

    inner class HeaderViewHolder(val binding: ItemHomeSectionHeaderBinding) :
        RecyclerView.ViewHolder(binding.root)
}

/**
 * Footer adapter for pagination loading indicator and "end of articles" notice
 */
class FooterLoadingAdapter : RecyclerView.Adapter<FooterLoadingAdapter.FooterViewHolder>() {

    private var isLoadingMore: Boolean = false
    private var showNoMoreArticles: Boolean = false

    fun setState(loading: Boolean, showNoMore: Boolean) {
        val wasVisible = isLoadingMore || showNoMoreArticles
        isLoadingMore = loading
        showNoMoreArticles = showNoMore
        val isVisible = isLoadingMore || showNoMoreArticles

        if (!wasVisible && isVisible) {
            notifyItemInserted(0)
        } else if (wasVisible && !isVisible) {
            notifyItemRemoved(0)
        } else if (isVisible) {
            notifyItemChanged(0)
        }
    }

    override fun getItemCount(): Int = if (isLoadingMore || showNoMoreArticles) 1 else 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FooterViewHolder {
        val binding = ItemHomeFooterBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FooterViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FooterViewHolder, position: Int) {
        holder.binding.progressPagination.visibility = if (isLoadingMore) View.VISIBLE else View.GONE
        holder.binding.tvNoMoreArticles.visibility = if (showNoMoreArticles && !isLoadingMore) View.VISIBLE else View.GONE
    }

    inner class FooterViewHolder(val binding: ItemHomeFooterBinding) :
        RecyclerView.ViewHolder(binding.root)
}
