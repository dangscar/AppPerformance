package com.nlhd.appperformance.Fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Configuration
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
import androidx.annotation.IdRes
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnLayout
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.nlhd.appperformance.Activity.UploadVideoActivity
import com.nlhd.appperformance.R
import com.nlhd.appperformance.Utils.Navigation
import com.nlhd.appperformance.ViewModel.MainViewModel
import com.nlhd.appperformance.databinding.FragmentMainContainerBinding
import dagger.hilt.android.AndroidEntryPoint
import androidx.core.graphics.toColorInt
import androidx.core.widget.ImageViewCompat

@AndroidEntryPoint
class MainContainerFragment : Fragment() {

    private val viewModel: MainViewModel by activityViewModels()
    private var _binding: FragmentMainContainerBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMainContainerBinding.inflate(inflater, container, false)
        return _binding?.root
    }

    private fun navigateBottom(@IdRes destination: Int, navController: NavController) {

        val currentDestination = navController.currentDestination?.id

        if (currentDestination == destination) {
            return
        }

        navController.navigate(destination, null, NavOptions.Builder()
            .setLaunchSingleTop(true)
            .setRestoreState(true)
            .setPopUpTo(
                navController.graph.findStartDestination().id,
                false,
                true
            )
            .build()
        )
    }

    private fun setBottomBarStyle(
        background: Int,
        homeColor: Int,
        storeColor: Int,
        inboxColor: Int,
        profileColor: Int
    ) {

        binding.bottomBar.setBackgroundColor(background)

        binding.iconHome.setColorFilter(homeColor)
        binding.textHome.setTextColor(homeColor)

        binding.iconStore.setColorFilter(storeColor)
        binding.textStore.setTextColor(storeColor)

        binding.iconInbox.setColorFilter(inboxColor)
        binding.textInbox.setTextColor(inboxColor)

        binding.iconProfile.setColorFilter(profileColor)
        binding.textProfile.setTextColor(profileColor)
    }

    @SuppressLint("ResourceAsColor", "ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navHostFragment = childFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment
        val navController = navHostFragment.navController

        //val bottomNav = view.findViewById<BottomNavigationView>(R.id.bottomNav)

        view.doOnLayout {
            val insets = ViewCompat.getRootWindowInsets(view)
                ?.getInsets(WindowInsetsCompat.Type.systemBars())

            insets?.let {
                binding.bottomBar.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    bottomMargin = it.bottom
                }
            }
        }

        binding.navHome.setOnClickListener {

            navigateBottom(R.id.homeFragment, navController)
        }

        binding.navStore.setOnClickListener {

            navigateBottom(R.id.shopFragment, navController)
        }

        binding.navInbox.setOnClickListener {

            navigateBottom(R.id.inboxFragment, navController)
        }

        binding.navProfile.setOnClickListener {

            navigateBottom(R.id.profileFragment, navController)
        }

        viewModel.navigation.observe(viewLifecycleOwner) {
            when (it) {
                Navigation.Home -> {
                    setBottomBarStyle(
                        background = Color.BLACK,
                        homeColor = Color.WHITE,
                        storeColor = "#888888".toColorInt(),
                        inboxColor = "#888888".toColorInt(),
                        profileColor = "#888888".toColorInt()
                    )
                    binding.iconHome.setImageResource(R.drawable.home)
                    binding.iconStore.setImageResource(R.drawable.ic_explore)
                    binding.iconInbox.setImageResource(R.drawable.inbox)
                    binding.iconProfile.setImageResource(R.drawable.ic_profile)
                    binding.fabAdd.setImageResource(R.drawable.ic_addwhite)
                }
                Navigation.Shop -> {
                    setBottomBarStyle(
                        background = Color.WHITE,
                        homeColor = "#888888".toColorInt(),
                        storeColor = Color.BLACK,
                        inboxColor = "#888888".toColorInt(),
                        profileColor = "#888888".toColorInt()
                    )
                    binding.iconHome.setImageResource(R.drawable.home_white)
                    binding.iconStore.setImageResource(R.drawable.ic_explore)
                    binding.iconInbox.setImageResource(R.drawable.inbox)
                    binding.iconProfile.setImageResource(R.drawable.ic_profile)
                    binding.fabAdd.setImageResource(R.drawable.addblack_new)
                }
                Navigation.Profile -> {
                    setBottomBarStyle(
                        background = Color.WHITE,
                        homeColor = "#888888".toColorInt(),
                        storeColor = "#888888".toColorInt(),
                        inboxColor = "#888888".toColorInt(),
                        profileColor = Color.BLACK
                    )
                    binding.iconHome.setImageResource(R.drawable.home_white)
                    binding.iconStore.setImageResource(R.drawable.ic_explore)
                    binding.iconInbox.setImageResource(R.drawable.inbox)
                    binding.iconProfile.setImageResource(R.drawable.ic_profile)
                    binding.fabAdd.setImageResource(R.drawable.addblack_new)
                }
                Navigation.Inbox -> {
                    setBottomBarStyle(
                        background = Color.WHITE,
                        homeColor = "#888888".toColorInt(),
                        storeColor = "#888888".toColorInt(),
                        inboxColor = Color.BLACK,
                        profileColor = "#888888".toColorInt()
                    )
                    binding.iconHome.setImageResource(R.drawable.home_white)
                    binding.iconStore.setImageResource(R.drawable.ic_explore)
                    binding.iconInbox.setImageResource(R.drawable.inboxwhite)
                    binding.iconProfile.setImageResource(R.drawable.ic_profile)
                    binding.fabAdd.setImageResource(R.drawable.addblack_new)
                }
                Navigation.User -> {}

            }

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


            binding.bottomBar.setBackgroundColor(it)

            if (it == Color.BLACK) {
                binding.fabAdd.setImageResource(R.drawable.ic_addwhite)
                binding.iconHome.setColorFilter(Color.WHITE)
                binding.textHome.setTextColor(Color.WHITE)
            } else {
                binding.fabAdd.setImageResource(R.drawable.addblack_new)
                binding.iconHome.setColorFilter(Color.BLACK)
                binding.textHome.setTextColor(Color.BLACK)
            }
        }

        viewModel.showBar.observe(viewLifecycleOwner) {
            if (it) {
                binding.bottomBar.alpha = 1f
                binding.fabAdd.alpha = 1f
            } else {
                binding.bottomBar.alpha = 0f
                binding.fabAdd.alpha = 0f
            }
        }

        viewModel.isLandscape.observe(viewLifecycleOwner) {
            if (it) {
                binding.bottomBar.visibility = View.GONE
                binding.fabAdd.visibility = View.GONE
            } else {
                binding.bottomBar.visibility = View.VISIBLE
                binding.fabAdd.visibility = View.VISIBLE
            }
        }


        //bottomNav.setupWithNavController(navController)

        /*navController.addOnDestinationChangedListener { _, destination, _ ->
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
                    fabAdd.setImageResource(R.drawable.addblack_new)
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
                    fabAdd.setImageResource(R.drawable.addblack_new)
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
                    fabAdd.setImageResource(R.drawable.addblack_new)
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
            *//*when (item.itemId) {
                R.id.homeFragment -> item.setIcon(R.drawable.home_white)
                R.id.shopFragment -> item.setIcon(R.drawable.ic_shop_active)
                R.id.inboxFragment -> item.setIcon(R.drawable.ic_inbox_active)
                R.id.profileFragment -> item.setIcon(R.drawable.ic_profile_active)
            }*//*
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
                fabAdd.setImageResource(R.drawable.addblack_new)
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
        }*/

        binding.fabAdd.setOnClickListener {
            val intent = Intent(requireContext(), UploadVideoActivity::class.java)
            startActivity(intent)
        }

    }

    fun Int.dpToPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()
}
