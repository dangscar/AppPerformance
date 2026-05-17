package com.nlhd.appperformance.Adapter.IconTextLayout

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nlhd.appperformance.databinding.ItemShareUserBinding

data class UserShareItem(
    val name: String,
    val avatar: Int // Resource ID for demo
)

class UserShareAdapter(private val items: List<UserShareItem>) :
    RecyclerView.Adapter<UserShareAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemShareUserBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemShareUserBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.binding.tvUserName.text = item.name
        holder.binding.ivUserAvatar.setImageResource(item.avatar)
    }

    override fun getItemCount() = items.size
}