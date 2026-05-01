package com.nlhd.appperformance.ThuNghiem

import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2

class TwoFingerScrollHelper(private val viewPager: ViewPager2) {

    private var lastPage = viewPager.currentItem

    init {
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                // Chặn kéo quá 1 trang khi đang scroll
                val diff = position - lastPage
                if (diff > 1) {
                    viewPager.setCurrentItem(lastPage + 1, false)
                } else if (diff < -1) {
                    viewPager.setCurrentItem(lastPage - 1, false)
                }
            }

            override fun onPageSelected(position: Int) {
                lastPage = position
            }
        })
    }
}