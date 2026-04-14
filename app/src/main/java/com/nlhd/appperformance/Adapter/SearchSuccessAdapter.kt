package com.nlhd.appperformance.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.nlhd.appperformance.Domain.Entity.Video.Video
import com.nlhd.appperformance.R

class SearchSuccessAdapter(
    val onClickCard: (Int) -> Unit
): PagingDataAdapter<Video,SearchSuccessAdapter.SearchSuccessViewHolder>(DiffCallback) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SearchSuccessViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_feed_card, parent, false)
        return SearchSuccessViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: SearchSuccessViewHolder,
        position: Int
    ) {
       val video = getItem(position)!!
        Glide.with(holder.ivThumb).load(video.thumbnailUrl).into(holder.ivThumb)
        holder.tvTitle.text = video.caption
        holder.tvUser.text = video.user.name
        holder.tvViews.text = video.views.toString()
        holder.cvVideo.setOnClickListener {
            onClickCard(position)
        }

    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Video>() {
            override fun areItemsTheSame(
                oldItem: Video,
                newItem: Video
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: Video,
                newItem: Video
            ): Boolean {
                return oldItem == newItem
            }

        }
    }



    inner class SearchSuccessViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val ivThumb: ImageView = view.findViewById(R.id.ivThumb)
        val ivAvatar: ImageView = view.findViewById(R.id.ivAvatar)
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val tvUser: TextView = view.findViewById(R.id.tvUser)
        val tvViews: TextView = view.findViewById(R.id.tvViews)
        val cvVideo: CardView = view.findViewById(R.id.cv_video)
    }
}