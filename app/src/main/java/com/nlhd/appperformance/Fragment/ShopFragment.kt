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
import com.nlhd.appperformance.Adapter.Shop.FlashSaleAdapter
import com.nlhd.appperformance.Adapter.Shop.Product
import com.nlhd.appperformance.Adapter.Shop.ProductPagerAdapter
import com.nlhd.appperformance.R
import com.nlhd.appperformance.Utils.Navigation
import com.nlhd.appperformance.ViewModel.MainViewModel
import com.nlhd.appperformance.databinding.FragmentShopBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ShopFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
class ShopFragment : Fragment() {

    private val mainViewModel: MainViewModel by viewModels()
    private var _binding: FragmentShopBinding? = null
    private val binding get() = _binding!!
    private lateinit var productPagerAdapter: ProductPagerAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentShopBinding.inflate(inflater, container, false)
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
        productPagerAdapter = ProductPagerAdapter(requireActivity())
        val list = listOf(
            CategoryItem(R.drawable.ic_order, "Đơn hàng"),
            CategoryItem(R.drawable.ic_local_activity, "HÀNG VIỆT"),
            CategoryItem(R.drawable.ic_location, "Vị trí"),
            CategoryItem(R.drawable.ic_help, "Trợ giúp"),
            CategoryItem(R.drawable.support, "Chính sách"),
            CategoryItem(R.drawable.liveshow, "Trực tiếp"),
        )
        val listFlashSale = listOf(
            Product("32.000đ", "https://images2.thanhnien.vn/528068263637045248/2024/3/29/3-skincare-17116985744571787565621.jpg"),
            Product("32.000đ", "https://www.sieuthimaychu.vn/datafiles/setone/17058975609003.jpg"),
            Product("32.000đ", "https://mstarcorp.vn/wp-content/uploads/2025/03/Thumbnail-san-pham-DS1825-1.png"),
            Product("324.000đ", "https://cdn.tgdd.vn/Products/Images/42/301798/samsung-galaxy-s23-plus-5-600x600.jpg"),
        )
        binding.rvCategory.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = ItemCategoryAdapter(list)
        }

        binding.rvFlashSale.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = FlashSaleAdapter(listFlashSale)
        }

        binding.viewPager.adapter = productPagerAdapter
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Tất cả"
                1 -> "Quần áo nữ"
                2 -> "Mỹ phẩm"
                else -> "Chăm sóc cá nhân"
            }
        }.attach()

    }
}