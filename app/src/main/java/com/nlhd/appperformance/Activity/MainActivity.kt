package com.nlhd.appperformance.Activity

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import androidx.activity.viewModels
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.nlhd.appperformance.Adapter.MainPagerAdapter
import com.nlhd.appperformance.Fragment.ProfileFragment
import com.nlhd.appperformance.R
import com.nlhd.appperformance.Utils.Navigation
import com.nlhd.appperformance.Utils.TabSelected
import com.nlhd.appperformance.ViewModel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModels()
    private lateinit var viewPager: ViewPager2
    private lateinit var ll_play_speed: LinearLayout
    private var id = -1
    fun Int.dpToPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()

    @OptIn(UnstableApi::class)
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val splashScreen = installSplashScreen()
        setContentView(R.layout.activity_main)

        splashScreen.setKeepOnScreenCondition { false }

        window.statusBarColor = Color.TRANSPARENT
        WindowCompat.setDecorFitsSystemWindows(window, false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }

        viewPager = findViewById(R.id.viewPager)

        viewPager.adapter = MainPagerAdapter(this)

        viewModel.navigation.observe(this) {
            if (it == Navigation.Home || it == Navigation.User) {
                viewPager.isUserInputEnabled = true
            } else {
                viewPager.isUserInputEnabled = false
            }
        }
        viewModel.tabSelected.observe(this) {
            if (it == TabSelected.Explore) {
                viewPager.isUserInputEnabled = false
            } else {
                viewPager.isUserInputEnabled = true
            }
        }
         // Cho phép vuốt sang Profile

        val root = findViewById<View>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, 0)
            insets
        }

        val recyclerView = viewPager.getChildAt(0) as RecyclerView
        recyclerView.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
            private var startX = 0f
            private var startY = 0f
            private val CLICK_THRESHOLD = 10.dpToPx()

            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                val bottomNavTop = rv.height - 100.dpToPx()

                when (e.action) {
                    MotionEvent.ACTION_DOWN -> {
                        startX = e.x
                        startY = e.y
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val dx = Math.abs(e.x - startX)
                        val dy = Math.abs(e.y - startY)
                        val isSwiping = dx > CLICK_THRESHOLD || dy > CLICK_THRESHOLD

                        val inBottomNav = e.y > bottomNavTop

                        // Chỉ block khi swipe, không block click
                        if (inBottomNav && isSwiping) return true
                    }
                }
                return false
            }
            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}
            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
        })


        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                when (position) {
                    0 -> {
                        window.navigationBarColor = Color.BLACK
                        viewModel.setNavigation(Navigation.Home)
                    }
                    else -> {
                        window.navigationBarColor = Color.WHITE
                        viewModel.setNavigation(Navigation.User)
                    }
                }
            }
        })

        viewModel.idProfile.observe(this@MainActivity) {
            if (it != id) {
                viewModel.triggerScrollToTop()  //Top đối với AppBarLayout
                viewModel.setScrollToTopRecyclerView(true) //Top đối với VideoGridFragment
                id = it
            }
        }

        viewModel.backPressed.observe(this@MainActivity) {
            if (it && viewPager.currentItem == 1) {
                viewPager.currentItem = 0
                viewModel.setBackPressed(false)
            } else if (it && viewPager.currentItem == 0) {
                onBackPressedDispatcher.onBackPressed()
                viewModel.setBackPressed(false)
                moveTaskToBack(true)
            }
        }

    }
}
