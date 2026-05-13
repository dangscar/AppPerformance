package com.nlhd.appperformance.Fragment

import android.graphics.Color
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
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.tabs.TabLayoutMediator
import com.nlhd.appperformance.Adapter.IconTextLayout.CategoryItem
import com.nlhd.appperformance.Adapter.IconTextLayout.ItemCategoryAdapter
import com.nlhd.appperformance.Adapter.ProfilePagerAdapter
import com.nlhd.appperformance.Adapter.Shop.FlashSaleAdapter
import com.nlhd.appperformance.Adapter.Shop.Product
import com.nlhd.appperformance.Adapter.Shop.ProductPagerAdapter
import com.nlhd.appperformance.R
import com.nlhd.appperformance.Utils.Navigation
import com.nlhd.appperformance.ViewModel.MainViewModel
import com.nlhd.appperformance.databinding.FragmentProfileUpBinding
import dagger.hilt.android.AndroidEntryPoint
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

    private val mainViewModel: MainViewModel by viewModels()
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
                binding.appBarLayout2.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    topMargin = it.top
                }
                binding.viewPager.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    bottomMargin = it.bottom
                }
            }
        }

        mainViewModel.setNavigation(Navigation.Profile)
        requireActivity().window.statusBarColor = Color.WHITE
        requireActivity().window.navigationBarColor = ContextCompat.getColor(requireContext(), R.color.white)

        binding.viewPager.adapter = ProfilePagerAdapter(requireActivity())
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.icon = when (position) {
                0 -> ContextCompat.getDrawable(requireContext(), R.drawable.ic_video)
                1 -> ContextCompat.getDrawable(requireContext(), R.drawable.repost)
                2 -> ContextCompat.getDrawable(requireContext(), R.drawable.lock)
                3 -> ContextCompat.getDrawable(requireContext(), R.drawable.ic_bookmark_border)
                else -> ContextCompat.getDrawable(requireContext(), R.drawable.ic_favorite_border)
            }
        }.attach()

    }
}