package com.gujaratpost.app.ui.category

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
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
        val previous = selectedIndex
        selectedIndex = position
        notifyItemChanged(previous)
        notifyItemChanged(selectedIndex)
    }

    fun updateCategories(newCategories: List<Category>) {
        categoryList.clear()
        categoryList.addAll(newCategories)
        notifyDataSetChanged()
    }

    inner class CategoryViewHolder(
        private val binding: ItemCategoryChipBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(position: Int) {
            val item = categoryList[position]
            binding.tvCategoryName.text = item.displayName.uppercase()

            val isSelected = position == selectedIndex

            if (isSelected) {
                binding.tvCategoryName.setTextColor(Color.WHITE)
                binding.viewIndicator.visibility = View.VISIBLE
            } else {
                binding.tvCategoryName.setTextColor(Color.parseColor("#B0BEC5"))
                binding.viewIndicator.visibility = View.GONE
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
