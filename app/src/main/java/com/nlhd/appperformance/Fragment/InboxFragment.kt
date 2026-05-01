package com.nlhd.appperformance.Fragment

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.nlhd.appperformance.Adapter.InboxMessageAdapter
import com.nlhd.appperformance.Adapter.InboxMessageItem
import com.nlhd.appperformance.Adapter.StoryAdapter
import com.nlhd.appperformance.Adapter.StoryItem
import com.nlhd.appperformance.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class InboxFragment : Fragment(R.layout.fragment_inbox) {

    private lateinit var rvStories: RecyclerView
    private lateinit var rvInbox: RecyclerView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().window.navigationBarColor = ContextCompat.getColor(requireContext(), R.color.white)
        rvStories = view.findViewById(R.id.rvStories)
        rvInbox = view.findViewById(R.id.rvInbox)

        setupStories()
        setupInbox()
    }

    private fun setupStories() {
        val stories = listOf(
            StoryItem("Quay", R.drawable.asus, hasAddBadge = true),
            StoryItem("Kỷ niệm xưa", R.drawable.asus, isOnline = true),
            StoryItem("Nạn nhân c...", R.drawable.asus),
            StoryItem("Décor lovely", R.drawable.asus),
            StoryItem("User 5", R.drawable.asus),
            StoryItem("User 6", R.drawable.asus)
        )
        rvStories.adapter = StoryAdapter(stories)
    }

    private fun setupInbox() {
        val messages = listOf(
            InboxMessageItem("Những Follower mới", "Nhật Anh Nè đã bắt đầu follow bạn.", R.drawable.friend),
            InboxMessageItem("Hoạt động", "2 người đã xem hồ sơ của bạn.", R.drawable.ic_heart),
            InboxMessageItem("Minh Huy", "Hoạt động 2 giờ trước", R.drawable.asus, showCamera = true),
            InboxMessageItem("Thông báo hệ thống", "LIVE: Mở khóa phiên L... · 2 ngày trước", R.drawable.ic_inbox, isUnread = true),
            InboxMessageItem("Dư Điển", "giúp mình một tym vs ạ🥰 · 9 tháng 3", R.drawable.asus, showCamera = true),
            InboxMessageItem("Nhật Anh Nè", "Hãy chào Nhật Anh Nè", R.drawable.asus, showCamera = true),
            InboxMessageItem("ngonnguyen621", "Hãy chào ngonnguyen621", R.drawable.asus, showCamera = true),
            InboxMessageItem("Yêu cầu tin nhắn", "Bạn nhận được 2 yêu cầu", R.drawable.ic_chat, showArrow = true),
            InboxMessageItem("Thông báo hệ thống", "LIVE: Mở khóa phiên L... · 2 ngày trước", R.drawable.ic_inbox, isUnread = true),
            InboxMessageItem("Dư Điển", "giúp mình một tym vs ạ🥰 · 9 tháng 3", R.drawable.asus, showCamera = true),
            InboxMessageItem("Nhật Anh Nè", "Hãy chào Nhật Anh Nè", R.drawable.asus, showCamera = true),
            InboxMessageItem("ngonnguyen621", "Hãy chào ngonnguyen621", R.drawable.asus, showCamera = true),
            InboxMessageItem("Yêu cầu tin nhắn", "Bạn nhận được 2 yêu cầu", R.drawable.ic_chat, showArrow = true)
        )
        rvInbox.adapter = InboxMessageAdapter(messages)
    }
}