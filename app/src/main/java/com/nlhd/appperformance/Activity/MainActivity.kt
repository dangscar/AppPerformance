package com.nlhd.appperformance.Activity

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.nlhd.appperformance.ViewModel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import androidx.media3.common.util.UnstableApi
import com.nlhd.appperformance.Fragment.HomeFragment
import com.nlhd.appperformance.R
import com.nlhd.appperformance.Utils.Navigation

@AndroidEntryPoint
class MainActivity: AppCompatActivity() {
    private val viewModel: MainViewModel by viewModels()

    @OptIn(UnstableApi::class)
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val splashScreen = installSplashScreen()
        setContentView(R.layout.activity_main)

        // Giữ splash nếu cần load dữ liệu
        splashScreen.setKeepOnScreenCondition {
            false // đổi thành true nếu đang loading
        }

        val isLandscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
        viewModel.navigation.observe(this) {
            val color = if (it == Navigation.Home) R.color.black else R.color.white
            window.statusBarColor = ContextCompat.getColor(this, color)
            window.navigationBarColor = ContextCompat.getColor(this, color)
            WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false
        }


        WindowCompat.setDecorFitsSystemWindows(window, false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }

        val root = findViewById<View>(R.id.main)
        /*ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val cutout = insets.getInsets(WindowInsetsCompat.Type.displayCutout())
            v.setPadding(maxOf(systemBars.left, cutout.left), 0, systemBars.right, systemBars.bottom)
            insets
        }*/

        val navHost = findViewById<View>(R.id.nav_host)
        ViewCompat.setOnApplyWindowInsetsListener(navHost) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                systemBars.left,
                systemBars.top, // ❌ không padding top để cho phép tràn lên status bar
                systemBars.right,
                0 // ❌ nếu bạn muốn full màn hình (video)
            )
            insets
        }

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host) as NavHostFragment
        val navController = navHostFragment.navController
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        val fabAdd = findViewById<ImageView>(R.id.fabAdd)
        //val dividerBottomBar = findViewById<MaterialDivider>(R.id.dvd_bottombar)
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
                }
            }
        }

        bottomNav.setOnItemReselectedListener { item ->

            if (item.itemId == R.id.homeFragment) {

                val navHostFragment =
                    supportFragmentManager.findFragmentById(R.id.nav_host)

                val currentFragment =
                    navHostFragment?.childFragmentManager?.fragments?.firstOrNull()

                if (currentFragment is HomeFragment) {
                    currentFragment.refreshData()
                }
            }
        }

        viewModel.colorBottomNav.observe(this) {
            val selected = if (it == Color.BLACK) "#FFFFFF" else "#000000"
            val colors = ColorStateList(
                arrayOf(
                    intArrayOf(android.R.attr.state_checked),
                    intArrayOf(-android.R.attr.state_checked)
                ),
                intArrayOf(
                    Color.parseColor(selected), // selected
                    Color.parseColor("#888888")  // unselected
                )
            )

            bottomNav.itemIconTintList = colors
            bottomNav.itemTextColor = colors
            bottomNav.setBackgroundColor(it)
            if (it == Color.BLACK) {
                fabAdd.setImageResource(R.drawable.ic_addwhite)
            } else {
                fabAdd.setImageResource(R.drawable.addblack_enhanced)
            }
            //dividerBottomBar.dividerColor = if (it == Color.BLACK) Color.parseColor("#98282727") else Color.parseColor("#98DEDDDD")
        }


        viewModel.showBar.observe(this) {
            if (it) {
                bottomNav.alpha = 1f
                fabAdd.alpha = 1f
                //dividerBottomBar.alpha = 1f
            } else {
                bottomNav.alpha = 0f
                fabAdd.alpha = 0f
                //dividerBottomBar.alpha = 0f
            }
        }
        viewModel.isLandscape.observe(this) {
            if (it) {
                bottomNav.visibility = View.GONE
                fabAdd.visibility = View.GONE
                //dividerBottomBar.visibility = View.GONE
            } else {
                bottomNav.visibility = View.VISIBLE
                fabAdd.visibility = View.VISIBLE
                //dividerBottomBar.visibility = View.VISIBLE
            }
        }

        fabAdd.setOnClickListener {
            val intent = Intent(this, UploadVideoActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

    }

}