package com.gujaratpost.app.ui.detail

import android.content.Context
import android.os.Build
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.gujaratpost.app.R
import com.gujaratpost.app.data.models.Article
import com.gujaratpost.app.databinding.ItemArticleDetailPageBinding
import com.gujaratpost.app.utils.DateFormatter

class ArticlePagerAdapter(
    initialArticles: List<Article>
) : RecyclerView.Adapter<ArticlePagerAdapter.ArticlePageViewHolder>() {

    private val articles = initialArticles.toMutableList()

    fun updateArticleAt(position: Int, fullArticle: Article) {
        if (position in 0 until articles.size) {
            articles[position] = fullArticle
            notifyItemChanged(position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticlePageViewHolder {
        val binding = ItemArticleDetailPageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ArticlePageViewHolder(binding, parent.context)
    }

    override fun onBindViewHolder(holder: ArticlePageViewHolder, position: Int) {
        holder.bind(articles[position])
    }

    override fun getItemCount(): Int = articles.size

    inner class ArticlePageViewHolder(
        private val binding: ItemArticleDetailPageBinding,
        private val context: Context
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(article: Article) {
            binding.tvPageTitle.text = article.displayTitle
            binding.tvPageCategory.text = article.categoryName

            val dateStr = DateFormatter.formatIsoDate(article.publishedAt ?: article.createdAt)
            binding.tvPageDate.text = dateStr

            val viewCount = if (article.views > 0) article.views else article.viewCount
            binding.tvPageViews.text = if (viewCount > 0) "👁️ $viewCount" else ""

            val authorName = article.author?.displayName ?: "ગુજરાત પોસ્ટ બ્યુરો"
            binding.tvPageAuthor.text = "રિપોર્ટ: $authorName"

            // Lead excerpt
            val excerpt = article.displayExcerpt.trim()
            val rawContent = article.displayContent.trim()
            if (excerpt.isNotBlank() && excerpt != rawContent && excerpt != article.displayTitle.trim()) {
                binding.tvPageExcerpt.visibility = View.VISIBLE
                binding.tvPageExcerpt.text = excerpt
            } else {
                binding.tvPageExcerpt.visibility = View.GONE
            }

            // Full formatted body content (HTML or clean plain text paragraphs)
            val hasHtml = rawContent.contains("<p>", ignoreCase = true) ||
                          rawContent.contains("<br", ignoreCase = true) ||
                          rawContent.contains("<div>", ignoreCase = true) ||
                          rawContent.contains("<span>", ignoreCase = true)

            val formatted = if (hasHtml) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Html.fromHtml(rawContent, Html.FROM_HTML_MODE_COMPACT)
                } else {
                    @Suppress("DEPRECATION")
                    Html.fromHtml(rawContent)
                }
            } else {
                rawContent
            }
            binding.tvPageContent.text = formatted

            // Cover Image
            val imageUrl = article.resolvedImageUrl
            if (!imageUrl.isNullOrBlank()) {
                binding.ivPageImage.visibility = View.VISIBLE
                Glide.with(context)
                    .load(imageUrl)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .placeholder(R.drawable.rounded_card_bg)
                    .error(R.drawable.rounded_card_bg)
                    .into(binding.ivPageImage)
            } else {
                binding.ivPageImage.visibility = View.GONE
            }
        }
    }
}
