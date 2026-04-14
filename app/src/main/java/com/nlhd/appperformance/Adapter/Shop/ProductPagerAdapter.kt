package com.nlhd.appperformance.Adapter.Shop

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.nlhd.appperformance.Fragment.ProductFragment

class ProductPagerAdapter(
    activity: FragmentActivity
): FragmentStateAdapter(activity) {
    override fun createFragment(position: Int): Fragment {
        return ProductFragment()
    }

    override fun getItemCount(): Int {
        return 4
    }

}