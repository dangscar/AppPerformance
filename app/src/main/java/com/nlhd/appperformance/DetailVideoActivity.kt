package com.nlhd.appperformance

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.media3.exoplayer.ExoPlayer
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.nlhd.appperformance.Fragment.DetailVideoFragment
import com.nlhd.appperformance.Fragment.ProfileFragment
import com.nlhd.appperformance.Utils.Navigation
import com.nlhd.appperformance.ViewModel.GeneralViewModel
import com.nlhd.appperformance.ViewModel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DetailVideoActivity : AppCompatActivity() {
    private lateinit var viewPager: ViewPager2
    private val viewModel: MainViewModel by viewModels()

    @Inject
    lateinit var players: MutableMap<Int, ExoPlayer>
    private var id = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detail_video)

        viewPager = findViewById(R.id.viewPagerMain)

        val adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = 2
            override fun createFragment(position: Int): Fragment {
                return when (position) {
                    0 -> DetailVideoFragment()
                    else -> ProfileFragment()
                }
            }
        }
        
        viewPager.adapter = adapter
        viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (viewPager.currentItem > 0) {
                    viewPager.currentItem = 0
                } else {
                    if (players.isNotEmpty()) {
                        players.values.forEach { player ->
                            player.stop()
                            player.clearMediaItems()
                            player.release()
                        }
                        players.clear()
                    }
                    finish()
                    overridePendingTransition(
                        R.anim.slide_in_left,
                        R.anim.slide_out_right
                    )
                }
            }
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

        viewModel.idProfile.observe(this) {
            if (it != id) {
                viewModel.triggerScrollToTop()  //Top đối với AppBarLayout
                viewModel.setScrollToTopRecyclerView(true) //Top đối với VideoGridFragment
                id = it
            }
        }

        viewModel.backPressed.observe(this) {
            if (it && viewPager.currentItem == 1) {
                viewPager.currentItem = 0
                viewModel.setBackPressed(false)
            } else if (it && viewPager.currentItem == 0) {
                onBackPressedDispatcher.onBackPressed()
                viewModel.setBackPressed(false)
                if (players.isNotEmpty()) {
                    players.values.forEach { player ->
                        player.stop()
                        player.clearMediaItems()
                        player.release()
                    }
                    players.clear()
                }
                finish()
                overridePendingTransition(
                    R.anim.slide_in_left,
                    R.anim.slide_out_right
                )

            }
        }

        val isUserInputEnable = intent.getBooleanExtra("isUserInputEnable", true)
        viewPager.isUserInputEnabled = isUserInputEnable
    }

    fun switchToProfile() {
        viewPager.currentItem = 1
    }

}