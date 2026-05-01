package com.nlhd.appperformance.Fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.nlhd.appperformance.Adapter.*
import com.nlhd.appperformance.R
import com.nlhd.appperformance.databinding.FragmentSearchBinding

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecentSearch()
        setupYouMightLike()
        setupTrending()
    }

    private fun setupRecentSearch() {
        val recentItems = listOf(
            RecentSearchItem("chao vlog meme"),
            RecentSearchItem("triệt râu quai nón"),
            RecentSearchItem("himass bắn giải")
        )
        binding.rvRecentSearch.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = RecentSearchAdapter(recentItems)
        }
    }

    private fun setupYouMightLike() {
        val suggestions = listOf(
            SearchSuggestionItem("cao thủ liên quân official", R.drawable.asus),
            SearchSuggestionItem("duy nến", R.drawable.asus),
            SearchSuggestionItem("Meme Sang Vlog", R.drawable.asus, isHighlighted = false),
            SearchSuggestionItem("Râu Quai Nón", R.drawable.asus, isHighlighted = false),
            SearchSuggestionItem("vlog một ngày đi học của chao", R.drawable.asus, isHighlighted = false)
        )
        binding.rvYouMightLike.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = SearchSuggestionAdapter(suggestions)
        }
    }

    private fun setupTrending() {
        val trendingItems = listOf(
            SearchTrendingItem("Doraemon đến Việt Nam", isHot = true),
            SearchTrendingItem("tiểu tam bóng ma hạnh phúc", isHot = true),
            SearchTrendingItem("Anh Trai Say Hi Concert Day 9", isHot = true),
            SearchTrendingItem("Đâu Có Ai Nhìn HIEUTHUHAI"),
            SearchTrendingItem("bị cáo xét xử"),
            SearchTrendingItem("Bài Hát Bất Khổ Nguyệt Lân Ỷ Kỳ")
        )
        binding.rvTrending.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = SearchTrendingAdapter(trendingItems)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}