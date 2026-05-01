package com.nlhd.appperformance.Fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.GridLayoutManager
import com.nlhd.appperformance.Adapter.SearchSuccessAdapter
import com.nlhd.appperformance.DetailVideoActivity
import com.nlhd.appperformance.ViewModel.SearchSuccessViewModel
import com.nlhd.appperformance.databinding.FragmentSearchTopBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SearchFeedFragment : Fragment() {

    private var _binding: FragmentSearchTopBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: SearchSuccessAdapter
    private val viewModel: SearchSuccessViewModel by viewModels()
    private var keyword: String = ""

    companion object {
        fun newInstance(keyword: String): SearchFeedFragment {
            val fragment = SearchFeedFragment()
            val args = Bundle()
            args.putString("keyword", keyword)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchTopBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        keyword = arguments?.getString("keyword") ?: ""

        setupTabs()
        setupRecyclerView()
        observeViewModel()
    }

    private fun setupTabs() {
        val filters = listOf("Tất cả", "Chưa xem", "Đã xem", "Được tải lên gần đây")
        filters.forEach { filter ->
            binding.tabLayoutFilter.addTab(binding.tabLayoutFilter.newTab().setText(filter))
        }
    }

    private fun setupRecyclerView() {
        adapter = SearchSuccessAdapter(
            onClickCard = { position ->
                val intent = Intent(requireContext(), DetailVideoActivity::class.java)
                intent.putExtra("position", position)
                intent.putExtra("keyword", keyword)
                startActivity(intent)
            }
        )

        binding.rvFeed.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = this@SearchFeedFragment.adapter
            setHasFixedSize(true)
        }

        adapter.addLoadStateListener { loadStates ->
            val isLoading = loadStates.refresh is LoadState.Loading
            val isError = loadStates.refresh is LoadState.Error

            binding.loadingView.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.rvFeed.visibility = if (isLoading) View.GONE else View.VISIBLE
            binding.llError.visibility = if (isError) View.VISIBLE else View.GONE
        }

        binding.errorView.setOnClickListener {
            adapter.retry()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.videos(keyword).collectLatest { pagingData ->
                adapter.submitData(pagingData)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
