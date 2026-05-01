package com.nlhd.appperformance.Adapter

import androidx.annotation.OptIn
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.media3.common.util.UnstableApi
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.nlhd.appperformance.Fragment.ExploreFragment
import com.nlhd.appperformance.Fragment.FollowingFragment
import com.nlhd.appperformance.Fragment.ForYouFragment
import com.nlhd.appperformance.Fragment.FriendFragment
import com.nlhd.appperformance.Utils.TabSelected

@UnstableApi
class HomePagerAdapter(activity: FragmentActivity): FragmentStateAdapter(activity) {
    val forYouFragment = ForYouFragment()
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> FriendFragment()
            1-> FriendFragment()
            2-> FollowingFragment()
            3-> ExploreFragment()
            else -> forYouFragment
        }
    }

    fun refreshData() {
        forYouFragment.refreshData()
    }

    override fun getItemCount(): Int {
        return 5
    }
}