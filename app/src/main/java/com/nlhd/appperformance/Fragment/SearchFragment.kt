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
            RecentSearchItem("aura"),
            RecentSearchItem("edit"),
            RecentSearchItem("nội dung hay")
        )
        binding.rvRecentSearch.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = RecentSearchAdapter(recentItems)
        }
    }

    private fun setupYouMightLike() {
        val suggestions = listOf(
            SearchSuggestionItem("official", R.drawable.asus),
            SearchSuggestionItem("Dangkt", R.drawable.asus),
            SearchSuggestionItem("Vlog", R.drawable.asus, isHighlighted = false),
            SearchSuggestionItem("Hướng dẫn làm video", R.drawable.asus, isHighlighted = false),
            SearchSuggestionItem("Hài lòng", R.drawable.asus, isHighlighted = false)
        )
        binding.rvYouMightLike.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = SearchSuggestionAdapter(suggestions)
        }
    }

    private fun setupTrending() {
        val trendingItems = listOf(
            SearchTrendingItem("Doraemon đến Việt Nam", isHot = true),
            SearchTrendingItem("phim", isHot = true),
            SearchTrendingItem("oke ", isHot = true),
            SearchTrendingItem("đời sống"),
            SearchTrendingItem("bị cáo xét xử"),
            SearchTrendingItem("Bài Hát")
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