package com.nlhd.appperformance.Adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.nlhd.appperformance.Fragment.SearchFeedFragment

class SearchPagerAdapter(fragment: Fragment, private val keyword: String) : FragmentStateAdapter(fragment) {
    private val tabs = listOf("Top", "Video", "Ảnh", "Người dùng", "Cửa hàng", "Âm thanh")

    override fun getItemCount(): Int = tabs.size

    override fun createFragment(position: Int): Fragment {
        // Hiện tại tất cả các tab đều dùng chung logic hiển thị grid video để demo
        return SearchFeedFragment.newInstance(keyword)
    }
}
