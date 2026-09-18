package com.gujaratpost.app.ui.home

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.gujaratpost.app.R
import com.gujaratpost.app.data.models.Article
import com.gujaratpost.app.data.models.Reel
import com.gujaratpost.app.data.models.Video
import com.gujaratpost.app.databinding.ItemCardReelBinding
import com.gujaratpost.app.databinding.ItemCardTopStoryBinding
import com.gujaratpost.app.databinding.ItemCardVideoBinding
import com.gujaratpost.app.databinding.ItemHomeBreakingBinding
import com.gujaratpost.app.databinding.ItemHomeFooterBinding
import com.gujaratpost.app.databinding.ItemHomeHeroBinding
import com.gujaratpost.app.databinding.ItemHomeMostReadBinding
import com.gujaratpost.app.databinding.ItemHomeReelsBinding
import com.gujaratpost.app.databinding.ItemHomeSectionHeaderBinding
import com.gujaratpost.app.databinding.ItemHomeTopStoriesBinding
import com.gujaratpost.app.databinding.ItemHomeVideosBinding
import com.gujaratpost.app.utils.DateFormatter
import kotlin.math.abs

/**
 * 1. Live Breaking News Ticker Header
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
 * 2. Hero Featured Headline Carousel (Top 5 stories with auto-advance and swipe)
 */
class HeroCarouselAdapter(
    private val onArticleClick: (Article) -> Unit
) : RecyclerView.Adapter<HeroCarouselAdapter.HeroViewHolder>() {

    private var heroArticles: List<Article> = emptyList()
    private var currentIndex: Int = 0

    fun submitHeroArticles(articles: List<Article>) {
        val wasEmpty = heroArticles.isEmpty()
        heroArticles = articles.take(5)
        currentIndex = 0
        if (wasEmpty && heroArticles.isNotEmpty()) {
            notifyItemInserted(0)
        } else if (!wasEmpty && heroArticles.isEmpty()) {
            notifyItemRemoved(0)
        } else {
            notifyItemChanged(0)
        }
    }

    fun rotateNext() {
        if (heroArticles.size <= 1) return
        currentIndex = (currentIndex + 1) % heroArticles.size
        notifyItemChanged(0, "HERO_SLIDE")
    }

    fun rotatePrev() {
        if (heroArticles.size <= 1) return
        currentIndex = (currentIndex - 1 + heroArticles.size) % heroArticles.size
        notifyItemChanged(0, "HERO_SLIDE")
    }

    override fun getItemCount(): Int = if (heroArticles.isNotEmpty()) 1 else 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HeroViewHolder {
        val binding = ItemHomeHeroBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HeroViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HeroViewHolder, position: Int) {
        holder.bind()
    }

    override fun onBindViewHolder(holder: HeroViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isNotEmpty()) {
            holder.updateContentOnly()
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    override fun onViewRecycled(holder: HeroViewHolder) {
        super.onViewRecycled(holder)
        try {
            Glide.with(holder.itemView.context).clear(holder.binding.ivHeroImage)
        } catch (e: Throwable) {
            // Ignore
        }
    }

    inner class HeroViewHolder(
        val binding: ItemHomeHeroBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private val gestureDetector = GestureDetector(itemView.context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                if (e1 == null) return false
                val diffX = e2.x - e1.x
                val diffY = e2.y - e1.y
                if (abs(diffX) > abs(diffY) && abs(diffX) > 100 && abs(velocityX) > 100) {
                    if (diffX < 0) {
                        rotateNext()
                    } else {
                        rotatePrev()
                    }
                    return true
                }
                return false
            }

            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                if (heroArticles.isNotEmpty()) {
                    val current = heroArticles[currentIndex.coerceIn(0, heroArticles.size - 1)]
                    onArticleClick(current)
                }
                return true
            }
        })

        init {
            binding.cardHeroFeatured.setOnTouchListener { _, event ->
                gestureDetector.onTouchEvent(event)
                true
            }
        }

        fun bind() {
            if (heroArticles.isEmpty()) return
            updateContentOnly()
        }

        fun updateContentOnly() {
            if (heroArticles.isEmpty()) return
            val current = heroArticles[currentIndex.coerceIn(0, heroArticles.size - 1)]

            binding.tvHeroTitle.text = current.displayTitle
            binding.tvHeroCategory.text = current.categoryName
            binding.tvHeroDate.text = DateFormatter.formatIsoDate(current.publishedAt ?: current.createdAt)

            val total = heroArticles.size
            binding.tvHeroSlideCounter.text = "${currentIndex + 1}/$total"

            // Update 5 dot indicators
            val dots = listOf(
                binding.dot0,
                binding.dot1,
                binding.dot2,
                binding.dot3,
                binding.dot4
            )
            val density = itemView.context.resources.displayMetrics.density
            val activeWidth = (14 * density).toInt()
            val inactiveWidth = (5 * density).toInt()

            for (i in dots.indices) {
                if (i < total) {
                    dots[i].visibility = View.VISIBLE
                    if (i == currentIndex) {
                        dots[i].setBackgroundResource(R.drawable.bg_dot_active)
                        dots[i].layoutParams.width = activeWidth
                    } else {
                        dots[i].setBackgroundResource(R.drawable.bg_dot_inactive)
                        dots[i].layoutParams.width = inactiveWidth
                    }
                    dots[i].requestLayout()
                } else {
                    dots[i].visibility = View.GONE
                }
            }

            // Load high-impact image
            val imageUrl = current.resolvedImageUrl
            if (!imageUrl.isNullOrBlank()) {
                Glide.with(itemView.context)
                    .load(imageUrl)
                    .override(1080, 600)
                    .centerCrop()
                    .transition(DrawableTransitionOptions.withCrossFade(200))
                    .placeholder(R.drawable.rounded_card_bg)
                    .error(R.drawable.rounded_card_bg)
                    .into(binding.ivHeroImage)
            } else {
                binding.ivHeroImage.setImageResource(R.drawable.rounded_card_bg)
            }
        }
    }
}

