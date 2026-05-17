package com.nlhd.appperformance.Fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.OptIn
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.media3.common.util.UnstableApi
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.nlhd.appperformance.Adapter.HomePagerAdapter
import com.nlhd.appperformance.R
import com.nlhd.appperformance.Activity.SearchActivity
import com.nlhd.appperformance.ViewModel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import androidx.core.graphics.toColorInt
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnLayout
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import com.nlhd.appperformance.Utils.Navigation
import com.nlhd.appperformance.Utils.TabSelected
import com.nlhd.appperformance.Utils.tabs

@AndroidEntryPoint
class HomeFragment: Fragment(R.layout.fragment_home) {
    private lateinit var viewPager: ViewPager2
    private lateinit var topBar: LinearLayout
    private lateinit var tabLayout: TabLayout
    private lateinit var fontBold: Typeface
    private lateinit var fontSemiBold: Typeface
    private lateinit var iv_search: ImageView
    private lateinit var iv_refresh: ImageView
    private val mainViewModel: MainViewModel by activityViewModels()

    private lateinit var homePagerAdapter: HomePagerAdapter

    private fun centerSelectedTab(tabLayout: TabLayout, tab: TabLayout.Tab?) {
        if (tab == null) return

        val tabStrip = tabLayout.getChildAt(0) as? ViewGroup ?: return
        val tabView = tabStrip.getChildAt(tab.position) ?: return

        val scrollX =
            tabView.left - (tabLayout.width - tabView.width) / 2

        tabLayout.smoothScrollTo(scrollX, 0)
    }

    private fun updateTabColor(tabLayout: TabLayout, selectedPos: Int) {
        for (i in 0 until tabLayout.tabCount) {
            val tab = tabLayout.getTabAt(i) ?: continue
            val tv = tab.customView as? TextView ?: continue
            if (i != tabLayout.tabCount - 1 && selectedPos != tabLayout.tabCount - 1 && selectedPos != tabLayout.tabCount-3) {
                tv.setTextColor(Color.BLACK)
                iv_refresh.imageTintList = ColorStateList.valueOf(Color.BLACK)
                iv_search.imageTintList = ColorStateList.valueOf(Color.BLACK)
            } else if (i == tabLayout.tabCount - 1 && selectedPos != tabLayout.tabCount - 1 && selectedPos != tabLayout.tabCount-3) {
                tv.setTextColor(Color.BLACK)
                iv_refresh.imageTintList = ColorStateList.valueOf(Color.BLACK)
                iv_search.imageTintList = ColorStateList.valueOf(Color.BLACK)
            }
            else {
                tv.setTextColor("#B3FFFFFF".toColorInt())
                iv_refresh.imageTintList = ColorStateList.valueOf(Color.WHITE)
                iv_search.imageTintList = ColorStateList.valueOf(Color.WHITE)
            }
            if (i == tabLayout.tabCount-3 && (selectedPos == tabLayout.tabCount-3)) {
                tv.setTextColor(Color.WHITE)
                iv_refresh.imageTintList = ColorStateList.valueOf(Color.WHITE)
                iv_search.imageTintList = ColorStateList.valueOf(Color.WHITE)
            }
            if (i == tabLayout.tabCount-1 && (selectedPos == tabLayout.tabCount-1)) {
                tv.setTextColor(Color.WHITE)
                iv_refresh.imageTintList = ColorStateList.valueOf(Color.WHITE)
                iv_search.imageTintList = ColorStateList.valueOf(Color.WHITE)
            }

        }
    }

    // Extension
    fun Int.dpToPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()

