package com.nlhd.appperformance.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.nlhd.appperformance.R

data class InboxMessageItem(
    val title: String,
    val description: String,
    val imageRes: Int,
    val isUnread: Boolean = false,
    val showCamera: Boolean = false,
    val showArrow: Boolean = false
)

class InboxMessageAdapter(private val items: List<InboxMessageItem>) :
    RecyclerView.Adapter<InboxMessageAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivAvatar: ImageView = view.findViewById(R.id.ivAvatar)
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val tvDescription: TextView = view.findViewById(R.id.tvDescription)
        val ivAction: ImageView = view.findViewById(R.id.ivAction)
        val vUnreadIndicator: View = view.findViewById(R.id.vUnreadIndicator)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_inbox_message, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvTitle.text = item.title
        holder.tvDescription.text = item.description
        holder.ivAvatar.setImageResource(item.imageRes)

        holder.vUnreadIndicator.visibility = if (item.isUnread) View.VISIBLE else View.GONE
        
        if (item.showCamera) {
            holder.ivAction.visibility = View.VISIBLE
            holder.ivAction.setImageResource(R.drawable.ic_camera)
        } else if (item.showArrow) {
            holder.ivAction.visibility = View.VISIBLE
            holder.ivAction.setImageResource(R.drawable.ic_arrow_forward)
        } else {
            holder.ivAction.visibility = View.GONE
        }
    }

    override fun getItemCount() = items.size
}