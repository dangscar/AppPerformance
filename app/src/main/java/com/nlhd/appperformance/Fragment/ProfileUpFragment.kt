package com.nlhd.appperformance.Fragment

import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.doOnLayout
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayoutMediator
import com.nlhd.appperformance.Adapter.IconTextLayout.CategoryItem
import com.nlhd.appperformance.Adapter.IconTextLayout.ItemCategoryAdapter
import com.nlhd.appperformance.Adapter.ProfilePagerAdapter
import com.nlhd.appperformance.Adapter.Shop.FlashSaleAdapter
import com.nlhd.appperformance.Adapter.Shop.Product
import com.nlhd.appperformance.Adapter.Shop.ProductPagerAdapter
import com.nlhd.appperformance.Domain.Entity.GetUser.User
import com.nlhd.appperformance.R
import com.nlhd.appperformance.Utils.Navigation
import com.nlhd.appperformance.Utils.ResultUI
import com.nlhd.appperformance.ViewModel.MainViewModel
import com.nlhd.appperformance.ViewModel.ProfileViewModel
import com.nlhd.appperformance.databinding.FragmentProfileUpBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.getValue

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ProfileUpFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
class ProfileUpFragment : Fragment() {

    private val mainViewModel: MainViewModel by activityViewModels()
    private val viewModel: ProfileViewModel by viewModels()
    private var _binding: FragmentProfileUpBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        view.doOnLayout {
            val insets = ViewCompat.getRootWindowInsets(view)
                ?.getInsets(WindowInsetsCompat.Type.systemBars())

            insets?.let {
                binding.appBarLayout.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    topMargin = it.top
                }
                binding.viewPager.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    bottomMargin = it.bottom
                }
            }
        }

        val colorMatrix = ColorMatrix(
            floatArrayOf(
                1.1f, 0f, 0f, 0f, 20f,  // Red
                0f, 1.1f, 0f, 0f, 20f,  // Green
                0f, 0f, 1.1f, 0f, 20f,  // Blue
                0f, 0f, 0f, 1.1f, 0f      // Alpha
            )
        )

        binding.ivAvatar.colorFilter = ColorMatrixColorFilter(colorMatrix)

        mainViewModel.setNavigation(Navigation.Profile)
        requireActivity().window.statusBarColor = Color.WHITE
        requireActivity().window.navigationBarColor = ContextCompat.getColor(requireContext(), R.color.white)

        val adapter = object : FragmentStateAdapter(requireActivity()) {
            override fun getItemCount(): Int = 5
            override fun createFragment(position: Int): Fragment {
                return when (position) {
                    0 -> MyVideoFragment()
                    1 -> VideoGridFragment()
                    2 -> FriendFragment()
                    3 -> FriendFragment()
                    else -> FriendFragment()
                }
            }
        }
        binding.viewPager.adapter = adapter
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.icon = when (position) {
                0 -> ContextCompat.getDrawable(requireContext(), R.drawable.ic_video)
                1 -> ContextCompat.getDrawable(requireContext(), R.drawable.repost)
                2 -> ContextCompat.getDrawable(requireContext(), R.drawable.lock)
                3 -> ContextCompat.getDrawable(requireContext(), R.drawable.ic_bookmark_border)
                else -> ContextCompat.getDrawable(requireContext(), R.drawable.ic_favorite_border)
            }
        }.attach()

        lifecycleScope.launch {
            viewModel.userState.collect { userPreference ->
                if (userPreference.isLoggedIn && userPreference.token.isNotEmpty()) {
                    viewModel.getUser(userPreference.token)
                    Glide.with(requireContext()).load(userPreference.avatarUrl).error(R.drawable.asus).into(binding.ivAvatar)
                    binding.tvName.text = userPreference.name
                    binding.tvUsername.text = userPreference.email
                }
            }
        }

        binding.appBarLayout.addOnOffsetChangedListener({ appBarLayout, verticalOffset ->
            val ratio = (
                    kotlin.math.abs(verticalOffset).toFloat() /
                            binding.topBar.height
                    ).coerceIn(0f, 1f)

            binding.topBar.setBackgroundColor(
                Color.argb(
                    (255 * ratio).toInt(),
                    255,
                    255,
                    255
                )
            )

        }
        )

        viewModel.state.observe(viewLifecycleOwner) {
            when (it) {
                is ResultUI.Error<*> -> {
                    setText()
                }
                ResultUI.Idle -> {}
                ResultUI.Loading -> {}
                is ResultUI.Success<*> -> {
                    setText(it.data as User)
                }
            }
        }

    }

    fun setText() {
        binding.tvFollowed.text = "-"
        binding.tvFollower.text = "-"
        binding.tvLike.text = "-"
    }

    fun setText(user: User) {
        binding.tvName.text = user.name
        binding.tvUsername.text = user.email
        binding.tvFollowed.text = user.followingsCount.toString()
        binding.tvFollower.text = user.followersCount.toString()
        binding.tvLike.text = user.receivedLikesCount.toString()
    }

    override fun onResume() {
        super.onResume()
        mainViewModel.setNavigation(Navigation.Profile)
    }
}