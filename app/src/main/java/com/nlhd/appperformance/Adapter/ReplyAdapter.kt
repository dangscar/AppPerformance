package com.nlhd.appperformance.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.nlhd.appperformance.Domain.Entity.Video.Reply
import com.nlhd.appperformance.R
import com.nlhd.appperformance.databinding.ItemReplyBinding

class ReplyAdapter(private var replies: List<Reply>) : RecyclerView.Adapter<ReplyAdapter.ReplyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReplyViewHolder {
        val binding = ItemReplyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReplyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReplyViewHolder, position: Int) {
        val reply = replies[position]
        holder.binding.tvUser.text = reply.user.name
        holder.binding.tvComment.text = reply.content
        holder.binding.tvTime.text = reply.createdAt ?: "Vừa xong"
        Glide.with(holder.binding.ivAvatar)
            .load(reply.user.avatarUrl)
            .error(R.drawable.asus)
            .into(holder.binding.ivAvatar)
    }

    override fun getItemCount(): Int = replies.size

    fun updateData(newReplies: List<Reply>) {
        this.replies = newReplies
        notifyDataSetChanged()
    }

    inner class ReplyViewHolder(val binding: ItemReplyBinding) : RecyclerView.ViewHolder(binding.root)
}
