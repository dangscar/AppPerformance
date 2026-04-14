package com.nlhd.appperformance.Adapter.IconTextLayout

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nlhd.appperformance.databinding.ItemAttributeSheetBinding

data class AttributeSheetItem(
    val icon: Int,
    val text: String
)
class AttributeSheetAdapter(
    private val items: List<AttributeSheetItem>
): RecyclerView.Adapter<AttributeSheetAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val binding = ItemAttributeSheetBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val item = items[position]
        holder.binding.apply {
            imgIcon.setImageResource(item.icon)
            tvText.text = item.text
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class ViewHolder(val binding: ItemAttributeSheetBinding): RecyclerView.ViewHolder(binding.root)
}