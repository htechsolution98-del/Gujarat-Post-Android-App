package com.gujaratpost.app.ui.category

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.gujaratpost.app.R
import com.gujaratpost.app.data.models.Category
import com.gujaratpost.app.databinding.ItemCategoryChipBinding

class CategoryAdapter(
    private val categories: List<Category>,
    private var selectedIndex: Int = 0,
    private val onCategorySelected: (Category?) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

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

    override fun getItemCount(): Int = categories.size

    fun setSelectedPosition(position: Int) {
        val previous = selectedIndex
        selectedIndex = position
        notifyItemChanged(previous)
        notifyItemChanged(selectedIndex)
    }

    inner class CategoryViewHolder(
        private val binding: ItemCategoryChipBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(position: Int) {
            val item = categories[position]
            binding.tvCategoryName.text = item.displayName

            val isSelected = position == selectedIndex
            val context = binding.root.context

            if (isSelected) {
                binding.tvCategoryName.setBackgroundResource(R.drawable.badge_breaking)
                binding.tvCategoryName.setTextColor(ContextCompat.getColor(context, R.color.white))
            } else {
                binding.tvCategoryName.setBackgroundResource(R.drawable.rounded_card_bg)
                binding.tvCategoryName.setTextColor(ContextCompat.getColor(context, R.color.text_primary))
            }

            binding.root.setOnClickListener {
                setSelectedPosition(adapterPosition)
                // If "All" (first item), pass null slug to load all articles
                if (adapterPosition == 0) {
                    onCategorySelected(null)
                } else {
                    onCategorySelected(item)
                }
            }
        }
    }
}
