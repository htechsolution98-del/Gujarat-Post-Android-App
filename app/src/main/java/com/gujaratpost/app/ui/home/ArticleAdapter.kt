package com.gujaratpost.app.ui.home

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.gujaratpost.app.R
import com.gujaratpost.app.data.models.Article
import com.gujaratpost.app.databinding.ItemArticleCardBinding
import com.gujaratpost.app.utils.DateFormatter

class ArticleAdapter(
    private val onArticleClick: (Article) -> Unit
) : ListAdapter<Article, ArticleAdapter.ArticleViewHolder>(ArticleDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        val binding = ItemArticleCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ArticleViewHolder(binding, parent.context)
    }

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onViewRecycled(holder: ArticleViewHolder) {
        super.onViewRecycled(holder)
        holder.clear()
    }

    inner class ArticleViewHolder(
        private val binding: ItemArticleCardBinding,
        private val context: Context
    ) : RecyclerView.ViewHolder(binding.root) {

        fun clear() {
            try {
                Glide.with(context).clear(binding.ivThumbnail)
            } catch (e: Throwable) {
                // Safe fallback
            }
        }

        fun bind(article: Article) {
            binding.tvTitle.text = article.displayTitle
            binding.tvExcerpt.text = article.displayExcerpt
            binding.tvDate.text = DateFormatter.formatIsoDate(article.publishedAt ?: article.createdAt)
            binding.tvCategoryTag.text = article.categoryName

            // Thumbnail image loading via Glide with downsampling and centerCrop
            val imageUrl = article.resolvedImageUrl
            if (!imageUrl.isNullOrBlank()) {
                binding.ivThumbnail.visibility = View.VISIBLE
                Glide.with(context)
                    .load(imageUrl)
                    .override(300, 240)
                    .centerCrop()
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .placeholder(R.drawable.rounded_card_bg)
                    .error(R.drawable.rounded_card_bg)
                    .into(binding.ivThumbnail)
            } else {
                binding.ivThumbnail.visibility = View.GONE
            }

            binding.root.setOnClickListener {
                onArticleClick(article)
            }
        }
    }

    class ArticleDiffCallback : DiffUtil.ItemCallback<Article>() {
        override fun areItemsTheSame(oldItem: Article, newItem: Article): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Article, newItem: Article): Boolean {
            return oldItem == newItem
        }
    }
}
