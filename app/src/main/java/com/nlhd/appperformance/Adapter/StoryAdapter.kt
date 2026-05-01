package com.nlhd.appperformance.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.nlhd.appperformance.R

data class StoryItem(
    val name: String,
    val imageRes: Int,
    val hasAddBadge: Boolean = false,
    val isOnline: Boolean = false
)

class StoryAdapter(private val items: List<StoryItem>) :
    RecyclerView.Adapter<StoryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivAvatar: ImageView = view.findViewById(R.id.ivAvatar)
        val ivBadge: ImageView = view.findViewById(R.id.ivBadge)
        val vOnlineStatus: View = view.findViewById(R.id.vOnlineStatus)
        val tvName: TextView = view.findViewById(R.id.tvName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_inbox_story, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvName.text = item.name
        holder.ivAvatar.setImageResource(item.imageRes)
        
        holder.ivBadge.visibility = if (item.hasAddBadge) View.VISIBLE else View.GONE
        holder.vOnlineStatus.visibility = if (item.isOnline) View.VISIBLE else View.GONE
    }

    override fun getItemCount() = items.size
}