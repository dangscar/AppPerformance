package com.nlhd.appperformance.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.nlhd.appperformance.R

data class SearchSuggestionItem(
    val title: String,
    val imageRes: Int,
    val isHighlighted: Boolean = true
)

class SearchSuggestionAdapter(private val items: List<SearchSuggestionItem>) :
    RecyclerView.Adapter<SearchSuggestionAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val ivThumbnail: ImageView = view.findViewById(R.id.ivThumbnail)
        val vDot: View = view.findViewById(R.id.vDot)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_search_suggestion, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvTitle.text = item.title
        holder.ivThumbnail.setImageResource(item.imageRes)
        
        if (item.isHighlighted) {
            holder.tvTitle.setTextColor(0xFFFE2C55.toInt())
            holder.vDot.visibility = View.VISIBLE
        } else {
            holder.tvTitle.setTextColor(0xFF000000.toInt())
            holder.vDot.visibility = View.GONE
        }
    }

    override fun getItemCount() = items.size
}