package com.nlhd.appperformance.Adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.nlhd.appperformance.Domain.Entity.VideoStore
import com.nlhd.appperformance.R
import com.nlhd.appperformance.databinding.ItemVideoStoreBinding

class VideoStorePagingAdapter(
    private val onClickCard: (Int) -> Unit
): PagingDataAdapter<VideoStore, VideoStorePagingAdapter.VideoStoreViewHolder>(diffCallback) {
    override fun getItemViewType(position: Int): Int {
        return VIEW_TYPE
    }
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): VideoStoreViewHolder {
        val binding = ItemVideoStoreBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VideoStoreViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: VideoStoreViewHolder,
        position: Int
    ) {
        val video = getItem(position)
        video?.let {
            holder.binding.apply {
                /*this.tvName.text = it.name
                this.tvDuration.text = it.duration.toString()*/
                Glide.with(holder.binding.ivThumb).load(video.uri).error(R.drawable.asus).into(holder.binding.ivThumb)
            }
        }

        holder.binding.llItem.setOnClickListener {
            onClickCard(position)
        }
    }

    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<VideoStore>() {
            override fun areItemsTheSame(
                oldItem: VideoStore,
                newItem: VideoStore
            ): Boolean {
                return oldItem.uri == newItem.uri
            }

            override fun areContentsTheSame(
                oldItem: VideoStore,
                newItem: VideoStore
            ): Boolean {
                return oldItem == newItem
            }

        }

        const val VIEW_TYPE = 2000
    }
    inner class VideoStoreViewHolder(val binding: ItemVideoStoreBinding): RecyclerView.ViewHolder(binding.root)
}