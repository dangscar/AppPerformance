package com.nlhd.appperformance.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.nlhd.appperformance.R

data class SearchTrendingItem(
    val title: String,
    val subTitle: String = "Lượng tương tác cao",
    val isHot: Boolean = false
)

class SearchTrendingAdapter(private val items: List<SearchTrendingItem>) :
    RecyclerView.Adapter<SearchTrendingAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val tvSubTitle: TextView = view.findViewById(R.id.tvSubTitle)
        val ivTrend: ImageView = view.findViewById(R.id.ivTrend)
        val vDot: View = view.findViewById(R.id.vDot)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_search_trending, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvTitle.text = item.title
        holder.tvSubTitle.text = item.subTitle
        
        if (item.isHot) {
            holder.vDot.visibility = View.VISIBLE
            holder.ivTrend.setImageResource(R.drawable.ic_arrow_up)
            holder.ivTrend.visibility = View.VISIBLE
        } else {
            holder.vDot.visibility = View.GONE
            holder.ivTrend.visibility = View.GONE
        }
    }

    override fun getItemCount() = items.size
}