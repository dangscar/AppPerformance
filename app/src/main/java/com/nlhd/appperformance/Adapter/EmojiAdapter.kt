package com.nlhd.appperformance.Adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nlhd.appperformance.databinding.ItemEmojiBinding

class EmojiAdapter(
    private val list: List<Int>,
    private val onClick: (Int) -> Unit
) : RecyclerView.Adapter<EmojiAdapter.EmojiViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): EmojiViewHolder {
        val binding = ItemEmojiBinding.inflate(android.view.LayoutInflater.from(parent.context), parent, false)
        return EmojiViewHolder(binding)
    }

    inner class EmojiViewHolder(val binding: ItemEmojiBinding) : RecyclerView.ViewHolder(binding.root)


    override fun onBindViewHolder(holder: EmojiViewHolder, position: Int) {
        val emoji = list[position]
        holder.binding.imgEmoji.setImageResource(emoji)

        holder.itemView.setOnClickListener {
            onClick(emoji)
        }
    }

    override fun getItemCount(): Int = list.size
}