/**
 * 3. "ટોપ સમાચાર" (Top Stories) Horizontal Gallery Header Adapter
 */
class TopStoriesHeaderAdapter(
    private val onArticleClick: (Article) -> Unit,
    private val onViewAllClick: () -> Unit
) : RecyclerView.Adapter<TopStoriesHeaderAdapter.TopStoriesViewHolder>() {

    private var articles: List<Article> = emptyList()

    fun submitArticles(list: List<Article>) {
        val wasEmpty = articles.isEmpty()
        articles = list
        if (wasEmpty && list.isNotEmpty()) {
            notifyItemInserted(0)
        } else if (!wasEmpty && list.isEmpty()) {
            notifyItemRemoved(0)
        } else {
            notifyItemChanged(0)
        }
    }

    override fun getItemCount(): Int = if (articles.isNotEmpty()) 1 else 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopStoriesViewHolder {
        val binding = ItemHomeTopStoriesBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TopStoriesViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TopStoriesViewHolder, position: Int) {
        holder.bind(articles)
    }

    inner class TopStoriesViewHolder(
        val binding: ItemHomeTopStoriesBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private val subAdapter = TopStoryItemAdapter { article ->
            onArticleClick(article)
        }

        init {
            binding.rvTopStories.apply {
                layoutManager = LinearLayoutManager(itemView.context, LinearLayoutManager.HORIZONTAL, false)
                adapter = subAdapter
                setHasFixedSize(true)
            }
            binding.tvTopStoriesViewAll.setOnClickListener {
                onViewAllClick()
            }
        }

        fun bind(list: List<Article>) {
            subAdapter.submitList(list)
        }
    }
}

/**
 * Sub-adapter for individual Top Story cards
 */
