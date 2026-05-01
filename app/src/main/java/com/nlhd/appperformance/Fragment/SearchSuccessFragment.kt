package com.nlhd.appperformance.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.google.android.material.tabs.TabLayoutMediator
import com.nlhd.appperformance.Adapter.SearchPagerAdapter
import com.nlhd.appperformance.databinding.FragmentSearchSuccessBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchSuccessFragment : Fragment() {

    private var _binding: FragmentSearchSuccessBinding? = null
    private val binding get() = _binding!!
    private val args: SearchSuccessFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchSuccessBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val pagerAdapter = SearchPagerAdapter(this, args.keyword)
        binding.viewPager.adapter = pagerAdapter

        val tabTitles = listOf("Top", "Video", "Ảnh", "Người dùng", "Cửa hàng", "Âm thanh")
        
        TabLayoutMediator(binding.tabLayoutMain, binding.viewPager) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}