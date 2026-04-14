package com.nlhd.appperformance.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.nlhd.appperformance.Domain.Entity.Video.Comment
import com.nlhd.appperformance.R
import com.nlhd.appperformance.databinding.ItemCommentBinding

class CommentPagerAdapter: PagingDataAdapter<Comment, CommentPagerAdapter.CommentViewHolder>(DiffCallback) {
    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Comment>() {
            override fun areItemsTheSame(
                oldItem: Comment,
                newItem: Comment
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: Comment,
                newItem: Comment
            ): Boolean {
                return oldItem == newItem
            }

        }
    }
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CommentViewHolder {
        val binding = ItemCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CommentViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: CommentViewHolder,
        position: Int
    ) {
        val comment = getItem(position) ?: return
        holder.binding.tvComment.text = comment.content
        holder.binding.tvUser.text = comment.user.name
        Glide.with(holder.binding.ivAvatar).load(comment.user.avatarUrl).error(R.drawable.asus).into(holder.binding.ivAvatar)
    }

    inner class CommentViewHolder(val binding: ItemCommentBinding): RecyclerView.ViewHolder(binding.root)
}