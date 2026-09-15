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
            try {
                binding.tvPageTitle.text = article.displayTitle
                binding.tvPageCategory.text = article.categoryName

                // Location
                val loc = article.location?.trim().orEmpty()
                if (loc.isNotBlank()) {
                    binding.tvPageLocation.visibility = View.VISIBLE
                    binding.tvPageLocation.text = "📍 $loc"
                } else {
                    binding.tvPageLocation.visibility = View.GONE
                }

                // Reading time
                val rTime = article.readingTime ?: 3
                binding.tvPageReadingTime.visibility = View.VISIBLE
                binding.tvPageReadingTime.text = "⏱️ $rTime મિનિટ"

                val dateStr = DateFormatter.formatIsoDate(article.publishedAt ?: article.createdAt)
                binding.tvPageDate.text = dateStr

                val viewsNum = article.views ?: 0L
                val viewCountNum = article.viewCount ?: 0L
                val viewCount = if (viewsNum > 0) viewsNum else viewCountNum
                binding.tvPageViews.text = if (viewCount > 0) "👁️ $viewCount" else ""

                // Author Profile
                val author = article.author
                val authorName = author?.displayName ?: "ગુજરાત પોસ્ટ બ્યુરો"
                binding.tvPageAuthor.text = "રિપોર્ટ: $authorName"

                val role = author?.displayDesignation?.trim().orEmpty()
                if (role.isNotBlank()) {
                    binding.tvPageAuthorRole.visibility = View.VISIBLE
                    binding.tvPageAuthorRole.text = role
                } else {
                    binding.tvPageAuthorRole.visibility = View.GONE
                }

                val authorImgUrl = author?.image?.trim().orEmpty()
                if (authorImgUrl.isNotBlank()) {
                    Glide.with(context)
                        .load(authorImgUrl)
                        .circleCrop()
                        .placeholder(R.drawable.ic_author_default)
                        .error(R.drawable.ic_author_default)
                        .into(binding.ivPageAuthorAvatar)
                } else {
                    binding.ivPageAuthorAvatar.setImageResource(R.drawable.ic_author_default)
                }

                // Lead excerpt callout
                val excerpt = article.displayExcerpt.trim()
                val rawContent = article.displayContent.trim()
                if (excerpt.isNotBlank() && excerpt != rawContent && excerpt != article.displayTitle.trim()) {
                    binding.layoutPageExcerpt.visibility = View.VISIBLE
                    binding.tvPageExcerpt.text = excerpt
                } else {
                    binding.layoutPageExcerpt.visibility = View.GONE
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

                // Tags Chips - Safe accessor prevents any NullPointerException
                val tagsList = article.safeTags
                if (tagsList.isNotEmpty()) {
                    binding.layoutPageTags.visibility = View.VISIBLE
                    binding.containerTagChips.removeAllViews()
                    for (t in tagsList) {
                        val chip = android.widget.TextView(context).apply {
                            text = if (t.startsWith("#")) t else "#$t"
                            setBackgroundResource(R.drawable.bg_chip_tag)
                            setPadding(24, 12, 24, 12)
                            setTextColor(android.graphics.Color.parseColor("#334155"))
                            textSize = 12f
                            val lp = android.widget.LinearLayout.LayoutParams(
                                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                            ).apply {
                                setMargins(0, 0, 16, 0)
                            }
                            layoutParams = lp
                        }
                        binding.containerTagChips.addView(chip)
                    }
                } else {
                    binding.layoutPageTags.visibility = View.GONE
                }

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
            } catch (e: Throwable) {
                android.util.Log.e("ArticlePagerAdapter", "Error rendering article detail", e)
                try {
                    binding.tvPageTitle.text = article.displayTitle
                    binding.tvPageContent.text = article.displayContent
                } catch (e2: Throwable) {}
            }
        }
    }
}