class TopStoryItemAdapter(
    private val onItemClick: (Article) -> Unit
) : RecyclerView.Adapter<TopStoryItemAdapter.CardViewHolder>() {

    private var items: List<Article> = emptyList()

    fun submitList(list: List<Article>) {
        items = list
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val binding = ItemCardTopStoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        holder.bind(items[position])
    }

    inner class CardViewHolder(val binding: ItemCardTopStoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(article: Article) {
            binding.tvTopStoryTitle.text = article.displayTitle
            binding.tvTopStoryCategory.text = article.categoryName
            binding.tvTopStoryTime.text = DateFormatter.formatIsoDate(article.publishedAt ?: article.createdAt)

            val img = article.resolvedImageUrl
            if (!img.isNullOrBlank()) {
                Glide.with(itemView.context)
                    .load(img)
                    .override(350, 210)
                    .centerCrop()
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .placeholder(R.drawable.rounded_card_bg)
                    .error(R.drawable.rounded_card_bg)
                    .into(binding.ivTopStoryImage)
            } else {
                binding.ivTopStoryImage.setImageResource(R.drawable.rounded_card_bg)
            }

            binding.root.setOnClickListener {
                onItemClick(article)
            }
        }
    }
}

/**
 * 4. "ઇન્સ્ટાગ્રામ રીલ્સ" (Instagram Reels) Header Adapter
 */
class ReelsHeaderAdapter(
    private val onReelClick: (Reel) -> Unit,
    private val onViewAllClick: () -> Unit
) : RecyclerView.Adapter<ReelsHeaderAdapter.ReelsViewHolder>() {

    private var reelsList: List<Reel> = emptyList()

    fun submitReels(list: List<Reel>) {
        val wasEmpty = reelsList.isEmpty()
        reelsList = list
        if (wasEmpty && list.isNotEmpty()) {
            notifyItemInserted(0)
        } else if (!wasEmpty && list.isEmpty()) {
            notifyItemRemoved(0)
        } else {
            notifyItemChanged(0)
        }
    }

    override fun getItemCount(): Int = if (reelsList.isNotEmpty()) 1 else 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReelsViewHolder {
        val binding = ItemHomeReelsBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ReelsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReelsViewHolder, position: Int) {
        holder.bind(reelsList)
    }

    inner class ReelsViewHolder(
        val binding: ItemHomeReelsBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private val subAdapter = ReelItemAdapter { reel ->
            onReelClick(reel)
        }

        init {
            binding.rvReels.apply {
                layoutManager = LinearLayoutManager(itemView.context, LinearLayoutManager.HORIZONTAL, false)
                adapter = subAdapter
                setHasFixedSize(true)
            }
            binding.tvReelsViewAll.setOnClickListener {
                onViewAllClick()
            }
        }

        fun bind(list: List<Reel>) {
            subAdapter.submitList(list)
        }
    }
}

/**
 * Sub-adapter for individual 9:16 vertical Reel cards
 */
