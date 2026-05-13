package com.nlhd.appperformance.Fragment

import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.annotation.OptIn
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnLayout
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.nlhd.appperformance.Adapter.ProfilePagerAdapter
import com.nlhd.appperformance.Domain.Entity.GetUser.User
import com.nlhd.appperformance.Feature.LoginScreen.LoginBottomSheet
import com.nlhd.appperformance.R
import com.nlhd.appperformance.Utils.Navigation
import com.nlhd.appperformance.Utils.ResultUI
import com.nlhd.appperformance.ViewModel.MainViewModel
import com.nlhd.appperformance.ViewModel.ProfileViewModel
import com.nlhd.appperformance.databinding.FragmentProfileBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
@AndroidEntryPoint
class ProfileFragment: Fragment(R.layout.fragment_profile) {
    private lateinit var profilePagerAdapter: ProfilePagerAdapter

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels()
    private val mainViewModel: MainViewModel by activityViewModels()
    private val bottomSheet: LoginBottomSheet = LoginBottomSheet()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return _binding!!.root
    }

    fun Int.dpToPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()

    @OptIn(UnstableApi::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.doOnLayout {
            val insets = ViewCompat.getRootWindowInsets(view)
                ?.getInsets(WindowInsetsCompat.Type.systemBars())

            insets?.let {
                binding.appBarLayout.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    topMargin = it.top
                }
                binding.cdlProfile.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    bottomMargin = it.bottom
                }
            }
        }

        profilePagerAdapter = ProfilePagerAdapter(requireActivity())
        binding.viewPager.adapter = profilePagerAdapter
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.icon = when (position) {
                0 -> ContextCompat.getDrawable(requireContext(), R.drawable.ic_video)
                1 -> ContextCompat.getDrawable(requireContext(), R.drawable.repost)
                2 -> ContextCompat.getDrawable(requireContext(), R.drawable.lock)
                3 -> ContextCompat.getDrawable(requireContext(), R.drawable.ic_bookmark_border)
                else -> ContextCompat.getDrawable(requireContext(), R.drawable.ic_favorite_border)
            }
        }.attach()

        // Fix Nested ViewPager2 Scrolling
        val recyclerView = binding.viewPager.getChildAt(0) as RecyclerView
        var startX = 0f
        recyclerView.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                when (e.action) {
                    MotionEvent.ACTION_DOWN -> {
                        startX = e.x // Lưu điểm bắt đầu chạm
                        rv.parent.requestDisallowInterceptTouchEvent(false)
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val deltaX = e.x - startX
                        /*
                        * Delta < 0 vuốt sang phải
                        * Delta > 0 vuốt sang trái
                        * */
                        when {
                            deltaX < 0 -> {
                                rv.parent.requestDisallowInterceptTouchEvent(true)
                            }
                            deltaX > 0 -> {
                                if (binding.viewPager.currentItem == 0) {
                                    rv.parent.requestDisallowInterceptTouchEvent(false)
                                } else {
                                    rv.parent.requestDisallowInterceptTouchEvent(true)
                                }

                            }
                        }
                    }

                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        startX = 0f
                    }
                }
                return false
            }
            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}
            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
        })

        //Nếu người dùng
        lifecycleScope.launch {
            viewModel.userState.collect { userPreference ->
                if (!userPreference.isLoggedIn) {
                    bottomSheet.show(requireActivity().supportFragmentManager, LoginBottomSheet::class.java.simpleName)
                } else {
                    if (userPreference.token.isNotEmpty()){
                        viewModel.getUser(userPreference.token)
                    }
                }
            }
        }

        viewModel.state.observe(viewLifecycleOwner) {
            when (it) {
                is ResultUI.Error<*> -> {
                    setText()
                }
                is ResultUI.Success<*> -> {
                    val user = (it as ResultUI.Success).data
                    setText(user)
                }
                else -> {}
            }
        }

        binding.btnBack.setOnClickListener {
            mainViewModel.setBackPressed(true)
        }

        requireActivity().onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                mainViewModel.setBackPressed(true)
            }
        })

        // Scroll to Top đối với appBarLayout
        mainViewModel.scrollToTop.observe(viewLifecycleOwner) { shouldScroll ->
            if (shouldScroll) {
                scrollToTop()
                mainViewModel.resetScrollToTop()
            }
        }
    }

    fun setText(user: User) {
        binding.tvName.text = user.name
        binding.tvUsername.text = user.email
        binding.tvFollowed.text = user.followingsCount.toString()
        binding.tvFollower.text = user.followersCount.toString()
        binding.tvLike.text = user.receivedLikesCount.toString()
        Glide.with(requireContext()).load(user.avatarUrl ?: R.drawable.asus).error(R.drawable.asus).into(binding.ivAvatar)
    }

    fun setText() {
        binding.tvName.text = "Unknown"
        binding.tvUsername.text = "@username"
        binding.tvFollowed.text = "0"
        binding.tvFollower.text = "0"
        binding.tvLike.text = "0"
    }


    fun scrollToTop() {
        binding.appBarLayout.setExpanded(true, false)
    }

}