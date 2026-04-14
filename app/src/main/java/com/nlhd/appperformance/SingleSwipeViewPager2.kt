package com.nlhd.appperformance

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2

class SingleSwipeViewPager2 @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ViewPager(context, attrs) {

    private var swipeHandled = false

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        // Chặn multi-touch
        if (ev.pointerCount > 1) return false

        if (ev.actionMasked == MotionEvent.ACTION_DOWN) {
            swipeHandled = false
        }

        return super.onInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        // Chặn multi-touch
        if (ev.pointerCount > 1) return false

        when (ev.actionMasked) {
            MotionEvent.ACTION_MOVE -> {
                if (swipeHandled) return false
            }

            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL -> {
                swipeHandled = true
            }
        }
        return super.onTouchEvent(ev)
    }
}
