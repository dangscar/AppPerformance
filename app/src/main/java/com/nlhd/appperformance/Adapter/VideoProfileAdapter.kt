package com.nlhd.appperformance.Adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.nlhd.appperformance.Adapter.VideoPagerAdapter.Companion.PAYLOAD_FOLLOW
import com.nlhd.appperformance.Domain.Entity.Video.Video
import com.nlhd.appperformance.Domain.Entity.VideoStore
import com.nlhd.appperformance.R
import com.nlhd.appperformance.databinding.ItemVideoStoreBinding

class VideoProfileAdapter(
    private val onClickCard: (Int) -> Unit
): PagingDataAdapter<Video, VideoProfileAdapter.VideoProfileViewHolder>(diffCallback) {
    override fun getItemViewType(position: Int): Int {
        return VIEW_TYPE
    }
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): VideoProfileViewHolder {
        val binding = ItemVideoStoreBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VideoProfileViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: VideoProfileViewHolder,
        position: Int
    ) {
        val video = getItem(position)
        video?.let {
            holder.binding.apply {
                /*this.tvName.text = it.name
                this.tvDuration.text = it.duration.toString()*/
                Glide.with(holder.binding.ivThumb).load(video.thumbnailUrl).error(R.drawable.asus).into(holder.binding.ivThumb)
            }
        }

        holder.binding.llItem.setOnClickListener {
            onClickCard(position)
        }
    }

    fun updateAllFollowState(
        isFollowing: String
    ) {

        snapshot().items.forEach { video ->

            video.isFollowing = isFollowing
        }

        notifyItemRangeChanged(
            0,
            snapshot().items.size,
            PAYLOAD_FOLLOW
        )
    }

    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<Video>() {
            override fun areItemsTheSame(
                oldItem: Video,
                newItem: Video
            ): Boolean {
                return oldItem.videoUrl == newItem.videoUrl
            }

            override fun areContentsTheSame(
                oldItem: Video,
                newItem: Video
            ): Boolean {
                return oldItem == newItem
            }

        }

        const val VIEW_TYPE = 2000
        private val PAYLOAD_FOLLOW = "payload_follow"
    }
    inner class VideoProfileViewHolder(val binding: ItemVideoStoreBinding): RecyclerView.ViewHolder(binding.root)
}