    @SuppressLint("ResourceType", "ClickableViewAccessibility")
    @OptIn(UnstableApi::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        viewPager = view.findViewById(R.id.vpTabs)
        topBar = view.findViewById(R.id.topBar)
        tabLayout = view.findViewById(R.id.tabLayout)
        fontBold = ResourcesCompat.getFont(requireContext(), R.font.tiktoksans_bold)!!
        fontSemiBold = ResourcesCompat.getFont(requireContext(), R.font.tiktoksans_semibold)!!
        iv_search = view.findViewById(R.id.ivSearch)
        iv_refresh = view.findViewById(R.id.ivRefresh)

        // Fix Nested ViewPager2 Scrolling
        val recyclerView = viewPager.getChildAt(0) as RecyclerView
        recyclerView.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                when (e.action) {
                    MotionEvent.ACTION_DOWN -> {
                        // Khi bắt đầu chạm vào ViewPager con, yêu cầu cha không chặn
                        if (e.x < viewPager.width-100.dpToPx()) {
                            rv.parent.requestDisallowInterceptTouchEvent(true)
                        } else {
                            rv.parent.requestDisallowInterceptTouchEvent(false)

                        }


                    }
                }
                return false
            }
            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}
            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
        })

        view.doOnLayout {
            val insets = ViewCompat.getRootWindowInsets(view)
                ?.getInsets(WindowInsetsCompat.Type.systemBars())

            insets?.let {
                topBar.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    topMargin = it.top  // ✅ margin top = status bar height
                }
            }
        }

        homePagerAdapter = HomePagerAdapter(requireActivity())

        viewPager.adapter = homePagerAdapter
        viewPager.offscreenPageLimit = 2

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = tabs[position]
            val tv = LayoutInflater.from(requireContext()).inflate(R.layout.item_tab_home, null) as TextView
                        tv.text = tabs[position]
                        tv.scaleY = 1.25f
                        tv.typeface = fontSemiBold
            tv.setShadowLayer(
                3f,
                0f,
                3f,
                "#20000000".toColorInt() // đen mờ
            )
                        tab.customView = tv
            //Padding text tablayout
            tabLayout.post {
                val tabStrip = tabLayout.getChildAt(0) as ViewGroup
                for (i in 0 until tabStrip.childCount) {
                    val tabView = tabStrip.getChildAt(i)
                    val startPadding = if (i == 0) 22 else 1
                    val endPadding = if (i == tabStrip.childCount - 1) 22 else 1
                    tabView.minimumWidth = 0
                    tabView.setPadding(startPadding, 0, endPadding, 0)
                }
            }

        }.attach()

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                centerSelectedTab(tabLayout, tab)
                val tv = tab?.customView as? TextView ?: return
                tv.typeface = fontBold
                tv.isSelected = true

                //Thiết lập vị trí tab
                mainViewModel.setCurrentTab(true)

                if (tab.position == tabs.size - 1) {
                    //Màu đen cho bottomBar
                    mainViewModel.setColorBottomNav(Color.BLACK)
                    tabLayout.setSelectedTabIndicatorColor(ContextCompat.getColor(requireContext(), R.color.white))
                    colorSystemBars(R.color.black)
                } else if (tab.position == tabs.size - 3) {
                    mainViewModel.setColorBottomNav(Color.BLACK)
                    tabLayout.setSelectedTabIndicatorColor(ContextCompat.getColor(requireContext(), R.color.white))
                    colorSystemBars(R.color.black)
                }
                else {
                    //Màu trắng cho bottomBar
                    mainViewModel.setColorBottomNav(Color.WHITE)
                    tabLayout.setSelectedTabIndicatorColor(ContextCompat.getColor(requireContext(), R.color.black))
                    colorSystemBars(R.color.white)
                }
                val tabStrip = tabLayout.getChildAt(0) as ViewGroup
                for (i in 0 until tabStrip.childCount) {
                    val tabView = tabStrip.getChildAt(i)
                    val startPadding = if (i == 0) 22 else 1
                    val endPadding = if (i == tabStrip.childCount - 1) 22 else 1
                    tabView.minimumWidth = 0
                    tabView.setPadding(startPadding, 0, endPadding, 0)
                }
                updateTabColor(tabLayout, tab.position)

                when (tab.position) {
                    4 -> { mainViewModel.setTabSelected(TabSelected.Suggestions) }
                    2 -> { mainViewModel.setTabSelected(TabSelected.Following) }
                    else -> { mainViewModel.setTabSelected(TabSelected.Explore) }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                val tv = tab?.customView as? TextView ?: return
                tv.typeface = fontSemiBold
                tv.isSelected = false
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

        })

        //Giữ trạng thái tab current hiện tại
        mainViewModel.currentTab.observe(viewLifecycleOwner) {
            if (!it) {
                viewPager.setCurrentItem(tabs.size-1, false)
            }
        }

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)

                val blendedColor = when {
                    position == 3 && positionOffset > 0.5f -> {
                        updateTabColor(tabLayout, position+1)
                        Color.BLACK
                    }
                    position == 3 && positionOffset <= 0.5f -> {
                        updateTabColor(tabLayout, position)
                        val blend = positionOffset / 0.5f
                        interpolateColor(Color.WHITE, Color.BLACK, blend)
                    }
                    position == 2 && positionOffset >= 0.5f -> {
                        updateTabColor(tabLayout, position)
                        val blend = (positionOffset - 0.5f) / 0.5f
                        interpolateColor(Color.BLACK, Color.WHITE, blend)
                    }
                    else -> {
                        Color.BLACK
                    }
                }

                mainViewModel.setColorBottomNav(blendedColor)
            }
        })



        iv_search.setOnClickListener {
            val intent = Intent(requireContext(), SearchActivity::class.java)
            startActivity(intent)
        }

        iv_refresh.setOnClickListener {
            mainViewModel.setRefresh(true)
        }

        val isLandscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        mainViewModel.showBar.observe(viewLifecycleOwner) {
            topBar.visibility = if (it && !isLandscape) View.VISIBLE else View.GONE
        }


    }

    private fun colorSystemBars(color: Int) {
        requireActivity().window.navigationBarColor = ContextCompat.getColor(requireContext(), color)
        requireActivity().window.statusBarColor = Color.TRANSPARENT
    }

    private fun interpolateColor(startColor: Int, endColor: Int, fraction: Float): Int {
        val startA = Color.alpha(startColor)
        val startR = Color.red(startColor)
        val startG = Color.green(startColor)
        val startB = Color.blue(startColor)

        val endA = Color.alpha(endColor)
        val endR = Color.red(endColor)
        val endG = Color.green(endColor)
        val endB = Color.blue(endColor)

        val blendedA = (startA + (endA - startA) * fraction).toInt()
        val blendedR = (startR + (endR - startR) * fraction).toInt()
        val blendedG = (startG + (endG - startG) * fraction).toInt()
        val blendedB = (startB + (endB - startB) * fraction).toInt()

        return Color.argb(blendedA, blendedR, blendedG, blendedB)
    }


    @OptIn(UnstableApi::class)
    fun refreshData() {

    }

}