class ReelItemAdapter(
    private val onReelClick: (Reel) -> Unit
) : RecyclerView.Adapter<ReelItemAdapter.ReelCardViewHolder>() {

    private var items: List<Reel> = emptyList()

    fun submitList(list: List<Reel>) {
        items = list
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReelCardViewHolder {
        val binding = ItemCardReelBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ReelCardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReelCardViewHolder, position: Int) {
        holder.bind(items[position])
    }

    inner class ReelCardViewHolder(val binding: ItemCardReelBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(reel: Reel) {
            binding.tvReelTitle.text = reel.displayHeading

            val viewsStr = reel.displayViews
            if (!viewsStr.isNullOrBlank()) {
                binding.tvReelViews.visibility = View.VISIBLE
                binding.tvReelViews.text = viewsStr
            } else {
                binding.tvReelViews.visibility = View.GONE
            }

            val thumb = reel.thumbnail
            if (!thumb.isNullOrBlank()) {
                Glide.with(itemView.context)
                    .load(thumb)
                    .override(270, 420)
                    .centerCrop()
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .placeholder(R.drawable.rounded_card_bg)
                    .error(R.drawable.rounded_card_bg)
                    .into(binding.ivReelThumbnail)
            } else {
                binding.ivReelThumbnail.setImageResource(R.drawable.rounded_card_bg)
            }

            binding.root.setOnClickListener {
                onReelClick(reel)
            }
        }
    }
}

/**
 * 5. "સૌથી વધુ વંચાયેલા" (Most Read / Trending #1..#4) Header Adapter
 */
class MostReadHeaderAdapter(
    private val onArticleClick: (Article) -> Unit,
    private val onViewAllClick: () -> Unit
) : RecyclerView.Adapter<MostReadHeaderAdapter.MostReadViewHolder>() {

    private var articles: List<Article> = emptyList()

    fun submitArticles(list: List<Article>) {
        val wasEmpty = articles.isEmpty()
        articles = list.take(4)
        if (wasEmpty && articles.isNotEmpty()) {
            notifyItemInserted(0)
        } else if (!wasEmpty && articles.isEmpty()) {
            notifyItemRemoved(0)
        } else {
            notifyItemChanged(0)
        }
    }

    override fun getItemCount(): Int = if (articles.isNotEmpty()) 1 else 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MostReadViewHolder {
        val binding = ItemHomeMostReadBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MostReadViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MostReadViewHolder, position: Int) {
        holder.bind(articles)
    }

    inner class MostReadViewHolder(
        val binding: ItemHomeMostReadBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.tvMostReadViewAll.setOnClickListener {
                onViewAllClick()
            }
        }

        fun bind(list: List<Article>) {
            // Row 1
            if (list.isNotEmpty()) {
                val art1 = list[0]
                binding.rowMostRead1.visibility = View.VISIBLE
                binding.tvMostReadTitle1.text = art1.displayTitle
                binding.tvMostReadMeta1.text = "${art1.categoryName} • ${DateFormatter.formatIsoDate(art1.publishedAt ?: art1.createdAt)}"
                binding.rowMostRead1.setOnClickListener { onArticleClick(art1) }
            } else {
                binding.rowMostRead1.visibility = View.GONE
            }

            // Row 2
            if (list.size > 1) {
                val art2 = list[1]
                binding.rowMostRead2.visibility = View.VISIBLE
                binding.tvMostReadTitle2.text = art2.displayTitle
                binding.tvMostReadMeta2.text = "${art2.categoryName} • ${DateFormatter.formatIsoDate(art2.publishedAt ?: art2.createdAt)}"
                binding.rowMostRead2.setOnClickListener { onArticleClick(art2) }
            } else {
                binding.rowMostRead2.visibility = View.GONE
            }

            // Row 3
            if (list.size > 2) {
                val art3 = list[2]
                binding.rowMostRead3.visibility = View.VISIBLE
                binding.tvMostReadTitle3.text = art3.displayTitle
                binding.tvMostReadMeta3.text = "${art3.categoryName} • ${DateFormatter.formatIsoDate(art3.publishedAt ?: art3.createdAt)}"
                binding.rowMostRead3.setOnClickListener { onArticleClick(art3) }
            } else {
                binding.rowMostRead3.visibility = View.GONE
            }

            // Row 4
            if (list.size > 3) {
                val art4 = list[3]
                binding.rowMostRead4.visibility = View.VISIBLE
                binding.tvMostReadTitle4.text = art4.displayTitle
                binding.tvMostReadMeta4.text = "${art4.categoryName} • ${DateFormatter.formatIsoDate(art4.publishedAt ?: art4.createdAt)}"
                binding.rowMostRead4.setOnClickListener { onArticleClick(art4) }
            } else {
                binding.rowMostRead4.visibility = View.GONE
            }
        }
    }
}

/**
 * 6. "વીડિયો ડેસ્ક" (Videos) Header Adapter
 */
