package com.gujaratpost.app.ui.category

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.gujaratpost.app.R
import com.gujaratpost.app.data.models.Category
import com.gujaratpost.app.databinding.ItemCategoryChipBinding

class CategoryAdapter(
    categories: List<Category>,
    private var selectedIndex: Int = 0,
    private val onCategorySelected: (Category?) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    private val categoryList = categories.toMutableList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryChipBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int = categoryList.size

    fun setSelectedPosition(position: Int) {
        if (position < 0 || position >= categoryList.size) return
        val previous = selectedIndex
        selectedIndex = position
        notifyItemChanged(previous)
        notifyItemChanged(selectedIndex)
    }

    /**
     * Programmatically selects the category matching the given slug and returns its index.
     */
    fun selectCategoryBySlug(slug: String?): Int {
        val target = if (slug == null || slug == "all") "all" else slug.lowercase().trim()
        val index = categoryList.indexOfFirst {
            if (target == "all") {
                it.slug.equals("all", ignoreCase = true) || it.id.equals("all", ignoreCase = true)
            } else {
                it.slug.equals(target, ignoreCase = true) ||
                it.name.equals(target, ignoreCase = true) ||
                it.nameGu.equals(target, ignoreCase = true) ||
                (target == "gujarat" && (it.slug?.contains("gujarat", ignoreCase = true) == true || it.nameGu?.contains("ગુજરાત") == true))
            }
        }
        if (index >= 0) {
            setSelectedPosition(index)
            return index
        }
        return -1
    }

    fun updateCategories(newCategories: List<Category>, activeSlug: String? = null) {
        categoryList.clear()
        categoryList.addAll(newCategories)
        val target = if (activeSlug == null || activeSlug == "all") "all" else activeSlug.lowercase().trim()
        selectedIndex = categoryList.indexOfFirst {
            if (target == "all") {
                it.slug.equals("all", ignoreCase = true) || it.id.equals("all", ignoreCase = true)
            } else {
                it.slug.equals(target, ignoreCase = true) ||
                it.name.equals(target, ignoreCase = true) ||
                it.nameGu.equals(target, ignoreCase = true) ||
                (target == "gujarat" && (it.slug?.contains("gujarat", ignoreCase = true) == true || it.nameGu?.contains("ગુજરાત") == true))
            }
        }.coerceAtLeast(0)
        notifyDataSetChanged()
    }

    inner class CategoryViewHolder(
        private val binding: ItemCategoryChipBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(position: Int) {
            val item = categoryList[position]
            binding.tvCategoryName.text = item.displayName

            val isSelected = position == selectedIndex

            if (isSelected) {
                // Active capsule: solid red background + bold white text
                binding.layoutChipContainer.setBackgroundResource(R.drawable.bg_category_active)
                binding.tvCategoryName.setTextColor(Color.WHITE)
                binding.tvCategoryName.setTypeface(null, android.graphics.Typeface.BOLD)
                binding.viewIndicator.visibility = View.GONE
            } else {
                // Inactive capsule: white card + subtle border + slate text
                binding.layoutChipContainer.setBackgroundResource(R.drawable.bg_category_inactive)
                binding.tvCategoryName.setTextColor(Color.parseColor("#334155"))
                binding.tvCategoryName.setTypeface(null, android.graphics.Typeface.NORMAL)
                binding.viewIndicator.visibility = View.GONE
            }

            binding.root.setOnClickListener {
                val pos = adapterPosition
                if (pos == RecyclerView.NO_POSITION) return@setOnClickListener
                setSelectedPosition(pos)
                if (pos == 0 || item.slug == "all") {
                    onCategorySelected(null)
                } else {
                    onCategorySelected(item)
                }
            }
        }
    }
}

