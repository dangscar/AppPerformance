package com.nlhd.appperformance.Fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.nlhd.appperformance.Activity.UploadVideoActivity
import com.nlhd.appperformance.R
import com.nlhd.appperformance.Utils.Navigation
import com.nlhd.appperformance.ViewModel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainContainerFragment : Fragment() {

    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main_container, container, false)
    }

    @SuppressLint("ResourceAsColor", "ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navHostFragment = childFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment
        val navController = navHostFragment.navController
        val bottomNav = view.findViewById<BottomNavigationView>(R.id.bottomNav)
        val fabAdd = view.findViewById<ImageView>(R.id.fabAdd)


        bottomNav.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.homeFragment -> {
                    val colors = ColorStateList(
                        arrayOf(
                            intArrayOf(android.R.attr.state_checked),
                            intArrayOf(-android.R.attr.state_checked)
                        ),
                        intArrayOf(
                            Color.parseColor("#FFFFFF"), // selected
                            Color.parseColor("#888888")  // unselected
                        )
                    )

                    bottomNav.itemIconTintList = colors
                    bottomNav.itemTextColor = colors
                    bottomNav.setBackgroundColor(Color.BLACK)
                    fabAdd.setImageResource(R.drawable.ic_addwhite)
                    viewModel.setNavigation(Navigation.Home)
                    bottomNav.menu.findItem(R.id.homeFragment).setIcon(R.drawable.home)
                    bottomNav.menu.findItem(R.id.inboxFragment).setIcon(R.drawable.inbox)
                    bottomNav.menu.findItem(R.id.profileFragment).setIcon(R.drawable.profile)
                }
                R.id.inboxFragment -> {
                    val colors = ColorStateList(
                        arrayOf(
                            intArrayOf(android.R.attr.state_checked),
                            intArrayOf(-android.R.attr.state_checked)
                        ),
                        intArrayOf(
                            Color.parseColor("#000000"), // selected
                            Color.parseColor("#888888")  // unselected
                        )
                    )
                    bottomNav.itemIconTintList = colors
                    bottomNav.itemTextColor = colors
                    bottomNav.setBackgroundColor(Color.WHITE)
                    fabAdd.setImageResource(R.drawable.addblack_enhanced)
                    viewModel.setNavigation(Navigation.Profile)
                    bottomNav.menu.findItem(R.id.homeFragment).setIcon(R.drawable.home_white)
                    bottomNav.menu.findItem(R.id.inboxFragment).setIcon(R.drawable.inboxwhite)
                    bottomNav.menu.findItem(R.id.profileFragment).setIcon(R.drawable.profile)
                }
                R.id.shopFragment -> {
                    val colors = ColorStateList(
                        arrayOf(
                            intArrayOf(android.R.attr.state_checked),
                            intArrayOf(-android.R.attr.state_checked)
                        ),
                        intArrayOf(
                            Color.parseColor("#000000"), // selected
                            Color.parseColor("#888888")  // unselected
                        )
                    )
                    bottomNav.itemIconTintList = colors
                    bottomNav.itemTextColor = colors
                    bottomNav.setBackgroundColor(Color.WHITE)
                    fabAdd.setImageResource(R.drawable.addblack_enhanced)
                    viewModel.setNavigation(Navigation.Profile)
                    bottomNav.menu.findItem(R.id.homeFragment).setIcon(R.drawable.home_white)
                    bottomNav.menu.findItem(R.id.inboxFragment).setIcon(R.drawable.inbox)
                    bottomNav.menu.findItem(R.id.profileFragment).setIcon(R.drawable.profile)
                }
                R.id.profileFragment -> {
                    val colors = ColorStateList(
                        arrayOf(
                            intArrayOf(android.R.attr.state_checked),
                            intArrayOf(-android.R.attr.state_checked)
                        ),
                        intArrayOf(
                            Color.parseColor("#000000"), // selected
                            Color.parseColor("#888888")  // unselected
                        )
                    )
                    bottomNav.itemIconTintList = colors
                    bottomNav.itemTextColor = colors
                    bottomNav.setBackgroundColor(Color.WHITE)
                    fabAdd.setImageResource(R.drawable.addblack_enhanced)
                    viewModel.setNavigation(Navigation.Profile)
                    bottomNav.menu.findItem(R.id.homeFragment).setIcon(R.drawable.home_white)
                    bottomNav.menu.findItem(R.id.inboxFragment).setIcon(R.drawable.inbox)
                    bottomNav.menu.findItem(R.id.profileFragment).setIcon(R.drawable.profilewhite)
                }
            }
        }

        bottomNav.setOnItemReselectedListener { item ->
            if (item.itemId == R.id.homeFragment) {
                val currentFragment = navHostFragment.childFragmentManager.fragments.firstOrNull()
                if (currentFragment is HomeFragment) {
                    currentFragment.refreshData()
                }
            }

//            bottomNav.menu.findItem(R.id.shopFragment).setIcon(R.drawable.shop)
//            bottomNav.menu.findItem(R.id.inboxFragment).setIcon(R.drawable.ic_inbox)
//            bottomNav.menu.findItem(R.id.profileFragment).setIcon(R.drawable.ic_profile)

            // Set icon active cho item được chọn
            /*when (item.itemId) {
                R.id.homeFragment -> item.setIcon(R.drawable.home_white)
                R.id.shopFragment -> item.setIcon(R.drawable.ic_shop_active)
                R.id.inboxFragment -> item.setIcon(R.drawable.ic_inbox_active)
                R.id.profileFragment -> item.setIcon(R.drawable.ic_profile_active)
            }*/
        }

        viewModel.colorBottomNav.observe(viewLifecycleOwner) {
            val selected = if (it == Color.BLACK) "#FFFFFF" else "#000000"
            val colors = ColorStateList(
                arrayOf(
                    intArrayOf(android.R.attr.state_checked),
                    intArrayOf(-android.R.attr.state_checked)
                ),
                intArrayOf(
                    Color.parseColor(selected),
                    Color.parseColor("#888888")
                )
            )

            bottomNav.setBackgroundColor(it)

            if (it == Color.BLACK) {
                fabAdd.setImageResource(R.drawable.ic_addwhite)
            } else {
                fabAdd.setImageResource(R.drawable.addblack_enhanced)
            }

            // ✅ Set tintList SAU CÙNG → trigger redraw icon
            bottomNav.itemIconTintList = colors
            bottomNav.itemTextColor = colors
        }

        viewModel.showBar.observe(viewLifecycleOwner) {
            if (it) {
                bottomNav.alpha = 1f
                fabAdd.alpha = 1f
            } else {
                bottomNav.alpha = 0f
                fabAdd.alpha = 0f
            }
        }
        
        viewModel.isLandscape.observe(viewLifecycleOwner) {
            if (it) {
                bottomNav.visibility = View.GONE
                fabAdd.visibility = View.GONE
            } else {
                bottomNav.visibility = View.VISIBLE
                fabAdd.visibility = View.VISIBLE
            }
        }

        fabAdd.setOnClickListener {
            val intent = Intent(requireContext(), UploadVideoActivity::class.java)
            startActivity(intent)
        }
    }

    fun Int.dpToPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()
}