class VideosHeaderAdapter(
    private val onVideoClick: (Video) -> Unit,
    private val onViewAllClick: () -> Unit
) : RecyclerView.Adapter<VideosHeaderAdapter.VideosViewHolder>() {

    private var videosList: List<Video> = emptyList()

    fun submitVideos(list: List<Video>) {
        val wasEmpty = videosList.isEmpty()
        videosList = list
        if (wasEmpty && list.isNotEmpty()) {
            notifyItemInserted(0)
        } else if (!wasEmpty && list.isEmpty()) {
            notifyItemRemoved(0)
        } else {
            notifyItemChanged(0)
        }
    }

    override fun getItemCount(): Int = if (videosList.isNotEmpty()) 1 else 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideosViewHolder {
        val binding = ItemHomeVideosBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VideosViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VideosViewHolder, position: Int) {
        holder.bind(videosList)
    }

    inner class VideosViewHolder(
        val binding: ItemHomeVideosBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private val subAdapter = VideoItemAdapter { video ->
            onVideoClick(video)
        }

        init {
            binding.rvVideos.apply {
                layoutManager = LinearLayoutManager(itemView.context, LinearLayoutManager.HORIZONTAL, false)
                adapter = subAdapter
                setHasFixedSize(true)
            }
            binding.tvVideosViewAll.setOnClickListener {
                onViewAllClick()
            }
        }

        fun bind(list: List<Video>) {
            subAdapter.submitList(list)
        }
    }
}

/**
 * Sub-adapter for individual Video cards
 */
class VideoItemAdapter(
    private val onVideoClick: (Video) -> Unit
) : RecyclerView.Adapter<VideoItemAdapter.VideoCardViewHolder>() {

    private var items: List<Video> = emptyList()

    fun submitList(list: List<Video>) {
        items = list
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = items.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoCardViewHolder {
        val binding = ItemCardVideoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VideoCardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VideoCardViewHolder, position: Int) {
        holder.bind(items[position])
    }

    inner class VideoCardViewHolder(val binding: ItemCardVideoBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(video: Video) {
            binding.tvVideoTitle.text = video.displayTitle
            binding.tvVideoDuration.text = video.displayDuration

            val thumb = video.resolvedThumbnailUrl
            if (thumb.isNotBlank()) {
                Glide.with(itemView.context)
                    .load(thumb)
                    .override(350, 210)
                    .centerCrop()
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .placeholder(R.drawable.rounded_card_bg)
                    .error(R.drawable.rounded_card_bg)
                    .into(binding.ivVideoThumbnail)
            } else {
                binding.ivVideoThumbnail.setImageResource(R.drawable.rounded_card_bg)
            }

            binding.root.setOnClickListener {
                onVideoClick(video)
            }
        }
    }
}

/**
 * 7. Section Header Adapter (e.g. "તાજા સમાચાર ફિડ" or Category title)
 */
class SectionHeaderAdapter : RecyclerView.Adapter<SectionHeaderAdapter.HeaderViewHolder>() {

    private var title: String? = null
    private var badge: String? = "નવીનતમ"

    fun setTitle(newTitle: String?, newBadge: String? = null) {
        val hadTitle = !title.isNullOrBlank()
        title = newTitle
        badge = newBadge
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
        if (!badge.isNullOrBlank()) {
            holder.binding.tvSectionBadge.visibility = View.VISIBLE
            holder.binding.tvSectionBadge.text = badge
        } else {
            holder.binding.tvSectionBadge.visibility = View.GONE
        }
    }

    inner class HeaderViewHolder(val binding: ItemHomeSectionHeaderBinding) :
        RecyclerView.ViewHolder(binding.root)
}

/**
 * 8. Footer Adapter for pagination loading indicator and "All news loaded" banner
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
        holder.binding.layoutNoMore.visibility = if (showNoMoreArticles && !isLoadingMore) View.VISIBLE else View.GONE
    }

    inner class FooterViewHolder(val binding: ItemHomeFooterBinding) :
        RecyclerView.ViewHolder(binding.root)
}
