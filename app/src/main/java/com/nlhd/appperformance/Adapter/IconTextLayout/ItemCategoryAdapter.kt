package com.nlhd.appperformance.Adapter.IconTextLayout

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nlhd.appperformance.databinding.ItemAttributeSheetBinding
import com.nlhd.appperformance.databinding.ItemCategoryBinding

data class CategoryItem(
    val icon: Int,
    val text: String
)
class ItemCategoryAdapter(
    private val items: List<CategoryItem>
): RecyclerView.Adapter<ItemCategoryAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding = ItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val item = items[position]
        holder.binding.apply {
            ivItem.setImageResource(item.icon)
            tvItem.text = item.text
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class ViewHolder(val binding: ItemCategoryBinding): RecyclerView.ViewHolder(binding.root)
}