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
    val showArrow: Boolean = false,
    val isOnline: Boolean = false,
    val badgeCount: Int = 0
)

class InboxMessageAdapter(private val items: List<InboxMessageItem>) :
    RecyclerView.Adapter<InboxMessageAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivAvatar: ImageView = view.findViewById(R.id.ivAvatar)
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val tvDescription: TextView = view.findViewById(R.id.tvDescription)
        val ivAction: ImageView = view.findViewById(R.id.ivAction)
        val vOnlineStatus: View = view.findViewById(R.id.vOnlineStatus)
        val tvBadge: TextView = view.findViewById(R.id.tvBadge)
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

        holder.vOnlineStatus.visibility = if (item.isOnline) View.VISIBLE else View.GONE
        
        if (item.badgeCount > 0) {
            holder.tvBadge.visibility = View.VISIBLE
            holder.tvBadge.text = item.badgeCount.toString()
            holder.ivAction.visibility = View.GONE
        } else {
            holder.tvBadge.visibility = View.GONE
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
    }

    override fun getItemCount() = items.size
}