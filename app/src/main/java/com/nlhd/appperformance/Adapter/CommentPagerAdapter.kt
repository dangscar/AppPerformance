package com.nlhd.appperformance.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.nlhd.appperformance.Domain.Entity.Video.Comment
import com.nlhd.appperformance.R
import com.nlhd.appperformance.databinding.ItemCommentBinding

class CommentPagerAdapter : PagingDataAdapter<Comment, CommentPagerAdapter.CommentViewHolder>(DiffCallback) {

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Comment>() {
            override fun areItemsTheSame(oldItem: Comment, newItem: Comment): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Comment, newItem: Comment): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val binding = ItemCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CommentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = getItem(position) ?: return
        holder.bind(comment)
    }

    inner class CommentViewHolder(val binding: ItemCommentBinding) : RecyclerView.ViewHolder(binding.root) {
        private var replyAdapter: ReplyAdapter? = null

        fun bind(comment: Comment) {
            binding.tvUser.text = comment.user.name
            binding.tvComment.text = comment.content
            Glide.with(binding.ivAvatar)
                .load(comment.user.avatarUrl)
                .error(R.drawable.asus)
                .into(binding.ivAvatar)

            // Setup nested RecyclerView for replies
            if (comment.replies!!.isNotEmpty()) {
                binding.tvViewComment.text = if (comment.isExpanded!!) "Ẩn phản hồi" else "Xem ${comment.replies.size} phản hồi"
                binding.rvReplies.visibility = if (comment.isExpanded!!) View.VISIBLE else View.GONE
                
                if (replyAdapter == null) {
                    replyAdapter = ReplyAdapter(comment.replies)
                    binding.rvReplies.layoutManager = LinearLayoutManager(itemView.context)
                    binding.rvReplies.adapter = replyAdapter
                } else {
                    replyAdapter?.updateData(comment.replies)
                }
            } else {
                binding.rvReplies.visibility = View.GONE
            }

            binding.tvViewComment.setOnClickListener {
                if (comment.replies.isNotEmpty()) {
                    comment.isExpanded = !comment.isExpanded!!
                    notifyItemChanged(absoluteAdapterPosition)
                } else {
                    // Logic for replying to a comment (showing keyboard/input)
                }
            }
        }
    }
}
