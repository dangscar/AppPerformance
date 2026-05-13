package com.nlhd.appperformance.Adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.nlhd.appperformance.Fragment.InboxFragment
import com.nlhd.appperformance.Fragment.MainContainerFragment
import com.nlhd.appperformance.Fragment.ProfileFragment
import com.nlhd.appperformance.Fragment.ProfileUpFragment
import com.nlhd.appperformance.Fragment.ShopFragment

class MainPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> MainContainerFragment()
            1 -> ProfileFragment()
            else -> MainContainerFragment()
        }
    }
}
