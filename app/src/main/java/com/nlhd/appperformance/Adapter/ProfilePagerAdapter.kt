package com.nlhd.appperformance.Adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.nlhd.appperformance.Fragment.FollowingFragment
import com.nlhd.appperformance.Fragment.FriendFragment
import com.nlhd.appperformance.Fragment.VideoGridFragment

class ProfilePagerAdapter(
    activity: FragmentActivity
): FragmentStateAdapter(activity) {
    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> VideoGridFragment()
            1 -> FriendFragment()
            2 -> FriendFragment()
            3 -> FriendFragment()
            else -> FriendFragment()
        }
    }

    override fun getItemCount(): Int {
        return 5
    }
}