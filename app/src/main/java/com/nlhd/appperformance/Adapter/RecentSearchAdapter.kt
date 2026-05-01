package com.nlhd.appperformance.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.nlhd.appperformance.R

data class RecentSearchItem(
    val keyword: String
)

class RecentSearchAdapter(private val items: List<RecentSearchItem>) :
    RecyclerView.Adapter<RecentSearchAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvKeyword: TextView = view.findViewById(R.id.tvKeyword)
        val ivDelete: ImageView = view.findViewById(R.id.ivDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recent_search, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvKeyword.text = item.keyword
        holder.ivDelete.setOnClickListener {
            // Handle delete logic
        }
    }

    override fun getItemCount() = items.size
}