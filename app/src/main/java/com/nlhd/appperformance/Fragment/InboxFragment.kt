package com.nlhd.appperformance.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnLayout
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import com.nlhd.appperformance.Adapter.InboxMessageAdapter
import com.nlhd.appperformance.Adapter.InboxMessageItem
import com.nlhd.appperformance.Adapter.StoryAdapter
import com.nlhd.appperformance.Adapter.StoryItem
import com.nlhd.appperformance.R
import com.nlhd.appperformance.Utils.Navigation
import com.nlhd.appperformance.ViewModel.MainViewModel
import com.nlhd.appperformance.databinding.FragmentInboxBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class InboxFragment : Fragment() {

    private lateinit var _binding: FragmentInboxBinding
    private val binding get() = _binding

    private val mainViewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentInboxBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().window.navigationBarColor = ContextCompat.getColor(requireContext(), R.color.white)

        view.doOnLayout {
            val insets = ViewCompat.getRootWindowInsets(view)
                ?.getInsets(WindowInsetsCompat.Type.systemBars())

            insets?.let {
                binding.topBar.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    topMargin = it.top
                }
            }
        }
        //setupStories()
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
        binding.rvStories.adapter = StoryAdapter(stories)
    }

    private fun setupInbox() {
        val messages = listOf(
            InboxMessageItem("Hoạt động", "Soo🎀 đã chấp thuận yêu cầu follow ...",
                R.drawable.facebook, showArrow = true),
            InboxMessageItem("Những Follower mới", "Soo🎀 đã bắt đầu follow bạn.",
                R.drawable.coin, showArrow = true),
            InboxMessageItem("Đối tác nổi bật", "Hãy khám phá đối tác mới nh... · 2 ngày",
                R.drawable.x, showArrow = true, badgeCount = 2),
            InboxMessageItem("Minh Huy", "Bạn đã gửi một nhãn dán · 1 tuần",
                R.drawable.pin, isOnline = true),
            InboxMessageItem("Heli", "Bạn đã gửi một nhãn dán · 1 tuần", R.drawable.`in`, isOnline = true),
            InboxMessageItem("Thông báo hệ thống", "Đã cập nhật kết quả báo cáo · 1 tuần",
                R.drawable.facebook, showArrow = true),
            InboxMessageItem("Ánh", "đã gửi một nhãn dán · 05-05", R.drawable.coin),
            InboxMessageItem("Dư Điển", "giúp mình một tym vs ạ🥰 · 03-09", R.drawable.x),
            InboxMessageItem("Nhật Anh Nè", "Hãy chào Nhật Anh Nè · 01-23", R.drawable.pin),
            InboxMessageItem("ngonnguyen621", "Hãy chào ngonnguyen621 · 01-18", R.drawable.`in`, isOnline = true)
        )
        binding.rvInbox.adapter = InboxMessageAdapter(messages)
    }

    override fun onResume() {
        super.onResume()
        mainViewModel.setNavigation(Navigation.Inbox)
    }
}
